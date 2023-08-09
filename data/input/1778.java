public class FormattedFloatingDecimal{
    boolean     isExceptional;
    boolean     isNegative;
    int         decExponent;  
    int         decExponentRounded;
    char        digits[];
    int         nDigits;
    int         bigIntExp;
    int         bigIntNBits;
    boolean     mustSetRoundDir = false;
    boolean     fromHex = false;
    int         roundDir = 0; 
    int         precision;    
    public enum Form { SCIENTIFIC, COMPATIBLE, DECIMAL_FLOAT, GENERAL };
    private Form form;
    private     FormattedFloatingDecimal( boolean negSign, int decExponent, char []digits, int n, boolean e, int precision, Form form )
    {
        isNegative = negSign;
        isExceptional = e;
        this.decExponent = decExponent;
        this.digits = digits;
        this.nDigits = n;
        this.precision = precision;
        this.form = form;
    }
    static final long   signMask = 0x8000000000000000L;
    static final long   expMask  = 0x7ff0000000000000L;
    static final long   fractMask= ~(signMask|expMask);
    static final int    expShift = 52;
    static final int    expBias  = 1023;
    static final long   fractHOB = ( 1L<<expShift ); 
    static final long   expOne   = ((long)expBias)<<expShift; 
    static final int    maxSmallBinExp = 62;
    static final int    minSmallBinExp = -( 63 / 3 );
    static final int    maxDecimalDigits = 15;
    static final int    maxDecimalExponent = 308;
    static final int    minDecimalExponent = -324;
    static final int    bigDecimalExponent = 324; 
    static final long   highbyte = 0xff00000000000000L;
    static final long   highbit  = 0x8000000000000000L;
    static final long   lowbytes = ~highbyte;
    static final int    singleSignMask =    0x80000000;
    static final int    singleExpMask  =    0x7f800000;
    static final int    singleFractMask =   ~(singleSignMask|singleExpMask);
    static final int    singleExpShift  =   23;
    static final int    singleFractHOB  =   1<<singleExpShift;
    static final int    singleExpBias   =   127;
    static final int    singleMaxDecimalDigits = 7;
    static final int    singleMaxDecimalExponent = 38;
    static final int    singleMinDecimalExponent = -45;
    static final int    intDecimalDigits = 9;
    private static int
    countBits( long v ){
        if ( v == 0L ) return 0;
        while ( ( v & highbyte ) == 0L ){
            v <<= 8;
        }
        while ( v > 0L ) { 
            v <<= 1;
        }
        int n = 0;
        while (( v & lowbytes ) != 0L ){
            v <<= 8;
            n += 8;
        }
        while ( v != 0L ){
            v <<= 1;
            n += 1;
        }
        return n;
    }
    private static FDBigInt b5p[];
    private static synchronized FDBigInt
    big5pow( int p ){
        assert p >= 0 : p; 
        if ( b5p == null ){
            b5p = new FDBigInt[ p+1 ];
        }else if (b5p.length <= p ){
            FDBigInt t[] = new FDBigInt[ p+1 ];
            System.arraycopy( b5p, 0, t, 0, b5p.length );
            b5p = t;
        }
        if ( b5p[p] != null )
            return b5p[p];
        else if ( p < small5pow.length )
            return b5p[p] = new FDBigInt( small5pow[p] );
        else if ( p < long5pow.length )
            return b5p[p] = new FDBigInt( long5pow[p] );
        else {
            int q, r;
            q = p >> 1;
            r = p - q;
            FDBigInt bigq =  b5p[q];
            if ( bigq == null )
                bigq = big5pow ( q );
            if ( r < small5pow.length ){
                return (b5p[p] = bigq.mult( small5pow[r] ) );
            }else{
                FDBigInt bigr = b5p[ r ];
                if ( bigr == null )
                    bigr = big5pow( r );
                return (b5p[p] = bigq.mult( bigr ) );
            }
        }
    }
    private static FDBigInt
    multPow52( FDBigInt v, int p5, int p2 ){
        if ( p5 != 0 ){
            if ( p5 < small5pow.length ){
                v = v.mult( small5pow[p5] );
            } else {
                v = v.mult( big5pow( p5 ) );
            }
        }
        if ( p2 != 0 ){
            v.lshiftMe( p2 );
        }
        return v;
    }
    private static FDBigInt
    constructPow52( int p5, int p2 ){
        FDBigInt v = new FDBigInt( big5pow( p5 ) );
        if ( p2 != 0 ){
            v.lshiftMe( p2 );
        }
        return v;
    }
    private FDBigInt
    doubleToBigInt( double dval ){
        long lbits = Double.doubleToLongBits( dval ) & ~signMask;
        int binexp = (int)(lbits >>> expShift);
        lbits &= fractMask;
        if ( binexp > 0 ){
            lbits |= fractHOB;
        } else {
            assert lbits != 0L : lbits; 
            binexp +=1;
            while ( (lbits & fractHOB ) == 0L){
                lbits <<= 1;
                binexp -= 1;
            }
        }
        binexp -= expBias;
        int nbits = countBits( lbits );
        int lowOrderZeros = expShift+1-nbits;
        lbits >>>= lowOrderZeros;
        bigIntExp = binexp+1-nbits;
        bigIntNBits = nbits;
        return new FDBigInt( lbits );
    }
    private static double ulp( double dval, boolean subtracting ){
        long lbits = Double.doubleToLongBits( dval ) & ~signMask;
        int binexp = (int)(lbits >>> expShift);
        double ulpval;
        if ( subtracting && ( binexp >= expShift ) && ((lbits&fractMask) == 0L) ){
            binexp -= 1;
        }
        if ( binexp > expShift ){
            ulpval = Double.longBitsToDouble( ((long)(binexp-expShift))<<expShift );
        } else if ( binexp == 0 ){
            ulpval = Double.MIN_VALUE;
        } else {
            ulpval = Double.longBitsToDouble( 1L<<(binexp-1) );
        }
        if ( subtracting ) ulpval = - ulpval;
        return ulpval;
    }
    float
    stickyRound( double dval ){
        long lbits = Double.doubleToLongBits( dval );
        long binexp = lbits & expMask;
        if ( binexp == 0L || binexp == expMask ){
            return (float) dval;
        }
        lbits += (long)roundDir; 
        return (float)Double.longBitsToDouble( lbits );
    }
    private void
    developLongDigits( int decExponent, long lvalue, long insignificant ){
        char digits[];
        int  ndigits;
        int  digitno;
        int  c;
        int i;
        for ( i = 0; insignificant >= 10L; i++ )
            insignificant /= 10L;
        if ( i != 0 ){
            long pow10 = long5pow[i] << i; 
            long residue = lvalue % pow10;
            lvalue /= pow10;
            decExponent += i;
            if ( residue >= (pow10>>1) ){
                lvalue++;
            }
        }
        if ( lvalue <= Integer.MAX_VALUE ){
            assert lvalue > 0L : lvalue; 
            int  ivalue = (int)lvalue;
            ndigits = 10;
            digits = (char[])(perThreadBuffer.get());
            digitno = ndigits-1;
            c = ivalue%10;
            ivalue /= 10;
            while ( c == 0 ){
                decExponent++;
                c = ivalue%10;
                ivalue /= 10;
            }
            while ( ivalue != 0){
                digits[digitno--] = (char)(c+'0');
                decExponent++;
                c = ivalue%10;
                ivalue /= 10;
            }
            digits[digitno] = (char)(c+'0');
        } else {
            ndigits = 20;
            digits = (char[])(perThreadBuffer.get());
            digitno = ndigits-1;
            c = (int)(lvalue%10L);
            lvalue /= 10L;
            while ( c == 0 ){
                decExponent++;
                c = (int)(lvalue%10L);
                lvalue /= 10L;
            }
            while ( lvalue != 0L ){
                digits[digitno--] = (char)(c+'0');
                decExponent++;
                c = (int)(lvalue%10L);
                lvalue /= 10;
            }
            digits[digitno] = (char)(c+'0');
        }
        char result [];
        ndigits -= digitno;
        result = new char[ ndigits ];
        System.arraycopy( digits, digitno, result, 0, ndigits );
        this.digits = result;
        this.decExponent = decExponent+1;
        this.nDigits = ndigits;
    }
    private void
    roundup(){
        int i;
        int q = digits[ i = (nDigits-1)];
        if ( q == '9' ){
            while ( q == '9' && i > 0 ){
                digits[i] = '0';
                q = digits[--i];
            }
            if ( q == '9' ){
                decExponent += 1;
                digits[0] = '1';
                return;
            }
        }
        digits[i] = (char)(q+1);
    }
    private int checkExponent(int length) {
        if (length >= nDigits || length < 0)
            return decExponent;
        for (int i = 0; i < length; i++)
            if (digits[i] != '9')
                return decExponent;
        return decExponent + (digits[length] >= '5' ? 1 : 0);
    }
    private char [] applyPrecision(int length) {
        char [] result = new char[nDigits];
        for (int i = 0; i < result.length; i++) result[i] = '0';
        if (length >= nDigits || length < 0) {
            System.arraycopy(digits, 0, result, 0, nDigits);
            return result;
        }
        if (length == 0) {
            if (digits[0] >= '5') {
                result[0] = '1';
            }
            return result;
        }
        int i = length;
        int q = digits[i];
        if (q >= '5' && i > 0) {
            q = digits[--i];
            if ( q == '9' ) {
                while ( q == '9' && i > 0 ){
                    q = digits[--i];
                }
                if ( q == '9' ){
                    result[0] = '1';
                    return result;
                }
            }
            result[i] = (char)(q + 1);
        }
        while (--i >= 0) {
            result[i] = digits[i];
        }
        return result;
    }
    public FormattedFloatingDecimal( double d )
    {
        this(d, Integer.MAX_VALUE, Form.COMPATIBLE);
    }
    public FormattedFloatingDecimal( double d, int precision, Form form )
    {
        long    dBits = Double.doubleToLongBits( d );
        long    fractBits;
        int     binExp;
        int     nSignificantBits;
        this.precision = precision;
        this.form      = form;
        if ( (dBits&signMask) != 0 ){
            isNegative = true;
            dBits ^= signMask;
        } else {
            isNegative = false;
        }
        binExp = (int)( (dBits&expMask) >> expShift );
        fractBits = dBits&fractMask;
        if ( binExp == (int)(expMask>>expShift) ) {
            isExceptional = true;
            if ( fractBits == 0L ){
                digits =  infinity;
            } else {
                digits = notANumber;
                isNegative = false; 
            }
            nDigits = digits.length;
            return;
        }
        isExceptional = false;
        if ( binExp == 0 ){
            if ( fractBits == 0L ){
                decExponent = 0;
                digits = zero;
                nDigits = 1;
                return;
            }
            while ( (fractBits&fractHOB) == 0L ){
                fractBits <<= 1;
                binExp -= 1;
            }
            nSignificantBits = expShift + binExp +1; 
            binExp += 1;
        } else {
            fractBits |= fractHOB;
            nSignificantBits = expShift+1;
        }
        binExp -= expBias;
        dtoa( binExp, fractBits, nSignificantBits );
    }
    public FormattedFloatingDecimal( float f )
    {
        this(f, Integer.MAX_VALUE, Form.COMPATIBLE);
    }
    public FormattedFloatingDecimal( float f, int precision, Form form )
    {
        int     fBits = Float.floatToIntBits( f );
        int     fractBits;
        int     binExp;
        int     nSignificantBits;
        this.precision = precision;
        this.form      = form;
        if ( (fBits&singleSignMask) != 0 ){
            isNegative = true;
            fBits ^= singleSignMask;
        } else {
            isNegative = false;
        }
        binExp = (int)( (fBits&singleExpMask) >> singleExpShift );
        fractBits = fBits&singleFractMask;
        if ( binExp == (int)(singleExpMask>>singleExpShift) ) {
            isExceptional = true;
            if ( fractBits == 0L ){
                digits =  infinity;
            } else {
                digits = notANumber;
                isNegative = false; 
            }
            nDigits = digits.length;
            return;
        }
        isExceptional = false;
        if ( binExp == 0 ){
            if ( fractBits == 0 ){
                decExponent = 0;
                digits = zero;
                nDigits = 1;
                return;
            }
            while ( (fractBits&singleFractHOB) == 0 ){
                fractBits <<= 1;
                binExp -= 1;
            }
            nSignificantBits = singleExpShift + binExp +1; 
            binExp += 1;
        } else {
            fractBits |= singleFractHOB;
            nSignificantBits = singleExpShift+1;
        }
        binExp -= singleExpBias;
        dtoa( binExp, ((long)fractBits)<<(expShift-singleExpShift), nSignificantBits );
    }
    private void
    dtoa( int binExp, long fractBits, int nSignificantBits )
    {
        int     nFractBits; 
        int     nTinyBits;  
        int     decExp;
        nFractBits = countBits( fractBits );
        nTinyBits = Math.max( 0, nFractBits - binExp - 1 );
        if ( binExp <= maxSmallBinExp && binExp >= minSmallBinExp ){
            if ( (nTinyBits < long5pow.length) && ((nFractBits + n5bits[nTinyBits]) < 64 ) ){
                long halfULP;
                if ( nTinyBits == 0 ) {
                    if ( binExp > nSignificantBits ){
                        halfULP = 1L << ( binExp-nSignificantBits-1);
                    } else {
                        halfULP = 0L;
                    }
                    if ( binExp >= expShift ){
                        fractBits <<= (binExp-expShift);
                    } else {
                        fractBits >>>= (expShift-binExp) ;
                    }
                    developLongDigits( 0, fractBits, halfULP );
                    return;
                }
            }
        }
        double d2 = Double.longBitsToDouble(
            expOne | ( fractBits &~ fractHOB ) );
        decExp = (int)Math.floor(
            (d2-1.5D)*0.289529654D + 0.176091259 + (double)binExp * 0.301029995663981 );
        int B2, B5; 
        int S2, S5; 
        int M2, M5; 
        int Bbits; 
        int tenSbits; 
        FDBigInt Sval, Bval, Mval;
        B5 = Math.max( 0, -decExp );
        B2 = B5 + nTinyBits + binExp;
        S5 = Math.max( 0, decExp );
        S2 = S5 + nTinyBits;
        M5 = B5;
        M2 = B2 - nSignificantBits;
        fractBits >>>= (expShift+1-nFractBits);
        B2 -= nFractBits-1;
        int common2factor = Math.min( B2, S2 );
        B2 -= common2factor;
        S2 -= common2factor;
        M2 -= common2factor;
        if ( nFractBits == 1 )
            M2 -= 1;
        if ( M2 < 0 ){
            B2 -= M2;
            S2 -= M2;
            M2 =  0;
        }
        char digits[] = this.digits = new char[18];
        int  ndigit = 0;
        boolean low, high;
        long lowDigitDifference;
        int  q;
        Bbits = nFractBits + B2 + (( B5 < n5bits.length )? n5bits[B5] : ( B5*3 ));
        tenSbits = S2+1 + (( (S5+1) < n5bits.length )? n5bits[(S5+1)] : ( (S5+1)*3 ));
        if ( Bbits < 64 && tenSbits < 64){
            if ( Bbits < 32 && tenSbits < 32){
                int b = ((int)fractBits * small5pow[B5] ) << B2;
                int s = small5pow[S5] << S2;
                int m = small5pow[M5] << M2;
                int tens = s * 10;
                ndigit = 0;
                q = b / s;
                b = 10 * ( b % s );
                m *= 10;
                low  = (b <  m );
                high = (b+m > tens );
                assert q < 10 : q; 
                if ( (q == 0) && ! high ){
                    decExp--;
                } else {
                    digits[ndigit++] = (char)('0' + q);
                }
                if (! (form == Form.COMPATIBLE && -3 < decExp && decExp < 8)) {
                    high = low = false;
                }
                while( ! low && ! high ){
                    q = b / s;
                    b = 10 * ( b % s );
                    m *= 10;
                    assert q < 10 : q; 
                    if ( m > 0L ){
                        low  = (b <  m );
                        high = (b+m > tens );
                    } else {
                        low = true;
                        high = true;
                    }
                    digits[ndigit++] = (char)('0' + q);
                }
                lowDigitDifference = (b<<1) - tens;
            } else {
                long b = (fractBits * long5pow[B5] ) << B2;
                long s = long5pow[S5] << S2;
                long m = long5pow[M5] << M2;
                long tens = s * 10L;
                ndigit = 0;
                q = (int) ( b / s );
                b = 10L * ( b % s );
                m *= 10L;
                low  = (b <  m );
                high = (b+m > tens );
                assert q < 10 : q; 
                if ( (q == 0) && ! high ){
                    decExp--;
                } else {
                    digits[ndigit++] = (char)('0' + q);
                }
                if (! (form == Form.COMPATIBLE && -3 < decExp && decExp < 8)) {
                    high = low = false;
                }
                while( ! low && ! high ){
                    q = (int) ( b / s );
                    b = 10 * ( b % s );
                    m *= 10;
                    assert q < 10 : q;  
                    if ( m > 0L ){
                        low  = (b <  m );
                        high = (b+m > tens );
                    } else {
                        low = true;
                        high = true;
                    }
                    digits[ndigit++] = (char)('0' + q);
                }
                lowDigitDifference = (b<<1) - tens;
            }
        } else {
            FDBigInt tenSval;
            int  shiftBias;
            Bval = multPow52( new FDBigInt( fractBits  ), B5, B2 );
            Sval = constructPow52( S5, S2 );
            Mval = constructPow52( M5, M2 );
            Bval.lshiftMe( shiftBias = Sval.normalizeMe() );
            Mval.lshiftMe( shiftBias );
            tenSval = Sval.mult( 10 );
            ndigit = 0;
            q = Bval.quoRemIteration( Sval );
            Mval = Mval.mult( 10 );
            low  = (Bval.cmp( Mval ) < 0);
            high = (Bval.add( Mval ).cmp( tenSval ) > 0 );
            assert q < 10 : q; 
            if ( (q == 0) && ! high ){
                decExp--;
            } else {
                digits[ndigit++] = (char)('0' + q);
            }
            if (! (form == Form.COMPATIBLE && -3 < decExp && decExp < 8)) {
                high = low = false;
            }
            while( ! low && ! high ){
                q = Bval.quoRemIteration( Sval );
                Mval = Mval.mult( 10 );
                assert q < 10 : q;  
                low  = (Bval.cmp( Mval ) < 0);
                high = (Bval.add( Mval ).cmp( tenSval ) > 0 );
                digits[ndigit++] = (char)('0' + q);
            }
            if ( high && low ){
                Bval.lshiftMe(1);
                lowDigitDifference = Bval.cmp(tenSval);
            } else
                lowDigitDifference = 0L; 
        }
        this.decExponent = decExp+1;
        this.digits = digits;
        this.nDigits = ndigit;
        if ( high ){
            if ( low ){
                if ( lowDigitDifference == 0L ){
                    if ( (digits[nDigits-1]&1) != 0 ) roundup();
                } else if ( lowDigitDifference > 0 ){
                    roundup();
                }
            } else {
                roundup();
            }
        }
    }
    public String
    toString(){
        StringBuffer result = new StringBuffer( nDigits+8 );
        if ( isNegative ){ result.append( '-' ); }
        if ( isExceptional ){
            result.append( digits, 0, nDigits );
        } else {
            result.append( "0.");
            result.append( digits, 0, nDigits );
            result.append('e');
            result.append( decExponent );
        }
        return new String(result);
    }
    public int getExponent() {
        return decExponent - 1;
    }
    public int getExponentRounded() {
        return decExponentRounded - 1;
    }
    public int getChars(char[] result) {
        assert nDigits <= 19 : nDigits; 
        int i = 0;
        if (isNegative) { result[0] = '-'; i = 1; }
        if (isExceptional) {
            System.arraycopy(digits, 0, result, i, nDigits);
            i += nDigits;
        } else {
            char digits [] = this.digits;
            int exp = decExponent;
            switch (form) {
            case COMPATIBLE:
                break;
            case DECIMAL_FLOAT:
                exp = checkExponent(decExponent + precision);
                digits = applyPrecision(decExponent + precision);
                break;
            case SCIENTIFIC:
                exp = checkExponent(precision + 1);
                digits = applyPrecision(precision + 1);
                break;
            case GENERAL:
                exp = checkExponent(precision);
                digits = applyPrecision(precision);
                if (exp - 1 < -4 || exp - 1 >= precision) {
                    form = Form.SCIENTIFIC;
                    precision--;
                } else {
                    form = Form.DECIMAL_FLOAT;
                    precision = precision - exp;
                }
                break;
            default:
                assert false;
            }
            decExponentRounded = exp;
            if (exp > 0
                && ((form == Form.COMPATIBLE && (exp < 8))
                    || (form == Form.DECIMAL_FLOAT)))
            {
                int charLength = Math.min(nDigits, exp);
                System.arraycopy(digits, 0, result, i, charLength);
                i += charLength;
                if (charLength < exp) {
                    charLength = exp-charLength;
                    for (int nz = 0; nz < charLength; nz++)
                        result[i++] = '0';
                    if (form == Form.COMPATIBLE) {
                        result[i++] = '.';
                        result[i++] = '0';
                    }
                } else {
                    if (form == Form.COMPATIBLE) {
                        result[i++] = '.';
                        if (charLength < nDigits) {
                            int t = Math.min(nDigits - charLength, precision);
                            System.arraycopy(digits, charLength, result, i, t);
                            i += t;
                        } else {
                            result[i++] = '0';
                        }
                    } else {
                        int t = Math.min(nDigits - charLength, precision);
                        if (t > 0) {
                            result[i++] = '.';
                            System.arraycopy(digits, charLength, result, i, t);
                            i += t;
                        }
                    }
                }
            } else if (exp <= 0
                       && ((form == Form.COMPATIBLE && exp > -3)
                       || (form == Form.DECIMAL_FLOAT)))
            {
                result[i++] = '0';
                if (exp != 0) {
                    int t = Math.min(-exp, precision);
                    if (t > 0) {
                        result[i++] = '.';
                        for (int nz = 0; nz < t; nz++)
                            result[i++] = '0';
                    }
                }
                int t = Math.min(digits.length, precision + exp);
                if (t > 0) {
                    if (i == 1)
                        result[i++] = '.';
                    System.arraycopy(digits, 0, result, i, t);
                    i += t;
                }
            } else {
                result[i++] = digits[0];
                if (form == Form.COMPATIBLE) {
                    result[i++] = '.';
                    if (nDigits > 1) {
                        System.arraycopy(digits, 1, result, i, nDigits-1);
                        i += nDigits-1;
                    } else {
                        result[i++] = '0';
                    }
                    result[i++] = 'E';
                } else {
                    if (nDigits > 1) {
                        int t = Math.min(nDigits -1, precision);
                        if (t > 0) {
                            result[i++] = '.';
                            System.arraycopy(digits, 1, result, i, t);
                            i += t;
                        }
                    }
                    result[i++] = 'e';
                }
                int e;
                if (exp <= 0) {
                    result[i++] = '-';
                    e = -exp+1;
                } else {
                    if (form != Form.COMPATIBLE)
                        result[i++] = '+';
                    e = exp-1;
                }
                if (e <= 9) {
                    if (form != Form.COMPATIBLE)
                        result[i++] = '0';
                    result[i++] = (char)(e+'0');
                } else if (e <= 99) {
                    result[i++] = (char)(e/10 +'0');
                    result[i++] = (char)(e%10 + '0');
                } else {
                    result[i++] = (char)(e/100+'0');
                    e %= 100;
                    result[i++] = (char)(e/10+'0');
                    result[i++] = (char)(e%10 + '0');
                }
            }
        }
        return i;
    }
    private static ThreadLocal perThreadBuffer = new ThreadLocal() {
            protected synchronized Object initialValue() {
                return new char[26];
            }
        };
    public strictfp double doubleValue(){
        int     kDigits = Math.min( nDigits, maxDecimalDigits+1 );
        long    lValue;
        double  dValue;
        double  rValue, tValue;
        if(digits == infinity || digits == notANumber) {
            if(digits == notANumber)
                return Double.NaN;
            else
                return (isNegative?Double.NEGATIVE_INFINITY:Double.POSITIVE_INFINITY);
        }
        else {
            if (mustSetRoundDir) {
                roundDir = 0;
            }
            int iValue = (int)digits[0]-(int)'0';
            int iDigits = Math.min( kDigits, intDecimalDigits );
            for ( int i=1; i < iDigits; i++ ){
                iValue = iValue*10 + (int)digits[i]-(int)'0';
            }
            lValue = (long)iValue;
            for ( int i=iDigits; i < kDigits; i++ ){
                lValue = lValue*10L + (long)((int)digits[i]-(int)'0');
            }
            dValue = (double)lValue;
            int exp = decExponent-kDigits;
            if ( nDigits <= maxDecimalDigits ){
                if (exp == 0 || dValue == 0.0)
                    return (isNegative)? -dValue : dValue; 
                else if ( exp >= 0 ){
                    if ( exp <= maxSmallTen ){
                        rValue = dValue * small10pow[exp];
                        if ( mustSetRoundDir ){
                            tValue = rValue / small10pow[exp];
                            roundDir = ( tValue ==  dValue ) ? 0
                                :( tValue < dValue ) ? 1
                                : -1;
                        }
                        return (isNegative)? -rValue : rValue;
                    }
                    int slop = maxDecimalDigits - kDigits;
                    if ( exp <= maxSmallTen+slop ){
                        dValue *= small10pow[slop];
                        rValue = dValue * small10pow[exp-slop];
                        if ( mustSetRoundDir ){
                            tValue = rValue / small10pow[exp-slop];
                            roundDir = ( tValue ==  dValue ) ? 0
                                :( tValue < dValue ) ? 1
                                : -1;
                        }
                        return (isNegative)? -rValue : rValue;
                    }
                } else {
                    if ( exp >= -maxSmallTen ){
                        rValue = dValue / small10pow[-exp];
                        tValue = rValue * small10pow[-exp];
                        if ( mustSetRoundDir ){
                            roundDir = ( tValue ==  dValue ) ? 0
                                :( tValue < dValue ) ? 1
                                : -1;
                        }
                        return (isNegative)? -rValue : rValue;
                    }
                }
            }
            if ( exp > 0 ){
                if ( decExponent > maxDecimalExponent+1 ){
                    return (isNegative)? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
                if ( (exp&15) != 0 ){
                    dValue *= small10pow[exp&15];
                }
                if ( (exp>>=4) != 0 ){
                    int j;
                    for( j = 0; exp > 1; j++, exp>>=1 ){
                        if ( (exp&1)!=0)
                            dValue *= big10pow[j];
                    }
                    double t = dValue * big10pow[j];
                    if ( Double.isInfinite( t ) ){
                        t = dValue / 2.0;
                        t *= big10pow[j];
                        if ( Double.isInfinite( t ) ){
                            return (isNegative)? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        }
                        t = Double.MAX_VALUE;
                    }
                    dValue = t;
                }
            } else if ( exp < 0 ){
                exp = -exp;
                if ( decExponent < minDecimalExponent-1 ){
                    return (isNegative)? -0.0 : 0.0;
                }
                if ( (exp&15) != 0 ){
                    dValue /= small10pow[exp&15];
                }
                if ( (exp>>=4) != 0 ){
                    int j;
                    for( j = 0; exp > 1; j++, exp>>=1 ){
                        if ( (exp&1)!=0)
                            dValue *= tiny10pow[j];
                    }
                    double t = dValue * tiny10pow[j];
                    if ( t == 0.0 ){
                        t = dValue * 2.0;
                        t *= tiny10pow[j];
                        if ( t == 0.0 ){
                            return (isNegative)? -0.0 : 0.0;
                        }
                        t = Double.MIN_VALUE;
                    }
                    dValue = t;
                }
            }
            FDBigInt bigD0 = new FDBigInt( lValue, digits, kDigits, nDigits );
            exp   = decExponent - nDigits;
            correctionLoop:
            while(true){
                FDBigInt bigB = doubleToBigInt( dValue );
                int B2, B5; 
                int     D2, D5; 
                int Ulp2;   
                if ( exp >= 0 ){
                    B2 = B5 = 0;
                    D2 = D5 = exp;
                } else {
                    B2 = B5 = -exp;
                    D2 = D5 = 0;
                }
                if ( bigIntExp >= 0 ){
                    B2 += bigIntExp;
                } else {
                    D2 -= bigIntExp;
                }
                Ulp2 = B2;
                int hulpbias;
                if ( bigIntExp+bigIntNBits <= -expBias+1 ){
                    hulpbias = bigIntExp+ expBias + expShift;
                } else {
                    hulpbias = expShift + 2 - bigIntNBits;
                }
                B2 += hulpbias;
                D2 += hulpbias;
                int common2 = Math.min( B2, Math.min( D2, Ulp2 ) );
                B2 -= common2;
                D2 -= common2;
                Ulp2 -= common2;
                bigB = multPow52( bigB, B5, B2 );
                FDBigInt bigD = multPow52( new FDBigInt( bigD0 ), D5, D2 );
                FDBigInt diff;
                int cmpResult;
                boolean overvalue;
                if ( (cmpResult = bigB.cmp( bigD ) ) > 0 ){
                    overvalue = true; 
                    diff = bigB.sub( bigD );
                    if ( (bigIntNBits == 1) && (bigIntExp > -expBias) ){
                        Ulp2 -= 1;
                        if ( Ulp2 < 0 ){
                            Ulp2 = 0;
                            diff.lshiftMe( 1 );
                        }
                    }
                } else if ( cmpResult < 0 ){
                    overvalue = false; 
                    diff = bigD.sub( bigB );
                } else {
                    break correctionLoop;
                }
                FDBigInt halfUlp = constructPow52( B5, Ulp2 );
                if ( (cmpResult = diff.cmp( halfUlp ) ) < 0 ){
                    if (mustSetRoundDir) {
                        roundDir = overvalue ? -1 : 1;
                    }
                    break correctionLoop;
                } else if ( cmpResult == 0 ){
                    dValue += 0.5*ulp( dValue, overvalue );
                    if (mustSetRoundDir) {
                        roundDir = overvalue ? -1 : 1;
                    }
                    break correctionLoop;
                } else {
                    dValue += ulp( dValue, overvalue );
                    if ( dValue == 0.0 || dValue == Double.POSITIVE_INFINITY )
                        break correctionLoop; 
                    continue; 
                }
            }
            return (isNegative)? -dValue : dValue;
        }
    }
    public strictfp float floatValue(){
        int     kDigits = Math.min( nDigits, singleMaxDecimalDigits+1 );
        int     iValue;
        float   fValue;
        if(digits == infinity || digits == notANumber) {
            if(digits == notANumber)
                return Float.NaN;
            else
                return (isNegative?Float.NEGATIVE_INFINITY:Float.POSITIVE_INFINITY);
        }
        else {
            iValue = (int)digits[0]-(int)'0';
            for ( int i=1; i < kDigits; i++ ){
                iValue = iValue*10 + (int)digits[i]-(int)'0';
            }
            fValue = (float)iValue;
            int exp = decExponent-kDigits;
            if ( nDigits <= singleMaxDecimalDigits ){
                if (exp == 0 || fValue == 0.0f)
                    return (isNegative)? -fValue : fValue; 
                else if ( exp >= 0 ){
                    if ( exp <= singleMaxSmallTen ){
                        fValue *= singleSmall10pow[exp];
                        return (isNegative)? -fValue : fValue;
                    }
                    int slop = singleMaxDecimalDigits - kDigits;
                    if ( exp <= singleMaxSmallTen+slop ){
                        fValue *= singleSmall10pow[slop];
                        fValue *= singleSmall10pow[exp-slop];
                        return (isNegative)? -fValue : fValue;
                    }
                } else {
                    if ( exp >= -singleMaxSmallTen ){
                        fValue /= singleSmall10pow[-exp];
                        return (isNegative)? -fValue : fValue;
                    }
                }
            } else if ( (decExponent >= nDigits) && (nDigits+decExponent <= maxDecimalDigits) ){
                long lValue = (long)iValue;
                for ( int i=kDigits; i < nDigits; i++ ){
                    lValue = lValue*10L + (long)((int)digits[i]-(int)'0');
                }
                double dValue = (double)lValue;
                exp = decExponent-nDigits;
                dValue *= small10pow[exp];
                fValue = (float)dValue;
                return (isNegative)? -fValue : fValue;
            }
            if ( decExponent > singleMaxDecimalExponent+1 ){
                return (isNegative)? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            } else if ( decExponent < singleMinDecimalExponent-1 ){
                return (isNegative)? -0.0f : 0.0f;
            }
            mustSetRoundDir = !fromHex;
            double dValue = doubleValue();
            return stickyRound( dValue );
        }
    }
    private static final double small10pow[] = {
        1.0e0,
        1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
        1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
        1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15,
        1.0e16, 1.0e17, 1.0e18, 1.0e19, 1.0e20,
        1.0e21, 1.0e22
    };
    private static final float singleSmall10pow[] = {
        1.0e0f,
        1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
        1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };
    private static final double big10pow[] = {
        1e16, 1e32, 1e64, 1e128, 1e256 };
    private static final double tiny10pow[] = {
        1e-16, 1e-32, 1e-64, 1e-128, 1e-256 };
    private static final int maxSmallTen = small10pow.length-1;
    private static final int singleMaxSmallTen = singleSmall10pow.length-1;
    private static final int small5pow[] = {
        1,
        5,
        5*5,
        5*5*5,
        5*5*5*5,
        5*5*5*5*5,
        5*5*5*5*5*5,
        5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5*5*5
    };
    private static final long long5pow[] = {
        1L,
        5L,
        5L*5,
        5L*5*5,
        5L*5*5*5,
        5L*5*5*5*5,
        5L*5*5*5*5*5,
        5L*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
    };
    private static final int n5bits[] = {
        0,
        3,
        5,
        7,
        10,
        12,
        14,
        17,
        19,
        21,
        24,
        26,
        28,
        31,
        33,
        35,
        38,
        40,
        42,
        45,
        47,
        49,
        52,
        54,
        56,
        59,
        61,
    };
    private static final char infinity[] = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };
    private static final char notANumber[] = { 'N', 'a', 'N' };
    private static final char zero[] = { '0', '0', '0', '0', '0', '0', '0', '0' };
}
