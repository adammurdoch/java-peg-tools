package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.java.JavaToken;
import net.rubygrapefruit.parser.peg.Region;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Highlighter {
    public static void main(String[] args) throws IOException {
        parseFiles(args);
    }

    private static void parseFiles(String[] args) throws IOException {
        JavaParser parser = new JavaParser();
        for (String arg : args) {
            File file = new File(arg);
            System.out.println("-------");
            System.out.println("PARSING " + file.getName());
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
                        case Whitespace:
                            System.out.print(" ");
                            break;
                        case Comment:
                            System.out.print("/*..*/");
                            break;
                        default:
                            System.out.print(match.getText());
                    }
                }

                @Override
                public void failed(String message, Region remainder) {
                    System.out.println();
                    System.out.println("FAILED: " + message);
                }
            });
            System.out.println();
            System.out.println("-------");
            System.out.println();
        }
    }
}
