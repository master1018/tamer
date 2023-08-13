public class ClassPathOpener {
    private final String pathname;
    private final Consumer consumer;
    private final boolean sort;
    public interface Consumer {
        boolean processFileBytes(String name, byte[] bytes);
        void onException(Exception ex);
        void onProcessArchiveStart(File file);
    }
    public ClassPathOpener(String pathname, boolean sort, Consumer consumer) {
        this.pathname = pathname;
        this.sort = sort;
        this.consumer = consumer;
    }
    public boolean process() {
        File file = new File(pathname);
        return processOne(file, true);
    }
    private boolean processOne(File file, boolean topLevel) {
        try {
            if (file.isDirectory()) {
                return processDirectory(file, topLevel);
            }
            String path = file.getPath();
            if (path.endsWith(".zip") ||
                    path.endsWith(".jar") ||
                    path.endsWith(".apk")) {
                return processArchive(file);
            }
            byte[] bytes = FileUtils.readFile(file);
            return consumer.processFileBytes(path, bytes);
        } catch (Exception ex) {
            consumer.onException(ex);
            return false;
        }
    }
    private static int compareClassNames(String a, String b) {
        a = a.replace('$','0');
        b = b.replace('$','0');
        a = a.replace("package-info", "");
        b = b.replace("package-info", "");
        return a.compareTo(b);
    }
    private boolean processDirectory(File dir, boolean topLevel) {
        if (topLevel) {
            dir = new File(dir, ".");
        }
        File[] files = dir.listFiles();
        int len = files.length;
        boolean any = false;
        if (sort) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File a, File b) {
                    return compareClassNames(a.getName(), b.getName());
                }
            });
        }
        for (int i = 0; i < len; i++) {
            any |= processOne(files[i], false);
        }
        return any;
    }
    private boolean processArchive(File file) throws IOException {
        ZipFile zip = new ZipFile(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(40000);
        byte[] buf = new byte[20000];
        boolean any = false;
        ArrayList<? extends java.util.zip.ZipEntry> entriesList 
                = Collections.list(zip.entries());
        if (sort) {
            Collections.sort(entriesList, new Comparator<ZipEntry>() {
               public int compare (ZipEntry a, ZipEntry b) {
                   return compareClassNames(a.getName(), b.getName());
               }
            });
        }
        consumer.onProcessArchiveStart(file);
        for (ZipEntry one : entriesList) {
            if (one.isDirectory()) {
                continue;
            }
            String path = one.getName();
            InputStream in = zip.getInputStream(one);
            baos.reset();
            for (;;) {
                int amt = in.read(buf);
                if (amt < 0) {
                    break;
                }
                baos.write(buf, 0, amt);
            }
            in.close();
            byte[] bytes = baos.toByteArray();
            any |= consumer.processFileBytes(path, bytes);
        }
        zip.close();
        return any;
    }
}
