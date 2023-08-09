public class SetTTLTo0 {
    public static void main(String args[]) throws Exception {
        MulticastSocket soc = null;
        try {
            soc = new MulticastSocket();
        } catch(Exception e) {
            throw new Exception("Unexpected Exception");
        }
        soc.setTTL((byte)0);
        soc.setTimeToLive(0);
    }
}
