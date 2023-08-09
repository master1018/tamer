public class FileAttributes {
    static void assertTrue(boolean okay) {
        if (!okay)
            throw new RuntimeException("Assertion Failed");
    }
    static void checkEqual(Object o1, Object o2) {
        if (o1 == null) {
            assertTrue(o2 == null);
        } else {
            assertTrue (o1.equals(o2));
        }
    }
    static void checkNearEqual(FileTime t1, FileTime t2) {
        long diff = Math.abs(t1.toMillis() - t2.toMillis());
        assertTrue(diff <= 1000);
    }
    static void checkBasicAttributes(Path file, BasicFileAttributes attrs)
        throws IOException
    {
        checkEqual(attrs.size(), Files.getAttribute(file, "size"));
        checkEqual(attrs.lastModifiedTime(), Files.getAttribute(file, "basic:lastModifiedTime"));
        checkEqual(attrs.lastAccessTime(), Files.getAttribute(file, "lastAccessTime"));
        checkEqual(attrs.creationTime(), Files.getAttribute(file, "basic:creationTime"));
        assertTrue((Boolean)Files.getAttribute(file, "isRegularFile"));
        assertTrue(!(Boolean)Files.getAttribute(file, "basic:isDirectory"));
        assertTrue(!(Boolean)Files.getAttribute(file, "isSymbolicLink"));
        assertTrue(!(Boolean)Files.getAttribute(file, "basic:isOther"));
        checkEqual(attrs.fileKey(), Files.getAttribute(file, "basic:fileKey"));
        FileTime modTime = attrs.lastModifiedTime();
        Files.setAttribute(file, "basic:lastModifiedTime", FileTime.fromMillis(0L));
        checkEqual(Files.getLastModifiedTime(file),
                   FileTime.fromMillis(0L));
        Files.setAttribute(file, "lastModifiedTime", modTime);
        checkEqual(Files.getLastModifiedTime(file), modTime);
        Map<String,Object> map;
        map = Files.readAttributes(file, "*");
        assertTrue(map.size() >= 9);
        checkEqual(attrs.isRegularFile(), map.get("isRegularFile")); 
        map = Files.readAttributes(file, "basic:*");
        assertTrue(map.size() >= 9);
        checkEqual(attrs.lastAccessTime(), map.get("lastAccessTime")); 
        map = Files.readAttributes(file, "size,lastModifiedTime");
        assertTrue(map.size() == 2);
        checkEqual(attrs.size(), map.get("size"));
        checkEqual(attrs.lastModifiedTime(), map.get("lastModifiedTime"));
    }
    static void checkPosixAttributes(Path file, PosixFileAttributes attrs)
        throws IOException
    {
        checkBasicAttributes(file, attrs);
        checkEqual(attrs.permissions(), Files.getAttribute(file, "posix:permissions"));
        checkEqual(attrs.owner(), Files.getAttribute(file, "posix:owner"));
        checkEqual(attrs.group(), Files.getAttribute(file, "posix:group"));
        Set<PosixFilePermission> orig = attrs.permissions();
        Set<PosixFilePermission> newPerms = new HashSet<>(orig);
        newPerms.remove(PosixFilePermission.OTHERS_READ);
        newPerms.remove(PosixFilePermission.OTHERS_WRITE);
        newPerms.remove(PosixFilePermission.OTHERS_EXECUTE);
        Files.setAttribute(file, "posix:permissions", newPerms);
        checkEqual(Files.getPosixFilePermissions(file), newPerms);
        Files.setAttribute(file, "posix:permissions", orig);
        checkEqual(Files.getPosixFilePermissions(file), orig);
        Files.setAttribute(file, "posix:owner", attrs.owner());
        Files.setAttribute(file, "posix:group", attrs.group());
        Map<String,Object> map;
        map = Files.readAttributes(file, "posix:*");
        assertTrue(map.size() >= 12);
        checkEqual(attrs.permissions(), map.get("permissions")); 
        map = Files.readAttributes(file, "posix:size,owner");
        assertTrue(map.size() == 2);
        checkEqual(attrs.size(), map.get("size"));
        checkEqual(attrs.owner(), map.get("owner"));
    }
    static void checkUnixAttributes(Path file) throws IOException {
        int mode = (Integer)Files.getAttribute(file, "unix:mode");
        long ino = (Long)Files.getAttribute(file, "unix:ino");
        long dev = (Long)Files.getAttribute(file, "unix:dev");
        long rdev = (Long)Files.getAttribute(file, "unix:rdev");
        int nlink = (Integer)Files.getAttribute(file, "unix:nlink");
        int uid = (Integer)Files.getAttribute(file, "unix:uid");
        int gid = (Integer)Files.getAttribute(file, "unix:gid");
        FileTime ctime = (FileTime)Files.getAttribute(file, "unix:ctime");
        Map<String,Object> map;
        map = Files.readAttributes(file, "unix:*");
        assertTrue(map.size() >= 20);
        map = Files.readAttributes(file, "unix:size,uid,gid");
        assertTrue(map.size() == 3);
        checkEqual(map.get("size"),
                   Files.readAttributes(file, BasicFileAttributes.class).size());
    }
    static void checkDosAttributes(Path file, DosFileAttributes attrs)
        throws IOException
    {
        checkBasicAttributes(file, attrs);
        checkEqual(attrs.isReadOnly(), Files.getAttribute(file, "dos:readonly"));
        checkEqual(attrs.isHidden(), Files.getAttribute(file, "dos:hidden"));
        checkEqual(attrs.isSystem(), Files.getAttribute(file, "dos:system"));
        checkEqual(attrs.isArchive(), Files.getAttribute(file, "dos:archive"));
        boolean value;
        value = attrs.isReadOnly();
        Files.setAttribute(file, "dos:readonly", !value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isReadOnly(), !value);
        Files.setAttribute(file, "dos:readonly", value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isReadOnly(), value);
        value = attrs.isHidden();
        Files.setAttribute(file, "dos:hidden", !value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isHidden(), !value);
        Files.setAttribute(file, "dos:hidden", value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isHidden(), value);
        value = attrs.isSystem();
        Files.setAttribute(file, "dos:system", !value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isSystem(), !value);
        Files.setAttribute(file, "dos:system", value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isSystem(), value);
        value = attrs.isArchive();
        Files.setAttribute(file, "dos:archive", !value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isArchive(), !value);
        Files.setAttribute(file, "dos:archive", value);
        checkEqual(Files.readAttributes(file, DosFileAttributes.class).isArchive(), value);
        Map<String,Object> map;
        map = Files.readAttributes(file, "dos:*");
        assertTrue(map.size() >= 13);
        checkEqual(attrs.isReadOnly(), map.get("readonly")); 
        map = Files.readAttributes(file, "dos:size,hidden");
        assertTrue(map.size() == 2);
        checkEqual(attrs.size(), map.get("size"));
        checkEqual(attrs.isHidden(), map.get("hidden"));
    }
    static void checkBadSet(Path file, String attribute, Object value)
        throws IOException
    {
        try {
            Files.setAttribute(file, attribute, 0);
            throw new RuntimeException("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) { }
    }
    static void checkBadGet(Path file, String attribute) throws IOException {
        try {
            Files.getAttribute(file, attribute);
            throw new RuntimeException("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) { }
    }
    static void checkBadRead(Path file, String attribute) throws IOException {
        try {
            Files.readAttributes(file, attribute);
            throw new RuntimeException("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) { }
    }
    static void miscTests(Path file) throws IOException {
        try {
            Files.setAttribute(file, "foo:bar", 0);
            throw new RuntimeException("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ignore) { }
        try {
            Files.getAttribute(file, "foo:bar");
            throw new RuntimeException("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ignore) { }
        try {
            Files.readAttributes(file, "foo:*");
            throw new RuntimeException("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ignore) { }
        checkBadSet(file, "", 0);
        checkBadSet(file, "basic:", 0);
        checkBadSet(file, "basic:foobar", 0);
        checkBadGet(file, "");
        checkBadGet(file, "basic:");
        checkBadGet(file, "basic:foobar");
        checkBadGet(file, "basic:size,lastModifiedTime");
        checkBadGet(file, "basic:*");
        checkBadRead(file, "");
        checkBadRead(file, "basic:");
        checkBadRead(file, "basic:foobar");
        checkBadRead(file, "basic:size,foobar");
        try {
            Files.getAttribute(file, null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException npe) { }
        try {
            Files.getAttribute(file, "isRegularFile", (LinkOption[])null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException npe) { }
        try {
            Files.setAttribute(file, null, 0L);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException npe) { }
    }
    static void doTests(Path dir) throws IOException {
        Path file = dir.resolve("foo");
        Files.createFile(file);
        FileStore store = Files.getFileStore(file);
        try {
            checkBasicAttributes(file,
                Files.readAttributes(file, BasicFileAttributes.class));
            if (store.supportsFileAttributeView("posix"))
                checkPosixAttributes(file,
                    Files.readAttributes(file, PosixFileAttributes.class));
            if (store.supportsFileAttributeView("unix"))
                checkUnixAttributes(file);
            if (store.supportsFileAttributeView("dos"))
                checkDosAttributes(file,
                    Files.readAttributes(file, DosFileAttributes.class));
            miscTests(file);
        } finally {
            Files.delete(file);
        }
    }
    public static void main(String[] args) throws IOException {
        Path dir = TestUtil.createTemporaryDirectory();
        try {
            doTests(dir);
        } finally {
            TestUtil.removeAll(dir);
        }
    }
}
