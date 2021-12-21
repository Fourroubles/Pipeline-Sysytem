package com.company;

import com.java_polytech.pipeline_interfaces.*;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class KelarevWriter implements IWriter {
    private OutputStream outputStream;
    private IMediator executorMediator;
    private int buffSize;
    private byte[] buffer;
    private final static TYPE[] types = new TYPE[]{TYPE.CHAR_ARRAY, TYPE.INT_ARRAY, TYPE.BYTE_ARRAY};
    private TYPE type;

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
    public RC setOutputStream(OutputStream outputStream){
        this.outputStream = outputStream;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setProvider(IProvider provider){
        type = getTypes(provider.getOutputTypes(), types);

        if(type == null){
            return RC.RC_EXECUTOR_TYPES_INTERSECTION_EMPTY_ERROR;
        }

        executorMediator = provider.getMediator(type);

        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume( ){
        RC error;
        buffer = converter(executorMediator.getData());
        if(buffer == null) {
            try {
                outputStream.close();
                error = RC.RC_SUCCESS;
            } catch (IOException e) {
                error = new RC(RC.RCWho.WRITER, RC.RCType.CODE_CUSTOM_ERROR, "Cannot close file");
            }
            return error;
        }

        try {
            outputStream.write(buffer,0, buffer.length);
        } catch (IOException e) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {

        SyntaticAnalysis writerConfig = new SyntaticAnalysis(RC.RCWho.WRITER, new WriterConfig());
        RC error = writerConfig.syntacticAnalysis(s);

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
