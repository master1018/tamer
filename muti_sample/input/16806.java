public class TimeToLive {
    static int[] new_ttls = { 0, 1, 127, 254, 255 };
    static int[] bad_ttls = { -1, 256 };
    public static void main(String[] args) throws Exception {
        MulticastSocket socket = new MulticastSocket(6789);
        int ttl = socket.getTimeToLive();
        System.out.println("default ttl: " + ttl);
        for (int i = 0; i < new_ttls.length; i++) {
            socket.setTimeToLive(new_ttls[i]);
            if (!(new_ttls[i] == socket.getTimeToLive())) {
                throw new RuntimeException("test failure, set/get differ: " +
                                           new_ttls[i] + " /  " +
                                           socket.getTimeToLive());
            }
        }
        for (int j = 0; j < bad_ttls.length; j++) {
            boolean exception = false;
            try {
                socket.setTimeToLive(bad_ttls[j]);
            } catch (IllegalArgumentException e) {
                exception = true;
            }
            if (!exception) {
                throw new RuntimeException("bad argument accepted: " + bad_ttls[j]);
            }
        }
    }
}
