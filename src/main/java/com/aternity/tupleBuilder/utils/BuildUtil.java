package com.aternity.tupleBuilder.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class BuildUtil {

    public static String getBuild(){
        String build = "NA";
        try {
            File file = new File("build.properties");
            FileInputStream fileInput = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();
            String buildVersion = properties.getProperty("build.version");
            String buildDate =  properties.getProperty("build.timestamp");
            build = buildVersion + " (" + buildDate + ")";
        }catch (Exception e){}
        return build;
    }
}
