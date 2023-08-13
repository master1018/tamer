public class SymLinks {
    final static PrintStream out = System.out;
    final static File top = new File(System.getProperty("test.dir", "."));
    final static File file              = new File(top, "foofile");
    final static File link2file         = new File(top, "link2file");
    final static File link2link2file    = new File(top, "link2link2file");
    final static File dir               = new File(top, "foodir");
    final static File link2dir          = new File(top, "link2dir");
    final static File link2link2dir     = new File(top, "link2link2dir");
    final static File link2nobody       = new File(top, "link2nobody");
    final static File link2link2nobody  = new File(top, "link2link2nobody");
    static void setup() throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        try {
            fos.write(new byte[16*1024]);
        } finally {
            fos.close();
        }
        mklink(link2file, file);
        mklink(link2link2file, link2file);
        assertTrue(dir.mkdir());
        mklink(link2dir, dir);
        mklink(link2link2dir, link2dir);
        mklink(link2nobody, new File(top, "DoesNotExist"));
        mklink(link2link2nobody, link2nobody);
    }
    static void cleanup() throws IOException {
        if (file != null)
            file.delete();
        if (link2file != null)
            Files.deleteIfExists(link2file.toPath());
        if (link2link2file != null)
            Files.deleteIfExists(link2link2file.toPath());
        if (dir != null)
            dir.delete();
        if (link2dir != null)
            Files.deleteIfExists(link2dir.toPath());
        if (link2link2dir != null)
            Files.deleteIfExists(link2link2dir.toPath());
        if (link2nobody != null)
            Files.deleteIfExists(link2nobody.toPath());
        if (link2link2nobody != null)
            Files.deleteIfExists(link2link2nobody.toPath());
    }
    static void mklink(File source, File target) throws IOException {
        Files.createSymbolicLink(source.toPath(), target.toPath());
    }
    static boolean isSymLink(File link) {
         return Files.isSymbolicLink(link.toPath());
    }
    static long lastModifiedOfSymLink(File link) throws IOException {
        BasicFileAttributes attrs =
            Files.readAttributes(link.toPath(), BasicFileAttributes.class, NOFOLLOW_LINKS);
        assertTrue(attrs.isSymbolicLink());
        return attrs.lastModifiedTime().toMillis();
    }
    static boolean supportsSymLinks(File dir) {
        Path link = dir.toPath().resolve("link");
        Path target = dir.toPath().resolve("target");
        try {
            Files.createSymbolicLink(link, target);
            Files.delete(link);
            return true;
        } catch (UnsupportedOperationException x) {
            return false;
        } catch (IOException x) {
            return false;
        }
    }
    static void assertTrue(boolean v) {
        if (!v) throw new RuntimeException("Test failed");
    }
    static void assertFalse(boolean v) {
        assertTrue(!v);
    }
    static void header(String h) {
        out.println();
        out.println();
        out.println("-- " + h + " --");
    }
    static void go() throws IOException {
        assertTrue(file.isFile());
        assertTrue(isSymLink(link2file));
        assertTrue(isSymLink(link2link2file));
        assertTrue(dir.isDirectory());
        assertTrue(isSymLink(link2dir));
        assertTrue(isSymLink(link2link2dir));
        assertTrue(isSymLink(link2nobody));
        assertTrue(isSymLink(link2link2nobody));
        header("createNewFile");
        assertFalse(file.createNewFile());
        assertFalse(link2file.createNewFile());
        assertFalse(link2link2file.createNewFile());
        assertFalse(dir.createNewFile());
        assertFalse(link2dir.createNewFile());
        assertFalse(link2link2dir.createNewFile());
        assertFalse(link2nobody.createNewFile());
        assertFalse(link2link2nobody.createNewFile());
        header("mkdir");
        assertFalse(file.mkdir());
        assertFalse(link2file.mkdir());
        assertFalse(link2link2file.mkdir());
        assertFalse(dir.mkdir());
        assertFalse(link2dir.mkdir());
        assertFalse(link2link2dir.mkdir());
        assertFalse(link2nobody.mkdir());
        assertFalse(link2link2nobody.mkdir());
        header("delete");
        File link = new File(top, "mylink");
        try {
            mklink(link, file);
            assertTrue(link.delete());
            assertTrue(!isSymLink(link));
            assertTrue(file.exists());
            mklink(link, link2file);
            assertTrue(link.delete());
            assertTrue(!isSymLink(link));
            assertTrue(link2file.exists());
            mklink(link, dir);
            assertTrue(link.delete());
            assertTrue(!isSymLink(link));
            assertTrue(dir.exists());
            mklink(link, link2dir);
            assertTrue(link.delete());
            assertTrue(!isSymLink(link));
            assertTrue(link2dir.exists());
            mklink(link, link2nobody);
            assertTrue(link.delete());
            assertTrue(!isSymLink(link));
            assertTrue(isSymLink(link2nobody));
        } finally {
            Files.deleteIfExists(link.toPath());
        }
        header("renameTo");
        File newlink = new File(top, "newlink");
        assertTrue(link2file.renameTo(newlink));
        try {
            assertTrue(file.exists());
            assertTrue(isSymLink(newlink));
            assertTrue(!isSymLink(link2file));
        } finally {
            newlink.renameTo(link2file);  
        }
        assertTrue(link2dir.renameTo(newlink));
        try {
            assertTrue(dir.exists());
            assertTrue(isSymLink(newlink));
            assertTrue(!isSymLink(link2dir));
        } finally {
            newlink.renameTo(link2dir);  
        }
        header("list");
        final String name = "entry";
        File entry = new File(dir, name);
        try {
            assertTrue(dir.list().length == 0);   
            assertTrue(link2dir.list().length == 0);
            assertTrue(link2link2dir.list().length == 0);
            assertTrue(entry.createNewFile());
            assertTrue(dir.list().length == 1);
            assertTrue(dir.list()[0].equals(name));
            assertTrue(link2dir.list().length == 1);
            assertTrue(link2dir.list()[0].equals(name));
            assertTrue(link2link2dir.list().length == 1);
            assertTrue(link2link2dir.list()[0].equals(name));
            assertTrue(link2file.list() == null);
            assertTrue(link2nobody.list() == null);
        } finally {
            entry.delete();
        }
        header("isXXX");
        assertTrue(file.isFile());
        assertTrue(link2file.isFile());
        assertTrue(link2link2file.isFile());
        assertTrue(dir.isDirectory());
        assertTrue(link2dir.isDirectory());
        assertTrue(link2link2dir.isDirectory());
        if (System.getProperty("os.name").startsWith("Windows")) {
            DosFileAttributeView view = Files
                .getFileAttributeView(file.toPath(), DosFileAttributeView.class);
            view.setHidden(true);
            try {
                assertTrue(file.isHidden());
                assertTrue(link2file.isHidden());
                assertTrue(link2link2file.isHidden());
            } finally {
                view.setHidden(false);
            }
            assertFalse(file.isHidden());
            assertFalse(link2file.isHidden());
            assertFalse(link2link2file.isHidden());
        }
        header("length");
        long len = file.length();
        assertTrue(len > 0L);
        assertTrue(link2file.length() == len);
        assertTrue(link2link2file.length() == len);
        assertTrue(link2nobody.length() == 0L);
        header("lastModified / setLastModified");
        long origLastModified = file.lastModified();
        assertTrue(origLastModified != 0L);
        try { Thread.sleep(2000); } catch (InterruptedException x) { }
        file.setLastModified(System.currentTimeMillis());
        long lastModified = file.lastModified();
        assertTrue(lastModified != origLastModified);
        assertTrue(lastModifiedOfSymLink(link2file) != lastModified);
        assertTrue(lastModifiedOfSymLink(link2link2file) != lastModified);
        assertTrue(link2file.lastModified() == lastModified);
        assertTrue(link2link2file.lastModified() == lastModified);
        assertTrue(link2nobody.lastModified() == 0L);
        origLastModified = dir.lastModified();
        assertTrue(origLastModified != 0L);
        dir.setLastModified(0L);
        assertTrue(dir.lastModified() == 0L);
        assertTrue(link2dir.lastModified() == 0L);
        assertTrue(link2link2dir.lastModified() == 0L);
        dir.setLastModified(origLastModified);
        header("setXXX / canXXX");
        assertTrue(file.canRead());
        assertTrue(file.canWrite());
        assertTrue(link2file.canRead());
        assertTrue(link2file.canWrite());
        assertTrue(link2link2file.canRead());
        assertTrue(link2link2file.canWrite());
        if (file.setReadOnly()) {
            assertFalse(file.canWrite());
            assertFalse(link2file.canWrite());
            assertFalse(link2link2file.canWrite());
            assertTrue(file.setWritable(true));             
            assertTrue(file.canWrite());
            assertTrue(link2file.canWrite());
            assertTrue(link2link2file.canWrite());
            assertTrue(link2file.setReadOnly());            
            assertFalse(file.canWrite());
            assertFalse(link2file.canWrite());
            assertFalse(link2link2file.canWrite());
            assertTrue(link2link2file.setWritable(true));   
            assertTrue(file.canWrite());
            assertTrue(link2file.canWrite());
            assertTrue(link2link2file.canWrite());
        }
    }
    public static void main(String[] args) throws IOException {
        if (supportsSymLinks(top)) {
            try {
                setup();
                go();
            } finally {
                cleanup();
            }
        }
    }
}
