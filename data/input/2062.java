public class Accordion {
    private static void readFully(InputStream s, byte[] buf) throws Throwable {
        int pos = 0;
        int n;
        while ((n = s.read(buf, pos, buf.length-pos)) > 0)
            pos += n;
        if (pos != buf.length)
            throw new Exception("Unexpected EOF");
    }
    private static volatile Throwable trouble;
    public static void main(String[] args) throws Throwable {
        if (args.length > 1)
            throw new Exception("Usage: java Accordion [BYTES]");
        final long bytes = args.length == 0 ? 10001 : Long.parseLong(args[0]);
        final int bufsize = 1729;
        final long count = bytes/bufsize;
        final PipedOutputStream out = new PipedOutputStream();
        final PipedInputStream in = new PipedInputStream(out);
        final Random random = new Random();
        final byte[] data = new byte[1729];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte)random.nextInt(255);
        System.out.println("count="+count);
        Thread compressor = new Thread() { public void run() {
            try (GZIPOutputStream s = new GZIPOutputStream(out)) {
                for (long i = 0; i < count; i++)
                    s.write(data, 0, data.length);
            } catch (Throwable t) { trouble = t; }}};
        Thread uncompressor = new Thread() { public void run() {
            try (GZIPInputStream s = new GZIPInputStream(in)) {
                final byte[] maybeBytes = new byte[data.length];
                for (long i = 0; i < count; i++) {
                    readFully(s, maybeBytes);
                    if (! Arrays.equals(data, maybeBytes))
                        throw new Exception("data corruption");
                }
                if (s.read(maybeBytes, 0, 1) > 0)
                    throw new Exception("Unexpected NON-EOF");
            } catch (Throwable t) { trouble = t; }}};
        compressor.start(); uncompressor.start();
        compressor.join();  uncompressor.join();
        if (trouble != null)
            throw trouble;
    }
}
