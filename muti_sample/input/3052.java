public class CheckBoundaries {
    public static void main(String[] args) {
        boolean exception = false;
        try {
            final int offset = Integer.MAX_VALUE;
            final int length = 1;
            new DatagramPacket(new byte[1024], offset, length);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        if (!exception)
            throw new RuntimeException("IllegalArgumentException not thrown!");
    }
}
