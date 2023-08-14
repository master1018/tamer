public class BidiBase {
    class Point {
        int pos;    
        int flag;   
    }
    class InsertPoints {
        int size;
        int confirmed;
        Point[] points = new Point[0];
    }
    public static final byte INTERNAL_LEVEL_DEFAULT_LTR = (byte)0x7e;
    public static final byte INTERNAL_LEVEL_DEFAULT_RTL = (byte)0x7f;
    public static final byte MAX_EXPLICIT_LEVEL = 61;
    public static final byte INTERNAL_LEVEL_OVERRIDE = (byte)0x80;
    public static final int MAP_NOWHERE = -1;
    public static final byte MIXED = 2;
    public static final short DO_MIRRORING = 2;
    private static final short REORDER_DEFAULT = 0;
    private static final short REORDER_NUMBERS_SPECIAL = 1;
    private static final short REORDER_GROUP_NUMBERS_WITH_R = 2;
    private static final short REORDER_RUNS_ONLY = 3;
    private static final short REORDER_INVERSE_NUMBERS_AS_L = 4;
    private static final short REORDER_INVERSE_LIKE_DIRECT = 5;
    private static final short REORDER_INVERSE_FOR_NUMBERS_SPECIAL = 6;
    private static final short REORDER_LAST_LOGICAL_TO_VISUAL =
            REORDER_NUMBERS_SPECIAL;
    private static final int OPTION_INSERT_MARKS = 1;
    private static final int OPTION_REMOVE_CONTROLS = 2;
    private static final int OPTION_STREAMING = 4;
    private static final byte L   = 0;
    private static final byte R   = 1;
    private static final byte EN  = 2;
    private static final byte ES  = 3;
    private static final byte ET  = 4;
    private static final byte AN  = 5;
    private static final byte CS  = 6;
    static final byte B   = 7;
    private static final byte S   = 8;
    private static final byte WS  = 9;
    private static final byte ON  = 10;
    private static final byte LRE = 11;
    private static final byte LRO = 12;
    private static final byte AL  = 13;
    private static final byte RLE = 14;
    private static final byte RLO = 15;
    private static final byte PDF = 16;
    private static final byte NSM = 17;
    private static final byte BN  = 18;
    private static final int MASK_R_AL = (1 << R | 1 << AL);
    private static final char CR = '\r';
    private static final char LF = '\n';
    static final int LRM_BEFORE = 1;
    static final int LRM_AFTER = 2;
    static final int RLM_BEFORE = 4;
    static final int RLM_AFTER = 8;
    BidiBase                paraBidi;
    final UBiDiProps    bdp;
    char[]              text;
    int                 originalLength;
    public int                 length;
    int                 resultLength;
    boolean             mayAllocateText;
    boolean             mayAllocateRuns;
    byte[]              dirPropsMemory = new byte[1];
    byte[]              levelsMemory = new byte[1];
    byte[]              dirProps;
    byte[]              levels;
    boolean             orderParagraphsLTR;
    byte                paraLevel;
    byte                defaultParaLevel;
    ImpTabPair          impTabPair;  
    byte                direction;
    int                 flags;
    int                 lastArabicPos;
    int                 trailingWSStart;
    int                 paraCount;       
    int[]               parasMemory = new int[1];
    int[]               paras;           
    int[]               simpleParas = {0};
    int                 runCount;     
    BidiRun[]           runsMemory = new BidiRun[0];
    BidiRun[]           runs;
    BidiRun[]           simpleRuns = {new BidiRun()};
    int[]               logicalToVisualRunsMap;
    boolean             isGoodLogicalToVisualRunsMap;
    InsertPoints        insertPoints = new InsertPoints();
    int                 controlCount;
    static int DirPropFlag(byte dir) {
        return (1 << dir);
    }
    static final byte CONTEXT_RTL_SHIFT = 6;
    static final byte CONTEXT_RTL = (byte)(1<<CONTEXT_RTL_SHIFT);   
    static byte NoContextRTL(byte dir)
    {
        return (byte)(dir & ~CONTEXT_RTL);
    }
    static int DirPropFlagNC(byte dir) {
        return (1<<(dir & ~CONTEXT_RTL));
    }
    static final int DirPropFlagMultiRuns = DirPropFlag((byte)31);
    static final int DirPropFlagLR[] = { DirPropFlag(L), DirPropFlag(R) };
    static final int DirPropFlagE[] = { DirPropFlag(LRE), DirPropFlag(RLE) };
    static final int DirPropFlagO[] = { DirPropFlag(LRO), DirPropFlag(RLO) };
    static final int DirPropFlagLR(byte level) { return DirPropFlagLR[level & 1]; }
    static final int DirPropFlagE(byte level)  { return DirPropFlagE[level & 1]; }
    static final int DirPropFlagO(byte level)  { return DirPropFlagO[level & 1]; }
    static final int MASK_LTR =
        DirPropFlag(L)|DirPropFlag(EN)|DirPropFlag(AN)|DirPropFlag(LRE)|DirPropFlag(LRO);
    static final int MASK_RTL = DirPropFlag(R)|DirPropFlag(AL)|DirPropFlag(RLE)|DirPropFlag(RLO);
    private static final int MASK_LRX = DirPropFlag(LRE)|DirPropFlag(LRO);
    private static final int MASK_RLX = DirPropFlag(RLE)|DirPropFlag(RLO);
    private static final int MASK_EXPLICIT = MASK_LRX|MASK_RLX|DirPropFlag(PDF);
    private static final int MASK_BN_EXPLICIT = DirPropFlag(BN)|MASK_EXPLICIT;
    private static final int MASK_B_S = DirPropFlag(B)|DirPropFlag(S);
    static final int MASK_WS = MASK_B_S|DirPropFlag(WS)|MASK_BN_EXPLICIT;
    private static final int MASK_N = DirPropFlag(ON)|MASK_WS;
    private static final int MASK_POSSIBLE_N = DirPropFlag(CS)|DirPropFlag(ES)|DirPropFlag(ET)|MASK_N;
    static final int MASK_EMBEDDING = DirPropFlag(NSM)|MASK_POSSIBLE_N;
    private static byte GetLRFromLevel(byte level)
    {
        return (byte)(level & 1);
    }
    private static boolean IsDefaultLevel(byte level)
    {
        return ((level & INTERNAL_LEVEL_DEFAULT_LTR) == INTERNAL_LEVEL_DEFAULT_LTR);
    }
    byte GetParaLevelAt(int index)
    {
        return (defaultParaLevel != 0) ?
                (byte)(dirProps[index]>>CONTEXT_RTL_SHIFT) : paraLevel;
    }
    static boolean IsBidiControlChar(int c)
    {
        return (((c & 0xfffffffc) == 0x200c) || ((c >= 0x202a) && (c <= 0x202e)));
    }
    public void verifyValidPara()
    {
        if (this != this.paraBidi) {
            throw new IllegalStateException("");
        }
    }
    public void verifyValidParaOrLine()
    {
        BidiBase para = this.paraBidi;
        if (this == para) {
            return;
        }
        if ((para == null) || (para != para.paraBidi)) {
            throw new IllegalStateException();
        }
    }
    public void verifyRange(int index, int start, int limit)
    {
        if (index < start || index >= limit) {
            throw new IllegalArgumentException("Value " + index +
                      " is out of range " + start + " to " + limit);
        }
    }
    public void verifyIndex(int index, int start, int limit)
    {
        if (index < start || index >= limit) {
            throw new ArrayIndexOutOfBoundsException("Index " + index +
                      " is out of range " + start + " to " + limit);
        }
    }
    public BidiBase(int maxLength, int maxRunCount)
     {
        if (maxLength < 0 || maxRunCount < 0) {
            throw new IllegalArgumentException();
        }
        try {
            bdp = UBiDiProps.getSingleton();
        }
        catch (IOException e) {
            throw new MissingResourceException(e.getMessage(), "(BidiProps)", "");
        }
        if (maxLength > 0) {
            getInitialDirPropsMemory(maxLength);
            getInitialLevelsMemory(maxLength);
        } else {
            mayAllocateText = true;
        }
        if (maxRunCount > 0) {
            if (maxRunCount > 1) {
                getInitialRunsMemory(maxRunCount);
            }
        } else {
            mayAllocateRuns = true;
        }
    }
    private Object getMemory(String label, Object array, Class arrayClass,
            boolean mayAllocate, int sizeNeeded)
    {
        int len = Array.getLength(array);
        if (sizeNeeded == len) {
            return array;
        }
        if (!mayAllocate) {
            if (sizeNeeded <= len) {
                return array;
            }
            throw new OutOfMemoryError("Failed to allocate memory for "
                                       + label);
        }
        try {
            return Array.newInstance(arrayClass, sizeNeeded);
        } catch (Exception e) {
            throw new OutOfMemoryError("Failed to allocate memory for "
                                       + label);
        }
    }
    private void getDirPropsMemory(boolean mayAllocate, int len)
    {
        Object array = getMemory("DirProps", dirPropsMemory, Byte.TYPE, mayAllocate, len);
        dirPropsMemory = (byte[]) array;
    }
    void getDirPropsMemory(int len)
    {
        getDirPropsMemory(mayAllocateText, len);
    }
    private void getLevelsMemory(boolean mayAllocate, int len)
    {
        Object array = getMemory("Levels", levelsMemory, Byte.TYPE, mayAllocate, len);
        levelsMemory = (byte[]) array;
    }
    void getLevelsMemory(int len)
    {
        getLevelsMemory(mayAllocateText, len);
    }
    private void getRunsMemory(boolean mayAllocate, int len)
    {
        Object array = getMemory("Runs", runsMemory, BidiRun.class, mayAllocate, len);
        runsMemory = (BidiRun[]) array;
    }
    void getRunsMemory(int len)
    {
        getRunsMemory(mayAllocateRuns, len);
    }
    private void getInitialDirPropsMemory(int len)
    {
        getDirPropsMemory(true, len);
    }
    private void getInitialLevelsMemory(int len)
    {
        getLevelsMemory(true, len);
    }
    private void getInitialParasMemory(int len)
    {
        Object array = getMemory("Paras", parasMemory, Integer.TYPE, true, len);
        parasMemory = (int[]) array;
    }
    private void getInitialRunsMemory(int len)
    {
        getRunsMemory(true, len);
    }
    private void getDirProps()
    {
        int i = 0, i0, i1;
        flags = 0;          
        int uchar;
        byte dirProp;
        byte paraDirDefault = 0;   
        boolean isDefaultLevel = IsDefaultLevel(paraLevel);
        lastArabicPos = -1;
        controlCount = 0;
        final int NOT_CONTEXTUAL = 0;         
        final int LOOKING_FOR_STRONG = 1;     
        final int FOUND_STRONG_CHAR = 2;      
        int state;
        int paraStart = 0;                    
        byte paraDir;                         
        byte lastStrongDir=0;                 
        int lastStrongLTR=0;                  
        if (isDefaultLevel) {
            paraDirDefault = ((paraLevel & 1) != 0) ? CONTEXT_RTL : 0;
            paraDir = paraDirDefault;
            lastStrongDir = paraDirDefault;
            state = LOOKING_FOR_STRONG;
        } else {
            state = NOT_CONTEXTUAL;
            paraDir = 0;
        }
        for (i = 0; i < originalLength; ) {
            i0 = i;                     
            uchar = UTF16.charAt(text, 0, originalLength, i);
            i += Character.charCount(uchar);
            i1 = i - 1; 
            dirProp = (byte)bdp.getClass(uchar);
            flags |= DirPropFlag(dirProp);
            dirProps[i1] = (byte)(dirProp | paraDir);
            if (i1 > i0) {     
                flags |= DirPropFlag(BN);
                do {
                    dirProps[--i1] = (byte)(BN | paraDir);
                } while (i1 > i0);
            }
            if (state == LOOKING_FOR_STRONG) {
                if (dirProp == L) {
                    state = FOUND_STRONG_CHAR;
                    if (paraDir != 0) {
                        paraDir = 0;
                        for (i1 = paraStart; i1 < i; i1++) {
                            dirProps[i1] &= ~CONTEXT_RTL;
                        }
                    }
                    continue;
                }
                if (dirProp == R || dirProp == AL) {
                    state = FOUND_STRONG_CHAR;
                    if (paraDir == 0) {
                        paraDir = CONTEXT_RTL;
                        for (i1 = paraStart; i1 < i; i1++) {
                            dirProps[i1] |= CONTEXT_RTL;
                        }
                    }
                    continue;
                }
            }
            if (dirProp == L) {
                lastStrongDir = 0;
                lastStrongLTR = i;      
            }
            else if (dirProp == R) {
                lastStrongDir = CONTEXT_RTL;
            }
            else if (dirProp == AL) {
                lastStrongDir = CONTEXT_RTL;
                lastArabicPos = i-1;
            }
            else if (dirProp == B) {
                if (i < originalLength) {   
                    if (!((uchar == (int)CR) && (text[i] == (int)LF))) {
                        paraCount++;
                    }
                    if (isDefaultLevel) {
                        state=LOOKING_FOR_STRONG;
                        paraStart = i;        
                        paraDir = paraDirDefault;
                        lastStrongDir = paraDirDefault;
                    }
                }
            }
        }
        if (isDefaultLevel) {
            paraLevel = GetParaLevelAt(0);
        }
        flags |= DirPropFlagLR(paraLevel);
        if (orderParagraphsLTR && (flags & DirPropFlag(B)) != 0) {
            flags |= DirPropFlag(L);
        }
    }
    private byte directionFromFlags() {
        if (!((flags & MASK_RTL) != 0 ||
              ((flags & DirPropFlag(AN)) != 0 &&
               (flags & MASK_POSSIBLE_N) != 0))) {
            return Bidi.DIRECTION_LEFT_TO_RIGHT;
        } else if ((flags & MASK_LTR) == 0) {
            return Bidi.DIRECTION_RIGHT_TO_LEFT;
        } else {
            return MIXED;
        }
    }
    private byte resolveExplicitLevels() {
        int i = 0;
        byte dirProp;
        byte level = GetParaLevelAt(0);
        byte dirct;
        int paraIndex = 0;
        dirct = directionFromFlags();
        if ((dirct != MIXED) && (paraCount == 1)) {
        } else if ((paraCount == 1) &&
                   ((flags & MASK_EXPLICIT) == 0)) {
            for (i = 0; i < length; ++i) {
                levels[i] = level;
            }
        } else {
            byte embeddingLevel = level;
            byte newLevel;
            byte stackTop = 0;
            byte[] stack = new byte[MAX_EXPLICIT_LEVEL];    
            int countOver60 = 0;
            int countOver61 = 0;  
            flags = 0;
            for (i = 0; i < length; ++i) {
                dirProp = NoContextRTL(dirProps[i]);
                switch(dirProp) {
                case LRE:
                case LRO:
                    newLevel = (byte)((embeddingLevel+2) & ~(INTERNAL_LEVEL_OVERRIDE | 1)); 
                    if (newLevel <= MAX_EXPLICIT_LEVEL) {
                        stack[stackTop] = embeddingLevel;
                        ++stackTop;
                        embeddingLevel = newLevel;
                        if (dirProp == LRO) {
                            embeddingLevel |= INTERNAL_LEVEL_OVERRIDE;
                        }
                    } else if ((embeddingLevel & ~INTERNAL_LEVEL_OVERRIDE) == MAX_EXPLICIT_LEVEL) {
                        ++countOver61;
                    } else  {
                        ++countOver60;
                    }
                    flags |= DirPropFlag(BN);
                    break;
                case RLE:
                case RLO:
                    newLevel=(byte)(((embeddingLevel & ~INTERNAL_LEVEL_OVERRIDE) + 1) | 1); 
                    if (newLevel<=MAX_EXPLICIT_LEVEL) {
                        stack[stackTop] = embeddingLevel;
                        ++stackTop;
                        embeddingLevel = newLevel;
                        if (dirProp == RLO) {
                            embeddingLevel |= INTERNAL_LEVEL_OVERRIDE;
                        }
                    } else {
                        ++countOver61;
                    }
                    flags |= DirPropFlag(BN);
                    break;
                case PDF:
                    if (countOver61 > 0) {
                        --countOver61;
                    } else if (countOver60 > 0 && (embeddingLevel & ~INTERNAL_LEVEL_OVERRIDE) != MAX_EXPLICIT_LEVEL) {
                        --countOver60;
                    } else if (stackTop > 0) {
                        --stackTop;
                        embeddingLevel = stack[stackTop];
                    }
                    flags |= DirPropFlag(BN);
                    break;
                case B:
                    stackTop = 0;
                    countOver60 = 0;
                    countOver61 = 0;
                    level = GetParaLevelAt(i);
                    if ((i + 1) < length) {
                        embeddingLevel = GetParaLevelAt(i+1);
                        if (!((text[i] == CR) && (text[i + 1] == LF))) {
                            paras[paraIndex++] = i+1;
                        }
                    }
                    flags |= DirPropFlag(B);
                    break;
                case BN:
                    flags |= DirPropFlag(BN);
                    break;
                default:
                    if (level != embeddingLevel) {
                        level = embeddingLevel;
                        if ((level & INTERNAL_LEVEL_OVERRIDE) != 0) {
                            flags |= DirPropFlagO(level) | DirPropFlagMultiRuns;
                        } else {
                            flags |= DirPropFlagE(level) | DirPropFlagMultiRuns;
                        }
                    }
                    if ((level & INTERNAL_LEVEL_OVERRIDE) == 0) {
                        flags |= DirPropFlag(dirProp);
                    }
                    break;
                }
                levels[i] = level;
            }
            if ((flags & MASK_EMBEDDING) != 0) {
                flags |= DirPropFlagLR(paraLevel);
            }
            if (orderParagraphsLTR && (flags & DirPropFlag(B)) != 0) {
                flags |= DirPropFlag(L);
            }
            dirct = directionFromFlags();
        }
        return dirct;
    }
    private byte checkExplicitLevels() {
        byte dirProp;
        int i;
        this.flags = 0;     
        byte level;
        int paraIndex = 0;
        for (i = 0; i < length; ++i) {
            if (levels[i] == 0) {
                levels[i] = paraLevel;
            }
            if (MAX_EXPLICIT_LEVEL < (levels[i]&0x7f)) {
                if ((levels[i] & INTERNAL_LEVEL_OVERRIDE) != 0) {
                    levels[i] =  (byte)(paraLevel|INTERNAL_LEVEL_OVERRIDE);
                } else {
                    levels[i] = paraLevel;
                }
            }
            level = levels[i];
            dirProp = NoContextRTL(dirProps[i]);
            if ((level & INTERNAL_LEVEL_OVERRIDE) != 0) {
                level &= ~INTERNAL_LEVEL_OVERRIDE;     
                flags |= DirPropFlagO(level);
            } else {
                flags |= DirPropFlagE(level) | DirPropFlag(dirProp);
            }
            if ((level < GetParaLevelAt(i) &&
                    !((0 == level) && (dirProp == B))) ||
                    (MAX_EXPLICIT_LEVEL <level)) {
                throw new IllegalArgumentException("level " + level +
                                                   " out of bounds at index " + i);
            }
            if ((dirProp == B) && ((i + 1) < length)) {
                if (!((text[i] == CR) && (text[i + 1] == LF))) {
                    paras[paraIndex++] = i + 1;
                }
            }
        }
        if ((flags&MASK_EMBEDDING) != 0) {
            flags |= DirPropFlagLR(paraLevel);
        }
        return directionFromFlags();
    }
    private static final int IMPTABPROPS_COLUMNS = 14;
    private static final int IMPTABPROPS_RES = IMPTABPROPS_COLUMNS - 1;
    private static short GetStateProps(short cell) {
        return (short)(cell & 0x1f);
    }
    private static short GetActionProps(short cell) {
        return (short)(cell >> 5);
    }
    private static final short groupProp[] =          
    {
        0,  1,  2,  7,  8,  3,  9,  6,  5,  4,  4,  10, 10, 12, 10, 10, 10, 11, 10
    };
    private static final short _L  = 0;
    private static final short _R  = 1;
    private static final short _EN = 2;
    private static final short _AN = 3;
    private static final short _ON = 4;
    private static final short _S  = 5;
    private static final short _B  = 6; 
    private static final short impTabProps[][] =
    {
 {     1,     2,     4,     5,     7,    15,    17,     7,     9,     7,     0,     7,     3,  _ON },
 {     1,  32+2,  32+4,  32+5,  32+7, 32+15, 32+17,  32+7,  32+9,  32+7,     1,     1,  32+3,   _L },
 {  32+1,     2,  32+4,  32+5,  32+7, 32+15, 32+17,  32+7,  32+9,  32+7,     2,     2,  32+3,   _R },
 {  32+1,  32+2,  32+6,  32+6,  32+8, 32+16, 32+17,  32+8,  32+8,  32+8,     3,     3,     3,   _R },
 {  32+1,  32+2,     4,  32+5,  32+7, 32+15, 32+17, 64+10,    11, 64+10,     4,     4,  32+3,  _EN },
 {  32+1,  32+2,  32+4,     5,  32+7, 32+15, 32+17,  32+7,  32+9, 64+12,     5,     5,  32+3,  _AN },
 {  32+1,  32+2,     6,     6,  32+8, 32+16, 32+17,  32+8,  32+8, 64+13,     6,     6,  32+3,  _AN },
 {  32+1,  32+2,  32+4,  32+5,     7, 32+15, 32+17,     7, 64+14,     7,     7,     7,  32+3,  _ON },
 {  32+1,  32+2,  32+6,  32+6,     8, 32+16, 32+17,     8,     8,     8,     8,     8,  32+3,  _ON },
 {  32+1,  32+2,     4,  32+5,     7, 32+15, 32+17,     7,     9,     7,     9,     9,  32+3,  _ON },
 {  96+1,  96+2,     4,  96+5, 128+7, 96+15, 96+17, 128+7,128+14, 128+7,    10, 128+7,  96+3,  _EN },
 {  32+1,  32+2,     4,  32+5,  32+7, 32+15, 32+17,  32+7,    11,  32+7,    11,    11,  32+3,  _EN },
 {  96+1,  96+2,  96+4,     5, 128+7, 96+15, 96+17, 128+7,128+14, 128+7,    12, 128+7,  96+3,  _AN },
 {  96+1,  96+2,     6,     6, 128+8, 96+16, 96+17, 128+8, 128+8, 128+8,    13, 128+8,  96+3,  _AN },
 {  32+1,  32+2, 128+4,  32+5,     7, 32+15, 32+17,     7,    14,     7,    14,    14,  32+3,  _ON },
 {  32+1,  32+2,  32+4,  32+5,  32+7,    15, 32+17,  32+7,  32+9,  32+7,    15,  32+7,  32+3,   _S },
 {  32+1,  32+2,  32+6,  32+6,  32+8,    16, 32+17,  32+8,  32+8,  32+8,    16,  32+8,  32+3,   _S },
 {  32+1,  32+2,  32+4,  32+5,  32+7, 32+15,    17,  32+7,  32+9,  32+7,    17,  32+7,  32+3,   _B }
    };
    private static final int IMPTABLEVELS_COLUMNS = _B + 2;
    private static final int IMPTABLEVELS_RES = IMPTABLEVELS_COLUMNS - 1;
    private static short GetState(byte cell) { return (short)(cell & 0x0f); }
    private static short GetAction(byte cell) { return (short)(cell >> 4); }
    private static class ImpTabPair {
        byte[][][] imptab;
        short[][] impact;
        ImpTabPair(byte[][] table1, byte[][] table2,
                   short[] act1, short[] act2) {
            imptab = new byte[][][] {table1, table2};
            impact = new short[][] {act1, act2};
        }
    }
    private static final byte impTabL_DEFAULT[][] = 
    {
         {     0,     1,     0,     2,     0,     0,     0,  0 },
         {     0,     1,     3,     3,  0x14,  0x14,     0,  1 },
         {     0,     1,     0,     2,  0x15,  0x15,     0,  2 },
         {     0,     1,     3,     3,  0x14,  0x14,     0,  2 },
         {  0x20,     1,     3,     3,     4,     4,  0x20,  1 },
         {  0x20,     1,  0x20,     2,     5,     5,  0x20,  1 }
    };
    private static final byte impTabR_DEFAULT[][] = 
    {
         {     1,     0,     2,     2,     0,     0,     0,  0 },
         {     1,     0,     1,     3,  0x14,  0x14,     0,  1 },
         {     1,     0,     2,     2,     0,     0,     0,  1 },
         {     1,     0,     1,     3,     5,     5,     0,  1 },
         {  0x21,     0,  0x21,     3,     4,     4,     0,  0 },
         {     1,     0,     1,     3,     5,     5,     0,  0 }
    };
    private static final short[] impAct0 = {0,1,2,3,4,5,6};
    private static final ImpTabPair impTab_DEFAULT = new ImpTabPair(
            impTabL_DEFAULT, impTabR_DEFAULT, impAct0, impAct0);
    private static final byte impTabL_NUMBERS_SPECIAL[][] = { 
         {     0,     2,     1,     1,     0,     0,     0,  0 },
         {     0,     2,     1,     1,     0,     0,     0,  2 },
         {     0,     2,     4,     4,  0x13,     0,     0,  1 },
         {  0x20,     2,     4,     4,     3,     3,  0x20,  1 },
         {     0,     2,     4,     4,  0x13,  0x13,     0,  2 }
    };
    private static final ImpTabPair impTab_NUMBERS_SPECIAL = new ImpTabPair(
            impTabL_NUMBERS_SPECIAL, impTabR_DEFAULT, impAct0, impAct0);
    private static final byte impTabL_GROUP_NUMBERS_WITH_R[][] = {
         {     0,     3,  0x11,  0x11,     0,     0,     0,  0 },
         {  0x20,     3,     1,     1,     2,  0x20,  0x20,  2 },
         {  0x20,     3,     1,     1,     2,  0x20,  0x20,  1 },
         {     0,     3,     5,     5,  0x14,     0,     0,  1 },
         {  0x20,     3,     5,     5,     4,  0x20,  0x20,  1 },
         {     0,     3,     5,     5,  0x14,     0,     0,  2 }
    };
    private static final byte impTabR_GROUP_NUMBERS_WITH_R[][] = {
         {     2,     0,     1,     1,     0,     0,     0,  0 },
         {     2,     0,     1,     1,     0,     0,     0,  1 },
         {     2,     0,  0x14,  0x14,  0x13,     0,     0,  1 },
         {  0x22,     0,     4,     4,     3,     0,     0,  0 },
         {  0x22,     0,     4,     4,     3,     0,     0,  1 }
    };
    private static final ImpTabPair impTab_GROUP_NUMBERS_WITH_R = new
            ImpTabPair(impTabL_GROUP_NUMBERS_WITH_R,
                       impTabR_GROUP_NUMBERS_WITH_R, impAct0, impAct0);
    private static final byte impTabL_INVERSE_NUMBERS_AS_L[][] = {
         {     0,     1,     0,     0,     0,     0,     0,  0 },
         {     0,     1,     0,     0,  0x14,  0x14,     0,  1 },
         {     0,     1,     0,     0,  0x15,  0x15,     0,  2 },
         {     0,     1,     0,     0,  0x14,  0x14,     0,  2 },
         {  0x20,     1,  0x20,  0x20,     4,     4,  0x20,  1 },
         {  0x20,     1,  0x20,  0x20,     5,     5,  0x20,  1 }
    };
    private static final byte impTabR_INVERSE_NUMBERS_AS_L[][] = {
         {     1,     0,     1,     1,     0,     0,     0,  0 },
         {     1,     0,     1,     1,  0x14,  0x14,     0,  1 },
         {     1,     0,     1,     1,     0,     0,     0,  1 },
         {     1,     0,     1,     1,     5,     5,     0,  1 },
         {  0x21,     0,  0x21,  0x21,     4,     4,     0,  0 },
         {     1,     0,     1,     1,     5,     5,     0,  0 }
    };
    private static final ImpTabPair impTab_INVERSE_NUMBERS_AS_L = new ImpTabPair
            (impTabL_INVERSE_NUMBERS_AS_L, impTabR_INVERSE_NUMBERS_AS_L,
             impAct0, impAct0);
    private static final byte impTabR_INVERSE_LIKE_DIRECT[][] = {  
         {     1,     0,     2,     2,     0,     0,     0,  0 },
         {     1,     0,     1,     2,  0x13,  0x13,     0,  1 },
         {     1,     0,     2,     2,     0,     0,     0,  1 },
         {  0x21,  0x30,     6,     4,     3,     3,  0x30,  0 },
         {  0x21,  0x30,     6,     4,     5,     5,  0x30,  3 },
         {  0x21,  0x30,     6,     4,     5,     5,  0x30,  2 },
         {  0x21,  0x30,     6,     4,     3,     3,  0x30,  1 }
    };
    private static final short[] impAct1 = {0,1,11,12};
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT = new ImpTabPair(
            impTabL_DEFAULT, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);
    private static final byte impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS[][] = {
         {     0,  0x63,     0,     1,     0,     0,     0,  0 },
         {     0,  0x63,     0,     1,  0x12,  0x30,     0,  4 },
         {  0x20,  0x63,  0x20,     1,     2,  0x30,  0x20,  3 },
         {     0,  0x63,  0x55,  0x56,  0x14,  0x30,     0,  3 },
         {  0x30,  0x43,  0x55,  0x56,     4,  0x30,  0x30,  3 },
         {  0x30,  0x43,     5,  0x56,  0x14,  0x30,  0x30,  4 },
         {  0x30,  0x43,  0x55,     6,  0x14,  0x30,  0x30,  4 }
    };
    private static final byte impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS[][] = {
         {  0x13,     0,     1,     1,     0,     0,     0,  0 },
         {  0x23,     0,     1,     1,     2,  0x40,     0,  1 },
         {  0x23,     0,     1,     1,     2,  0x40,     0,  0 },
         {    3 ,     0,     3,  0x36,  0x14,  0x40,     0,  1 },
         {  0x53,  0x40,     5,  0x36,     4,  0x40,  0x40,  0 },
         {  0x53,  0x40,     5,  0x36,     4,  0x40,  0x40,  1 },
         {  0x53,  0x40,     6,     6,     4,  0x40,  0x40,  3 }
    };
    private static final short impAct2[] = {0,1,7,8,9,10};
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT_WITH_MARKS =
            new ImpTabPair(impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS,
                           impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct0, impAct2);
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL = new ImpTabPair(
            impTabL_NUMBERS_SPECIAL, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);
    private static final byte impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS[][] = {
         {     0,  0x62,     1,     1,     0,     0,     0,  0 },
         {     0,  0x62,     1,     1,     0,  0x30,     0,  4 },
         {     0,  0x62,  0x54,  0x54,  0x13,  0x30,     0,  3 },
         {  0x30,  0x42,  0x54,  0x54,     3,  0x30,  0x30,  3 },
         {  0x30,  0x42,     4,     4,  0x13,  0x30,  0x30,  4 }
    };
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = new
            ImpTabPair(impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS,
                       impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct0, impAct2);
    private class LevState {
        byte[][] impTab;                
        short[] impAct;                 
        int startON;                    
        int startL2EN;                  
        int lastStrongRTL;              
        short state;                    
        byte runLevel;                  
    }
    static final int FIRSTALLOC = 10;
    private void addPoint(int pos, int flag)
    {
        Point point = new Point();
        int len = insertPoints.points.length;
        if (len == 0) {
            insertPoints.points = new Point[FIRSTALLOC];
            len = FIRSTALLOC;
        }
        if (insertPoints.size >= len) { 
            Point[] savePoints = insertPoints.points;
            insertPoints.points = new Point[len * 2];
            System.arraycopy(savePoints, 0, insertPoints.points, 0, len);
        }
        point.pos = pos;
        point.flag = flag;
        insertPoints.points[insertPoints.size] = point;
        insertPoints.size++;
    }
    private void processPropertySeq(LevState levState, short _prop,
            int start, int limit) {
        byte cell;
        byte[][] impTab = levState.impTab;
        short[] impAct = levState.impAct;
        short oldStateSeq,actionSeq;
        byte level, addLevel;
        int start0, k;
        start0 = start;                 
        oldStateSeq = levState.state;
        cell = impTab[oldStateSeq][_prop];
        levState.state = GetState(cell);        
        actionSeq = impAct[GetAction(cell)];    
        addLevel = (byte)impTab[levState.state][IMPTABLEVELS_RES];
        if (actionSeq != 0) {
            switch (actionSeq) {
            case 1:                     
                levState.startON = start0;
                break;
            case 2:                     
                start = levState.startON;
                break;
            case 3:                     
                if (levState.startL2EN >= 0) {
                    addPoint(levState.startL2EN, LRM_BEFORE);
                }
                levState.startL2EN = -1;  
                if ((insertPoints.points.length == 0) ||
                        (insertPoints.size <= insertPoints.confirmed)) {
                    levState.lastStrongRTL = -1;
                    level = (byte)impTab[oldStateSeq][IMPTABLEVELS_RES];
                    if ((level & 1) != 0 && levState.startON > 0) { 
                        start = levState.startON;   
                    }
                    if (_prop == _S) {              
                        addPoint(start0, LRM_BEFORE);
                        insertPoints.confirmed = insertPoints.size;
                    }
                    break;
                }
                for (k = levState.lastStrongRTL + 1; k < start0; k++) {
                    levels[k] = (byte)((levels[k] - 2) & ~1);
                }
                insertPoints.confirmed = insertPoints.size;
                levState.lastStrongRTL = -1;
                if (_prop == _S) {           
                    addPoint(start0, LRM_BEFORE);
                    insertPoints.confirmed = insertPoints.size;
                }
                break;
            case 4:                     
                if (insertPoints.points.length > 0)
                    insertPoints.size = insertPoints.confirmed;
                levState.startON = -1;
                levState.startL2EN = -1;
                levState.lastStrongRTL = limit - 1;
                break;
            case 5:                     
                if ((_prop == _AN) && (NoContextRTL(dirProps[start0]) == AN)) {
                    if (levState.startL2EN == -1) { 
                        levState.lastStrongRTL = limit - 1;
                        break;
                    }
                    if (levState.startL2EN >= 0)  { 
                        addPoint(levState.startL2EN, LRM_BEFORE);
                        levState.startL2EN = -2;
                    }
                    addPoint(start0, LRM_BEFORE);
                    break;
                }
                if (levState.startL2EN == -1) {
                    levState.startL2EN = start0;
                }
                break;
            case 6:                     
                levState.lastStrongRTL = limit - 1;
                levState.startON = -1;
                break;
            case 7:                     
                for (k = start0-1; k >= 0 && ((levels[k] & 1) == 0); k--) {
                }
                if (k >= 0) {
                    addPoint(k, RLM_BEFORE);    
                    insertPoints.confirmed = insertPoints.size; 
                }
                levState.startON = start0;
                break;
            case 8:                     
                addPoint(start0, LRM_BEFORE);   
                addPoint(start0, LRM_AFTER);    
                break;
            case 9:                     
                insertPoints.size=insertPoints.confirmed;
                if (_prop == _S) {          
                    addPoint(start0, RLM_BEFORE);
                    insertPoints.confirmed = insertPoints.size;
                }
                break;
            case 10:                    
                level = (byte)(levState.runLevel + addLevel);
                for (k=levState.startON; k < start0; k++) {
                    if (levels[k] < level) {
                        levels[k] = level;
                    }
                }
                insertPoints.confirmed = insertPoints.size;   
                levState.startON = start0;
                break;
            case 11:                    
                level = (byte)levState.runLevel;
                for (k = start0-1; k >= levState.startON; k--) {
                    if (levels[k] == level+3) {
                        while (levels[k] == level+3) {
                            levels[k--] -= 2;
                        }
                        while (levels[k] == level) {
                            k--;
                        }
                    }
                    if (levels[k] == level+2) {
                        levels[k] = level;
                        continue;
                    }
                    levels[k] = (byte)(level+1);
                }
                break;
            case 12:                    
                level = (byte)(levState.runLevel+1);
                for (k = start0-1; k >= levState.startON; k--) {
                    if (levels[k] > level) {
                        levels[k] -= 2;
                    }
                }
                break;
            default:                        
                throw new IllegalStateException("Internal ICU error in processPropertySeq");
            }
        }
        if ((addLevel) != 0 || (start < start0)) {
            level = (byte)(levState.runLevel + addLevel);
            for (k = start; k < limit; k++) {
                levels[k] = level;
            }
        }
    }
    private void resolveImplicitLevels(int start, int limit, short sor, short eor)
    {
        LevState levState = new LevState();
        int i, start1, start2;
        short oldStateImp, stateImp, actionImp;
        short gprop, resProp, cell;
        short nextStrongProp = R;
        int nextStrongPos = -1;
        levState.startL2EN = -1;        
        levState.lastStrongRTL = -1;    
        levState.state = 0;
        levState.runLevel = levels[start];
        levState.impTab = impTabPair.imptab[levState.runLevel & 1];
        levState.impAct = impTabPair.impact[levState.runLevel & 1];
        processPropertySeq(levState, (short)sor, start, start);
        if (dirProps[start] == NSM) {
            stateImp = (short)(1 + sor);
        } else {
            stateImp = 0;
        }
        start1 = start;
        start2 = 0;
        for (i = start; i <= limit; i++) {
            if (i >= limit) {
                gprop = eor;
            } else {
                short prop, prop1;
                prop = NoContextRTL(dirProps[i]);
                gprop = groupProp[prop];
            }
            oldStateImp = stateImp;
            cell = impTabProps[oldStateImp][gprop];
            stateImp = GetStateProps(cell);     
            actionImp = GetActionProps(cell);   
            if ((i == limit) && (actionImp == 0)) {
                actionImp = 1;                  
            }
            if (actionImp != 0) {
                resProp = impTabProps[oldStateImp][IMPTABPROPS_RES];
                switch (actionImp) {
                case 1:             
                    processPropertySeq(levState, resProp, start1, i);
                    start1 = i;
                    break;
                case 2:             
                    start2 = i;
                    break;
                case 3:             
                    processPropertySeq(levState, resProp, start1, start2);
                    processPropertySeq(levState, _ON, start2, i);
                    start1 = i;
                    break;
                case 4:             
                    processPropertySeq(levState, resProp, start1, start2);
                    start1 = start2;
                    start2 = i;
                    break;
                default:            
                    throw new IllegalStateException("Internal ICU error in resolveImplicitLevels");
                }
            }
        }
        processPropertySeq(levState, (short)eor, limit, limit);
    }
    private void adjustWSLevels() {
        int i;
        if ((flags & MASK_WS) != 0) {
            int flag;
            i = trailingWSStart;
            while (i > 0) {
                while (i > 0 && ((flag = DirPropFlagNC(dirProps[--i])) & MASK_WS) != 0) {
                    if (orderParagraphsLTR && (flag & DirPropFlag(B)) != 0) {
                        levels[i] = 0;
                    } else {
                        levels[i] = GetParaLevelAt(i);
                    }
                }
                while (i > 0) {
                    flag = DirPropFlagNC(dirProps[--i]);
                    if ((flag & MASK_BN_EXPLICIT) != 0) {
                        levels[i] = levels[i + 1];
                    } else if (orderParagraphsLTR && (flag & DirPropFlag(B)) != 0) {
                        levels[i] = 0;
                        break;
                    } else if ((flag & MASK_B_S) != 0){
                        levels[i] = GetParaLevelAt(i);
                        break;
                    }
                }
            }
        }
    }
    private int Bidi_Min(int x, int y) {
        return x < y ? x : y;
    }
    private int Bidi_Abs(int x) {
        return x >= 0 ? x : -x;
    }
    void setPara(String text, byte paraLevel, byte[] embeddingLevels)
    {
        if (text == null) {
            setPara(new char[0], paraLevel, embeddingLevels);
        } else {
            setPara(text.toCharArray(), paraLevel, embeddingLevels);
        }
    }
    public void setPara(char[] chars, byte paraLevel, byte[] embeddingLevels)
    {
        if (paraLevel < INTERNAL_LEVEL_DEFAULT_LTR) {
            verifyRange(paraLevel, 0, MAX_EXPLICIT_LEVEL + 1);
        }
        if (chars == null) {
            chars = new char[0];
        }
        this.paraBidi = null;          
        this.text = chars;
        this.length = this.originalLength = this.resultLength = text.length;
        this.paraLevel = paraLevel;
        this.direction = Bidi.DIRECTION_LEFT_TO_RIGHT;
        this.paraCount = 1;
        dirProps = new byte[0];
        levels = new byte[0];
        runs = new BidiRun[0];
        isGoodLogicalToVisualRunsMap = false;
        insertPoints.size = 0;          
        insertPoints.confirmed = 0;     
        if (IsDefaultLevel(paraLevel)) {
            defaultParaLevel = paraLevel;
        } else {
            defaultParaLevel = 0;
        }
        if (length == 0) {
            if (IsDefaultLevel(paraLevel)) {
                this.paraLevel &= 1;
                defaultParaLevel = 0;
            }
            if ((this.paraLevel & 1) != 0) {
                flags = DirPropFlag(R);
                direction = Bidi.DIRECTION_RIGHT_TO_LEFT;
            } else {
                flags = DirPropFlag(L);
                direction = Bidi.DIRECTION_LEFT_TO_RIGHT;
            }
            runCount = 0;
            paraCount = 0;
            paraBidi = this;         
            return;
        }
        runCount = -1;
        getDirPropsMemory(length);
        dirProps = dirPropsMemory;
        getDirProps();
        trailingWSStart = length;  
        if (paraCount > 1) {
            getInitialParasMemory(paraCount);
            paras = parasMemory;
            paras[paraCount - 1] = length;
        } else {
            paras = simpleParas;
            simpleParas[0] = length;
        }
        if (embeddingLevels == null) {
            getLevelsMemory(length);
            levels = levelsMemory;
            direction = resolveExplicitLevels();
        } else {
            levels = embeddingLevels;
            direction = checkExplicitLevels();
        }
        switch (direction) {
        case Bidi.DIRECTION_LEFT_TO_RIGHT:
            paraLevel = (byte)((paraLevel + 1) & ~1);
            trailingWSStart = 0;
            break;
        case Bidi.DIRECTION_RIGHT_TO_LEFT:
            paraLevel |= 1;
            trailingWSStart = 0;
            break;
        default:
            this.impTabPair = impTab_DEFAULT;
            if (embeddingLevels == null && paraCount <= 1 &&
                (flags & DirPropFlagMultiRuns) == 0) {
                resolveImplicitLevels(0, length,
                        GetLRFromLevel(GetParaLevelAt(0)),
                        GetLRFromLevel(GetParaLevelAt(length - 1)));
            } else {
                int start, limit = 0;
                byte level, nextLevel;
                short sor, eor;
                level = GetParaLevelAt(0);
                nextLevel = levels[0];
                if (level < nextLevel) {
                    eor = GetLRFromLevel(nextLevel);
                } else {
                    eor = GetLRFromLevel(level);
                }
                do {
                    start = limit;
                    level = nextLevel;
                    if ((start > 0) && (NoContextRTL(dirProps[start - 1]) == B)) {
                        sor = GetLRFromLevel(GetParaLevelAt(start));
                    } else {
                        sor = eor;
                    }
                    while (++limit < length && levels[limit] == level) {}
                    if (limit < length) {
                        nextLevel = levels[limit];
                    } else {
                        nextLevel = GetParaLevelAt(length - 1);
                    }
                    if ((level & ~INTERNAL_LEVEL_OVERRIDE) < (nextLevel & ~INTERNAL_LEVEL_OVERRIDE)) {
                        eor = GetLRFromLevel(nextLevel);
                    } else {
                        eor = GetLRFromLevel(level);
                    }
                    if ((level & INTERNAL_LEVEL_OVERRIDE) == 0) {
                        resolveImplicitLevels(start, limit, sor, eor);
                    } else {
                        do {
                            levels[start++] &= ~INTERNAL_LEVEL_OVERRIDE;
                        } while (start < limit);
                    }
                } while (limit  < length);
            }
            adjustWSLevels();
            break;
        }
        resultLength += insertPoints.size;
        paraBidi = this;             
    }
    public void setPara(AttributedCharacterIterator paragraph)
    {
        byte paraLvl;
        Boolean runDirection =
            (Boolean) paragraph.getAttribute(TextAttributeConstants.RUN_DIRECTION);
        Object shaper = paragraph.getAttribute(TextAttributeConstants.NUMERIC_SHAPING);
        if (runDirection == null) {
            paraLvl = INTERNAL_LEVEL_DEFAULT_LTR;
        } else {
            paraLvl = (runDirection.equals(TextAttributeConstants.RUN_DIRECTION_LTR)) ?
                        (byte)Bidi.DIRECTION_LEFT_TO_RIGHT : (byte)Bidi.DIRECTION_RIGHT_TO_LEFT;
        }
        byte[] lvls = null;
        int len = paragraph.getEndIndex() - paragraph.getBeginIndex();
        byte[] embeddingLevels = new byte[len];
        char[] txt = new char[len];
        int i = 0;
        char ch = paragraph.first();
        while (ch != AttributedCharacterIterator.DONE) {
            txt[i] = ch;
            Integer embedding =
                (Integer) paragraph.getAttribute(TextAttributeConstants.BIDI_EMBEDDING);
            if (embedding != null) {
                byte level = embedding.byteValue();
                if (level == 0) {
                } else if (level < 0) {
                    lvls = embeddingLevels;
                    embeddingLevels[i] = (byte)((0 - level) | INTERNAL_LEVEL_OVERRIDE);
                } else {
                    lvls = embeddingLevels;
                    embeddingLevels[i] = level;
                }
            }
            ch = paragraph.next();
            ++i;
        }
        if (shaper != null) {
            NumericShapings.shape(shaper, txt, 0, len);
        }
        setPara(txt, paraLvl, lvls);
    }
    private void orderParagraphsLTR(boolean ordarParaLTR) {
        orderParagraphsLTR = ordarParaLTR;
    }
    private byte getDirection()
    {
        verifyValidParaOrLine();
        return direction;
    }
    public int getLength()
    {
        verifyValidParaOrLine();
        return originalLength;
    }
    public byte getParaLevel()
    {
        verifyValidParaOrLine();
        return paraLevel;
    }
    public int getParagraphIndex(int charIndex)
    {
        verifyValidParaOrLine();
        BidiBase bidi = paraBidi;             
        verifyRange(charIndex, 0, bidi.length);
        int paraIndex;
        for (paraIndex = 0; charIndex >= bidi.paras[paraIndex]; paraIndex++) {
        }
        return paraIndex;
    }
    public Bidi setLine(Bidi bidi, BidiBase bidiBase, Bidi newBidi, BidiBase newBidiBase, int start, int limit)
    {
        verifyValidPara();
        verifyRange(start, 0, limit);
        verifyRange(limit, 0, length+1);
        return BidiLine.setLine(bidi, this, newBidi, newBidiBase, start, limit);
    }
    public byte getLevelAt(int charIndex)
    {
        if (charIndex < 0 || charIndex >= length) {
            return (byte)getBaseLevel();
        }
        verifyValidParaOrLine();
        verifyRange(charIndex, 0, length);
        return BidiLine.getLevelAt(this, charIndex);
    }
    private byte[] getLevels()
    {
        verifyValidParaOrLine();
        if (length <= 0) {
            return new byte[0];
        }
        return BidiLine.getLevels(this);
    }
    public int countRuns()
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        return runCount;
    }
    private int[] getVisualMap()
    {
        countRuns();
        if (resultLength <= 0) {
            return new int[0];
        }
        return BidiLine.getVisualMap(this);
    }
    private static int[] reorderVisual(byte[] levels)
    {
        return BidiLine.reorderVisual(levels);
    }
    private static final int INTERNAL_DIRECTION_DEFAULT_LEFT_TO_RIGHT = 0x7e;
    private static final int INTERMAL_DIRECTION_DEFAULT_RIGHT_TO_LEFT = 0x7f;
    public BidiBase(char[] text,
             int textStart,
             byte[] embeddings,
             int embStart,
             int paragraphLength,
             int flags)
     {
        this(0, 0);
        byte paraLvl;
        switch (flags) {
        case Bidi.DIRECTION_LEFT_TO_RIGHT:
        default:
            paraLvl = Bidi.DIRECTION_LEFT_TO_RIGHT;
            break;
        case Bidi.DIRECTION_RIGHT_TO_LEFT:
            paraLvl = Bidi.DIRECTION_RIGHT_TO_LEFT;
            break;
        case Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT:
            paraLvl = INTERNAL_LEVEL_DEFAULT_LTR;
            break;
        case Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT:
            paraLvl = INTERNAL_LEVEL_DEFAULT_RTL;
            break;
        }
        byte[] paraEmbeddings;
        if (embeddings == null) {
            paraEmbeddings = null;
        } else {
            paraEmbeddings = new byte[paragraphLength];
            byte lev;
            for (int i = 0; i < paragraphLength; i++) {
                lev = embeddings[i + embStart];
                if (lev < 0) {
                    lev = (byte)((- lev) | INTERNAL_LEVEL_OVERRIDE);
                } else if (lev == 0) {
                    lev = paraLvl;
                    if (paraLvl > MAX_EXPLICIT_LEVEL) {
                        lev &= 1;
                    }
                }
                paraEmbeddings[i] = lev;
            }
        }
        if (textStart == 0 && embStart == 0 && paragraphLength == text.length) {
            setPara(text, paraLvl, paraEmbeddings);
        } else {
            char[] paraText = new char[paragraphLength];
            System.arraycopy(text, textStart, paraText, 0, paragraphLength);
            setPara(paraText, paraLvl, paraEmbeddings);
        }
    }
    public boolean isMixed()
    {
        return (!isLeftToRight() && !isRightToLeft());
    }
    public boolean isLeftToRight()
    {
        return (getDirection() == Bidi.DIRECTION_LEFT_TO_RIGHT && (paraLevel & 1) == 0);
    }
    public boolean isRightToLeft()
    {
        return (getDirection() == Bidi.DIRECTION_RIGHT_TO_LEFT && (paraLevel & 1) == 1);
    }
    public boolean baseIsLeftToRight()
    {
        return (getParaLevel() == Bidi.DIRECTION_LEFT_TO_RIGHT);
    }
    public int getBaseLevel()
    {
        return getParaLevel();
    }
    private void getLogicalToVisualRunsMap()
    {
        if (isGoodLogicalToVisualRunsMap) {
            return;
        }
        int count = countRuns();
        if ((logicalToVisualRunsMap == null) ||
            (logicalToVisualRunsMap.length < count)) {
            logicalToVisualRunsMap = new int[count];
        }
        int i;
        long[] keys = new long[count];
        for (i = 0; i < count; i++) {
            keys[i] = ((long)(runs[i].start)<<32) + i;
        }
        Arrays.sort(keys);
        for (i = 0; i < count; i++) {
            logicalToVisualRunsMap[i] = (int)(keys[i] & 0x00000000FFFFFFFF);
        }
        keys = null;
        isGoodLogicalToVisualRunsMap = true;
    }
    public int getRunLevel(int run)
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        if (runCount == 1) {
            return getParaLevel();
        }
        verifyIndex(run, 0, runCount);
        getLogicalToVisualRunsMap();
        return runs[logicalToVisualRunsMap[run]].level;
    }
    public int getRunStart(int run)
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        if (runCount == 1) {
            return 0;
        } else if (run == runCount) {
            return length;
        }
        verifyIndex(run, 0, runCount);
        getLogicalToVisualRunsMap();
        return runs[logicalToVisualRunsMap[run]].start;
    }
    public int getRunLimit(int run)
    {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        if (runCount == 1) {
            return length;
        }
        verifyIndex(run, 0, runCount);
        getLogicalToVisualRunsMap();
        int idx = logicalToVisualRunsMap[run];
        int len = idx == 0 ? runs[idx].limit :
                                runs[idx].limit - runs[idx-1].limit;
        return runs[idx].start + len;
    }
    public static boolean requiresBidi(char[] text,
            int start,
            int limit)
    {
        final int RTLMask = (1 << Bidi.DIRECTION_RIGHT_TO_LEFT |
                1 << AL |
                1 << RLE |
                1 << RLO |
                1 << AN);
        if (0 > start || start > limit || limit > text.length) {
            throw new IllegalArgumentException("Value start " + start +
                      " is out of range 0 to " + limit);
        }
        for (int i = start; i < limit; ++i) {
            if (Character.isHighSurrogate(text[i]) && i < (limit-1) &&
                Character.isLowSurrogate(text[i+1])) {
                if (((1 << UCharacter.getDirection(Character.codePointAt(text, i))) & RTLMask) != 0) {
                    return true;
                }
            } else if (((1 << UCharacter.getDirection(text[i])) & RTLMask) != 0) {
                return true;
            }
        }
        return false;
    }
    public static void reorderVisually(byte[] levels,
            int levelStart,
            Object[] objects,
            int objectStart,
            int count)
    {
        if (0 > levelStart || levels.length <= levelStart) {
            throw new IllegalArgumentException("Value levelStart " +
                      levelStart + " is out of range 0 to " +
                      (levels.length-1));
        }
        if (0 > objectStart || objects.length <= objectStart) {
            throw new IllegalArgumentException("Value objectStart " +
                      levelStart + " is out of range 0 to " +
                      (objects.length-1));
        }
        if (0 > count || objects.length < (objectStart+count)) {
            throw new IllegalArgumentException("Value count " +
                      levelStart + " is out of range 0 to " +
                      (objects.length - objectStart));
        }
        byte[] reorderLevels = new byte[count];
        System.arraycopy(levels, levelStart, reorderLevels, 0, count);
        int[] indexMap = reorderVisual(reorderLevels);
        Object[] temp = new Object[count];
        System.arraycopy(objects, objectStart, temp, 0, count);
        for (int i = 0; i < count; ++i) {
            objects[objectStart + i] = temp[indexMap[i]];
        }
    }
    public String toString() {
        StringBuffer buf = new StringBuffer(super.toString());
        buf.append("[dir: " + direction);
        buf.append(" baselevel: " + paraLevel);
        buf.append(" length: " + length);
        buf.append(" runs: ");
        if (levels == null) {
            buf.append("null");
        } else {
            buf.append('[');
            buf.append(levels[0]);
            for (int i = 0; i < levels.length; i++) {
                buf.append(' ');
                buf.append(levels[i]);
            }
            buf.append(']');
        }
        buf.append(" text: [0x");
        buf.append(Integer.toHexString(text[0]));
        for (int i = 0; i < text.length; i++) {
            buf.append(" 0x");
            buf.append(Integer.toHexString(text[i]));
        }
        buf.append(']');
        buf.append(']');
        return buf.toString();
    }
    private static class TextAttributeConstants {
        private static final Class<?> clazz = getClass("java.awt.font.TextAttribute");
        static final AttributedCharacterIterator.Attribute RUN_DIRECTION =
            getTextAttribute("RUN_DIRECTION");
        static final AttributedCharacterIterator.Attribute NUMERIC_SHAPING =
            getTextAttribute("NUMERIC_SHAPING");
        static final AttributedCharacterIterator.Attribute BIDI_EMBEDDING =
            getTextAttribute("BIDI_EMBEDDING");
        static final Boolean RUN_DIRECTION_LTR = (clazz == null) ?
            Boolean.FALSE : (Boolean)getStaticField(clazz, "RUN_DIRECTION_LTR");
        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        private static Object getStaticField(Class<?> clazz, String name) {
            try {
                Field f = clazz.getField(name);
                return f.get(null);
            } catch (NoSuchFieldException | IllegalAccessException x) {
                throw new AssertionError(x);
            }
        }
        private static AttributedCharacterIterator.Attribute
            getTextAttribute(String name)
        {
            if (clazz == null) {
                return new AttributedCharacterIterator.Attribute(name) { };
            } else {
                return (AttributedCharacterIterator.Attribute)getStaticField(clazz, name);
            }
        }
    }
    private static class NumericShapings {
        private static final Class<?> clazz =
            getClass("java.awt.font.NumericShaper");
        private static final Method shapeMethod =
            getMethod(clazz, "shape", char[].class, int.class, int.class);
        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        private static Method getMethod(Class<?> clazz,
                                        String name,
                                        Class<?>... paramTypes)
        {
            if (clazz != null) {
                try {
                    return clazz.getMethod(name, paramTypes);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(e);
                }
            } else {
                return null;
            }
        }
        static void shape(Object shaper, char[] text, int start, int count) {
            if (shapeMethod == null)
                throw new AssertionError("Should not get here");
            try {
                shapeMethod.invoke(shaper, text, start, count);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException)cause;
                throw new AssertionError(e);
            } catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }
    }
}
