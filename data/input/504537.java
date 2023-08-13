public class DeleteOnExit extends Thread {
    private static DeleteOnExit instance;
    private ArrayList<String> files = new ArrayList<String>();
    public static synchronized DeleteOnExit getInstance() {
        if (instance == null) {
            instance = new DeleteOnExit();
            Runtime.getRuntime().addShutdownHook(instance);
        }
        return instance;
    }
    public void addFile(String filename) {
        synchronized(files) {
            if (!files.contains(filename)) {
                files.add(filename);
            }
        }
    }
    @Override
    public void run() {
        Collections.sort(files);
        for (int i = files.size() - 1; i >= 0; i--) {
            new File(files.get(i)).delete();
        }
    }
}
