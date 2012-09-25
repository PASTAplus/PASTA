package edu.lternet.pasta.common;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.junit.Test;

import edu.lternet.pasta.common.PercentEncoder;

public class TestPercentEncoder {

    private static final String UTF8 = "UTF-8";
    
    @Test
    public void testEncodeSpace() throws UnsupportedEncodingException {
        assertEquals("%20", PercentEncoder.encode(" "));
        assertEquals("+", URLEncoder.encode(" ", UTF8));
    }
    
    @Test
    public void testDecodeSpace() throws UnsupportedEncodingException {
        assertEquals(" ", PercentEncoder.decode("%20"));
        assertEquals(" ", URLDecoder.decode("%20", UTF8));
        assertEquals(" ", PercentEncoder.decode("+"));
        assertEquals(" ", URLDecoder.decode("+", UTF8));
    }
    
    @Test
    public void testEncodePlus() throws UnsupportedEncodingException {
        assertEquals("%2B", PercentEncoder.encode("+"));
        assertEquals("%2B", URLEncoder.encode("+", UTF8));
    }
    
    @Test
    public void testDecodePlus() throws UnsupportedEncodingException {
        assertEquals("+", PercentEncoder.decode("%2B"));
        assertEquals("+", URLDecoder.decode("%2B", UTF8));
    }
    

}
