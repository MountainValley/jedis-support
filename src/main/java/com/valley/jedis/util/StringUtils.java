package com.valley.jedis.util;

/**
 * @author penghuanhu
 * @since 2024/9/13
 **/
public class StringUtils {

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public static boolean isALLBlank(CharSequence... array) {
        for (CharSequence charSequence : array) {
            if (!isBlank(charSequence)){
                return false;
            }
        }
        return true;
    }

    public static boolean isAnyBlank(CharSequence... array) {
        for (CharSequence charSequence : array) {
            if (isBlank(charSequence)){
                return true;
            }
        }
        return false;
    }
}
