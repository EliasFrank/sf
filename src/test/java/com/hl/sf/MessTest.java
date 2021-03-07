package com.hl.sf;

import org.junit.Test;

public class MessTest {
    @Test
    public void testReplace() {
        String s = "updateTimeOrder";
        String s2 = s.replaceAll("([A-Z])", "_" +  "$1".toLowerCase());
        System.out.println(s2);
    }
}
