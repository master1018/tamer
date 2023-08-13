public class ExceptionCalls implements Benchmark {
    static class FooException extends Exception {
    }
    interface Server extends Remote {
        public void call() throws RemoteException, FooException;
    }
    static class ServerImpl extends UnicastRemoteObject implements Server {
        public ServerImpl() throws RemoteException {
        }
        public void call() throws RemoteException, FooException {
            throw new FooException();
        }
    }
    static class ServerFactory implements BenchServer.RemoteObjectFactory {
        public Remote create() throws RemoteException {
            return new ServerImpl();
        }
    }
    public long run(String[] args) throws Exception {
        int reps = Integer.parseInt(args[0]);
        BenchServer bsrv = Main.getBenchServer();
        Server stub = (Server) bsrv.create(new ServerFactory());
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            try {
                stub.call();
            } catch (FooException e) {}
        }
        long time = System.currentTimeMillis() - start;
        bsrv.unexport(stub, true);
        return time;
    }
}
