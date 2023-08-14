public class CorruptedUTFConsumption {
    static Random rand = new Random(System.currentTimeMillis());
    public static void main(String[] args) throws Exception {
        StringBuffer sbuf = new StringBuffer();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        for (int i = 0; i < 1200; i++) {
            sbuf.append(i % 10);
            bout.reset();
            dout.writeUTF(sbuf.toString());
            byte[] utf = bout.toByteArray();
            utf[utf.length - 1] = (byte) (0xC0 | rand.nextInt() & 0x1F);
            checkConsume(utf);
            utf[utf.length - 1] = (byte) (0xE0 | rand.nextInt() & 0x0F);
            checkConsume(utf);
            if (utf.length >= 4) {      
                utf[utf.length - 2] = (byte) (0xE0 | rand.nextInt() & 0x0F);
                utf[utf.length - 1] = (byte) (0x80 | rand.nextInt() & 0x3F);
                checkConsume(utf);
            }
        }
    }
    static void checkConsume(byte[] utf) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.write(utf);
        oout.writeByte(0);      
        oout.close();
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        try {
            oin.readUTF();
            throw new Error();
        } catch (UTFDataFormatException ex) {
        }
        oin.readByte();
    }
}
