public abstract class NumberFormat extends Format {
    private static final long serialVersionUID = -2308460125733713944L;
    public static final int INTEGER_FIELD = 0;
    public static final int FRACTION_FIELD = 1;
    private boolean groupingUsed = true, parseIntegerOnly = false;
    private int maximumIntegerDigits = 40, minimumIntegerDigits = 1,
            maximumFractionDigits = 3, minimumFractionDigits = 0;
    public NumberFormat() {
    }
    @Override
    public Object clone() {
        return super.clone();
    }
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof NumberFormat)) {
            return false;
        }
        NumberFormat obj = (NumberFormat) object;
        return groupingUsed == obj.groupingUsed
                && parseIntegerOnly == obj.parseIntegerOnly
                && maximumFractionDigits == obj.maximumFractionDigits
                && maximumIntegerDigits == obj.maximumIntegerDigits
                && minimumFractionDigits == obj.minimumFractionDigits
                && minimumIntegerDigits == obj.minimumIntegerDigits;
    }
    public final String format(double value) {
        return format(value, new StringBuffer(), new FieldPosition(0))
                .toString();
    }
    public abstract StringBuffer format(double value, StringBuffer buffer, FieldPosition field);
    public final String format(long value) {
        return format(value, new StringBuffer(), new FieldPosition(0))
                .toString();
    }
    public abstract StringBuffer format(long value, StringBuffer buffer, FieldPosition field);
    @Override
    public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
        if (object instanceof Byte || object instanceof Short || object instanceof Integer ||
                object instanceof Long ||
                (object instanceof BigInteger && ((BigInteger) object).bitLength() < 64)) {
            long lv = ((Number) object).longValue();
            return format(lv, buffer, field);
        } else if (object instanceof Number) {
            double dv = ((Number) object).doubleValue();
            return format(dv, buffer, field);
        }
        throw new IllegalArgumentException();
    }
    public static Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }
    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }
    public final static NumberFormat getCurrencyInstance() {
        return getCurrencyInstance(Locale.getDefault());
    }
    public static NumberFormat getCurrencyInstance(Locale locale) {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        return getInstance(localeData.currencyPattern, locale);
    }
    public final static NumberFormat getIntegerInstance() {
        return getIntegerInstance(Locale.getDefault());
    }
    public static NumberFormat getIntegerInstance(Locale locale) {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        NumberFormat result = getInstance(localeData.integerPattern, locale);
        result.setParseIntegerOnly(true);
        return result;
    }
    public final static NumberFormat getInstance() {
        return getNumberInstance();
    }
    public static NumberFormat getInstance(Locale locale) {
        return getNumberInstance(locale);
    }
    private static NumberFormat getInstance(String pattern, Locale locale) {
        return new DecimalFormat(pattern, locale);
    }
    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }
    public int getMaximumIntegerDigits() {
        return maximumIntegerDigits;
    }
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }
    public int getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }
    public final static NumberFormat getNumberInstance() {
        return getNumberInstance(Locale.getDefault());
    }
    public static NumberFormat getNumberInstance(Locale locale) {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        return getInstance(localeData.numberPattern, locale);
    }
    public final static NumberFormat getPercentInstance() {
        return getPercentInstance(Locale.getDefault());
    }
    public static NumberFormat getPercentInstance(Locale locale) {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        return getInstance(localeData.percentPattern, locale);
    }
    @Override
    public int hashCode() {
        return (groupingUsed ? 1231 : 1237) + (parseIntegerOnly ? 1231 : 1237)
                + maximumFractionDigits + maximumIntegerDigits
                + minimumFractionDigits + minimumIntegerDigits;
    }
    public boolean isGroupingUsed() {
        return groupingUsed;
    }
    public boolean isParseIntegerOnly() {
        return parseIntegerOnly;
    }
    public Number parse(String string) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Number number = parse(string, pos);
        if (pos.getIndex() == 0) {
            throw new ParseException(
                    Messages.getString("text.1D", string), pos.getErrorIndex()); 
        }
        return number;
    }
    public abstract Number parse(String string, ParsePosition position);
    @Override
    public final Object parseObject(String string, ParsePosition position) {
        if (position == null) {
            throw new NullPointerException(Messages.getString("text.1A")); 
        }
        try {
            return parse(string, position);
        } catch (Exception e) {
            return null;
        }
    }
    public void setCurrency(Currency currency) {
        throw new UnsupportedOperationException();
    }
    public void setGroupingUsed(boolean value) {
        groupingUsed = value;
    }
    public void setMaximumFractionDigits(int value) {
        maximumFractionDigits = value < 0 ? 0 : value;
        if (maximumFractionDigits < minimumFractionDigits) {
            minimumFractionDigits = maximumFractionDigits;
        }
    }
    public void setMaximumIntegerDigits(int value) {
        maximumIntegerDigits = value < 0 ? 0 : value;
        if (maximumIntegerDigits < minimumIntegerDigits) {
            minimumIntegerDigits = maximumIntegerDigits;
        }
    }
    public void setMinimumFractionDigits(int value) {
        minimumFractionDigits = value < 0 ? 0 : value;
        if (maximumFractionDigits < minimumFractionDigits) {
            maximumFractionDigits = minimumFractionDigits;
        }
    }
    public void setMinimumIntegerDigits(int value) {
        minimumIntegerDigits = value < 0 ? 0 : value;
        if (maximumIntegerDigits < minimumIntegerDigits) {
            maximumIntegerDigits = minimumIntegerDigits;
        }
    }
    public void setParseIntegerOnly(boolean value) {
        parseIntegerOnly = value;
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("groupingUsed", Boolean.TYPE), 
            new ObjectStreamField("maxFractionDigits", Byte.TYPE), 
            new ObjectStreamField("maximumFractionDigits", Integer.TYPE), 
            new ObjectStreamField("maximumIntegerDigits", Integer.TYPE), 
            new ObjectStreamField("maxIntegerDigits", Byte.TYPE), 
            new ObjectStreamField("minFractionDigits", Byte.TYPE), 
            new ObjectStreamField("minimumFractionDigits", Integer.TYPE), 
            new ObjectStreamField("minimumIntegerDigits", Integer.TYPE), 
            new ObjectStreamField("minIntegerDigits", Byte.TYPE), 
            new ObjectStreamField("parseIntegerOnly", Boolean.TYPE), 
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("groupingUsed", groupingUsed); 
        fields
                .put(
                        "maxFractionDigits", 
                        maximumFractionDigits < Byte.MAX_VALUE ? (byte) maximumFractionDigits
                                : Byte.MAX_VALUE);
        fields.put("maximumFractionDigits", maximumFractionDigits); 
        fields.put("maximumIntegerDigits", maximumIntegerDigits); 
        fields
                .put(
                        "maxIntegerDigits", 
                        maximumIntegerDigits < Byte.MAX_VALUE ? (byte) maximumIntegerDigits
                                : Byte.MAX_VALUE);
        fields
                .put(
                        "minFractionDigits", 
                        minimumFractionDigits < Byte.MAX_VALUE ? (byte) minimumFractionDigits
                                : Byte.MAX_VALUE);
        fields.put("minimumFractionDigits", minimumFractionDigits); 
        fields.put("minimumIntegerDigits", minimumIntegerDigits); 
        fields
                .put(
                        "minIntegerDigits", 
                        minimumIntegerDigits < Byte.MAX_VALUE ? (byte) minimumIntegerDigits
                                : Byte.MAX_VALUE);
        fields.put("parseIntegerOnly", parseIntegerOnly); 
        fields.put("serialVersionOnStream", 1); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        groupingUsed = fields.get("groupingUsed", true); 
        parseIntegerOnly = fields.get("parseIntegerOnly", false); 
        if (fields.get("serialVersionOnStream", 0) == 0) { 
            maximumFractionDigits = fields.get("maxFractionDigits", (byte) 3); 
            maximumIntegerDigits = fields.get("maxIntegerDigits", (byte) 40); 
            minimumFractionDigits = fields.get("minFractionDigits", (byte) 0); 
            minimumIntegerDigits = fields.get("minIntegerDigits", (byte) 1); 
        } else {
            maximumFractionDigits = fields.get("maximumFractionDigits", 3); 
            maximumIntegerDigits = fields.get("maximumIntegerDigits", 40); 
            minimumFractionDigits = fields.get("minimumFractionDigits", 0); 
            minimumIntegerDigits = fields.get("minimumIntegerDigits", 1); 
        }
        if (minimumIntegerDigits > maximumIntegerDigits
                || minimumFractionDigits > maximumFractionDigits) {
            throw new InvalidObjectException(Messages.getString("text.00")); 
        }
        if (minimumIntegerDigits < 0 || maximumIntegerDigits < 0
                || minimumFractionDigits < 0 || maximumFractionDigits < 0) {
            throw new InvalidObjectException(Messages.getString("text.01")); 
        }
    }
    public static class Field extends Format.Field {
        private static final long serialVersionUID = 7494728892700160890L;
        public static final Field SIGN = new Field("sign"); 
        public static final Field INTEGER = new Field("integer"); 
        public static final Field FRACTION = new Field("fraction"); 
        public static final Field EXPONENT = new Field("exponent"); 
        public static final Field EXPONENT_SIGN = new Field("exponent sign"); 
        public static final Field EXPONENT_SYMBOL = new Field("exponent symbol"); 
        public static final Field DECIMAL_SEPARATOR = new Field(
                "decimal separator"); 
        public static final Field GROUPING_SEPARATOR = new Field(
                "grouping separator"); 
        public static final Field PERCENT = new Field("percent"); 
        public static final Field PERMILLE = new Field("per mille"); 
        public static final Field CURRENCY = new Field("currency"); 
        protected Field(String fieldName) {
            super(fieldName);
        }
        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (this.equals(INTEGER)) {
                return INTEGER;
            }
            if (this.equals(FRACTION)) {
                return FRACTION;
            }
            if (this.equals(EXPONENT)) {
                return EXPONENT;
            }
            if (this.equals(EXPONENT_SIGN)) {
                return EXPONENT_SIGN;
            }
            if (this.equals(EXPONENT_SYMBOL)) {
                return EXPONENT_SYMBOL;
            }
            if (this.equals(CURRENCY)) {
                return CURRENCY;
            }
            if (this.equals(DECIMAL_SEPARATOR)) {
                return DECIMAL_SEPARATOR;
            }
            if (this.equals(GROUPING_SEPARATOR)) {
                return GROUPING_SEPARATOR;
            }
            if (this.equals(PERCENT)) {
                return PERCENT;
            }
            if (this.equals(PERMILLE)) {
                return PERMILLE;
            }
            if (this.equals(SIGN)) {
                return SIGN;
            }
            throw new InvalidObjectException(Messages.getString("text.02")); 
        }
    }
}
