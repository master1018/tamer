public final class MathContext implements Serializable {
    public static final MathContext DECIMAL128 = new MathContext(34,
            RoundingMode.HALF_EVEN);
    public static final MathContext DECIMAL32 = new MathContext(7,
            RoundingMode.HALF_EVEN);
    public static final MathContext DECIMAL64 = new MathContext(16,
            RoundingMode.HALF_EVEN);
    public static final MathContext UNLIMITED = new MathContext(0,
            RoundingMode.HALF_UP);
    private static final long serialVersionUID = 5579720004786848255L;
    private int precision;
    private RoundingMode roundingMode;
    private final static char[] chPrecision = { 'p', 'r', 'e', 'c', 'i', 's',
            'i', 'o', 'n', '=' };
    private final static char[] chRoundingMode = { 'r', 'o', 'u', 'n', 'd',
            'i', 'n', 'g', 'M', 'o', 'd', 'e', '=' };
    public MathContext(int precision) {
        this(precision, RoundingMode.HALF_UP);
    }
    public MathContext(int precision, RoundingMode roundingMode) {
        if (precision < 0) {
            throw new IllegalArgumentException(Messages.getString("math.0C")); 
        }
        if (roundingMode == null) {
            throw new NullPointerException(Messages.getString("math.0D")); 
        }
        this.precision = precision;
        this.roundingMode = roundingMode;
    }
    public MathContext(String val) {
        char[] charVal = val.toCharArray();
        int i; 
        int j; 
        int digit; 
        if ((charVal.length < 27) || (charVal.length > 45)) {
            throw new IllegalArgumentException(Messages.getString("math.0E")); 
        }
        for (i = 0; (i < chPrecision.length) && (charVal[i] == chPrecision[i]); i++) {
            ;
        }
        if (i < chPrecision.length) {
            throw new IllegalArgumentException(Messages.getString("math.0E")); 
        }
        digit = Character.digit(charVal[i], 10);
        if (digit == -1) {
            throw new IllegalArgumentException(Messages.getString("math.0E")); 
        }
        this.precision = digit;
        i++;
        do {
            digit = Character.digit(charVal[i], 10);
            if (digit == -1) {
                if (charVal[i] == ' ') {
                    i++;
                    break;
                }
                throw new IllegalArgumentException(Messages.getString("math.0E")); 
            }
            this.precision = this.precision * 10 + digit;
            if (this.precision < 0) {
                throw new IllegalArgumentException(Messages.getString("math.0E")); 
            }
            i++;
        } while (true);
        for (j = 0; (j < chRoundingMode.length)
                && (charVal[i] == chRoundingMode[j]); i++, j++) {
            ;
        }
        if (j < chRoundingMode.length) {
            throw new IllegalArgumentException(Messages.getString("math.0E")); 
        }
        this.roundingMode = RoundingMode.valueOf(String.valueOf(charVal, i,
                charVal.length - i));
    }
    public int getPrecision() {
        return precision;
    }
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
    @Override
    public boolean equals(Object x) {
        return ((x instanceof MathContext)
                && (((MathContext) x).getPrecision() == precision) && (((MathContext) x)
                .getRoundingMode() == roundingMode));
    }
    @Override
    public int hashCode() {
        return ((precision << 3) | roundingMode.ordinal());
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(45);
        sb.append(chPrecision);
        sb.append(precision);
        sb.append(' ');
        sb.append(chRoundingMode);
        sb.append(roundingMode);
        return sb.toString();
    }
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        if (precision < 0) {
            throw new StreamCorruptedException(Messages.getString("math.0F")); 
        }
        if (roundingMode == null) {
            throw new StreamCorruptedException(Messages.getString("math.10")); 
        }
    }
}
