package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.common.TranslationMatrix;
import com.adaptivescale.rosetta.translator.Translator;
import com.adaptivescale.rosetta.translator.model.ConvertType;
import com.adaptivescale.rosetta.translator.model.TranslateInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import picocli.CommandLine;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class Main {
    public static void main(String... args) throws Exception {
//        TranslationMatrix.getInstance();
//        TranslationMatrix.TranslationModel byId = TranslationMatrix.getInstance().get(1);
//        System.out.println("byId:"+byId.toString());
//        TranslationMatrix.TranslationModel bySourceType = TranslationMatrix.getInstance().get("mysql","unsigned integer");
//        System.out.println("bySourceType:"+bySourceType.toString());
//        TranslationMatrix.TranslationModel sourceToTarget = TranslationMatrix.getInstance().get("mysql","unsigned integer",
//                "postgres");
//        System.out.println("sourceToTarget:"+sourceToTarget.toString());
//        TranslationMatrix.TranslationModel femi = TranslationMatrix.getInstance().get("mysql","unsigned integer",
//                "snowflake");
//        System.out.println("femi" + femi);
//
//        System.out.println(byId.toString());
        int exitCode = new CommandLine(new Cli()).execute(args);
        System.exit(exitCode);
    }

//    public static void JsonToCSV() throws Exception{
//        List<String> strings = Arrays.asList("kinetica", "mysql", "bigquery", "postgres", "snowflake");
////        AtomicReference<Integer> i = new AtomicReference<>(new Integer(1));
//        AtomicInteger integer = new AtomicInteger(1);
//        FileWriter writer = new FileWriter("translation.csv");
//        StringBuilder sb = new StringBuilder();
//        strings.forEach(source -> strings.forEach(target -> {
//            String resourceName = String.format("translations/%s_%s.json", source, target);
//
//            try {
//                InputStream resourceAsStream = TranslationMatrix.class.getClassLoader().getResourceAsStream(resourceName);
//                TranslateInfo translateInfo = new ObjectMapper().readValue(resourceAsStream, TranslateInfo.class);
//                translateInfo.getConverters().forEach(convertType -> convertType.getCompatibleTypes().forEach(compatibleType -> {
//                    sb.append(String.format("%d,%s,%s,%s,%s\n", integer.get(), source, compatibleType.getTypeName(), target, convertType.getTargetTypeName()));
//                    integer.set(integer.get() + 1);
//                }));
//            } catch (Exception e) {}
//        }));
//
//        writer.append(sb);
//        writer.flush();
//        writer.close();
//
//    }

}
