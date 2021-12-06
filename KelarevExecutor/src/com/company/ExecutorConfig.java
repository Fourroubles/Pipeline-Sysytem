package com.company;


import com.java_polytech.pipeline_interfaces.RC;

public class ExecutorConfig implements IGrammar{

    private RC.RCWho name;
    private final String delimiter = ":";
    private String[] strListConfig;

    public enum listConfig {
        SIDE("SIDE"),
        SHIFT("SHIFT");

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
        for(ExecutorConfig.listConfig str : ExecutorConfig.listConfig.values()) {
           strListConfig[listConfig.valueOf(str.getStr()).ordinal()] = str.getStr();
        }

        return strListConfig;
    }
}
