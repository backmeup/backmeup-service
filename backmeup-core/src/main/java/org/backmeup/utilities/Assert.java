package org.backmeup.utilities;

public final class Assert {
    private Assert() {
        // Utility classes should not have public constructor
    }
    
    public static void notNull(Object object, String message) {
       if (object == null) {
           throw new IllegalArgumentException  (message);
       }
    }
 }