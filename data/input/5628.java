public final class UBiDiProps {
    public UBiDiProps() throws IOException{
        InputStream is=ICUData.getStream(DATA_FILE_NAME);
        BufferedInputStream b=new BufferedInputStream(is, 4096 );
        readData(b);
        b.close();
        is.close();
    }
    private void readData(InputStream is) throws IOException {
        DataInputStream inputStream=new DataInputStream(is);
        ICUBinary.readHeader(inputStream, FMT, new IsAcceptable());
        int i, count;
        count=inputStream.readInt();
        if(count<IX_INDEX_TOP) {
            throw new IOException("indexes[0] too small in "+DATA_FILE_NAME);
        }
        indexes=new int[count];
        indexes[0]=count;
        for(i=1; i<count; ++i) {
            indexes[i]=inputStream.readInt();
        }
        trie=new CharTrie(inputStream, null);
        count=indexes[IX_MIRROR_LENGTH];
        if(count>0) {
            mirrors=new int[count];
            for(i=0; i<count; ++i) {
                mirrors[i]=inputStream.readInt();
            }
        }
        count=indexes[IX_JG_LIMIT]-indexes[IX_JG_START];
        jgArray=new byte[count];
        for(i=0; i<count; ++i) {
            jgArray[i]=inputStream.readByte();
        }
    }
    private final class IsAcceptable implements ICUBinary.Authenticate {
        public boolean isDataVersionAcceptable(byte version[]) {
            return version[0]==1 &&
                   version[2]==Trie.INDEX_STAGE_1_SHIFT_ && version[3]==Trie.INDEX_STAGE_2_SHIFT_;
        }
    }
    private static UBiDiProps gBdp=null;
    public static final synchronized UBiDiProps getSingleton() throws IOException {
        if(gBdp==null) {
            gBdp=new UBiDiProps();
        }
        return gBdp;
    }
    private static UBiDiProps gBdpDummy=null;
    private UBiDiProps(boolean makeDummy) { 
        indexes=new int[IX_TOP];
        indexes[0]=IX_TOP;
        trie=new CharTrie(0, 0, null); 
    }
    public static final synchronized UBiDiProps getDummy() {
        if(gBdpDummy==null) {
            gBdpDummy=new UBiDiProps(true);
        }
        return gBdpDummy;
    }
    public final int getClass(int c) {
        return getClassFromProps(trie.getCodePointValue(c));
    }
    private int indexes[];
    private int mirrors[];
    private byte jgArray[];
    private CharTrie trie;
    private static final String DATA_FILE_NAME = "/sun/text/resources/ubidi.icu";
    private static final byte FMT[]={ 0x42, 0x69, 0x44, 0x69 };
    private static final int IX_INDEX_TOP=0;
    private static final int IX_MIRROR_LENGTH=3;
    private static final int IX_JG_START=4;
    private static final int IX_JG_LIMIT=5;
    private static final int IX_TOP=16;
    private static final int CLASS_MASK=    0x0000001f;
    private static final int getClassFromProps(int props) {
        return props&CLASS_MASK;
    }
}
