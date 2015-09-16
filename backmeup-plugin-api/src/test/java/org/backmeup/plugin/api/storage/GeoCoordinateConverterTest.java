package org.backmeup.plugin.api.storage;

import org.backmeup.plugin.util.GeoMetadataConverter;
import org.junit.Assert;
import org.junit.Test;

public class GeoCoordinateConverterTest {

    @Test
    public void testConversion() {
        //input tikaprop_GPS Latitude" : "48.0° 48.0' 57.59299999999257"
        double degrees = 48.0;
        double minutes = 48;
        double seconds = 57.59299999999257;
        double result = GeoMetadataConverter.convertGeoCoordinates(degrees, minutes, seconds);
        Assert.assertEquals(48.81599805555555, result, 0);
    }

    @Test
    public void extractLatitudeFromTikaString() {
        String s = "48.0° 48.0' 57.59299999999257";
        double result = GeoMetadataConverter.extractAndConvertGeoCoordinates(s);
        Assert.assertEquals(48.81599805555555, result, 0);
    }

}
