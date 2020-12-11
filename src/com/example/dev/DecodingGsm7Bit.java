package com.example.dev;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DecodingGsm7Bit {
    public static void main(String[] args) {
        String msg0 =
                "CEF43CCC7689C36E72581E1E9902A55E0A8402FC42AF52A4B5" +
                "5289442CD789250205463FC1A63CD8F4C0401F6F93DAA036BE" +
                "0D6F03FC2AC865BB6BFC6EB3404C1A283D07C960B01C";
        String msg4 = decodeGsm7BitHex(msg0);

        System.out.println(msg4);
    }

    public static String decodeGsm7BitHex(String hex) {
        int nOctets = hex.length() / 2;
        int nSeptets = (nOctets * 8) / 7;

        /* convert hex string into byte array */
        int[] octets = new int[nOctets];
        for (int i = 0; i < nOctets; i++) {
            String octetStr = hex.substring(i*2, i*2+2);
            octets[i] = Integer.parseInt(octetStr, 16);
        }

        int[] l = {0x7F, 0x3F, 0x1F, 0x0F, 0x07, 0x03, 0x01};
        int[] u = {0x80, 0xC0, 0xE0, 0xF0, 0xF8, 0xFC, 0xFE};

        ByteBuffer septets = ByteBuffer.allocate(nSeptets);
        for (int j = 0; j < nOctets; j++) {
            int mask  = j % 7;
            int uMask = mask == 0 ? 0 : mask - 1; // ignored when mask == 0
            int i     = j == 0 ? 0 : j - 1;       // index of previous octet, wrap for the 0

            byte upper = (byte) (((byte)octets[j] & l[mask])  <<       mask );
            byte lower = (byte) (((byte)octets[i] & u[uMask]) >>> (8 - mask));
            septets.put((byte) (upper | lower));

            if (mask == 6) {
                // there are two characters at the boundary
                septets.put((byte) (((byte)octets[j] & u[6]) >>> 1));
            }
        }

        //noinspection InjectedReferences
        return Charset.forName("X-Gsm7Bit").decode(ByteBuffer.wrap(septets.array())).toString();
    }
}
