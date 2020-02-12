package com.aternity.tupleBuilder.utils;

import com.aternity.agentSimulator.simulator.Configuration;
import com.aternity.agentSimulator.simulator.scriptReaderManager.Feeder;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    public StringUtils() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static String obj2string(Object o) {
        String s = "";
        Field[] fields = o.getClass().getFields();

        for(int i = 0; i < fields.length; ++i) {
            s = s + fields[i].getName() + " = " + field2String(o, fields[i]);
            if (i < fields.length - 1) {
                s = s + "\n";
            }
        }

        return s;
    }

    private static String field2String(Object o, Field field) {
        Class type = field.getType();
        String s = "";

        try {
            Object a = field.get(o);
            if (type.isArray() && a != null) {
                int length = Array.getLength(a);
                s = s + "[";

                for(int i = 0; i < length - 1; ++i) {
                    s = s + Array.get(a, i).toString() + ", ";
                }

                if (length > 0) {
                    s = s + Array.get(a, length - 1);
                }

                s = s + "]";
            } else {
                s = a != null ? a.toString() : "null";
            }
        } catch (IllegalAccessException var8) {
            try {
                field.setAccessible(true);
                return field2String(o, field);
            } catch (SecurityException var7) {
                ;
            }
        }

        return s;
    }

    public static String replaceTimestampInStr(String input) {
        for(int i = org.apache.commons.lang.StringUtils.countMatches(input, "TIMESTAMP_PLACE_HOLDER"); i > 0; --i) {
            input = input.replaceFirst("TIMESTAMP_PLACE_HOLDER", String.valueOf(System.currentTimeMillis() - Configuration.timestampDeltaMillis * (long)(i - 1) - (long)i));
        }

        input = input.replaceAll("DATE_PLACE_HOLDER", Configuration.formatter.format(new Date()));
        if (input.contains("SESSIONS_START_TIME_PLACE_HOLDER")) {
            input = Feeder.setSessionStartTimeBySessionId(input);
        }

        return input;
    }

    public static boolean isCommentLine(String line) {
        Pattern MY_PATTERN = Pattern.compile("^#|^REMARK|^comment|^::|^--", 2);
        Matcher m = MY_PATTERN.matcher(line);
        return m.find();
    }

    public static String getTimestampString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getTimestampStringWithSeconds() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
