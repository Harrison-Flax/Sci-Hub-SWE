package utils;

import java.util.Random;

public class PasswordUtils {
    private static final int MIN_NUMBER = 33;
    private static final int MAX_NUMBER = 126;
    private static final int BOUND = MAX_NUMBER - MIN_NUMBER + 1;
    private static final int RANDOM_PASSWORD_LENGTH = 8;

    /**
     * 随机生成指定位数的密码
     *
     * char[33,126]，可表示数字、大小写字母、特殊字符
     *
     */
    public static String randomPassword() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < RANDOM_PASSWORD_LENGTH; i++) {
            char value = (char) (random.nextInt(BOUND) + MIN_NUMBER);
            builder.append(value);
        }
        return builder.toString();
    }
}
