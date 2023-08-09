public class DoubleArrayCalls implements Benchmark {
    interface Server extends Remote {
        public double[] call(double[] a) throws RemoteException;
    }
    static class ServerImpl extends UnicastRemoteObject implements Server {
        public ServerImpl() throws RemoteException {
        }
        public double[] call(double[] a) throws RemoteException {
            return a;
        }
    }
    static class ServerFactory implements BenchServer.RemoteObjectFactory {
        public Remote create() throws RemoteException {
            return new ServerImpl();
        }
    }
    public long run(String[] args) throws Exception {
        int size = Integer.parseInt(args[0]);
        int reps = Integer.parseInt(args[1]);
        BenchServer bsrv = Main.getBenchServer();
        Server stub = (Server) bsrv.create(new ServerFactory());
        double[] array = new double[size];
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++)
            stub.call(array);
        long time = System.currentTimeMillis() - start;
        bsrv.unexport(stub, true);
        return time;
    }
}
