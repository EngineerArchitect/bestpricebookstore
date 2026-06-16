package com.demo.bestpricebookstore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RestCallStatistics {

    private static final DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy-hh-mm-ss");
    private static final Path textFilePath = Paths.get("timing.log");

    private final Map<String, Long> timeMap = Collections.synchronizedMap(new HashMap<String, Long>());

    static {

        try {
            boolean exists = Files.exists(textFilePath);
            if (exists) {
                String dateStr = dateFormat.format(new Date());
                Files.move(textFilePath, Paths.get("timing-till-" + dateStr + ".log"));
            }

            Files.createFile(textFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addTiming(String storeName, long time) {
        getTimeMap().put(storeName, time);
    }

    public void dumpTiming() {
        try {
            Files.write(textFilePath,
                    String.format("%s;%s;%s\n",
                            getTimeMap().get("Wonder Book Store"),
                            getTimeMap().get("Mascot Book Store"),
                            getTimeMap().get("Best Price Store")).getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Long> getTimeMap() {
        return timeMap;
    }

}
