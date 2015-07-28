package org.backmeup.plugin.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

public class MetainfoTest {
    @Test
    public void testParseDate() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2015, 12, 31, 23, 59, 17);
        Date date = cal.getTime();
        
        Metainfo info = new Metainfo();
        info.setBackupDate(date);
        Date actualDate = info.getBackupDate();
        
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z");
        Assert.assertEquals(df.format(date), df.format(actualDate));
    }
    
    @Test(expected=NullPointerException.class)
    public void testParseDateNull() {
        Metainfo info = new Metainfo();
        info.setBackupDate(null);
    }
    
    @Test
    public void testToString() {
        Metainfo info = new Metainfo();
        info.setAttribute("key", "value");
        
        String expected = "key = value\n";
        String actual = info.toString();
        
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testEquals() {
        Metainfo info1 = new Metainfo();
        info1.setId("testId");
        info1.setAttribute("key", "value");
        
        Metainfo info2 = new Metainfo();
        info2.setId("testId");
        info2.setAttribute("key", "value");
        
        Assert.assertTrue(info1.equals(info2));
    }
    
    @Test
    public void testEqualsFalse() {
        Metainfo info1 = new Metainfo();
        info1.setId("testId");
        info1.setAttribute("key", "value");
        
        Metainfo info2 = new Metainfo();
        info2.setAttribute("key", "value");
        
        Assert.assertFalse(info1.equals(info2));
    }
}
