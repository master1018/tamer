public class Cleanup {
    public static void main(String[] args) {
        File file = new File(args[0]);
        String name = file.toString();
        if (name.equals(".") || name.equals("..") ||
                name.endsWith(File.separator + ".") ||
                name.endsWith(File.separator + "..")) {
            throw new RuntimeException("too risky to process: " + name);
        }
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            for (int i = 0; i < contents.length; i++) {
                contents[i].delete();
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Unable to delete " + file);
        }
    }
}
