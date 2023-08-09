public class ProxyClassDesc implements Benchmark {
    static interface A1 {};
    static interface A2 {};
    static interface A3 {};
    static interface A4 {};
    static interface A5 {};
    static interface B1 {};
    static interface B2 {};
    static interface B3 {};
    static interface B4 {};
    static interface B5 {};
    static interface C1 {};
    static interface C2 {};
    static interface C3 {};
    static interface C4 {};
    static interface C5 {};
    public long run(String[] args) throws Exception {
        int ncycles = Integer.parseInt(args[0]);
        StreamBuffer sbuf = new StreamBuffer();
        ObjectOutputStream oout =
            new ObjectOutputStream(sbuf.getOutputStream());
        ObjectInputStream oin =
            new ObjectInputStream(sbuf.getInputStream());
        ObjectStreamClass[] descs = genDescs();
        doReps(oout, oin, sbuf, descs, 1);      
        long start = System.currentTimeMillis();
        doReps(oout, oin, sbuf, descs, ncycles);
        return System.currentTimeMillis() - start;
    }
    ObjectStreamClass[] genDescs() {
        ClassLoader ldr = ProxyClassDesc.class.getClassLoader();
        Class[] ifaces = new Class[3];
        Class[] a =
            new Class[] { A1.class, A2.class, A3.class, A4.class, A5.class };
        Class[] b =
            new Class[] { B1.class, B2.class, B3.class, B4.class, B5.class };
        Class[] c =
            new Class[] { C1.class, C2.class, C3.class, C4.class, C5.class };
        ObjectStreamClass[] descs =
            new ObjectStreamClass[a.length * b.length * c.length];
        int n = 0;
        for (int i = 0; i < a.length; i++) {
            ifaces[0] = a[i];
            for (int j = 0; j < b.length; j++) {
                ifaces[1] = b[j];
                for (int k = 0; k < c.length; k++) {
                    ifaces[2] = c[k];
                    Class proxyClass = Proxy.getProxyClass(ldr, ifaces);
                    descs[n++] = ObjectStreamClass.lookup(proxyClass);
                }
            }
        }
        return descs;
    }
    void doReps(ObjectOutputStream oout, ObjectInputStream oin,
                StreamBuffer sbuf, ObjectStreamClass[] descs, int ncycles)
        throws Exception
    {
        int ndescs = descs.length;
        for (int i = 0; i < ncycles; i++) {
            sbuf.reset();
            oout.reset();
            for (int j = 0; j < ndescs; j++) {
                oout.writeObject(descs[j]);
            }
            oout.flush();
            for (int j = 0; j < ndescs; j++) {
                oin.readObject();
            }
        }
    }
}
