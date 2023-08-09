public class Strings implements Benchmark {
    public long run(String[] args) throws Exception {
        int slen = Integer.parseInt(args[0]);
        int nbatches = Integer.parseInt(args[1]);
        int ncycles = Integer.parseInt(args[2]);
        String[] strs = genStrings(slen, ncycles);
        StreamBuffer sbuf = new StreamBuffer();
        ObjectOutputStream oout =
            new ObjectOutputStream(sbuf.getOutputStream());
        ObjectInputStream oin =
            new ObjectInputStream(sbuf.getInputStream());
        doReps(oout, oin, sbuf, strs, 1, ncycles);      
        long start = System.currentTimeMillis();
        doReps(oout, oin, sbuf, strs, nbatches, ncycles);
        return System.currentTimeMillis() - start;
    }
    String[] genStrings(int len, int nstrings) {
        String[] strs = new String[nstrings];
        char[] ca = new char[len];
        Random rand = new Random(System.currentTimeMillis());
        for (int i = 0; i < nstrings; i++) {
            for (int j = 0; j < len; j++) {
                ca[j] = (char) rand.nextInt();
            }
            strs[i] = new String(ca);
        }
        return strs;
    }
    void doReps(ObjectOutputStream oout, ObjectInputStream oin,
                StreamBuffer sbuf, String[] strs, int nbatches, int ncycles)
        throws Exception
    {
        for (int i = 0; i < nbatches; i++) {
            sbuf.reset();
            oout.reset();
            for (int j = 0; j < ncycles; j++) {
                oout.writeObject(strs[j]);
            }
            oout.flush();
            for (int j = 0; j < ncycles; j++) {
                oin.readObject();
            }
        }
    }
}
