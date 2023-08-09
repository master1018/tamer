public class Bug4640234  {
  static SimpleDateFormat sdfEn = new SimpleDateFormat("zzzz", Locale.US);
  static SimpleDateFormat sdfEnShort = new SimpleDateFormat("z", Locale.US);
  static Locale locEn = Locale.ENGLISH;
  static SimpleDateFormat sdfLoc;
  static SimpleDateFormat sdfLocShort;
  static Date date = new Date();
  static Locale[] locales2Test = new Locale[] {
    new Locale("de"),
    new Locale("es"),
    new Locale("fr"),
    new Locale("it"),
    new Locale("ja"),
    new Locale("ko"),
    new Locale("sv"),
    new Locale("zh", "CN"),
    new Locale("zh", "TW")
  };
  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    StringBuffer errors = new StringBuffer("");
    StringBuffer warnings = new StringBuffer("");
    String[] timezones = TimeZone.getAvailableIDs();
    String[] countries = locEn.getISOCountries();
    String[] languages = locEn.getISOLanguages();
    ResourceBundle resEn = ResourceBundle.getBundle("sun.util.resources.LocaleNames", locEn);
    Map<String, String> countryMapEn = getList(resEn, true);
    Map<String, String> languageMapEn = getList(resEn, false);
    ResourceBundle resLoc;
    Map<String, String> countryMap;
    Map<String, String> languageMap;
    for (Locale locale : locales2Test) {
      resLoc = ResourceBundle.getBundle("sun.util.resources.LocaleNames", locale);
      sdfLoc = new SimpleDateFormat("zzzz", locale);
      sdfLocShort = new SimpleDateFormat("z", locale);
      for (String timezone : timezones) {
        if (isTZIgnored(timezone)) {
          continue;
        }
        warnings.append(testTZ(timezone, locale));
      }
      countryMap = getList(resLoc, true);
      for (String country : countries) {
        String[] result = testEntry(country,
          countryMapEn,
          countryMap,
          locale,
          "ERROR: {0} country name for country code: {1} not found!\n",
          "WARNING: {0} country name for country code: {1} not localized!\n"
        );
        if (warnings.indexOf(result[0]) == -1) {
          warnings.append(result[0]);
        }
        if (errors.indexOf(result[1]) == -1) {
          errors.append(result[1]);
        }
      }
      languageMap = getList(resLoc, false);
      for (String language : languages) {
        String[] result = testEntry(language,
          languageMapEn,
          languageMap,
          locale,
          "ERROR: {0} language name for language code: {1} not found!\n",
          "WARNING: {0} language name for language code: {1} not localized!\n");
        if (warnings.indexOf(result[0]) == -1) {
          warnings.append(result[0]);
        }
        if (errors.indexOf(result[1]) == -1) {
          errors.append(result[1]);
        }
      }
    }
    StringBuffer message = new StringBuffer("");
    if (!"".equals(errors.toString())) {
      message.append("Test failed! ");
      message.append("ERROR: some keys are missing! ");
    }
    if ("".equals(message.toString())) {
      System.out.println("\nTest passed");
      System.out.println(warnings.toString());
    } else {
      System.out.println("\nTest failed!");
      System.out.println(errors.toString());
      System.out.println(warnings.toString());
      throw new Exception("\n" + message);
    }
  }
  private static String testTZ(String timeZoneName, Locale locale) {
    StringBuffer timeZoneResult = new StringBuffer("");
    TimeZone tz = TimeZone.getTimeZone(timeZoneName);
    sdfEn.setTimeZone(tz);
    sdfEnShort.setTimeZone(tz);
    sdfLoc.setTimeZone(tz);
    sdfLocShort.setTimeZone(tz);
    String en, enShort, loc, locShort;
    en = sdfEn.format(date);
    enShort = sdfEnShort.format(date);
    loc = sdfLoc.format(date);
    locShort = sdfLocShort.format(date);
    String displayLanguage = locale.getDisplayLanguage();
    String displayCountry = locale.getDisplayCountry();
    if (loc.equals(en)) {
      timeZoneResult.append("[");
      timeZoneResult.append(displayLanguage);
      if (!"".equals(displayCountry)) {
        timeZoneResult.append(" ");
        timeZoneResult.append(displayCountry);
      }
      timeZoneResult.append("] timezone \"");
      timeZoneResult.append(timeZoneName);
      timeZoneResult.append("\" long name \"" + en);
      timeZoneResult.append("\" not localized!\n");
    }
    if (!locShort.equals(enShort)) {
      timeZoneResult.append("[");
      timeZoneResult.append(displayLanguage);
      if (!"".equals(displayCountry)) {
        timeZoneResult.append(" ");
        timeZoneResult.append(displayCountry);
      }
      timeZoneResult.append("] timezone \"");
      timeZoneResult.append(timeZoneName);
      timeZoneResult.append("\" short name \"" + enShort);
      timeZoneResult.append("\" is localized \"");
      timeZoneResult.append(locShort);
      timeZoneResult.append("\"!\n");
    }
    return timeZoneResult.toString();
  }
  private static String[] testEntry(String ISOCode,
                                    Map<String, String> entriesEn,
                                    Map<String, String> entriesLoc,
                                    Locale locale,
                                    String notFoundMessage,
                                    String notLocalizedMessage) {
    String nameEn = null;
    String nameLoc = null;
    for (String key: entriesEn.keySet()) {
      if (ISOCode.equalsIgnoreCase(key)) {
        nameEn = entriesEn.get(key);
        break;
      }
    }
    for (String key: entriesLoc.keySet()) {
      if (ISOCode.equalsIgnoreCase(key)) {
        nameLoc = entriesLoc.get(key);
        break;
      }
    }
    if (nameEn == null) {
      return new String[] {"", MessageFormat.format(notFoundMessage,
        new String[] {"English", ISOCode})};
    }
    if (nameLoc == null) {
      return new String[] {"", MessageFormat.format(notFoundMessage,
        new String[] {locale.getDisplayName(), ISOCode})};
    }
    if (nameEn.equals(nameLoc)) {
      return new String[] {MessageFormat.format(notLocalizedMessage,
        new String[] {locale.getDisplayName(), ISOCode}), ""};
    }
    return new String[] {"", ""};
  }
  private static boolean isTZIgnored(String TZName) {
    if (TZName.startsWith("Etc/GMT") ||
        TZName.indexOf("Riyadh8") != -1 ||
        TZName.equals("GMT0") ||
        TZName.equals("MET")
        ) {
      return true;
    }
    return false;
  }
    private static Map<String, String> getList(ResourceBundle rs, Boolean getCountryList) {
        char beginChar = 'a';
        char endChar = 'z';
        if (getCountryList) {
            beginChar = 'A';
            endChar = 'Z';
        }
        Map<String, String> hm = new HashMap<String, String>();
        Enumeration<String> keys = rs.getKeys();
        while (keys.hasMoreElements()) {
            String s = keys.nextElement();
            if (s.length() == 2 &&
                s.charAt(0) >= beginChar && s.charAt(0) <= endChar) {
                hm.put(s, rs.getString(s));
            }
        }
        return hm;
    }
}
