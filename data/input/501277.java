public class DataInputStreamTest extends TestCase {
    @SmallTest
    public void testDataInputStream() throws Exception {
        String str = "AbCdEfGhIjKlM\nOpQ\rStUvWxYz";
        ByteArrayInputStream aa = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ba = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ca = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream da = new ByteArrayInputStream(str.getBytes());
        DataInputStream a = new DataInputStream(aa);
        try {
            assertEquals(str, IOUtil.read(a));
        } finally {
            a.close();
        }
        DataInputStream b = new DataInputStream(ba);
        try {
            assertEquals("AbCdEfGhIj", IOUtil.read(b, 10));
        } finally {
            b.close();
        }
        DataInputStream c = new DataInputStream(ca);
        try {
            assertEquals("bdfhjl\np\rtvxz", IOUtil.skipRead(c));
        } finally {
            c.close();
        }
        DataInputStream d = new DataInputStream(da);
        try {
            assertEquals("AbCdEfGhIjKlM", d.readLine());
            assertEquals("OpQ", d.readLine());
            assertEquals("StUvWxYz", d.readLine());
        } finally {
            d.close();
        }
        ByteArrayOutputStream e = new ByteArrayOutputStream();
        DataOutputStream f = new DataOutputStream(e);
        try {
            f.writeBoolean(true);
            f.writeByte('a');
            f.writeBytes("BCD");
            f.writeChar('e');
            f.writeChars("FGH");
            f.writeUTF("ijklm");
            f.writeDouble(1);
            f.writeFloat(2);
            f.writeInt(3);
            f.writeLong(4);
            f.writeShort(5);
        } finally {
            f.close();
        }
        ByteArrayInputStream ga = new ByteArrayInputStream(e.toByteArray());
        DataInputStream g = new DataInputStream(ga);
        try {
            assertTrue(g.readBoolean());
            assertEquals('a', g.readByte());
            assertEquals(2, g.skipBytes(2));
            assertEquals('D', g.readByte());
            assertEquals('e', g.readChar());
            assertEquals('F', g.readChar());
            assertEquals('G', g.readChar());
            assertEquals('H', g.readChar());
            assertEquals("ijklm", g.readUTF());
            assertEquals(1, g.readDouble(), 0);
            assertEquals(2f, g.readFloat(), 0f);
            assertEquals(3, g.readInt());
            assertEquals(4, g.readLong());
            assertEquals(5, g.readShort());
        } finally {
            g.close();
        }
    }
}
