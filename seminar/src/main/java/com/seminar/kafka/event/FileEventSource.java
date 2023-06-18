package com.seminar.kafka.event;

import com.seminar.kafka.model.OrderModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class FileEventSource implements Runnable /*쓰레드로 생성 가능하도록 생성*/ {
    public static final Logger logger = LoggerFactory.getLogger(FileEventSource.class.getName());
    public boolean keepRunning = true;
    private final int updateInterval;
    private final File file;
    private long filePointer = 0;

    private final EventHandler eventHandler;

    public FileEventSource(int updateInterval, File file, EventHandler eventHandler) {
        this.updateInterval = updateInterval;
        this.file = file;
        this.eventHandler = eventHandler;
    }

    @Override
    public void run() {
        try {
            while(this.keepRunning) {
                Thread.sleep(this.updateInterval);
                //file의 크기를 계산
                long len = this.file.length();
                // file의 크기가 포인터보다 작을경우 파일이 리셋되었다고 판단.
                if (len < this.filePointer) {
                    logger.info("file was reset as filePointer is longer than fail length");
                    filePointer = len;
                } else if (len > this.filePointer) {
                    readAppendAndSend();
                } else {
                    continue;
                }
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } catch (ExecutionException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void readAppendAndSend() throws IOException, ExecutionException, InterruptedException {
        RandomAccessFile raf = new RandomAccessFile(this.file, "r");
        raf.seek(this.filePointer);
        String line = null;
        while((line = raf.readLine()) != null) {
            sendMessage(line);
        }
        // file이 변경되었으므로 file의 filePointer를 현재 file의 마지막으로 재설정함.
        this.filePointer = raf.getFilePointer();
    }
    private void sendMessage(String line) throws ExecutionException, InterruptedException {
        final String delimiter = ",";
        String[] tokens = line.split(delimiter);
        String key = tokens[0];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        OrderModel orderModel = new OrderModel(tokens[1], tokens[2], tokens[3], tokens[4],
                tokens[5], tokens[6], LocalDateTime.parse(tokens[7].trim(), formatter));
        MessageEvent messageEvent = new MessageEvent(key, orderModel);
        this.eventHandler.onMessage(messageEvent);
    }
}
