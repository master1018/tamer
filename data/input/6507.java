public class ProxyArrays implements Benchmark {
    static class DummyHandler implements InvocationHandler, Serializable {
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            return null;
        }
    }
    static interface DummyInterface {
        public void foo();
    }
    public long run(String[] args) throws Exception {
        int size = Integer.parseInt(args[0]);
        int nbatches = Integer.parseInt(args[1]);
        int ncycles = Integer.parseInt(args[2]);
        Proxy[][] arrays = genArrays(size, ncycles);
        StreamBuffer sbuf = new StreamBuffer();
        ObjectOutputStream oout =
            new ObjectOutputStream(sbuf.getOutputStream());
        ObjectInputStream oin =
            new ObjectInputStream(sbuf.getInputStream());
        doReps(oout, oin, sbuf, arrays, 1);     
        long start = System.currentTimeMillis();
        doReps(oout, oin, sbuf, arrays, nbatches);
        return System.currentTimeMillis() - start;
    }
    Proxy[][] genArrays(int size, int narrays) throws Exception {
        Class proxyClass =
            Proxy.getProxyClass(DummyInterface.class.getClassLoader(),
                    new Class[] { DummyInterface.class });
        Constructor proxyCons =
            proxyClass.getConstructor(new Class[] { InvocationHandler.class });
        Object[] consArgs = new Object[] { new DummyHandler() };
        Proxy[][] arrays = new Proxy[narrays][size];
        for (int i = 0; i < narrays; i++) {
            for (int j = 0; j < size; j++) {
                arrays[i][j] = (Proxy) proxyCons.newInstance(consArgs);
            }
        }
        return arrays;
    }
    void doReps(ObjectOutputStream oout, ObjectInputStream oin,
                StreamBuffer sbuf, Proxy[][] arrays, int nbatches)
        throws Exception
    {
        int ncycles = arrays.length;
        for (int i = 0; i < nbatches; i++) {
            sbuf.reset();
            oout.reset();
            for (int j = 0; j < ncycles; j++) {
                oout.writeObject(arrays[j]);
            }
            oout.flush();
            for (int j = 0; j < ncycles; j++) {
                oin.readObject();
            }
        }
    }
}
