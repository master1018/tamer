public class MetaIndex {
    private static volatile Map<File, MetaIndex> jarMap;
    private String[] contents;
    private boolean isClassOnlyJar;
    public static MetaIndex forJar(File jar) {
        return getJarMap().get(jar);
    }
    public static synchronized void registerDirectory(File dir) {
        File indexFile = new File(dir, "meta-index");
        if (indexFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(indexFile));
                String line = null;
                String curJarName = null;
                boolean isCurJarContainClassOnly = false;
                List<String> contents = new ArrayList<String>();
                Map<File, MetaIndex> map = getJarMap();
                dir = dir.getCanonicalFile();
                line = reader.readLine();
                if (line == null ||
                    !line.equals("% VERSION 2")) {
                    reader.close();
                    return;
                }
                while ((line = reader.readLine()) != null) {
                    switch (line.charAt(0)) {
                    case '!':
                    case '#':
                    case '@': {
                        if ((curJarName != null) && (contents.size() > 0)) {
                            map.put(new File(dir, curJarName),
                                    new MetaIndex(contents,
                                                  isCurJarContainClassOnly));
                            contents.clear();
                        }
                        curJarName = line.substring(2);
                        if (line.charAt(0) == '!') {
                            isCurJarContainClassOnly = true;
                        } else if (isCurJarContainClassOnly) {
                            isCurJarContainClassOnly = false;
                        }
                        break;
                    }
                    case '%':
                        break;
                    default: {
                        contents.add(line);
                    }
                    }
                }
                if ((curJarName != null) && (contents.size() > 0)) {
                    map.put(new File(dir, curJarName),
                            new MetaIndex(contents, isCurJarContainClassOnly));
                }
                reader.close();
            } catch (IOException e) {
            }
        }
    }
    public boolean mayContain(String entry) {
        if  (isClassOnlyJar && !entry.endsWith(".class")){
            return false;
        }
        String[] conts = contents;
        for (int i = 0; i < conts.length; i++) {
            if (entry.startsWith(conts[i])) {
                return true;
            }
        }
        return false;
    }
    private MetaIndex(List<String> entries, boolean isClassOnlyJar)
        throws IllegalArgumentException {
        if (entries == null) {
            throw new IllegalArgumentException();
        }
        contents = entries.toArray(new String[0]);
        this.isClassOnlyJar = isClassOnlyJar;
    }
    private static Map<File, MetaIndex> getJarMap() {
        if (jarMap == null) {
            synchronized (MetaIndex.class) {
                if (jarMap == null) {
                    jarMap = new HashMap<File, MetaIndex>();
                }
            }
        }
        assert jarMap != null;
        return jarMap;
    }
}
