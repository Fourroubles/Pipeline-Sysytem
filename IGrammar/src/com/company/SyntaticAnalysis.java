package com.company;

import com.java_polytech.pipeline_interfaces.RC;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SyntaticAnalysis {
    private RC.RCWho name;
    private final String delimiter;
    private Map<String, String> dataConfig = new HashMap<>();
    IGrammar grammar;
    String[] listConfig;

    public SyntaticAnalysis(RC.RCWho name, IGrammar grammar) {
        this.grammar = grammar;
        this.name = name;
        listConfig = new String[grammar.getListConfig().length];
        listConfig = grammar.getListConfig();
        delimiter = grammar.getDelimitr();
    }

    public RC syntacticAnalysis(String file) {

        try {
            FileReader fileReaderr = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReaderr);

            String line = bufferedReader.readLine();

            while (line != null) {
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

        for(int i = 0; i < listConfig.length; ++i) {
            if(listConfig[i].equals(spliteLine[0])){
                dataConfig.put(listConfig[i], spliteLine[1]);
                return true;
            }
        }

        return false;
    }

    public Map<String, String> getDataConfig() {
        return dataConfig;
    }
}