package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        JavaParser parser = new JavaParser();
        for (String arg : args) {
            File file = new File(arg);
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
            parser.parse(stringBuilder.toString(), new TokenVisitor() {
                @Override
                public void token(String token) {
                    if (token.matches("\\s+")) {
                        System.out.print(" ");
                    } else {
                        System.out.print(token);
                    }
                }

                @Override
                public void failed(String message) {
                    System.out.println();
                    System.out.println("FAILED: " + message);
                }
            });
            System.out.println();
        }
    }
}
