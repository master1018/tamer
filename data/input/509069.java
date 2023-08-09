public final class DecimalFormatSymbols implements Cloneable, Serializable {
    private static final long serialVersionUID = 5772796243397350300L;
    private final int ZeroDigit = 0, Digit = 1, DecimalSeparator = 2,
            GroupingSeparator = 3, PatternSeparator = 4, Percent = 5,
            PerMill = 6, Exponent = 7, MonetaryDecimalSeparator = 8,
            MinusSign = 9;
    private transient char[] patternChars;
    private transient Currency currency;
    private transient Locale locale;
    private String infinity, NaN, currencySymbol, intlCurrencySymbol;
    public DecimalFormatSymbols() {
        this(Locale.getDefault());
    }
    public DecimalFormatSymbols(Locale locale) {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        this.patternChars = localeData.decimalPatternChars.toCharArray();
        this.infinity = localeData.infinity;
        this.NaN = localeData.NaN;
        this.locale = locale;
        try {
            currency = Currency.getInstance(locale);
            currencySymbol = currency.getSymbol(locale);
            intlCurrencySymbol = currency.getCurrencyCode();
        } catch (IllegalArgumentException e) {
            currency = Currency.getInstance("XXX"); 
            currencySymbol = localeData.currencySymbol;
            intlCurrencySymbol = localeData.internationalCurrencySymbol;
        }
    }
    @Override
    public Object clone() {
        try {
            DecimalFormatSymbols symbols = (DecimalFormatSymbols) super.clone();
            symbols.patternChars = patternChars.clone();
            return symbols;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DecimalFormatSymbols)) {
            return false;
        }
        DecimalFormatSymbols obj = (DecimalFormatSymbols) object;
        return Arrays.equals(patternChars, obj.patternChars)
                && infinity.equals(obj.infinity) && NaN.equals(obj.NaN)
                && currencySymbol.equals(obj.currencySymbol)
                && intlCurrencySymbol.equals(obj.intlCurrencySymbol);
    }
    public Currency getCurrency() {
        return currency;
    }
    public String getInternationalCurrencySymbol() {
        return intlCurrencySymbol;
    }
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    public char getDecimalSeparator() {
        return patternChars[DecimalSeparator];
    }
    public char getDigit() {
        return patternChars[Digit];
    }
    public char getGroupingSeparator() {
        return patternChars[GroupingSeparator];
    }
    public String getInfinity() {
        return infinity;
    }
    public char getMinusSign() {
        return patternChars[MinusSign];
    }
    public char getMonetaryDecimalSeparator() {
        return patternChars[MonetaryDecimalSeparator];
    }
    public String getNaN() {
        return NaN;
    }
    public char getPatternSeparator() {
        return patternChars[PatternSeparator];
    }
    public char getPercent() {
        return patternChars[Percent];
    }
    public char getPerMill() {
        return patternChars[PerMill];
    }
    public char getZeroDigit() {
        return patternChars[ZeroDigit];
    }
    char getExponential() {
        return patternChars[Exponent];
    }
    @Override
    public int hashCode() {
        return new String(patternChars).hashCode() + infinity.hashCode()
                + NaN.hashCode() + currencySymbol.hashCode()
                + intlCurrencySymbol.hashCode();
    }
    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        if (currency == this.currency) {
            return;
        }
        this.currency = currency;
        intlCurrencySymbol = currency.getCurrencyCode();
        currencySymbol = currency.getSymbol(locale);
    }
    public void setInternationalCurrencySymbol(String value) {
        if (value == null) {
            currency = null;
            intlCurrencySymbol = null;
            return;
        }
        if (value.equals(intlCurrencySymbol)) {
            return;
        }
        try {
            currency = Currency.getInstance(value);
            currencySymbol = currency.getSymbol(locale);
        } catch (IllegalArgumentException e) {
            currency = null;
        }
        intlCurrencySymbol = value;
    }
    public void setCurrencySymbol(String value) {
        currencySymbol = value;
    }
    public void setDecimalSeparator(char value) {
        patternChars[DecimalSeparator] = value;
    }
    public void setDigit(char value) {
        patternChars[Digit] = value;
    }
    public void setGroupingSeparator(char value) {
        patternChars[GroupingSeparator] = value;
    }
    public void setInfinity(String value) {
        infinity = value;
    }
    public void setMinusSign(char value) {
        patternChars[MinusSign] = value;
    }
    public void setMonetaryDecimalSeparator(char value) {
        patternChars[MonetaryDecimalSeparator] = value;
    }
    public void setNaN(String value) {
        NaN = value;
    }
    public void setPatternSeparator(char value) {
        patternChars[PatternSeparator] = value;
    }
    public void setPercent(char value) {
        patternChars[Percent] = value;
    }
    public void setPerMill(char value) {
        patternChars[PerMill] = value;
    }
    public void setZeroDigit(char value) {
        patternChars[ZeroDigit] = value;
    }
    void setExponential(char value) {
        patternChars[Exponent] = value;
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("currencySymbol", String.class), 
            new ObjectStreamField("decimalSeparator", Character.TYPE), 
            new ObjectStreamField("digit", Character.TYPE), 
            new ObjectStreamField("exponential", Character.TYPE), 
            new ObjectStreamField("groupingSeparator", Character.TYPE), 
            new ObjectStreamField("infinity", String.class), 
            new ObjectStreamField("intlCurrencySymbol", String.class), 
            new ObjectStreamField("minusSign", Character.TYPE), 
            new ObjectStreamField("monetarySeparator", Character.TYPE), 
            new ObjectStreamField("NaN", String.class), 
            new ObjectStreamField("patternSeparator", Character.TYPE), 
            new ObjectStreamField("percent", Character.TYPE), 
            new ObjectStreamField("perMill", Character.TYPE), 
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), 
            new ObjectStreamField("zeroDigit", Character.TYPE), 
            new ObjectStreamField("locale", Locale.class), }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("currencySymbol", currencySymbol); 
        fields.put("decimalSeparator", getDecimalSeparator()); 
        fields.put("digit", getDigit()); 
        fields.put("exponential", getExponential()); 
        fields.put("groupingSeparator", getGroupingSeparator()); 
        fields.put("infinity", infinity); 
        fields.put("intlCurrencySymbol", intlCurrencySymbol); 
        fields.put("minusSign", getMinusSign()); 
        fields.put("monetarySeparator", getMonetaryDecimalSeparator()); 
        fields.put("NaN", NaN); 
        fields.put("patternSeparator", getPatternSeparator()); 
        fields.put("percent", getPercent()); 
        fields.put("perMill", getPerMill()); 
        fields.put("serialVersionOnStream", 1); 
        fields.put("zeroDigit", getZeroDigit()); 
        fields.put("locale", locale); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        patternChars = new char[10];
        currencySymbol = (String) fields.get("currencySymbol", ""); 
        setDecimalSeparator(fields.get("decimalSeparator", '.')); 
        setDigit(fields.get("digit", '#')); 
        setGroupingSeparator(fields.get("groupingSeparator", ',')); 
        infinity = (String) fields.get("infinity", ""); 
        intlCurrencySymbol = (String) fields.get("intlCurrencySymbol", ""); 
        setMinusSign(fields.get("minusSign", '-')); 
        NaN = (String) fields.get("NaN", ""); 
        setPatternSeparator(fields.get("patternSeparator", ';')); 
        setPercent(fields.get("percent", '%')); 
        setPerMill(fields.get("perMill", '\u2030')); 
        setZeroDigit(fields.get("zeroDigit", '0')); 
        locale = (Locale) fields.get("locale", null); 
        if (fields.get("serialVersionOnStream", 0) == 0) { 
            setMonetaryDecimalSeparator(getDecimalSeparator());
            setExponential('E');
        } else {
            setMonetaryDecimalSeparator(fields.get("monetarySeparator", '.')); 
            setExponential(fields.get("exponential", 'E')); 
        }
        try {
            currency = Currency.getInstance(intlCurrencySymbol);
        } catch (IllegalArgumentException e) {
            currency = null;
        }
    }
}
