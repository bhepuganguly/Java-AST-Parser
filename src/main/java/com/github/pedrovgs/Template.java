package com.github.pedrovgs;
import java.util.Map;

//Map<String,String> myMap = new HashMap<String,String>();
public class Template {
    public static void instrum(String lineNumber, String statement_type,Object ...args){
        String complete_objects_list="";
        //String v1, Object variable, String v2, Object variable2, String v3, Object variable3){
        for (int i = 0; i < args.length; ++i) {
            Object object = args[i];
            complete_objects_list+=" , "+object.toString();
        }
        System.out.println("Line Number: "+lineNumber+" Instrumented Statement Type : "+statement_type+"  "+complete_objects_list);
    }
}