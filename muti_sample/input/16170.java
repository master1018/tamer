public class ExternObjTrees implements Benchmark {
    static class Node implements Externalizable {
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
        public Node() {
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeBoolean(z);
            out.writeByte(b);
            out.writeChar(c);
            out.writeShort(s);
            out.writeInt(i);
            out.writeFloat(f);
            out.writeLong(j);
            out.writeDouble(d);
            out.writeObject(str);
            out.writeObject(parent);
            out.writeObject(left);
            out.writeObject(right);
        }
        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            z = in.readBoolean();
            b = in.readByte();
            c = in.readChar();
            s = in.readShort();
            i = in.readInt();
            f = in.readFloat();
            j = in.readLong();
            d = in.readDouble();
            str = (String) in.readObject();
            parent = in.readObject();
            left = in.readObject();
            right = in.readObject();
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
