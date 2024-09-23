package com.valley.jedis.util;

import redis.clients.jedis.HostAndPort;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
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
            if (!isBlank(charSequence)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAnyBlank(CharSequence... array) {
        for (CharSequence charSequence : array) {
            if (isBlank(charSequence)) {
                return true;
            }
        }
        return false;
    }

    public static Set<HostAndPort> parseHostAndPorts(String sentinels) {
        return Arrays.stream(sentinels.split(",")).map(HostAndPort::from).collect(Collectors.toSet());
    }
}
