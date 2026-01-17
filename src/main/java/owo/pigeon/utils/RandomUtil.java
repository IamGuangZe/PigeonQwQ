package owo.pigeon.utils;

import java.util.Random;

public class RandomUtil {
    // 双开区间随机数
    public static int intRandom(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        Random random = new Random();
        return min + random.nextInt(max - min + 1);
    }

    public static double doubleRandom(double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        Random random = new Random();
        return min + (random.nextDouble() * (max - min + Double.MIN_VALUE));
    }
}
