package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.java.JavaToken;
import net.rubygrapefruit.parser.peg.Region;
import net.rubygrapefruit.parser.peg.TokenVisitor;
import net.rubygrapefruit.parser.sample.util.FileCollector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Java source code syntax highlighter that generates HTML from Java source files.
 *
 * Usage: `java Highlighter output-file source-files-or-directories
 */
public class Highlighter {
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        new Highlighter().parseFiles(args);
    }

    private void parseFiles(final String[] args) throws IOException, InterruptedException {
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

        List<File> files = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            files.add(new File(arg));
        }
        long start = System.nanoTime();
        final JavaParser parser = new JavaParser();
        new FileCollector().process(files, new FileCollector.Worker() {
            @Override
            public void handle(File file, String content) throws Exception {
                parse(parser, file, content, out);
            }
        }, 1);
        long end = System.nanoTime();
        out.println("</body>");
        out.println("</html>");
        out.flush();

        System.out.println(String.format("Parsed %d files with %d errors in %dms", count.get(), failed.get(), (end-start)/1000000));
    }

    private void parse(JavaParser parser, File file, String content, final PrintWriter out) {
        count.incrementAndGet();
        out.println("<h1>" + file.getName() + "</h1>");
        out.println("<pre class='code'>");
        parser.parse(content, new TokenVisitor<JavaToken>() {
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
                failed.incrementAndGet();
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
