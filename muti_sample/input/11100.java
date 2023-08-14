public class BitFile {
    ImageOutputStream output;
    byte buffer[];
    int index;
    int bitsLeft; 
    boolean blocks = false;
    public BitFile(ImageOutputStream output, boolean blocks) {
        this.output = output;
        this.blocks = blocks;
        buffer = new byte[256];
        index = 0;
        bitsLeft = 8;
    }
    public void flush() throws IOException {
        int numBytes = index + (bitsLeft == 8 ? 0 : 1);
        if (numBytes > 0) {
            if (blocks) {
                output.write(numBytes);
            }
            output.write(buffer, 0, numBytes);
            buffer[0] = 0;
            index = 0;
            bitsLeft = 8;
        }
    }
    public void writeBits(int bits, int numbits) throws IOException {
        int bitsWritten = 0;
        int numBytes = 255;  
        do {
            if ((index == 254 && bitsLeft == 0) || index > 254) {
                if (blocks) {
                    output.write(numBytes);
                }
                output.write(buffer, 0, numBytes);
                buffer[0] = 0;
                index = 0;
                bitsLeft = 8;
            }
            if (numbits <= bitsLeft) { 
                if (blocks) { 
                    buffer[index] |= (bits & ((1 << numbits) - 1)) << (8 - bitsLeft);
                    bitsWritten += numbits;
                    bitsLeft -= numbits;
                    numbits = 0;
                } else {
                    buffer[index] |= (bits & ((1 << numbits) - 1)) << (bitsLeft - numbits);
                    bitsWritten += numbits;
                    bitsLeft -= numbits;
                    numbits = 0;
                }
            } else { 
                if (blocks) { 
                    buffer[index] |= (bits & ((1 << bitsLeft) - 1)) << (8 - bitsLeft);
                    bitsWritten += bitsLeft;
                    bits >>= bitsLeft;
                    numbits -= bitsLeft;
                    buffer[++index] = 0;
                    bitsLeft = 8;
                } else {
                    int topbits = (bits >>> (numbits - bitsLeft)) & ((1 << bitsLeft) - 1);
                    buffer[index] |= topbits;
                    numbits -= bitsLeft;  
                    bitsWritten += bitsLeft;
                    buffer[++index] = 0;  
                    bitsLeft = 8;
                }
            }
        } while (numbits != 0);
    }
}
