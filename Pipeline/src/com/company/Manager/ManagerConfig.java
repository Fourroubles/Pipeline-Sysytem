package com.company.Manager;

import com.company.ExecutorConfig;
import com.company.IGrammar;
import com.company.ReaderConfig;
import com.java_polytech.pipeline_interfaces.RC;

public class ManagerConfig implements IGrammar {
    private RC.RCWho name;
    private final String delimiter = ":";
    private String[] strListConfig;

    public enum listConfig {
        INPUT_FILE("INPUT_FILE"),
        OUTPUT_FILE("OUTPUT_FILE"),
        READER_NAME("READER_NAME"),
        WRITER_NAME("WRITER_NAME"),
        EXECUTOR_NAME("EXECUTOR_NAME"),
        READER_CONFIG("READER_CONFIG"),
        WRITER_CONFIG("WRITER_CONFIG"),
        EXECUTOR_CONFIG("EXECUTOR_CONFIG");


        private String str;

        listConfig(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }



    public String getDelimitr(){
        return delimiter;
    }

    public String[] getListConfig(){
        strListConfig = new String[listConfig.values().length];

        for(ManagerConfig.listConfig str : ManagerConfig.listConfig.values()) {
           strListConfig[listConfig.valueOf(str.getStr()).ordinal()] = str.getStr();
        }

        return strListConfig;
    }
}
