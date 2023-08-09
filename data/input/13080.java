public class Offset {
    public static void main(String args[]) throws Exception {
        byte b1[] = new byte[1024];
        DatagramPacket p = new DatagramPacket(b1, 512, 512 );
        byte b2[] = new byte[20];
        p.setData(b2);
        if (p.getOffset() != 0) {
            throw new Exception("setData(byte[]) didn't reset offset");
        }
    }
}
