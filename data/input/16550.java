public class LimitDirectMemory {
    private static int K = 1024;
    public static void main(String [] args) throws Exception {
        if (args.length < 2)
            throw new RuntimeException();
        boolean throwp = parseThrow(args[0]);
        int size = parseSize(args[1]);
        int incr = (args.length > 2 ? parseSize(args[2]) : size);
        Properties p = System.getProperties();
        if (p.getProperty("sun.nio.MaxDirectMemorySize") != null)
            throw new RuntimeException("sun.nio.MaxDirectMemorySize defined");
        ByteBuffer [] b = new ByteBuffer[K];
        int i = 0;
        while (size >= incr) {
            b[i++] = ByteBuffer.allocateDirect(incr);
            size -= incr;
        }
        if (throwp) {
            try {
                b[i] = ByteBuffer.allocateDirect(incr);
                throw new RuntimeException("OutOfMemoryError not thrown: "
                                           + incr);
            } catch (OutOfMemoryError e) {
                e.printStackTrace(System.out);
                System.out.println("OK - Error thrown as expected ");
            }
        } else {
            b[i] = ByteBuffer.allocateDirect(incr);
            System.out.println("OK - Error not thrown");
        }
    }
    private static boolean parseThrow(String s) {
        if (s.equals("true"))  return true;
        if (s.equals("false")) return false;
        throw new RuntimeException("Unrecognized expectation: " + s);
    }
    private static int parseSize(String size) throws Exception {
        if (size.equals("DEFAULT"))
            return (int)Runtime.getRuntime().maxMemory();
        if (size.equals("DEFAULT+1"))
            return (int)Runtime.getRuntime().maxMemory() + 1;
        if (size.equals("DEFAULT+1M"))
            return (int)Runtime.getRuntime().maxMemory() + (1 << 20);
        if (size.equals("DEFAULT-1"))
            return (int)Runtime.getRuntime().maxMemory() - 1;
        if (size.equals("DEFAULT/2"))
            return (int)Runtime.getRuntime().maxMemory() / 2;
        int idx = 0, len = size.length();
        int result = 1;
        for (int i = 0; i < len; i++) {
            if (Character.isDigit(size.charAt(i))) idx++;
            else break;
        }
        if (idx == 0)
            throw new RuntimeException("No digits detected: " + size);
        result = Integer.parseInt(size.substring(0, idx));
        if (idx < len) {
            for (int i = idx; i < len; i++) {
                switch(size.charAt(i)) {
                case 'T': case 't': result *= K; 
                case 'G': case 'g': result *= K; 
                case 'M': case 'm': result *= K; 
                case 'K': case 'k': result *= K;
                    break;
                default:
                    throw new RuntimeException("Unrecognized size: " + size);
                }
            }
        }
        return result;
    }
}
