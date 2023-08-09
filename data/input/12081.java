public class UCEncoder extends CharacterEncoder {
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
    protected void encodeAtom(OutputStream outStream, byte data[], int offset, int len) throws IOException
    {
        int     i;
        int     p1, p2; 
        byte    a, b;
        a = data[offset];
        if (len == 2) {
            b = data[offset+1];
        } else {
            b = 0;
        }
        crc.update(a);
        if (len == 2) {
            crc.update(b);
        }
        outStream.write(map_array[((a >>> 2) & 0x38) + ((b >>> 5) & 0x7)]);
        p1 = 0; p2 = 0;
        for (i = 1; i < 256; i = i * 2) {
            if ((a & i) != 0) {
                p1++;
            }
            if ((b & i) != 0) {
                p2++;
            }
        }
        p1 = (p1 & 1) * 32;
        p2 = (p2 & 1) * 32;
        outStream.write(map_array[(a & 31) + p1]);
        outStream.write(map_array[(b & 31) + p2]);
        return;
    }
    protected void encodeLinePrefix(OutputStream outStream, int length) throws IOException {
        outStream.write('*');
        crc.value = 0;
        tmp[0] = (byte) length;
        tmp[1] = (byte) sequence;
        sequence = (sequence + 1) & 0xff;
        encodeAtom(outStream, tmp, 0, 2);
    }
    protected void encodeLineSuffix(OutputStream outStream) throws IOException {
        tmp[0] = (byte) ((crc.value >>> 8) & 0xff);
        tmp[1] = (byte) (crc.value & 0xff);
        encodeAtom(outStream, tmp, 0, 2);
        super.pStream.println();
    }
    protected void encodeBufferPrefix(OutputStream a) throws IOException {
        sequence = 0;
        super.encodeBufferPrefix(a);
    }
}
