package owo.pigeon.utils.hypixel.dailyreward;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RewardPageParser {

    private static final Pattern REWARD_CODE_PATTERN = Pattern.compile("[0-9a-fA-F]+$");

    private RewardPageParser() {
    }

    public static String extractRewardCode(String url) {
        Matcher matcher = REWARD_CODE_PATTERN.matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    public static String extractJsQuotedString(String source, int startIndex, char quote) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\\') {
                if (i + 1 >= source.length()) {
                    break;
                }
                char next = source.charAt(++i);
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (i + 4 < source.length()) {
                            sb.append((char) Integer.parseInt(source.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(next);
                }
            } else if (c == quote) {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
