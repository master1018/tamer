class TempFileHelper {
    private TempFileHelper() { }
    private static final Path tmpdir =
        Paths.get(doPrivileged(new GetPropertyAction("java.io.tmpdir")));
    private static final boolean isPosix =
        FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    private static final SecureRandom random = new SecureRandom();
    private static Path generatePath(String prefix, String suffix, Path dir) {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        Path name = dir.getFileSystem().getPath(prefix + Long.toString(n) + suffix);
        if (name.getParent() != null)
            throw new IllegalArgumentException("Invalid prefix or suffix");
        return dir.resolve(name);
    }
    private static class PosixPermissions {
        static final FileAttribute<Set<PosixFilePermission>> filePermissions =
            PosixFilePermissions.asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE));
        static final FileAttribute<Set<PosixFilePermission>> dirPermissions =
            PosixFilePermissions.asFileAttribute(EnumSet
                .of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE));
    }
    private static Path create(Path dir,
                               String prefix,
                               String suffix,
                               boolean createDirectory,
                               FileAttribute[] attrs)
        throws IOException
    {
        if (prefix == null)
            prefix = "";
        if (suffix == null)
            suffix = (createDirectory) ? "" : ".tmp";
        if (dir == null)
            dir = tmpdir;
        if (isPosix && (dir.getFileSystem() == FileSystems.getDefault())) {
            if (attrs.length == 0) {
                attrs = new FileAttribute<?>[1];
                attrs[0] = (createDirectory) ? PosixPermissions.dirPermissions :
                                               PosixPermissions.filePermissions;
            } else {
                boolean hasPermissions = false;
                for (int i=0; i<attrs.length; i++) {
                    if (attrs[i].name().equals("posix:permissions")) {
                        hasPermissions = true;
                        break;
                    }
                }
                if (!hasPermissions) {
                    FileAttribute<?>[] copy = new FileAttribute<?>[attrs.length+1];
                    System.arraycopy(attrs, 0, copy, 0, attrs.length);
                    attrs = copy;
                    attrs[attrs.length-1] = (createDirectory) ?
                        PosixPermissions.dirPermissions :
                        PosixPermissions.filePermissions;
                }
            }
        }
        SecurityManager sm = System.getSecurityManager();
        for (;;) {
            Path f;
            try {
                f = generatePath(prefix, suffix, dir);
            } catch (InvalidPathException e) {
                if (sm != null)
                    throw new IllegalArgumentException("Invalid prefix or suffix");
                throw e;
            }
            try {
                if (createDirectory) {
                    return Files.createDirectory(f, attrs);
                } else {
                    return Files.createFile(f, attrs);
                }
            } catch (SecurityException e) {
                if (dir == tmpdir && sm != null)
                    throw new SecurityException("Unable to create temporary file or directory");
                throw e;
            } catch (FileAlreadyExistsException e) {
            }
        }
    }
    static Path createTempFile(Path dir,
                               String prefix,
                               String suffix,
                               FileAttribute[] attrs)
        throws IOException
    {
        return create(dir, prefix, suffix, false, attrs);
    }
    static Path createTempDirectory(Path dir,
                                    String prefix,
                                    FileAttribute[] attrs)
        throws IOException
    {
        return create(dir, prefix, null, true, attrs);
    }
}
