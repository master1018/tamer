public class JarReorder {
    private PrintStream out;
    private void usage() {
        String help;
        help =
                "Usage:  jar JarReorder [-o <outputfile>] <order_list> <exclude_list> <file> ...\n"
                + "   order_list    is a file containing names of files to load\n"
                + "                 in order at the end of a jar file unless\n"
                + "                 excluded in the exclude list.\n"
                + "   exclude_list  is a file containing names of files/directories\n"
                + "                 NOT to be included in a jar file.\n"
                + "\n"
                + "The order_list or exclude_list may be replaced by a \"-\" if no\n"
                + "data is to be provided.\n"
                + "\n"
                + "   The remaining arguments are files or directories to be included\n"
                + "   in a jar file, from which will be excluded those entries which\n"
                + "   appear in the exclude list.\n";
        System.err.println(help);
    }
    public static void main(String[] args) {
        JarReorder jr = new JarReorder();
        jr.run(args);
    }
    private void run(String args[]) {
        int arglen = args.length;
        int argpos = 0;
        if (arglen > 0) {
            if (arglen >= 2 && args[0].equals("-o")) {
                try {
                    out = new PrintStream(new FileOutputStream(args[1]));
                } catch (FileNotFoundException e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
                argpos += 2;
                arglen -= 2;
            } else {
                System.err.println("Error: Illegal arg count");
                System.exit(1);
            }
        } else {
            out = System.out;
        }
        if (arglen <= 2) {
            usage();
            System.exit(1);
        }
        String classListFile = args[argpos];
        String excludeListFile = args[argpos + 1];
        argpos += 2;
        arglen -= 2;
        List<String> orderList = readListFromFile(classListFile, true);
        List<String> excludeList = readListFromFile(excludeListFile, false);
        Set<String> processed = new HashSet<String>();
        Set<String> excludeSet = new HashSet<String>(excludeList);
        Set<String> allFilesExcluded = expand(null, excludeSet, processed);
        processed.addAll(orderList);
        Set<String> inputSet = new HashSet<String>();
        for (int i = 0; i < arglen; ++i) {
            String name = args[argpos + i];
            name = cleanPath(new File(name));
            if ( name != null && name.length() > 0 && !inputSet.contains(name) ) {
                inputSet.add(name);
            }
        }
        Set<String> allFilesIncluded = expand(null, inputSet, processed);
        List<String> allFiles = new ArrayList<String>(allFilesIncluded);
        Collections.sort(allFiles);
        for (int i = orderList.size() - 1; i >= 0; --i) {
            String s = orderList.get(i);
            if (allFilesExcluded.contains(s)) {
                System.err.println("Included order file " + s
                    + " is also excluded, skipping.");
            } else if (new File(s).exists()) {
                allFiles.add(s);
            } else {
                System.err.println("Included order file " + s
                    + " missing, skipping.");
            }
        }
        for (String str : allFiles) {
            out.println(str);
        }
        out.flush();
        out.close();
    }
    private List<String> readListFromFile(String fileName,
            boolean addClassSuffix) {
        BufferedReader br = null;
        List<String> list = new ArrayList<String>();
        if ("-".equals(fileName)) {
            return list;
        }
        try {
            br = new BufferedReader(new FileReader(fileName));
            while (true) {
                String path = br.readLine();
                if (path == null) {
                    break;
                }
                path = path.trim();
                if (path.length() == 0
                        || path.charAt(0) == '#') {
                    continue;
                }
                if (addClassSuffix && !path.endsWith(".class")) {
                    path = path + ".class";
                }
                path = cleanPath(new File(path));
                if (path != null && path.length() > 0 && !list.contains(path)) {
                    list.add(path);
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("Can't find file \"" + fileName + "\".");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
        return list;
    }
    private Set<String> expand(File dir,
            Set<String> inputSet,
            Set<String> processed) {
        Set<String> includedFiles = new HashSet<String>();
        if (inputSet.isEmpty()) {
            return includedFiles;
        }
        for (String name : inputSet) {
            File f = (dir == null) ? new File(name)
                    : new File(dir, name);
            String path = cleanPath(f);
            if (path != null && path.length() > 0
                    && !processed.contains(path)) {
                if (f.isFile()) {
                    includedFiles.add(path);
                    processed.add(path);
                } else if (f.isDirectory()) {
                    String[] dirList = f.list();
                    Set<String> dirInputSet = new HashSet<String>();
                    for (String x : dirList) {
                        dirInputSet.add(x);
                    }
                    Set<String> subList = expand(f, dirInputSet, processed);
                    includedFiles.addAll(subList);
                    processed.add(path);
                }
            }
        }
        return includedFiles;
    }
    private String cleanPath(File f) {
        String path = f.getPath();
        if (f.isFile()) {
            path = cleanFilePath(path);
        } else if (f.isDirectory()) {
            path = cleanDirPath(path);
        } else {
            System.err.println("WARNING: Path does not exist as file or directory: " + path);
            path = null;
        }
        return path;
    }
    private String cleanFilePath(String path) {
        path = path.trim();
        if (File.separatorChar == '/') {
            path = path.replace('\\', '/');
        } else {
            path = path.replace('/', '\\');
        }
        if (path.startsWith("." + File.separator)) {
            path = path.substring(2);
        }
        return path;
    }
    private String cleanDirPath(String path) {
        path = cleanFilePath(path);
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        return path;
    }
}
