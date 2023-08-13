public class BigDecimal extends Number implements Comparable<BigDecimal> {
    private volatile BigInteger intVal;
    private int scale;  
    private transient int precision;
    private transient String stringCache;
    static final long INFLATED = Long.MIN_VALUE;
    private transient long intCompact;
    private static final int MAX_COMPACT_DIGITS = 18;
    private static final int MAX_BIGINT_BITS = 62;
    private static final long serialVersionUID = 6108874887143696463L;
    private static final ThreadLocal<StringBuilderHelper>
        threadLocalStringBuilderHelper = new ThreadLocal<StringBuilderHelper>() {
        @Override
        protected StringBuilderHelper initialValue() {
            return new StringBuilderHelper();
        }
    };
    private static final BigDecimal zeroThroughTen[] = {
        new BigDecimal(BigInteger.ZERO,         0,  0, 1),
        new BigDecimal(BigInteger.ONE,          1,  0, 1),
        new BigDecimal(BigInteger.valueOf(2),   2,  0, 1),
        new BigDecimal(BigInteger.valueOf(3),   3,  0, 1),
        new BigDecimal(BigInteger.valueOf(4),   4,  0, 1),
        new BigDecimal(BigInteger.valueOf(5),   5,  0, 1),
        new BigDecimal(BigInteger.valueOf(6),   6,  0, 1),
        new BigDecimal(BigInteger.valueOf(7),   7,  0, 1),
        new BigDecimal(BigInteger.valueOf(8),   8,  0, 1),
        new BigDecimal(BigInteger.valueOf(9),   9,  0, 1),
        new BigDecimal(BigInteger.TEN,          10, 0, 2),
    };
    private static final BigDecimal[] ZERO_SCALED_BY = {
        zeroThroughTen[0],
        new BigDecimal(BigInteger.ZERO, 0, 1, 1),
        new BigDecimal(BigInteger.ZERO, 0, 2, 1),
        new BigDecimal(BigInteger.ZERO, 0, 3, 1),
        new BigDecimal(BigInteger.ZERO, 0, 4, 1),
        new BigDecimal(BigInteger.ZERO, 0, 5, 1),
        new BigDecimal(BigInteger.ZERO, 0, 6, 1),
        new BigDecimal(BigInteger.ZERO, 0, 7, 1),
        new BigDecimal(BigInteger.ZERO, 0, 8, 1),
        new BigDecimal(BigInteger.ZERO, 0, 9, 1),
        new BigDecimal(BigInteger.ZERO, 0, 10, 1),
        new BigDecimal(BigInteger.ZERO, 0, 11, 1),
        new BigDecimal(BigInteger.ZERO, 0, 12, 1),
        new BigDecimal(BigInteger.ZERO, 0, 13, 1),
        new BigDecimal(BigInteger.ZERO, 0, 14, 1),
        new BigDecimal(BigInteger.ZERO, 0, 15, 1),
    };
    private static final long HALF_LONG_MAX_VALUE = Long.MAX_VALUE / 2;
    private static final long HALF_LONG_MIN_VALUE = Long.MIN_VALUE / 2;
    public static final BigDecimal ZERO =
        zeroThroughTen[0];
    public static final BigDecimal ONE =
        zeroThroughTen[1];
    public static final BigDecimal TEN =
        zeroThroughTen[10];
    BigDecimal(BigInteger intVal, long val, int scale, int prec) {
        this.scale = scale;
        this.precision = prec;
        this.intCompact = val;
        this.intVal = intVal;
    }
    public BigDecimal(char[] in, int offset, int len) {
        if (offset+len > in.length || offset < 0)
            throw new NumberFormatException();
        int prec = 0;                 
        int scl = 0;                  
        long rs = 0;                  
        BigInteger rb = null;         
        try {
            boolean isneg = false;          
            if (in[offset] == '-') {
                isneg = true;               
                offset++;
                len--;
            } else if (in[offset] == '+') { 
                offset++;
                len--;
            }
            boolean dot = false;             
            int cfirst = offset;             
            long exp = 0;                    
            char c;                          
            boolean isCompact = (len <= MAX_COMPACT_DIGITS);
            char coeff[] = isCompact ? null : new char[len];
            int idx = 0;
            for (; len > 0; offset++, len--) {
                c = in[offset];
                if ((c >= '0' && c <= '9') || Character.isDigit(c)) {
                    if (isCompact) {
                        int digit = Character.digit(c, 10);
                        if (digit == 0) {
                            if (prec == 0)
                                prec = 1;
                            else if (rs != 0) {
                                rs *= 10;
                                ++prec;
                            } 
                        } else {
                            if (prec != 1 || rs != 0)
                                ++prec; 
                            rs = rs * 10 + digit;
                        }
                    } else { 
                        if (c == '0' || Character.digit(c, 10) == 0) {
                            if (prec == 0) {
                                coeff[idx] = c;
                                prec = 1;
                            } else if (idx != 0) {
                                coeff[idx++] = c;
                                ++prec;
                            } 
                        } else {
                            if (prec != 1 || idx != 0)
                                ++prec; 
                            coeff[idx++] = c;
                        }
                    }
                    if (dot)
                        ++scl;
                    continue;
                }
                if (c == '.') {
                    if (dot)         
                        throw new NumberFormatException();
                    dot = true;
                    continue;
                }
                if ((c != 'e') && (c != 'E'))
                    throw new NumberFormatException();
                offset++;
                c = in[offset];
                len--;
                boolean negexp = (c == '-');
                if (negexp || c == '+') {
                    offset++;
                    c = in[offset];
                    len--;
                }
                if (len <= 0)    
                    throw new NumberFormatException();
                while (len > 10 && Character.digit(c, 10) == 0) {
                    offset++;
                    c = in[offset];
                    len--;
                }
                if (len > 10)  
                    throw new NumberFormatException();
                for (;; len--) {
                    int v;
                    if (c >= '0' && c <= '9') {
                        v = c - '0';
                    } else {
                        v = Character.digit(c, 10);
                        if (v < 0)            
                            throw new NumberFormatException();
                    }
                    exp = exp * 10 + v;
                    if (len == 1)
                        break;               
                    offset++;
                    c = in[offset];
                }
                if (negexp)                  
                    exp = -exp;
                if ((int)exp != exp)         
                    throw new NumberFormatException();
                break;                       
            }
            if (prec == 0)              
                throw new NumberFormatException();
            if (exp != 0) {                  
                long adjustedScale = scl - exp;
                if (adjustedScale > Integer.MAX_VALUE ||
                    adjustedScale < Integer.MIN_VALUE)
                    throw new NumberFormatException("Scale out of range.");
                scl = (int)adjustedScale;
            }
            if (isCompact) {
                rs = isneg ? -rs : rs;
            } else {
                char quick[];
                if (!isneg) {
                    quick = (coeff.length != prec) ?
                        Arrays.copyOf(coeff, prec) : coeff;
                } else {
                    quick = new char[prec + 1];
                    quick[0] = '-';
                    System.arraycopy(coeff, 0, quick, 1, prec);
                }
                rb = new BigInteger(quick);
                rs = compactValFor(rb);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NumberFormatException();
        } catch (NegativeArraySizeException e) {
            throw new NumberFormatException();
        }
        this.scale = scl;
        this.precision = prec;
        this.intCompact = rs;
        this.intVal = (rs != INFLATED) ? null : rb;
    }
    public BigDecimal(char[] in, int offset, int len, MathContext mc) {
        this(in, offset, len);
        if (mc.precision > 0)
            roundThis(mc);
    }
    public BigDecimal(char[] in) {
        this(in, 0, in.length);
    }
    public BigDecimal(char[] in, MathContext mc) {
        this(in, 0, in.length, mc);
    }
    public BigDecimal(String val) {
        this(val.toCharArray(), 0, val.length());
    }
    public BigDecimal(String val, MathContext mc) {
        this(val.toCharArray(), 0, val.length());
        if (mc.precision > 0)
            roundThis(mc);
    }
    public BigDecimal(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val))
            throw new NumberFormatException("Infinite or NaN");
        long valBits = Double.doubleToLongBits(val);
        int sign = ((valBits >> 63)==0 ? 1 : -1);
        int exponent = (int) ((valBits >> 52) & 0x7ffL);
        long significand = (exponent==0 ? (valBits & ((1L<<52) - 1)) << 1
                            : (valBits & ((1L<<52) - 1)) | (1L<<52));
        exponent -= 1075;
        if (significand == 0) {
            intVal = BigInteger.ZERO;
            intCompact = 0;
            precision = 1;
            return;
        }
        while((significand & 1) == 0) {    
            significand >>= 1;
            exponent++;
        }
        long s = sign * significand;
        BigInteger b;
        if (exponent < 0) {
            b = BigInteger.valueOf(5).pow(-exponent).multiply(s);
            scale = -exponent;
        } else if (exponent > 0) {
            b = BigInteger.valueOf(2).pow(exponent).multiply(s);
        } else {
            b = BigInteger.valueOf(s);
        }
        intCompact = compactValFor(b);
        intVal = (intCompact != INFLATED) ? null : b;
    }
    public BigDecimal(double val, MathContext mc) {
        this(val);
        if (mc.precision > 0)
            roundThis(mc);
    }
    public BigDecimal(BigInteger val) {
        intCompact = compactValFor(val);
        intVal = (intCompact != INFLATED) ? null : val;
    }
    public BigDecimal(BigInteger val, MathContext mc) {
        this(val);
        if (mc.precision > 0)
            roundThis(mc);
    }
    public BigDecimal(BigInteger unscaledVal, int scale) {
        this(unscaledVal);
        this.scale = scale;
    }
    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        this(unscaledVal);
        this.scale = scale;
        if (mc.precision > 0)
            roundThis(mc);
    }
    public BigDecimal(int val) {
        intCompact = val;
    }
    public BigDecimal(int val, MathContext mc) {
        intCompact = val;
        if (mc.precision > 0)
            roundThis(mc);
    }
    public BigDecimal(long val) {
        this.intCompact = val;
        this.intVal = (val == INFLATED) ? BigInteger.valueOf(val) : null;
    }
    public BigDecimal(long val, MathContext mc) {
        this(val);
        if (mc.precision > 0)
            roundThis(mc);
    }
    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0)
            return valueOf(unscaledVal);
        else if (unscaledVal == 0) {
            if (scale > 0 && scale < ZERO_SCALED_BY.length)
                return ZERO_SCALED_BY[scale];
            else
                return new BigDecimal(BigInteger.ZERO, 0, scale, 1);
        }
        return new BigDecimal(unscaledVal == INFLATED ?
                              BigInteger.valueOf(unscaledVal) : null,
                              unscaledVal, scale, 0);
    }
    public static BigDecimal valueOf(long val) {
        if (val >= 0 && val < zeroThroughTen.length)
            return zeroThroughTen[(int)val];
        else if (val != INFLATED)
            return new BigDecimal(null, val, 0, 0);
        return new BigDecimal(BigInteger.valueOf(val), val, 0, 0);
    }
    public static BigDecimal valueOf(double val) {
        return new BigDecimal(Double.toString(val));
    }
    public BigDecimal add(BigDecimal augend) {
        long xs = this.intCompact;
        long ys = augend.intCompact;
        BigInteger fst = (xs != INFLATED) ? null : this.intVal;
        BigInteger snd = (ys != INFLATED) ? null : augend.intVal;
        int rscale = this.scale;
        long sdiff = (long)rscale - augend.scale;
        if (sdiff != 0) {
            if (sdiff < 0) {
                int raise = checkScale(-sdiff);
                rscale = augend.scale;
                if (xs == INFLATED ||
                    (xs = longMultiplyPowerTen(xs, raise)) == INFLATED)
                    fst = bigMultiplyPowerTen(raise);
            } else {
                int raise = augend.checkScale(sdiff);
                if (ys == INFLATED ||
                    (ys = longMultiplyPowerTen(ys, raise)) == INFLATED)
                    snd = augend.bigMultiplyPowerTen(raise);
            }
        }
        if (xs != INFLATED && ys != INFLATED) {
            long sum = xs + ys;
            if ( (((sum ^ xs) & (sum ^ ys))) >= 0L) 
                return BigDecimal.valueOf(sum, rscale);
        }
        if (fst == null)
            fst = BigInteger.valueOf(xs);
        if (snd == null)
            snd = BigInteger.valueOf(ys);
        BigInteger sum = fst.add(snd);
        return (fst.signum == snd.signum) ?
            new BigDecimal(sum, INFLATED, rscale, 0) :
            new BigDecimal(sum, rscale);
    }
    public BigDecimal add(BigDecimal augend, MathContext mc) {
        if (mc.precision == 0)
            return add(augend);
        BigDecimal lhs = this;
        this.inflate();
        augend.inflate();
        {
            boolean lhsIsZero = lhs.signum() == 0;
            boolean augendIsZero = augend.signum() == 0;
            if (lhsIsZero || augendIsZero) {
                int preferredScale = Math.max(lhs.scale(), augend.scale());
                BigDecimal result;
                if (lhsIsZero && augendIsZero)
                    return new BigDecimal(BigInteger.ZERO, 0, preferredScale, 0);
                result = lhsIsZero ? doRound(augend, mc) : doRound(lhs, mc);
                if (result.scale() == preferredScale)
                    return result;
                else if (result.scale() > preferredScale) {
                    BigDecimal scaledResult =
                        new BigDecimal(result.intVal, result.intCompact,
                                       result.scale, 0);
                    scaledResult.stripZerosToMatchScale(preferredScale);
                    return scaledResult;
                } else { 
                    int precisionDiff = mc.precision - result.precision();
                    int scaleDiff     = preferredScale - result.scale();
                    if (precisionDiff >= scaleDiff)
                        return result.setScale(preferredScale); 
                    else
                        return result.setScale(result.scale() + precisionDiff);
                }
            }
        }
        long padding = (long)lhs.scale - augend.scale;
        if (padding != 0) {        
            BigDecimal arg[] = preAlign(lhs, augend, padding, mc);
            matchScale(arg);
            lhs    = arg[0];
            augend = arg[1];
        }
        BigDecimal d = new BigDecimal(lhs.inflate().add(augend.inflate()),
                                      lhs.scale);
        return doRound(d, mc);
    }
    private BigDecimal[] preAlign(BigDecimal lhs, BigDecimal augend,
                                  long padding, MathContext mc) {
        assert padding != 0;
        BigDecimal big;
        BigDecimal small;
        if (padding < 0) {     
            big   = lhs;
            small = augend;
        } else {               
            big   = augend;
            small = lhs;
        }
        long estResultUlpScale = (long)big.scale - big.precision() + mc.precision;
        long smallHighDigitPos = (long)small.scale - small.precision() + 1;
        if (smallHighDigitPos > big.scale + 2 &&         
            smallHighDigitPos > estResultUlpScale + 2) { 
            small = BigDecimal.valueOf(small.signum(),
                                       this.checkScale(Math.max(big.scale, estResultUlpScale) + 3));
        }
        BigDecimal[] result = {big, small};
        return result;
    }
    public BigDecimal subtract(BigDecimal subtrahend) {
        return add(subtrahend.negate());
    }
    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        BigDecimal nsubtrahend = subtrahend.negate();
        if (mc.precision == 0)
            return add(nsubtrahend);
        return add(nsubtrahend, mc);
    }
    public BigDecimal multiply(BigDecimal multiplicand) {
        long x = this.intCompact;
        long y = multiplicand.intCompact;
        int productScale = checkScale((long)scale + multiplicand.scale);
        if (x != INFLATED && y != INFLATED) {
            long product = x * y;
            long prec = this.precision() + multiplicand.precision();
            if (prec < 19 || (prec < 21 && (y == 0 || product / y == x)))
                return BigDecimal.valueOf(product, productScale);
            return new BigDecimal(BigInteger.valueOf(x).multiply(y), INFLATED,
                                  productScale, 0);
        }
        BigInteger rb;
        if (x == INFLATED && y == INFLATED)
            rb = this.intVal.multiply(multiplicand.intVal);
        else if (x != INFLATED)
            rb = multiplicand.intVal.multiply(x);
        else
            rb = this.intVal.multiply(y);
        return new BigDecimal(rb, INFLATED, productScale, 0);
    }
    public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
        if (mc.precision == 0)
            return multiply(multiplicand);
        return doRound(this.multiply(multiplicand), mc);
    }
    public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("Invalid rounding mode");
        BigDecimal dividend = this;
        if (checkScale((long)scale + divisor.scale) > this.scale)
            dividend = this.setScale(scale + divisor.scale, ROUND_UNNECESSARY);
        else
            divisor = divisor.setScale(checkScale((long)this.scale - scale),
                                       ROUND_UNNECESSARY);
        return divideAndRound(dividend.intCompact, dividend.intVal,
                              divisor.intCompact, divisor.intVal,
                              scale, roundingMode, scale);
    }
    private static BigDecimal divideAndRound(long ldividend, BigInteger bdividend,
                                             long ldivisor,  BigInteger bdivisor,
                                             int scale, int roundingMode,
                                             int preferredScale) {
        boolean isRemainderZero;       
        int qsign;                     
        long q = 0, r = 0;             
        MutableBigInteger mq = null;   
        MutableBigInteger mr = null;   
        MutableBigInteger mdivisor = null;
        boolean isLongDivision = (ldividend != INFLATED && ldivisor != INFLATED);
        if (isLongDivision) {
            q = ldividend / ldivisor;
            if (roundingMode == ROUND_DOWN && scale == preferredScale)
                return new BigDecimal(null, q, scale, 0);
            r = ldividend % ldivisor;
            isRemainderZero = (r == 0);
            qsign = ((ldividend < 0) == (ldivisor < 0)) ? 1 : -1;
        } else {
            if (bdividend == null)
                bdividend = BigInteger.valueOf(ldividend);
            MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
            mq = new MutableBigInteger();
            if (ldivisor != INFLATED) {
                r = mdividend.divide(ldivisor, mq);
                isRemainderZero = (r == 0);
                qsign = (ldivisor < 0) ? -bdividend.signum : bdividend.signum;
            } else {
                mdivisor = new MutableBigInteger(bdivisor.mag);
                mr = mdividend.divide(mdivisor, mq);
                isRemainderZero = mr.isZero();
                qsign = (bdividend.signum != bdivisor.signum) ? -1 : 1;
            }
        }
        boolean increment = false;
        if (!isRemainderZero) {
            int cmpFracHalf;
            if (roundingMode == ROUND_UNNECESSARY) {  
                throw new ArithmeticException("Rounding necessary");
            } else if (roundingMode == ROUND_UP) {      
                increment = true;
            } else if (roundingMode == ROUND_DOWN) {    
                increment = false;
            } else if (roundingMode == ROUND_CEILING) { 
                increment = (qsign > 0);
            } else if (roundingMode == ROUND_FLOOR) {   
                increment = (qsign < 0);
            } else {
                if (isLongDivision || ldivisor != INFLATED) {
                    if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
                        cmpFracHalf = 1;    
                    } else {
                        cmpFracHalf = longCompareMagnitude(2 * r, ldivisor);
                    }
                } else {
                    cmpFracHalf = mr.compareHalf(mdivisor);
                }
                if (cmpFracHalf < 0)
                    increment = false;     
                else if (cmpFracHalf > 0)  
                    increment = true;
                else if (roundingMode == ROUND_HALF_UP)
                    increment = true;
                else if (roundingMode == ROUND_HALF_DOWN)
                    increment = false;
                else  
                    increment = isLongDivision ? (q & 1L) != 0L : mq.isOdd();
            }
        }
        BigDecimal res;
        if (isLongDivision)
            res = new BigDecimal(null, (increment ? q + qsign : q), scale, 0);
        else {
            if (increment)
                mq.add(MutableBigInteger.ONE);
            res = mq.toBigDecimal(qsign, scale);
        }
        if (isRemainderZero && preferredScale != scale)
            res.stripZerosToMatchScale(preferredScale);
        return res;
    }
    public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        return divide(divisor, scale, roundingMode.oldMode);
    }
    public BigDecimal divide(BigDecimal divisor, int roundingMode) {
            return this.divide(divisor, scale, roundingMode);
    }
    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        return this.divide(divisor, scale, roundingMode.oldMode);
    }
    public BigDecimal divide(BigDecimal divisor) {
        if (divisor.signum() == 0) {   
            if (this.signum() == 0)    
                throw new ArithmeticException("Division undefined");  
            throw new ArithmeticException("Division by zero");
        }
        int preferredScale = saturateLong((long)this.scale - divisor.scale);
        if (this.signum() == 0)        
            return (preferredScale >= 0 &&
                    preferredScale < ZERO_SCALED_BY.length) ?
                ZERO_SCALED_BY[preferredScale] :
                BigDecimal.valueOf(0, preferredScale);
        else {
            this.inflate();
            divisor.inflate();
            MathContext mc = new MathContext( (int)Math.min(this.precision() +
                                                            (long)Math.ceil(10.0*divisor.precision()/3.0),
                                                            Integer.MAX_VALUE),
                                              RoundingMode.UNNECESSARY);
            BigDecimal quotient;
            try {
                quotient = this.divide(divisor, mc);
            } catch (ArithmeticException e) {
                throw new ArithmeticException("Non-terminating decimal expansion; " +
                                              "no exact representable decimal result.");
            }
            int quotientScale = quotient.scale();
            if (preferredScale > quotientScale)
                return quotient.setScale(preferredScale, ROUND_UNNECESSARY);
            return quotient;
        }
    }
    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        int mcp = mc.precision;
        if (mcp == 0)
            return divide(divisor);
        BigDecimal dividend = this;
        long preferredScale = (long)dividend.scale - divisor.scale;
        if (divisor.signum() == 0) {      
            if (dividend.signum() == 0)    
                throw new ArithmeticException("Division undefined");  
            throw new ArithmeticException("Division by zero");
        }
        if (dividend.signum() == 0)        
            return new BigDecimal(BigInteger.ZERO, 0,
                                  saturateLong(preferredScale), 1);
        int xscale = dividend.precision();
        int yscale = divisor.precision();
        dividend = new BigDecimal(dividend.intVal, dividend.intCompact,
                                  xscale, xscale);
        divisor = new BigDecimal(divisor.intVal, divisor.intCompact,
                                 yscale, yscale);
        if (dividend.compareMagnitude(divisor) > 0) 
            yscale = divisor.scale -= 1;            
        BigDecimal quotient;
        int scl = checkScale(preferredScale + yscale - xscale + mcp);
        if (checkScale((long)mcp + yscale) > xscale)
            dividend = dividend.setScale(mcp + yscale, ROUND_UNNECESSARY);
        else
            divisor = divisor.setScale(checkScale((long)xscale - mcp),
                                       ROUND_UNNECESSARY);
        quotient = divideAndRound(dividend.intCompact, dividend.intVal,
                                  divisor.intCompact, divisor.intVal,
                                  scl, mc.roundingMode.oldMode,
                                  checkScale(preferredScale));
        quotient = doRound(quotient, mc);
        return quotient;
    }
    public BigDecimal divideToIntegralValue(BigDecimal divisor) {
        int preferredScale = saturateLong((long)this.scale - divisor.scale);
        if (this.compareMagnitude(divisor) < 0) {
            return BigDecimal.valueOf(0, preferredScale);
        }
        if(this.signum() == 0 && divisor.signum() != 0)
            return this.setScale(preferredScale, ROUND_UNNECESSARY);
        int maxDigits = (int)Math.min(this.precision() +
                                      (long)Math.ceil(10.0*divisor.precision()/3.0) +
                                      Math.abs((long)this.scale() - divisor.scale()) + 2,
                                      Integer.MAX_VALUE);
        BigDecimal quotient = this.divide(divisor, new MathContext(maxDigits,
                                                                   RoundingMode.DOWN));
        if (quotient.scale > 0) {
            quotient = quotient.setScale(0, RoundingMode.DOWN);
            quotient.stripZerosToMatchScale(preferredScale);
        }
        if (quotient.scale < preferredScale) {
            quotient = quotient.setScale(preferredScale, ROUND_UNNECESSARY);
        }
        return quotient;
    }
    public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0 ||                        
            (this.compareMagnitude(divisor) < 0) )      
            return divideToIntegralValue(divisor);
        int preferredScale = saturateLong((long)this.scale - divisor.scale);
        BigDecimal result = this.
            divide(divisor, new MathContext(mc.precision, RoundingMode.DOWN));
        if (result.scale() < 0) {
            BigDecimal product = result.multiply(divisor);
            if (this.subtract(product).compareMagnitude(divisor) >= 0) {
                throw new ArithmeticException("Division impossible");
            }
        } else if (result.scale() > 0) {
            result = result.setScale(0, RoundingMode.DOWN);
        }
        int precisionDiff;
        if ((preferredScale > result.scale()) &&
            (precisionDiff = mc.precision - result.precision()) > 0) {
            return result.setScale(result.scale() +
                                   Math.min(precisionDiff, preferredScale - result.scale) );
        } else {
            result.stripZerosToMatchScale(preferredScale);
            return result;
        }
    }
    public BigDecimal remainder(BigDecimal divisor) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor);
        return divrem[1];
    }
    public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor, mc);
        return divrem[1];
    }
    public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
        BigDecimal[] result = new BigDecimal[2];
        result[0] = this.divideToIntegralValue(divisor);
        result[1] = this.subtract(result[0].multiply(divisor));
        return result;
    }
    public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0)
            return divideAndRemainder(divisor);
        BigDecimal[] result = new BigDecimal[2];
        BigDecimal lhs = this;
        result[0] = lhs.divideToIntegralValue(divisor, mc);
        result[1] = lhs.subtract(result[0].multiply(divisor));
        return result;
    }
    public BigDecimal pow(int n) {
        if (n < 0 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        int newScale = checkScale((long)scale * n);
        this.inflate();
        return new BigDecimal(intVal.pow(n), newScale);
    }
    public BigDecimal pow(int n, MathContext mc) {
        if (mc.precision == 0)
            return pow(n);
        if (n < -999999999 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        if (n == 0)
            return ONE;                      
        this.inflate();
        BigDecimal lhs = this;
        MathContext workmc = mc;           
        int mag = Math.abs(n);               
        if (mc.precision > 0) {
            int elength = longDigitLength(mag); 
            if (elength > mc.precision)        
                throw new ArithmeticException("Invalid operation");
            workmc = new MathContext(mc.precision + elength + 1,
                                      mc.roundingMode);
        }
        BigDecimal acc = ONE;           
        boolean seenbit = false;        
        for (int i=1;;i++) {            
            mag += mag;                 
            if (mag < 0) {              
                seenbit = true;         
                acc = acc.multiply(lhs, workmc); 
            }
            if (i == 31)
                break;                  
            if (seenbit)
                acc=acc.multiply(acc, workmc);   
        }
        if (n<0)                          
            acc=ONE.divide(acc, workmc);
        return doRound(acc, mc);
    }
    public BigDecimal abs() {
        return (signum() < 0 ? negate() : this);
    }
    public BigDecimal abs(MathContext mc) {
        return (signum() < 0 ? negate(mc) : plus(mc));
    }
    public BigDecimal negate() {
        BigDecimal result;
        if (intCompact != INFLATED)
            result = BigDecimal.valueOf(-intCompact, scale);
        else {
            result = new BigDecimal(intVal.negate(), scale);
            result.precision = precision;
        }
        return result;
    }
    public BigDecimal negate(MathContext mc) {
        return negate().plus(mc);
    }
    public BigDecimal plus() {
        return this;
    }
    public BigDecimal plus(MathContext mc) {
        if (mc.precision == 0)                 
            return this;
        return doRound(this, mc);
    }
    public int signum() {
        return (intCompact != INFLATED)?
            Long.signum(intCompact):
            intVal.signum();
    }
    public int scale() {
        return scale;
    }
    public int precision() {
        int result = precision;
        if (result == 0) {
            long s = intCompact;
            if (s != INFLATED)
                result = longDigitLength(s);
            else
                result = bigDigitLength(inflate());
            precision = result;
        }
        return result;
    }
    public BigInteger unscaledValue() {
        return this.inflate();
    }
    public final static int ROUND_UP =           0;
    public final static int ROUND_DOWN =         1;
    public final static int ROUND_CEILING =      2;
    public final static int ROUND_FLOOR =        3;
    public final static int ROUND_HALF_UP =      4;
    public final static int ROUND_HALF_DOWN =    5;
    public final static int ROUND_HALF_EVEN =    6;
    public final static int ROUND_UNNECESSARY =  7;
    public BigDecimal round(MathContext mc) {
        return plus(mc);
    }
    public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
        return setScale(newScale, roundingMode.oldMode);
    }
    public BigDecimal setScale(int newScale, int roundingMode) {
        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("Invalid rounding mode");
        int oldScale = this.scale;
        if (newScale == oldScale)        
            return this;
        if (this.signum() == 0)            
            return BigDecimal.valueOf(0, newScale);
        long rs = this.intCompact;
        if (newScale > oldScale) {
            int raise = checkScale((long)newScale - oldScale);
            BigInteger rb = null;
            if (rs == INFLATED ||
                (rs = longMultiplyPowerTen(rs, raise)) == INFLATED)
                rb = bigMultiplyPowerTen(raise);
            return new BigDecimal(rb, rs, newScale,
                                  (precision > 0) ? precision + raise : 0);
        } else {
            int drop = checkScale((long)oldScale - newScale);
            if (drop < LONG_TEN_POWERS_TABLE.length)
                return divideAndRound(rs, this.intVal,
                                      LONG_TEN_POWERS_TABLE[drop], null,
                                      newScale, roundingMode, newScale);
            else
                return divideAndRound(rs, this.intVal,
                                      INFLATED, bigTenToThe(drop),
                                      newScale, roundingMode, newScale);
        }
    }
    public BigDecimal setScale(int newScale) {
        return setScale(newScale, ROUND_UNNECESSARY);
    }
    public BigDecimal movePointLeft(int n) {
        int newScale = checkScale((long)scale + n);
        BigDecimal num = new BigDecimal(intVal, intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
    }
    public BigDecimal movePointRight(int n) {
        int newScale = checkScale((long)scale - n);
        BigDecimal num = new BigDecimal(intVal, intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
    }
    public BigDecimal scaleByPowerOfTen(int n) {
        return new BigDecimal(intVal, intCompact,
                              checkScale((long)scale - n), precision);
    }
    public BigDecimal stripTrailingZeros() {
        this.inflate();
        BigDecimal result = new BigDecimal(intVal, scale);
        result.stripZerosToMatchScale(Long.MIN_VALUE);
        return result;
    }
    public int compareTo(BigDecimal val) {
        if (scale == val.scale) {
            long xs = intCompact;
            long ys = val.intCompact;
            if (xs != INFLATED && ys != INFLATED)
                return xs != ys ? ((xs > ys) ? 1 : -1) : 0;
        }
        int xsign = this.signum();
        int ysign = val.signum();
        if (xsign != ysign)
            return (xsign > ysign) ? 1 : -1;
        if (xsign == 0)
            return 0;
        int cmp = compareMagnitude(val);
        return (xsign > 0) ? cmp : -cmp;
    }
    private int compareMagnitude(BigDecimal val) {
        long ys = val.intCompact;
        long xs = this.intCompact;
        if (xs == 0)
            return (ys == 0) ? 0 : -1;
        if (ys == 0)
            return 1;
        int sdiff = this.scale - val.scale;
        if (sdiff != 0) {
            int xae = this.precision() - this.scale;   
            int yae = val.precision() - val.scale;     
            if (xae < yae)
                return -1;
            if (xae > yae)
                return 1;
            BigInteger rb = null;
            if (sdiff < 0) {
                if ( (xs == INFLATED ||
                      (xs = longMultiplyPowerTen(xs, -sdiff)) == INFLATED) &&
                     ys == INFLATED) {
                    rb = bigMultiplyPowerTen(-sdiff);
                    return rb.compareMagnitude(val.intVal);
                }
            } else { 
                if ( (ys == INFLATED ||
                      (ys = longMultiplyPowerTen(ys, sdiff)) == INFLATED) &&
                     xs == INFLATED) {
                    rb = val.bigMultiplyPowerTen(sdiff);
                    return this.intVal.compareMagnitude(rb);
                }
            }
        }
        if (xs != INFLATED)
            return (ys != INFLATED) ? longCompareMagnitude(xs, ys) : -1;
        else if (ys != INFLATED)
            return 1;
        else
            return this.intVal.compareMagnitude(val.intVal);
    }
    @Override
    public boolean equals(Object x) {
        if (!(x instanceof BigDecimal))
            return false;
        BigDecimal xDec = (BigDecimal) x;
        if (x == this)
            return true;
        if (scale != xDec.scale)
            return false;
        long s = this.intCompact;
        long xs = xDec.intCompact;
        if (s != INFLATED) {
            if (xs == INFLATED)
                xs = compactValFor(xDec.intVal);
            return xs == s;
        } else if (xs != INFLATED)
            return xs == compactValFor(this.intVal);
        return this.inflate().equals(xDec.inflate());
    }
    public BigDecimal min(BigDecimal val) {
        return (compareTo(val) <= 0 ? this : val);
    }
    public BigDecimal max(BigDecimal val) {
        return (compareTo(val) >= 0 ? this : val);
    }
    @Override
    public int hashCode() {
        if (intCompact != INFLATED) {
            long val2 = (intCompact < 0)? -intCompact : intCompact;
            int temp = (int)( ((int)(val2 >>> 32)) * 31  +
                              (val2 & LONG_MASK));
            return 31*((intCompact < 0) ?-temp:temp) + scale;
        } else
            return 31*intVal.hashCode() + scale;
    }
    @Override
    public String toString() {
        String sc = stringCache;
        if (sc == null)
            stringCache = sc = layoutChars(true);
        return sc;
    }
    public String toEngineeringString() {
        return layoutChars(false);
    }
    public String toPlainString() {
        BigDecimal bd = this;
        if (bd.scale < 0)
            bd = bd.setScale(0);
        bd.inflate();
        if (bd.scale == 0)      
            return bd.intVal.toString();
        return bd.getValueString(bd.signum(), bd.intVal.abs().toString(), bd.scale);
    }
    private String getValueString(int signum, String intString, int scale) {
        StringBuilder buf;
        int insertionPoint = intString.length() - scale;
        if (insertionPoint == 0) {  
            return (signum<0 ? "-0." : "0.") + intString;
        } else if (insertionPoint > 0) { 
            buf = new StringBuilder(intString);
            buf.insert(insertionPoint, '.');
            if (signum < 0)
                buf.insert(0, '-');
        } else { 
            buf = new StringBuilder(3-insertionPoint + intString.length());
            buf.append(signum<0 ? "-0." : "0.");
            for (int i=0; i<-insertionPoint; i++)
                buf.append('0');
            buf.append(intString);
        }
        return buf.toString();
    }
    public BigInteger toBigInteger() {
        return this.setScale(0, ROUND_DOWN).inflate();
    }
    public BigInteger toBigIntegerExact() {
        return this.setScale(0, ROUND_UNNECESSARY).inflate();
    }
    public long longValue(){
        return (intCompact != INFLATED && scale == 0) ?
            intCompact:
            toBigInteger().longValue();
    }
    public long longValueExact() {
        if (intCompact != INFLATED && scale == 0)
            return intCompact;
        if ((precision() - scale) > 19) 
            throw new java.lang.ArithmeticException("Overflow");
        if (this.signum() == 0)
            return 0;
        if ((this.precision() - this.scale) <= 0)
            throw new ArithmeticException("Rounding necessary");
        BigDecimal num = this.setScale(0, ROUND_UNNECESSARY);
        if (num.precision() >= 19) 
            LongOverflow.check(num);
        return num.inflate().longValue();
    }
    private static class LongOverflow {
        private static final BigInteger LONGMIN = BigInteger.valueOf(Long.MIN_VALUE);
        private static final BigInteger LONGMAX = BigInteger.valueOf(Long.MAX_VALUE);
        public static void check(BigDecimal num) {
            num.inflate();
            if ((num.intVal.compareTo(LONGMIN) < 0) ||
                (num.intVal.compareTo(LONGMAX) > 0))
                throw new java.lang.ArithmeticException("Overflow");
        }
    }
    public int intValue() {
        return  (intCompact != INFLATED && scale == 0) ?
            (int)intCompact :
            toBigInteger().intValue();
    }
    public int intValueExact() {
       long num;
       num = this.longValueExact();     
       if ((int)num != num)
           throw new java.lang.ArithmeticException("Overflow");
       return (int)num;
    }
    public short shortValueExact() {
       long num;
       num = this.longValueExact();     
       if ((short)num != num)
           throw new java.lang.ArithmeticException("Overflow");
       return (short)num;
    }
    public byte byteValueExact() {
       long num;
       num = this.longValueExact();     
       if ((byte)num != num)
           throw new java.lang.ArithmeticException("Overflow");
       return (byte)num;
    }
    public float floatValue(){
        if (scale == 0 && intCompact != INFLATED)
                return (float)intCompact;
        return Float.parseFloat(this.toString());
    }
    public double doubleValue(){
        if (scale == 0 && intCompact != INFLATED)
            return (double)intCompact;
        return Double.parseDouble(this.toString());
    }
    public BigDecimal ulp() {
        return BigDecimal.valueOf(1, this.scale());
    }
    static class StringBuilderHelper {
        final StringBuilder sb;    
        final char[] cmpCharArray; 
        StringBuilderHelper() {
            sb = new StringBuilder();
            cmpCharArray = new char[19];
        }
        StringBuilder getStringBuilder() {
            sb.setLength(0);
            return sb;
        }
        char[] getCompactCharArray() {
            return cmpCharArray;
        }
        int putIntCompact(long intCompact) {
            assert intCompact >= 0;
            long q;
            int r;
            int charPos = cmpCharArray.length;
            while (intCompact > Integer.MAX_VALUE) {
                q = intCompact / 100;
                r = (int)(intCompact - q * 100);
                intCompact = q;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }
            int q2;
            int i2 = (int)intCompact;
            while (i2 >= 100) {
                q2 = i2 / 100;
                r  = i2 - q2 * 100;
                i2 = q2;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }
            cmpCharArray[--charPos] = DIGIT_ONES[i2];
            if (i2 >= 10)
                cmpCharArray[--charPos] = DIGIT_TENS[i2];
            return charPos;
        }
        final static char[] DIGIT_TENS = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        };
        final static char[] DIGIT_ONES = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        };
    }
    private String layoutChars(boolean sci) {
        if (scale == 0)                      
            return (intCompact != INFLATED) ?
                Long.toString(intCompact):
                intVal.toString();
        StringBuilderHelper sbHelper = threadLocalStringBuilderHelper.get();
        char[] coeff;
        int offset;  
        if (intCompact != INFLATED) {
            offset = sbHelper.putIntCompact(Math.abs(intCompact));
            coeff  = sbHelper.getCompactCharArray();
        } else {
            offset = 0;
            coeff  = intVal.abs().toString().toCharArray();
        }
        StringBuilder buf = sbHelper.getStringBuilder();
        if (signum() < 0)             
            buf.append('-');
        int coeffLen = coeff.length - offset;
        long adjusted = -(long)scale + (coeffLen -1);
        if ((scale >= 0) && (adjusted >= -6)) { 
            int pad = scale - coeffLen;         
            if (pad >= 0) {                     
                buf.append('0');
                buf.append('.');
                for (; pad>0; pad--) {
                    buf.append('0');
                }
                buf.append(coeff, offset, coeffLen);
            } else {                         
                buf.append(coeff, offset, -pad);
                buf.append('.');
                buf.append(coeff, -pad + offset, scale);
            }
        } else { 
            if (sci) {                       
                buf.append(coeff[offset]);   
                if (coeffLen > 1) {          
                    buf.append('.');
                    buf.append(coeff, offset + 1, coeffLen - 1);
                }
            } else {                         
                int sig = (int)(adjusted % 3);
                if (sig < 0)
                    sig += 3;                
                adjusted -= sig;             
                sig++;
                if (signum() == 0) {
                    switch (sig) {
                    case 1:
                        buf.append('0'); 
                        break;
                    case 2:
                        buf.append("0.00");
                        adjusted += 3;
                        break;
                    case 3:
                        buf.append("0.0");
                        adjusted += 3;
                        break;
                    default:
                        throw new AssertionError("Unexpected sig value " + sig);
                    }
                } else if (sig >= coeffLen) {   
                    buf.append(coeff, offset, coeffLen);
                    for (int i = sig - coeffLen; i > 0; i--)
                        buf.append('0');
                } else {                     
                    buf.append(coeff, offset, sig);
                    buf.append('.');
                    buf.append(coeff, offset + sig, coeffLen - sig);
                }
            }
            if (adjusted != 0) {             
                buf.append('E');
                if (adjusted > 0)            
                    buf.append('+');
                buf.append(adjusted);
            }
        }
        return buf.toString();
    }
    private static BigInteger bigTenToThe(int n) {
        if (n < 0)
            return BigInteger.ZERO;
        if (n < BIG_TEN_POWERS_TABLE_MAX) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            if (n < pows.length)
                return pows[n];
            else
                return expandBigIntegerTenPowers(n);
        }
        char tenpow[] = new char[n + 1];
        tenpow[0] = '1';
        for (int i = 1; i <= n; i++)
            tenpow[i] = '0';
        return new BigInteger(tenpow);
    }
    private static BigInteger expandBigIntegerTenPowers(int n) {
        synchronized(BigDecimal.class) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            int curLen = pows.length;
            if (curLen <= n) {
                int newLen = curLen << 1;
                while (newLen <= n)
                    newLen <<= 1;
                pows = Arrays.copyOf(pows, newLen);
                for (int i = curLen; i < newLen; i++)
                    pows[i] = pows[i - 1].multiply(BigInteger.TEN);
                BIG_TEN_POWERS_TABLE = pows;
            }
            return pows[n];
        }
    }
    private static final long[] LONG_TEN_POWERS_TABLE = {
        1,                     
        10,                    
        100,                   
        1000,                  
        10000,                 
        100000,                
        1000000,               
        10000000,              
        100000000,             
        1000000000,            
        10000000000L,          
        100000000000L,         
        1000000000000L,        
        10000000000000L,       
        100000000000000L,      
        1000000000000000L,     
        10000000000000000L,    
        100000000000000000L,   
        1000000000000000000L   
    };
    private static volatile BigInteger BIG_TEN_POWERS_TABLE[] = {BigInteger.ONE,
        BigInteger.valueOf(10),       BigInteger.valueOf(100),
        BigInteger.valueOf(1000),     BigInteger.valueOf(10000),
        BigInteger.valueOf(100000),   BigInteger.valueOf(1000000),
        BigInteger.valueOf(10000000), BigInteger.valueOf(100000000),
        BigInteger.valueOf(1000000000),
        BigInteger.valueOf(10000000000L),
        BigInteger.valueOf(100000000000L),
        BigInteger.valueOf(1000000000000L),
        BigInteger.valueOf(10000000000000L),
        BigInteger.valueOf(100000000000000L),
        BigInteger.valueOf(1000000000000000L),
        BigInteger.valueOf(10000000000000000L),
        BigInteger.valueOf(100000000000000000L),
        BigInteger.valueOf(1000000000000000000L)
    };
    private static final int BIG_TEN_POWERS_TABLE_INITLEN =
        BIG_TEN_POWERS_TABLE.length;
    private static final int BIG_TEN_POWERS_TABLE_MAX =
        16 * BIG_TEN_POWERS_TABLE_INITLEN;
    private static final long THRESHOLDS_TABLE[] = {
        Long.MAX_VALUE,                     
        Long.MAX_VALUE/10L,                 
        Long.MAX_VALUE/100L,                
        Long.MAX_VALUE/1000L,               
        Long.MAX_VALUE/10000L,              
        Long.MAX_VALUE/100000L,             
        Long.MAX_VALUE/1000000L,            
        Long.MAX_VALUE/10000000L,           
        Long.MAX_VALUE/100000000L,          
        Long.MAX_VALUE/1000000000L,         
        Long.MAX_VALUE/10000000000L,        
        Long.MAX_VALUE/100000000000L,       
        Long.MAX_VALUE/1000000000000L,      
        Long.MAX_VALUE/10000000000000L,     
        Long.MAX_VALUE/100000000000000L,    
        Long.MAX_VALUE/1000000000000000L,   
        Long.MAX_VALUE/10000000000000000L,  
        Long.MAX_VALUE/100000000000000000L, 
        Long.MAX_VALUE/1000000000000000000L 
    };
    private static long longMultiplyPowerTen(long val, int n) {
        if (val == 0 || n <= 0)
            return val;
        long[] tab = LONG_TEN_POWERS_TABLE;
        long[] bounds = THRESHOLDS_TABLE;
        if (n < tab.length && n < bounds.length) {
            long tenpower = tab[n];
            if (val == 1)
                return tenpower;
            if (Math.abs(val) <= bounds[n])
                return val * tenpower;
        }
        return INFLATED;
    }
    private BigInteger bigMultiplyPowerTen(int n) {
        if (n <= 0)
            return this.inflate();
        if (intCompact != INFLATED)
            return bigTenToThe(n).multiply(intCompact);
        else
            return intVal.multiply(bigTenToThe(n));
    }
    private BigInteger inflate() {
        if (intVal == null)
            intVal = BigInteger.valueOf(intCompact);
        return intVal;
    }
    private static void matchScale(BigDecimal[] val) {
        if (val[0].scale == val[1].scale) {
            return;
        } else if (val[0].scale < val[1].scale) {
            val[0] = val[0].setScale(val[1].scale, ROUND_UNNECESSARY);
        } else if (val[1].scale < val[0].scale) {
            val[1] = val[1].setScale(val[0].scale, ROUND_UNNECESSARY);
        }
    }
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (intVal == null) {
            String message = "BigDecimal: null intVal in stream";
            throw new java.io.StreamCorruptedException(message);
        }
        intCompact = compactValFor(intVal);
    }
   private void writeObject(java.io.ObjectOutputStream s)
       throws java.io.IOException {
       this.inflate();
       s.defaultWriteObject();
   }
    private static int longDigitLength(long x) {
        assert x != INFLATED;
        if (x < 0)
            x = -x;
        if (x < 10) 
            return 1;
        int n = 64; 
        int y = (int)(x >>> 32);
        if (y == 0) { n -= 32; y = (int)x; }
        if (y >>> 16 == 0) { n -= 16; y <<= 16; }
        if (y >>> 24 == 0) { n -=  8; y <<=  8; }
        if (y >>> 28 == 0) { n -=  4; y <<=  4; }
        if (y >>> 30 == 0) { n -=  2; y <<=  2; }
        int r = (((y >>> 31) + n) * 1233) >>> 12;
        long[] tab = LONG_TEN_POWERS_TABLE;
        return (r >= tab.length || x < tab[r])? r : r+1;
    }
    private static int bigDigitLength(BigInteger b) {
        if (b.signum == 0)
            return 1;
        int r = (int)((((long)b.bitLength() + 1) * 646456993) >>> 31);
        return b.compareMagnitude(bigTenToThe(r)) < 0? r : r+1;
    }
    private BigDecimal stripZerosToMatchScale(long preferredScale) {
        this.inflate();
        BigInteger qr[];                
        while ( intVal.compareMagnitude(BigInteger.TEN) >= 0 &&
                scale > preferredScale) {
            if (intVal.testBit(0))
                break;                  
            qr = intVal.divideAndRemainder(BigInteger.TEN);
            if (qr[1].signum() != 0)
                break;                  
            intVal=qr[0];
            scale = checkScale((long)scale-1);  
            if (precision > 0)          
              precision--;
        }
        if (intVal != null)
            intCompact = compactValFor(intVal);
        return this;
    }
    private int checkScale(long val) {
        int asInt = (int)val;
        if (asInt != val) {
            asInt = val>Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            BigInteger b;
            if (intCompact != 0 &&
                ((b = intVal) == null || b.signum() != 0))
                throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }
    private BigDecimal roundOp(MathContext mc) {
        BigDecimal rounded = doRound(this, mc);
        return rounded;
    }
    private void roundThis(MathContext mc) {
        BigDecimal rounded = doRound(this, mc);
        if (rounded == this)                 
            return;
        this.intVal     = rounded.intVal;
        this.intCompact = rounded.intCompact;
        this.scale      = rounded.scale;
        this.precision  = rounded.precision;
    }
    private static BigDecimal doRound(BigDecimal d, MathContext mc) {
        int mcp = mc.precision;
        int drop;
        while ((drop = d.precision() - mcp) > 0) {
            int newScale = d.checkScale((long)d.scale - drop);
            int mode = mc.roundingMode.oldMode;
            if (drop < LONG_TEN_POWERS_TABLE.length)
                d = divideAndRound(d.intCompact, d.intVal,
                                   LONG_TEN_POWERS_TABLE[drop], null,
                                   newScale, mode, newScale);
            else
                d = divideAndRound(d.intCompact, d.intVal,
                                   INFLATED, bigTenToThe(drop),
                                   newScale, mode, newScale);
        }
        return d;
    }
    private static long compactValFor(BigInteger b) {
        int[] m = b.mag;
        int len = m.length;
        if (len == 0)
            return 0;
        int d = m[0];
        if (len > 2 || (len == 2 && d < 0))
            return INFLATED;
        long u = (len == 2)?
            (((long) m[1] & LONG_MASK) + (((long)d) << 32)) :
            (((long)d)   & LONG_MASK);
        return (b.signum < 0)? -u : u;
    }
    private static int longCompareMagnitude(long x, long y) {
        if (x < 0)
            x = -x;
        if (y < 0)
            y = -y;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
    private static int saturateLong(long s) {
        int i = (int)s;
        return (s == i) ? i : (s < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE);
    }
    private static void print(String name, BigDecimal bd) {
        System.err.format("%s:\tintCompact %d\tintVal %d\tscale %d\tprecision %d%n",
                          name,
                          bd.intCompact,
                          bd.intVal,
                          bd.scale,
                          bd.precision);
    }
    private BigDecimal audit() {
        if (intCompact == INFLATED) {
            if (intVal == null) {
                print("audit", this);
                throw new AssertionError("null intVal");
            }
            if (precision > 0 && precision != bigDigitLength(intVal)) {
                print("audit", this);
                throw new AssertionError("precision mismatch");
            }
        } else {
            if (intVal != null) {
                long val = intVal.longValue();
                if (val != intCompact) {
                    print("audit", this);
                    throw new AssertionError("Inconsistent state, intCompact=" +
                                             intCompact + "\t intVal=" + val);
                }
            }
            if (precision > 0 && precision != longDigitLength(intCompact)) {
                print("audit", this);
                throw new AssertionError("precision mismatch");
            }
        }
        return this;
    }
}
