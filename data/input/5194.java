public class LZWStringTable {
    private final static int RES_CODES = 2;
    private final static short HASH_FREE = (short)0xFFFF;
    private final static short NEXT_FIRST = (short)0xFFFF;
    private final static int MAXBITS = 12;
    private final static int MAXSTR = (1 << MAXBITS);
    private final static short HASHSIZE = 9973;
    private final static short HASHSTEP = 2039;
    byte[]  strChr;  
    short[] strNxt;  
    short[] strHsh;  
    short numStrings;  
    int[] strLen;
    public LZWStringTable() {
        strChr = new byte[MAXSTR];
        strNxt = new short[MAXSTR];
        strLen = new int[MAXSTR];
        strHsh = new short[HASHSIZE];
    }
    public int addCharString(short index, byte b) {
        int hshidx;
        if (numStrings >= MAXSTR) { 
            return 0xFFFF;
        }
        hshidx = hash(index, b);
        while (strHsh[hshidx] != HASH_FREE) {
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;
        }
        strHsh[hshidx] = numStrings;
        strChr[numStrings] = b;
        if (index == HASH_FREE) {
            strNxt[numStrings] = NEXT_FIRST;
            strLen[numStrings] = 1;
        } else {
            strNxt[numStrings] = index;
            strLen[numStrings] = strLen[index] + 1;
        }
        return numStrings++; 
    }
    public short findCharString(short index, byte b) {
        int hshidx, nxtidx;
        if (index == HASH_FREE) {
            return (short)(b & 0xFF);    
        }
        hshidx = hash(index, b);
        while ((nxtidx = strHsh[hshidx]) != HASH_FREE) { 
            if (strNxt[nxtidx] == index && strChr[nxtidx] == b) {
                return (short)nxtidx;
            }
            hshidx = (hshidx + HASHSTEP) % HASHSIZE;
        }
        return (short)0xFFFF;
    }
    public void clearTable(int codesize) {
        numStrings = 0;
        for (int q = 0; q < HASHSIZE; q++) {
            strHsh[q] = HASH_FREE;
        }
        int w = (1 << codesize) + RES_CODES;
        for (int q = 0; q < w; q++) {
            addCharString((short)0xFFFF, (byte)q); 
        }
    }
    static public int hash(short index, byte lastbyte) {
        return ((int)((short)(lastbyte << 8) ^ index) & 0xFFFF) % HASHSIZE;
    }
    public int expandCode(byte[] buf, int offset, short code, int skipHead) {
        if (offset == -2) {
            if (skipHead == 1) {
                skipHead = 0;
            }
        }
        if (code == (short)0xFFFF ||    
            skipHead == strLen[code])  
        {
            return 0;
        }
        int expandLen;  
        int codeLen = strLen[code] - skipHead; 
        int bufSpace = buf.length - offset;  
        if (bufSpace > codeLen) {
            expandLen = codeLen; 
        } else {
            expandLen = bufSpace;
        }
        int skipTail = codeLen - expandLen;  
        int idx = offset + expandLen;   
        while ((idx > offset) && (code != (short)0xFFFF)) {
            if (--skipTail < 0) { 
                buf[--idx] = strChr[code];
            }
            code = strNxt[code];    
        }
        if (codeLen > expandLen) {
            return -expandLen; 
        } else {
            return expandLen;     
        }
    }
    public void dump(PrintStream out) {
        int i;
        for (i = 258; i < numStrings; ++i) {
            out.println(" strNxt[" + i + "] = " + strNxt[i]
                        + " strChr " + Integer.toHexString(strChr[i] & 0xFF)
                        + " strLen " + Integer.toHexString(strLen[i]));
        }
    }
}
