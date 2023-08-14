public class RuleBasedNumberFormat extends NumberFormat {
    public enum RBNFType {
        SPELLOUT(0),
        ORDINAL(1),
        DURATION(2);
        int type;
        RBNFType(int t) {
            type = t;
        }
        int getType() {
            return type;
        }
    }
    @Override
    protected void finalize(){
        close();
    }
    private int addr = 0;
    public void open(RBNFType type) {
        this.addr = openRBNFImpl(type.getType(),
                Locale.getDefault().toString());
    }
    public void open(RBNFType type, Locale locale) {
        String loc = locale.toString();
        if (loc == null) {
            throw new NullPointerException();
        }
        this.addr = openRBNFImpl(type.getType(), loc);
    }
    private static native int openRBNFImpl(int type, String loc);
    public void open(String rule) {
        if (rule == null) {
            throw new NullPointerException();
        }
        this.addr = openRBNFImpl(rule, Locale.getDefault().toString());
    }
    public void open(String rule, Locale locale) {
        String loc = locale.toString();
        if (loc == null || rule == null) {
            throw new NullPointerException();
        }
        this.addr = openRBNFImpl(rule, locale.toString());
    }
    private static native int openRBNFImpl(String rule, String loc);
    public void close() {
        if(this.addr != 0) {
            closeRBNFImpl(this.addr);
            this.addr = 0;
        }
    }
    private static native void closeRBNFImpl(int addr); 
    @Override
    public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
        if(buffer == null) {
            throw new NullPointerException();
        }
        String fieldType = null;
        if(field != null) {
            fieldType = getFieldType(field.getFieldAttribute());
        }
        String result = formatRBNFImpl(this.addr, value, field, 
                fieldType, null);
        buffer.append(result.toCharArray(), 0, result.length());
        return buffer;
    }
    private static native String formatRBNFImpl(int addr, long value, 
            FieldPosition field, String fieldType, StringBuffer buffer);
    @Override
    public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
        if(buffer == null) {
            throw new NullPointerException();
        }
        String fieldType = null;
        if(field != null) {
            fieldType = getFieldType(field.getFieldAttribute());
        }
        String result = formatRBNFImpl(this.addr, value, field, 
                fieldType, null);
        buffer.append(result.toCharArray(), 0, result.length());
        return buffer;
    }
    private static native String formatRBNFImpl(int addr, double value, 
            FieldPosition field, String fieldType, StringBuffer buffer);
    @Override
    public Number parse(String string, ParsePosition position) {
        if (string == null || position == null) {
            throw new NullPointerException();
        }
        return parseRBNFImpl(this.addr, string, position, false);
    }
    public Number parseLenient(String string, ParsePosition position) {
        if (string == null || position == null) {
            throw new NullPointerException();
        }
        return parseRBNFImpl(this.addr, string, position, true);
    }
    static native Number parseRBNFImpl(int addr, String string, ParsePosition position, boolean lenient);
    static private String getFieldType(Format.Field field) {
        if(field == null) {
            return null;
        }
        if(field.equals(NumberFormat.Field.SIGN)) {
            return "sign";
        }
        if(field.equals(NumberFormat.Field.INTEGER)) {
            return "integer";
        }
        if(field.equals(NumberFormat.Field.FRACTION)) {
            return "fraction";
        }
        if(field.equals(NumberFormat.Field.EXPONENT)) {
            return "exponent";
        }
        if(field.equals(NumberFormat.Field.EXPONENT_SIGN)) {
            return "exponent_sign";
        }
        if(field.equals(NumberFormat.Field.EXPONENT_SYMBOL)) {
            return "exponent_symbol";
        }
        if(field.equals(NumberFormat.Field.CURRENCY)) {
            return "currency";
        }
        if(field.equals(NumberFormat.Field.GROUPING_SEPARATOR)) {
            return "grouping_separator";
        }
        if(field.equals(NumberFormat.Field.DECIMAL_SEPARATOR)) {
            return "decimal_separator";
        }
        if(field.equals(NumberFormat.Field.PERCENT)) {
            return "percent";
        }
        if(field.equals(NumberFormat.Field.PERMILLE)) {
            return "permille";
        }
        return null;
    }
}
