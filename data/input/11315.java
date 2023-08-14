public class PCMtoPCMCodec extends SunCodec {
    private static final AudioFormat.Encoding[] inputEncodings = {
        AudioFormat.Encoding.PCM_SIGNED,
        AudioFormat.Encoding.PCM_UNSIGNED,
    };
    private static final AudioFormat.Encoding[] outputEncodings = {
        AudioFormat.Encoding.PCM_SIGNED,
        AudioFormat.Encoding.PCM_UNSIGNED,
    };
    private static final int tempBufferSize = 64;
    private byte tempBuffer [] = null;
    public PCMtoPCMCodec() {
        super( inputEncodings, outputEncodings);
    }
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat){
        if( sourceFormat.getEncoding().equals( AudioFormat.Encoding.PCM_SIGNED ) ||
            sourceFormat.getEncoding().equals( AudioFormat.Encoding.PCM_UNSIGNED ) ) {
                AudioFormat.Encoding encs[] = new AudioFormat.Encoding[2];
                encs[0] = AudioFormat.Encoding.PCM_SIGNED;
                encs[1] = AudioFormat.Encoding.PCM_UNSIGNED;
                return encs;
            } else {
                return new AudioFormat.Encoding[0];
            }
    }
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat){
        AudioFormat[] formats = getOutputFormats( sourceFormat );
        Vector newFormats = new Vector();
        for(int i=0; i<formats.length; i++ ) {
            if( formats[i].getEncoding().equals( targetEncoding ) ) {
                newFormats.addElement( formats[i] );
            }
        }
        AudioFormat[] formatArray = new AudioFormat[newFormats.size()];
        for (int i = 0; i < formatArray.length; i++) {
            formatArray[i] = (AudioFormat)(newFormats.elementAt(i));
        }
        return formatArray;
    }
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
        if( isConversionSupported(targetEncoding, sourceStream.getFormat()) ) {
            AudioFormat sourceFormat = sourceStream.getFormat();
            AudioFormat targetFormat = new AudioFormat( targetEncoding,
                                                        sourceFormat.getSampleRate(),
                                                        sourceFormat.getSampleSizeInBits(),
                                                        sourceFormat.getChannels(),
                                                        sourceFormat.getFrameSize(),
                                                        sourceFormat.getFrameRate(),
                                                        sourceFormat.isBigEndian() );
            return getAudioInputStream( targetFormat, sourceStream );
        } else {
            throw new IllegalArgumentException("Unsupported conversion: " + sourceStream.getFormat().toString() + " to " + targetEncoding.toString() );
        }
    }
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream){
        return getConvertedStream( targetFormat, sourceStream );
    }
    private AudioInputStream getConvertedStream(AudioFormat outputFormat, AudioInputStream stream) {
        AudioInputStream cs = null;
        AudioFormat inputFormat = stream.getFormat();
        if( inputFormat.matches(outputFormat) ) {
            cs = stream;
        } else {
            cs = (AudioInputStream) (new PCMtoPCMCodecStream(stream, outputFormat));
            tempBuffer = new byte[tempBufferSize];
        }
        return cs;
    }
    private AudioFormat[] getOutputFormats(AudioFormat inputFormat) {
        Vector formats = new Vector();
        AudioFormat format;
        int sampleSize = inputFormat.getSampleSizeInBits();
        boolean isBigEndian = inputFormat.isBigEndian();
        if ( sampleSize==8 ) {
            if ( AudioFormat.Encoding.PCM_SIGNED.equals(inputFormat.getEncoding()) ) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
            }
            if ( AudioFormat.Encoding.PCM_UNSIGNED.equals(inputFormat.getEncoding()) ) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
            }
        } else if ( sampleSize==16 ) {
            if ( AudioFormat.Encoding.PCM_SIGNED.equals(inputFormat.getEncoding()) && isBigEndian ) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         true );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
            }
            if ( AudioFormat.Encoding.PCM_UNSIGNED.equals(inputFormat.getEncoding()) && isBigEndian ) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         true );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
            }
            if ( AudioFormat.Encoding.PCM_SIGNED.equals(inputFormat.getEncoding()) && !isBigEndian ) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         true );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         true );
                formats.addElement(format);
            }
            if ( AudioFormat.Encoding.PCM_UNSIGNED.equals(inputFormat.getEncoding()) && !isBigEndian ) {
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         false );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         true );
                formats.addElement(format);
                format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                         inputFormat.getSampleRate(),
                                         inputFormat.getSampleSizeInBits(),
                                         inputFormat.getChannels(),
                                         inputFormat.getFrameSize(),
                                         inputFormat.getFrameRate(),
                                         true );
                formats.addElement(format);
            }
        }
        AudioFormat[] formatArray;
        synchronized(formats) {
            formatArray = new AudioFormat[formats.size()];
            for (int i = 0; i < formatArray.length; i++) {
                formatArray[i] = (AudioFormat)(formats.elementAt(i));
            }
        }
        return formatArray;
    }
    class PCMtoPCMCodecStream extends AudioInputStream {
        private final int PCM_SWITCH_SIGNED_8BIT                = 1;
        private final int PCM_SWITCH_ENDIAN                             = 2;
        private final int PCM_SWITCH_SIGNED_LE                  = 3;
        private final int PCM_SWITCH_SIGNED_BE                  = 4;
        private final int PCM_UNSIGNED_LE2SIGNED_BE             = 5;
        private final int PCM_SIGNED_LE2UNSIGNED_BE             = 6;
        private final int PCM_UNSIGNED_BE2SIGNED_LE             = 7;
        private final int PCM_SIGNED_BE2UNSIGNED_LE             = 8;
        private int sampleSizeInBytes = 0;
        private int conversionType = 0;
        PCMtoPCMCodecStream(AudioInputStream stream, AudioFormat outputFormat) {
            super(stream, outputFormat, -1);
            int sampleSizeInBits = 0;
            AudioFormat.Encoding inputEncoding = null;
            AudioFormat.Encoding outputEncoding = null;
            boolean inputIsBigEndian;
            boolean outputIsBigEndian;
            AudioFormat inputFormat = stream.getFormat();
            if ( ! (isConversionSupported(inputFormat, outputFormat)) ) {
                throw new IllegalArgumentException("Unsupported conversion: " + inputFormat.toString() + " to " + outputFormat.toString());
            }
            inputEncoding = inputFormat.getEncoding();
            outputEncoding = outputFormat.getEncoding();
            inputIsBigEndian = inputFormat.isBigEndian();
            outputIsBigEndian = outputFormat.isBigEndian();
            sampleSizeInBits = inputFormat.getSampleSizeInBits();
            sampleSizeInBytes = sampleSizeInBits/8;
            if( sampleSizeInBits==8 ) {
                if( AudioFormat.Encoding.PCM_UNSIGNED.equals(inputEncoding) &&
                    AudioFormat.Encoding.PCM_SIGNED.equals(outputEncoding) ) {
                    conversionType = PCM_SWITCH_SIGNED_8BIT;
                    if(Printer.debug) Printer.debug("PCMtoPCMCodecStream: conversionType = PCM_SWITCH_SIGNED_8BIT");
                } else if( AudioFormat.Encoding.PCM_SIGNED.equals(inputEncoding) &&
                           AudioFormat.Encoding.PCM_UNSIGNED.equals(outputEncoding) ) {
                    conversionType = PCM_SWITCH_SIGNED_8BIT;
                    if(Printer.debug) Printer.debug("PCMtoPCMCodecStream: conversionType = PCM_SWITCH_SIGNED_8BIT");
                }
            } else {
                if( inputEncoding.equals(outputEncoding) && (inputIsBigEndian != outputIsBigEndian) ) {
                    conversionType = PCM_SWITCH_ENDIAN;
                    if(Printer.debug) Printer.debug("PCMtoPCMCodecStream: conversionType = PCM_SWITCH_ENDIAN");
                } else if (AudioFormat.Encoding.PCM_UNSIGNED.equals(inputEncoding) && !inputIsBigEndian &&
                            AudioFormat.Encoding.PCM_SIGNED.equals(outputEncoding) && outputIsBigEndian) {
                    conversionType = PCM_UNSIGNED_LE2SIGNED_BE;
                    if(Printer.debug) Printer.debug("PCMtoPCMCodecStream: conversionType = PCM_UNSIGNED_LE2SIGNED_BE");
                } else if (AudioFormat.Encoding.PCM_SIGNED.equals(inputEncoding) && !inputIsBigEndian &&
                           AudioFormat.Encoding.PCM_UNSIGNED.equals(outputEncoding) && outputIsBigEndian) {
                    conversionType = PCM_SIGNED_LE2UNSIGNED_BE;
                    if(Printer.debug) Printer.debug("PCMtoPCMCodecStream: conversionType = PCM_SIGNED_LE2UNSIGNED_BE");
                } else if (AudioFormat.Encoding.PCM_UNSIGNED.equals(inputEncoding) && inputIsBigEndian &&
                           AudioFormat.Encoding.PCM_SIGNED.equals(outputEncoding) && !outputIsBigEndian) {
                    conversionType = PCM_UNSIGNED_BE2SIGNED_LE;
                    if(Printer.debug) Printer.debug("PCMtoPCMCodecStream: conversionType = PCM_UNSIGNED_BE2SIGNED_LE");
                } else if (AudioFormat.Encoding.PCM_SIGNED.equals(inputEncoding) && inputIsBigEndian &&
                           AudioFormat.Encoding.PCM_UNSIGNED.equals(outputEncoding) && !outputIsBigEndian) {
                    conversionType = PCM_SIGNED_BE2UNSIGNED_LE;
                    if(Printer.debug) Printer.debug("PCMtoPCMCodecStream: conversionType = PCM_SIGNED_BE2UNSIGNED_LE");
                }
            }
            frameSize = inputFormat.getFrameSize();
            if( frameSize == AudioSystem.NOT_SPECIFIED ) {
                frameSize=1;
            }
            if( stream instanceof AudioInputStream ) {
                frameLength = stream.getFrameLength();
            } else {
                frameLength = AudioSystem.NOT_SPECIFIED;
            }
            framePos = 0;
        }
        public int read() throws IOException {
            int temp;
            byte tempbyte;
            if( frameSize==1 ) {
                if( conversionType == PCM_SWITCH_SIGNED_8BIT ) {
                    temp = super.read();
                    if( temp < 0 ) return temp;         
                    tempbyte = (byte) (temp & 0xf);
                    tempbyte = (tempbyte >= 0) ? (byte)(0x80 | tempbyte) : (byte)(0x7F & tempbyte);
                    temp = (int) tempbyte & 0xf;
                    return temp;
                } else {
                    throw new IOException("cannot read a single byte if frame size > 1");
                }
            } else {
                throw new IOException("cannot read a single byte if frame size > 1");
            }
        }
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        public int read(byte[] b, int off, int len) throws IOException {
            int i;
            if ( len%frameSize != 0 ) {
                len -= (len%frameSize);
            }
            if( (frameLength!=AudioSystem.NOT_SPECIFIED) && ( (len/frameSize) >(frameLength-framePos)) ) {
                len = (int)(frameLength-framePos) * frameSize;
            }
            int readCount = super.read(b, off, len);
            byte tempByte;
            if(readCount<0) {   
                return readCount;
            }
            switch( conversionType ) {
            case PCM_SWITCH_SIGNED_8BIT:
                switchSigned8bit(b,off,len,readCount);
                break;
            case PCM_SWITCH_ENDIAN:
                switchEndian(b,off,len,readCount);
                break;
            case PCM_SWITCH_SIGNED_LE:
                switchSignedLE(b,off,len,readCount);
                break;
            case PCM_SWITCH_SIGNED_BE:
                switchSignedBE(b,off,len,readCount);
                break;
            case PCM_UNSIGNED_LE2SIGNED_BE:
            case PCM_SIGNED_LE2UNSIGNED_BE:
                switchSignedLE(b,off,len,readCount);
                switchEndian(b,off,len,readCount);
                break;
            case PCM_UNSIGNED_BE2SIGNED_LE:
            case PCM_SIGNED_BE2UNSIGNED_LE:
                switchSignedBE(b,off,len,readCount);
                switchEndian(b,off,len,readCount);
                break;
            default:
            }
            return readCount;
        }
        private void switchSigned8bit(byte[] b, int off, int len, int readCount) {
            for(int i=off; i < (off+readCount); i++) {
                b[i] = (b[i] >= 0) ? (byte)(0x80 | b[i]) : (byte)(0x7F & b[i]);
            }
        }
        private void switchSignedBE(byte[] b, int off, int len, int readCount) {
            for(int i=off; i < (off+readCount); i+= sampleSizeInBytes ) {
                b[i] = (b[i] >= 0) ? (byte)(0x80 | b[i]) : (byte)(0x7F & b[i]);
            }
        }
        private void switchSignedLE(byte[] b, int off, int len, int readCount) {
            for(int i=(off+sampleSizeInBytes-1); i < (off+readCount); i+= sampleSizeInBytes ) {
                b[i] = (b[i] >= 0) ? (byte)(0x80 | b[i]) : (byte)(0x7F & b[i]);
            }
        }
        private void switchEndian(byte[] b, int off, int len, int readCount) {
            if(sampleSizeInBytes == 2) {
                for(int i=off; i < (off+readCount); i += sampleSizeInBytes ) {
                    byte temp;
                    temp = b[i];
                    b[i] = b[i+1];
                    b[i+1] = temp;
                }
            }
        }
    } 
} 
