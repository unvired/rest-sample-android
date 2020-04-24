package com.unvired.restsample.util;

import com.unvired.restsample.be.WEATHER_HEADER;

/**
 * Created by nishchith on 24/07/17.
 */

public class Utils {

    public static String getTemperature(WEATHER_HEADER header) {
        String temp = header.getTEMPERATURE();

        temp = temp.replace(" degree", "" + (char) 0x00B0);
        temp = temp.replace(" Degree", "" + (char) 0x00B0);
        temp = temp.replace(" Celsius", "C");
        temp = temp.replace(" celsius", "C");

        return temp;
    }
}

