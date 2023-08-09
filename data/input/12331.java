public class UlawCodec extends SunCodec {
    final static byte ULAW_TABH[] = new byte[256];
    final static byte ULAW_TABL[] = new byte[256];
    private static final AudioFormat.Encoding[] ulawEncodings = {AudioFormat.Encoding.ULAW,
                                                                 AudioFormat.Encoding.PCM_SIGNED};
    private static final short seg_end [] = {0xFF, 0x1FF, 0x3FF,
                                             0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF};
    static {
        for (int i=0;i<256;i++) {
            int ulaw = ~i;
            int t;
            ulaw &= 0xFF;
            t = ((ulaw & 0xf)<<3) + 132;
            t <<= ((ulaw & 0x70) >> 4);
            t = ( (ulaw&0x80) != 0 ) ? (132-t) : (t-132);
            ULAW_TABL[i] = (byte) (t&0xff);
            ULAW_TABH[i] = (byte) ((t>>8) & 0xff);
        }
    }
    public UlawCodec() {
        super(ulawEncodings, ulawEncodings);
    }
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat){
        if( AudioFormat.Encoding.PCM_SIGNED.equals(sourceFormat.getEncoding()) ) {
            if( sourceFormat.getSampleSizeInBits() == 16 ) {
                AudioFormat.Encoding enc[] = new AudioFormat.Encoding[1];
                enc[0] = AudioFormat.Encoding.ULAW;
                return enc;
            } else {
                return new AudioFormat.Encoding[0];
            }
        } else if (AudioFormat.Encoding.ULAW.equals(sourceFormat.getEncoding())) {
            if (sourceFormat.getSampleSizeInBits() == 8) {
                AudioFormat.Encoding enc[] = new AudioFormat.Encoding[1];
                enc[0] = AudioFormat.Encoding.PCM_SIGNED;
                return enc;
            } else {
                return new AudioFormat.Encoding[0];
            }
        } else {
            return new AudioFormat.Encoding[0];
        }
    }
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat){
        if( (AudioFormat.Encoding.PCM_SIGNED.equals(targetEncoding)
             && AudioFormat.Encoding.ULAW.equals(sourceFormat.getEncoding()))
            ||
            (AudioFormat.Encoding.ULAW.equals(targetEncoding)
             && AudioFormat.Encoding.PCM_SIGNED.equals(sourceFormat.getEncoding()))) {
                return getOutputFormats(sourceFormat);
            } else {
                return new AudioFormat[0];
            }
    }
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream){
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat.Encoding sourceEncoding = sourceFormat.getEncoding();
        if (sourceEncoding.equals(targetEncoding)) {
            return sourceStream;
        } else {
            AudioFormat targetFormat = null;
            if (!isConversionSupported(targetEncoding,sourceStream.getFormat())) {
                throw new IllegalArgumentException("Unsupported conversion: " + sourceStream.getFormat().toString() + " to " + targetEncoding.toString());
            }
            if (AudioFormat.Encoding.ULAW.equals(sourceEncoding) &&
                AudioFormat.Encoding.PCM_SIGNED.equals(targetEncoding) ) {
                targetFormat = new AudioFormat( targetEncoding,
                                                sourceFormat.getSampleRate(),
                                                16,
                                                sourceFormat.getChannels(),
                                                2*sourceFormat.getChannels(),
                                                sourceFormat.getSampleRate(),
                                                sourceFormat.isBigEndian());
            } else if (AudioFormat.Encoding.PCM_SIGNED.equals(sourceEncoding) &&
                       AudioFormat.Encoding.ULAW.equals(targetEncoding)) {
                targetFormat = new AudioFormat( targetEncoding,
                                                sourceFormat.getSampleRate(),
                                                8,
                                                sourceFormat.getChannels(),
                                                sourceFormat.getChannels(),
                                                sourceFormat.getSampleRate(),
                                                false);
            } else {
                throw new IllegalArgumentException("Unsupported conversion: " + sourceStream.getFormat().toString() + " to " + targetEncoding.toString());
            }
            return getAudioInputStream( targetFormat, sourceStream );
        }
    }
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream){
        return getConvertedStream(targetFormat, sourceStream);
    }
    private AudioInputStream getConvertedStream(AudioFormat outputFormat, AudioInputStream stream) {
        AudioInputStream cs = null;
        AudioFormat inputFormat = stream.getFormat();
        if( inputFormat.matches(outputFormat) ) {
            cs = stream;
        } else {
            cs = (AudioInputStream) (new UlawCodecStream(stream, outputFormat));
        }
        return cs;
    }
    private AudioFormat[] getOutputFormats(AudioFormat inputFormat) {
        Vector formats = new Vector();
        AudioFormat format;
        if ((inputFormat.getSampleSizeInBits() == 16)
            && AudioFormat.Encoding.PCM_SIGNED.equals(inputFormat.getEncoding())) {
            format = new AudioFormat(AudioFormat.Encoding.ULAW,
                                     inputFormat.getSampleRate(),
                                     8,
                                     inputFormat.getChannels(),
                                     inputFormat.getChannels(),
                                     inputFormat.getSampleRate(),
                                     false );
            formats.addElement(format);
        }
        if (AudioFormat.Encoding.ULAW.equals(inputFormat.getEncoding())) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                     inputFormat.getSampleRate(),
                                     16,
                                     inputFormat.getChannels(),
                                     inputFormat.getChannels()*2,
                                     inputFormat.getSampleRate(),
                                     false );
            formats.addElement(format);
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                     inputFormat.getSampleRate(),
                                     16,
                                     inputFormat.getChannels(),
                                     inputFormat.getChannels()*2,
                                     inputFormat.getSampleRate(),
                                     true );
            formats.addElement(format);
        }
        AudioFormat[] formatArray = new AudioFormat[formats.size()];
        for (int i = 0; i < formatArray.length; i++) {
            formatArray[i] = (AudioFormat)(formats.elementAt(i));
        }
        return formatArray;
    }
    class UlawCodecStream extends AudioInputStream {
        private static final int tempBufferSize = 64;
        private byte tempBuffer [] = null;
        boolean encode = false;
        AudioFormat encodeFormat;
        AudioFormat decodeFormat;
        byte tabByte1[] = null;
        byte tabByte2[] = null;
        int highByte = 0;
        int lowByte  = 1;
        UlawCodecStream(AudioInputStream stream, AudioFormat outputFormat) {
            super(stream, outputFormat, AudioSystem.NOT_SPECIFIED);
            AudioFormat inputFormat = stream.getFormat();
            if (!(isConversionSupported(outputFormat, inputFormat))) {
                throw new IllegalArgumentException("Unsupported conversion: " + inputFormat.toString() + " to " + outputFormat.toString());
            }
            boolean PCMIsBigEndian;
            if (AudioFormat.Encoding.ULAW.equals(inputFormat.getEncoding())) {
                encode = false;
                encodeFormat = inputFormat;
                decodeFormat = outputFormat;
                PCMIsBigEndian = outputFormat.isBigEndian();
            } else {
                encode = true;
                encodeFormat = outputFormat;
                decodeFormat = inputFormat;
                PCMIsBigEndian = inputFormat.isBigEndian();
                tempBuffer = new byte[tempBufferSize];
            }
            if (PCMIsBigEndian) {
                tabByte1 = ULAW_TABH;
                tabByte2 = ULAW_TABL;
                highByte = 0;
                lowByte  = 1;
            } else {
                tabByte1 = ULAW_TABL;
                tabByte2 = ULAW_TABH;
                highByte = 1;
                lowByte  = 0;
            }
            if (stream instanceof AudioInputStream) {
                frameLength = ((AudioInputStream)stream).getFrameLength();
            }
            framePos = 0;
            frameSize = inputFormat.getFrameSize();
            if (frameSize == AudioSystem.NOT_SPECIFIED) {
                frameSize = 1;
            }
        }
        private short search(short val, short table[], short size) {
            for(short i = 0; i < size; i++) {
                if (val <= table[i]) { return i; }
            }
            return size;
        }
        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b, 0, b.length) == 1) {
                return b[1] & 0xFF;
            }
            return -1;
        }
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        public int read(byte[] b, int off, int len) throws IOException {
            if( len%frameSize != 0 ) {
                len -= (len%frameSize);
            }
            if (encode) {
                short BIAS = 0x84;
                short mask;
                short seg;
                int i;
                short sample;
                byte enc;
                int readCount = 0;
                int currentPos = off;
                int readLeft = len*2;
                int readLen = ( (readLeft>tempBufferSize) ? tempBufferSize : readLeft );
                while ((readCount = super.read(tempBuffer,0,readLen))>0) {
                    for(i = 0; i < readCount; i+=2) {
                        sample = (short)(( (tempBuffer[i + highByte]) << 8) & 0xFF00);
                        sample |= (short)( (short) (tempBuffer[i + lowByte]) & 0xFF);
                        if(sample < 0) {
                            sample = (short) (BIAS - sample);
                            mask = 0x7F;
                        } else {
                            sample += BIAS;
                            mask = 0xFF;
                        }
                        seg = search(sample, seg_end, (short) 8);
                        if (seg >= 8) {  
                            enc = (byte) (0x7F ^ mask);
                        } else {
                            enc = (byte) ((seg << 4) | ((sample >> (seg+3)) & 0xF));
                            enc ^= mask;
                        }
                        b[currentPos] = enc;
                        currentPos++;
                    }
                    readLeft -= readCount;
                    readLen = ( (readLeft>tempBufferSize) ? tempBufferSize : readLeft );
                }
                if( currentPos==off && readCount<0 ) {  
                    return readCount;
                }
                return (currentPos - off);  
            } else {
                int i;
                int readLen = len/2;
                int readOffset = off + len/2;
                int readCount = super.read(b, readOffset, readLen);
                if(readCount<0) {               
                    return readCount;
                }
                for (i = off; i < (off + (readCount*2)); i+=2) {
                    b[i]        = (byte)tabByte1[b[readOffset] & 0xFF];
                    b[i+1]      = (byte)tabByte2[b[readOffset] & 0xFF];
                    readOffset++;
                }
                return (i - off);
            }
        }
    } 
} 
