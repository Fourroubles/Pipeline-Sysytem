package com.company;

import com.java_polytech.pipeline_interfaces.*;
import java.io.InputStream;
import java.io.IOException;

public class KelarevReader implements IReader {
    private InputStream inputStream;
    private byte buffer[];
    private IConsumer consumer;
    private int buffSize;

    @Override
    public RC setInputStream(InputStream inputStream){
        if(inputStream == null){
            return new RC(RC.RCWho.READER, RC.RCType.CODE_CUSTOM_ERROR, "Input file is empty");
        }

        this.inputStream = inputStream;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC run(){
        int lastSizeBuff;
        try {
            while ((lastSizeBuff = inputStream.available()) > 0) {
                if(lastSizeBuff > buffSize) {
                    lastSizeBuff = buffSize;
                }

                inputStream.read(buffer, 0, lastSizeBuff);
                RC error = consumer.consume(buffer);

                if(!error.isSuccess()) {
                    return error;
                }
            }
        }
        catch(IOException ex)
        {
            return RC.RC_READER_FAILED_TO_READ;
        }

        RC error = consumer.consume(null);

        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iConsumer){
        this.consumer = iConsumer;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {
        ReaderConfig readerConfig = new ReaderConfig(RC.RCWho.READER);
        RC error =  readerConfig .syntacticAnalysis(s);

        if(!error.isSuccess()) {
            return error;
        }

        try{
            buffSize = Integer.parseInt(readerConfig.getDataConfig().get(ReaderConfig.listConfig.BUFF_SIZE.getStr()));
        }
        catch (Exception ex) {
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        if(buffSize < 0){
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        buffer = new byte[buffSize];

        if(buffer == null) {
            return new RC(RC.RCWho.READER, RC.RCType.CODE_CUSTOM_ERROR, "Memory is not allocated");
        }

        return RC.RC_SUCCESS;
    }
}
