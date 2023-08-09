class Coding implements Comparable, CodingMethod, Histogram.BitMetric {
    private static int saturate32(long x) {
        if (x > Integer.MAX_VALUE)   return Integer.MAX_VALUE;
        if (x < Integer.MIN_VALUE)   return Integer.MIN_VALUE;
        return (int)x;
    }
    private static long codeRangeLong(int B, int H) {
        return codeRangeLong(B, H, B);
    }
    private static long codeRangeLong(int B, int H, int nMax) {
        assert(nMax >= 0 && nMax <= B);
        assert(B >= 1 && B <= 5);
        assert(H >= 1 && H <= 256);
        if (nMax == 0)  return 0;  
        if (B == 1)     return H;  
        int L = 256-H;
        long sum = 0;
        long H_i = 1;
        for (int n = 1; n <= nMax; n++) {
            sum += H_i;
            H_i *= H;
        }
        sum *= L;
        if (nMax == B)
            sum += H_i;
        return sum;
    }
    public static int codeMax(int B, int H, int S, int nMax) {
        long range = codeRangeLong(B, H, nMax);
        if (range == 0)
            return -1;  
        if (S == 0 || range >= (long)1<<32)
            return saturate32(range-1);
        long maxPos = range-1;
        while (isNegativeCode(maxPos, S)) {
            --maxPos;
        }
        if (maxPos < 0)  return -1;  
        int smax = decodeSign32(maxPos, S);
        if (smax < 0)
            return Integer.MAX_VALUE;
        return smax;
    }
    public static int codeMin(int B, int H, int S, int nMax) {
        long range = codeRangeLong(B, H, nMax);
        if (range >= (long)1<<32 && nMax == B) {
            return Integer.MIN_VALUE;
        }
        if (S == 0) {
            return 0;
        }
        long maxNeg = range-1;
        while (!isNegativeCode(maxNeg, S))
            --maxNeg;
        if (maxNeg < 0)  return 0;  
        return decodeSign32(maxNeg, S);
    }
    private static long toUnsigned32(int sx) {
        return ((long)sx << 32) >>> 32;
    }
    private static boolean isNegativeCode(long ux, int S) {
        assert(S > 0);
        assert(ux >= -1);  
        int Smask = (1<<S)-1;
        return (((int)ux+1) & Smask) == 0;
    }
    private static boolean hasNegativeCode(int sx, int S) {
        assert(S > 0);
        return (0 > sx) && (sx >= ~(-1>>>S));
    }
    private static int decodeSign32(long ux, int S) {
        assert(ux == toUnsigned32((int)ux))  
            : (Long.toHexString(ux));
        if (S == 0) {
            return (int) ux;  
        }
        int sx;
        if (isNegativeCode(ux, S)) {
            sx = ~((int)ux >>> S);
        } else {
            sx = (int)ux - ((int)ux >>> S);
        }
        assert(!(S == 1) || sx == (((int)ux >>> 1) ^ -((int)ux & 1)));
        return sx;
    }
    private static long encodeSign32(int sx, int S) {
        if (S == 0) {
            return toUnsigned32(sx);  
        }
        int Smask = (1<<S)-1;
        long ux;
        if (!hasNegativeCode(sx, S)) {
            ux = sx + (toUnsigned32(sx) / Smask);
        } else {
            ux = (-sx << S) - 1;
        }
        ux = toUnsigned32((int)ux);
        assert(sx == decodeSign32(ux, S))
            : (Long.toHexString(ux)+" -> "+
               Integer.toHexString(sx)+" != "+
               Integer.toHexString(decodeSign32(ux, S)));
        return ux;
    }
    public static void writeInt(byte[] out, int[] outpos, int sx, int B, int H, int S) {
        long ux = encodeSign32(sx, S);
        assert(ux == toUnsigned32((int)ux));
        assert(ux < codeRangeLong(B, H))
            : Long.toHexString(ux);
        int L = 256-H;
        long sum = ux;
        int pos = outpos[0];
        for (int i = 0; i < B-1; i++) {
            if (sum < L)
                break;
            sum -= L;
            int b_i = (int)( L + (sum % H) );
            sum /= H;
            out[pos++] = (byte)b_i;
        }
        out[pos++] = (byte)sum;
        outpos[0] = pos;
    }
    public static int readInt(byte[] in, int[] inpos, int B, int H, int S) {
        int L = 256-H;
        long sum = 0;
        long H_i = 1;
        int pos = inpos[0];
        for (int i = 0; i < B; i++) {
            int b_i = in[pos++] & 0xFF;
            sum += b_i*H_i;
            H_i *= H;
            if (b_i < L)  break;
        }
        inpos[0] = pos;
        return decodeSign32(sum, S);
    }
    public static int readIntFrom(InputStream in, int B, int H, int S) throws IOException {
        int L = 256-H;
        long sum = 0;
        long H_i = 1;
        for (int i = 0; i < B; i++) {
            int b_i = in.read();
            if (b_i < 0)  throw new RuntimeException("unexpected EOF");
            sum += b_i*H_i;
            H_i *= H;
            if (b_i < L)  break;
        }
        assert(sum >= 0 && sum < codeRangeLong(B, H));
        return decodeSign32(sum, S);
    }
    public static final int B_MAX = 5;    
    public static final int H_MAX = 256;  
    public static final int S_MAX = 2;    
    private final int B;        
    private final int H;      
    private final int L;      
    private final int S;        
    private final int del;      
    private final int min;              
    private final int max;              
    private final int umin;             
    private final int umax;             
    private final int[] byteMin;        
    private final int[] byteMax;        
    private Coding(int B, int H, int S) {
        this(B, H, S, 0);
    }
    private Coding(int B, int H, int S, int del) {
        this.B = B;
        this.H = H;
        this.L = 256-H;
        this.S = S;
        this.del = del;
        this.min = codeMin(B, H, S, B);
        this.max = codeMax(B, H, S, B);
        this.umin = codeMin(B, H, 0, B);
        this.umax = codeMax(B, H, 0, B);
        this.byteMin = new int[B];
        this.byteMax = new int[B];
        for (int nMax = 1; nMax <= B; nMax++) {
            byteMin[nMax-1] = codeMin(B, H, S, nMax);
            byteMax[nMax-1] = codeMax(B, H, S, nMax);
        }
    }
    public boolean equals(Object x) {
        if (!(x instanceof Coding))  return false;
        Coding that = (Coding) x;
        if (this.B != that.B)  return false;
        if (this.H != that.H)  return false;
        if (this.S != that.S)  return false;
        if (this.del != that.del)  return false;
        return true;
    }
    public int hashCode() {
        return (del<<14)+(S<<11)+(B<<8)+(H<<0);
    }
    private static Map<Coding, Coding> codeMap;
    private static synchronized Coding of(int B, int H, int S, int del) {
        if (codeMap == null)  codeMap = new HashMap<>();
        Coding x0 = new Coding(B, H, S, del);
        Coding x1 = codeMap.get(x0);
        if (x1 == null)  codeMap.put(x0, x1 = x0);
        return x1;
    }
    public static Coding of(int B, int H) {
        return of(B, H, 0, 0);
    }
    public static Coding of(int B, int H, int S) {
        return of(B, H, S, 0);
    }
    public boolean canRepresentValue(int x) {
        if (isSubrange())
            return canRepresentUnsigned(x);
        else
            return canRepresentSigned(x);
    }
    public boolean canRepresentSigned(int x) {
        return (x >= min && x <= max);
    }
    public boolean canRepresentUnsigned(int x) {
        return (x >= umin && x <= umax);
    }
    public int readFrom(byte[] in, int[] inpos) {
        return readInt(in, inpos, B, H, S);
    }
    public void writeTo(byte[] out, int[] outpos, int x) {
        writeInt(out, outpos, x, B, H, S);
    }
    public int readFrom(InputStream in) throws IOException {
        return readIntFrom(in, B, H, S);
    }
    public void writeTo(OutputStream out, int x) throws IOException {
        byte[] buf = new byte[B];
        int[] pos = new int[1];
        writeInt(buf, pos, x, B, H, S);
        out.write(buf, 0, pos[0]);
    }
    public void readArrayFrom(InputStream in, int[] a, int start, int end) throws IOException {
        for (int i = start; i < end; i++)
            a[i] = readFrom(in);
        for (int dstep = 0; dstep < del; dstep++) {
            long state = 0;
            for (int i = start; i < end; i++) {
                state += a[i];
                if (isSubrange()) {
                    state = reduceToUnsignedRange(state);
                }
                a[i] = (int) state;
            }
        }
    }
    public void writeArrayTo(OutputStream out, int[] a, int start, int end) throws IOException {
        if (end <= start)  return;
        for (int dstep = 0; dstep < del; dstep++) {
            int[] deltas;
            if (!isSubrange())
                deltas = makeDeltas(a, start, end, 0, 0);
            else
                deltas = makeDeltas(a, start, end, min, max);
            a = deltas;
            start = 0;
            end = deltas.length;
        }
        byte[] buf = new byte[1<<8];
        final int bufmax = buf.length-B;
        int[] pos = { 0 };
        for (int i = start; i < end; ) {
            while (pos[0] <= bufmax) {
                writeTo(buf, pos, a[i++]);
                if (i >= end)  break;
            }
            out.write(buf, 0, pos[0]);
            pos[0] = 0;
        }
    }
    boolean isSubrange() {
        return max < Integer.MAX_VALUE
            && ((long)max - (long)min + 1) <= Integer.MAX_VALUE;
    }
    boolean isFullRange() {
        return max == Integer.MAX_VALUE && min == Integer.MIN_VALUE;
    }
    int getRange() {
        assert(isSubrange());
        return (max - min) + 1;  
    }
    Coding setB(int B) { return Coding.of(B, H, S, del); }
    Coding setH(int H) { return Coding.of(B, H, S, del); }
    Coding setS(int S) { return Coding.of(B, H, S, del); }
    Coding setL(int L) { return setH(256-L); }
    Coding setD(int del) { return Coding.of(B, H, S, del); }
    Coding getDeltaCoding() { return setD(del+1); }
    Coding getValueCoding() {
        if (isDelta())
            return Coding.of(B, H, 0, del-1);
        else
            return this;
    }
    int reduceToUnsignedRange(long value) {
        if (value == (int)value && canRepresentUnsigned((int)value))
            return (int)value;
        int range = getRange();
        assert(range > 0);
        value %= range;
        if (value < 0)  value += range;
        assert(canRepresentUnsigned((int)value));
        return (int)value;
    }
    int reduceToSignedRange(int value) {
        if (canRepresentSigned(value))
            return value;
        return reduceToSignedRange(value, min, max);
    }
    static int reduceToSignedRange(int value, int min, int max) {
        int range = (max-min+1);
        assert(range > 0);
        int value0 = value;
        value -= min;
        if (value < 0 && value0 >= 0) {
            value -= range;
            assert(value >= 0);
        }
        value %= range;
        if (value < 0)  value += range;
        value += min;
        assert(min <= value && value <= max);
        return value;
    }
    boolean isSigned() {
        return min < 0;
    }
    boolean isDelta() {
        return del != 0;
    }
    public int B() { return B; }
    public int H() { return H; }
    public int L() { return L; }
    public int S() { return S; }
    public int del() { return del; }
    public int min() { return min; }
    public int max() { return max; }
    public int umin() { return umin; }
    public int umax() { return umax; }
    public int byteMin(int b) { return byteMin[b-1]; }
    public int byteMax(int b) { return byteMax[b-1]; }
    public int compareTo(Object x) {
        Coding that = (Coding) x;
        int dkey = this.del - that.del;
        if (dkey == 0)
            dkey = this.B - that.B;
        if (dkey == 0)
            dkey = this.H - that.H;
        if (dkey == 0)
            dkey = this.S - that.S;
        return dkey;
    }
    public int distanceFrom(Coding that) {
        int diffdel = this.del - that.del;
        if (diffdel < 0)  diffdel = -diffdel;
        int diffS = this.S - that.S;
        if (diffS < 0)  diffS = -diffS;
        int diffB = this.B - that.B;
        if (diffB < 0)  diffB = -diffB;
        int diffHL;
        if (this.H == that.H) {
            diffHL = 0;
        } else {
            int thisHL = this.getHL();
            int thatHL = that.getHL();
            thisHL *= thisHL;
            thatHL *= thatHL;
            if (thisHL > thatHL)
                diffHL = ceil_lg2(1+(thisHL-1)/thatHL);
            else
                diffHL = ceil_lg2(1+(thatHL-1)/thisHL);
        }
        int norm = 5*(diffdel + diffS + diffB) + diffHL;
        assert(norm != 0 || this.compareTo(that) == 0);
        return norm;
    }
    private int getHL() {
        if (H <= 128)  return H;
        if (L >= 1)    return 128*128/L;
        return 128*256;
    }
    static int ceil_lg2(int x) {
        assert(x-1 >= 0);  
        x -= 1;
        int lg = 0;
        while (x != 0) {
            lg++;
            x >>= 1;
        }
        return lg;
    }
    static private final byte[] byteBitWidths = new byte[0x100];
    static {
        for (int b = 0; b < byteBitWidths.length; b++) {
            byteBitWidths[b] = (byte) ceil_lg2(b + 1);
        }
        for (int i = 10; i >= 0; i = (i << 1) - (i >> 3)) {
            assert(bitWidth(i) == ceil_lg2(i + 1));
        }
    }
    static int bitWidth(int i) {
        if (i < 0)  i = ~i;  
        int w = 0;
        int lo = i;
        if (lo < byteBitWidths.length)
            return byteBitWidths[lo];
        int hi;
        hi = (lo >>> 16);
        if (hi != 0) {
            lo = hi;
            w += 16;
        }
        hi = (lo >>> 8);
        if (hi != 0) {
            lo = hi;
            w += 8;
        }
        w += byteBitWidths[lo];
        return w;
    }
    static int[] makeDeltas(int[] values, int start, int end,
                            int min, int max) {
        assert(max >= min);
        int count = end-start;
        int[] deltas = new int[count];
        int state = 0;
        if (min == max) {
            for (int i = 0; i < count; i++) {
                int value = values[start+i];
                deltas[i] = value - state;
                state = value;
            }
        } else {
            for (int i = 0; i < count; i++) {
                int value = values[start+i];
                assert(value >= 0 && value+min <= max);
                int delta = value - state;
                assert(delta == (long)value - (long)state); 
                state = value;
                delta = reduceToSignedRange(delta, min, max);
                deltas[i] = delta;
            }
        }
        return deltas;
    }
    boolean canRepresent(int minValue, int maxValue) {
        assert(minValue <= maxValue);
        if (del > 0) {
            if (isSubrange()) {
                return canRepresentUnsigned(maxValue)
                    && canRepresentUnsigned(minValue);
            } else {
                return isFullRange();
            }
        }
        else
            return canRepresentSigned(maxValue)
                && canRepresentSigned(minValue);
    }
    boolean canRepresent(int[] values, int start, int end) {
        int len = end-start;
        if (len == 0)       return true;
        if (isFullRange())  return true;
        int lmax = values[start];
        int lmin = lmax;
        for (int i = 1; i < len; i++) {
            int value = values[start+i];
            if (lmax < value)  lmax = value;
            if (lmin > value)  lmin = value;
        }
        return canRepresent(lmin, lmax);
    }
    public double getBitLength(int value) {  
        return (double) getLength(value) * 8;
    }
    public int getLength(int value) {
        if (isDelta() && isSubrange()) {
            if (!canRepresentUnsigned(value))
                return Integer.MAX_VALUE;
            value = reduceToSignedRange(value);
        }
        if (value >= 0) {
            for (int n = 0; n < B; n++) {
                if (value <= byteMax[n])  return n+1;
            }
        } else {
            for (int n = 0; n < B; n++) {
                if (value >= byteMin[n])  return n+1;
            }
        }
        return Integer.MAX_VALUE;
    }
    public int getLength(int[] values, int start, int end) {
        int len = end-start;
        if (B == 1)  return len;
        if (L == 0)  return len * B;
        if (isDelta()) {
            int[] deltas;
            if (!isSubrange())
                deltas = makeDeltas(values, start, end, 0, 0);
            else
                deltas = makeDeltas(values, start, end, min, max);
            values = deltas;
            start = 0;
        }
        int sum = len;  
        for (int n = 1; n <= B; n++) {
            int lmax = byteMax[n-1];
            int lmin = byteMin[n-1];
            int longer = 0;  
            for (int i = 0; i < len; i++) {
                int value = values[start+i];
                if (value >= 0) {
                    if (value > lmax)  longer++;
                } else {
                    if (value < lmin)  longer++;
                }
            }
            if (longer == 0)  break;  
            if (n == B)  return Integer.MAX_VALUE;  
            sum += longer;
        }
        return sum;
    }
    public byte[] getMetaCoding(Coding dflt) {
        if (dflt == this)  return new byte[]{ (byte) _meta_default };
        int canonicalIndex = BandStructure.indexOf(this);
        if (canonicalIndex > 0)
            return new byte[]{ (byte) canonicalIndex };
        return new byte[]{
            (byte)_meta_arb,
            (byte)(del + 2*S + 8*(B-1)),
            (byte)(H-1)
        };
    }
    public static int parseMetaCoding(byte[] bytes, int pos, Coding dflt, CodingMethod res[]) {
        int op = bytes[pos++] & 0xFF;
        if (_meta_canon_min <= op && op <= _meta_canon_max) {
            Coding c = BandStructure.codingForIndex(op);
            assert(c != null);
            res[0] = c;
            return pos;
        }
        if (op == _meta_arb) {
            int dsb = bytes[pos++] & 0xFF;
            int H_1 = bytes[pos++] & 0xFF;
            int del = dsb % 2;
            int S = (dsb / 2) % 4;
            int B = (dsb / 8)+1;
            int H = H_1+1;
            if (!((1 <= B && B <= B_MAX) &&
                  (0 <= S && S <= S_MAX) &&
                  (1 <= H && H <= H_MAX) &&
                  (0 <= del && del <= 1))
                || (B == 1 && H != 256)
                || (B == 5 && H == 256)) {
                throw new RuntimeException("Bad arb. coding: ("+B+","+H+","+S+","+del);
            }
            res[0] = Coding.of(B, H, S, del);
            return pos;
        }
        return pos-1;  
    }
    public String keyString() {
        return "("+B+","+H+","+S+","+del+")";
    }
    public String toString() {
        String str = "Coding"+keyString();
        return str;
    }
    static boolean verboseStringForDebug = false;
    String stringForDebug() {
        String minS = (min == Integer.MIN_VALUE ? "min" : ""+min);
        String maxS = (max == Integer.MAX_VALUE ? "max" : ""+max);
        String str = keyString()+" L="+L+" r=["+minS+","+maxS+"]";
        if (isSubrange())
            str += " subrange";
        else if (!isFullRange())
            str += " MIDRANGE";
        if (verboseStringForDebug) {
            str += " {";
            int prev_range = 0;
            for (int n = 1; n <= B; n++) {
                int range_n = saturate32((long)byteMax[n-1] - byteMin[n-1] + 1);
                assert(range_n == saturate32(codeRangeLong(B, H, n)));
                range_n -= prev_range;
                prev_range = range_n;
                String rngS = (range_n == Integer.MAX_VALUE ? "max" : ""+range_n);
                str += " #"+n+"="+rngS;
            }
            str += " }";
        }
        return str;
    }
}
