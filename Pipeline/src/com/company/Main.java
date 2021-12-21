package com.company;

import com.company.Manager.Manager;
import com.java_polytech.pipeline_interfaces.RC;


public class Main {

    public static void main(String[] args) {


        if(args.length != 1) {
            System.out.println("Incorrect amount arguments");
            return;
        }

        Manager manager = new Manager();
        RC rc = manager. buildPipeline(args[0]);

        if(rc.equals(RC.RC_SUCCESS)) {
            System.out.println("Program completed successfully!");
        }
        else {
            System.out.println(rc.who + ": " + rc.info);
            System.out.println("The program ended incorrectly!");
        }
    }
}
