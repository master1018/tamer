public class Chars implements Benchmark {
    public long run(String[] args) throws Exception {
        int nbatches = Integer.parseInt(args[0]);
        int ncycles = Integer.parseInt(args[1]);
        StreamBuffer sbuf = new StreamBuffer();
        ObjectOutputStream oout =
            new ObjectOutputStream(sbuf.getOutputStream());
        ObjectInputStream oin =
            new ObjectInputStream(sbuf.getInputStream());
        doReps(oout, oin, sbuf, 1, ncycles);    
        long start = System.currentTimeMillis();
        doReps(oout, oin, sbuf, nbatches, ncycles);
        return System.currentTimeMillis() - start;
    }
    void doReps(ObjectOutputStream oout, ObjectInputStream oin,
                StreamBuffer sbuf, int nbatches, int ncycles)
        throws Exception
    {
        for (int i = 0; i < nbatches; i++) {
            sbuf.reset();
            for (int j = 0; j < ncycles; j++) {
                oout.writeChar('0');
            }
            oout.flush();
            for (int j = 0; j < ncycles; j++) {
                oin.readChar();
            }
        }
    }
}
