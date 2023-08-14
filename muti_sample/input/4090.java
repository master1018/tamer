public class StrCodingBenchmarkUTF8 {
    public static void main(String[] args) throws Throwable {
        final int itrs = Integer.getInteger("iterations", 100000);
        final int size = 2048;
        final int subsize    = Integer.getInteger("subsize", 128);
        final Random rnd = new Random();
        final int maxchar    = 0x7f;
        Charset charset = Charset.forName("UTF-8");
        final String csn = charset.name();
        final Charset cs = charset;
        int[] starts = new int[] { 0, 0x80, 0x800, 0x10000};
        for (int nb = 1; nb <= 4; nb++) {
            final CharsetEncoder enc = cs.newEncoder();
            char[] cc = new char[size];
            int i = 0;
            while (i < size - 3) {
                i += Character.toChars(starts[nb - 1] + rnd.nextInt(maxchar), cc, i);
            }
            final String string = new String(cc);
            final byte[] bytes  = string.getBytes(cs);
            System.out.printf("%n--------%s[nb=%d]---------%n", csn, nb);
            int sz = 12;
            while (sz < size) {
                System.out.printf("   [len=%d]%n", sz);
                final byte[] bs  = Arrays.copyOf(bytes, sz);
                final String str = new String(bs, csn);
                StrCodingBenchmark.Job[] jobs = {
                    new StrCodingBenchmark.Job("String decode: csn") {
                    public void work() throws Throwable {
                        for (int i = 0; i < itrs; i++)
                            new String(bs, csn);
                    }},
                    new StrCodingBenchmark.Job("String decode: cs") {
                    public void work() throws Throwable {
                        for (int i = 0; i < itrs; i++)
                            new String(bs, cs);
                    }},
                    new StrCodingBenchmark.Job("String encode: csn") {
                    public void work() throws Throwable {
                        for (int i = 0; i < itrs; i++)
                                str.getBytes(csn);
                    }},
                    new StrCodingBenchmark.Job("String encode: cs") {
                    public void work() throws Throwable {
                         for (int i = 0; i < itrs; i++)
                          str.getBytes(cs);
                    }},
                };
                StrCodingBenchmark.time(StrCodingBenchmark.filter(null, jobs));
                sz <<= 1;
            }
        }
    }
}
