public final class Punycode {
    private static final int BASE           = 36;
    private static final int TMIN           = 1;
    private static final int TMAX           = 26;
    private static final int SKEW           = 38;
    private static final int DAMP           = 700;
    private static final int INITIAL_BIAS   = 72;
    private static final int INITIAL_N      = 0x80;
    private static final int HYPHEN         = 0x2d;
    private static final int DELIMITER      = HYPHEN;
    private static final int ZERO           = 0x30;
    private static final int NINE           = 0x39;
    private static final int SMALL_A        = 0x61;
    private static final int SMALL_Z        = 0x7a;
    private static final int CAPITAL_A      = 0x41;
    private static final int CAPITAL_Z      = 0x5a;
    private static final int MAX_CP_COUNT   = 256;
    private static final int UINT_MAGIC     = 0x80000000;
    private static final long ULONG_MAGIC   = 0x8000000000000000L;
    private static int adaptBias(int delta, int length, boolean firstTime){
        if(firstTime){
            delta /=DAMP;
        }else{
            delta /=  2;
        }
        delta += delta/length;
        int count=0;
        for(; delta>((BASE-TMIN)*TMAX)/2; count+=BASE) {
            delta/=(BASE-TMIN);
        }
        return count+(((BASE-TMIN+1)*delta)/(delta+SKEW));
    }
    static final int[]    basicToDigit= new int[]{
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1,
        -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
        -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };
    private static char asciiCaseMap(char b, boolean uppercase) {
        if(uppercase) {
            if(SMALL_A<=b && b<=SMALL_Z) {
                b-=(SMALL_A-CAPITAL_A);
            }
        } else {
            if(CAPITAL_A<=b && b<=CAPITAL_Z) {
                b+=(SMALL_A-CAPITAL_A);
            }
        }
        return b;
    }
    private static char digitToBasic(int digit, boolean uppercase) {
        if(digit<26) {
            if(uppercase) {
                return (char)(CAPITAL_A+digit);
            } else {
                return (char)(SMALL_A+digit);
            }
        } else {
            return (char)((ZERO-26)+digit);
        }
    }
    public static StringBuffer encode(StringBuffer src, boolean[] caseFlags) throws ParseException{
        int[] cpBuffer = new int[MAX_CP_COUNT];
        int n, delta, handledCPCount, basicLength, destLength, bias, j, m, q, k, t, srcCPCount;
        char c, c2;
        int srcLength = src.length();
        int destCapacity = MAX_CP_COUNT;
        char[] dest = new char[destCapacity];
        StringBuffer result = new StringBuffer();
        srcCPCount=destLength=0;
        for(j=0; j<srcLength; ++j) {
            if(srcCPCount==MAX_CP_COUNT) {
                throw new IndexOutOfBoundsException();
            }
            c=src.charAt(j);
            if(isBasic(c)) {
                if(destLength<destCapacity) {
                    cpBuffer[srcCPCount++]=0;
                    dest[destLength]=
                        caseFlags!=null ?
                            asciiCaseMap(c, caseFlags[j]) :
                            c;
                }
                ++destLength;
            } else {
                n=((caseFlags!=null && caseFlags[j])? 1 : 0)<<31L;
                if(!UTF16.isSurrogate(c)) {
                    n|=c;
                } else if(UTF16.isLeadSurrogate(c) && (j+1)<srcLength && UTF16.isTrailSurrogate(c2=src.charAt(j+1))) {
                    ++j;
                    n|=UCharacter.getCodePoint(c, c2);
                } else {
                    throw new ParseException("Illegal char found", -1);
                }
                cpBuffer[srcCPCount++]=n;
            }
        }
        basicLength=destLength;
        if(basicLength>0) {
            if(destLength<destCapacity) {
                dest[destLength]=DELIMITER;
            }
            ++destLength;
        }
        n=INITIAL_N;
        delta=0;
        bias=INITIAL_BIAS;
        for(handledCPCount=basicLength; handledCPCount<srcCPCount; ) {
            for(m=0x7fffffff, j=0; j<srcCPCount; ++j) {
                q=cpBuffer[j]&0x7fffffff; 
                if(n<=q && q<m) {
                    m=q;
                }
            }
            if(m-n>(0x7fffffff-MAX_CP_COUNT-delta)/(handledCPCount+1)) {
                throw new RuntimeException("Internal program error");
            }
            delta+=(m-n)*(handledCPCount+1);
            n=m;
            for(j=0; j<srcCPCount; ++j) {
                q=cpBuffer[j]&0x7fffffff; 
                if(q<n) {
                    ++delta;
                } else if(q==n) {
                    for(q=delta, k=BASE; ; k+=BASE) {
                        t=k-bias;
                        if(t<TMIN) {
                            t=TMIN;
                        } else if(k>=(bias+TMAX)) {
                            t=TMAX;
                        }
                        if(q<t) {
                            break;
                        }
                        if(destLength<destCapacity) {
                            dest[destLength++]=digitToBasic(t+(q-t)%(BASE-t), false);
                        }
                        q=(q-t)/(BASE-t);
                    }
                    if(destLength<destCapacity) {
                        dest[destLength++]=digitToBasic(q, (cpBuffer[j]<0));
                    }
                    bias=adaptBias(delta, handledCPCount+1,(handledCPCount==basicLength));
                    delta=0;
                    ++handledCPCount;
                }
            }
            ++delta;
            ++n;
        }
        return result.append(dest, 0, destLength);
    }
    private static boolean isBasic(int ch){
        return (ch < INITIAL_N);
    }
    private static boolean isBasicUpperCase(int ch){
        return( CAPITAL_A <= ch && ch <= CAPITAL_Z);
    }
    private static boolean isSurrogate(int ch){
        return (((ch)&0xfffff800)==0xd800);
    }
    public static StringBuffer decode(StringBuffer src, boolean[] caseFlags)
                               throws ParseException{
        int srcLength = src.length();
        StringBuffer result = new StringBuffer();
        int n, destLength, i, bias, basicLength, j, in, oldi, w, k, digit, t,
                destCPCount, firstSupplementaryIndex, cpLength;
        char b;
        int destCapacity = MAX_CP_COUNT;
        char[] dest = new char[destCapacity];
        for(j=srcLength; j>0;) {
            if(src.charAt(--j)==DELIMITER) {
                break;
            }
        }
        destLength=basicLength=destCPCount=j;
        while(j>0) {
            b=src.charAt(--j);
            if(!isBasic(b)) {
                throw new ParseException("Illegal char found", -1);
            }
            if(j<destCapacity) {
                dest[j]= b;
                if(caseFlags!=null) {
                    caseFlags[j]=isBasicUpperCase(b);
                }
            }
        }
        n=INITIAL_N;
        i=0;
        bias=INITIAL_BIAS;
        firstSupplementaryIndex=1000000000;
        for(in=basicLength>0 ? basicLength+1 : 0; in<srcLength; ) {
            for(oldi=i, w=1, k=BASE; ; k+=BASE) {
                if(in>=srcLength) {
                    throw new ParseException("Illegal char found", -1);
                }
                digit=basicToDigit[(byte)src.charAt(in++)];
                if(digit<0) {
                    throw new ParseException("Invalid char found", -1);
                }
                if(digit>(0x7fffffff-i)/w) {
                    throw new ParseException("Illegal char found", -1);
                }
                i+=digit*w;
                t=k-bias;
                if(t<TMIN) {
                    t=TMIN;
                } else if(k>=(bias+TMAX)) {
                    t=TMAX;
                }
                if(digit<t) {
                    break;
                }
                if(w>0x7fffffff/(BASE-t)) {
                    throw new ParseException("Illegal char found", -1);
                }
                w*=BASE-t;
            }
            ++destCPCount;
            bias=adaptBias(i-oldi, destCPCount, (oldi==0));
            if(i/destCPCount>(0x7fffffff-n)) {
                throw new ParseException("Illegal char found", -1);
            }
            n+=i/destCPCount;
            i%=destCPCount;
            if(n>0x10ffff || isSurrogate(n)) {
                throw new ParseException("Illegal char found", -1);
            }
            cpLength=UTF16.getCharCount(n);
            if((destLength+cpLength)<destCapacity) {
                int codeUnitIndex;
                if(i<=firstSupplementaryIndex) {
                    codeUnitIndex=i;
                    if(cpLength>1) {
                        firstSupplementaryIndex=codeUnitIndex;
                    } else {
                        ++firstSupplementaryIndex;
                    }
                } else {
                    codeUnitIndex=firstSupplementaryIndex;
                    codeUnitIndex=UTF16.moveCodePointOffset(dest, 0, destLength, codeUnitIndex, i-codeUnitIndex);
                }
                if(codeUnitIndex<destLength) {
                    System.arraycopy(dest, codeUnitIndex,
                                     dest, codeUnitIndex+cpLength,
                                    (destLength-codeUnitIndex));
                    if(caseFlags!=null) {
                        System.arraycopy(caseFlags, codeUnitIndex,
                                         caseFlags, codeUnitIndex+cpLength,
                                         destLength-codeUnitIndex);
                    }
                }
                if(cpLength==1) {
                    dest[codeUnitIndex]=(char)n;
                } else {
                    dest[codeUnitIndex]=UTF16.getLeadSurrogate(n);
                    dest[codeUnitIndex+1]=UTF16.getTrailSurrogate(n);
                }
                if(caseFlags!=null) {
                    caseFlags[codeUnitIndex]=isBasicUpperCase(src.charAt(in-1));
                    if(cpLength==2) {
                        caseFlags[codeUnitIndex+1]=false;
                    }
                }
            }
            destLength+=cpLength;
            ++i;
        }
        result.append(dest, 0, destLength);
        return result;
    }
}
