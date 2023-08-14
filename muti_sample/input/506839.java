final class HexStringParser {
    private static final int DOUBLE_EXPONENT_WIDTH = 11;
    private static final int DOUBLE_MANTISSA_WIDTH = 52;
    private static final int FLOAT_EXPONENT_WIDTH = 8;
    private static final int FLOAT_MANTISSA_WIDTH = 23;
    private static final int HEX_RADIX = 16;
    private static final int MAX_SIGNIFICANT_LENGTH = 15;
    private static final String HEX_SIGNIFICANT = "0[xX](\\p{XDigit}+\\.?|\\p{XDigit}*\\.\\p{XDigit}+)"; 
    private static final String BINARY_EXPONENT = "[pP]([+-]?\\d+)"; 
    private static final String FLOAT_TYPE_SUFFIX = "[fFdD]?"; 
    private static final String HEX_PATTERN = "[\\x00-\\x20]*([+-]?)" + HEX_SIGNIFICANT 
            + BINARY_EXPONENT + FLOAT_TYPE_SUFFIX + "[\\x00-\\x20]*"; 
    private static final Pattern PATTERN = Pattern.compile(HEX_PATTERN);
    private final int EXPONENT_WIDTH;
    private final int MANTISSA_WIDTH;
    private final long EXPONENT_BASE;
    private final long MAX_EXPONENT;
    private final long MIN_EXPONENT;
    private final long MANTISSA_MASK;
    private long sign;
    private long exponent;
    private long mantissa;
    private String abandonedNumber=""; 
    public HexStringParser(int exponent_width, int mantissa_width) {
        this.EXPONENT_WIDTH = exponent_width;
        this.MANTISSA_WIDTH = mantissa_width;
        this.EXPONENT_BASE = ~(-1L << (exponent_width - 1));
        this.MAX_EXPONENT = ~(-1L << exponent_width);
        this.MIN_EXPONENT = -(MANTISSA_WIDTH + 1);
        this.MANTISSA_MASK = ~(-1L << mantissa_width);
    }
    public static double parseDouble(String hexString) {
        HexStringParser parser = new HexStringParser(DOUBLE_EXPONENT_WIDTH,
                DOUBLE_MANTISSA_WIDTH);
        long result = parser.parse(hexString);
        return Double.longBitsToDouble(result);
    }
    public static float parseFloat(String hexString) {
        HexStringParser parser = new HexStringParser(FLOAT_EXPONENT_WIDTH,
                FLOAT_MANTISSA_WIDTH);
        int result = (int) parser.parse(hexString);
        return Float.intBitsToFloat(result);
    }
    private long parse(String hexString) {
        String[] hexSegments = getSegmentsFromHexString(hexString);
        String signStr = hexSegments[0];
        String significantStr = hexSegments[1];
        String exponentStr = hexSegments[2];
        parseHexSign(signStr);
        parseExponent(exponentStr);
        parseMantissa(significantStr);
        sign <<= (MANTISSA_WIDTH + EXPONENT_WIDTH);
        exponent <<= MANTISSA_WIDTH;
        return sign | exponent | mantissa;
    }
    private static String[] getSegmentsFromHexString(String hexString) {
        Matcher matcher = PATTERN.matcher(hexString);
        if (!matcher.matches()) {
            throw new NumberFormatException();
        }
        String[] hexSegments = new String[3];
        hexSegments[0] = matcher.group(1);
        hexSegments[1] = matcher.group(2);
        hexSegments[2] = matcher.group(3);
        return hexSegments;
    }
    private void parseHexSign(String signStr) {
        this.sign = signStr.equals("-") ? 1 : 0; 
    }
    private void parseExponent(String exponentStr) {
        char leadingChar = exponentStr.charAt(0);
        int expSign = (leadingChar == '-' ? -1 : 1);
        if (!Character.isDigit(leadingChar)) {
            exponentStr = exponentStr.substring(1);
        }
        try {
            exponent = expSign * Long.parseLong(exponentStr);
            checkedAddExponent(EXPONENT_BASE);
        } catch (NumberFormatException e) {
            exponent = expSign * Long.MAX_VALUE;
        }
    }
    private void parseMantissa(String significantStr) {
        String[] strings = significantStr.split("\\."); 
        String strIntegerPart = strings[0];
        String strDecimalPart = strings.length > 1 ? strings[1] : ""; 
        String significand = getNormalizedSignificand(strIntegerPart,strDecimalPart);
        if (significand.equals("0")) { 
            setZero();
            return;
        }
        int offset = getOffset(strIntegerPart, strDecimalPart);
        checkedAddExponent(offset);
        if (exponent >= MAX_EXPONENT) {
            setInfinite();
            return;
        }
        if (exponent <= MIN_EXPONENT) {
            setZero();
            return;
        }
        if (significand.length() > MAX_SIGNIFICANT_LENGTH) {
            abandonedNumber = significand.substring(MAX_SIGNIFICANT_LENGTH);
            significand = significand.substring(0, MAX_SIGNIFICANT_LENGTH);
        }
        mantissa = Long.parseLong(significand, HEX_RADIX);
        if (exponent >= 1) {
            processNormalNumber();
        } else{
            processSubNormalNumber();
        }
    }
    private void setInfinite() {
        exponent = MAX_EXPONENT;
        mantissa = 0;
    }
    private void setZero() {
        exponent = 0;
        mantissa = 0;
    }
    private void checkedAddExponent(long offset) {
        long result = exponent + offset;
        int expSign = Long.signum(exponent);
        if (expSign * Long.signum(offset) > 0 && expSign * Long.signum(result) < 0) {
            exponent = expSign * Long.MAX_VALUE;
        } else {
            exponent = result;
        }
    }
    private void processNormalNumber(){
        int desiredWidth = MANTISSA_WIDTH + 2;
        fitMantissaInDesiredWidth(desiredWidth);
        round();
        mantissa = mantissa & MANTISSA_MASK;
    }
    private void processSubNormalNumber(){
        int desiredWidth = MANTISSA_WIDTH + 1;
        desiredWidth += (int)exponent;
        exponent = 0;
        fitMantissaInDesiredWidth(desiredWidth);
        round();
        mantissa = mantissa & MANTISSA_MASK;
    }
    private void fitMantissaInDesiredWidth(int desiredWidth){
        int bitLength = countBitsLength(mantissa);
        if (bitLength > desiredWidth) {
            discardTrailingBits(bitLength - desiredWidth);
        } else {
            mantissa <<= (desiredWidth - bitLength);
        }
    }
    private void discardTrailingBits(long num) {
        long mask = ~(-1L << num);
        abandonedNumber += (mantissa & mask);
        mantissa >>= num;
    }
    private void round() {
        String result = abandonedNumber.replaceAll("0+", ""); 
        boolean moreThanZero = (result.length() > 0 ? true : false);
        int lastDiscardedBit = (int) (mantissa & 1L);
        mantissa >>= 1;
        int tailBitInMantissa = (int) (mantissa & 1L);
        if (lastDiscardedBit == 1 && (moreThanZero || tailBitInMantissa == 1)) {
            int oldLength = countBitsLength(mantissa);
            mantissa += 1L;
            int newLength = countBitsLength(mantissa);
            if (oldLength >= MANTISSA_WIDTH && newLength > oldLength) {
                checkedAddExponent(1);
            }
        }
    }
    private String getNormalizedSignificand(String strIntegerPart, String strDecimalPart) {
        String significand = strIntegerPart + strDecimalPart;
        significand = significand.replaceFirst("^0+", ""); 
        if (significand.length() == 0) {
            significand = "0"; 
        }
        return significand;
    }
    private int getOffset(String strIntegerPart, String strDecimalPart) {
        strIntegerPart = strIntegerPart.replaceFirst("^0+", ""); 
        if (strIntegerPart.length() != 0) {
            String leadingNumber = strIntegerPart.substring(0, 1);
            return (strIntegerPart.length() - 1) * 4 + countBitsLength(Long.parseLong(leadingNumber,HEX_RADIX)) - 1;
        }
        int i;
        for (i = 0; i < strDecimalPart.length() && strDecimalPart.charAt(i) == '0'; i++);   
        if (i == strDecimalPart.length()) {
            return 0;
        }
        String leadingNumber=strDecimalPart.substring(i,i + 1);
        return (-i - 1) * 4 + countBitsLength(Long.parseLong(leadingNumber, HEX_RADIX)) - 1;
    }
    private int countBitsLength(long value) {
        int leadingZeros = Long.numberOfLeadingZeros(value);
        return Long.SIZE - leadingZeros;
    }
}
