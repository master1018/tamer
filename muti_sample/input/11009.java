public class HttpCapture {
    private File file = null;
    private boolean incoming = true;
    private BufferedWriter out = null;
    private static boolean initialized = false;
    private static volatile ArrayList<Pattern> patterns = null;
    private static volatile ArrayList<String> capFiles = null;
    private static synchronized void init() {
        initialized = true;
        String rulesFile = java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<String>() {
                public String run() {
                    return NetProperties.get("sun.net.http.captureRules");
                }
            });
        if (rulesFile != null && !rulesFile.isEmpty()) {
            BufferedReader in;
            try {
                in = new BufferedReader(new FileReader(rulesFile));
            } catch (FileNotFoundException ex) {
                return;
            }
            try {
                String line = in.readLine();
                while (line != null) {
                    line = line.trim();
                    if (!line.startsWith("#")) {
                        String[] s = line.split(",");
                        if (s.length == 2) {
                            if (patterns == null) {
                                patterns = new ArrayList<Pattern>();
                                capFiles = new ArrayList<String>();
                            }
                            patterns.add(Pattern.compile(s[0].trim()));
                            capFiles.add(s[1].trim());
                        }
                    }
                    line = in.readLine();
                }
            } catch (IOException ioe) {
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    private static synchronized boolean isInitialized() {
        return initialized;
    }
    private HttpCapture(File f, java.net.URL url) {
        file = f;
        try {
            out = new BufferedWriter(new FileWriter(file, true));
            out.write("URL: " + url + "\n");
        } catch (IOException ex) {
            PlatformLogger.getLogger(HttpCapture.class.getName()).severe(null, ex);
        }
    }
    public synchronized void sent(int c) throws IOException {
        if (incoming) {
            out.write("\n------>\n");
            incoming = false;
            out.flush();
        }
        out.write(c);
    }
    public synchronized void received(int c) throws IOException {
        if (!incoming) {
            out.write("\n<------\n");
            incoming = true;
            out.flush();
        }
        out.write(c);
    }
    public synchronized void flush() throws IOException {
        out.flush();
    }
    public static HttpCapture getCapture(java.net.URL url) {
        if (!isInitialized()) {
            init();
        }
        if (patterns == null || patterns.isEmpty()) {
            return null;
        }
        String s = url.toString();
        for (int i = 0; i < patterns.size(); i++) {
            Pattern p = patterns.get(i);
            if (p.matcher(s).find()) {
                String f = capFiles.get(i);
                File fi;
                if (f.indexOf("%d") >= 0) {
                    java.util.Random rand = new java.util.Random();
                    do {
                        String f2 = f.replace("%d", Integer.toString(rand.nextInt()));
                        fi = new File(f2);
                    } while (fi.exists());
                } else {
                    fi = new File(f);
                }
                return new HttpCapture(fi, url);
            }
        }
        return null;
    }
}
