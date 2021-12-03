package com.company.Manager;

import com.java_polytech.pipeline_interfaces.*;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class Manager implements IConfigurable {
    private IReader reader;
    private IExecutor[] executor;
    private IWriter writer;

    private String readerName;
    private String[] executorName;
    private String writerName;

    private FileInputStream fis;
    private FileOutputStream fos;

    private String configReader;
    private String[] configExecutor;
    private String configWriter;

    private String[] order;

    private final String delimiter = ",";

    @Override
    public RC setConfig(String str) {
        ManagerConfig managerConfig = new ManagerConfig(RC.RCWho.MANAGER);
        RC error = managerConfig .syntacticAnalysis(str);

        if(!error.isSuccess()) {
            return error;
        }

        try {
            fis = new FileInputStream(managerConfig.getDataConfig().get(ManagerConfig.listConfig.INPUT_FILE.getStr()));
        }
        catch (Exception ex) {
            return RC.RC_MANAGER_INVALID_INPUT_FILE;
        }

        try {
            fos = new FileOutputStream(managerConfig.getDataConfig().get(ManagerConfig.listConfig.OUTPUT_FILE.getStr()));
        }
        catch (Exception ex) {
            return RC.RC_MANAGER_INVALID_OUTPUT_FILE;
        }

        readerName = managerConfig.getDataConfig().get(ManagerConfig.listConfig.READER_NAME.getStr());
        writerName = managerConfig.getDataConfig().get(ManagerConfig.listConfig.WRITER_NAME.getStr());
        configReader = managerConfig.getDataConfig().get(ManagerConfig.listConfig.READER_CONFIG.getStr());
        configWriter = managerConfig.getDataConfig().get(ManagerConfig.listConfig.WRITER_CONFIG.getStr());
        executorName  = managerConfig.getDataConfig().get(ManagerConfig.listConfig.EXECUTOR_NAME.getStr())
                       .replaceAll("\\s+","").split(delimiter);
        configExecutor = managerConfig.getDataConfig().get(ManagerConfig.listConfig.EXECUTOR_CONFIG.getStr())
                       .replaceAll("\\s+","").split(delimiter);

        executor = new IExecutor[executorName.length];

        if(executor == null){
            return new RC(RC.RCWho.MANAGER, RC.RCType.CODE_CUSTOM_ERROR, "Memory is not allocated");
        }

        return RC.RC_SUCCESS;
    }

    private RC setWriter(){
        RC error;

        error = writer.setConfig(configWriter);
        if(!error.isSuccess()) {
            return error;
        }

        error = writer.setOutputStream(fos);
        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    private RC setExecutor(){
        RC error;

        for(int i = 0; i < executor.length; ++i) {
            error = executor[i].setConfig(configExecutor[i]);
            if (!error.isSuccess()) {
                return error;
            }
        }

        for(int i = 0; i < executor.length - 1; ++i) {
            error = executor[i].setConsumer(executor[i + 1]);
            if (!error.isSuccess()) {
                return error;
            }
        }

        error = executor[executor.length - 1].setConsumer(writer);
        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    private RC setReader(){
        RC error;

        error = reader.setConfig(configReader);
        if(!error.isSuccess()) {
            return error;
        }

        error = reader.setInputStream(fis);
        if(!error.isSuccess()) {
            return error;
        }

        error = reader.setConsumer(executor[0]);
        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }

    public RC buildPipeline(String str) {
        RC error;
        error = setConfig(str);

        if(!error.isSuccess()) {
            return error;
        }


        try {
            Class<?> aClass = Class.forName(readerName);
            if (IReader.class.isAssignableFrom(aClass)) {
                reader = (IReader) aClass.getDeclaredConstructor().newInstance();
            }
            else {
                return RC.RC_MANAGER_INVALID_READER_CLASS;
            }
        }
        catch (Exception ex) {
            System.out.println(readerName);
            return RC.RC_MANAGER_INVALID_READER_CLASS;
        }

        try {

            for(int i = 0; i < executor.length; ++i) {
                Class<?> aClass = Class.forName(executorName[i]);
                if (IExecutor.class.isAssignableFrom(aClass)) {
                    executor[i] = (IExecutor) aClass.getDeclaredConstructor().newInstance();
                } else {
                    return RC.RC_MANAGER_INVALID_READER_CLASS;
                }
            }
        }
        catch (Exception ex){
            return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        }

        try {
            Class<?> aClass = Class.forName(writerName);
            if (IWriter.class.isAssignableFrom(aClass)) {
                writer = (IWriter) aClass.getDeclaredConstructor().newInstance();
            }
            else {
                return RC.RC_MANAGER_INVALID_WRITER_CLASS;
            }
        }
        catch (Exception ex){
            return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        }

        error = setReader();
        if(!error.isSuccess()) {
            return error;
        }

        error = setExecutor();
        if(!error.isSuccess()) {
            return error;
        }

        error = setWriter();
        if(!error.isSuccess()) {
            return error;
        }

        error = reader.run();
        if(!error.isSuccess()) {
            return error;
        }

        return RC.RC_SUCCESS;
    }
}
