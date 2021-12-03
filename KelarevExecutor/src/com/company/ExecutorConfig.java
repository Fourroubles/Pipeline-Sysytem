package com.company;


import com.java_polytech.pipeline_interfaces.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExecutorConfig {

    private RC.RCWho name;
    private final String delimiter = ":";
    private Map<String, String> dataConfig = new HashMap<>();

    public enum listConfig {
        SIDE("side"),
        SHIFT("shift");

        private String str;

        listConfig(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }

    ExecutorConfig(RC.RCWho name) {
        this.name = name;
    }

    public RC syntacticAnalysis(String file) {

        try {
            FileReader fileReaderr = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReaderr);

            String line = bufferedReader.readLine();

            while (line != null) {
                String[] spliteLine = line.replaceAll("\\s+","").split(delimiter);
                if(!checkConfig(line)) {
                    return new RC(name, RC.RCType.CODE_CONFIG_FILE_ERROR, line + ": Invalid string");
                }
                line = bufferedReader.readLine();
            }

        } catch (IOException ex) {
            return new RC(name, RC.RCType.CODE_CONFIG_FILE_ERROR, "File not open");
        }

        return RC.RC_SUCCESS;
    }

    private boolean checkConfig(String line) {
        String[] spliteLine = line.replaceAll("\\s+","").split(delimiter);

        for(ExecutorConfig.listConfig str : ExecutorConfig.listConfig.values()) {
            if(str.getStr().equals(spliteLine[0])){
                dataConfig.put(str.getStr(), spliteLine[1]);
                return true;
            }
        }

        return false;
    }

    public Map<String, String> getDataConfig() {
        return dataConfig;
    }
}
