package org.backmeup.plugin.util;

import java.util.regex.Pattern;

public class GeoMetadataConverter {

    /**
     * Converts geocoordinate representation from
     * 
     * @param degrees
     * @param minutes
     * @param seconds
     * @return
     */
    public static double convertGeoCoordinates(double degrees, double minutes, double seconds) {

        return Math.signum(degrees) * (Math.abs(degrees) + (minutes / 60.0) + (seconds / 3600.0));
    }

    /**
     * expects something like "48.0° 48.0' 57.59299999999257" as input and calculatees the decimal coordinate value
     */
    public static double extractAndConvertGeoCoordinates(String s) {
        String[] split = s.split(Pattern.quote("."));
        if ((split.length != 4) || (!s.contains("°")) || (!s.contains("'"))) {
            throw new IllegalArgumentException("String " + s
                    + " does not match the expected format. e.g. 48.0° 48.0' 57.59299999999257");
        }
        try {
            double degrees = Double.valueOf(s.substring(0, s.indexOf("°")));
            double minutes = Double.valueOf(s.substring(s.indexOf("°") + 2, s.indexOf("'")));
            double seconds = Double.valueOf(s.substring(s.indexOf("'") + 2, s.length()));
            return convertGeoCoordinates(degrees, minutes, seconds);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Converting " + s
                    + " failed. Expecting input in form of e.g. 48.0° 48.0' 57.59299999999257");
        }
    }

    public static boolean isValidGeoCoordinate(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
