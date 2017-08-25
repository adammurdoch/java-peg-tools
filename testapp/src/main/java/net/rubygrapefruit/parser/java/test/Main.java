package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.java.JavaToken;
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
import java.util.ArrayList;
import java.util.List;
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
        status.setPreferredSize(new Dimension(150, 100));
        status.setEditable(false);

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(source), new JScrollPane(parsed));
        pane.setResizeWeight(0.5);

        JPanel root = new JPanel();
        root.setLayout(new BorderLayout());
        root.add(status, BorderLayout.NORTH);
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
        private final Style comment;
        private final Style identifier;
        private final Style keyword;

        ParseLoop(ParseQueue queue, JTextPane parsed, JTextPane status) {
            this.queue = queue;
            this.parsed = parsed;
            this.status = status;
            plain = parsed.addStyle("plain", null);
            StyleConstants.setForeground(plain, new Color(109, 109, 109));
            broken = parsed.addStyle("broken", null);
            StyleConstants.setForeground(broken, Color.WHITE);
            StyleConstants.setBackground(broken, Color.RED);
            comment = parsed.addStyle("comment", null);
            StyleConstants.setForeground(comment, new Color(89, 136, 61));
            identifier = parsed.addStyle("identifier", null);
            StyleConstants.setForeground(identifier, new Color(130, 100, 176));
            StyleConstants.setBold(identifier, true);
            keyword = parsed.addStyle("keyword", null);
            StyleConstants.setForeground(keyword, new Color(53, 132, 172));
            StyleConstants.setBold(keyword, true);
        }

        @Override
        public void run() {
            while (true) {
                doOnce();
            }
        }

        private void doOnce() {
            final String str = queue.getNext();

            final ResultCollector collector = new ResultCollector();
            javaParser.parse(str, collector);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document document = parsed.getDocument();
                        document.remove(0, document.getLength());
                        for (Span span : collector.spans) {
                            document.insertString(document.getLength(), span.text, span.style);
                        }
                        if (collector.failure != null) {
                            status.setText(collector.failure);
                        } else {
                            status.setText("");
                        }
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        private class ResultCollector implements TokenVisitor<JavaToken> {
            final List<Span> spans = new ArrayList<Span>();
            String failure = null;

            @Override
            public void token(JavaToken type, Region match) {
                switch (type) {
                    case Comment:
                        spans.add(new Span(match.getText(), comment));
                        break;
                    case Keyword:
                        spans.add(new Span(match.getText(), keyword));
                        break;
                    case Identifier:
                        spans.add(new Span(match.getText(), identifier));
                        break;
                    default:
                        spans.add(new Span(match.getText(), plain));
                }
            }

            @Override
            public void failed(String message, Region remainder) {
                spans.add(new Span(remainder.getText(), broken));
                failure = message;
            }
        }
    }

    private static class Span {
        final String text;
        final Style style;

        public Span(String text, Style style) {
            this.text = text;
            this.style = style;
        }
    }
}
