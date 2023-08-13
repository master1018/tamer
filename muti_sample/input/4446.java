public class RepeatObjs implements Benchmark {
    static class Node implements Serializable {
    }
    public long run(String[] args) throws Exception {
        int size = Integer.parseInt(args[0]);
        int nbatches = Integer.parseInt(args[1]);
        Node[] objs = genObjs(size);
        StreamBuffer sbuf = new StreamBuffer();
        ObjectOutputStream oout =
            new ObjectOutputStream(sbuf.getOutputStream());
        ObjectInputStream oin =
            new ObjectInputStream(sbuf.getInputStream());
        doReps(oout, oin, sbuf, objs, 1);       
        long start = System.currentTimeMillis();
        doReps(oout, oin, sbuf, objs, nbatches);
        return System.currentTimeMillis() - start;
    }
    Node[] genObjs(int nobjs) {
        Node[] objs = new Node[nobjs];
        for (int i = 0; i < nobjs; i++)
            objs[i] = new Node();
        return objs;
    }
    void doReps(ObjectOutputStream oout, ObjectInputStream oin,
                StreamBuffer sbuf, Node[] objs, int nbatches)
        throws Exception
    {
        int nobjs = objs.length;
        for (int i = 0; i < nbatches; i++) {
            sbuf.reset();
            for (int j = 0; j < nobjs; j++) {
                oout.writeObject(objs[j]);
            }
            oout.flush();
            for (int j = 0; j < nobjs; j++) {
                oin.readObject();
            }
        }
    }
}
