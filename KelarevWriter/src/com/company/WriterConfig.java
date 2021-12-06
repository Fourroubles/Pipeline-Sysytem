package com.company;

import com.java_polytech.pipeline_interfaces.RC;

public class WriterConfig implements  IGrammar{

    private RC.RCWho name;
    private final String delimiter = ":";
    private String[] strListConfig;

    public enum listConfig {
        BUFF_SIZE("BUFF_SIZE");

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
        for(WriterConfig.listConfig str : WriterConfig.listConfig.values()) {
            strListConfig[listConfig.valueOf(str.getStr()).ordinal()] = str.getStr();
        }
        return strListConfig;
    }
}
