package com.company;

import com.java_polytech.pipeline_interfaces.*;

public class SecondKelarevExecutor implements IExecutor {
    private IConsumer consumer;
    private String side;
    private int shift;
    private final int  byteSize = 8;

    public enum listConfig {
        RIGHT("R"),
        LEFT("L");

        private String str;

        listConfig(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }

    @Override
    public RC consume(byte[] bytes){
        if(bytes == null) {
            consumer.consume(null);
            return RC.RC_SUCCESS;
        }

        cyclicShift(bytes);
        RC error = consumer.consume(bytes);

        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {
        ExecutorConfig executorConfig = new ExecutorConfig(RC.RCWho.EXECUTOR);
        RC error = executorConfig.syntacticAnalysis(s);

        if(!error.isSuccess()) {
            return error;
        }

        try {
            shift = Integer.parseInt(executorConfig.getDataConfig().get(ExecutorConfig.listConfig.SHIFT.getStr()));
        }
        catch (Exception ex) {
            return RC.RC_EXECUTOR_CONFIG_GRAMMAR_ERROR;
        }

        side = executorConfig.getDataConfig().get(ExecutorConfig.listConfig.SIDE.getStr());

        if(!cheakShift()){
            return RC.RC_EXECUTOR_CONFIG_GRAMMAR_ERROR;
        }

        if(!cheakSide()){
            return RC.RC_EXECUTOR_CONFIG_GRAMMAR_ERROR;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iConsumer){
        this.consumer = iConsumer;
        return RC.RC_SUCCESS;
    }

    private void cyclicShift(byte[] buffer){

        for(SecondKelarevExecutor.listConfig str : SecondKelarevExecutor.listConfig.values()) {
            if(side.equals(listConfig.RIGHT.getStr())){
                for(int i = 0; i < buffer.length; ++i){
                    buffer[i] = cyclicShiftRight(buffer[i]);
                }
            }

            if(side.equals(listConfig.LEFT.getStr())){
                for(int i = 0; i < buffer.length; ++i){
                    buffer[i] = cyclicShiftLeft(buffer[i]);
                }
            }
        }
    }

    private byte cyclicShiftRight(byte _byte){
        int x = _byte & 0xFF;
        int y = shift % byteSize;
        return (byte) ((x >> y) | (x  << (byteSize - y)));
    }

    private byte cyclicShiftLeft(byte _byte){
        int x = _byte & 0xFF;
        int y = shift % byteSize;
        return (byte) ((x << y) | (x  >> (byteSize - y)));
    }

    private boolean cheakSide(){
        for(SecondKelarevExecutor.listConfig str : SecondKelarevExecutor.listConfig.values()) {
            if(str.getStr().equals(side)){
                return true;
            }
        }
        return true;
    }

    private boolean cheakShift(){
        if(shift <= 0){
            return false;
        }
        return true;
    }
}
