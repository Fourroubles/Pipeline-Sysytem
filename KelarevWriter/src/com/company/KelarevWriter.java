package com.company;

import com.java_polytech.pipeline_interfaces.*;
import java.io.OutputStream;
import java.io.IOException;

public class KelarevWriter implements IWriter {
    private OutputStream outputStream;
    private int buffSize;

    @Override
    public RC setOutputStream(OutputStream outputStream){
        this.outputStream = outputStream;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume(byte[] bytes){
        RC error;
        if(bytes == null) {
            try {
                outputStream.close();
                error = RC.RC_SUCCESS;
            } catch (IOException e) {
                error = new RC(RC.RCWho.WRITER, RC.RCType.CODE_CUSTOM_ERROR, "Cannot close file");
            }
            return error;
        }

        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {
        WriterConfig writerConfig = new WriterConfig(RC.RCWho.WRITER);
        RC error =  writerConfig.syntacticAnalysis(s);

        if(!error.isSuccess()) {
            return error;
        }

        try{
            buffSize = Integer.parseInt(writerConfig.getDataConfig().get(WriterConfig.listConfig.BUFF_SIZE.getStr()));
        }
        catch (Exception ex) {
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        if(buffSize < 0){
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        return RC.RC_SUCCESS;
    }
}
