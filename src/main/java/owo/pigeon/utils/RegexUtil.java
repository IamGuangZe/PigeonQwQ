package owo.pigeon.utils;

import java.util.regex.Pattern;

public class RegexUtil {
    public static String regexGetPart(String regex, String message, int part) {
        return Pattern.compile(regex)
                .matcher(message)
                .results()
                .map(match -> match.group(part))
                .findFirst()
                .orElse(null);
    }
}
