package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.peg.Region;
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
        if (args.length >= 1 && args[0].equals("--gui")) {
            showGui(args);
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
                public void token(Region match) {
                    String text = match.getText();
                    if (text.matches("\\s+")) {
                        System.out.print(" ");
                    } else {
                        System.out.print(text);
                    }
                }

                @Override
                public void failed(String message, Region remainder) {
                    System.out.println();
                    System.out.println("FAILED: " + message);
                }
            });
            System.out.println("-------");
            System.out.println();
        }
    }

    private static void showGui(String[] args) throws IOException {
        final Executor executor = Executors.newSingleThreadExecutor();
        ParseQueue queue = new ParseQueue();

        JTextPane source = new JTextPane();
        JTextPane parsed = new JTextPane();
        JTextPane status = new JTextPane();

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        source.setFont(font);
        source.getDocument().addDocumentListener(new TextChangeHandler(source, queue));

        parsed.setFont(font);
        parsed.setEditable(false);

        status.setFont(font);
        status.setEditable(false);

        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        result.add(new JScrollPane(parsed), BorderLayout.CENTER);
        result.add(status, BorderLayout.NORTH);

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(source), result);
        pane.setResizeWeight(0.5);

        JPanel root = new JPanel();
        root.setLayout(new BorderLayout());
        root.add(pane, BorderLayout.CENTER);

        JFrame frame = new JFrame("Parser test");
        frame.setContentPane(root);
        frame.setSize(1000, 800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        executor.execute(new ParseLoop(queue, parsed, status));

        if (args.length >= 2) {
            File file = new File(args[1]);
            StringBuilder builder = new StringBuilder((int) file.length());
            char[] buffer = new char[1024];
            FileReader reader = new FileReader(file);
            while (true) {
                int nread = reader.read(buffer);
                if (nread < 0) {
                    break;
                }
                builder.append(buffer, 0, nread);
            }
            String str = builder.toString();
            source.setText(str);
        }
    }

    private static class ParseQueue {
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private String current;

        void onUpdate(String text) {
            lock.lock();
            try {
                current = text;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        String getNext() {
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
            return str;
        }
    }

    private static class TextChangeHandler implements DocumentListener {
        private final JTextPane source;
        private final ParseQueue queue;

        TextChangeHandler(JTextPane source, ParseQueue queue) {
            this.source = source;
            this.queue = queue;
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
            queue.onUpdate(source.getText());
        }
    }

    private static class ParseLoop implements Runnable {
        private final JavaParser javaParser = new JavaParser();
        private final ParseQueue queue;
        private final JTextPane parsed;
        private final JTextPane status;
        private final Style plain;
        private final Style broken;

        ParseLoop(ParseQueue queue, JTextPane parsed, JTextPane status) {
            this.queue = queue;
            this.parsed = parsed;
            this.status = status;
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
            String str = queue.getNext();

            final ResultCollector collector = new ResultCollector();
            javaParser.parse(str, collector);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document document = parsed.getDocument();
                        document.remove(0, document.getLength());
                        document.insertString(0, collector.builder.toString(), plain);
                        document.insertString(document.getLength(), collector.failureBuilder.toString(), broken);
                        status.getDocument().remove(0, status.getDocument().getLength());
                        if (collector.failure != null) {
                            status.getDocument().insertString(0, "FAILED: " + collector.failure, broken);
                        } else {
                            status.getDocument().insertString(0, "OK", plain);
                        }
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        private class ResultCollector implements TokenVisitor {
            final StringBuilder builder = new StringBuilder();
            final StringBuilder failureBuilder = new StringBuilder();
            String failure = null;

            @Override
            public void token(Region match) {
                builder.append(match.getText());
            }

            @Override
            public void failed(String message, Region remainder) {
                failure = message;
                failureBuilder.append(remainder.getText());
            }
        }
    }
}
