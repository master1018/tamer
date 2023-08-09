public final class TimeZoneNameUtility {
    private static ConcurrentHashMap<Locale, SoftReference<OpenListResourceBundle>> cachedBundles =
        new ConcurrentHashMap<Locale, SoftReference<OpenListResourceBundle>>();
    private static ConcurrentHashMap<Locale, SoftReference<String[][]>> cachedZoneData =
        new ConcurrentHashMap<Locale, SoftReference<String[][]>>();
    public static final String[][] getZoneStrings(Locale locale) {
        String[][] zones;
        SoftReference<String[][]> data = cachedZoneData.get(locale);
        if (data == null || ((zones = data.get()) == null)) {
            zones = loadZoneStrings(locale);
            data = new SoftReference<String[][]>(zones);
            cachedZoneData.put(locale, data);
        }
        return zones;
    }
    private static final String[][] loadZoneStrings(Locale locale) {
        List<String[]> zones = new LinkedList<String[]>();
        OpenListResourceBundle rb = getBundle(locale);
        Enumeration<String> keys = rb.getKeys();
        String[] names = null;
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            names = retrieveDisplayNames(rb, key, locale);
            if (names != null) {
                zones.add(names);
            }
        }
        String[][] zonesArray = new String[zones.size()][];
        return zones.toArray(zonesArray);
    }
    public static final String[] retrieveDisplayNames(String id, Locale locale) {
        OpenListResourceBundle rb = getBundle(locale);
        return retrieveDisplayNames(rb, id, locale);
    }
    private static final String[] retrieveDisplayNames(OpenListResourceBundle rb,
                                                String id, Locale locale) {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(TimeZoneNameProvider.class);
        String[] names = null;
        if (pool.hasProviders()) {
            names = pool.getLocalizedObject(
                            TimeZoneNameGetter.INSTANCE,
                            locale, rb, id);
        }
        if (names == null) {
            try {
                names = rb.getStringArray(id);
            } catch (MissingResourceException mre) {
            }
        }
        return names;
    }
    private static final OpenListResourceBundle getBundle(Locale locale) {
        OpenListResourceBundle rb;
        SoftReference<OpenListResourceBundle> data = cachedBundles.get(locale);
        if (data == null || ((rb = data.get()) == null)) {
            rb = LocaleData.getTimeZoneNames(locale);
            data = new SoftReference<OpenListResourceBundle>(rb);
            cachedBundles.put(locale, data);
        }
        return rb;
    }
    private static class TimeZoneNameGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<TimeZoneNameProvider,
                                                                   String[]>{
        private static final TimeZoneNameGetter INSTANCE =
            new TimeZoneNameGetter();
        public String[] getObject(TimeZoneNameProvider timeZoneNameProvider,
                                Locale locale,
                                String requestID,
                                Object... params) {
            assert params.length == 0;
            String[] names = null;
            String queryID = requestID;
            if (queryID.equals("GMT")) {
                names = buildZoneStrings(timeZoneNameProvider, locale, queryID);
            } else {
                Map<String, String> aliases = ZoneInfo.getAliasTable();
                if (aliases != null) {
                    if (aliases.containsKey(queryID)) {
                        String prevID = queryID;
                        while ((queryID = aliases.get(queryID)) != null) {
                            prevID = queryID;
                        }
                        queryID = prevID;
                    }
                    names = buildZoneStrings(timeZoneNameProvider, locale, queryID);
                    if (names == null) {
                        names = examineAliases(timeZoneNameProvider, locale,
                                               queryID, aliases, aliases.entrySet());
                    }
                }
            }
            if (names != null) {
                names[0] = requestID;
            }
            return names;
        }
        private static String[] examineAliases(TimeZoneNameProvider tznp, Locale locale,
                                               String id,
                                               Map<String, String> aliases,
                                               Set<Map.Entry<String, String>> aliasesSet) {
            if (aliases.containsValue(id)) {
                for (Map.Entry<String, String> entry : aliasesSet) {
                    if (entry.getValue().equals(id)) {
                        String alias = entry.getKey();
                        String[] names = buildZoneStrings(tznp, locale, alias);
                        if (names != null) {
                            return names;
                        } else {
                            names = examineAliases(tznp, locale, alias, aliases, aliasesSet);
                            if (names != null) {
                                return names;
                            }
                        }
                    }
                }
            }
            return null;
        }
        private static String[] buildZoneStrings(TimeZoneNameProvider tznp,
                                    Locale locale, String id) {
            String[] names = new String[5];
            for (int i = 1; i <= 4; i ++) {
                names[i] = tznp.getDisplayName(id, i>=3, i%2, locale);
                if (i >= 3 && names[i] == null) {
                    names[i] = names[i-2];
                }
            }
            if (names[1] == null) {
                names = null;
            }
            return names;
        }
    }
}
