package com.unvired.restsample.be;

import com.unvired.database.DBException;
import com.unvired.model.DataStructure;

/*
This class is part of the BE "WEATHER".
*/
public class WEATHER_HEADER extends DataStructure {

    public static final String TABLE_NAME = "WEATHER_HEADER";

    // City
    public static final String FIELD_CITY = "CITY";

    // Weather
    public static final String FIELD_WEATHER_DESC = "WEATHER_DESC";

    // Temperature
    public static final String FIELD_TEMPERATURE = "TEMPERATURE";

    // Humidity
    public static final String FIELD_HUMIDITY = "HUMIDITY";

    public WEATHER_HEADER() throws DBException {
        super(TABLE_NAME, true);
    }

    public String getCITY() {
        return (String) getField(FIELD_CITY);
    }

    public void setCITY(String value) {
        setField(FIELD_CITY, value);
    }

    public String getWEATHER_DESC() {
        return (String) getField(FIELD_WEATHER_DESC);
    }

    public void setWEATHER_DESC(String value) {
        setField(FIELD_WEATHER_DESC, value);
    }

    public String getTEMPERATURE() {
        return (String) getField(FIELD_TEMPERATURE);
    }

    public void setTEMPERATURE(String value) {
        setField(FIELD_TEMPERATURE, value);
    }

    public String getHUMIDITY() {
        return (String) getField(FIELD_HUMIDITY);
    }

    public void setHUMIDITY(String value) {
        setField(FIELD_HUMIDITY, value);
    }
}