public class ClassLoading implements Benchmark {
    static final String ALTROOT = "!/bench/rmi/altroot/";
    static final String CLASSNAME = "Node";
    public long run(String[] args) throws Exception {
        int reps = Integer.parseInt(args[0]);
        CodeSource csrc = getClass().getProtectionDomain().getCodeSource();
        String url = "jar:" + csrc.getLocation().toString() + ALTROOT;
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++)
            RMIClassLoader.loadClass(url, CLASSNAME);
        long time = System.currentTimeMillis() - start;
        return time;
    }
}
