package net.rubygrapefruit.parser.java.test;

import net.rubygrapefruit.parser.java.JavaParser;
import net.rubygrapefruit.parser.java.JavaToken;
import net.rubygrapefruit.parser.peg.Region;
import net.rubygrapefruit.parser.peg.TokenVisitor;
import net.rubygrapefruit.parser.sample.util.FileCollector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Benchmark {
    public static void main(String[] args) throws InterruptedException {
        final AtomicInteger count = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();

        List<File> files = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            files.add(new File(arg));
        }
        long start = System.nanoTime();
        final JavaParser parser = new JavaParser();
        new FileCollector().process(files, new FileCollector.Worker() {
            @Override
            public void handle(File file, String content) throws Exception {
                count.incrementAndGet();
                parser.parse(content, new TokenVisitor<JavaToken>() {
                    @Override
                    public void token(JavaToken type, Region match) {
                    }

                    @Override
                    public void failed(String message, Region remainder) {
                        failed.incrementAndGet();
                    }
                });
            }
        }, 4);
        long end = System.nanoTime();
        System.out.println(String.format("Parsed %d files with %d errors in %dms", count.get(), failed.get(), (end - start) / 1000000));
    }
}
