public class TemporaryFiles {
    static void checkInDirectory(Path file, Path dir) {
        if (dir == null)
            dir = Paths.get(System.getProperty("java.io.tmpdir"));
        if (!file.getParent().equals(dir))
            throw new RuntimeException("Not in expected directory");
    }
    static void testTempFile(String prefix, String suffix, Path dir)
        throws IOException
    {
        Path file = (dir == null) ?
            Files.createTempFile(prefix, suffix) :
            Files.createTempFile(dir, prefix, suffix);
        try {
            String name = file.getFileName().toString();
            if (prefix != null && !name.startsWith(prefix))
                throw new RuntimeException("Should start with " + prefix);
            if (suffix == null && !name.endsWith(".tmp"))
                throw new RuntimeException("Should end with .tmp");
            if (suffix != null && !name.endsWith(suffix))
                throw new RuntimeException("Should end with " + suffix);
            checkInDirectory(file, dir);
            Files.newByteChannel(file, READ).close();
            Files.newByteChannel(file, WRITE).close();
            Files.newByteChannel(file, READ,WRITE).close();
            if (Files.getFileStore(file).supportsFileAttributeView("posix")) {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(file);
                perms.remove(PosixFilePermission.OWNER_READ);
                perms.remove(PosixFilePermission.OWNER_WRITE);
                if (!perms.isEmpty())
                    throw new RuntimeException("Temporary file is not secure");
            }
        } finally {
            Files.delete(file);
        }
    }
    static void testTempFile(String prefix, String suffix)
        throws IOException
    {
        testTempFile(prefix, suffix, null);
    }
    static void testTempDirectory(String prefix, Path dir) throws IOException {
        Path subdir = (dir == null) ?
            Files.createTempDirectory(prefix) :
            Files.createTempDirectory(dir, prefix);
        try {
            String name = subdir.getFileName().toString();
            if (prefix != null && !name.startsWith(prefix))
                throw new RuntimeException("Should start with " + prefix);
            checkInDirectory(subdir, dir);
            DirectoryStream<Path> stream = Files.newDirectoryStream(subdir);
            try {
                if (stream.iterator().hasNext())
                    throw new RuntimeException("Tempory directory not empty");
            } finally {
                stream.close();
            }
            Path file = Files.createFile(subdir.resolve("foo"));
            try {
                Files.newByteChannel(file, READ,WRITE).close();
            } finally {
                Files.delete(file);
            }
            if (Files.getFileStore(subdir).supportsFileAttributeView("posix")) {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(subdir);
                perms.remove(PosixFilePermission.OWNER_READ);
                perms.remove(PosixFilePermission.OWNER_WRITE);
                perms.remove(PosixFilePermission.OWNER_EXECUTE);
                if (!perms.isEmpty())
                    throw new RuntimeException("Temporary directory is not secure");
            }
        } finally {
            Files.delete(subdir);
        }
    }
    static void testTempDirectory(String prefix) throws IOException {
        testTempDirectory(prefix, null);
    }
    static void testInvalidFileTemp(String prefix, String suffix) throws IOException {
        try {
            Path file = Files.createTempFile(prefix, suffix);
            Files.delete(file);
            throw new RuntimeException("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) { }
    }
    public static void main(String[] args) throws IOException {
        testTempFile("blah", ".dat");
        testTempFile("blah", null);
        testTempFile(null, ".dat");
        testTempFile(null, null);
        testTempDirectory("blah");
        testTempDirectory(null);
        Path dir = Files.createTempDirectory("tmpdir");
        try {
            testTempFile("blah", ".dat", dir);
            testTempFile("blah", null, dir);
            testTempFile(null, ".dat", dir);
            testTempFile(null, null, dir);
            testTempDirectory("blah", dir);
            testTempDirectory(null, dir);
        } finally {
            Files.delete(dir);
        }
        testInvalidFileTemp("../blah", null);
        testInvalidFileTemp("dir/blah", null);
        testInvalidFileTemp("blah", ".dat/foo");
        try {
            Files.createTempFile("blah", ".tmp", (FileAttribute<?>[])null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            Files.createTempFile("blah", ".tmp", new FileAttribute<?>[] { null });
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            Files.createTempDirectory("blah", (FileAttribute<?>[])null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            Files.createTempDirectory("blah", new FileAttribute<?>[] { null });
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            Files.createTempFile((Path)null, "blah", ".tmp");
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            Files.createTempDirectory((Path)null, "blah");
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
    }
}
