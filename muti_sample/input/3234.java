public class UCDecoder extends CharacterDecoder {
    protected int bytesPerAtom() {
        return (2);
    }
    protected int bytesPerLine() {
        return (48);
    }
    private final static byte map_array[] = {
        (byte)'0',(byte)'1',(byte)'2',(byte)'3',(byte)'4',(byte)'5',(byte)'6',(byte)'7', 
        (byte)'8',(byte)'9',(byte)'A',(byte)'B',(byte)'C',(byte)'D',(byte)'E',(byte)'F', 
        (byte)'G',(byte)'H',(byte)'I',(byte)'J',(byte)'K',(byte)'L',(byte)'M',(byte)'N', 
        (byte)'O',(byte)'P',(byte)'Q',(byte)'R',(byte)'S',(byte)'T',(byte)'U',(byte)'V', 
        (byte)'W',(byte)'X',(byte)'Y',(byte)'Z',(byte)'a',(byte)'b',(byte)'c',(byte)'d', 
        (byte)'e',(byte)'f',(byte)'g',(byte)'h',(byte)'i',(byte)'j',(byte)'k',(byte)'l', 
        (byte)'m',(byte)'n',(byte)'o',(byte)'p',(byte)'q',(byte)'r',(byte)'s',(byte)'t', 
        (byte)'u',(byte)'v',(byte)'w',(byte)'x',(byte)'y',(byte)'z',(byte)'(',(byte)')'  
    };
    private int sequence;
    private byte tmp[] = new byte[2];
    private CRC16 crc = new CRC16();
    protected void decodeAtom(PushbackInputStream inStream, OutputStream outStream, int l) throws IOException {
        int i, p1, p2, np1, np2;
        byte a = -1, b = -1, c = -1;
        byte high_byte, low_byte;
        byte tmp[] = new byte[3];
        i = inStream.read(tmp);
        if (i != 3) {
                throw new CEStreamExhausted();
        }
        for (i = 0; (i < 64) && ((a == -1) || (b == -1) || (c == -1)); i++) {
            if (tmp[0] == map_array[i]) {
                a = (byte) i;
            }
            if (tmp[1] == map_array[i]) {
                b = (byte) i;
            }
            if (tmp[2] == map_array[i]) {
                c = (byte) i;
            }
        }
        high_byte = (byte) (((a & 0x38) << 2) + (b & 0x1f));
        low_byte = (byte) (((a & 0x7) << 5) + (c & 0x1f));
        p1 = 0;
        p2 = 0;
        for (i = 1; i < 256; i = i * 2) {
            if ((high_byte & i) != 0)
                p1++;
            if ((low_byte & i) != 0)
                p2++;
        }
        np1 = (b & 32) / 32;
        np2 = (c & 32) / 32;
        if ((p1 & 1) != np1) {
            throw new CEFormatException("UCDecoder: High byte parity error.");
        }
        if ((p2 & 1) != np2) {
            throw new CEFormatException("UCDecoder: Low byte parity error.");
        }
        outStream.write(high_byte);
        crc.update(high_byte);
        if (l == 2) {
            outStream.write(low_byte);
            crc.update(low_byte);
        }
    }
    private ByteArrayOutputStream lineAndSeq = new ByteArrayOutputStream(2);
    protected void decodeBufferPrefix(PushbackInputStream inStream, OutputStream outStream) {
        sequence = 0;
    }
    protected int decodeLinePrefix(PushbackInputStream inStream, OutputStream outStream)  throws IOException {
        int     i;
        int     nLen, nSeq;
        byte    xtmp[];
        int     c;
        crc.value = 0;
        while (true) {
            c = inStream.read(tmp, 0, 1);
            if (c == -1) {
                throw new CEStreamExhausted();
            }
            if (tmp[0] == '*') {
                break;
            }
        }
        lineAndSeq.reset();
        decodeAtom(inStream, lineAndSeq, 2);
        xtmp = lineAndSeq.toByteArray();
        nLen = xtmp[0] & 0xff;
        nSeq = xtmp[1] & 0xff;
        if (nSeq != sequence) {
            throw new CEFormatException("UCDecoder: Out of sequence line.");
        }
        sequence = (sequence + 1) & 0xff;
        return (nLen);
    }
    protected void decodeLineSuffix(PushbackInputStream inStream, OutputStream outStream) throws IOException {
        int i;
        int lineCRC = crc.value;
        int readCRC;
        byte tmp[];
        lineAndSeq.reset();
        decodeAtom(inStream, lineAndSeq, 2);
        tmp = lineAndSeq.toByteArray();
        readCRC = ((tmp[0] << 8) & 0xFF00) + (tmp[1] & 0xff);
        if (readCRC != lineCRC) {
            throw new CEFormatException("UCDecoder: CRC check failed.");
        }
    }
}
