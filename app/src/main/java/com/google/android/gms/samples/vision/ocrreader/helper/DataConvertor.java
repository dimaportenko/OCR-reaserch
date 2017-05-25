package com.google.android.gms.samples.vision.ocrreader.helper;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dmytro Portenko on 11/17/16.
 */

public class DataConvertor {

    public static String getNumberFromString(String string) {
        StringBuffer sBuffer = new StringBuffer();
        Pattern p = Pattern.compile("[0-9]+.[0-9]*|[0-9]*.[0-9]+|[0-9]+");
        Matcher m = p.matcher(string);
        boolean found = false;
        while (m.find()) {
            sBuffer.append(m.group());
            found = true;
        }
        if(found) {
            String number = sBuffer.toString();
            number = number.replaceAll("\\D", ".");
            String[] parts = number.split("\\.", 2);
            if(parts.length > 1) {
                number = parts[0] + "." + parts[1].replaceAll("\\.", "");
            }

            return number;
        } else {
            return null;
        }
    }
}
