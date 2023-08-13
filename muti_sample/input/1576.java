public class UnicodeCleanup {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            delete(new File(args[i]));
        }
    }
    private static void delete(File file) {
        String name = file.toString();
        if (name.equals(".") || name.equals("..") ||
                name.endsWith(File.separator + ".") ||
                name.endsWith(File.separator + "..")) {
            throw new RuntimeException("too risky to process: " + name);
        }
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            for (int i = 0; i < contents.length; i++) {
                delete(contents[i]);
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Unable to delete " + file);
        }
    }
}
