package com.company;

import com.java_polytech.pipeline_interfaces.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class KelarevExecutor implements IExecutor {
    private IConsumer consumer;
    private IMediator readerMediator;
    private String side;
    private int shift;
    private final int  byteSize = 8;

    private final static TYPE[] types = new TYPE[]{ TYPE.CHAR_ARRAY, TYPE.INT_ARRAY, TYPE.BYTE_ARRAY};
    private TYPE type;
    private byte[] buffer;

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

    public TYPE getTypes(TYPE[] first, TYPE[] second){
        TYPE result = null;
        for(int i = 0; i < first.length; ++i){
            for(int j = 0; j < second.length; ++j){
                if(first[i].equals(second[j])){
                    result = first[i];
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public RC setProvider(IProvider provider){
        type = getTypes(provider.getOutputTypes(), types);

        if(type == null){
            return RC.RC_EXECUTOR_TYPES_INTERSECTION_EMPTY_ERROR;
        }

        readerMediator = provider.getMediator(type);

        return RC.RC_SUCCESS;
    }

    private byte[] converter(Object data){
        if (data == null) {
            return null;
        }

        byte[] result = null;

        switch(type){
            case BYTE_ARRAY:
            {
                result = (byte[])data;
                break;
            }
            case INT_ARRAY:
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(((int[])data).length * Integer.BYTES);
                byteBuffer.asIntBuffer().put((int[])data);
                result = byteBuffer.array();
                break;
            }
            case CHAR_ARRAY:
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(((char[])data).length * Character.BYTES);
                byteBuffer.asCharBuffer().put((char[])data);
                result = byteBuffer.array();
                break;
            }
        }

        return result;
    }

    public IMediator getMediator(TYPE type) {
        IMediator result = null;
        switch (type){
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

            if (buffer == null) {
                return null;
            }
            else {
                return Arrays.copyOf(buffer, buffer.length);
            }
        }
    }

    public class IntMediator implements IMediator{
        @Override
        public Object getData() {

            if (buffer == null) {
                return null;
            }

            IntBuffer intBuf = ByteBuffer.wrap(buffer,0, buffer.length).asIntBuffer();
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

            CharBuffer charBuffer = ByteBuffer.wrap(buffer,0, buffer.length).asCharBuffer();
            char[] result = new char[charBuffer.remaining()];
            charBuffer.get(result);

            return result;
        }
    }

    @Override
    public TYPE[] getOutputTypes(){
        return types;
    }

    @Override
    public RC consume(){
        buffer = converter(readerMediator.getData());

        if(buffer != null){
            cyclicShift(buffer);
        }

        RC error = this.consumer.consume();

        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {

        SyntaticAnalysis executorConfig = new SyntaticAnalysis(RC.RCWho.EXECUTOR, new ExecutorConfig());
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
        RC error;
        this.consumer = iConsumer;
        error = this.consumer.setProvider(this);

        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    private void cyclicShift(byte[] buffer){
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
        for(KelarevExecutor.listConfig str : KelarevExecutor.listConfig.values()) {
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
