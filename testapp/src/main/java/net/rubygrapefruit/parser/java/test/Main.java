package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("--gui")) {
            showGui();
        } else {
            parseFiles(args);
        }
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
            System.out.println("-------");
            System.out.println();
        }
    }

    private static void showGui() {
        final JTextPane source = new JTextPane();
        final JTextPane parsed = new JTextPane();

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        source.setFont(font);
        source.getDocument().addDocumentListener(new TextChangeHandler(parsed, source));

        parsed.setFont(font);
        parsed.setEditable(false);

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(source), new JScrollPane(parsed));
        pane.setResizeWeight(0.5);

        JPanel root = new JPanel();
        root.setLayout(new BorderLayout());
        root.add(pane, BorderLayout.CENTER);

        JFrame frame = new JFrame("Parser test");
        frame.setContentPane(root);
        frame.setSize(1000, 800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static class TextChangeHandler implements DocumentListener {
        private final JTextPane source;
        private final Executor executor = Executors.newSingleThreadExecutor();
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private final JavaParser javaParser = new JavaParser();
        private String current;

        TextChangeHandler(JTextPane parsed, JTextPane source) {
            this.source = source;
            executor.execute(new ParseLoop(parsed));
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            onUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            onUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            onUpdate();
        }

        private void onUpdate() {
            lock.lock();
            try {
                current = source.getText();
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        private class ParseLoop implements Runnable {
            private final JTextPane parsed;
            private final Style plain;
            private final Style broken;

            ParseLoop(JTextPane parsed) {
                this.parsed = parsed;
                plain = parsed.addStyle("plain", null);
                broken = parsed.addStyle("broken", null);
                StyleConstants.setForeground(broken, Color.RED);
            }

            @Override
            public void run() {
                while (true) {
                    doOnce();
                }
            }

            private void doOnce() {
                String str;
                lock.lock();
                try {
                    while (current == null) {
                        try {
                            condition.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    str = current;
                    current = null;
                } finally {
                    lock.unlock();
                }

                final ResultCollector collector = new ResultCollector();
                javaParser.parse(str, collector);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Document document = parsed.getDocument();
                            document.remove(0, document.getLength());
                            document.insertString(0, collector.builder.toString(), plain);
                            if (collector.failure != null) {
                                document.insertString(document.getEndPosition().getOffset() - 1, "\n\nFAILED: " + collector.failure,
                                        broken);
                            }
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            private class ResultCollector implements TokenVisitor {
                final StringBuilder builder = new StringBuilder();
                String failure = null;

                @Override
                public void token(String token) {
                    builder.append(token);
                }

                @Override
                public void failed(String message) {
                    failure = message;
                }
            }
        }
    }
}
