public class ProxyArrayCalls implements Benchmark {
    static class DummyHandler implements InvocationHandler, Serializable {
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            return null;
        }
    }
    public static interface DummyInterface {
        public void foo();
    }
    interface Server extends Remote {
        public Proxy[] call(Proxy[] a) throws RemoteException;
    }
    static class ServerImpl extends UnicastRemoteObject implements Server {
        public ServerImpl() throws RemoteException {
        }
        public Proxy[] call(Proxy[] a) throws RemoteException {
            return a;
        }
    }
    static class ServerFactory implements BenchServer.RemoteObjectFactory {
        public Remote create() throws RemoteException {
            return new ServerImpl();
        }
    }
    Proxy[] genProxies(int size) throws Exception {
        Class proxyClass =
            Proxy.getProxyClass(DummyInterface.class.getClassLoader(),
                    new Class[] { DummyInterface.class });
        Constructor proxyCons =
            proxyClass.getConstructor(new Class[] { InvocationHandler.class });
        Object[] consArgs = new Object[] { new DummyHandler() };
        Proxy[] proxies = new Proxy[size];
        for (int i = 0; i < size; i++)
            proxies[i] = (Proxy) proxyCons.newInstance(consArgs);
        return proxies;
    }
    public long run(String[] args) throws Exception {
        int size = Integer.parseInt(args[0]);
        int reps = Integer.parseInt(args[1]);
        BenchServer bsrv = Main.getBenchServer();
        Server stub = (Server) bsrv.create(new ServerFactory());
        Proxy[] proxies = genProxies(size);
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++)
            stub.call(proxies);
        long time = System.currentTimeMillis() - start;
        bsrv.unexport(stub, true);
        return time;
    }
}
