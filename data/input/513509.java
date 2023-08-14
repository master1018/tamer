public class TouchDex {
    public static int start(String dexFiles) {
        return trampoline(dexFiles, System.getProperty("java.boot.class.path"));
    }
    native private static int trampoline(String dexFiles, String bcp);
    public static void main(String[] args) {
        if ("-".equals(args[0])) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in), 256);
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    prepFiles(line);
                }
            } catch (IOException ex) {
                throw new RuntimeException ("Error processing stdin");
            }
        } else {
            prepFiles(args[0]);
        }
        System.out.println(" Prep complete");
    }
    private static String expandDirectories(String dexPath) {
        String[] parts = dexPath.split(":");
        StringBuilder outPath = new StringBuilder(dexPath.length());
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || name.endsWith(".apk");
            }
        };
        for (String part: parts) {
            File f = new File(part);
            if (f.isFile()) {
                outPath.append(part);
                outPath.append(':');
            } else if (f.isDirectory()) {
                String[] filenames = f.list(filter);
                if (filenames == null) {
                    System.err.println("I/O error with directory: " + part);
                    continue;
                }
                for (String filename: filenames) {
                    outPath.append(part);
                    outPath.append(File.separatorChar);
                    outPath.append(filename);
                    outPath.append(':');
                }
            } else {
                System.err.println("File not found: " + part);
            }
        }
        return outPath.toString();
    }
    private static void prepFiles(String dexPath) {
        System.out.println(" Prepping: " + dexPath);
        TouchDexLoader loader
                = new TouchDexLoader(expandDirectories(dexPath), null);
        try {
            loader.loadClass("com.google.NonexistentClassNeverFound");
            throw new RuntimeException("nonexistent class loaded?!");
        } catch (ClassNotFoundException cnfe) {
        }
    }
}
