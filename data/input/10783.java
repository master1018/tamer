public class OldIDMappingTest {
    private static final String MAPPING_PROPERTY_NAME = "sun.timezone.ids.oldmapping";
    private static final Map<String, String> newmap = new HashMap<String, String>();
    static {
        newmap.put("EST", "EST");
        newmap.put("MST", "MST");
        newmap.put("HST", "HST");
    }
    public static void main(String[] args) {
        boolean useOldMapping = true;
        String arg = args[0];
        if (arg.equals("-new")) {
            useOldMapping = false;
        } else if (arg.equals("-old")) {
            useOldMapping = true;
        } else {
            throw new RuntimeException("-old or -new must be specified; got " + arg);
        }
        Map<String, String> oldmap = null;
        try {
            Class<?> oldmapClass = Class.forName("sun.util.calendar.TzIDOldMapping");
            Field map = oldmapClass.getDeclaredField("MAP");
            map.setAccessible(true);
            oldmap = (Map<String, String>) map.get(null);
        } catch (Exception e) {
            throw new RuntimeException("can't get TzIDOldMapping.MAP", e);
        }
        String prop = System.getProperty(MAPPING_PROPERTY_NAME);
        System.out.println(MAPPING_PROPERTY_NAME + "=" + prop);
        for (int count = 0; count < 3; count++) {
            for (String id : oldmap.keySet()) {
                TimeZone tzAlias = TimeZone.getTimeZone(id);
                TimeZone tz = TimeZone.getTimeZone(oldmap.get(id));
                if (useOldMapping) {
                    if (!tzAlias.hasSameRules(tz)) {
                        throw new RuntimeException("OLDMAP: " + MAPPING_PROPERTY_NAME + "=" + prop + ": "
                                                   + id + " isn't an alias of " + oldmap.get(id));
                    }
                    if (count == 0) {
                        System.out.println("    " + id + " => " + oldmap.get(id));
                    }
                    tzAlias.setRawOffset(tzAlias.getRawOffset() * count);
                } else {
                    if (!newmap.containsKey(id)) {
                        if (count == 0) {
                            System.out.println("    " + id + " => " + oldmap.get(id));
                        }
                        tzAlias.setRawOffset(tzAlias.getRawOffset() * count);
                        continue;
                    }
                    if (tzAlias.hasSameRules(tz)) {
                        throw new RuntimeException("NEWMAP: " + MAPPING_PROPERTY_NAME + "=" + prop + ": "
                                                   + id + " is an alias of " + oldmap.get(id));
                    }
                    tz = TimeZone.getTimeZone(newmap.get(id));
                    if (!tzAlias.hasSameRules(tz)) {
                        throw new RuntimeException("NEWMAP: " + MAPPING_PROPERTY_NAME + "=" + prop + ": "
                                                   + id + " isn't an alias of " + newmap.get(id));
                    }
                    if (count == 0) {
                        System.out.println("    " + id + " => " + newmap.get(id));
                    }
                    tzAlias.setRawOffset(tzAlias.getRawOffset() * count);
                }
            }
        }
    }
}
