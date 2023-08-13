public class KrbDataOutputStream extends BufferedOutputStream {
    public KrbDataOutputStream(OutputStream os) {
        super(os);
    }
    public void write32(int num) throws IOException {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)((num & 0xff000000) >> 24 & 0xff);
        bytes[1] = (byte)((num & 0x00ff0000) >> 16 & 0xff);
        bytes[2] = (byte)((num & 0x0000ff00) >> 8 & 0xff);
        bytes[3] = (byte)(num & 0xff);
        write(bytes, 0, 4);
    }
    public void write16(int num) throws IOException {
        byte[] bytes = new byte[2];
        bytes[0] = (byte)((num & 0xff00) >> 8 & 0xff);
        bytes[1] = (byte)(num & 0xff);
        write(bytes, 0, 2);
    }
    public void write8(int num) throws IOException {
        write(num & 0xff);
    }
}
