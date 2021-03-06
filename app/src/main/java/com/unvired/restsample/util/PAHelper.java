package com.unvired.restsample.util;

import com.unvired.exception.ApplicationException;
import com.unvired.logger.Logger;
import com.unvired.restsample.be.WEATHER_HEADER;
import com.unvired.sync.SyncConstants;
import com.unvired.sync.SyncEngine;
import com.unvired.sync.out.ISyncAppCallback;

/**
 * Created by nishchith on 24/07/17.
 */

/*
* Process Agent(PA) Helper
*/
public class PAHelper {

    public static void getWeather(WEATHER_HEADER header, ISyncAppCallback callback) {

        try {
            SyncEngine.getInstance().submitInSyncMode(SyncConstants.MESSAGE_REQUEST_TYPE.PULL, header, "", Constants.PA_GET_WEATHER, false, callback);
        } catch (ApplicationException e) {
            Logger.e(e.getMessage());
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }

    }
}
