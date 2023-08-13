public class SetTTLAndGetTTL {
    public static void main(String args[]) throws Exception {
        MulticastSocket soc = null;
        try {
            soc = new MulticastSocket();
        } catch(Exception e) {
            throw new Exception("Unexpected Exception");
        }
        soc.setTTL((byte)200);
        byte ttlValue = soc.getTTL();
        if (ttlValue != (byte)200)
            throw new Exception("setTTL/getTTL is broken");
    }
}
