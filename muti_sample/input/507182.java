public class DecimalFormat extends NumberFormat {
    private static final long serialVersionUID = 864413376551465018L;
    private transient boolean parseBigDecimal = false;
    private transient DecimalFormatSymbols symbols;
    private transient NativeDecimalFormat dform;
    public DecimalFormat() {
        Locale locale = Locale.getDefault();
        this.symbols = new DecimalFormatSymbols(locale);
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        initNative(localeData.numberPattern);
    }
    public DecimalFormat(String pattern) {
        this(pattern, Locale.getDefault());
    }
    public DecimalFormat(String pattern, DecimalFormatSymbols value) {
        this.symbols = (DecimalFormatSymbols) value.clone();
        initNative(pattern);
    }
    DecimalFormat(String pattern, Locale locale) {
        this.symbols = new DecimalFormatSymbols(locale);
        initNative(pattern);
    }
    private void initNative(String pattern) {
        try {
            this.dform = new NativeDecimalFormat(pattern, symbols);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(pattern);
        }
        super.setMaximumFractionDigits(dform.getMaximumFractionDigits());
        super.setMaximumIntegerDigits(dform.getMaximumIntegerDigits());
        super.setMinimumFractionDigits(dform.getMinimumFractionDigits());
        super.setMinimumIntegerDigits(dform.getMinimumIntegerDigits());
    }
    public void applyLocalizedPattern(String pattern) {
        dform.applyLocalizedPattern(pattern);
    }
    public void applyPattern(String pattern) {
        dform.applyPattern(pattern);
    }
    @Override
    public Object clone() {
        DecimalFormat clone = (DecimalFormat) super.clone();
        clone.dform = (NativeDecimalFormat) dform.clone();
        clone.symbols = (DecimalFormatSymbols) symbols.clone();
        return clone;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DecimalFormat)) {
            return false;
        }
        DecimalFormat other = (DecimalFormat) object;
        return (this.dform == null ? other.dform == null : this.dform.equals(other.dform)) &&
                getDecimalFormatSymbols().equals(other.getDecimalFormatSymbols());
    }
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return dform.formatToCharacterIterator(object);
    }
    @Override
    public StringBuffer format(double value, StringBuffer buffer, FieldPosition position) {
        return dform.format(value, buffer, position);
    }
    @Override
    public StringBuffer format(long value, StringBuffer buffer, FieldPosition position) {
        return dform.format(value, buffer, position);
    }
    @Override
    public final StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) number;
            if (bigInteger.bitLength() < 64) {
                return dform.format(bigInteger.longValue(), toAppendTo, pos);
            } else {
                return dform.formatBigInteger(bigInteger, toAppendTo, pos);
            }
        } else if (number instanceof BigDecimal) {
            return dform.formatBigDecimal((BigDecimal) number, toAppendTo, pos);
        }
        return super.format(number, toAppendTo, pos);
    }
    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return (DecimalFormatSymbols) symbols.clone();
    }
    @Override
    public Currency getCurrency() {
        return symbols.getCurrency();
    }
    public int getGroupingSize() {
        return dform.getGroupingSize();
    }
    public int getMultiplier() {
        return dform.getMultiplier();
    }
    public String getNegativePrefix() {
        return dform.getNegativePrefix();
    }
    public String getNegativeSuffix() {
        return dform.getNegativeSuffix();
    }
    public String getPositivePrefix() {
        return dform.getPositivePrefix();
    }
    public String getPositiveSuffix() {
        return dform.getPositiveSuffix();
    }
    @Override
    public int hashCode() {
        return dform.hashCode();
    }
    public boolean isDecimalSeparatorAlwaysShown() {
        return dform.isDecimalSeparatorAlwaysShown();
    }
    public boolean isParseBigDecimal() {
        return this.parseBigDecimal;
    }
    @Override
    public void setParseIntegerOnly(boolean value) {
        dform.setParseIntegerOnly(value);
    }
    @Override
    public boolean isParseIntegerOnly() {
        return dform.isParseIntegerOnly();
    }
    private static final Double NEGATIVE_ZERO_DOUBLE = new Double(-0.0);
    @Override
    public Number parse(String string, ParsePosition position) {
        Number number = dform.parse(string, position);
        if (null == number) {
            return null;
        }
        if (this.isParseBigDecimal()) {
            if (number instanceof Long) {
                return new BigDecimal(number.longValue());
            }
            if ((number instanceof Double) && !((Double) number).isInfinite()
                    && !((Double) number).isNaN()) {
                return new BigDecimal(number.toString());
            }
            if (number instanceof BigInteger) {
                return new BigDecimal(number.toString());
            }
            return number;
        }
        if ((number instanceof BigDecimal) || (number instanceof BigInteger)) {
            return new Double(number.doubleValue());
        }
        if (this.isParseIntegerOnly() && number.equals(NEGATIVE_ZERO_DOUBLE)) {
            return Long.valueOf(0); 
        }
        return number;
    }
    public void setDecimalFormatSymbols(DecimalFormatSymbols value) {
        if (value != null) {
            this.symbols = (DecimalFormatSymbols) value.clone();
            dform.setDecimalFormatSymbols(this.symbols);
        }
    }
    @Override
    public void setCurrency(Currency currency) {
        dform.setCurrency(Currency.getInstance(currency.getCurrencyCode()));
        symbols.setCurrency(currency);
    }
    public void setDecimalSeparatorAlwaysShown(boolean value) {
        dform.setDecimalSeparatorAlwaysShown(value);
    }
    public void setGroupingSize(int value) {
        dform.setGroupingSize(value);
    }
    @Override
    public void setGroupingUsed(boolean value) {
        dform.setGroupingUsed(value);
    }
    @Override
    public boolean isGroupingUsed() {
        return dform.isGroupingUsed();
    }
    @Override
    public void setMaximumFractionDigits(int value) {
        super.setMaximumFractionDigits(value);
        dform.setMaximumFractionDigits(getMaximumFractionDigits());
    }
    @Override
    public void setMaximumIntegerDigits(int value) {
        super.setMaximumIntegerDigits(value);
        dform.setMaximumIntegerDigits(getMaximumIntegerDigits());
    }
    @Override
    public void setMinimumFractionDigits(int value) {
        super.setMinimumFractionDigits(value);
        dform.setMinimumFractionDigits(getMinimumFractionDigits());
    }
    @Override
    public void setMinimumIntegerDigits(int value) {
        super.setMinimumIntegerDigits(value);
        dform.setMinimumIntegerDigits(getMinimumIntegerDigits());
    }
    public void setMultiplier(int value) {
        dform.setMultiplier(value);
    }
    public void setNegativePrefix(String value) {
        dform.setNegativePrefix(value);
    }
    public void setNegativeSuffix(String value) {
        dform.setNegativeSuffix(value);
    }
    public void setPositivePrefix(String value) {
        dform.setPositivePrefix(value);
    }
    public void setPositiveSuffix(String value) {
        dform.setPositiveSuffix(value);
    }
    public void setParseBigDecimal(boolean newValue) {
        this.parseBigDecimal = newValue;
    }
    public String toLocalizedPattern() {
        return dform.toLocalizedPattern();
    }
    public String toPattern() {
        return dform.toPattern();
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("positivePrefix", String.class), 
            new ObjectStreamField("positiveSuffix", String.class), 
            new ObjectStreamField("negativePrefix", String.class), 
            new ObjectStreamField("negativeSuffix", String.class), 
            new ObjectStreamField("posPrefixPattern", String.class), 
            new ObjectStreamField("posSuffixPattern", String.class), 
            new ObjectStreamField("negPrefixPattern", String.class), 
            new ObjectStreamField("negSuffixPattern", String.class), 
            new ObjectStreamField("multiplier", int.class), 
            new ObjectStreamField("groupingSize", byte.class), 
            new ObjectStreamField("groupingUsed", boolean.class), 
            new ObjectStreamField("decimalSeparatorAlwaysShown", boolean.class), 
            new ObjectStreamField("parseBigDecimal", boolean.class), 
            new ObjectStreamField("symbols", DecimalFormatSymbols.class), 
            new ObjectStreamField("useExponentialNotation", boolean.class), 
            new ObjectStreamField("minExponentDigits", byte.class), 
            new ObjectStreamField("maximumIntegerDigits", int.class), 
            new ObjectStreamField("minimumIntegerDigits", int.class), 
            new ObjectStreamField("maximumFractionDigits", int.class), 
            new ObjectStreamField("minimumFractionDigits", int.class), 
            new ObjectStreamField("serialVersionOnStream", int.class), }; 
    @SuppressWarnings("nls")
    private void writeObject(ObjectOutputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("positivePrefix", dform.getPositivePrefix());
        fields.put("positiveSuffix", dform.getPositiveSuffix());
        fields.put("negativePrefix", dform.getNegativePrefix());
        fields.put("negativeSuffix", dform.getNegativeSuffix());
        fields.put("posPrefixPattern", (String) null);
        fields.put("posSuffixPattern", (String) null);
        fields.put("negPrefixPattern", (String) null);
        fields.put("negSuffixPattern", (String) null);
        fields.put("multiplier", dform.getMultiplier());
        fields.put("groupingSize", (byte) dform.getGroupingSize());
        fields.put("groupingUsed", dform.isGroupingUsed());
        fields.put("decimalSeparatorAlwaysShown", dform
                .isDecimalSeparatorAlwaysShown());
        fields.put("parseBigDecimal", parseBigDecimal);
        fields.put("symbols", symbols);
        fields.put("useExponentialNotation", false);
        fields.put("minExponentDigits", (byte) 0);
        fields.put("maximumIntegerDigits", dform.getMaximumIntegerDigits());
        fields.put("minimumIntegerDigits", dform.getMinimumIntegerDigits());
        fields.put("maximumFractionDigits", dform.getMaximumFractionDigits());
        fields.put("minimumFractionDigits", dform.getMinimumFractionDigits());
        fields.put("serialVersionOnStream", 3);
        stream.writeFields();
    }
    @SuppressWarnings("nls")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        this.symbols = (DecimalFormatSymbols) fields.get("symbols", null);
        initNative("");
        dform.setPositivePrefix((String) fields.get("positivePrefix", ""));
        dform.setPositiveSuffix((String) fields.get("positiveSuffix", ""));
        dform.setNegativePrefix((String) fields.get("negativePrefix", "-"));
        dform.setNegativeSuffix((String) fields.get("negativeSuffix", ""));
        dform.setMultiplier(fields.get("multiplier", 1));
        dform.setGroupingSize(fields.get("groupingSize", (byte) 3));
        dform.setGroupingUsed(fields.get("groupingUsed", true));
        dform.setDecimalSeparatorAlwaysShown(fields.get("decimalSeparatorAlwaysShown", false));
        final int maximumIntegerDigits = fields.get("maximumIntegerDigits", 309);
        final int minimumIntegerDigits = fields.get("minimumIntegerDigits", 309);
        final int maximumFractionDigits = fields.get("maximumFractionDigits", 340);
        final int minimumFractionDigits = fields.get("minimumFractionDigits", 340);
        dform.setMaximumIntegerDigits(maximumIntegerDigits);
        super.setMaximumIntegerDigits(dform.getMaximumIntegerDigits());
        setMinimumIntegerDigits(minimumIntegerDigits);
        setMinimumFractionDigits(minimumFractionDigits);
        setMaximumFractionDigits(maximumFractionDigits);
        setParseBigDecimal(fields.get("parseBigDecimal", false));
        if (fields.get("serialVersionOnStream", 0) < 3) {
            setMaximumIntegerDigits(super.getMaximumIntegerDigits());
            setMinimumIntegerDigits(super.getMinimumIntegerDigits());
            setMaximumFractionDigits(super.getMaximumFractionDigits());
            setMinimumFractionDigits(super.getMinimumFractionDigits());
        }
    }
}
