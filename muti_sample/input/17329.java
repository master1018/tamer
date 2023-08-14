public class Cons implements Benchmark {
    static class Dummy implements Serializable {
    }
    public long run(String[] args) throws Exception {
        int reps = Integer.parseInt(args[0]);
        Dummy dummy = new Dummy();
        StreamBuffer sbuf = new StreamBuffer();
        doReps(sbuf, dummy, 1);         
        long start = System.currentTimeMillis();
        doReps(sbuf, dummy, reps);
        return System.currentTimeMillis() - start;
    }
    void doReps(StreamBuffer sbuf, Dummy dummy, int reps) throws Exception {
        OutputStream out = sbuf.getOutputStream();
        InputStream in = sbuf.getInputStream();
        for (int i = 0; i < reps; i++) {
            sbuf.reset();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(dummy);
            oout.flush();
            ObjectInputStream oin = new ObjectInputStream(in);
            oin.readObject();
        }
    }
}
