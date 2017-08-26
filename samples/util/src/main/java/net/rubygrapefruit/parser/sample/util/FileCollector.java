package net.rubygrapefruit.parser.sample.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FileCollector {
    /**
     * Process the given source files and directories using the given number of threads.
     */
    public void process(Collection<File> files, final Worker worker, int threads) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        final List<File> copy = new ArrayList<>(files);
        final File endQueue = new File(".");
        final LinkedBlockingQueue<File> queue = new LinkedBlockingQueue<>();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (File file : copy) {
                    collect(file, queue);
                }
                queue.add(endQueue);
            }
        });
        for (int i = 0; i < threads; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            File file = queue.take();
                            if (file == endQueue) {
                                queue.add(endQueue);
                                break;
                            }
                            StringBuilder content = new StringBuilder();
                            BufferedReader reader = new BufferedReader(new FileReader(file));
                            try {
                                char[] chars = new char[4096];
                                while (true) {
                                    int nread = reader.read(chars, 0, chars.length);
                                    if (nread < 0) {
                                        break;
                                    }
                                    content.append(chars, 0, nread);
                                }
                            } finally {
                                reader.close();
                            }

                            worker.handle(file, content.toString());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    private void collect(File source, Collection<File> files) {
        if (source.isFile()) {
            files.add(source);
        } else if (source.isDirectory()) {
            for (File file : source.listFiles()) {
                collect(file, files);
            }
        }
    }

    public interface Worker {
        void handle(File file, String content) throws Exception;
    }
}
