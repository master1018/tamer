public final class NormalizerBase implements Cloneable {
    private char[] buffer = new char[100];
    private int bufferStart = 0;
    private int bufferPos   = 0;
    private int bufferLimit = 0;
    private UCharacterIterator  text;
    private Mode                mode = NFC;
    private int                 options = 0;
    private int                 currentIndex;
    private int                 nextIndex;
    public static final int UNICODE_3_2=0x20;
    public static final int DONE = UCharacterIterator.DONE;
    public static class Mode {
        private int modeValue;
        private Mode(int value) {
            modeValue = value;
        }
        protected int normalize(char[] src, int srcStart, int srcLimit,
                                char[] dest,int destStart,int destLimit,
                                UnicodeSet nx) {
            int srcLen = (srcLimit - srcStart);
            int destLen = (destLimit - destStart);
            if( srcLen > destLen ) {
                return srcLen;
            }
            System.arraycopy(src,srcStart,dest,destStart,srcLen);
            return srcLen;
        }
        protected int normalize(char[] src, int srcStart, int srcLimit,
                                char[] dest,int destStart,int destLimit,
                                int options) {
            return normalize(   src, srcStart, srcLimit,
                                dest,destStart,destLimit,
                                NormalizerImpl.getNX(options)
                                );
        }
        protected String normalize(String src, int options) {
            return src;
        }
        protected int getMinC() {
            return -1;
        }
        protected int getMask() {
            return -1;
        }
        protected IsPrevBoundary getPrevBoundary() {
            return null;
        }
        protected IsNextBoundary getNextBoundary() {
            return null;
        }
        protected QuickCheckResult quickCheck(char[] src,int start, int limit,
                                              boolean allowMaybe,UnicodeSet nx) {
            if(allowMaybe) {
                return MAYBE;
            }
            return NO;
        }
        protected boolean isNFSkippable(int c) {
            return true;
        }
    }
    public static final Mode NONE = new Mode(1);
    public static final Mode NFD = new NFDMode(2);
    private static final class NFDMode extends Mode {
        private NFDMode(int value) {
            super(value);
        }
        protected int normalize(char[] src, int srcStart, int srcLimit,
                                char[] dest,int destStart,int destLimit,
                                UnicodeSet nx) {
            int[] trailCC = new int[1];
            return NormalizerImpl.decompose(src,  srcStart,srcLimit,
                                            dest, destStart,destLimit,
                                            false, trailCC,nx);
        }
        protected String normalize( String src, int options) {
            return decompose(src,false,options);
        }
        protected int getMinC() {
            return NormalizerImpl.MIN_WITH_LEAD_CC;
        }
        protected IsPrevBoundary getPrevBoundary() {
            return new IsPrevNFDSafe();
        }
        protected IsNextBoundary getNextBoundary() {
            return new IsNextNFDSafe();
        }
        protected int getMask() {
            return (NormalizerImpl.CC_MASK|NormalizerImpl.QC_NFD);
        }
        protected QuickCheckResult quickCheck(char[] src,int start,
                                              int limit,boolean allowMaybe,
                                              UnicodeSet nx) {
            return NormalizerImpl.quickCheck(
                                             src, start,limit,
                                             NormalizerImpl.getFromIndexesArr(
                                                                              NormalizerImpl.INDEX_MIN_NFD_NO_MAYBE
                                                                              ),
                                             NormalizerImpl.QC_NFD,
                                             0,
                                             allowMaybe,
                                             nx
                                             );
        }
        protected boolean isNFSkippable(int c) {
            return NormalizerImpl.isNFSkippable(c,this,
                                                (NormalizerImpl.CC_MASK|NormalizerImpl.QC_NFD)
                                                );
        }
    }
    public static final Mode NFKD = new NFKDMode(3);
    private static final class NFKDMode extends Mode {
        private NFKDMode(int value) {
            super(value);
        }
        protected int normalize(char[] src, int srcStart, int srcLimit,
                                char[] dest,int destStart,int destLimit,
                                UnicodeSet nx) {
            int[] trailCC = new int[1];
            return NormalizerImpl.decompose(src,  srcStart,srcLimit,
                                            dest, destStart,destLimit,
                                            true, trailCC, nx);
        }
        protected String normalize( String src, int options) {
            return decompose(src,true,options);
        }
        protected int getMinC() {
            return NormalizerImpl.MIN_WITH_LEAD_CC;
        }
        protected IsPrevBoundary getPrevBoundary() {
            return new IsPrevNFDSafe();
        }
        protected IsNextBoundary getNextBoundary() {
            return new IsNextNFDSafe();
        }
        protected int getMask() {
            return (NormalizerImpl.CC_MASK|NormalizerImpl.QC_NFKD);
        }
        protected QuickCheckResult quickCheck(char[] src,int start,
                                              int limit,boolean allowMaybe,
                                              UnicodeSet nx) {
            return NormalizerImpl.quickCheck(
                                             src,start,limit,
                                             NormalizerImpl.getFromIndexesArr(
                                                                              NormalizerImpl.INDEX_MIN_NFKD_NO_MAYBE
                                                                              ),
                                             NormalizerImpl.QC_NFKD,
                                             NormalizerImpl.OPTIONS_COMPAT,
                                             allowMaybe,
                                             nx
                                             );
        }
        protected boolean isNFSkippable(int c) {
            return NormalizerImpl.isNFSkippable(c, this,
                                                (NormalizerImpl.CC_MASK|NormalizerImpl.QC_NFKD)
                                                );
        }
    }
    public static final Mode NFC = new NFCMode(4);
    private static final class NFCMode extends Mode{
        private NFCMode(int value) {
            super(value);
        }
        protected int normalize(char[] src, int srcStart, int srcLimit,
                                char[] dest,int destStart,int destLimit,
                                UnicodeSet nx) {
            return NormalizerImpl.compose( src, srcStart, srcLimit,
                                           dest,destStart,destLimit,
                                           0, nx);
        }
        protected String normalize( String src, int options) {
            return compose(src, false, options);
        }
        protected int getMinC() {
            return NormalizerImpl.getFromIndexesArr(
                                                    NormalizerImpl.INDEX_MIN_NFC_NO_MAYBE
                                                    );
        }
        protected IsPrevBoundary getPrevBoundary() {
            return new IsPrevTrueStarter();
        }
        protected IsNextBoundary getNextBoundary() {
            return new IsNextTrueStarter();
        }
        protected int getMask() {
            return (NormalizerImpl.CC_MASK|NormalizerImpl.QC_NFC);
        }
        protected QuickCheckResult quickCheck(char[] src,int start,
                                              int limit,boolean allowMaybe,
                                              UnicodeSet nx) {
            return NormalizerImpl.quickCheck(
                                             src,start,limit,
                                             NormalizerImpl.getFromIndexesArr(
                                                                              NormalizerImpl.INDEX_MIN_NFC_NO_MAYBE
                                                                              ),
                                             NormalizerImpl.QC_NFC,
                                             0,
                                             allowMaybe,
                                             nx
                                             );
        }
        protected boolean isNFSkippable(int c) {
            return NormalizerImpl.isNFSkippable(c,this,
                                                ( NormalizerImpl.CC_MASK|NormalizerImpl.COMBINES_ANY|
                                                  (NormalizerImpl.QC_NFC & NormalizerImpl.QC_ANY_NO)
                                                  )
                                                );
        }
    };
    public static final Mode NFKC =new NFKCMode(5);
    private static final class NFKCMode extends Mode{
        private NFKCMode(int value) {
            super(value);
        }
        protected int normalize(char[] src, int srcStart, int srcLimit,
                                char[] dest,int destStart,int destLimit,
                                UnicodeSet nx) {
            return NormalizerImpl.compose(src,  srcStart,srcLimit,
                                          dest, destStart,destLimit,
                                          NormalizerImpl.OPTIONS_COMPAT, nx);
        }
        protected String normalize( String src, int options) {
            return compose(src, true, options);
        }
        protected int getMinC() {
            return NormalizerImpl.getFromIndexesArr(
                                                    NormalizerImpl.INDEX_MIN_NFKC_NO_MAYBE
                                                    );
        }
        protected IsPrevBoundary getPrevBoundary() {
            return new IsPrevTrueStarter();
        }
        protected IsNextBoundary getNextBoundary() {
            return new IsNextTrueStarter();
        }
        protected int getMask() {
            return (NormalizerImpl.CC_MASK|NormalizerImpl.QC_NFKC);
        }
        protected QuickCheckResult quickCheck(char[] src,int start,
                                              int limit,boolean allowMaybe,
                                              UnicodeSet nx) {
            return NormalizerImpl.quickCheck(
                                             src,start,limit,
                                             NormalizerImpl.getFromIndexesArr(
                                                                              NormalizerImpl.INDEX_MIN_NFKC_NO_MAYBE
                                                                              ),
                                             NormalizerImpl.QC_NFKC,
                                             NormalizerImpl.OPTIONS_COMPAT,
                                             allowMaybe,
                                             nx
                                             );
        }
        protected boolean isNFSkippable(int c) {
            return NormalizerImpl.isNFSkippable(c, this,
                                                ( NormalizerImpl.CC_MASK|NormalizerImpl.COMBINES_ANY|
                                                  (NormalizerImpl.QC_NFKC & NormalizerImpl.QC_ANY_NO)
                                                  )
                                                );
        }
    };
    public static final class QuickCheckResult{
        private int resultValue;
        private QuickCheckResult(int value) {
            resultValue=value;
        }
    }
    public static final QuickCheckResult NO = new QuickCheckResult(0);
    public static final QuickCheckResult YES = new QuickCheckResult(1);
    public static final QuickCheckResult MAYBE = new QuickCheckResult(2);
    public NormalizerBase(String str, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance(str);
        this.mode = mode;
        this.options=opt;
    }
    public NormalizerBase(CharacterIterator iter, Mode mode) {
          this(iter, mode, UNICODE_LATEST);
    }
    public NormalizerBase(CharacterIterator iter, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance(
                                                   (CharacterIterator)iter.clone()
                                                   );
        this.mode = mode;
        this.options = opt;
    }
    public Object clone() {
        try {
            NormalizerBase copy = (NormalizerBase) super.clone();
            copy.text = (UCharacterIterator) text.clone();
            if (buffer != null) {
                copy.buffer = new char[buffer.length];
                System.arraycopy(buffer,0,copy.buffer,0,buffer.length);
            }
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    public static String compose(String str, boolean compat, int options) {
        char[] dest, src;
        if (options == UNICODE_3_2_0_ORIGINAL) {
            String mappedStr = NormalizerImpl.convert(str);
            dest = new char[mappedStr.length()*MAX_BUF_SIZE_COMPOSE];
            src = mappedStr.toCharArray();
        } else {
            dest = new char[str.length()*MAX_BUF_SIZE_COMPOSE];
            src = str.toCharArray();
        }
        int destSize=0;
        UnicodeSet nx = NormalizerImpl.getNX(options);
        options&=~(NormalizerImpl.OPTIONS_SETS_MASK|NormalizerImpl.OPTIONS_COMPAT|NormalizerImpl.OPTIONS_COMPOSE_CONTIGUOUS);
        if(compat) {
            options|=NormalizerImpl.OPTIONS_COMPAT;
        }
        for(;;) {
            destSize=NormalizerImpl.compose(src,0,src.length,
                                            dest,0,dest.length,options,
                                            nx);
            if(destSize<=dest.length) {
                return new String(dest,0,destSize);
            } else {
                dest = new char[destSize];
            }
        }
    }
    private static final int MAX_BUF_SIZE_COMPOSE = 2;
    private static final int MAX_BUF_SIZE_DECOMPOSE = 3;
    public static String decompose(String str, boolean compat) {
        return decompose(str,compat,UNICODE_LATEST);
    }
    public static String decompose(String str, boolean compat, int options) {
        int[] trailCC = new int[1];
        int destSize=0;
        UnicodeSet nx = NormalizerImpl.getNX(options);
        char[] dest;
        if (options == UNICODE_3_2_0_ORIGINAL) {
            String mappedStr = NormalizerImpl.convert(str);
            dest = new char[mappedStr.length()*MAX_BUF_SIZE_DECOMPOSE];
            for(;;) {
                destSize=NormalizerImpl.decompose(mappedStr.toCharArray(),0,mappedStr.length(),
                                                  dest,0,dest.length,
                                                  compat,trailCC, nx);
                if(destSize<=dest.length) {
                    return new String(dest,0,destSize);
                } else {
                    dest = new char[destSize];
                }
            }
        } else {
            dest = new char[str.length()*MAX_BUF_SIZE_DECOMPOSE];
            for(;;) {
                destSize=NormalizerImpl.decompose(str.toCharArray(),0,str.length(),
                                                  dest,0,dest.length,
                                                  compat,trailCC, nx);
                if(destSize<=dest.length) {
                    return new String(dest,0,destSize);
                } else {
                    dest = new char[destSize];
                }
            }
        }
    }
    public static int normalize(char[] src,int srcStart, int srcLimit,
                                char[] dest,int destStart, int destLimit,
                                Mode  mode, int options) {
        int length = mode.normalize(src,srcStart,srcLimit,dest,destStart,destLimit, options);
        if(length<=(destLimit-destStart)) {
            return length;
        } else {
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }
    }
    public int current() {
        if(bufferPos<bufferLimit || nextNormalize()) {
            return getCodePointAt(bufferPos);
        } else {
            return DONE;
        }
    }
    public int next() {
        if(bufferPos<bufferLimit ||  nextNormalize()) {
            int c=getCodePointAt(bufferPos);
            bufferPos+=(c>0xFFFF) ? 2 : 1;
            return c;
        } else {
            return DONE;
        }
    }
    public int previous() {
        if(bufferPos>0 || previousNormalize()) {
            int c=getCodePointAt(bufferPos-1);
            bufferPos-=(c>0xFFFF) ? 2 : 1;
            return c;
        } else {
            return DONE;
        }
    }
    public void reset() {
        text.setIndex(0);
        currentIndex=nextIndex=0;
        clearBuffer();
    }
    public void setIndexOnly(int index) {
        text.setIndex(index);
        currentIndex=nextIndex=index; 
        clearBuffer();
    }
     public int setIndex(int index) {
         setIndexOnly(index);
         return current();
     }
    public int getBeginIndex() {
        return 0;
    }
    public int getEndIndex() {
        return endIndex();
    }
    public int getIndex() {
        if(bufferPos<bufferLimit) {
            return currentIndex;
        } else {
            return nextIndex;
        }
    }
    public int endIndex() {
        return text.getLength();
    }
    public void setMode(Mode newMode) {
        mode = newMode;
    }
    public Mode getMode() {
        return mode;
    }
    public void setText(String newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new InternalError("Could not create a new UCharacterIterator");
        }
        text = newIter;
        reset();
    }
    public void setText(CharacterIterator newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new InternalError("Could not create a new UCharacterIterator");
        }
        text = newIter;
        currentIndex=nextIndex=0;
        clearBuffer();
    }
    private static  long getPrevNorm32(UCharacterIterator src,
                                       int minC,
                                       int mask,
                                       char[] chars) {
        long norm32;
        int ch=0;
        if((ch=src.previous()) == UCharacterIterator.DONE) {
            return 0;
        }
        chars[0]=(char)ch;
        chars[1]=0;
        if(chars[0]<minC) {
            return 0;
        } else if(!UTF16.isSurrogate(chars[0])) {
            return NormalizerImpl.getNorm32(chars[0]);
        } else if(UTF16.isLeadSurrogate(chars[0]) || (src.getIndex()==0)) {
            chars[1]=(char)src.current();
            return 0;
        } else if(UTF16.isLeadSurrogate(chars[1]=(char)src.previous())) {
            norm32=NormalizerImpl.getNorm32(chars[1]);
            if((norm32&mask)==0) {
                return 0;
            } else {
                return NormalizerImpl.getNorm32FromSurrogatePair(norm32,chars[0]);
            }
        } else {
            src.moveIndex( 1);
            return 0;
        }
    }
    private interface IsPrevBoundary{
        public boolean isPrevBoundary(UCharacterIterator src,
                                      int minC,
                                      int mask,
                                      char[] chars);
    }
    private static final class IsPrevNFDSafe implements IsPrevBoundary{
        public boolean isPrevBoundary(UCharacterIterator src,
                                      int minC,
                                      int ccOrQCMask,
                                      char[] chars) {
            return NormalizerImpl.isNFDSafe(getPrevNorm32(src, minC,
                                                          ccOrQCMask, chars),
                                            ccOrQCMask,
                                            ccOrQCMask& NormalizerImpl.QC_MASK);
        }
    }
    private static final class IsPrevTrueStarter implements IsPrevBoundary{
        public boolean isPrevBoundary(UCharacterIterator src,
                                      int minC,
                                      int ccOrQCMask,
                                      char[] chars) {
            long norm32;
            int decompQCMask;
            decompQCMask=(ccOrQCMask<<2)&0xf; 
            norm32=getPrevNorm32(src, minC, ccOrQCMask|decompQCMask, chars);
            return NormalizerImpl.isTrueStarter(norm32,ccOrQCMask,decompQCMask);
        }
    }
    private static int findPreviousIterationBoundary(UCharacterIterator src,
                                                     IsPrevBoundary obj,
                                                     int minC,
                                                     int mask,
                                                     char[] buffer,
                                                     int[] startIndex) {
        char[] chars=new char[2];
        boolean isBoundary;
        startIndex[0] = buffer.length;
        chars[0]=0;
        while(src.getIndex()>0 && chars[0]!=UCharacterIterator.DONE) {
            isBoundary=obj.isPrevBoundary(src, minC, mask, chars);
            if(startIndex[0] < (chars[1]==0 ? 1 : 2)) {
                char[] newBuf = new char[buffer.length*2];
                System.arraycopy(buffer,startIndex[0],newBuf,
                                 newBuf.length-(buffer.length-startIndex[0]),
                                 buffer.length-startIndex[0]);
                startIndex[0]+=newBuf.length-buffer.length;
                buffer=newBuf;
                newBuf=null;
            }
            buffer[--startIndex[0]]=chars[0];
            if(chars[1]!=0) {
                buffer[--startIndex[0]]=chars[1];
            }
            if(isBoundary) {
                break;
            }
        }
        return buffer.length-startIndex[0];
    }
    private static int previous(UCharacterIterator src,
                                char[] dest, int destStart, int destLimit,
                                Mode mode,
                                boolean doNormalize,
                                boolean[] pNeededToNormalize,
                                int options) {
        IsPrevBoundary isPreviousBoundary;
        int destLength, bufferLength;
        int mask;
        int c,c2;
        char minC;
        int destCapacity = destLimit-destStart;
        destLength=0;
        if(pNeededToNormalize!=null) {
            pNeededToNormalize[0]=false;
        }
        minC = (char)mode.getMinC();
        mask = mode.getMask();
        isPreviousBoundary = mode.getPrevBoundary();
        if(isPreviousBoundary==null) {
            destLength=0;
            if((c=src.previous())>=0) {
                destLength=1;
                if(UTF16.isTrailSurrogate((char)c)) {
                    c2= src.previous();
                    if(c2!= UCharacterIterator.DONE) {
                        if(UTF16.isLeadSurrogate((char)c2)) {
                            if(destCapacity>=2) {
                                dest[1]=(char)c; 
                                destLength=2;
                            }
                            c=c2;
                        } else {
                            src.moveIndex(1);
                        }
                    }
                }
                if(destCapacity>0) {
                    dest[0]=(char)c;
                }
            }
            return destLength;
        }
        char[] buffer = new char[100];
        int[] startIndex= new int[1];
        bufferLength=findPreviousIterationBoundary(src,
                                                   isPreviousBoundary,
                                                   minC, mask,buffer,
                                                   startIndex);
        if(bufferLength>0) {
            if(doNormalize) {
                destLength=NormalizerBase.normalize(buffer,startIndex[0],
                                                startIndex[0]+bufferLength,
                                                dest, destStart,destLimit,
                                                mode, options);
                if(pNeededToNormalize!=null) {
                    pNeededToNormalize[0]=(boolean)(destLength!=bufferLength ||
                                                    Utility.arrayRegionMatches(
                                                                               buffer,0,dest,
                                                                               destStart,destLimit
                                                                               ));
                }
            } else {
                if(destCapacity>0) {
                    System.arraycopy(buffer,startIndex[0],dest,0,
                                     (bufferLength<destCapacity) ?
                                     bufferLength : destCapacity
                                     );
                }
            }
        }
        return destLength;
    }
    private interface IsNextBoundary{
        boolean isNextBoundary(UCharacterIterator src,
                               int minC,
                               int mask,
                               int[] chars);
    }
    private static long  getNextNorm32(UCharacterIterator src,
                                                   int minC,
                                                   int mask,
                                                   int[] chars) {
        long norm32;
        chars[0]=src.next();
        chars[1]=0;
        if(chars[0]<minC) {
            return 0;
        }
        norm32=NormalizerImpl.getNorm32((char)chars[0]);
        if(UTF16.isLeadSurrogate((char)chars[0])) {
            if(src.current()!=UCharacterIterator.DONE &&
               UTF16.isTrailSurrogate((char)(chars[1]=src.current()))) {
                src.moveIndex(1); 
                if((norm32&mask)==0) {
                    return 0;
                } else {
                    return NormalizerImpl.getNorm32FromSurrogatePair(norm32,(char)chars[1]);
                }
            } else {
                return 0;
            }
        }
        return norm32;
    }
    private static final class IsNextNFDSafe implements IsNextBoundary{
        public boolean isNextBoundary(UCharacterIterator src,
                                      int minC,
                                      int ccOrQCMask,
                                      int[] chars) {
            return NormalizerImpl.isNFDSafe(getNextNorm32(src,minC,ccOrQCMask,chars),
                                            ccOrQCMask, ccOrQCMask&NormalizerImpl.QC_MASK);
        }
    }
    private static final class IsNextTrueStarter implements IsNextBoundary{
        public boolean isNextBoundary(UCharacterIterator src,
                                      int minC,
                                      int ccOrQCMask,
                                      int[] chars) {
            long norm32;
            int decompQCMask;
            decompQCMask=(ccOrQCMask<<2)&0xf; 
            norm32=getNextNorm32(src, minC, ccOrQCMask|decompQCMask, chars);
            return NormalizerImpl.isTrueStarter(norm32, ccOrQCMask, decompQCMask);
        }
    }
    private static int findNextIterationBoundary(UCharacterIterator src,
                                                 IsNextBoundary obj,
                                                 int minC,
                                                 int mask,
                                                 char[] buffer) {
        if(src.current()==UCharacterIterator.DONE) {
            return 0;
        }
        int[] chars = new int[2];
        chars[0]=src.next();
        buffer[0]=(char)chars[0];
        int bufferIndex = 1;
        if(UTF16.isLeadSurrogate((char)chars[0])&&
           src.current()!=UCharacterIterator.DONE) {
            if(UTF16.isTrailSurrogate((char)(chars[1]=src.next()))) {
                buffer[bufferIndex++]=(char)chars[1];
            } else {
                src.moveIndex(-1); 
            }
        }
        while( src.current()!=UCharacterIterator.DONE) {
            if(obj.isNextBoundary(src, minC, mask, chars)) {
                src.moveIndex(chars[1]==0 ? -1 : -2);
                break;
            } else {
                if(bufferIndex+(chars[1]==0 ? 1 : 2)<=buffer.length) {
                    buffer[bufferIndex++]=(char)chars[0];
                    if(chars[1]!=0) {
                        buffer[bufferIndex++]=(char)chars[1];
                    }
                } else {
                    char[] newBuf = new char[buffer.length*2];
                    System.arraycopy(buffer,0,newBuf,0,bufferIndex);
                    buffer = newBuf;
                    buffer[bufferIndex++]=(char)chars[0];
                    if(chars[1]!=0) {
                        buffer[bufferIndex++]=(char)chars[1];
                    }
                }
            }
        }
        return bufferIndex;
    }
    private static int next(UCharacterIterator src,
                            char[] dest, int destStart, int destLimit,
                            NormalizerBase.Mode mode,
                            boolean doNormalize,
                            boolean[] pNeededToNormalize,
                            int options) {
        IsNextBoundary isNextBoundary;
        int  mask;
        int  bufferLength;
        int c,c2;
        char minC;
        int destCapacity = destLimit - destStart;
        int destLength = 0;
        if(pNeededToNormalize!=null) {
            pNeededToNormalize[0]=false;
        }
        minC = (char)mode.getMinC();
        mask = mode.getMask();
        isNextBoundary = mode.getNextBoundary();
        if(isNextBoundary==null) {
            destLength=0;
            c=src.next();
            if(c!=UCharacterIterator.DONE) {
                destLength=1;
                if(UTF16.isLeadSurrogate((char)c)) {
                    c2= src.next();
                    if(c2!= UCharacterIterator.DONE) {
                        if(UTF16.isTrailSurrogate((char)c2)) {
                            if(destCapacity>=2) {
                                dest[1]=(char)c2; 
                                destLength=2;
                            }
                        } else {
                            src.moveIndex(-1);
                        }
                    }
                }
                if(destCapacity>0) {
                    dest[0]=(char)c;
                }
            }
            return destLength;
        }
        char[] buffer=new char[100];
        int[] startIndex = new int[1];
        bufferLength=findNextIterationBoundary(src,isNextBoundary, minC, mask,
                                               buffer);
        if(bufferLength>0) {
            if(doNormalize) {
                destLength=mode.normalize(buffer,startIndex[0],bufferLength,
                                          dest,destStart,destLimit, options);
                if(pNeededToNormalize!=null) {
                    pNeededToNormalize[0]=(boolean)(destLength!=bufferLength ||
                                                    Utility.arrayRegionMatches(buffer,startIndex[0],
                                                                               dest,destStart,
                                                                               destLength));
                }
            } else {
                if(destCapacity>0) {
                    System.arraycopy(buffer,0,dest,destStart,
                                     Math.min(bufferLength,destCapacity)
                                     );
                }
            }
        }
        return destLength;
    }
    private void clearBuffer() {
        bufferLimit=bufferStart=bufferPos=0;
    }
    private boolean nextNormalize() {
        clearBuffer();
        currentIndex=nextIndex;
        text.setIndex(nextIndex);
        bufferLimit=next(text,buffer,bufferStart,buffer.length,mode,true,null,options);
        nextIndex=text.getIndex();
        return (bufferLimit>0);
    }
    private boolean previousNormalize() {
        clearBuffer();
        nextIndex=currentIndex;
        text.setIndex(currentIndex);
        bufferLimit=previous(text,buffer,bufferStart,buffer.length,mode,true,null,options);
        currentIndex=text.getIndex();
        bufferPos = bufferLimit;
        return bufferLimit>0;
    }
    private int getCodePointAt(int index) {
        if( UTF16.isSurrogate(buffer[index])) {
            if(UTF16.isLeadSurrogate(buffer[index])) {
                if((index+1)<bufferLimit &&
                   UTF16.isTrailSurrogate(buffer[index+1])) {
                    return UCharacterProperty.getRawSupplementary(
                                                                  buffer[index],
                                                                  buffer[index+1]
                                                                  );
                }
            }else if(UTF16.isTrailSurrogate(buffer[index])) {
                if(index>0 && UTF16.isLeadSurrogate(buffer[index-1])) {
                    return UCharacterProperty.getRawSupplementary(
                                                                  buffer[index-1],
                                                                  buffer[index]
                                                                  );
                }
            }
        }
        return buffer[index];
    }
    public static boolean isNFSkippable(int c, Mode mode) {
        return mode.isNFSkippable(c);
    }
    public static final int UNICODE_3_2_0_ORIGINAL =
                               UNICODE_3_2 |
                               NormalizerImpl.WITHOUT_CORRIGENDUM4_CORRECTIONS |
                               NormalizerImpl.BEFORE_PRI_29;
    public static final int UNICODE_LATEST = 0x00;
    public NormalizerBase(String str, Mode mode) {
          this(str, mode, UNICODE_LATEST);
    }
    public static String normalize(String str, Normalizer.Form form) {
        return normalize(str, form, UNICODE_LATEST);
    }
    public static String normalize(String str, Normalizer.Form form, int options) {
        int len = str.length();
        boolean asciiOnly = true;
        if (len < 80) {
            for (int i = 0; i < len; i++) {
                if (str.charAt(i) > 127) {
                    asciiOnly = false;
                    break;
                }
            }
        } else {
            char[] a = str.toCharArray();
            for (int i = 0; i < len; i++) {
                if (a[i] > 127) {
                    asciiOnly = false;
                    break;
                }
            }
        }
        switch (form) {
        case NFC :
            return asciiOnly ? str : NFC.normalize(str, options);
        case NFD :
            return asciiOnly ? str : NFD.normalize(str, options);
        case NFKC :
            return asciiOnly ? str : NFKC.normalize(str, options);
        case NFKD :
            return asciiOnly ? str : NFKD.normalize(str, options);
        }
        throw new IllegalArgumentException("Unexpected normalization form: " +
                                           form);
    }
    public static boolean isNormalized(String str, Normalizer.Form form) {
        return isNormalized(str, form, UNICODE_LATEST);
    }
    public static boolean isNormalized(String str, Normalizer.Form form, int options) {
        switch (form) {
        case NFC:
            return (NFC.quickCheck(str.toCharArray(),0,str.length(),false,NormalizerImpl.getNX(options))==YES);
        case NFD:
            return (NFD.quickCheck(str.toCharArray(),0,str.length(),false,NormalizerImpl.getNX(options))==YES);
        case NFKC:
            return (NFKC.quickCheck(str.toCharArray(),0,str.length(),false,NormalizerImpl.getNX(options))==YES);
        case NFKD:
            return (NFKD.quickCheck(str.toCharArray(),0,str.length(),false,NormalizerImpl.getNX(options))==YES);
        }
        throw new IllegalArgumentException("Unexpected normalization form: " +
                                           form);
    }
}
