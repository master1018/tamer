public class BigDecimal extends Number implements Comparable<BigDecimal>, Serializable {
    public static final BigDecimal ZERO = new BigDecimal(0, 0);
    public static final BigDecimal ONE = new BigDecimal(1, 0);
    public static final BigDecimal TEN = new BigDecimal(10, 0);
    public static final int ROUND_UP = 0;
    public static final int ROUND_DOWN = 1;
    public static final int ROUND_CEILING = 2;
    public static final int ROUND_FLOOR = 3;
    public static final int ROUND_HALF_UP = 4;
    public static final int ROUND_HALF_DOWN = 5;
    public static final int ROUND_HALF_EVEN = 6;
    public static final int ROUND_UNNECESSARY = 7;
    private static final long serialVersionUID = 6108874887143696463L;
    private static final double LOG10_2 = 0.3010299956639812;
    private transient String toStringImage = null;
    private transient int hashCode = 0;
    private static final BigInteger FIVE_POW[];
    private static final BigInteger TEN_POW[];
    private static final long[] LONG_TEN_POW = new long[]
    {   1L,
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L,
        10000000L,
        100000000L,
        1000000000L,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L,
        10000000000000000L,
        100000000000000000L,
        1000000000000000000L, };
    private static final long[] LONG_FIVE_POW = new long[]
    {   1L,
        5L,
        25L,
        125L,
        625L,
        3125L,
        15625L,
        78125L,
        390625L,
        1953125L,
        9765625L,
        48828125L,
        244140625L,
        1220703125L,
        6103515625L,
        30517578125L,
        152587890625L,
        762939453125L,
        3814697265625L,
        19073486328125L,
        95367431640625L,
        476837158203125L,
        2384185791015625L,
        11920928955078125L,
        59604644775390625L,
        298023223876953125L,
        1490116119384765625L,
        7450580596923828125L, };
    private static final int[] LONG_FIVE_POW_BIT_LENGTH = new int[LONG_FIVE_POW.length];
    private static final int[] LONG_TEN_POW_BIT_LENGTH = new int[LONG_TEN_POW.length];
    private static final int BI_SCALED_BY_ZERO_LENGTH = 11;
    private static final BigDecimal BI_SCALED_BY_ZERO[] = new BigDecimal[BI_SCALED_BY_ZERO_LENGTH];
    private static final BigDecimal ZERO_SCALED_BY[] = new BigDecimal[11];
    private static final char[] CH_ZEROS = new char[100];
    static {
        int i = 0;
        for (; i < ZERO_SCALED_BY.length; i++) {
            BI_SCALED_BY_ZERO[i] = new BigDecimal(i, 0);
            ZERO_SCALED_BY[i] = new BigDecimal(0, i);
            CH_ZEROS[i] = '0';
        }
        for (; i < CH_ZEROS.length; i++) {
            CH_ZEROS[i] = '0';
        }
        for(int j=0; j<LONG_FIVE_POW_BIT_LENGTH.length; j++) {
            LONG_FIVE_POW_BIT_LENGTH[j] = bitLength(LONG_FIVE_POW[j]);
        }
        for(int j=0; j<LONG_TEN_POW_BIT_LENGTH.length; j++) {
            LONG_TEN_POW_BIT_LENGTH[j] = bitLength(LONG_TEN_POW[j]);
        }
        TEN_POW = Multiplication.bigTenPows;
        FIVE_POW = Multiplication.bigFivePows;
    }
    private BigInteger intVal;
    private transient int bitLength;
    private transient long smallValue;
    private int scale;
    private transient int precision = 0;
    private BigDecimal(long smallValue, int scale){
        this.smallValue = smallValue;
        this.scale = scale;
        this.bitLength = bitLength(smallValue);
    }
    private BigDecimal(int smallValue, int scale){
        this.smallValue = smallValue;
        this.scale = scale;
        this.bitLength = bitLength(smallValue);
    }
    public BigDecimal(char[] in, int offset, int len) {
        int begin = offset; 
        int last = offset + (len - 1); 
        String scaleString = null; 
        StringBuilder unscaledBuffer; 
        long newScale; 
        if (in == null) {
            throw new NullPointerException();
        }
        if ((last >= in.length) || (offset < 0) || (len <= 0) || (last < 0)) {
            throw new NumberFormatException();
        }
        unscaledBuffer = new StringBuilder(len);
        int bufLength = 0;
        if ((offset <= last) && (in[offset] == '+')) {
            offset++;
            begin++;
        }
        int counter = 0;
        boolean wasNonZero = false;
        for (; (offset <= last) && (in[offset] != '.')
        && (in[offset] != 'e') && (in[offset] != 'E'); offset++) {
            if (!wasNonZero) {
                if (in[offset] == '0') {
                    counter++;
                } else {
                    wasNonZero = true;
                }
            }
        }
        unscaledBuffer.append(in, begin, offset - begin);
        bufLength += offset - begin;
        if ((offset <= last) && (in[offset] == '.')) {
            offset++;
            begin = offset;
            for (; (offset <= last) && (in[offset] != 'e')
            && (in[offset] != 'E'); offset++) {
                if (!wasNonZero) {
                    if (in[offset] == '0') {
                        counter++;
                    } else {
                        wasNonZero = true;
                    }
                }
            }
            scale = offset - begin;
            bufLength +=scale;
            unscaledBuffer.append(in, begin, scale);
        } else {
            scale = 0;
        }
        if ((offset <= last) && ((in[offset] == 'e') || (in[offset] == 'E'))) {
            offset++;
            begin = offset;
            if ((offset <= last) && (in[offset] == '+')) {
                offset++;
                if ((offset <= last) && (in[offset] != '-')) {
                    begin++;
                }
            }
            scaleString = String.valueOf(in, begin, last + 1 - begin);
            newScale = (long)scale - Integer.parseInt(scaleString);
            scale = (int)newScale;
            if (newScale != scale) {
                throw new NumberFormatException(Messages.getString("math.02")); 
            }
        }
        if (bufLength < 19) {
            smallValue = Long.parseLong(unscaledBuffer.toString());
            bitLength = bitLength(smallValue);
        } else {
            setUnscaledValue(new BigInteger(unscaledBuffer.toString()));
        }
        precision = unscaledBuffer.length() - counter;
        if (unscaledBuffer.charAt(0) == '-') {
            precision --;
        }
    }
    public BigDecimal(char[] in, int offset, int len, MathContext mc) {
        this(in, offset, len);
        inplaceRound(mc);
    }
    public BigDecimal(char[] in) {
        this(in, 0, in.length);
    }
    public BigDecimal(char[] in, MathContext mc) {
        this(in, 0, in.length);
        inplaceRound(mc);
    }
    public BigDecimal(String val) {
        this(val.toCharArray(), 0, val.length());
    }
    public BigDecimal(String val, MathContext mc) {
        this(val.toCharArray(), 0, val.length());
        inplaceRound(mc);
    }
    public BigDecimal(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val)) {
            throw new NumberFormatException(Messages.getString("math.03")); 
        }
        long bits = Double.doubleToLongBits(val); 
        long mantisa;
        int trailingZeros;
        scale = 1075 - (int)((bits >> 52) & 0x7FFL);
        mantisa = (scale == 1075) ? (bits & 0xFFFFFFFFFFFFFL) << 1
                : (bits & 0xFFFFFFFFFFFFFL) | 0x10000000000000L;
        if (mantisa == 0) {
            scale = 0;
            precision = 1;
        }
        if (scale > 0) {
            trailingZeros = Math.min(scale, Long.numberOfTrailingZeros(mantisa));
            mantisa >>>= trailingZeros;
            scale -= trailingZeros;
        }
        if((bits >> 63) != 0) {
            mantisa = -mantisa;
        }
        int mantisaBits = bitLength(mantisa);
        if (scale < 0) {
            bitLength = mantisaBits == 0 ? 0 : mantisaBits - scale;
            if(bitLength < 64) {
                smallValue = mantisa << (-scale);
            } else {
                BigInt bi = new BigInt();
                bi.putLongInt(mantisa);
                bi.shift(-scale);
                intVal = new BigInteger(bi);
            }
            scale = 0;
        } else if (scale > 0) {
            if(scale < LONG_FIVE_POW.length
                    && mantisaBits+LONG_FIVE_POW_BIT_LENGTH[scale] < 64) {
                smallValue = mantisa * LONG_FIVE_POW[scale];
                bitLength = bitLength(smallValue);
            } else {
                setUnscaledValue(Multiplication.multiplyByFivePow(BigInteger.valueOf(mantisa), scale));
            }
        } else { 
            smallValue = mantisa;
            bitLength = mantisaBits;
        }
    }
    public BigDecimal(double val, MathContext mc) {
        this(val);
        inplaceRound(mc);
    }
    public BigDecimal(BigInteger val) {
        this(val, 0);
    }
    public BigDecimal(BigInteger val, MathContext mc) {
        this(val);
        inplaceRound(mc);
    }
    public BigDecimal(BigInteger unscaledVal, int scale) {
        if (unscaledVal == null) {
            throw new NullPointerException();
        }
        this.scale = scale;
        setUnscaledValue(unscaledVal);
    }
    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        this(unscaledVal, scale);
        inplaceRound(mc);
    }
    public BigDecimal(int val) {
        this(val,0);
    }
    public BigDecimal(int val, MathContext mc) {
        this(val,0);
        inplaceRound(mc);
    }
    public BigDecimal(long val) {
        this(val,0);
    }
    public BigDecimal(long val, MathContext mc) {
        this(val);
        inplaceRound(mc);
    }
    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0) {
            return valueOf(unscaledVal);
        }
        if ((unscaledVal == 0) && (scale >= 0)
                && (scale < ZERO_SCALED_BY.length)) {
            return ZERO_SCALED_BY[scale];
        }
        return new BigDecimal(unscaledVal, scale);
    }
    public static BigDecimal valueOf(long unscaledVal) {
        if ((unscaledVal >= 0) && (unscaledVal < BI_SCALED_BY_ZERO_LENGTH)) {
            return BI_SCALED_BY_ZERO[(int)unscaledVal];
        }
        return new BigDecimal(unscaledVal,0);
    }
    public static BigDecimal valueOf(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val)) {
            throw new NumberFormatException(Messages.getString("math.03")); 
        }
        return new BigDecimal(Double.toString(val));
    }
    public BigDecimal add(BigDecimal augend) {
        int diffScale = this.scale - augend.scale;
        if (this.isZero()) {
            if (diffScale <= 0) {
                return augend;
            }
            if (augend.isZero()) {
                return this;
            }
        } else if (augend.isZero()) {
            if (diffScale >= 0) {
                return this;
            }
        }
        if (diffScale == 0) {
            if (Math.max(this.bitLength, augend.bitLength) + 1 < 64) {
                return valueOf(this.smallValue + augend.smallValue, this.scale);
            }
            return new BigDecimal(this.getUnscaledValue().add(augend.getUnscaledValue()), this.scale);
        } else if (diffScale > 0) {
            return addAndMult10(this, augend, diffScale);
        } else {
            return addAndMult10(augend, this, -diffScale);
        }
    }
    private static BigDecimal addAndMult10(BigDecimal thisValue,BigDecimal augend, int diffScale) {
        if(diffScale < LONG_TEN_POW.length &&
                Math.max(thisValue.bitLength,augend.bitLength+LONG_TEN_POW_BIT_LENGTH[diffScale])+1<64) {
            return valueOf(thisValue.smallValue+augend.smallValue*LONG_TEN_POW[diffScale],thisValue.scale);
        } else {
            BigInt bi = Multiplication.multiplyByTenPow(augend.getUnscaledValue(),diffScale).bigInt;
            bi.add(thisValue.getUnscaledValue().bigInt);
            return new BigDecimal(new BigInteger(bi), thisValue.scale);
        }
    }
    public BigDecimal add(BigDecimal augend, MathContext mc) {
        BigDecimal larger; 
        BigDecimal smaller; 
        BigInteger tempBI;
        long diffScale = (long)this.scale - augend.scale;
        int largerSignum;
        if ((augend.isZero()) || (this.isZero())
                || (mc.getPrecision() == 0)) {
            return add(augend).round(mc);
        }
        if (this.aproxPrecision() < diffScale - 1) {
            larger = augend;
            smaller = this;
        } else if (augend.aproxPrecision() < -diffScale - 1) {
            larger = this;
            smaller = augend;
        } else {
            return add(augend).round(mc);
        }
        if (mc.getPrecision() >= larger.aproxPrecision()) {
            return add(augend).round(mc);
        }
        largerSignum = larger.signum();
        if (largerSignum == smaller.signum()) {
            tempBI = Multiplication.multiplyByPositiveInt(larger.getUnscaledValue(),10)
            .add(BigInteger.valueOf(largerSignum));
        } else {
            tempBI = larger.getUnscaledValue().subtract(
                    BigInteger.valueOf(largerSignum));
            tempBI = Multiplication.multiplyByPositiveInt(tempBI,10)
            .add(BigInteger.valueOf(largerSignum * 9));
        }
        larger = new BigDecimal(tempBI, larger.scale + 1);
        return larger.round(mc);
    }
    public BigDecimal subtract(BigDecimal subtrahend) {
        int diffScale = this.scale - subtrahend.scale;
        if (this.isZero()) {
            if (diffScale <= 0) {
                return subtrahend.negate();
            }
            if (subtrahend.isZero()) {
                return this;
            }
        } else if (subtrahend.isZero()) {
            if (diffScale >= 0) {
                return this;
            }
        }
        if (diffScale == 0) {
            if (Math.max(this.bitLength, subtrahend.bitLength) + 1 < 64) {
                return valueOf(this.smallValue - subtrahend.smallValue,this.scale);
            }
            return new BigDecimal(this.getUnscaledValue().subtract(subtrahend.getUnscaledValue()), this.scale);
        } else if (diffScale > 0) {
            if(diffScale < LONG_TEN_POW.length &&
                    Math.max(this.bitLength,subtrahend.bitLength+LONG_TEN_POW_BIT_LENGTH[diffScale])+1<64) {
                return valueOf(this.smallValue-subtrahend.smallValue*LONG_TEN_POW[diffScale],this.scale);
            }
            return new BigDecimal(this.getUnscaledValue().subtract(
                    Multiplication.multiplyByTenPow(subtrahend.getUnscaledValue(),diffScale)), this.scale);
        } else {
            diffScale = -diffScale;
            if(diffScale < LONG_TEN_POW.length &&
                    Math.max(this.bitLength+LONG_TEN_POW_BIT_LENGTH[diffScale],subtrahend.bitLength)+1<64) {
                return valueOf(this.smallValue*LONG_TEN_POW[diffScale]-subtrahend.smallValue,subtrahend.scale);
            }
            return new BigDecimal(Multiplication.multiplyByTenPow(this.getUnscaledValue(),diffScale)
            .subtract(subtrahend.getUnscaledValue()), subtrahend.scale);
        }
    }
    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        long diffScale = subtrahend.scale - (long)this.scale;
        int thisSignum;
        BigDecimal leftOperand; 
        BigInteger tempBI;
        if ((subtrahend.isZero()) || (this.isZero())
                || (mc.getPrecision() == 0)) {
            return subtract(subtrahend).round(mc);
        }
        if (subtrahend.aproxPrecision() < diffScale - 1) {
            if (mc.getPrecision() < this.aproxPrecision()) {
                thisSignum = this.signum();
                if (thisSignum != subtrahend.signum()) {
                    tempBI = Multiplication.multiplyByPositiveInt(this.getUnscaledValue(), 10)
                    .add(BigInteger.valueOf(thisSignum));
                } else {
                    tempBI = this.getUnscaledValue().subtract(BigInteger.valueOf(thisSignum));
                    tempBI = Multiplication.multiplyByPositiveInt(tempBI, 10)
                    .add(BigInteger.valueOf(thisSignum * 9));
                }
                leftOperand = new BigDecimal(tempBI, this.scale + 1);
                return leftOperand.round(mc);
            }
        }
        return subtract(subtrahend).round(mc);
    }
    public BigDecimal multiply(BigDecimal multiplicand) {
        long newScale = (long)this.scale + multiplicand.scale;
        if ((this.isZero()) || (multiplicand.isZero())) {
            return zeroScaledBy(newScale);
        }
        if(this.bitLength + multiplicand.bitLength < 64) {
            return valueOf(this.smallValue*multiplicand.smallValue,toIntScale(newScale));
        }
        return new BigDecimal(this.getUnscaledValue().multiply(
                multiplicand.getUnscaledValue()), toIntScale(newScale));
    }
    public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
        BigDecimal result = multiply(multiplicand);
        result.inplaceRound(mc);
        return result;
    }
    public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
        return divide(divisor, scale, RoundingMode.valueOf(roundingMode));
    }
    public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        if (roundingMode == null) {
            throw new NullPointerException();
        }
        if (divisor.isZero()) {
            throw new ArithmeticException(Messages.getString("math.04")); 
        }
        long diffScale = ((long)this.scale - divisor.scale) - scale;
        if(this.bitLength < 64 && divisor.bitLength < 64 ) {
            if(diffScale == 0) {
                return dividePrimitiveLongs(this.smallValue,
                        divisor.smallValue,
                        scale,
                        roundingMode );
            } else if(diffScale > 0) {
                if(diffScale < LONG_TEN_POW.length &&
                        divisor.bitLength + LONG_TEN_POW_BIT_LENGTH[(int)diffScale] < 64) {
                    return dividePrimitiveLongs(this.smallValue,
                            divisor.smallValue*LONG_TEN_POW[(int)diffScale],
                            scale,
                            roundingMode);
                }
            } else { 
                if(-diffScale < LONG_TEN_POW.length &&
                        this.bitLength + LONG_TEN_POW_BIT_LENGTH[(int)-diffScale] < 64) {
                    return dividePrimitiveLongs(this.smallValue*LONG_TEN_POW[(int)-diffScale],
                            divisor.smallValue,
                            scale,
                            roundingMode);
                }
            }
        }
        BigInteger scaledDividend = this.getUnscaledValue();
        BigInteger scaledDivisor = divisor.getUnscaledValue(); 
        if (diffScale > 0) {
            scaledDivisor = Multiplication.multiplyByTenPow(scaledDivisor, (int)diffScale);
        } else if (diffScale < 0) {
            scaledDividend  = Multiplication.multiplyByTenPow(scaledDividend, (int)-diffScale);
        }
        return divideBigIntegers(scaledDividend, scaledDivisor, scale, roundingMode);
        }
    private static BigDecimal divideBigIntegers(BigInteger scaledDividend, BigInteger scaledDivisor, int scale, RoundingMode roundingMode) {
        BigInteger[] quotAndRem = scaledDividend.divideAndRemainder(scaledDivisor);  
        BigInteger quotient = quotAndRem[0];
        BigInteger remainder = quotAndRem[1];
        if (remainder.signum() == 0) {
            return new BigDecimal(quotient, scale);
        }
        int sign = scaledDividend.signum() * scaledDivisor.signum();
        int compRem;                                      
        if(scaledDivisor.bitLength() < 63) { 
            long rem = remainder.longValue();
            long divisor = scaledDivisor.longValue();
            compRem = longCompareTo(Math.abs(rem) << 1,Math.abs(divisor));
            compRem = roundingBehavior(quotient.testBit(0) ? 1 : 0,
                    sign * (5 + compRem), roundingMode);
        } else {
            compRem = remainder.abs().shiftLeftOneBit().compareTo(scaledDivisor.abs());
            compRem = roundingBehavior(quotient.testBit(0) ? 1 : 0,
                    sign * (5 + compRem), roundingMode);
        }
            if (compRem != 0) {
            if(quotient.bitLength() < 63) {
                return valueOf(quotient.longValue() + compRem,scale);
            }
            quotient = quotient.add(BigInteger.valueOf(compRem));
            return new BigDecimal(quotient, scale);
        }
        return new BigDecimal(quotient, scale);
    }
    private static BigDecimal dividePrimitiveLongs(long scaledDividend, long scaledDivisor, int scale, RoundingMode roundingMode) {
        long quotient = scaledDividend / scaledDivisor;
        long remainder = scaledDividend % scaledDivisor;
        int sign = Long.signum( scaledDividend ) * Long.signum( scaledDivisor );
        if (remainder != 0) {
            int compRem;                                      
            compRem = longCompareTo(Math.abs(remainder) << 1,Math.abs(scaledDivisor));
            quotient += roundingBehavior(((int)quotient) & 1,
                    sign * (5 + compRem),
                    roundingMode);
        }
        return valueOf(quotient, scale);
    }
    public BigDecimal divide(BigDecimal divisor, int roundingMode) {
        return divide(divisor, scale, RoundingMode.valueOf(roundingMode));
    }
    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        return divide(divisor, scale, roundingMode);
    }
    public BigDecimal divide(BigDecimal divisor) {
        BigInteger p = this.getUnscaledValue();
        BigInteger q = divisor.getUnscaledValue();
        BigInteger gcd; 
        BigInteger quotAndRem[];
        long diffScale = (long)scale - divisor.scale;
        int newScale; 
        int k; 
        int l = 0; 
        int i = 1;
        int lastPow = FIVE_POW.length - 1;
        if (divisor.isZero()) {
            throw new ArithmeticException(Messages.getString("math.04")); 
        }
        if (p.signum() == 0) {
            return zeroScaledBy(diffScale);
        }
        gcd = p.gcd(q);
        p = p.divide(gcd);
        q = q.divide(gcd);
        k = q.getLowestSetBit();
        q = q.shiftRight(k);
        do {
            quotAndRem = q.divideAndRemainder(FIVE_POW[i]);
            if (quotAndRem[1].signum() == 0) {
                l += i;
                if (i < lastPow) {
                    i++;
                }
                q = quotAndRem[0];
            } else {
                if (i == 1) {
                    break;
                }
                i = 1;
            }
        } while (true);
        if (!q.abs().equals(BigInteger.ONE)) {
            throw new ArithmeticException(Messages.getString("math.05")); 
        }
        if (q.signum() < 0) {
            p = p.negate();
        }
        newScale = toIntScale(diffScale + Math.max(k, l));
        i = k - l;
        p = (i > 0) ? Multiplication.multiplyByFivePow(p, i)
        : p.shiftLeft(-i);
        return new BigDecimal(p, newScale);
    }
    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        long traillingZeros = mc.getPrecision() + 2L
                + divisor.aproxPrecision() - aproxPrecision();
        long diffScale = (long)scale - divisor.scale;
        long newScale = diffScale; 
        int compRem; 
        int i = 1; 
        int lastPow = TEN_POW.length - 1; 
        BigInteger integerQuot; 
        BigInteger quotAndRem[] = {getUnscaledValue()};
        if ((mc.getPrecision() == 0) || (this.isZero())
        || (divisor.isZero())) {
            return this.divide(divisor);
        }
        if (traillingZeros > 0) {
            quotAndRem[0] = getUnscaledValue().multiply( Multiplication.powerOf10(traillingZeros) );
            newScale += traillingZeros;
        }
        quotAndRem = quotAndRem[0].divideAndRemainder( divisor.getUnscaledValue() );
        integerQuot = quotAndRem[0];
        if (quotAndRem[1].signum() != 0) {
            compRem = quotAndRem[1].shiftLeftOneBit().compareTo( divisor.getUnscaledValue() );
            integerQuot = integerQuot.multiply(BigInteger.TEN)
            .add(BigInteger.valueOf(quotAndRem[0].signum() * (5 + compRem)));
            newScale++;
        } else {
            while (!integerQuot.testBit(0)) {
                quotAndRem = integerQuot.divideAndRemainder(TEN_POW[i]);
                if ((quotAndRem[1].signum() == 0)
                        && (newScale - i >= diffScale)) {
                    newScale -= i;
                    if (i < lastPow) {
                        i++;
                    }
                    integerQuot = quotAndRem[0];
                } else {
                    if (i == 1) {
                        break;
                    }
                    i = 1;
                }
            }
        }
        return new BigDecimal(integerQuot, toIntScale(newScale), mc);
    }
    public BigDecimal divideToIntegralValue(BigDecimal divisor) {
        BigInteger integralValue; 
        BigInteger powerOfTen; 
        BigInteger quotAndRem[] = {getUnscaledValue()};
        long newScale = (long)this.scale - divisor.scale;
        long tempScale = 0;
        int i = 1;
        int lastPow = TEN_POW.length - 1;
        if (divisor.isZero()) {
            throw new ArithmeticException(Messages.getString("math.04")); 
        }
        if ((divisor.aproxPrecision() + newScale > this.aproxPrecision() + 1L)
        || (this.isZero())) {
            integralValue = BigInteger.ZERO;
        } else if (newScale == 0) {
            integralValue = getUnscaledValue().divide( divisor.getUnscaledValue() );
        } else if (newScale > 0) {
            powerOfTen = Multiplication.powerOf10(newScale);
            integralValue = getUnscaledValue().divide( divisor.getUnscaledValue().multiply(powerOfTen) );
            integralValue = integralValue.multiply(powerOfTen);
        } else {
            powerOfTen = Multiplication.powerOf10(-newScale);
            integralValue = getUnscaledValue().multiply(powerOfTen).divide( divisor.getUnscaledValue() );
            while (!integralValue.testBit(0)) {
                quotAndRem = integralValue.divideAndRemainder(TEN_POW[i]);
                if ((quotAndRem[1].signum() == 0)
                        && (tempScale - i >= newScale)) {
                    tempScale -= i;
                    if (i < lastPow) {
                        i++;
                    }
                    integralValue = quotAndRem[0];
                } else {
                    if (i == 1) {
                        break;
                    }
                    i = 1;
                }
            }
            newScale = tempScale;
        }
        return ((integralValue.signum() == 0)
        ? zeroScaledBy(newScale)
                : new BigDecimal(integralValue, toIntScale(newScale)));
    }
    public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        int mcPrecision = mc.getPrecision();
        int diffPrecision = this.precision() - divisor.precision();
        int lastPow = TEN_POW.length - 1;
        long diffScale = (long)this.scale - divisor.scale;
        long newScale = diffScale;
        long quotPrecision = diffPrecision - diffScale + 1;
        BigInteger quotAndRem[] = new BigInteger[2];
        if ((mcPrecision == 0) || (this.isZero()) || (divisor.isZero())) {
            return this.divideToIntegralValue(divisor);
        }
        if (quotPrecision <= 0) {
            quotAndRem[0] = BigInteger.ZERO;
        } else if (diffScale == 0) {
            quotAndRem[0] = this.getUnscaledValue().divide( divisor.getUnscaledValue() );
        } else if (diffScale > 0) {
            quotAndRem[0] = this.getUnscaledValue().divide(
                    divisor.getUnscaledValue().multiply(Multiplication.powerOf10(diffScale)) );
            newScale = Math.min(diffScale, Math.max(mcPrecision - quotPrecision + 1, 0));
            quotAndRem[0] = quotAndRem[0].multiply(Multiplication.powerOf10(newScale));
        } else {
            long exp = Math.min(-diffScale, Math.max((long)mcPrecision - diffPrecision, 0));
            long compRemDiv;
            quotAndRem = this.getUnscaledValue().multiply(Multiplication.powerOf10(exp)).
                    divideAndRemainder(divisor.getUnscaledValue());
            newScale += exp; 
            exp = -newScale; 
            if ((quotAndRem[1].signum() != 0) && (exp > 0)) {
                compRemDiv = (new BigDecimal(quotAndRem[1])).precision()
                + exp - divisor.precision();
                if (compRemDiv == 0) {
                    quotAndRem[1] = quotAndRem[1].multiply(Multiplication.powerOf10(exp)).
                            divide(divisor.getUnscaledValue());
                    compRemDiv = Math.abs(quotAndRem[1].signum());
                }
                if (compRemDiv > 0) {
                    throw new ArithmeticException(Messages.getString("math.06")); 
                }
            }
        }
        if (quotAndRem[0].signum() == 0) {
            return zeroScaledBy(diffScale);
        }
        BigInteger strippedBI = quotAndRem[0];
        BigDecimal integralValue = new BigDecimal(quotAndRem[0]);
        long resultPrecision = integralValue.precision();
        int i = 1;
        while (!strippedBI.testBit(0)) {
            quotAndRem = strippedBI.divideAndRemainder(TEN_POW[i]);
            if ((quotAndRem[1].signum() == 0) &&
                    ((resultPrecision - i >= mcPrecision)
                    || (newScale - i >= diffScale)) ) {
                resultPrecision -= i;
                newScale -= i;
                if (i < lastPow) {
                    i++;
                }
                strippedBI = quotAndRem[0];
            } else {
                if (i == 1) {
                    break;
                }
                i = 1;
            }
        }
        if (resultPrecision > mcPrecision) {
            throw new ArithmeticException(Messages.getString("math.06")); 
        }
        integralValue.scale = toIntScale(newScale);
        integralValue.setUnscaledValue(strippedBI);
        return integralValue;
    }
    public BigDecimal remainder(BigDecimal divisor) {
        return divideAndRemainder(divisor)[1];
    }
    public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
        return divideAndRemainder(divisor, mc)[1];
    }
    public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
        BigDecimal quotAndRem[] = new BigDecimal[2];
        quotAndRem[0] = this.divideToIntegralValue(divisor);
        quotAndRem[1] = this.subtract( quotAndRem[0].multiply(divisor) );
        return quotAndRem;
    }
    public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
        BigDecimal quotAndRem[] = new BigDecimal[2];
        quotAndRem[0] = this.divideToIntegralValue(divisor, mc);
        quotAndRem[1] = this.subtract( quotAndRem[0].multiply(divisor) );
        return quotAndRem;
    }
    public BigDecimal pow(int n) {
        if (n == 0) {
            return ONE;
        }
        if ((n < 0) || (n > 999999999)) {
            throw new ArithmeticException(Messages.getString("math.07")); 
        }
        long newScale = scale * (long)n;
        return ((isZero())
        ? zeroScaledBy(newScale)
        : new BigDecimal(getUnscaledValue().pow(n), toIntScale(newScale)));
    }
    public BigDecimal pow(int n, MathContext mc) {
        int m = Math.abs(n);
        int mcPrecision = mc.getPrecision();
        int elength = (int)Math.log10(m) + 1;   
        int oneBitMask; 
        BigDecimal accum; 
        MathContext newPrecision = mc; 
        if ((n == 0) || ((isZero()) && (n > 0))) {
            return pow(n);
        }
        if ((m > 999999999) || ((mcPrecision == 0) && (n < 0))
                || ((mcPrecision > 0) && (elength > mcPrecision))) {
            throw new ArithmeticException(Messages.getString("math.07")); 
        }
        if (mcPrecision > 0) {
            newPrecision = new MathContext( mcPrecision + elength + 1,
                    mc.getRoundingMode());
        }
        accum = round(newPrecision);
        oneBitMask = Integer.highestOneBit(m) >> 1;
        while (oneBitMask > 0) {
            accum = accum.multiply(accum, newPrecision);
            if ((m & oneBitMask) == oneBitMask) {
                accum = accum.multiply(this, newPrecision);
            }
            oneBitMask >>= 1;
        }
        if (n < 0) {
            accum = ONE.divide(accum, newPrecision);
        }
        accum.inplaceRound(mc);
        return accum;
    }
    public BigDecimal abs() {
        return ((signum() < 0) ? negate() : this);
    }
    public BigDecimal abs(MathContext mc) {
        BigDecimal result = abs();
        result.inplaceRound(mc);
        return result;
    }
    public BigDecimal negate() {
        if(bitLength < 63 || (bitLength == 63 && smallValue!=Long.MIN_VALUE)) {
            return valueOf(-smallValue,scale);
        }
        return new BigDecimal(getUnscaledValue().negate(), scale);
    }
    public BigDecimal negate(MathContext mc) {
        BigDecimal result = negate();
        result.inplaceRound(mc);
        return result;
    }
    public BigDecimal plus() {
        return this;
    }
    public BigDecimal plus(MathContext mc) {
        return round(mc);
    }
    public int signum() {
        if( bitLength < 64) {
            return Long.signum( this.smallValue );
        }
        return getUnscaledValue().signum();
    }
    private boolean isZero() {
        return bitLength == 0 && this.smallValue != -1;
    }
    public int scale() {
        return scale;
    }
    public int precision() {
        if (precision > 0) {
            return precision;
        }
        int bitLength = this.bitLength;
        int decimalDigits = 1; 
        double doubleUnsc = 1;  
        if (bitLength < 1024) {
            if (bitLength >= 64) {
                doubleUnsc = getUnscaledValue().doubleValue();
            } else if (bitLength >= 1) {
                doubleUnsc = smallValue;
            }
            decimalDigits += Math.log10(Math.abs(doubleUnsc));
        } else {
            decimalDigits += (bitLength - 1) * LOG10_2;
            if (getUnscaledValue().divide(Multiplication.powerOf10(decimalDigits)).signum() != 0) {
                decimalDigits++;
            }
        }
        precision = decimalDigits;
        return precision;
    }
    public BigInteger unscaledValue() {
        return getUnscaledValue();
    }
    public BigDecimal round(MathContext mc) {
        BigDecimal thisBD = new BigDecimal(getUnscaledValue(), scale);
        thisBD.inplaceRound(mc);
        return thisBD;
    }
    public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
        if (roundingMode == null) {
            throw new NullPointerException();
        }
        long diffScale = newScale - (long)scale;
        if(diffScale == 0) {
            return this;
        }
        if(diffScale > 0) {
            if(diffScale < LONG_TEN_POW.length &&
                    (this.bitLength + LONG_TEN_POW_BIT_LENGTH[(int)diffScale]) < 64 ) {
                return valueOf(this.smallValue*LONG_TEN_POW[(int)diffScale],newScale);
            }
            return new BigDecimal(Multiplication.multiplyByTenPow(getUnscaledValue(),(int)diffScale), newScale);
        }
        if(this.bitLength < 64 && -diffScale < LONG_TEN_POW.length) {
            return dividePrimitiveLongs(this.smallValue, LONG_TEN_POW[(int)-diffScale], newScale,roundingMode);
        }
        return divideBigIntegers(this.getUnscaledValue(),Multiplication.powerOf10(-diffScale),newScale,roundingMode);
    }
    public BigDecimal setScale(int newScale, int roundingMode) {
        return setScale(newScale, RoundingMode.valueOf(roundingMode));
    }
    public BigDecimal setScale(int newScale) {
        return setScale(newScale, RoundingMode.UNNECESSARY);
    }
    public BigDecimal movePointLeft(int n) {
        return movePoint(scale + (long)n);
    }
    private BigDecimal movePoint(long newScale) {
        if (isZero()) {
            return zeroScaledBy(Math.max(newScale, 0));
        }
        if(newScale >= 0) {
            if(bitLength < 64) {
                return valueOf(smallValue,toIntScale(newScale));
            }
            return new BigDecimal(getUnscaledValue(), toIntScale(newScale));
        }
        if(-newScale < LONG_TEN_POW.length &&
                bitLength + LONG_TEN_POW_BIT_LENGTH[(int)-newScale] < 64 ) {
            return valueOf(smallValue*LONG_TEN_POW[(int)-newScale],0);
        }
        return new BigDecimal(Multiplication.multiplyByTenPow(getUnscaledValue(),(int)-newScale), 0);
    }
    public BigDecimal movePointRight(int n) {
        return movePoint(scale - (long)n);
    }
    public BigDecimal scaleByPowerOfTen(int n) {
        long newScale = scale - (long)n;
        if(bitLength < 64) {
            if( smallValue==0  ){
                return zeroScaledBy( newScale );
            }
            return valueOf(smallValue,toIntScale(newScale));
        }
        return new BigDecimal(getUnscaledValue(), toIntScale(newScale));
    }
    public BigDecimal stripTrailingZeros() {
        int i = 1; 
        int lastPow = TEN_POW.length - 1;
        long newScale = scale;
        if (isZero()) {
            return this;
        }
        BigInteger strippedBI = getUnscaledValue();
        BigInteger[] quotAndRem;
        while (!strippedBI.testBit(0)) {
            quotAndRem = strippedBI.divideAndRemainder(TEN_POW[i]);
            if (quotAndRem[1].signum() == 0) {
                newScale -= i;
                if (i < lastPow) {
                    i++;
                }
                strippedBI = quotAndRem[0];
            } else {
                if (i == 1) {
                    break;
                }
                i = 1;
            }
        }
        return new BigDecimal(strippedBI, toIntScale(newScale));
    }
    public int compareTo(BigDecimal val) {
        int thisSign = signum();
        int valueSign = val.signum();
        if( thisSign == valueSign) {
            if(this.scale == val.scale && this.bitLength<64 && val.bitLength<64 ) {
                return (smallValue < val.smallValue) ? -1 : (smallValue > val.smallValue) ? 1 : 0;
            }
            long diffScale = (long)this.scale - val.scale;
            int diffPrecision = this.aproxPrecision() - val.aproxPrecision();
            if (diffPrecision > diffScale + 1) {
                return thisSign;
            } else if (diffPrecision < diffScale - 1) {
                return -thisSign;
            } else {
                BigInteger thisUnscaled = this.getUnscaledValue();
                BigInteger valUnscaled = val.getUnscaledValue();
                if (diffScale < 0) {
                    thisUnscaled = thisUnscaled.multiply(Multiplication.powerOf10(-diffScale));
                } else if (diffScale > 0) {
                    valUnscaled = valUnscaled.multiply(Multiplication.powerOf10(diffScale));
                }
                return thisUnscaled.compareTo(valUnscaled);
            }
        } else if (thisSign < valueSign) {
            return -1;
        } else  {
            return 1;
        }
    }
    @Override
    public boolean equals(Object x) {
        if (this == x) {
            return true;
        }
        if (x instanceof BigDecimal) {
            BigDecimal x1 = (BigDecimal) x;
            return x1.scale == scale
                   && (bitLength < 64 ? (x1.smallValue == smallValue)
                    : intVal.equals(x1.intVal));
        }
        return false;
    }
    public BigDecimal min(BigDecimal val) {
        return ((compareTo(val) <= 0) ? this : val);
    }
    public BigDecimal max(BigDecimal val) {
        return ((compareTo(val) >= 0) ? this : val);
    }
    @Override
    public int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }
        if (bitLength < 64) {
            hashCode = (int)(smallValue & 0xffffffff);
            hashCode = 33 * hashCode +  (int)((smallValue >> 32) & 0xffffffff);
            hashCode = 17 * hashCode + scale;
            return hashCode;
        }
        hashCode = 17 * intVal.hashCode() + scale;
        return hashCode;
    }
    @Override
    public String toString() {
        if (toStringImage != null) {
            return toStringImage;
        }
        if(bitLength < 32) {
            toStringImage = Conversion.toDecimalScaledString(smallValue,scale);
            return toStringImage;
        }
        String intString = getUnscaledValue().toString();
        if (scale == 0) {
            return intString;
        }
        int begin = (getUnscaledValue().signum() < 0) ? 2 : 1;
        int end = intString.length();
        long exponent = -(long)scale + end - begin;
        StringBuffer result = new StringBuffer();
        result.append(intString);
        if ((scale > 0) && (exponent >= -6)) {
            if (exponent >= 0) {
                result.insert(end - scale, '.');
            } else {
                result.insert(begin - 1, "0."); 
                result.insert(begin + 1, CH_ZEROS, 0, -(int)exponent - 1);
            }
        } else {
            if (end - begin >= 1) {
                result.insert(begin, '.');
                end++;
            }
            result.insert(end, 'E');
            if (exponent > 0) {
                result.insert(++end, '+');
            }
            result.insert(++end, Long.toString(exponent));
        }
        toStringImage = result.toString();
        return toStringImage;
    }
    public String toEngineeringString() {
        String intString = getUnscaledValue().toString();
        if (scale == 0) {
            return intString;
        }
        int begin = (getUnscaledValue().signum() < 0) ? 2 : 1;
        int end = intString.length();
        long exponent = -(long)scale + end - begin;
        StringBuffer result = new StringBuffer(intString);
        if ((scale > 0) && (exponent >= -6)) {
            if (exponent >= 0) {
                result.insert(end - scale, '.');
            } else {
                result.insert(begin - 1, "0."); 
                result.insert(begin + 1, CH_ZEROS, 0, -(int)exponent - 1);
            }
        } else {
            int delta = end - begin;
            int rem = (int)(exponent % 3);
            if (rem != 0) {
                if (getUnscaledValue().signum() == 0) {
                    rem = (rem < 0) ? -rem : 3 - rem;
                    exponent += rem;
                } else {
                    rem = (rem < 0) ? rem + 3 : rem;
                    exponent -= rem;
                    begin += rem;
                }
                if (delta < 3) {
                    for (int i = rem - delta; i > 0; i--) {
                        result.insert(end++, '0');
                    }
                }
            }
            if (end - begin >= 1) {
                result.insert(begin, '.');
                end++;
            }
            if (exponent != 0) {
                result.insert(end, 'E');
                if (exponent > 0) {
                    result.insert(++end, '+');
                }
                result.insert(++end, Long.toString(exponent));
            }
        }
        return result.toString();
    }
    public String toPlainString() {
        String intStr = getUnscaledValue().toString();
        if ((scale == 0) || ((isZero()) && (scale < 0))) {
            return intStr;
        }
        int begin = (signum() < 0) ? 1 : 0;
        int delta = scale;
        StringBuffer result = new StringBuffer(intStr.length() + 1 + Math.abs(scale));
        if (begin == 1) {
            result.append('-');
        }
        if (scale > 0) {
            delta -= (intStr.length() - begin);
            if (delta >= 0) {
                result.append("0."); 
                for (; delta > CH_ZEROS.length; delta -= CH_ZEROS.length) {
                    result.append(CH_ZEROS);
                }
                result.append(CH_ZEROS, 0, delta);
                result.append(intStr.substring(begin));
            } else {
                delta = begin - delta;
                result.append(intStr.substring(begin, delta));
                result.append('.');
                result.append(intStr.substring(delta));
            }
        } else {
            result.append(intStr.substring(begin));
            for (; delta < -CH_ZEROS.length; delta += CH_ZEROS.length) {
                result.append(CH_ZEROS);
            }
            result.append(CH_ZEROS, 0, -delta);
        }
        return result.toString();
    }
    public BigInteger toBigInteger() {
        if ((scale == 0) || (isZero())) {
            return getUnscaledValue();
        } else if (scale < 0) {
            return getUnscaledValue().multiply(Multiplication.powerOf10(-(long)scale));
        } else {
            return getUnscaledValue().divide(Multiplication.powerOf10(scale));
        }
    }
    public BigInteger toBigIntegerExact() {
        if ((scale == 0) || (isZero())) {
            return getUnscaledValue();
        } else if (scale < 0) {
            return getUnscaledValue().multiply(Multiplication.powerOf10(-(long)scale));
        } else {
            BigInteger[] integerAndFraction;
            if ((scale > aproxPrecision()) || (scale > getUnscaledValue().getLowestSetBit())) {
                throw new ArithmeticException(Messages.getString("math.08")); 
            }
            integerAndFraction = getUnscaledValue().divideAndRemainder(Multiplication.powerOf10(scale));
            if (integerAndFraction[1].signum() != 0) {
                throw new ArithmeticException(Messages.getString("math.08")); 
            }
            return integerAndFraction[0];
        }
    }
    @Override
    public long longValue() {
        return ((scale <= -64) || (scale > aproxPrecision()) ? 0L
                : toBigInteger().longValue());
    }
    public long longValueExact() {
        return valueExact(64);
    }
    @Override
    public int intValue() {
        return ((scale <= -32) || (scale > aproxPrecision())
        ? 0
                : toBigInteger().intValue());
    }
    public int intValueExact() {
        return (int)valueExact(32);
    }
    public short shortValueExact() {
        return (short)valueExact(16);
    }
    public byte byteValueExact() {
        return (byte)valueExact(8);
    }
    @Override
    public float floatValue() {
        float floatResult = signum();
        long powerOfTwo = this.bitLength - (long)(scale / LOG10_2);
        if ((powerOfTwo < -149) || (floatResult == 0.0f)) {
            floatResult *= 0.0f;
        } else if (powerOfTwo > 129) {
            floatResult *= Float.POSITIVE_INFINITY;
        } else {
            floatResult = (float)doubleValue();
        }
        return floatResult;
    }
    @Override
    public double doubleValue() {
        int sign = signum();
        int exponent = 1076; 
        int lowestSetBit;
        int discardedSize;
        long powerOfTwo = this.bitLength - (long)(scale / LOG10_2);
        long bits; 
        long tempBits; 
        BigInteger mantisa;
        if ((powerOfTwo < -1074) || (sign == 0)) {
            return (sign * 0.0d);
        } else if (powerOfTwo > 1025) {
            return (sign * Double.POSITIVE_INFINITY);
        }
        mantisa = getUnscaledValue().abs();
        if (scale <= 0) {
            mantisa = mantisa.multiply(Multiplication.powerOf10(-scale));
        } else {
            BigInteger quotAndRem[];
            BigInteger powerOfTen = Multiplication.powerOf10(scale);
            int k = 100 - (int)powerOfTwo;
            int compRem;
            if (k > 0) {
                mantisa = mantisa.shiftLeft(k);
                exponent -= k;
            }
            quotAndRem = mantisa.divideAndRemainder(powerOfTen);
            compRem = quotAndRem[1].shiftLeftOneBit().compareTo(powerOfTen);
            mantisa = quotAndRem[0].shiftLeft(2).add(
                    BigInteger.valueOf((compRem * (compRem + 3)) / 2 + 1));
            exponent -= 2;
        }
        lowestSetBit = mantisa.getLowestSetBit();
        discardedSize = mantisa.bitLength() - 54;
        if (discardedSize > 0) {
            bits = mantisa.shiftRight(discardedSize).longValue();
            tempBits = bits;
            if ((((bits & 1) == 1) && (lowestSetBit < discardedSize))
                    || ((bits & 3) == 3)) {
                bits += 2;
            }
        } else {
            bits = mantisa.longValue() << -discardedSize;
            tempBits = bits;
            if ((bits & 3) == 3) {
                bits += 2;
            }
        }
        if ((bits & 0x40000000000000L) == 0) {
            bits >>= 1;
            exponent += discardedSize;
        } else {
            bits >>= 2;
            exponent += discardedSize + 1;
        }
        if (exponent > 2046) {
            return (sign * Double.POSITIVE_INFINITY);
        } else if (exponent <= 0) {
            if (exponent < -53) {
                return (sign * 0.0d);
            }
            bits = tempBits >> 1;
            tempBits = bits & (-1L >>> (63 + exponent));
            bits >>= (-exponent );
            if (((bits & 3) == 3) || (((bits & 1) == 1) && (tempBits != 0)
            && (lowestSetBit < discardedSize))) {
                bits += 1;
            }
            exponent = 0;
            bits >>= 1;
        }
        bits = (sign & 0x8000000000000000L) | ((long)exponent << 52)
                | (bits & 0xFFFFFFFFFFFFFL);
        return Double.longBitsToDouble(bits);
    }
    public BigDecimal ulp() {
        return valueOf(1, scale);
    }
    private void inplaceRound(MathContext mc) {
        int mcPrecision = mc.getPrecision();
        if (aproxPrecision() < mcPrecision || mcPrecision == 0) {
            return;
        }
        int discardedPrecision = precision() - mcPrecision;
        if ((discardedPrecision <= 0)) {
            return;
        }
        if (this.bitLength < 64) {
            smallRound(mc, discardedPrecision);
            return;
        }
        BigInteger sizeOfFraction = Multiplication.powerOf10(discardedPrecision);
        BigInteger[] integerAndFraction = getUnscaledValue().divideAndRemainder(sizeOfFraction);
        long newScale = (long)scale - discardedPrecision;
        int compRem;
        BigDecimal tempBD;
        if (integerAndFraction[1].signum() != 0) {
            compRem = (integerAndFraction[1].abs().shiftLeftOneBit().compareTo(sizeOfFraction));
            compRem =  roundingBehavior( integerAndFraction[0].testBit(0) ? 1 : 0,
                    integerAndFraction[1].signum() * (5 + compRem),
                    mc.getRoundingMode());
            if (compRem != 0) {
                integerAndFraction[0] = integerAndFraction[0].add(BigInteger.valueOf(compRem));
            }
            tempBD = new BigDecimal(integerAndFraction[0]);
            if (tempBD.precision() > mcPrecision) {
                integerAndFraction[0] = integerAndFraction[0].divide(BigInteger.TEN);
                newScale--;
            }
        }
        scale = toIntScale(newScale);
        precision = mcPrecision;
        setUnscaledValue(integerAndFraction[0]);
    }
    private static int longCompareTo(long value1, long value2) {
        return value1 > value2 ? 1 : (value1 < value2 ? -1 : 0);
    }
    private void smallRound(MathContext mc, int discardedPrecision) {
        long sizeOfFraction = LONG_TEN_POW[discardedPrecision];
        long newScale = (long)scale - discardedPrecision;
        long unscaledVal = smallValue;
        long integer = unscaledVal / sizeOfFraction;
        long fraction = unscaledVal % sizeOfFraction;
        int compRem;
        if (fraction != 0) {
            compRem = longCompareTo(Math.abs(fraction) << 1,sizeOfFraction);
            integer += roundingBehavior( ((int)integer) & 1,
                    Long.signum(fraction) * (5 + compRem),
                    mc.getRoundingMode());
            if (Math.log10(Math.abs(integer)) >= mc.getPrecision()) {
                integer /= 10;
                newScale--;
            }
        }
        scale = toIntScale(newScale);
        precision = mc.getPrecision();
        smallValue = integer;
        bitLength = bitLength(integer);
        intVal = null;
    }
    private static int roundingBehavior(int parityBit, int fraction, RoundingMode roundingMode) {
        int increment = 0; 
        switch (roundingMode) {
            case UNNECESSARY:
                if (fraction != 0) {
                    throw new ArithmeticException(Messages.getString("math.08")); 
                }
                break;
            case UP:
                increment = Integer.signum(fraction);
                break;
            case DOWN:
                break;
            case CEILING:
                increment = Math.max(Integer.signum(fraction), 0);
                break;
            case FLOOR:
                increment = Math.min(Integer.signum(fraction), 0);
                break;
            case HALF_UP:
                if (Math.abs(fraction) >= 5) {
                    increment = Integer.signum(fraction);
                }
                break;
            case HALF_DOWN:
                if (Math.abs(fraction) > 5) {
                    increment = Integer.signum(fraction);
                }
                break;
            case HALF_EVEN:
                if (Math.abs(fraction) + parityBit > 5) {
                    increment = Integer.signum(fraction);
                }
                break;
        }
        return increment;
    }
    private long valueExact(int bitLengthOfType) {
        BigInteger bigInteger = toBigIntegerExact();
        if (bigInteger.bitLength() < bitLengthOfType) {
            return bigInteger.longValue();
        }
        throw new ArithmeticException(Messages.getString("math.08")); 
    }
    private int aproxPrecision() {
        return precision > 0
                ? precision
                : (int) ((this.bitLength - 1) * LOG10_2) + 1;
    }
    private static int toIntScale(long longScale) {
        if (longScale < Integer.MIN_VALUE) {
            throw new ArithmeticException(Messages.getString("math.09")); 
        } else if (longScale > Integer.MAX_VALUE) {
            throw new ArithmeticException(Messages.getString("math.0A")); 
        } else {
            return (int)longScale;
        }
    }
    private static BigDecimal zeroScaledBy(long longScale) {
        if (longScale == (int) longScale) {
            return valueOf(0,(int)longScale);
            }
        if (longScale >= 0) {
            return new BigDecimal( 0, Integer.MAX_VALUE);
        }
        return new BigDecimal( 0, Integer.MIN_VALUE);
    }
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        this.bitLength = intVal.bitLength();
        if (this.bitLength < 64) {
            this.smallValue = intVal.longValue();
        }
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        getUnscaledValue();
        out.defaultWriteObject();
    }
    private BigInteger getUnscaledValue() {
        if(intVal == null) {
            intVal = BigInteger.valueOf(smallValue);
        }
        return intVal;
    }
    private void setUnscaledValue(BigInteger unscaledValue) {
        this.intVal = unscaledValue;
        this.bitLength = unscaledValue.bitLength();
        if(this.bitLength < 64) {
            this.smallValue = unscaledValue.longValue();
        }
    }
    private static int bitLength(long smallValue) {
        if(smallValue < 0) {
            smallValue = ~smallValue;
        }
        return 64 - Long.numberOfLeadingZeros(smallValue);
    }
    private static int bitLength(int smallValue) {
        if(smallValue < 0) {
            smallValue = ~smallValue;
        }
        return 32 - Integer.numberOfLeadingZeros(smallValue);
    }
}
