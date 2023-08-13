public class ExportObjs implements Benchmark {
    static class RemoteObj implements Remote {
    }
    public long run(String[] args) throws Exception {
        int size = Integer.parseInt(args[0]);
        Remote[] objs = new Remote[size];
        for (int i = 0; i < size; i++)
            objs[i] = new RemoteObj();
        long start = System.currentTimeMillis();
        for (int i = 0; i < size; i++)
            UnicastRemoteObject.exportObject(objs[i],0);
        long time = System.currentTimeMillis() - start;
        for (int i = 0; i < size; i++)
            UnicastRemoteObject.unexportObject(objs[i], true);
        return time;
    }
}
