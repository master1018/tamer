public class DateFormatSymbols implements Serializable, Cloneable {
    private static final long serialVersionUID = -5987973545549424702L;
    private String localPatternChars;
    String[] ampms, eras, months, shortMonths, shortWeekdays, weekdays;
    String[][] zoneStrings;
    transient boolean customZoneStrings;
    transient final Locale locale;
    synchronized String[][] internalZoneStrings() {
        if (zoneStrings == null) {
            zoneStrings = Resources.getDisplayTimeZones(locale.toString());
        }
        return zoneStrings;
    }
    public DateFormatSymbols() {
        this(Locale.getDefault());
    }
    public DateFormatSymbols(Locale locale) {
        this.locale = locale;
        this.localPatternChars = SimpleDateFormat.patternChars;
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        this.ampms = localeData.amPm;
        this.eras = localeData.eras;
        this.months = localeData.longMonthNames;
        this.shortMonths = localeData.shortMonthNames;
        this.weekdays = localeData.longWeekdayNames;
        this.shortWeekdays = localeData.shortWeekdayNames;
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        internalZoneStrings();
        oos.defaultWriteObject();
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DateFormatSymbols)) {
            return false;
        }
        DateFormatSymbols obj = (DateFormatSymbols) object;
        if (!localPatternChars.equals(obj.localPatternChars)) {
            return false;
        }
        if (!Arrays.equals(ampms, obj.ampms)) {
            return false;
        }
        if (!Arrays.equals(eras, obj.eras)) {
            return false;
        }
        if (!Arrays.equals(months, obj.months)) {
            return false;
        }
        if (!Arrays.equals(shortMonths, obj.shortMonths)) {
            return false;
        }
        if (!Arrays.equals(shortWeekdays, obj.shortWeekdays)) {
            return false;
        }
        if (!Arrays.equals(weekdays, obj.weekdays)) {
            return false;
        }
        if (zoneStrings == null && obj.zoneStrings == null
                    && !locale.equals(obj.locale)) {
            return false;
        }
        internalZoneStrings();
        obj.internalZoneStrings();
        if (zoneStrings.length != obj.zoneStrings.length) {
            return false;
        }
        for (String[] element : zoneStrings) {
            if (element.length != element.length) {
                return false;
            }
            for (int j = 0; j < element.length; j++) {
                if (element[j] != element[j]
                        && !(element[j].equals(element[j]))) {
                    return false;
                }
            }
        }
        return true;
    }
    public String[] getAmPmStrings() {
        return ampms.clone();
    }
    public String[] getEras() {
        return eras.clone();
    }
    public String getLocalPatternChars() {
        return localPatternChars;
    }
    public String[] getMonths() {
        return months.clone();
    }
    public String[] getShortMonths() {
        return shortMonths.clone();
    }
    public String[] getShortWeekdays() {
        return shortWeekdays.clone();
    }
    public String[] getWeekdays() {
        return weekdays.clone();
    }
    public String[][] getZoneStrings() {
        return Resources.clone2dStringArray(internalZoneStrings());
    }
    @Override
    public int hashCode() {
        String[][] zoneStrings = internalZoneStrings();
        int hashCode;
        hashCode = localPatternChars.hashCode();
        for (String element : ampms) {
            hashCode += element.hashCode();
        }
        for (String element : eras) {
            hashCode += element.hashCode();
        }
        for (String element : months) {
            hashCode += element.hashCode();
        }
        for (String element : shortMonths) {
            hashCode += element.hashCode();
        }
        for (String element : shortWeekdays) {
            hashCode += element.hashCode();
        }
        for (String element : weekdays) {
            hashCode += element.hashCode();
        }
        for (String[] element : zoneStrings) {
            for (int j = 0; j < element.length; j++) {
                if (element[j] != null) {
                    hashCode += element[j].hashCode();
                }
            }
        }
        return hashCode;
    }
    public void setAmPmStrings(String[] data) {
        ampms = data.clone();
    }
    public void setEras(String[] data) {
        eras = data.clone();
    }
    public void setLocalPatternChars(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        localPatternChars = data;
    }
    public void setMonths(String[] data) {
        months = data.clone();
    }
    public void setShortMonths(String[] data) {
        shortMonths = data.clone();
    }
    public void setShortWeekdays(String[] data) {
        shortWeekdays = data.clone();
    }
    public void setWeekdays(String[] data) {
        weekdays = data.clone();
    }
    public void setZoneStrings(String[][] data) {
        zoneStrings = Resources.clone2dStringArray(data);
        customZoneStrings = true;
    }
}
