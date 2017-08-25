package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.java.JavaToken;
import net.rubygrapefruit.parser.peg.Region;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.io.*;

/**
 * A Java source code syntax highlighter that generates HTML from Java source files.
 *
 * Usage: `java Highlighter output-file source-files-or-directories
 */
public class Highlighter {
    int count;
    int failed;

    public static void main(String[] args) throws IOException {
        new Highlighter().parseFiles(args);
    }

    private void parseFiles(String[] args) throws IOException {
        JavaParser parser = new JavaParser();
        File output = new File(args[0]);
        final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "utf8"));
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<style>");
        out.println("h1 { font-family: sans-serif; font-size: 16pt; }");
        out.println(".code { padding: 10pt; font-size: 11pt; border: 1px solid #a0a0a0; color: rgb(109,109,109); }");
        out.println(".comment { color: rgb(89,136,61); }");
        out.println(".keyword { color: rgb(42,104,136); font-weight: bold; }");
        out.println(".identifier { color: rgb(120,84,165); font-weight: bold; }");
        out.println(".failure { color: red; }");
        out.println(".remainder { color: white; background: red; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        long start = System.nanoTime();
        for (int i = 1; i < args.length; i++) {
            parse(parser, new File(args[i]), out);
        }
        long end = System.nanoTime();
        out.println("</body>");
        out.println("</html>");
        out.flush();

        System.out.println(String.format("Parsed %d files with %d errors in %dms", count, failed, (end-start)/1000000));
    }

    private void parse(JavaParser parser, File file, final PrintWriter out) throws IOException {
        if (file.isFile()) {
            parseFile(parser, file, out);
        } else if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                parse(parser, child, out);
            }
        }
    }

    private void parseFile(JavaParser parser, File file, final PrintWriter out) throws IOException {
        count++;
        out.println("<h1>" + file.getName() + "</h1>");
        out.println("<pre class='code'>");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = new char[4096];
        while (true) {
            int nread = reader.read(chars, 0, chars.length);
            if (nread < 0) {
                break;
            }
            stringBuilder.append(chars, 0, nread);
        }
        parser.parse(stringBuilder.toString(), new TokenVisitor<JavaToken>() {
            @Override
            public void token(JavaToken type, Region match) {
                switch (type) {
                    case Comment:
                        out.print("<span class='comment'>");
                        appendText(match.getText(), out);
                        out.print("</span>");
                        break;
                    case Identifier:
                        out.print("<span class='identifier'>");
                        appendText(match.getText(), out);
                        out.print("</span>");
                        break;
                    case Keyword:
                        out.print("<span class='keyword'>");
                        appendText(match.getText(), out);
                        out.print("</span>");
                        break;
                    default:
                        appendText(match.getText(), out);
                }
            }

            @Override
            public void failed(String message, Region remainder) {
                failed++;
                out.print("<span class='remainder'>");
                appendText(remainder.getText(), out);
                out.println("</span>");
                out.println();
                out.print("<span class='failure'>FAILED: ");
                appendText(message, out);
                out.println("</span>");
            }
        });
        out.println();
        out.println("</pre>");
    }

    private void appendText(String message, PrintWriter out) {
        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (ch == '<') {
                out.append("&lt;");
            } else {
                out.append(ch);
            }
        }
    }
}
