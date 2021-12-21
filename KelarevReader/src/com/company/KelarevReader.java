package com.company;

import com.java_polytech.pipeline_interfaces.*;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class KelarevReader implements IReader {
    private final static TYPE[] types = new TYPE[]{TYPE.CHAR_ARRAY , TYPE.INT_ARRAY, TYPE.BYTE_ARRAY};
    private InputStream inputStream;
    private byte[] buffer;
    private IConsumer consumer;
    private int buffSize;
    private int lastSizeBuff;

    @Override
    public RC setInputStream(InputStream inputStream){
        if(inputStream == null){
            return new RC(RC.RCWho.READER, RC.RCType.CODE_CUSTOM_ERROR, "Input file is empty");
        }

        this.inputStream = inputStream;
        return RC.RC_SUCCESS;
    }

    @Override
    public IMediator getMediator(TYPE chosenType){
        IMediator result = null;
        switch (chosenType){
            case CHAR_ARRAY: {
                result = new CharMediator();
                break;
            }
            case BYTE_ARRAY: {
                result = new ByteMediator();
                break;
            }
            case INT_ARRAY: {
                result = new IntMediator();
                break;
            }
        }
        return result;
    }

    public class ByteMediator implements IMediator{
        @Override
        public Object getData() {

            if (lastSizeBuff <= 0) {
                return null;
            }
            else {
                return Arrays.copyOf(buffer, lastSizeBuff);
            }
        }
    }

    public class IntMediator implements IMediator{
        @Override
        public Object getData() {

            if (buffer == null) {
                return null;
            }

            IntBuffer intBuf = ByteBuffer.wrap(buffer,0, lastSizeBuff).asIntBuffer();
            int[] result = new int[intBuf.remaining()];
            intBuf.get(result);
            return result;
        }
    }


    public class CharMediator implements IMediator{
        @Override
        public Object getData() {

            if (buffer == null) {
                return null;
            }

            CharBuffer charBuffer = ByteBuffer.wrap(buffer,0, lastSizeBuff).asCharBuffer();
            char[] result = new char[charBuffer.remaining()];
            charBuffer.get(result);

            return result;
        }
    }

    @Override
    public TYPE[] getOutputTypes() {
        return types;
    }

    @Override
    public RC run(){
        try {
            while ((lastSizeBuff = inputStream.available()) > 0) {

                lastSizeBuff = inputStream.read(buffer, 0, buffSize);

                RC error = consumer.consume();

                if(!error.isSuccess()) {
                    return error;
                }
            }
        }
        catch(IOException ex)
        {
            return RC.RC_READER_FAILED_TO_READ;
        }

       RC error = consumer.consume();

        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iConsumer){
        RC error;
        this.consumer = iConsumer;
        error = this.consumer.setProvider(this);

        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {

        SyntaticAnalysis readerConfig = new SyntaticAnalysis(RC.RCWho.READER, new ReaderConfig());
        RC error = readerConfig.syntacticAnalysis(s);

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
