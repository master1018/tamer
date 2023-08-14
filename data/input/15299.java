public abstract class NumberFormat extends Format  {
    public static final int INTEGER_FIELD = 0;
    public static final int FRACTION_FIELD = 1;
    protected NumberFormat() {
    }
    public StringBuffer format(Object number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        if (number instanceof Long || number instanceof Integer ||
            number instanceof Short || number instanceof Byte ||
            number instanceof AtomicInteger || number instanceof AtomicLong ||
            (number instanceof BigInteger &&
             ((BigInteger)number).bitLength() < 64)) {
            return format(((Number)number).longValue(), toAppendTo, pos);
        } else if (number instanceof Number) {
            return format(((Number)number).doubleValue(), toAppendTo, pos);
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Number");
        }
    }
    public final Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }
    public final String format(double number) {
        return format(number, new StringBuffer(),
                      DontCareFieldPosition.INSTANCE).toString();
    }
    public final String format(long number) {
        return format(number, new StringBuffer(),
                      DontCareFieldPosition.INSTANCE).toString();
    }
    public abstract StringBuffer format(double number,
                                        StringBuffer toAppendTo,
                                        FieldPosition pos);
    public abstract StringBuffer format(long number,
                                        StringBuffer toAppendTo,
                                        FieldPosition pos);
    public abstract Number parse(String source, ParsePosition parsePosition);
    public Number parse(String source) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number result = parse(source, parsePosition);
        if (parsePosition.index == 0) {
            throw new ParseException("Unparseable number: \"" + source + "\"",
                                     parsePosition.errorIndex);
        }
        return result;
    }
    public boolean isParseIntegerOnly() {
        return parseIntegerOnly;
    }
    public void setParseIntegerOnly(boolean value) {
        parseIntegerOnly = value;
    }
    public final static NumberFormat getInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), NUMBERSTYLE);
    }
    public static NumberFormat getInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }
    public final static NumberFormat getNumberInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), NUMBERSTYLE);
    }
    public static NumberFormat getNumberInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }
    public final static NumberFormat getIntegerInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), INTEGERSTYLE);
    }
    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return getInstance(inLocale, INTEGERSTYLE);
    }
    public final static NumberFormat getCurrencyInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), CURRENCYSTYLE);
    }
    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return getInstance(inLocale, CURRENCYSTYLE);
    }
    public final static NumberFormat getPercentInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), PERCENTSTYLE);
    }
    public static NumberFormat getPercentInstance(Locale inLocale) {
        return getInstance(inLocale, PERCENTSTYLE);
    }
     final static NumberFormat getScientificInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT), SCIENTIFICSTYLE);
    }
     static NumberFormat getScientificInstance(Locale inLocale) {
        return getInstance(inLocale, SCIENTIFICSTYLE);
    }
    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(NumberFormatProvider.class);
        return pool.getAvailableLocales();
    }
    public int hashCode() {
        return maximumIntegerDigits * 37 + maxFractionDigits;
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NumberFormat other = (NumberFormat) obj;
        return (maximumIntegerDigits == other.maximumIntegerDigits
            && minimumIntegerDigits == other.minimumIntegerDigits
            && maximumFractionDigits == other.maximumFractionDigits
            && minimumFractionDigits == other.minimumFractionDigits
            && groupingUsed == other.groupingUsed
            && parseIntegerOnly == other.parseIntegerOnly);
    }
    public Object clone() {
        NumberFormat other = (NumberFormat) super.clone();
        return other;
    }
    public boolean isGroupingUsed() {
        return groupingUsed;
    }
    public void setGroupingUsed(boolean newValue) {
        groupingUsed = newValue;
    }
    public int getMaximumIntegerDigits() {
        return maximumIntegerDigits;
    }
    public void setMaximumIntegerDigits(int newValue) {
        maximumIntegerDigits = Math.max(0,newValue);
        if (minimumIntegerDigits > maximumIntegerDigits) {
            minimumIntegerDigits = maximumIntegerDigits;
        }
    }
    public int getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }
    public void setMinimumIntegerDigits(int newValue) {
        minimumIntegerDigits = Math.max(0,newValue);
        if (minimumIntegerDigits > maximumIntegerDigits) {
            maximumIntegerDigits = minimumIntegerDigits;
        }
    }
    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }
    public void setMaximumFractionDigits(int newValue) {
        maximumFractionDigits = Math.max(0,newValue);
        if (maximumFractionDigits < minimumFractionDigits) {
            minimumFractionDigits = maximumFractionDigits;
        }
    }
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }
    public void setMinimumFractionDigits(int newValue) {
        minimumFractionDigits = Math.max(0,newValue);
        if (maximumFractionDigits < minimumFractionDigits) {
            maximumFractionDigits = minimumFractionDigits;
        }
    }
    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }
    public void setCurrency(Currency currency) {
        throw new UnsupportedOperationException();
    }
    public RoundingMode getRoundingMode() {
        throw new UnsupportedOperationException();
    }
    public void setRoundingMode(RoundingMode roundingMode) {
        throw new UnsupportedOperationException();
    }
    private static NumberFormat getInstance(Locale desiredLocale,
                                           int choice) {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(NumberFormatProvider.class);
        if (pool.hasProviders()) {
            NumberFormat providersInstance = pool.getLocalizedObject(
                                    NumberFormatGetter.INSTANCE,
                                    desiredLocale,
                                    choice);
            if (providersInstance != null) {
                return providersInstance;
            }
        }
        String[] numberPatterns = (String[])cachedLocaleData.get(desiredLocale);
        if (numberPatterns == null) { 
            ResourceBundle resource = LocaleData.getNumberFormatData(desiredLocale);
            numberPatterns = resource.getStringArray("NumberPatterns");
            cachedLocaleData.put(desiredLocale, numberPatterns);
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(desiredLocale);
        int entry = (choice == INTEGERSTYLE) ? NUMBERSTYLE : choice;
        DecimalFormat format = new DecimalFormat(numberPatterns[entry], symbols);
        if (choice == INTEGERSTYLE) {
            format.setMaximumFractionDigits(0);
            format.setDecimalSeparatorAlwaysShown(false);
            format.setParseIntegerOnly(true);
        } else if (choice == CURRENCYSTYLE) {
            format.adjustForCurrencyDefaultFractionDigits();
        }
        return format;
    }
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        if (serialVersionOnStream < 1) {
            maximumIntegerDigits = maxIntegerDigits;
            minimumIntegerDigits = minIntegerDigits;
            maximumFractionDigits = maxFractionDigits;
            minimumFractionDigits = minFractionDigits;
        }
        if (minimumIntegerDigits > maximumIntegerDigits ||
            minimumFractionDigits > maximumFractionDigits ||
            minimumIntegerDigits < 0 || minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        serialVersionOnStream = currentSerialVersion;
    }
    private void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        maxIntegerDigits = (maximumIntegerDigits > Byte.MAX_VALUE) ?
                           Byte.MAX_VALUE : (byte)maximumIntegerDigits;
        minIntegerDigits = (minimumIntegerDigits > Byte.MAX_VALUE) ?
                           Byte.MAX_VALUE : (byte)minimumIntegerDigits;
        maxFractionDigits = (maximumFractionDigits > Byte.MAX_VALUE) ?
                            Byte.MAX_VALUE : (byte)maximumFractionDigits;
        minFractionDigits = (minimumFractionDigits > Byte.MAX_VALUE) ?
                            Byte.MAX_VALUE : (byte)minimumFractionDigits;
        stream.defaultWriteObject();
    }
    private static final Hashtable cachedLocaleData = new Hashtable(3);
    private static final int NUMBERSTYLE = 0;
    private static final int CURRENCYSTYLE = 1;
    private static final int PERCENTSTYLE = 2;
    private static final int SCIENTIFICSTYLE = 3;
    private static final int INTEGERSTYLE = 4;
    private boolean groupingUsed = true;
    private byte    maxIntegerDigits = 40;
    private byte    minIntegerDigits = 1;
    private byte    maxFractionDigits = 3;    
    private byte    minFractionDigits = 0;
    private boolean parseIntegerOnly = false;
    private int    maximumIntegerDigits = 40;
    private int    minimumIntegerDigits = 1;
    private int    maximumFractionDigits = 3;    
    private int    minimumFractionDigits = 0;
    static final int currentSerialVersion = 1;
    private int serialVersionOnStream = currentSerialVersion;
    static final long serialVersionUID = -2308460125733713944L;
    public static class Field extends Format.Field {
        private static final long serialVersionUID = 7494728892700160890L;
        private static final Map instanceMap = new HashMap(11);
        protected Field(String name) {
            super(name);
            if (this.getClass() == NumberFormat.Field.class) {
                instanceMap.put(name, this);
            }
        }
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != NumberFormat.Field.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            Object instance = instanceMap.get(getName());
            if (instance != null) {
                return instance;
            } else {
                throw new InvalidObjectException("unknown attribute name");
            }
        }
        public static final Field INTEGER = new Field("integer");
        public static final Field FRACTION = new Field("fraction");
        public static final Field EXPONENT = new Field("exponent");
        public static final Field DECIMAL_SEPARATOR =
                            new Field("decimal separator");
        public static final Field SIGN = new Field("sign");
        public static final Field GROUPING_SEPARATOR =
                            new Field("grouping separator");
        public static final Field EXPONENT_SYMBOL = new
                            Field("exponent symbol");
        public static final Field PERCENT = new Field("percent");
        public static final Field PERMILLE = new Field("per mille");
        public static final Field CURRENCY = new Field("currency");
        public static final Field EXPONENT_SIGN = new Field("exponent sign");
    }
    private static class NumberFormatGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<NumberFormatProvider,
                                                                   NumberFormat> {
        private static final NumberFormatGetter INSTANCE = new NumberFormatGetter();
        public NumberFormat getObject(NumberFormatProvider numberFormatProvider,
                                Locale locale,
                                String key,
                                Object... params) {
            assert params.length == 1;
            int choice = (Integer)params[0];
            switch (choice) {
            case NUMBERSTYLE:
                return numberFormatProvider.getNumberInstance(locale);
            case PERCENTSTYLE:
                return numberFormatProvider.getPercentInstance(locale);
            case CURRENCYSTYLE:
                return numberFormatProvider.getCurrencyInstance(locale);
            case INTEGERSTYLE:
                return numberFormatProvider.getIntegerInstance(locale);
            default:
                assert false : choice;
            }
            return null;
        }
    }
}
