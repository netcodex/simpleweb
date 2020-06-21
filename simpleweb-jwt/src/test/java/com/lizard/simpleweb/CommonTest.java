package com.lizard.simpleweb;

import java.util.Base64;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * 描述：
 *
 * @author x
 * @since 2020-06-15 22:45
 */
public class CommonTest {
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final Base64.Encoder encoder = Base64.getEncoder();

    @Test
    public void testBase64() {
        String s = encoder.encodeToString("apic".getBytes());// YXBpYw==
        System.out.println("s = " + s);
        String str = "YXBpYw==";
        String decodeStr = new String(decoder.decode(str));
        System.out.println("decodeStr = " + decodeStr);// apic
    }

    @Test
    public void testStringFormat() {
        String messageBody = String.format(Locale.ROOT, "age=%d&name=%s&email=%s", 24, "bob", "bob@gmail.com");
        System.out.println("messageBody = " + messageBody);
    }
}
