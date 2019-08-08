package com.example.common;

import java.util.HashMap;
import java.util.Map;

public class TimeCost {

    public static Map<String, Long> sStartTime = new HashMap<>();
    public static Map<String, Long> sEndTime = new HashMap<>();

    public static void setStartTime(String methodName, long time) {
        sStartTime.put(methodName, time);
    }

    public static void setEndTime(String methodName, long time) {
        sEndTime.put(methodName, time);
    }

    public static String getCostTime(String methodName) {
        long start = sStartTime.get(methodName);
        long end = sEndTime.get(methodName);
        String result = "method: " + methodName + " cost " + Long.valueOf(end - start) + " ns";
        System.out.println(result);
        return result;
    }

}
