public class ShutdownHooks {
    private static File file;
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: ShutdownHooks <dir> <filename>");
        }
        Runtime.getRuntime().addShutdownHook(new Cleaner());
        File dir = new File(args[0]);
        file = new File(dir, args[1]);
        System.out.println("writing to "+ file);
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("Shutdown begins");
        }
    }
    public static class Cleaner extends Thread {
        public void run() {
            Console cons = System.console();
            file.deleteOnExit();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("file is being deleted");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
