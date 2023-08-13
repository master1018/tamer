class AiffFileFormat extends AudioFileFormat {
    static final int AIFF_MAGIC         = 1179603533;
    static final int AIFC_MAGIC                 = 0x41494643;   
    static final int AIFF_MAGIC2                = 0x41494646;   
    static final int FVER_MAGIC                 = 0x46564552;   
    static final int FVER_TIMESTAMP             = 0xA2805140;   
    static final int COMM_MAGIC                 = 0x434f4d4d;   
    static final int SSND_MAGIC                 = 0x53534e44;   
    static final int AIFC_PCM                   = 0x4e4f4e45;   
    static final int AIFC_ACE2                  = 0x41434532;   
    static final int AIFC_ACE8                  = 0x41434538;   
    static final int AIFC_MAC3                  = 0x4d414333;   
    static final int AIFC_MAC6                  = 0x4d414336;   
    static final int AIFC_ULAW                  = 0x756c6177;   
    static final int AIFC_IMA4                  = 0x696d6134;   
    static final int AIFF_HEADERSIZE    = 54;
    private int headerSize=AIFF_HEADERSIZE;
    private int commChunkSize=26;
    private int fverChunkSize=0;
    AiffFileFormat( AudioFileFormat aff ) {
        this( aff.getType(), aff.getByteLength(), aff.getFormat(), aff.getFrameLength() );
    }
    AiffFileFormat(Type type, int byteLength, AudioFormat format, int frameLength) {
        super(type, byteLength, format, frameLength);
    }
    int getHeaderSize() {
        return headerSize;
    }
    int getCommChunkSize() {
        return commChunkSize;
    }
    int getFverChunkSize() {
        return fverChunkSize;
    }
    int getSsndChunkOffset() {
        return getHeaderSize()-16;
    }
}
