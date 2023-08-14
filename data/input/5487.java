public class FloatingDecimal{
    boolean     isExceptional;
    boolean     isNegative;
    int         decExponent;
    char        digits[];
    int         nDigits;
    int         bigIntExp;
    int         bigIntNBits;
    boolean     mustSetRoundDir = false;
    boolean     fromHex = false;
    int         roundDir = 0; 
    private     FloatingDecimal( boolean negSign, int decExponent, char []digits, int n,  boolean e )
    {
        isNegative = negSign;
        isExceptional = e;
        this.decExponent = decExponent;
        this.digits = digits;
        this.nDigits = n;
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
    public FloatingDecimal( double d )
    {
        long    dBits = Double.doubleToLongBits( d );
        long    fractBits;
        int     binExp;
        int     nSignificantBits;
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
    public FloatingDecimal( float f )
    {
        int     fBits = Float.floatToIntBits( f );
        int     fractBits;
        int     binExp;
        int     nSignificantBits;
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
                if ( decExp < -3 || decExp >= 8 ){
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
                if ( decExp < -3 || decExp >= 8 ){
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
            if ( decExp < -3 || decExp >= 8 ){
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
    public String toJavaFormatString() {
        char result[] = (char[])(perThreadBuffer.get());
        int i = getChars(result);
        return new String(result, 0, i);
    }
    private int getChars(char[] result) {
        assert nDigits <= 19 : nDigits; 
        int i = 0;
        if (isNegative) { result[0] = '-'; i = 1; }
        if (isExceptional) {
            System.arraycopy(digits, 0, result, i, nDigits);
            i += nDigits;
        } else {
            if (decExponent > 0 && decExponent < 8) {
                int charLength = Math.min(nDigits, decExponent);
                System.arraycopy(digits, 0, result, i, charLength);
                i += charLength;
                if (charLength < decExponent) {
                    charLength = decExponent-charLength;
                    System.arraycopy(zero, 0, result, i, charLength);
                    i += charLength;
                    result[i++] = '.';
                    result[i++] = '0';
                } else {
                    result[i++] = '.';
                    if (charLength < nDigits) {
                        int t = nDigits - charLength;
                        System.arraycopy(digits, charLength, result, i, t);
                        i += t;
                    } else {
                        result[i++] = '0';
                    }
                }
            } else if (decExponent <=0 && decExponent > -3) {
                result[i++] = '0';
                result[i++] = '.';
                if (decExponent != 0) {
                    System.arraycopy(zero, 0, result, i, -decExponent);
                    i -= decExponent;
                }
                System.arraycopy(digits, 0, result, i, nDigits);
                i += nDigits;
            } else {
                result[i++] = digits[0];
                result[i++] = '.';
                if (nDigits > 1) {
                    System.arraycopy(digits, 1, result, i, nDigits-1);
                    i += nDigits-1;
                } else {
                    result[i++] = '0';
                }
                result[i++] = 'E';
                int e;
                if (decExponent <= 0) {
                    result[i++] = '-';
                    e = -decExponent+1;
                } else {
                    e = decExponent-1;
                }
                if (e <= 9) {
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
    public void appendTo(Appendable buf) {
          char result[] = (char[])(perThreadBuffer.get());
          int i = getChars(result);
        if (buf instanceof StringBuilder)
            ((StringBuilder) buf).append(result, 0, i);
        else if (buf instanceof StringBuffer)
            ((StringBuffer) buf).append(result, 0, i);
        else
            assert false;
    }
    public static FloatingDecimal
    readJavaFormatString( String in ) throws NumberFormatException {
        boolean isNegative = false;
        boolean signSeen   = false;
        int     decExp;
        char    c;
    parseNumber:
        try{
            in = in.trim(); 
            int l = in.length();
            if ( l == 0 ) throw new NumberFormatException("empty String");
            int i = 0;
            switch ( c = in.charAt( i ) ){
            case '-':
                isNegative = true;
            case '+':
                i++;
                signSeen = true;
            }
            c = in.charAt(i);
            if(c == 'N' || c == 'I') { 
                boolean potentialNaN = false;
                char targetChars[] = null;  
                if(c == 'N') {
                    targetChars = notANumber;
                    potentialNaN = true;
                } else {
                    targetChars = infinity;
                }
                int j = 0;
                while(i < l && j < targetChars.length) {
                    if(in.charAt(i) == targetChars[j]) {
                        i++; j++;
                    }
                    else 
                        break parseNumber;
                }
                if( (j == targetChars.length) && (i == l) ) { 
                    return (potentialNaN ? new FloatingDecimal(Double.NaN) 
                            : new FloatingDecimal(isNegative?
                                                  Double.NEGATIVE_INFINITY:
                                                  Double.POSITIVE_INFINITY)) ;
                }
                else { 
                    break parseNumber;
                }
            } else if (c == '0')  { 
                if (l > i+1 ) {
                    char ch = in.charAt(i+1);
                    if (ch == 'x' || ch == 'X' ) 
                        return parseHexString(in);
                }
            }  
            char[] digits = new char[ l ];
            int    nDigits= 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero= 0;
        digitLoop:
            while ( i < l ){
                switch ( c = in.charAt( i ) ){
                case '0':
                    if ( nDigits > 0 ){
                        nTrailZero += 1;
                    } else {
                        nLeadZero += 1;
                    }
                    break; 
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    while ( nTrailZero > 0 ){
                        digits[nDigits++] = '0';
                        nTrailZero -= 1;
                    }
                    digits[nDigits++] = c;
                    break; 
                case '.':
                    if ( decSeen ){
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i;
                    if ( signSeen ){
                        decPt -= 1;
                    }
                    decSeen = true;
                    break; 
                default:
                    break digitLoop;
                }
                i++;
            }
            if ( nDigits == 0 ){
                digits = zero;
                nDigits = 1;
                if ( nLeadZero == 0 ){
                    break parseNumber; 
                }
            }
            if ( decSeen ){
                decExp = decPt - nLeadZero;
            } else {
                decExp = nDigits+nTrailZero;
            }
            if ( (i < l) &&  (((c = in.charAt(i) )=='e') || (c == 'E') ) ){
                int expSign = 1;
                int expVal  = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch( in.charAt(++i) ){
                case '-':
                    expSign = -1;
                case '+':
                    i++;
                }
                int expAt = i;
            expLoop:
                while ( i < l  ){
                    if ( expVal >= reallyBig ){
                        expOverflow = true;
                    }
                    switch ( c = in.charAt(i++) ){
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        expVal = expVal*10 + ( (int)c - (int)'0' );
                        continue;
                    default:
                        i--;           
                        break expLoop; 
                    }
                }
                int expLimit = bigDecimalExponent+nDigits+nTrailZero;
                if ( expOverflow || ( expVal > expLimit ) ){
                    decExp = expSign*expLimit;
                } else {
                    decExp = decExp + expSign*expVal;
                }
                if ( i == expAt )
                    break parseNumber; 
            }
            if ( i < l &&
                ((i != l - 1) ||
                (in.charAt(i) != 'f' &&
                 in.charAt(i) != 'F' &&
                 in.charAt(i) != 'd' &&
                 in.charAt(i) != 'D'))) {
                break parseNumber; 
            }
            return new FloatingDecimal( isNegative, decExp, digits, nDigits,  false );
        } catch ( StringIndexOutOfBoundsException e ){ }
        throw new NumberFormatException("For input string: \"" + in + "\"");
    }
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
                    if ( (bigIntNBits == 1) && (bigIntExp > -expBias+1) ){
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
    private static Pattern hexFloatPattern = null;
    private static synchronized Pattern getHexFloatPattern() {
        if (hexFloatPattern == null) {
           hexFloatPattern = Pattern.compile(
                    "([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?"
                    );
        }
        return hexFloatPattern;
    }
   static FloatingDecimal parseHexString(String s) {
        Matcher m = getHexFloatPattern().matcher(s);
        boolean validInput = m.matches();
        if (!validInput) {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        } else { 
            String group1 = m.group(1);
            double sign = (( group1 == null ) || group1.equals("+"))? 1.0 : -1.0;
            String significandString =null;
            int signifLength = 0;
            int exponentAdjust = 0;
            {
                int leftDigits  = 0; 
                int rightDigits = 0; 
                String group4;
                if( (group4 = m.group(4)) != null) {  
                    significandString = stripLeadingZeros(group4);
                    leftDigits = significandString.length();
                }
                else {
                    String group6 = stripLeadingZeros(m.group(6));
                    leftDigits = group6.length();
                    String group7 = m.group(7);
                    rightDigits = group7.length();
                    significandString =
                        ((group6 == null)?"":group6) + 
                        group7;
                }
                significandString = stripLeadingZeros(significandString);
                signifLength  = significandString.length();
                if (leftDigits >= 1) {  
                    exponentAdjust = 4*(leftDigits - 1);
                } else {                
                    exponentAdjust = -4*( rightDigits - signifLength + 1);
                }
                if (signifLength == 0) { 
                    return new FloatingDecimal(sign * 0.0);
                }
            }
            String group8 = m.group(8);
            boolean positiveExponent = ( group8 == null ) || group8.equals("+");
            long unsignedRawExponent;
            try {
                unsignedRawExponent = Integer.parseInt(m.group(9));
            }
            catch (NumberFormatException e) {
                return new FloatingDecimal(sign * (positiveExponent ?
                                                   Double.POSITIVE_INFINITY : 0.0));
            }
            long rawExponent =
                (positiveExponent ? 1L : -1L) * 
                unsignedRawExponent;            
            long exponent = rawExponent + exponentAdjust ;
            boolean round = false;
            boolean sticky = false;
            int bitsCopied=0;
            int nextShift=0;
            long significand=0L;
            long leadingDigit = getHexDigit(significandString, 0);
            if (leadingDigit == 1) {
                significand |= leadingDigit << 52;
                nextShift = 52 - 4;
                     }
            else if (leadingDigit <= 3) { 
                significand |= leadingDigit << 51;
                nextShift = 52 - 5;
                exponent += 1;
            }
            else if (leadingDigit <= 7) { 
                significand |= leadingDigit << 50;
                nextShift = 52 - 6;
                exponent += 2;
            }
            else if (leadingDigit <= 15) { 
                significand |= leadingDigit << 49;
                nextShift = 52 - 7;
                exponent += 3;
            } else {
                throw new AssertionError("Result from digit conversion too large!");
            }
            int i = 0;
            for(i = 1;
                i < signifLength && nextShift >= 0;
                i++) {
                long currentDigit = getHexDigit(significandString, i);
                significand |= (currentDigit << nextShift);
                nextShift-=4;
            }
            if ( i < signifLength ) { 
                long currentDigit = getHexDigit(significandString, i);
                switch(nextShift) { 
                case -1:
                    significand |= ((currentDigit & 0xEL) >> 1);
                    round = (currentDigit & 0x1L)  != 0L;
                    break;
                case -2:
                    significand |= ((currentDigit & 0xCL) >> 2);
                    round = (currentDigit &0x2L)  != 0L;
                    sticky = (currentDigit & 0x1L) != 0;
                    break;
                case -3:
                    significand |= ((currentDigit & 0x8L)>>3);
                    round = (currentDigit &0x4L)  != 0L;
                    sticky = (currentDigit & 0x3L) != 0;
                    break;
                case -4:
                    round = ((currentDigit & 0x8L) != 0);  
                    sticky = (currentDigit & 0x7L) != 0;
                    break;
                default:
                    throw new AssertionError("Unexpected shift distance remainder.");
                }
                i++;
                while(i < signifLength && !sticky) {
                    currentDigit =  getHexDigit(significandString,i);
                    sticky = sticky || (currentDigit != 0);
                    i++;
                }
            }
            if (exponent > DoubleConsts.MAX_EXPONENT) {         
                return new FloatingDecimal(sign * Double.POSITIVE_INFINITY);
            } else {  
                if (exponent <= DoubleConsts.MAX_EXPONENT && 
                    exponent >= DoubleConsts.MIN_EXPONENT) {
                    significand = (( ((long)exponent +
                                     (long)DoubleConsts.EXP_BIAS) <<
                                     (DoubleConsts.SIGNIFICAND_WIDTH-1))
                                   & DoubleConsts.EXP_BIT_MASK) |
                        (DoubleConsts.SIGNIF_BIT_MASK & significand);
                }  else  {  
                    if (exponent < (DoubleConsts.MIN_SUB_EXPONENT -1 )) {
                        return new FloatingDecimal(sign * 0.0);
                    } else { 
                        sticky = sticky || round;
                        round = false;
                        int bitsDiscarded = 53 -
                            ((int)exponent - DoubleConsts.MIN_SUB_EXPONENT + 1);
                        assert bitsDiscarded >= 1 && bitsDiscarded <= 53;
                        round = (significand & (1L << (bitsDiscarded -1))) != 0L;
                        if (bitsDiscarded > 1) {
                            long mask = ~((~0L) << (bitsDiscarded -1));
                            sticky = sticky || ((significand & mask) != 0L ) ;
                        }
                        significand = significand >> bitsDiscarded;
                        significand = (( ((long)(DoubleConsts.MIN_EXPONENT -1) + 
                                          (long)DoubleConsts.EXP_BIAS) <<
                                         (DoubleConsts.SIGNIFICAND_WIDTH-1))
                                       & DoubleConsts.EXP_BIT_MASK) |
                            (DoubleConsts.SIGNIF_BIT_MASK & significand);
                    }
                }
                boolean incremented = false;
                boolean leastZero  = ((significand & 1L) == 0L);
                if( (  leastZero  && round && sticky ) ||
                    ((!leastZero) && round )) {
                    incremented = true;
                    significand++;
                }
                FloatingDecimal fd = new FloatingDecimal(FpUtils.rawCopySign(
                                                                 Double.longBitsToDouble(significand),
                                                                 sign));
                if ((exponent >= FloatConsts.MIN_SUB_EXPONENT-1) &&
                    (exponent <= FloatConsts.MAX_EXPONENT ) ){
                    if ((significand & 0xfffffffL) ==  0x0L) {
                        if( round || sticky) {
                            if (leastZero) { 
                                if (round ^ sticky) {
                                    fd.roundDir =  1;
                                }
                            }
                            else { 
                                if (round)
                                    fd.roundDir =  -1;
                            }
                        }
                    }
                }
                fd.fromHex = true;
                return fd;
            }
        }
    }
    static String stripLeadingZeros(String s) {
        return  s.replaceFirst("^0+", "");
    }
    static int getHexDigit(String s, int position) {
        int value = Character.digit(s.charAt(position), 16);
        if (value <= -1 || value >= 16) {
            throw new AssertionError("Unexpected failure of digit conversion of " +
                                     s.charAt(position));
        }
        return value;
    }
}
class FDBigInt {
    int nWords; 
    int data[]; 
    public FDBigInt( int v ){
        nWords = 1;
        data = new int[1];
        data[0] = v;
    }
    public FDBigInt( long v ){
        data = new int[2];
        data[0] = (int)v;
        data[1] = (int)(v>>>32);
        nWords = (data[1]==0) ? 1 : 2;
    }
    public FDBigInt( FDBigInt other ){
        data = new int[nWords = other.nWords];
        System.arraycopy( other.data, 0, data, 0, nWords );
    }
    private FDBigInt( int [] d, int n ){
        data = d;
        nWords = n;
    }
    public FDBigInt( long seed, char digit[], int nd0, int nd ){
        int n= (nd+8)/9;        
        if ( n < 2 ) n = 2;
        data = new int[n];      
        data[0] = (int)seed;    
        data[1] = (int)(seed>>>32);
        nWords = (data[1]==0) ? 1 : 2;
        int i = nd0;
        int limit = nd-5;       
        int v;
        while ( i < limit ){
            int ilim = i+5;
            v = (int)digit[i++]-(int)'0';
            while( i <ilim ){
                v = 10*v + (int)digit[i++]-(int)'0';
            }
            multaddMe( 100000, v); 
        }
        int factor = 1;
        v = 0;
        while ( i < nd ){
            v = 10*v + (int)digit[i++]-(int)'0';
            factor *= 10;
        }
        if ( factor != 1 ){
            multaddMe( factor, v );
        }
    }
    public void
    lshiftMe( int c )throws IllegalArgumentException {
        if ( c <= 0 ){
            if ( c == 0 )
                return; 
            else
                throw new IllegalArgumentException("negative shift count");
        }
        int wordcount = c>>5;
        int bitcount  = c & 0x1f;
        int anticount = 32-bitcount;
        int t[] = data;
        int s[] = data;
        if ( nWords+wordcount+1 > t.length ){
            t = new int[ nWords+wordcount+1 ];
        }
        int target = nWords+wordcount;
        int src    = nWords-1;
        if ( bitcount == 0 ){
            System.arraycopy( s, 0, t, wordcount, nWords );
            target = wordcount-1;
        } else {
            t[target--] = s[src]>>>anticount;
            while ( src >= 1 ){
                t[target--] = (s[src]<<bitcount) | (s[--src]>>>anticount);
            }
            t[target--] = s[src]<<bitcount;
        }
        while( target >= 0 ){
            t[target--] = 0;
        }
        data = t;
        nWords += wordcount + 1;
        while ( nWords > 1 && data[nWords-1] == 0 )
            nWords--;
    }
    public int
    normalizeMe() throws IllegalArgumentException {
        int src;
        int wordcount = 0;
        int bitcount  = 0;
        int v = 0;
        for ( src= nWords-1 ; src >= 0 && (v=data[src]) == 0 ; src--){
            wordcount += 1;
        }
        if ( src < 0 ){
            throw new IllegalArgumentException("zero value");
        }
        nWords -= wordcount;
        if ( (v & 0xf0000000) != 0 ){
            for( bitcount = 32 ; (v & 0xf0000000) != 0 ; bitcount-- )
                v >>>= 1;
        } else {
            while ( v <= 0x000fffff ){
                v <<= 8;
                bitcount += 8;
            }
            while ( v <= 0x07ffffff ){
                v <<= 1;
                bitcount += 1;
            }
        }
        if ( bitcount != 0 )
            lshiftMe( bitcount );
        return bitcount;
    }
    public FDBigInt
    mult( int iv ) {
        long v = iv;
        int r[];
        long p;
        r = new int[ ( v * ((long)data[nWords-1]&0xffffffffL) > 0xfffffffL ) ? nWords+1 : nWords ];
        p = 0L;
        for( int i=0; i < nWords; i++ ) {
            p += v * ((long)data[i]&0xffffffffL);
            r[i] = (int)p;
            p >>>= 32;
        }
        if ( p == 0L){
            return new FDBigInt( r, nWords );
        } else {
            r[nWords] = (int)p;
            return new FDBigInt( r, nWords+1 );
        }
    }
    public void
    multaddMe( int iv, int addend ) {
        long v = iv;
        long p;
        p = v * ((long)data[0]&0xffffffffL) + ((long)addend&0xffffffffL);
        data[0] = (int)p;
        p >>>= 32;
        for( int i=1; i < nWords; i++ ) {
            p += v * ((long)data[i]&0xffffffffL);
            data[i] = (int)p;
            p >>>= 32;
        }
        if ( p != 0L){
            data[nWords] = (int)p; 
            nWords++;
        }
    }
    public FDBigInt
    mult( FDBigInt other ){
        int r[] = new int[ nWords + other.nWords ];
        int i;
        for( i = 0; i < this.nWords; i++ ){
            long v = (long)this.data[i] & 0xffffffffL; 
            long p = 0L;
            int j;
            for( j = 0; j < other.nWords; j++ ){
                p += ((long)r[i+j]&0xffffffffL) + v*((long)other.data[j]&0xffffffffL); 
                r[i+j] = (int)p;
                p >>>= 32;
            }
            r[i+j] = (int)p;
        }
        for ( i = r.length-1; i> 0; i--)
            if ( r[i] != 0 )
                break;
        return new FDBigInt( r, i+1 );
    }
    public FDBigInt
    add( FDBigInt other ){
        int i;
        int a[], b[];
        int n, m;
        long c = 0L;
        if ( this.nWords >= other.nWords ){
            a = this.data;
            n = this.nWords;
            b = other.data;
            m = other.nWords;
        } else {
            a = other.data;
            n = other.nWords;
            b = this.data;
            m = this.nWords;
        }
        int r[] = new int[ n ];
        for ( i = 0; i < n; i++ ){
            c += (long)a[i] & 0xffffffffL;
            if ( i < m ){
                c += (long)b[i] & 0xffffffffL;
            }
            r[i] = (int) c;
            c >>= 32; 
        }
        if ( c != 0L ){
            int s[] = new int[ r.length+1 ];
            System.arraycopy( r, 0, s, 0, r.length );
            s[i++] = (int)c;
            return new FDBigInt( s, i );
        }
        return new FDBigInt( r, i );
    }
    public FDBigInt
    sub( FDBigInt other ){
        int r[] = new int[ this.nWords ];
        int i;
        int n = this.nWords;
        int m = other.nWords;
        int nzeros = 0;
        long c = 0L;
        for ( i = 0; i < n; i++ ){
            c += (long)this.data[i] & 0xffffffffL;
            if ( i < m ){
                c -= (long)other.data[i] & 0xffffffffL;
            }
            if ( ( r[i] = (int) c ) == 0 )
                nzeros++;
            else
                nzeros = 0;
            c >>= 32; 
        }
        assert c == 0L : c; 
        assert dataInRangeIsZero(i, m, other); 
        return new FDBigInt( r, n-nzeros );
    }
    private static boolean dataInRangeIsZero(int i, int m, FDBigInt other) {
        while ( i < m )
            if (other.data[i++] != 0)
                return false;
        return true;
    }
    public int
    cmp( FDBigInt other ){
        int i;
        if ( this.nWords > other.nWords ){
            int j = other.nWords-1;
            for ( i = this.nWords-1; i > j ; i-- )
                if ( this.data[i] != 0 ) return 1;
        }else if ( this.nWords < other.nWords ){
            int j = this.nWords-1;
            for ( i = other.nWords-1; i > j ; i-- )
                if ( other.data[i] != 0 ) return -1;
        } else{
            i = this.nWords-1;
        }
        for ( ; i > 0 ; i-- )
            if ( this.data[i] != other.data[i] )
                break;
        int a = this.data[i];
        int b = other.data[i];
        if ( a < 0 ){
            if ( b < 0 ){
                return a-b; 
            } else {
                return 1; 
            }
        } else {
            if ( b < 0 ) {
                return -1;
            } else {
                return a - b;
            }
        }
    }
    public int
    quoRemIteration( FDBigInt S )throws IllegalArgumentException {
        if ( nWords != S.nWords ){
            throw new IllegalArgumentException("disparate values");
        }
        int n = nWords-1;
        long q = ((long)data[n]&0xffffffffL) / (long)S.data[n];
        long diff = 0L;
        for ( int i = 0; i <= n ; i++ ){
            diff += ((long)data[i]&0xffffffffL) -  q*((long)S.data[i]&0xffffffffL);
            data[i] = (int)diff;
            diff >>= 32; 
        }
        if ( diff != 0L ) {
            long sum = 0L;
            while ( sum ==  0L ){
                sum = 0L;
                for ( int i = 0; i <= n; i++ ){
                    sum += ((long)data[i]&0xffffffffL) +  ((long)S.data[i]&0xffffffffL);
                    data[i] = (int) sum;
                    sum >>= 32; 
                }
                assert sum == 0 || sum == 1 : sum; 
                q -= 1;
            }
        }
        long p = 0L;
        for ( int i = 0; i <= n; i++ ){
            p += 10*((long)data[i]&0xffffffffL);
            data[i] = (int)p;
            p >>= 32; 
        }
        assert p == 0L : p; 
        return (int)q;
    }
    public long
    longValue(){
        assert this.nWords > 0 : this.nWords; 
        if (this.nWords == 1)
            return ((long)data[0]&0xffffffffL);
        assert dataInRangeIsZero(2, this.nWords, this); 
        assert data[1] >= 0;  
        return ((long)(data[1]) << 32) | ((long)data[0]&0xffffffffL);
    }
    public String
    toString() {
        StringBuffer r = new StringBuffer(30);
        r.append('[');
        int i = Math.min( nWords-1, data.length-1) ;
        if ( nWords > data.length ){
            r.append( "("+data.length+"<"+nWords+"!)" );
        }
        for( ; i> 0 ; i-- ){
            r.append( Integer.toHexString( data[i] ) );
            r.append(' ');
        }
        r.append( Integer.toHexString( data[0] ) );
        r.append(']');
        return new String( r );
    }
}
