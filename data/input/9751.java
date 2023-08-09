public class Extrema {
    public static void main(String[] args) throws Exception {
        if (Double.MIN_VALUE != Double.longBitsToDouble(0x1L))
            throw new RuntimeException("Double.MIN_VALUE is not equal "+
                                       "to longBitsToDouble(0x1L).");
        if (Double.MIN_NORMAL != Double.longBitsToDouble(0x0010000000000000L))
            throw new RuntimeException("Double.MIN_NORMAL is not equal "+
                                       "to longBitsToDouble(0x0010000000000000L).");
        if (Double.MAX_VALUE != Double.longBitsToDouble(0x7fefffffffffffffL))
            throw new RuntimeException("Double.MAX_VALUE is not equal "+
                                       "to longBitsToDouble(0x7fefffffffffffffL).");
    }
}
