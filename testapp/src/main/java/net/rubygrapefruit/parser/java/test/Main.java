package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.peg.TokenVisitor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
        final JTextArea source = new JTextArea();
        final JTextArea parsed = new JTextArea();
        source.getDocument().addDocumentListener(new TextChangeHandler(parsed, source));
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
        private final JTextArea source;
        private final Executor executor = Executors.newSingleThreadExecutor();
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private final JavaParser javaParser = new JavaParser();
        private String current;

        TextChangeHandler(final JTextArea parsed, JTextArea source) {
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
            private final JTextArea parsed;

            ParseLoop(JTextArea parsed) {
                this.parsed = parsed;
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

                final StringBuilder builder = new StringBuilder();
                javaParser.parse(str, new TokenVisitor() {
                    @Override
                    public void token(String token) {
                        builder.append(token);
                    }

                    @Override
                    public void failed(String message) {
                        builder.append("\n\n");
                        builder.append("FAILED: ");
                        builder.append(message);
                    }
                });

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        parsed.setText(builder.toString());
                    }
                });
            }
        }
    }
}
