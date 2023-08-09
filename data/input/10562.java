class AuFileFormat extends AudioFileFormat {
    static final int AU_SUN_MAGIC =     0x2e736e64;
    static final int AU_SUN_INV_MAGIC = 0x646e732e;
    static final int AU_DEC_MAGIC =         0x2e736400;
    static final int AU_DEC_INV_MAGIC = 0x0064732e;
    static final int AU_ULAW_8       = 1;  
    static final int AU_LINEAR_8     = 2;  
    static final int AU_LINEAR_16    = 3;  
    static final int AU_LINEAR_24    = 4;  
    static final int AU_LINEAR_32    = 5;  
    static final int AU_FLOAT        = 6;  
    static final int AU_DOUBLE       = 7;  
    static final int AU_ADPCM_G721   = 23; 
    static final int AU_ADPCM_G722   = 24; 
    static final int AU_ADPCM_G723_3 = 25; 
    static final int AU_ADPCM_G723_5 = 26; 
    static final int AU_ALAW_8       = 27; 
    static final int AU_HEADERSIZE       = 24;
    int auType;
    AuFileFormat( AudioFileFormat aff ) {
        this( aff.getType(), aff.getByteLength(), aff.getFormat(), aff.getFrameLength() );
    }
    AuFileFormat(AudioFileFormat.Type type, int lengthInBytes, AudioFormat format, int lengthInFrames) {
        super(type,lengthInBytes,format,lengthInFrames);
        AudioFormat.Encoding encoding = format.getEncoding();
        auType = -1;
        if( AudioFormat.Encoding.ALAW.equals(encoding) ) {
            if( format.getSampleSizeInBits()==8 ) {
                auType = AU_ALAW_8;
            }
        } else if( AudioFormat.Encoding.ULAW.equals(encoding) ) {
            if( format.getSampleSizeInBits()==8 ) {
                auType = AU_ULAW_8;
            }
        } else if( AudioFormat.Encoding.PCM_SIGNED.equals(encoding) ) {
            if( format.getSampleSizeInBits()==8 ) {
                auType = AU_LINEAR_8;
            } else if( format.getSampleSizeInBits()==16 ) {
                auType = AU_LINEAR_16;
            } else if( format.getSampleSizeInBits()==24 ) {
                auType = AU_LINEAR_24;
            } else if( format.getSampleSizeInBits()==32 ) {
                auType = AU_LINEAR_32;
            }
        }
    }
    public int getAuType() {
        return auType;
    }
}
