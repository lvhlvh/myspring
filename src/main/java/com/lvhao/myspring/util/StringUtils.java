package com.lvhao.myspring.util;

import java.util.Set;

public class StringUtils {

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }

    public static String firstLetterLowerCase(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String[] toStringArray(Set<String> s) {
        String[] res = new String[s.size()];
        int i = 0;
        for (String elem : s) {
            res[i] = elem;
            i++;
        }
        return res;
    }
}
