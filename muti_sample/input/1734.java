public class JarIndex {
    private HashMap indexMap;
    private HashMap jarMap;
    private String[] jarFiles;
    public static final String INDEX_NAME = "META-INF/INDEX.LIST";
    private static final boolean metaInfFilenames =
        "true".equals(System.getProperty("sun.misc.JarIndex.metaInfFilenames"));
    public JarIndex() {
        indexMap = new HashMap();
        jarMap = new HashMap();
    }
    public JarIndex(InputStream is) throws IOException {
        this();
        read(is);
    }
    public JarIndex(String[] files) throws IOException {
        this();
        this.jarFiles = files;
        parseJars(files);
    }
    public static JarIndex getJarIndex(JarFile jar) throws IOException {
        return getJarIndex(jar, null);
    }
    public static JarIndex getJarIndex(JarFile jar, MetaIndex metaIndex) throws IOException {
        JarIndex index = null;
        if (metaIndex != null &&
            !metaIndex.mayContain(INDEX_NAME)) {
            return null;
        }
        JarEntry e = jar.getJarEntry(INDEX_NAME);
        if (e != null) {
            index = new JarIndex(jar.getInputStream(e));
        }
        return index;
    }
    public String[] getJarFiles() {
        return jarFiles;
    }
    private void addToList(String key, String value, HashMap t) {
        LinkedList list = (LinkedList)t.get(key);
        if (list == null) {
            list = new LinkedList();
            list.add(value);
            t.put(key, list);
        } else if (!list.contains(value)) {
            list.add(value);
        }
    }
    public LinkedList get(String fileName) {
        LinkedList jarFiles = null;
        if ((jarFiles = (LinkedList)indexMap.get(fileName)) == null) {
            int pos;
            if((pos = fileName.lastIndexOf("/")) != -1) {
                jarFiles = (LinkedList)indexMap.get(fileName.substring(0, pos));
            }
        }
        return jarFiles;
    }
    public void add(String fileName, String jarName) {
        String packageName;
        int pos;
        if((pos = fileName.lastIndexOf("/")) != -1) {
            packageName = fileName.substring(0, pos);
        } else {
            packageName = fileName;
        }
        addToList(packageName, jarName, indexMap);
        addToList(jarName, packageName, jarMap);
    }
    private void addExplicit(String fileName, String jarName) {
        addToList(fileName, jarName, indexMap);
        addToList(jarName, fileName, jarMap);
     }
    private void parseJars(String[] files) throws IOException {
        if (files == null) {
            return;
        }
        String currentJar = null;
        for (int i = 0; i < files.length; i++) {
            currentJar = files[i];
            ZipFile zrf = new ZipFile(currentJar.replace
                                      ('/', File.separatorChar));
            Enumeration entries = zrf.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String fileName = entry.getName();
                if (fileName.equals("META-INF/") ||
                    fileName.equals(INDEX_NAME) ||
                    fileName.equals(JarFile.MANIFEST_NAME))
                    continue;
                if (!metaInfFilenames) {
                    add(fileName, currentJar);
                } else {
                    if (!fileName.startsWith("META-INF/")) {
                        add(fileName, currentJar);
                    } else if (!entry.isDirectory()) {
                        addExplicit(fileName, currentJar);
                    }
                }
            }
            zrf.close();
        }
    }
    public void write(OutputStream out) throws IOException {
        BufferedWriter bw = new BufferedWriter
            (new OutputStreamWriter(out, "UTF8"));
        bw.write("JarIndex-Version: 1.0\n\n");
        if (jarFiles != null) {
            for (int i = 0; i < jarFiles.length; i++) {
                String jar = jarFiles[i];
                bw.write(jar + "\n");
                LinkedList jarlist = (LinkedList)jarMap.get(jar);
                if (jarlist != null) {
                    Iterator listitr = jarlist.iterator();
                    while(listitr.hasNext()) {
                        bw.write((String)(listitr.next()) + "\n");
                    }
                }
                bw.write("\n");
            }
            bw.flush();
        }
    }
    public void read(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader
            (new InputStreamReader(is, "UTF8"));
        String line = null;
        String currentJar = null;
        Vector jars = new Vector();
        while((line = br.readLine()) != null && !line.endsWith(".jar"));
        for(;line != null; line = br.readLine()) {
            if (line.length() == 0)
                continue;
            if (line.endsWith(".jar")) {
                currentJar = line;
                jars.add(currentJar);
            } else {
                String name = line;
                addToList(name, currentJar, indexMap);
                addToList(currentJar, name, jarMap);
            }
        }
        jarFiles = (String[])jars.toArray(new String[jars.size()]);
    }
    public void merge(JarIndex toIndex, String path) {
        Iterator itr = indexMap.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry e = (Map.Entry)itr.next();
            String packageName = (String)e.getKey();
            LinkedList from_list = (LinkedList)e.getValue();
            Iterator listItr = from_list.iterator();
            while(listItr.hasNext()) {
                String jarName = (String)listItr.next();
                if (path != null) {
                    jarName = path.concat(jarName);
                }
                toIndex.add(packageName, jarName);
            }
        }
    }
}
