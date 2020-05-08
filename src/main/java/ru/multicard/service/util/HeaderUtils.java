package ru.multicard.service.util;

public class HeaderUtils {

    public static String extractSession(String cookie) {
        var parts = cookie.split(";");
        return parts[parts.length - 1].split("=") [1];
    }
}
