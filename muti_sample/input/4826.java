public class ReachableExcludesImpl implements ReachableExcludes {
    private File excludesFile;
    private long lastModified;
    private Hashtable methods;  
    public ReachableExcludesImpl(File excludesFile) {
        this.excludesFile = excludesFile;
        readFile();
    }
    private void readFileIfNeeded() {
        if (excludesFile.lastModified() != lastModified) {
            synchronized(this) {
                if (excludesFile.lastModified() != lastModified) {
                    readFile();
                }
            }
        }
    }
    private void readFile() {
        long lm = excludesFile.lastModified();
        Hashtable<String, String> m = new Hashtable<String, String>();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                                    new FileInputStream(excludesFile)));
            String method;
            while ((method = r.readLine()) != null) {
                m.put(method, method);
            }
            lastModified = lm;
            methods = m;        
        } catch (IOException ex) {
            System.out.println("Error reading " + excludesFile + ":  " + ex);
        }
    }
    public boolean isExcluded(String fieldName) {
        readFileIfNeeded();
        return methods.get(fieldName) != null;
    }
}
