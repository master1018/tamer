public final class NormalizerImpl {
    static final NormalizerImpl IMPL;
    static
    {
        try
        {
            IMPL = new NormalizerImpl();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
    static final int UNSIGNED_BYTE_MASK =0xFF;
    static final long UNSIGNED_INT_MASK = 0xffffffffL;
    private static final String DATA_FILE_NAME = "/sun/text/resources/unorm.icu";
    public static final int QC_NFC=0x11;          
    public static final int QC_NFKC=0x22;         
    public static final int QC_NFD=4;             
    public static final int QC_NFKD=8;            
    public static final int QC_ANY_NO=0xf;
    public static final int QC_MAYBE=0x10;
    public static final int QC_ANY_MAYBE=0x30;
    public static final int QC_MASK=0x3f;
    private static final int COMBINES_FWD=0x40;
    private static final int COMBINES_BACK=0x80;
    public  static final int COMBINES_ANY=0xc0;
    private static final int CC_SHIFT=8;
    public  static final int CC_MASK=0xff00;
    private static final int EXTRA_SHIFT=16;
    private static final long  MIN_SPECIAL    =  (long)(0xfc000000 & UNSIGNED_INT_MASK);
    private static final long  SURROGATES_TOP =  (long)(0xfff00000 & UNSIGNED_INT_MASK);
    private static final long  MIN_HANGUL     =  (long)(0xfff00000 & UNSIGNED_INT_MASK);
    private static final long  JAMO_V_TOP     =  (long)(0xfff30000 & UNSIGNED_INT_MASK);
    static final int INDEX_TRIE_SIZE           = 0;
    static final int INDEX_CHAR_COUNT           = 1;
    static final int INDEX_COMBINE_DATA_COUNT = 2;
    public static final int INDEX_MIN_NFC_NO_MAYBE   = 6;
    public static final int INDEX_MIN_NFKC_NO_MAYBE  = 7;
    public static final int INDEX_MIN_NFD_NO_MAYBE   = 8;
    public static final int INDEX_MIN_NFKD_NO_MAYBE  = 9;
    static final int INDEX_FCD_TRIE_SIZE      = 10;
    static final int INDEX_AUX_TRIE_SIZE      = 11;
    static final int INDEX_TOP                = 32;
    private static final int AUX_UNSAFE_SHIFT           = 11;
    private static final int AUX_COMP_EX_SHIFT           = 10;
    private static final int AUX_NFC_SKIPPABLE_F_SHIFT = 12;
    private static final int AUX_MAX_FNC          =   ((int)1<<AUX_COMP_EX_SHIFT);
    private static final int AUX_UNSAFE_MASK      =   (int)((1<<AUX_UNSAFE_SHIFT) & UNSIGNED_INT_MASK);
    private static final int AUX_FNC_MASK         =   (int)((AUX_MAX_FNC-1) & UNSIGNED_INT_MASK);
    private static final int AUX_COMP_EX_MASK     =   (int)((1<<AUX_COMP_EX_SHIFT) & UNSIGNED_INT_MASK);
    private static final long AUX_NFC_SKIP_F_MASK =   ((UNSIGNED_INT_MASK&1)<<AUX_NFC_SKIPPABLE_F_SHIFT);
    private static final int MAX_BUFFER_SIZE                    = 20;
    static final class NormTrieImpl implements Trie.DataManipulate{
        static IntTrie normTrie= null;
        public int getFoldingOffset(int value){
            return  BMP_INDEX_LENGTH+
                    ((value>>(EXTRA_SHIFT-SURROGATE_BLOCK_BITS))&
                    (0x3ff<<SURROGATE_BLOCK_BITS));
        }
    }
    static final class FCDTrieImpl implements Trie.DataManipulate{
        static CharTrie fcdTrie=null;
        public int getFoldingOffset(int value){
            return value;
        }
    }
    static final class AuxTrieImpl implements Trie.DataManipulate{
        static CharTrie auxTrie = null;
        public int getFoldingOffset(int value){
            return (int)(value &AUX_FNC_MASK)<<SURROGATE_BLOCK_BITS;
        }
    }
    private static FCDTrieImpl fcdTrieImpl;
    private static NormTrieImpl normTrieImpl;
    private static AuxTrieImpl auxTrieImpl;
    private static int[] indexes;
    private static char[] combiningTable;
    private static char[] extraData;
    private static boolean isDataLoaded;
    private static boolean isFormatVersion_2_1;
    private static boolean isFormatVersion_2_2;
    private static byte[] unicodeVersion;
    private static final int DATA_BUFFER_SIZE = 25000;
    public static final int MIN_WITH_LEAD_CC=0x300;
    private static final int DECOMP_FLAG_LENGTH_HAS_CC=0x80;
    private static final int DECOMP_LENGTH_MASK=0x7f;
    private static final int BMP_INDEX_LENGTH=0x10000>>Trie.INDEX_STAGE_1_SHIFT_;
    private static final int SURROGATE_BLOCK_BITS=10-Trie.INDEX_STAGE_1_SHIFT_;
   public static int getFromIndexesArr(int index){
        return indexes[index];
   }
    private NormalizerImpl() throws IOException {
        if(!isDataLoaded){
            InputStream i = ICUData.getRequiredStream(DATA_FILE_NAME);
            BufferedInputStream b = new BufferedInputStream(i,DATA_BUFFER_SIZE);
            NormalizerDataReader reader = new NormalizerDataReader(b);
            indexes = reader.readIndexes(NormalizerImpl.INDEX_TOP);
            byte[] normBytes = new byte[indexes[NormalizerImpl.INDEX_TRIE_SIZE]];
            int combiningTableTop = indexes[NormalizerImpl.INDEX_COMBINE_DATA_COUNT];
            combiningTable = new char[combiningTableTop];
            int extraDataTop = indexes[NormalizerImpl.INDEX_CHAR_COUNT];
            extraData = new char[extraDataTop];
            byte[] fcdBytes = new byte[indexes[NormalizerImpl.INDEX_FCD_TRIE_SIZE]];
            byte[] auxBytes = new byte[indexes[NormalizerImpl.INDEX_AUX_TRIE_SIZE]];
            fcdTrieImpl = new FCDTrieImpl();
            normTrieImpl = new NormTrieImpl();
            auxTrieImpl = new AuxTrieImpl();
            reader.read(normBytes, fcdBytes,auxBytes, extraData, combiningTable);
            NormTrieImpl.normTrie = new IntTrie( new ByteArrayInputStream(normBytes),normTrieImpl );
            FCDTrieImpl.fcdTrie   = new CharTrie( new ByteArrayInputStream(fcdBytes),fcdTrieImpl  );
            AuxTrieImpl.auxTrie   = new CharTrie( new ByteArrayInputStream(auxBytes),auxTrieImpl  );
            isDataLoaded = true;
            byte[] formatVersion = reader.getDataFormatVersion();
            isFormatVersion_2_1 =( formatVersion[0]>2
                                    ||
                                   (formatVersion[0]==2 && formatVersion[1]>=1)
                                 );
            isFormatVersion_2_2 =( formatVersion[0]>2
                                    ||
                                   (formatVersion[0]==2 && formatVersion[1]>=2)
                                 );
            unicodeVersion = reader.getUnicodeVersion();
            b.close();
        }
    }
    public static final int JAMO_L_BASE=0x1100;     
    public static final int JAMO_V_BASE=0x1161;     
    public static final int JAMO_T_BASE=0x11a7;     
    public static final int HANGUL_BASE=0xac00;
    public static final int JAMO_L_COUNT=19;
    public static final int JAMO_V_COUNT=21;
    public static final int JAMO_T_COUNT=28;
    public  static final int HANGUL_COUNT=JAMO_L_COUNT*JAMO_V_COUNT*JAMO_T_COUNT;
    private static boolean isHangulWithoutJamoT(char c) {
        c-=HANGUL_BASE;
        return c<HANGUL_COUNT && c%JAMO_T_COUNT==0;
    }
    private static boolean isNorm32Regular(long norm32) {
        return norm32<MIN_SPECIAL;
    }
    private static boolean isNorm32LeadSurrogate(long norm32) {
        return MIN_SPECIAL<=norm32 && norm32<SURROGATES_TOP;
    }
    private static boolean isNorm32HangulOrJamo(long norm32) {
        return norm32>=MIN_HANGUL;
    }
    private static boolean isJamoVTNorm32JamoV(long norm32) {
        return norm32<JAMO_V_TOP;
    }
    public static long getNorm32(char c) {
        return ((UNSIGNED_INT_MASK) & (NormTrieImpl.normTrie.getLeadValue(c)));
    }
    public static long getNorm32FromSurrogatePair(long norm32,
                                                               char c2) {
        return ((UNSIGNED_INT_MASK) &
                    NormTrieImpl.normTrie.getTrailValue((int)norm32, c2));
    }
    private static long getNorm32(int c){
        return (UNSIGNED_INT_MASK&(NormTrieImpl.normTrie.getCodePointValue(c)));
    }
    private static long getNorm32(char[] p,int start,
                                              int mask) {
        long norm32= getNorm32(p[start]);
        if(((norm32&mask)>0) && isNorm32LeadSurrogate(norm32)) {
            norm32=getNorm32FromSurrogatePair(norm32, p[start+1]);
        }
        return norm32;
    }
    public static VersionInfo getUnicodeVersion(){
        return VersionInfo.getInstance(unicodeVersion[0], unicodeVersion[1],
                                       unicodeVersion[2], unicodeVersion[3]);
    }
    public static char    getFCD16(char c) {
        return  FCDTrieImpl.fcdTrie.getLeadValue(c);
    }
    public static char getFCD16FromSurrogatePair(char fcd16, char c2) {
        return FCDTrieImpl.fcdTrie.getTrailValue(fcd16, c2);
    }
    public static int getFCD16(int c) {
        return  FCDTrieImpl.fcdTrie.getCodePointValue(c);
    }
    private static int getExtraDataIndex(long norm32) {
        return (int)(norm32>>EXTRA_SHIFT);
    }
    private static final class DecomposeArgs{
        int  cc;
        int  trailCC;
        int length;
    }
    private static int decompose(long norm32,
                                          int qcMask,
                                          DecomposeArgs args) {
        int p= getExtraDataIndex(norm32);
        args.length=extraData[p++];
        if((norm32&qcMask&QC_NFKD)!=0 && args.length>=0x100) {
            p+=((args.length>>7)&1)+(args.length&DECOMP_LENGTH_MASK);
            args.length>>=8;
        }
        if((args.length&DECOMP_FLAG_LENGTH_HAS_CC)>0) {
            char bothCCs=extraData[p++];
            args.cc=(UNSIGNED_BYTE_MASK) & (bothCCs>>8);
            args.trailCC=(UNSIGNED_BYTE_MASK) & bothCCs;
        } else {
            args.cc=args.trailCC=0;
        }
        args.length&=DECOMP_LENGTH_MASK;
        return p;
    }
    private static int decompose(long norm32,
                                 DecomposeArgs args) {
        int p= getExtraDataIndex(norm32);
        args.length=extraData[p++];
        if((args.length&DECOMP_FLAG_LENGTH_HAS_CC)>0) {
            char bothCCs=extraData[p++];
            args.cc=(UNSIGNED_BYTE_MASK) & (bothCCs>>8);
            args.trailCC=(UNSIGNED_BYTE_MASK) & bothCCs;
        } else {
            args.cc=args.trailCC=0;
        }
        args.length&=DECOMP_LENGTH_MASK;
        return p;
    }
    private static final class NextCCArgs{
        char[] source;
        int next;
        int limit;
        char c;
        char c2;
    }
    private static int  getNextCC(NextCCArgs args) {
        long  norm32;
        args.c=args.source[args.next++];
        norm32= getNorm32(args.c);
        if((norm32 & CC_MASK)==0) {
            args.c2=0;
            return 0;
        } else {
            if(!isNorm32LeadSurrogate(norm32)) {
                args.c2=0;
            } else {
                if(args.next!=args.limit &&
                        UTF16.isTrailSurrogate(args.c2=args.source[args.next])){
                    ++args.next;
                    norm32=getNorm32FromSurrogatePair(norm32, args.c2);
                } else {
                    args.c2=0;
                    return 0;
                }
            }
            return (int)((UNSIGNED_BYTE_MASK) & (norm32>>CC_SHIFT));
        }
    }
    private static final class PrevArgs{
        char[] src;
        int start;
        int current;
        char c;
        char c2;
    }
    private static long  getPrevNorm32(PrevArgs args,
                                                      int minC,
                                                      int mask) {
        long norm32;
        args.c=args.src[--args.current];
        args.c2=0;
        if(args.c<minC) {
            return 0;
        } else if(!UTF16.isSurrogate(args.c)) {
            return getNorm32(args.c);
        } else if(UTF16.isLeadSurrogate(args.c)) {
            return 0;
        } else if(args.current!=args.start &&
                    UTF16.isLeadSurrogate(args.c2=args.src[args.current-1])) {
            --args.current;
            norm32=getNorm32(args.c2);
            if((norm32&mask)==0) {
                return 0;
            } else {
                return getNorm32FromSurrogatePair(norm32, args.c);
            }
        } else {
            args.c2=0;
            return 0;
        }
    }
    private static int  getPrevCC(PrevArgs args) {
        return (int)((UNSIGNED_BYTE_MASK)&(getPrevNorm32(args, MIN_WITH_LEAD_CC,
                                                         CC_MASK)>>CC_SHIFT));
    }
    public static boolean isNFDSafe(long norm32,
                                     intccOrQCMask,
                                     int decompQCMask) {
        if((norm32&ccOrQCMask)==0) {
            return true; 
        }
        if(isNorm32Regular(norm32) && (norm32&decompQCMask)!=0) {
            DecomposeArgs args=new DecomposeArgs();
            decompose(norm32, decompQCMask, args);
            return args.cc==0;
        } else {
            return (norm32&CC_MASK)==0;
        }
    }
    public static boolean isTrueStarter(long norm32,
                                          int ccOrQCMask,
                                          int decompQCMask) {
        if((norm32&ccOrQCMask)==0) {
            return true; 
        }
        if((norm32&decompQCMask)!=0) {
            int p; 
            DecomposeArgs args=new DecomposeArgs();
            p=decompose(norm32, decompQCMask, args);
            if(args.cc==0) {
                int qcMask=ccOrQCMask&QC_MASK;
                if((getNorm32(extraData,p, qcMask)&qcMask)==0) {
                    return true;
                }
            }
        }
        return false;
    }
    private static int insertOrdered(char[] source,
                                                      int start,
                                                      int current, int p,
                                                         char c, char c2,
                                                         int cc) {
        int back, preBack;
        int r;
        int prevCC, trailCC=cc;
        if(start<current && cc!=0) {
            preBack=back=current;
            PrevArgs prevArgs = new PrevArgs();
            prevArgs.current  = current;
            prevArgs.start    = start;
            prevArgs.src      = source;
            prevCC=getPrevCC(prevArgs);
            preBack = prevArgs.current;
            if(cc<prevCC) {
                trailCC=prevCC;
                back=preBack;
                while(start<preBack) {
                    prevCC=getPrevCC(prevArgs);
                    preBack=prevArgs.current;
                    if(cc>=prevCC) {
                        break;
                    }
                    back=preBack;
                }
                r=p;
                do {
                    source[--r]=source[--current];
                } while(back!=current);
            }
        }
        source[current]=c;
        if(c2!=0) {
            source[(current+1)]=c2;
        }
        return trailCC;
    }
    private static int  mergeOrdered(char[] source,
                                                      int start,
                                                      int current,
                                                      char[] data,
                                                        int next,
                                                        int limit,
                                                        boolean isOrdered) {
            int r;
            int  cc, trailCC=0;
            boolean adjacent;
            adjacent= current==next;
            NextCCArgs ncArgs = new NextCCArgs();
            ncArgs.source = data;
            ncArgs.next   = next;
            ncArgs.limit  = limit;
            if(start!=current || !isOrdered) {
                while(ncArgs.next<ncArgs.limit) {
                    cc=getNextCC(ncArgs);
                    if(cc==0) {
                        trailCC=0;
                        if(adjacent) {
                            current=ncArgs.next;
                        } else {
                            data[current++]=ncArgs.c;
                            if(ncArgs.c2!=0) {
                                data[current++]=ncArgs.c2;
                            }
                        }
                        if(isOrdered) {
                            break;
                        } else {
                            start=current;
                        }
                    } else {
                        r=current+(ncArgs.c2==0 ? 1 : 2);
                        trailCC=insertOrdered(source,start, current, r,
                                              ncArgs.c, ncArgs.c2, cc);
                        current=r;
                    }
                }
            }
            if(ncArgs.next==ncArgs.limit) {
                return trailCC;
            } else {
                if(!adjacent) {
                    do {
                        source[current++]=data[ncArgs.next++];
                    } while(ncArgs.next!=ncArgs.limit);
                    ncArgs.limit=current;
                }
                PrevArgs prevArgs = new PrevArgs();
                prevArgs.src   = data;
                prevArgs.start = start;
                prevArgs.current =  ncArgs.limit;
                return getPrevCC(prevArgs);
            }
    }
    private static int  mergeOrdered(char[] source,
                                                      int start,
                                                      int current,
                                                      char[] data,
                                                        final int next,
                                                        final int limit) {
        return mergeOrdered(source,start,current,data,next,limit,true);
    }
    public static NormalizerBase.QuickCheckResult quickCheck(char[] src,
                                                            int srcStart,
                                                            int srcLimit,
                                                            int minNoMaybe,
                                                            int qcMask,
                                                            int options,
                                                            boolean allowMaybe,
                                                            UnicodeSet nx){
        int ccOrQCMask;
        long norm32;
        char c, c2;
        char cc, prevCC;
        long qcNorm32;
        NormalizerBase.QuickCheckResult result;
        ComposePartArgs args = new ComposePartArgs();
        char[] buffer ;
        int start = srcStart;
        if(!isDataLoaded) {
            return NormalizerBase.MAYBE;
        }
        ccOrQCMask=CC_MASK|qcMask;
        result=NormalizerBase.YES;
        prevCC=0;
        for(;;) {
            for(;;) {
                if(srcStart==srcLimit) {
                    return result;
                } else if((c=src[srcStart++])>=minNoMaybe &&
                                  (( norm32=getNorm32(c)) & ccOrQCMask)!=0) {
                    break;
                }
                prevCC=0;
            }
            if(isNorm32LeadSurrogate(norm32)) {
                if(srcStart!=srcLimit&& UTF16.isTrailSurrogate(c2=src[srcStart])) {
                    ++srcStart;
                    norm32=getNorm32FromSurrogatePair(norm32,c2);
                } else {
                    norm32=0;
                    c2=0;
                }
            }else{
                c2=0;
            }
            if(nx_contains(nx, c, c2)) {
                norm32=0;
            }
            cc=(char)((norm32>>CC_SHIFT)&0xFF);
            if(cc!=0 && cc<prevCC) {
                return NormalizerBase.NO;
            }
            prevCC=cc;
            qcNorm32 = norm32 & qcMask;
            if((qcNorm32& QC_ANY_NO)>=1) {
                result= NormalizerBase.NO;
                break;
            } else if(qcNorm32!=0) {
                if(allowMaybe){
                    result=NormalizerBase.MAYBE;
                }else{
                    int prevStarter;
                    int decompQCMask;
                    decompQCMask=(qcMask<<2)&0xf; 
                    prevStarter=srcStart-1;
                    if(UTF16.isTrailSurrogate(src[prevStarter])) {
                        --prevStarter;
                    }
                    prevStarter=findPreviousStarter(src, start, prevStarter,
                                                    ccOrQCMask, decompQCMask,
                                                    (char)minNoMaybe);
                    srcStart=findNextStarter(src,srcStart, srcLimit, qcMask,
                                             decompQCMask,(char) minNoMaybe);
                    args.prevCC = prevCC;
                    buffer = composePart(args,prevStarter,src,srcStart,srcLimit,options,nx);
                    if(0!=strCompare(buffer,0,args.length,src,prevStarter,srcStart, false)) {
                        result=NormalizerBase.NO; 
                        break;
                    }
                }
            }
        }
        return result;
    }
    public static int decompose(char[] src,int srcStart,int srcLimit,
                                char[] dest,int destStart,int destLimit,
                                 boolean compat,int[] outTrailCC,
                                 UnicodeSet nx) {
        char[] buffer = new char[3];
        int prevSrc;
        long norm32;
        int ccOrQCMask, qcMask;
        int reorderStartIndex, length;
        char c, c2, minNoMaybe;
        int cc, prevCC, trailCC;
        char[] p;
        int pStart;
        int destIndex = destStart;
        int srcIndex = srcStart;
        if(!compat) {
            minNoMaybe=(char)indexes[INDEX_MIN_NFD_NO_MAYBE];
            qcMask=QC_NFD;
        } else {
            minNoMaybe=(char)indexes[INDEX_MIN_NFKD_NO_MAYBE];
            qcMask=QC_NFKD;
        }
        ccOrQCMask=CC_MASK|qcMask;
        reorderStartIndex=0;
        prevCC=0;
        norm32=0;
        c=0;
        pStart=0;
        cc=trailCC=-1;
        for(;;) {
            prevSrc=srcIndex;
            while(srcIndex!=srcLimit &&((c=src[srcIndex])<minNoMaybe ||
                                        ((norm32=getNorm32(c))&ccOrQCMask)==0)){
                prevCC=0;
                ++srcIndex;
            }
            if(srcIndex!=prevSrc) {
                length=(int)(srcIndex-prevSrc);
                if((destIndex+length)<=destLimit) {
                    System.arraycopy(src,prevSrc,dest,destIndex,length);
                }
                destIndex+=length;
                reorderStartIndex=destIndex;
            }
            if(srcIndex==srcLimit) {
                break;
            }
            ++srcIndex;
            if(isNorm32HangulOrJamo(norm32)) {
                if(nx_contains(nx, c)) {
                    c2=0;
                    p=null;
                    length=1;
                } else {
                    p=buffer;
                    pStart=0;
                    cc=trailCC=0;
                    c-=HANGUL_BASE;
                    c2=(char)(c%JAMO_T_COUNT);
                    c/=JAMO_T_COUNT;
                    if(c2>0) {
                        buffer[2]=(char)(JAMO_T_BASE+c2);
                        length=3;
                    } else {
                        length=2;
                    }
                    buffer[1]=(char)(JAMO_V_BASE+c%JAMO_V_COUNT);
                    buffer[0]=(char)(JAMO_L_BASE+c/JAMO_V_COUNT);
                }
            } else {
                if(isNorm32Regular(norm32)) {
                    c2=0;
                    length=1;
                } else {
                    if(srcIndex!=srcLimit &&
                                    UTF16.isTrailSurrogate(c2=src[srcIndex])) {
                        ++srcIndex;
                        length=2;
                        norm32=getNorm32FromSurrogatePair(norm32, c2);
                    } else {
                        c2=0;
                        length=1;
                        norm32=0;
                    }
                }
                if(nx_contains(nx, c, c2)) {
                    cc=trailCC=0;
                    p=null;
                } else if((norm32&qcMask)==0) {
                    cc=trailCC=(int)((UNSIGNED_BYTE_MASK) & (norm32>>CC_SHIFT));
                    p=null;
                    pStart=-1;
                } else {
                    DecomposeArgs arg = new DecomposeArgs();
                    pStart=decompose(norm32, qcMask, arg);
                    p=extraData;
                    length=arg.length;
                    cc=arg.cc;
                    trailCC=arg.trailCC;
                    if(length==1) {
                        c=p[pStart];
                        c2=0;
                        p=null;
                        pStart=-1;
                    }
                }
            }
            if((destIndex+length)<=destLimit) {
                int reorderSplit=destIndex;
                if(p==null) {
                    if(cc!=0 && cc<prevCC) {
                        destIndex+=length;
                        trailCC=insertOrdered(dest,reorderStartIndex,
                                            reorderSplit, destIndex, c, c2, cc);
                    } else {
                        dest[destIndex++]=c;
                        if(c2!=0) {
                            dest[destIndex++]=c2;
                        }
                    }
                } else {
                    if(cc!=0 && cc<prevCC) {
                        destIndex+=length;
                        trailCC=mergeOrdered(dest,reorderStartIndex,
                                          reorderSplit,p, pStart,pStart+length);
                    } else {
                        do {
                            dest[destIndex++]=p[pStart++];
                        } while(--length>0);
                    }
                }
            } else {
                destIndex+=length;
            }
            prevCC=trailCC;
            if(prevCC==0) {
                reorderStartIndex=destIndex;
            }
        }
        outTrailCC[0]=prevCC;
        return destIndex - destStart;
    }
    private static final class NextCombiningArgs{
        char[] source;
        int start;
        char c;
        char c2;
        int combiningIndex;
        char  cc;
    }
    private static int     getNextCombining(NextCombiningArgs args,
                                                    int limit,
                                                    UnicodeSet nx) {
        long norm32;
        int combineFlags;
        args.c=args.source[args.start++];
        norm32=getNorm32(args.c);
        args.c2=0;
        args.combiningIndex=0;
        args.cc=0;
        if((norm32&(CC_MASK|COMBINES_ANY))==0) {
            return 0;
        } else {
            if(isNorm32Regular(norm32)) {
            } else if(isNorm32HangulOrJamo(norm32)) {
                args.combiningIndex=(int)((UNSIGNED_INT_MASK)&(0xfff0|
                                                        (norm32>>EXTRA_SHIFT)));
                return (int)(norm32&COMBINES_ANY);
            } else {
                if(args.start!=limit && UTF16.isTrailSurrogate(args.c2=
                                                     args.source[args.start])) {
                    ++args.start;
                    norm32=getNorm32FromSurrogatePair(norm32, args.c2);
                } else {
                    args.c2=0;
                    return 0;
                }
            }
            if(nx_contains(nx, args.c, args.c2)) {
                return 0; 
            }
            args.cc= (char)((norm32>>CC_SHIFT)&0xff);
            combineFlags=(int)(norm32&COMBINES_ANY);
            if(combineFlags!=0) {
                int index = getExtraDataIndex(norm32);
                args.combiningIndex=index>0 ? extraData[(index-1)] :0;
            }
            return combineFlags;
        }
    }
    private static int getCombiningIndexFromStarter(char c,char c2){
        long norm32;
        norm32=getNorm32(c);
        if(c2!=0) {
            norm32=getNorm32FromSurrogatePair(norm32, c2);
        }
        return extraData[(getExtraDataIndex(norm32)-1)];
    }
    private static int combine(char[]table,int tableStart,
                                   int combineBackIndex,
                                    int[] outValues) {
        int key;
        int value,value2;
        if(outValues.length<2){
            throw new IllegalArgumentException();
        }
        for(;;) {
            key=table[tableStart++];
            if(key>=combineBackIndex) {
                break;
            }
            tableStart+= ((table[tableStart]&0x8000) != 0)? 2 : 1;
        }
        if((key&0x7fff)==combineBackIndex) {
            value=table[tableStart];
            key=(int)((UNSIGNED_INT_MASK)&((value&0x2000)+1));
            if((value&0x8000) != 0) {
                if((value&0x4000) != 0) {
                    value=(int)((UNSIGNED_INT_MASK)&((value&0x3ff)|0xd800));
                    value2=table[tableStart+1];
                } else {
                    value=table[tableStart+1];
                    value2=0;
                }
            } else {
                value&=0x1fff;
                value2=0;
            }
            outValues[0]=value;
            outValues[1]=value2;
            return key;
        } else {
            return 0;
        }
    }
    private static final class RecomposeArgs{
        char[] source;
        int start;
        int limit;
    }
    private static char recompose(RecomposeArgs args, int options, UnicodeSet nx) {
        int  remove, q, r;
        int  combineFlags;
        int  combineFwdIndex, combineBackIndex;
        int  result, value=0, value2=0;
        int   prevCC;
        boolean starterIsSupplementary;
        int starter;
        int[] outValues = new int[2];
        starter=-1;                   
        combineFwdIndex=0;            
        starterIsSupplementary=false; 
        prevCC=0;
        NextCombiningArgs ncArg = new NextCombiningArgs();
        ncArg.source  = args.source;
        ncArg.cc      =0;
        ncArg.c2      =0;
        for(;;) {
            ncArg.start = args.start;
            combineFlags=getNextCombining(ncArg,args.limit,nx);
            combineBackIndex=ncArg.combiningIndex;
            args.start = ncArg.start;
            if(((combineFlags&COMBINES_BACK)!=0) && starter!=-1) {
                if((combineBackIndex&0x8000)!=0) {
                    if((options&BEFORE_PRI_29)!=0 || prevCC==0) {
                        remove=-1; 
                        combineFlags=0;
                        ncArg.c2=args.source[starter];
                        if(combineBackIndex==0xfff2) {
                            ncArg.c2=(char)(ncArg.c2-JAMO_L_BASE);
                            if(ncArg.c2<JAMO_L_COUNT) {
                                remove=args.start-1;
                                ncArg.c=(char)(HANGUL_BASE+(ncArg.c2*JAMO_V_COUNT+
                                               (ncArg.c-JAMO_V_BASE))*JAMO_T_COUNT);
                                if(args.start!=args.limit &&
                                            (ncArg.c2=(char)(args.source[args.start]
                                             -JAMO_T_BASE))<JAMO_T_COUNT) {
                                    ++args.start;
                                    ncArg.c+=ncArg.c2;
                                 } else {
                                     combineFlags=COMBINES_FWD;
                                }
                                if(!nx_contains(nx, ncArg.c)) {
                                    args.source[starter]=ncArg.c;
                                   } else {
                                    if(!isHangulWithoutJamoT(ncArg.c)) {
                                        --args.start; 
                                    }
                                    remove=args.start;
                                }
                            }
                        } else {
                            if(isHangulWithoutJamoT(ncArg.c2)) {
                                ncArg.c2+=ncArg.c-JAMO_T_BASE;
                                if(!nx_contains(nx, ncArg.c2)) {
                                    remove=args.start-1;
                                    args.source[starter]=ncArg.c2;
                                }
                            }
                        }
                        if(remove!=-1) {
                            q=remove;
                            r=args.start;
                            while(r<args.limit) {
                                args.source[q++]=args.source[r++];
                            }
                            args.start=remove;
                            args.limit=q;
                        }
                        ncArg.c2=0; 
                        if(combineFlags!=0) {
                            if(args.start==args.limit) {
                                return (char)prevCC;
                            }
                            combineFwdIndex=0xfff0;
                            continue;
                        }
                    }
                } else if(
                    !((combineFwdIndex&0x8000)!=0) &&
                    ((options&BEFORE_PRI_29)!=0 ?
                        (prevCC!=ncArg.cc || prevCC==0) :
                        (prevCC<ncArg.cc || prevCC==0)) &&
                    0!=(result=combine(combiningTable,combineFwdIndex,
                                       combineBackIndex, outValues)) &&
                    !nx_contains(nx, (char)value, (char)value2)
                ) {
                    value=outValues[0];
                    value2=outValues[1];
                    remove= ncArg.c2==0 ? args.start-1 : args.start-2; 
                    args.source[starter]=(char)value;
                    if(starterIsSupplementary) {
                        if(value2!=0) {
                            args.source[starter+1]=(char)value2;
                        } else {
                            starterIsSupplementary=false;
                            q=starter+1;
                            r=q+1;
                            while(r<remove) {
                                args.source[q++]=args.source[r++];
                            }
                            --remove;
                        }
                    } else if(value2!=0) { 
                        starterIsSupplementary=true;
                        args.source[starter+1]=(char)value2;
                    }
                    if(remove<args.start) {
                        q=remove;
                        r=args.start;
                        while(r<args.limit) {
                            args.source[q++]=args.source[r++];
                        }
                        args.start=remove;
                        args.limit=q;
                    }
                    if(args.start==args.limit) {
                        return (char)prevCC;
                    }
                    if(result>1) {
                       combineFwdIndex=getCombiningIndexFromStarter((char)value,
                                                                  (char)value2);
                    } else {
                       starter=-1;
                    }
                    continue;
                }
            }
            prevCC=ncArg.cc;
            if(args.start==args.limit) {
                return (char)prevCC;
            }
            if(ncArg.cc==0) {
                if((combineFlags&COMBINES_FWD)!=0) {
                    if(ncArg.c2==0) {
                        starterIsSupplementary=false;
                        starter=args.start-1;
                    } else {
                        starterIsSupplementary=false;
                        starter=args.start-2;
                    }
                    combineFwdIndex=combineBackIndex;
                } else {
                    starter=-1;
                }
            } else if((options&OPTIONS_COMPOSE_CONTIGUOUS)!=0) {
                starter=-1;
            }
        }
    }
    private static int findPreviousStarter(char[]src, int srcStart, int current,
                                          int ccOrQCMask,
                                          int decompQCMask,
                                          char minNoMaybe) {
       long norm32;
       PrevArgs args = new PrevArgs();
       args.src = src;
       args.start = srcStart;
       args.current = current;
       while(args.start<args.current) {
           norm32= getPrevNorm32(args, minNoMaybe, ccOrQCMask|decompQCMask);
           if(isTrueStarter(norm32, ccOrQCMask, decompQCMask)) {
               break;
           }
       }
       return args.current;
    }
    private static int    findNextStarter(char[] src,int start,int limit,
                                                 int qcMask,
                                                 int decompQCMask,
                                                 char minNoMaybe) {
        int p;
        long norm32;
        int ccOrQCMask;
        char c, c2;
        ccOrQCMask=CC_MASK|qcMask;
        DecomposeArgs decompArgs = new DecomposeArgs();
        for(;;) {
            if(start==limit) {
                break; 
            }
            c=src[start];
            if(c<minNoMaybe) {
                break; 
            }
            norm32=getNorm32(c);
            if((norm32&ccOrQCMask)==0) {
                break; 
            }
            if(isNorm32LeadSurrogate(norm32)) {
                if((start+1)==limit ||
                                   !UTF16.isTrailSurrogate(c2=(src[start+1]))){
                    break;
                }
                norm32=getNorm32FromSurrogatePair(norm32, c2);
                if((norm32&ccOrQCMask)==0) {
                    break; 
                }
            } else {
                c2=0;
            }
            if((norm32&decompQCMask)!=0) {
                p=decompose(norm32, decompQCMask, decompArgs);
                if(decompArgs.cc==0 && (getNorm32(extraData,p, qcMask)&qcMask)==0) {
                    break; 
                }
            }
            start+= c2==0 ? 1 : 2; 
        }
        return start;
    }
    private static final class ComposePartArgs{
        int prevCC;
        int length;   
    }
    private static char[] composePart(ComposePartArgs args,
                                      int prevStarter,
                                         char[] src, int start, int limit,
                                       int options,
                                       UnicodeSet nx) {
        int recomposeLimit;
        boolean compat =((options&OPTIONS_COMPAT)!=0);
        int[] outTrailCC = new int[1];
        char[] buffer = new char[(limit-prevStarter)*MAX_BUFFER_SIZE];
        for(;;){
            args.length=decompose(src,prevStarter,(start),
                                      buffer,0,buffer.length,
                                      compat,outTrailCC,nx);
            if(args.length<=buffer.length){
                break;
            }else{
                buffer = new char[args.length];
            }
        }
        recomposeLimit=args.length;
        if(args.length>=2) {
            RecomposeArgs rcArgs = new RecomposeArgs();
            rcArgs.source    = buffer;
            rcArgs.start    = 0;
            rcArgs.limit    = recomposeLimit;
            args.prevCC=recompose(rcArgs, options, nx);
            recomposeLimit = rcArgs.limit;
        }
        args.length=recomposeLimit;
        return buffer;
    }
    private static boolean composeHangul(char prev, char c,
                                         long norm32,
                                         char[] src,int[] srcIndex, int limit,
                                            boolean compat,
                                         char[] dest,int destIndex,
                                         UnicodeSet nx) {
        int start=srcIndex[0];
        if(isJamoVTNorm32JamoV(norm32)) {
            prev=(char)(prev-JAMO_L_BASE);
            if(prev<JAMO_L_COUNT) {
                c=(char)(HANGUL_BASE+(prev*JAMO_V_COUNT+
                                                 (c-JAMO_V_BASE))*JAMO_T_COUNT);
                if(start!=limit) {
                    char next, t;
                    next=src[start];
                    if((t=(char)(next-JAMO_T_BASE))<JAMO_T_COUNT) {
                        ++start;
                        c+=t;
                    } else if(compat) {
                        norm32=getNorm32(next);
                        if(isNorm32Regular(norm32) && ((norm32&QC_NFKD)!=0)) {
                            int p ;
                            DecomposeArgs dcArgs = new DecomposeArgs();
                            p=decompose(norm32, QC_NFKD, dcArgs);
                            if(dcArgs.length==1 &&
                                   (t=(char)(extraData[p]-JAMO_T_BASE))
                                                   <JAMO_T_COUNT) {
                                ++start;
                                c+=t;
                            }
                        }
                    }
                }
                if(nx_contains(nx, c)) {
                    if(!isHangulWithoutJamoT(c)) {
                        --start; 
                    }
                    return false;
                }
                dest[destIndex]=c;
                srcIndex[0]=start;
                return true;
            }
        } else if(isHangulWithoutJamoT(prev)) {
            c=(char)(prev+(c-JAMO_T_BASE));
            if(nx_contains(nx, c)) {
                return false;
            }
            dest[destIndex]=c;
            srcIndex[0]=start;
            return true;
        }
        return false;
    }
    public static int compose(char[] src, int srcStart, int srcLimit,
                              char[] dest,int destStart,int destLimit,
                              int options,UnicodeSet nx) {
        int prevSrc, prevStarter;
        long norm32;
        int ccOrQCMask, qcMask;
        int  reorderStartIndex, length;
        char c, c2, minNoMaybe;
        int cc, prevCC;
        int[] ioIndex = new int[1];
        int destIndex = destStart;
        int srcIndex = srcStart;
        if((options&OPTIONS_COMPAT)!=0) {
            minNoMaybe=(char)indexes[INDEX_MIN_NFKC_NO_MAYBE];
            qcMask=QC_NFKC;
        } else {
            minNoMaybe=(char)indexes[INDEX_MIN_NFC_NO_MAYBE];
            qcMask=QC_NFC;
        }
        prevStarter=srcIndex;
        ccOrQCMask=CC_MASK|qcMask;
        reorderStartIndex=0;
        prevCC=0;
        norm32=0;
        c=0;
        for(;;) {
            prevSrc=srcIndex;
            while(srcIndex!=srcLimit && ((c=src[srcIndex])<minNoMaybe ||
                     ((norm32=getNorm32(c))&ccOrQCMask)==0)) {
                prevCC=0;
                ++srcIndex;
            }
            if(srcIndex!=prevSrc) {
                length=(int)(srcIndex-prevSrc);
                if((destIndex+length)<=destLimit) {
                    System.arraycopy(src,prevSrc,dest,destIndex,length);
                }
                destIndex+=length;
                reorderStartIndex=destIndex;
                prevStarter=srcIndex-1;
                if(UTF16.isTrailSurrogate(src[prevStarter]) &&
                    prevSrc<prevStarter &&
                    UTF16.isLeadSurrogate(src[(prevStarter-1)])) {
                    --prevStarter;
                }
                prevSrc=srcIndex;
            }
            if(srcIndex==srcLimit) {
                break;
            }
            ++srcIndex;
            if(isNorm32HangulOrJamo(norm32)) {
                prevCC=cc=0;
                reorderStartIndex=destIndex;
                ioIndex[0]=srcIndex;
                if(
                    destIndex>0 &&
                    composeHangul(src[(prevSrc-1)], c, norm32,src, ioIndex,
                                  srcLimit, (options&OPTIONS_COMPAT)!=0, dest,
                                  destIndex<=destLimit ? destIndex-1: 0,
                                  nx)
                ) {
                    srcIndex=ioIndex[0];
                    prevStarter=srcIndex;
                    continue;
                }
                srcIndex = ioIndex[0];
                c2=0;
                length=1;
                prevStarter=prevSrc;
            } else {
                if(isNorm32Regular(norm32)) {
                    c2=0;
                    length=1;
                } else {
                    if(srcIndex!=srcLimit &&
                                     UTF16.isTrailSurrogate(c2=src[srcIndex])) {
                        ++srcIndex;
                        length=2;
                        norm32=getNorm32FromSurrogatePair(norm32, c2);
                    } else {
                        c2=0;
                        length=1;
                        norm32=0;
                    }
                }
                ComposePartArgs args =new ComposePartArgs();
                if(nx_contains(nx, c, c2)) {
                    cc=0;
                } else if((norm32&qcMask)==0) {
                    cc=(int)((UNSIGNED_BYTE_MASK)&(norm32>>CC_SHIFT));
                } else {
                    char[] p;
                    int decompQCMask=(qcMask<<2)&0xf; 
                    if(isTrueStarter(norm32, CC_MASK|qcMask, decompQCMask)) {
                        prevStarter=prevSrc;
                    } else {
                        destIndex-=prevSrc-prevStarter;
                    }
                    srcIndex=findNextStarter(src, srcIndex,srcLimit, qcMask,
                                               decompQCMask, minNoMaybe);
                    args.prevCC    = prevCC;
                    args.length = length;
                    p=composePart(args,prevStarter,src,srcIndex,srcLimit,options,nx);
                    if(p==null) {
                        break;
                    }
                    prevCC      = args.prevCC;
                    length      = args.length;
                    if((destIndex+args.length)<=destLimit) {
                        int i=0;
                        while(i<args.length) {
                            dest[destIndex++]=p[i++];
                            --length;
                        }
                    } else {
                        destIndex+=length;
                    }
                    prevStarter=srcIndex;
                    continue;
                }
            }
            if((destIndex+length)<=destLimit) {
                if(cc!=0 && cc<prevCC) {
                    int reorderSplit= destIndex;
                    destIndex+=length;
                    prevCC=insertOrdered(dest,reorderStartIndex, reorderSplit,
                                         destIndex, c, c2, cc);
                } else {
                    dest[destIndex++]=c;
                    if(c2!=0) {
                        dest[destIndex++]=c2;
                    }
                    prevCC=cc;
                }
            } else {
                destIndex+=length;
                prevCC=cc;
            }
        }
        return destIndex - destStart;
    }
    public static int getCombiningClass(int c) {
        long norm32;
        norm32=getNorm32(c);
        return (char)((norm32>>CC_SHIFT)&0xFF);
    }
    public static boolean isFullCompositionExclusion(int c) {
        if(isFormatVersion_2_1) {
            int aux =AuxTrieImpl.auxTrie.getCodePointValue(c);
            return (boolean)((aux & AUX_COMP_EX_MASK)!=0);
        } else {
            return false;
        }
    }
    public static boolean isCanonSafeStart(int c) {
        if(isFormatVersion_2_1) {
            int aux = AuxTrieImpl.auxTrie.getCodePointValue(c);
            return (boolean)((aux & AUX_UNSAFE_MASK)==0);
        } else {
            return false;
        }
    }
    public static boolean isNFSkippable(int c, NormalizerBase.Mode mode, long mask) {
        long  norm32;
        mask = mask & UNSIGNED_INT_MASK;
        char aux;
        norm32 = getNorm32(c);
        if((norm32&mask)!=0) {
            return false; 
        }
        if(mode == NormalizerBase.NFD || mode == NormalizerBase.NFKD || mode == NormalizerBase.NONE){
            return true; 
        }
        if((norm32& QC_NFD)==0) {
            return true; 
        }
        if(isNorm32HangulOrJamo(norm32)) {
            return !isHangulWithoutJamoT((char)c); 
        }
        if(!isFormatVersion_2_2) {
            return false; 
        }
        aux = AuxTrieImpl.auxTrie.getCodePointValue(c);
        return (aux&AUX_NFC_SKIP_F_MASK)==0; 
    }
    public static UnicodeSet addPropertyStarts(UnicodeSet set) {
        int c;
        TrieIterator normIter = new TrieIterator(NormTrieImpl.normTrie);
        RangeValueIterator.Element normResult = new RangeValueIterator.Element();
        while(normIter.next(normResult)){
            set.add(normResult.start);
        }
        TrieIterator fcdIter  = new TrieIterator(FCDTrieImpl.fcdTrie);
        RangeValueIterator.Element fcdResult = new RangeValueIterator.Element();
        while(fcdIter.next(fcdResult)){
            set.add(fcdResult.start);
        }
        if(isFormatVersion_2_1){
            TrieIterator auxIter  = new TrieIterator(AuxTrieImpl.auxTrie);
            RangeValueIterator.Element auxResult = new RangeValueIterator.Element();
            while(auxIter.next(auxResult)){
                set.add(auxResult.start);
            }
        }
        for(c=HANGUL_BASE; c<HANGUL_BASE+HANGUL_COUNT; c+=JAMO_T_COUNT) {
            set.add(c);
            set.add(c+1);
        }
        set.add(HANGUL_BASE+HANGUL_COUNT); 
        return set; 
    }
    public static final int quickCheck(int c, int modeValue) {
        final int qcMask[]={
            0, 0, QC_NFD, QC_NFKD, QC_NFC, QC_NFKC
        };
        int norm32=(int)getNorm32(c)&qcMask[modeValue];
        if(norm32==0) {
            return 1; 
        } else if((norm32&QC_ANY_NO)!=0) {
            return 0; 
        } else  {
            return 2; 
        }
    }
    private static int strCompare(char[] s1, int s1Start, int s1Limit,
                                  char[] s2, int s2Start, int s2Limit,
                                  boolean codePointOrder) {
        int start1, start2, limit1, limit2;
        char c1, c2;
        start1=s1Start;
        start2=s2Start;
        int length1, length2;
        length1 = s1Limit - s1Start;
        length2 = s2Limit - s2Start;
        int lengthResult;
        if(length1<length2) {
            lengthResult=-1;
            limit1=start1+length1;
        } else if(length1==length2) {
            lengthResult=0;
            limit1=start1+length1;
        } else  {
            lengthResult=1;
            limit1=start1+length2;
        }
        if(s1==s2) {
            return lengthResult;
        }
        for(;;) {
            if(s1Start==limit1) {
                return lengthResult;
            }
            c1=s1[s1Start];
            c2=s2[s2Start];
            if(c1!=c2) {
                break;
            }
            ++s1Start;
            ++s2Start;
        }
        limit1=start1+length1;
        limit2=start2+length2;
        if(c1>=0xd800 && c2>=0xd800 && codePointOrder) {
            if(
                ( c1<=0xdbff && (s1Start+1)!=limit1 &&
                  UTF16.isTrailSurrogate(s1[(s1Start+1)])
                ) ||
                ( UTF16.isTrailSurrogate(c1) && start1!=s1Start &&
                  UTF16.isLeadSurrogate(s1[(s1Start-1)])
                )
            ) {
            } else {
                c1-=0x2800;
            }
            if(
                ( c2<=0xdbff && (s2Start+1)!=limit2 &&
                  UTF16.isTrailSurrogate(s2[(s2Start+1)])
                ) ||
                ( UTF16.isTrailSurrogate(c2) && start2!=s2Start &&
                  UTF16.isLeadSurrogate(s2[(s2Start-1)])
                )
            ) {
            } else {
                c2-=0x2800;
            }
        }
        return (int)c1-(int)c2;
    }
    private static final int OPTIONS_NX_MASK=0x1f;
    private static final int OPTIONS_UNICODE_MASK=0xe0;
    public  static final int OPTIONS_SETS_MASK=0xff;
    private static final UnicodeSet[] nxCache = new UnicodeSet[OPTIONS_SETS_MASK+1];
    private static final int NX_HANGUL = 1;
    private static final int NX_CJK_COMPAT=2;
    public static final int BEFORE_PRI_29=0x100;
    public static final int OPTIONS_COMPAT=0x1000;
    public static final int OPTIONS_COMPOSE_CONTIGUOUS=0x2000;
    private static final synchronized UnicodeSet internalGetNXHangul() {
        if(nxCache[NX_HANGUL]==null) {
             nxCache[NX_HANGUL]=new UnicodeSet(0xac00, 0xd7a3);
        }
        return nxCache[NX_HANGUL];
    }
    private static final synchronized UnicodeSet internalGetNXCJKCompat() {
        if(nxCache[NX_CJK_COMPAT]==null) {
            UnicodeSet set, hasDecomp;
            set=new UnicodeSet("[:Ideographic:]");
            hasDecomp=new UnicodeSet();
            UnicodeSetIterator it = new UnicodeSetIterator(set);
            int start, end;
            long norm32;
            while(it.nextRange() && (it.codepoint != UnicodeSetIterator.IS_STRING)) {
                start=it.codepoint;
                end=it.codepointEnd;
                while(start<=end) {
                    norm32 = getNorm32(start);
                    if((norm32 & QC_NFD)>0) {
                        hasDecomp.add(start);
                    }
                    ++start;
                }
            }
             nxCache[NX_CJK_COMPAT]=hasDecomp;
        }
        return nxCache[NX_CJK_COMPAT];
    }
    private static final synchronized UnicodeSet internalGetNXUnicode(int options) {
        options &= OPTIONS_UNICODE_MASK;
        if(options==0) {
            return null;
        }
        if(nxCache[options]==null) {
            UnicodeSet set = new UnicodeSet();
            switch(options) {
            case NormalizerBase.UNICODE_3_2:
                set.applyPattern("[:^Age=3.2:]");
                break;
            default:
                return null;
            }
            nxCache[options]=set;
        }
        return nxCache[options];
    }
    private static final synchronized UnicodeSet internalGetNX(int options) {
        options&=OPTIONS_SETS_MASK;
        if(nxCache[options]==null) {
            if(options==NX_HANGUL) {
                return internalGetNXHangul();
            }
            if(options==NX_CJK_COMPAT) {
                return internalGetNXCJKCompat();
            }
            if((options & OPTIONS_UNICODE_MASK)!=0 && (options & OPTIONS_NX_MASK)==0) {
                return internalGetNXUnicode(options);
            }
            UnicodeSet set;
            UnicodeSet other;
            set=new UnicodeSet();
            if((options & NX_HANGUL)!=0 && null!=(other=internalGetNXHangul())) {
                set.addAll(other);
            }
            if((options&NX_CJK_COMPAT)!=0 && null!=(other=internalGetNXCJKCompat())) {
                set.addAll(other);
            }
            if((options&OPTIONS_UNICODE_MASK)!=0 && null!=(other=internalGetNXUnicode(options))) {
                set.addAll(other);
            }
               nxCache[options]=set;
        }
        return nxCache[options];
    }
    public static final UnicodeSet getNX(int options) {
        if((options&=OPTIONS_SETS_MASK)==0) {
            return null;
        } else {
            return internalGetNX(options);
        }
    }
    private static final boolean nx_contains(UnicodeSet nx, int c) {
        return nx!=null && nx.contains(c);
    }
    private static final boolean nx_contains(UnicodeSet nx, char c, char c2) {
        return nx!=null && nx.contains(c2==0 ? c : UCharacterProperty.getRawSupplementary(c, c2));
    }
    public static int getDecompose(int chars[], String decomps[]) {
        DecomposeArgs args = new DecomposeArgs();
        int length=0;
        long norm32 = 0;
        int ch = -1;
        int index = 0;
        int i = 0;
        while (++ch < 0x2fa1e) {   
            if (ch == 0x30ff)
                ch = 0xf900;
            else if (ch == 0x10000)
                ch = 0x1d15e;
            else if (ch == 0x1d1c1)
                ch = 0x2f800;
            norm32 = NormalizerImpl.getNorm32(ch);
            if((norm32 & QC_NFD)!=0 && i < chars.length) {
                chars[i] = ch;
                index = decompose(norm32, args);
                decomps[i++] = new String(extraData,index, args.length);
            }
        }
        return i;
    }
    private static boolean needSingleQuotation(char c) {
        return (c >= 0x0009 && c <= 0x000D) ||
               (c >= 0x0020 && c <= 0x002F) ||
               (c >= 0x003A && c <= 0x0040) ||
               (c >= 0x005B && c <= 0x0060) ||
               (c >= 0x007B && c <= 0x007E);
    }
    public static String canonicalDecomposeWithSingleQuotation(String string) {
        char[] src = string.toCharArray();
        int    srcIndex = 0;
        int    srcLimit = src.length;
        char[] dest = new char[src.length * 3];  
        int    destIndex = 0;
        int    destLimit = dest.length;
        char[] buffer = new char[3];
        int prevSrc;
        long norm32;
        int ccOrQCMask;
        int qcMask = QC_NFD;
        int reorderStartIndex, length;
        char c, c2;
        char minNoMaybe = (char)indexes[INDEX_MIN_NFD_NO_MAYBE];
        int cc, prevCC, trailCC;
        char[] p;
        int pStart;
        ccOrQCMask = CC_MASK | qcMask;
        reorderStartIndex = 0;
        prevCC = 0;
        norm32 = 0;
        c = 0;
        pStart = 0;
        cc = trailCC = -1; 
        for(;;) {
            prevSrc=srcIndex;
            while (srcIndex != srcLimit &&
                   (( c = src[srcIndex]) < minNoMaybe ||
                    ((norm32 = getNorm32(c)) & ccOrQCMask) == 0 ||
                    ( c >= '\uac00' && c <= '\ud7a3'))){
                prevCC = 0;
                ++srcIndex;
            }
            if (srcIndex != prevSrc) {
                length = (int)(srcIndex - prevSrc);
                if ((destIndex + length) <= destLimit) {
                    System.arraycopy(src,prevSrc,dest,destIndex,length);
                }
                destIndex += length;
                reorderStartIndex = destIndex;
            }
            if(srcIndex == srcLimit) {
                break;
            }
            ++srcIndex;
            if(isNorm32Regular(norm32)) {
                c2 = 0;
                length = 1;
            } else {
                if(srcIndex != srcLimit &&
                    Character.isLowSurrogate(c2 = src[srcIndex])) {
                        ++srcIndex;
                        length = 2;
                        norm32 = getNorm32FromSurrogatePair(norm32, c2);
                } else {
                    c2 = 0;
                    length = 1;
                    norm32 = 0;
                }
            }
            if((norm32 & qcMask) == 0) {
                cc = trailCC = (int)((UNSIGNED_BYTE_MASK) & (norm32 >> CC_SHIFT));
                p = null;
                pStart = -1;
            } else {
                DecomposeArgs arg = new DecomposeArgs();
                pStart = decompose(norm32, qcMask, arg);
                p = extraData;
                length = arg.length;
                cc = arg.cc;
                trailCC = arg.trailCC;
                if(length == 1) {
                    c = p[pStart];
                    c2 = 0;
                    p = null;
                    pStart = -1;
                }
            }
            if((destIndex + length * 3) >= destLimit) {  
                char[] tmpBuf = new char[destLimit * 2];
                System.arraycopy(dest, 0, tmpBuf, 0, destIndex);
                dest = tmpBuf;
                destLimit = dest.length;
            }
            {
                int reorderSplit = destIndex;
                if(p == null) {
                    if (needSingleQuotation(c)) {
                        dest[destIndex++] = '\'';
                        dest[destIndex++] = c;
                        dest[destIndex++] = '\'';
                        trailCC = 0;
                    } else if(cc != 0 && cc < prevCC) {
                        destIndex += length;
                        trailCC = insertOrdered(dest,reorderStartIndex,
                                                reorderSplit, destIndex, c, c2, cc);
                    } else {
                        dest[destIndex++] = c;
                        if(c2 != 0) {
                            dest[destIndex++] = c2;
                        }
                    }
                } else {
                    if (needSingleQuotation(p[pStart])) {
                        dest[destIndex++] = '\'';
                        dest[destIndex++] = p[pStart++];
                        dest[destIndex++] = '\'';
                        length--;
                        do {
                            dest[destIndex++] = p[pStart++];
                        } while(--length > 0);
                    } else
                    if(cc != 0 && cc < prevCC) {
                        destIndex += length;
                        trailCC = mergeOrdered(dest,reorderStartIndex,
                                               reorderSplit,p, pStart,pStart+length);
                    } else {
                        do {
                            dest[destIndex++] = p[pStart++];
                        } while(--length > 0);
                    }
                }
            }
            prevCC = trailCC;
            if(prevCC == 0) {
                reorderStartIndex = destIndex;
            }
        }
        return new String(dest, 0, destIndex);
    }
    public static final int WITHOUT_CORRIGENDUM4_CORRECTIONS=0x40000;
    private static final char[][] corrigendum4MappingTable = {
        {'\uD844', '\uDF6A'},  
        {'\u5F33'},            
        {'\u43AB'},            
        {'\u7AAE'},            
        {'\u4D57'}};           
    public static String convert(String str) {
        if (str == null) {
            return null;
        }
        int ch  = UCharacterIterator.DONE;
        StringBuffer dest = new StringBuffer();
        UCharacterIterator iter = UCharacterIterator.getInstance(str);
        while ((ch=iter.nextCodePoint())!= UCharacterIterator.DONE){
            switch (ch) {
            case 0x2F868:
                dest.append(corrigendum4MappingTable[0]);
                break;
            case 0x2F874:
                dest.append(corrigendum4MappingTable[1]);
                break;
            case 0x2F91F:
                dest.append(corrigendum4MappingTable[2]);
                break;
            case 0x2F95F:
                dest.append(corrigendum4MappingTable[3]);
                break;
            case 0x2F9BF:
                dest.append(corrigendum4MappingTable[4]);
                break;
            default:
                UTF16.append(dest,ch);
                break;
            }
        }
        return dest.toString();
    }
}
