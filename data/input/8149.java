public class GetPutFieldTrees implements Benchmark {
    static class Node implements Serializable {
        boolean z;
        byte b;
        char c;
        short s;
        int i;
        float f;
        long j;
        double d;
        String str = "bodega";
        Object parent, left, right;
        Node(Object parent, int depth) {
            this.parent = parent;
            if (depth > 0) {
                left = new Node(this, depth - 1);
                right = new Node(this, depth - 1);
            }
        }
        private void writeObject(ObjectOutputStream out) throws IOException {
            ObjectOutputStream.PutField fields = out.putFields();
            fields.put("z", z);
            fields.put("b", b);
            fields.put("c", c);
            fields.put("s", s);
            fields.put("i", i);
            fields.put("f", f);
            fields.put("j", j);
            fields.put("d", d);
            fields.put("str", str);
            fields.put("parent", parent);
            fields.put("left", left);
            fields.put("right", right);
            out.writeFields();
        }
        private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            ObjectInputStream.GetField fields = in.readFields();
            z = fields.get("z", false);
            b = fields.get("b", (byte) 0);
            c = fields.get("c", (char) 0);
            s = fields.get("s", (short) 0);
            i = fields.get("i", (int) 0);
            f = fields.get("f", (float) 0.0);
            j = fields.get("j", (long) 0);
            d = fields.get("d", (double) 0.0);
            str = (String) fields.get("str", null);
            parent = fields.get("parent", null);
            left = fields.get("left", null);
            right = fields.get("right", null);
        }
    }
    public long run(String[] args) throws Exception {
        int depth = Integer.parseInt(args[0]);
        int nbatches = Integer.parseInt(args[1]);
        int ncycles = Integer.parseInt(args[2]);
        Node[] trees = genTrees(depth, ncycles);
        StreamBuffer sbuf = new StreamBuffer();
        ObjectOutputStream oout =
            new ObjectOutputStream(sbuf.getOutputStream());
        ObjectInputStream oin =
            new ObjectInputStream(sbuf.getInputStream());
        doReps(oout, oin, sbuf, trees, 1);      
        long start = System.currentTimeMillis();
        doReps(oout, oin, sbuf, trees, nbatches);
        return System.currentTimeMillis() - start;
    }
    Node[] genTrees(int depth, int ntrees) {
        Node[] trees = new Node[ntrees];
        for (int i = 0; i < ntrees; i++) {
            trees[i] = new Node(null, depth);
        }
        return trees;
    }
    void doReps(ObjectOutputStream oout, ObjectInputStream oin,
                StreamBuffer sbuf, Node[] trees, int nbatches)
        throws Exception
    {
        int ncycles = trees.length;
        for (int i = 0; i < nbatches; i++) {
            sbuf.reset();
            oout.reset();
            for (int j = 0; j < ncycles; j++) {
                oout.writeObject(trees[j]);
            }
            oout.flush();
            for (int j = 0; j < ncycles; j++) {
                oin.readObject();
            }
        }
    }
}
