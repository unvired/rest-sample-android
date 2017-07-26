package com.unvired.sample.rest.util;

import com.unvired.core.FrameworkManager;
import com.unvired.core.UserSettingsManager;
import com.unvired.database.DBException;
import com.unvired.sample.rest.be.WEATHER_HEADER;

/**
 * Created by nishchith on 24/07/17.
 */

public class Utils {

    public static String getUserId() {
        try {
            return UserSettingsManager.getInstance().getUnviredUserId();
        } catch (DBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFEUserId() {
        try {
            FrameworkManager.getInstance().getFrameworkSettingsManager().getFrontEndUserId();
        } catch (DBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getServerURL() {
        try {
            FrameworkManager.getInstance().getFrameworkSettingsManager().getServerId();
        } catch (DBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTemperature(WEATHER_HEADER header){
        String temp = header.getTEMPERATURE();

        temp = temp.replace(" degree",""+ (char) 0x00B0);
        temp = temp.replace(" Degree",""+ (char) 0x00B0);
        temp = temp.replace(" Celsius","C");
        temp = temp.replace(" celsius","C");

        return temp;
    }

}

