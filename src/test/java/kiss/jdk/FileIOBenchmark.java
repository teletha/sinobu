/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import antibug.profiler.Benchmark;

public class FileIOBenchmark {

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark();

        String message = "Write This Message\n";

        BufferedWriter buffered = new BufferedWriter(new FileWriter("IO-BufferedFileWriter.log", false));
        benchmark.measure("BufferedFileWriter", () -> {
            buffered.append(message);
            return -1;
        });

        RandomAccessFile random = new RandomAccessFile("IO-RandomAccessFile.log", "rw");
        benchmark.measure("RandomAccessFile", () -> {
            random.writeChars(message);
            return -1;
        });

        ByteBuffer bytes = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
        FileChannel channel = FileChannel.open(Path.of("IO-FileChannel.log"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        benchmark.measure("FileChannel", () -> {
            channel.write(bytes);
            bytes.flip();
            return -1;
        });

        benchmark.perform();

        // cleanup
        buffered.close();
        random.close();
    }
}