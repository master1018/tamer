public class NumberFormatter extends InternationalFormatter {
    private String specialChars;
    public NumberFormatter() {
        this(NumberFormat.getNumberInstance());
    }
    public NumberFormatter(NumberFormat format) {
        super(format);
        setFormat(format);
        setAllowsInvalid(true);
        setCommitsOnValidEdit(false);
        setOverwriteMode(false);
    }
    public void setFormat(Format format) {
        super.setFormat(format);
        DecimalFormatSymbols dfs = getDecimalFormatSymbols();
        if (dfs != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(dfs.getCurrencySymbol());
            sb.append(dfs.getDecimalSeparator());
            sb.append(dfs.getGroupingSeparator());
            sb.append(dfs.getInfinity());
            sb.append(dfs.getInternationalCurrencySymbol());
            sb.append(dfs.getMinusSign());
            sb.append(dfs.getMonetaryDecimalSeparator());
            sb.append(dfs.getNaN());
            sb.append(dfs.getPercent());
            sb.append('+');
            specialChars = sb.toString();
        }
        else {
            specialChars = "";
        }
    }
    Object stringToValue(String text, Format f) throws ParseException {
        if (f == null) {
            return text;
        }
        Object value = f.parseObject(text);
        return convertValueToValueClass(value, getValueClass());
    }
    private Object convertValueToValueClass(Object value, Class valueClass) {
        if (valueClass != null && (value instanceof Number)) {
            Number numberValue = (Number)value;
            if (valueClass == Integer.class) {
                return Integer.valueOf(numberValue.intValue());
            }
            else if (valueClass == Long.class) {
                return Long.valueOf(numberValue.longValue());
            }
            else if (valueClass == Float.class) {
                return Float.valueOf(numberValue.floatValue());
            }
            else if (valueClass == Double.class) {
                return Double.valueOf(numberValue.doubleValue());
            }
            else if (valueClass == Byte.class) {
                return Byte.valueOf(numberValue.byteValue());
            }
            else if (valueClass == Short.class) {
                return Short.valueOf(numberValue.shortValue());
            }
        }
        return value;
    }
    private char getPositiveSign() {
        return '+';
    }
    private char getMinusSign() {
        DecimalFormatSymbols dfs = getDecimalFormatSymbols();
        if (dfs != null) {
            return dfs.getMinusSign();
        }
        return '-';
    }
    private char getDecimalSeparator() {
        DecimalFormatSymbols dfs = getDecimalFormatSymbols();
        if (dfs != null) {
            return dfs.getDecimalSeparator();
        }
        return '.';
    }
    private DecimalFormatSymbols getDecimalFormatSymbols() {
        Format f = getFormat();
        if (f instanceof DecimalFormat) {
            return ((DecimalFormat)f).getDecimalFormatSymbols();
        }
        return null;
    }
    boolean isLegalInsertText(String text) {
        if (getAllowsInvalid()) {
            return true;
        }
        for (int counter = text.length() - 1; counter >= 0; counter--) {
            char aChar = text.charAt(counter);
            if (!Character.isDigit(aChar) &&
                           specialChars.indexOf(aChar) == -1){
                return false;
            }
        }
        return true;
    }
    boolean isLiteral(Map attrs) {
        if (!super.isLiteral(attrs)) {
            if (attrs == null) {
                return false;
            }
            int size = attrs.size();
            if (attrs.get(NumberFormat.Field.GROUPING_SEPARATOR) != null) {
                size--;
                if (attrs.get(NumberFormat.Field.INTEGER) != null) {
                    size--;
                }
            }
            if (attrs.get(NumberFormat.Field.EXPONENT_SYMBOL) != null) {
                size--;
            }
            if (attrs.get(NumberFormat.Field.PERCENT) != null) {
                size--;
            }
            if (attrs.get(NumberFormat.Field.PERMILLE) != null) {
                size--;
            }
            if (attrs.get(NumberFormat.Field.CURRENCY) != null) {
                size--;
            }
            if (attrs.get(NumberFormat.Field.SIGN) != null) {
                size--;
            }
            return size == 0;
        }
        return true;
    }
    boolean isNavigatable(int index) {
        if (!super.isNavigatable(index)) {
            return getBufferedChar(index) == getDecimalSeparator();
        }
        return true;
    }
    private NumberFormat.Field getFieldFrom(int index, int direction) {
        if (isValidMask()) {
            int max = getFormattedTextField().getDocument().getLength();
            AttributedCharacterIterator iterator = getIterator();
            if (index >= max) {
                index += direction;
            }
            while (index >= 0 && index < max) {
                iterator.setIndex(index);
                Map attrs = iterator.getAttributes();
                if (attrs != null && attrs.size() > 0) {
                    for (Object key : attrs.keySet()) {
                        if (key instanceof NumberFormat.Field) {
                            return (NumberFormat.Field)key;
                        }
                    }
                }
                index += direction;
            }
        }
        return null;
    }
    void replace(DocumentFilter.FilterBypass fb, int offset, int length,
                String string, AttributeSet attr) throws BadLocationException {
        if (!getAllowsInvalid() && length == 0 && string != null &&
            string.length() == 1 &&
            toggleSignIfNecessary(fb, offset, string.charAt(0))) {
            return;
        }
        super.replace(fb, offset, length, string, attr);
    }
    private boolean toggleSignIfNecessary(DocumentFilter.FilterBypass fb,
                                              int offset, char aChar) throws
                              BadLocationException {
        if (aChar == getMinusSign() || aChar == getPositiveSign()) {
            NumberFormat.Field field = getFieldFrom(offset, -1);
            Object newValue;
            try {
                if (field == null ||
                    (field != NumberFormat.Field.EXPONENT &&
                     field != NumberFormat.Field.EXPONENT_SYMBOL &&
                     field != NumberFormat.Field.EXPONENT_SIGN)) {
                    newValue = toggleSign((aChar == getPositiveSign()));
                }
                else {
                    newValue = toggleExponentSign(offset, aChar);
                }
                if (newValue != null && isValidValue(newValue, false)) {
                    int lc = getLiteralCountTo(offset);
                    String string = valueToString(newValue);
                    fb.remove(0, fb.getDocument().getLength());
                    fb.insertString(0, string, null);
                    updateValue(newValue);
                    repositionCursor(getLiteralCountTo(offset) -
                                     lc + offset, 1);
                    return true;
                }
            } catch (ParseException pe) {
                invalidEdit();
            }
        }
        return false;
    }
    private Object toggleSign(boolean positive) throws ParseException {
        Object value = stringToValue(getFormattedTextField().getText());
        if (value != null) {
            String string = value.toString();
            if (string != null && string.length() > 0) {
                if (positive) {
                    if (string.charAt(0) == '-') {
                        string = string.substring(1);
                    }
                }
                else {
                    if (string.charAt(0) == '+') {
                        string = string.substring(1);
                    }
                    if (string.length() > 0 && string.charAt(0) != '-') {
                        string = "-" + string;
                    }
                }
                if (string != null) {
                    Class<?> valueClass = getValueClass();
                    if (valueClass == null) {
                        valueClass = value.getClass();
                    }
                    try {
                        Constructor cons = valueClass.getConstructor(
                                              new Class[] { String.class });
                        if (cons != null) {
                            return cons.newInstance(new Object[]{string});
                        }
                    } catch (Throwable ex) { }
                }
            }
        }
        return null;
    }
    private Object toggleExponentSign(int offset, char aChar) throws
                             BadLocationException, ParseException {
        String string = getFormattedTextField().getText();
        int replaceLength = 0;
        int loc = getAttributeStart(NumberFormat.Field.EXPONENT_SIGN);
        if (loc >= 0) {
            replaceLength = 1;
            offset = loc;
        }
        if (aChar == getPositiveSign()) {
            string = getReplaceString(offset, replaceLength, null);
        }
        else {
            string = getReplaceString(offset, replaceLength,
                                      new String(new char[] { aChar }));
        }
        return stringToValue(string);
    }
}
