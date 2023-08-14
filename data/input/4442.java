public class CopyAndMove {
    static final Random rand = new Random();
    static boolean heads() { return rand.nextBoolean(); }
    public static void main(String[] args) throws Exception {
        Path dir1 = TestUtil.createTemporaryDirectory();
        try {
            testCopyFileToFile(dir1, dir1, TestUtil.supportsLinks(dir1));
            testMove(dir1, dir1, TestUtil.supportsLinks(dir1));
            String testDir = System.getProperty("test.dir", ".");
            Path dir2 = TestUtil.createTemporaryDirectory(testDir);
            try {
                boolean testSymbolicLinks =
                    TestUtil.supportsLinks(dir1) && TestUtil.supportsLinks(dir2);
                testCopyFileToFile(dir1, dir2, testSymbolicLinks);
                testMove(dir1, dir2, testSymbolicLinks);
            } finally {
                TestUtil.removeAll(dir2);
            }
            Path dir3 = PassThroughFileSystem.create().getPath(dir1.toString());
            testCopyFileToFile(dir1, dir3, false);
            testMove(dir1, dir3, false);
            testCopyInputStreamToFile();
            testCopyFileToOuputStream();
        } finally {
            TestUtil.removeAll(dir1);
        }
    }
    static void checkBasicAttributes(BasicFileAttributes attrs1,
                                     BasicFileAttributes attrs2)
    {
        assertTrue(attrs1.isRegularFile() == attrs2.isRegularFile());
        assertTrue(attrs1.isDirectory() == attrs2.isDirectory());
        assertTrue(attrs1.isSymbolicLink() == attrs2.isSymbolicLink());
        assertTrue(attrs1.isOther() == attrs2.isOther());
        long time1 = attrs1.lastModifiedTime().toMillis();
        long time2 = attrs2.lastModifiedTime().toMillis();
        assertTrue(time1 == time2);
        if (attrs1.isRegularFile())
            assertTrue(attrs1.size() == attrs2.size());
    }
    static void checkPosixAttributes(PosixFileAttributes attrs1,
                                     PosixFileAttributes attrs2)
    {
        assertTrue(attrs1.permissions().equals(attrs2.permissions()));
        assertTrue(attrs1.owner().equals(attrs2.owner()));
        assertTrue(attrs1.group().equals(attrs2.group()));
    }
    static void checkDosAttributes(DosFileAttributes attrs1,
                                   DosFileAttributes attrs2)
    {
        assertTrue(attrs1.isReadOnly() == attrs2.isReadOnly());
        assertTrue(attrs1.isHidden() == attrs2.isHidden());
        assertTrue(attrs1.isSystem() == attrs2.isSystem());
    }
    static void checkUserDefinedFileAttributes(Map<String,ByteBuffer> attrs1,
                                     Map<String,ByteBuffer> attrs2)
    {
        assert attrs1.size() == attrs2.size();
        for (String name: attrs1.keySet()) {
            ByteBuffer bb1 = attrs1.get(name);
            ByteBuffer bb2 = attrs2.get(name);
            assertTrue(bb2 != null);
            assertTrue(bb1.equals(bb2));
        }
    }
    static Map<String,ByteBuffer> readUserDefinedFileAttributes(Path file)
        throws IOException
    {
        UserDefinedFileAttributeView view =
            getFileAttributeView(file, UserDefinedFileAttributeView.class);
        Map<String,ByteBuffer> result = new HashMap<>();
        for (String name: view.list()) {
            int size = view.size(name);
            ByteBuffer bb = ByteBuffer.allocate(size);
            int n = view.read(name, bb);
            assertTrue(n == size);
            bb.flip();
            result.put(name, bb);
        }
        return result;
    }
    static void moveAndVerify(Path source, Path target, CopyOption... options)
        throws IOException
    {
        BasicFileAttributes basicAttributes = null;
        PosixFileAttributes posixAttributes = null;
        DosFileAttributes dosAttributes = null;
        Map<String,ByteBuffer> namedAttributes = null;
        String os = System.getProperty("os.name");
        if (os.equals("SunOS") || os.equals("Linux")) {
            posixAttributes = readAttributes(source, PosixFileAttributes.class, NOFOLLOW_LINKS);
            basicAttributes = posixAttributes;
        }
        if (os.startsWith("Windows")) {
            dosAttributes = readAttributes(source, DosFileAttributes.class, NOFOLLOW_LINKS);
            basicAttributes = dosAttributes;
        }
        if (basicAttributes == null)
            basicAttributes = readAttributes(source, BasicFileAttributes.class, NOFOLLOW_LINKS);
        int hash = (basicAttributes.isRegularFile()) ? computeHash(source) : 0;
        Path linkTarget = null;
        if (basicAttributes.isSymbolicLink())
            linkTarget = readSymbolicLink(source);
        if (!basicAttributes.isSymbolicLink() &&
            getFileStore(source).supportsFileAttributeView("xattr"))
        {
            namedAttributes = readUserDefinedFileAttributes(source);
        }
        Path result = move(source, target, options);
        assertTrue(result == target);
        assertTrue(notExists(source));
        if (basicAttributes.isRegularFile()) {
            if (computeHash(target) != hash)
                throw new RuntimeException("Failed to verify move of regular file");
        }
        if (basicAttributes.isSymbolicLink()) {
            if (!readSymbolicLink(target).equals(linkTarget))
                throw new RuntimeException("Failed to verify move of symbolic link");
        }
        checkBasicAttributes(basicAttributes,
            readAttributes(target, BasicFileAttributes.class, NOFOLLOW_LINKS));
        if (source.getFileSystem().provider() == target.getFileSystem().provider()) {
            if (posixAttributes != null && !basicAttributes.isSymbolicLink()) {
                checkPosixAttributes(posixAttributes,
                    readAttributes(target, PosixFileAttributes.class, NOFOLLOW_LINKS));
            }
            if (dosAttributes != null && !basicAttributes.isSymbolicLink()) {
                DosFileAttributes attrs =
                    readAttributes(target, DosFileAttributes.class, NOFOLLOW_LINKS);
                checkDosAttributes(dosAttributes, attrs);
            }
            if (namedAttributes != null &&
                getFileStore(target).supportsFileAttributeView("xattr"))
            {
                checkUserDefinedFileAttributes(namedAttributes,
                                               readUserDefinedFileAttributes(target));
            }
        }
    }
    static void testMove(Path dir1, Path dir2, boolean supportsLinks)
        throws IOException
    {
        Path source, target, entry;
        boolean sameDevice = getFileStore(dir1).equals(getFileStore(dir2));
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        moveAndVerify(source, target);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        try {
            moveAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(target);
        createDirectory(target);
        try {
            moveAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        moveAndVerify(source, target, REPLACE_EXISTING);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        moveAndVerify(source, target, REPLACE_EXISTING);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        moveAndVerify(source, target, REPLACE_EXISTING);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        entry = target.resolve("foo");
        createFile(entry);
        try {
            moveAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(entry);
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir1);
        moveAndVerify(source, target, ATOMIC_MOVE);
        delete(target);
        if (!sameDevice) {
            source = createSourceFile(dir1);
            target = getTargetFile(dir2);
            try {
                moveAndVerify(source, target, ATOMIC_MOVE);
                throw new RuntimeException("AtomicMoveNotSupportedException expected");
            } catch (AtomicMoveNotSupportedException x) {
            }
            delete(source);
        }
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        moveAndVerify(source, target);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        try {
            moveAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(target);
        createDirectory(target);
        try {
            moveAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        moveAndVerify(source, target, REPLACE_EXISTING);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        moveAndVerify(source, target, REPLACE_EXISTING);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        moveAndVerify(source, target, REPLACE_EXISTING);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        entry = target.resolve("foo");
        createFile(entry);
        try {
            moveAndVerify(source, target, REPLACE_EXISTING);
            throw new RuntimeException("DirectoryNotEmptyException expected");
        } catch (DirectoryNotEmptyException x) {
        }
        delete(entry);
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        createFile(source.resolve("foo"));
        target = getTargetFile(dir1);
        moveAndVerify(source, target);
        delete(target.resolve("foo"));
        delete(target);
        if (!sameDevice) {
            source = createSourceDirectory(dir1);
            createFile(source.resolve("foo"));
            target = getTargetFile(dir2);
            try {
                moveAndVerify(source, target);
                throw new RuntimeException("IOException expected");
            } catch (IOException x) {
            }
            delete(source.resolve("foo"));
            delete(source);
        }
        source = createSourceDirectory(dir1);
        createFile(source.resolve("foo"));
        target = getTargetFile(dir1);
        moveAndVerify(source, target, ATOMIC_MOVE);
        delete(target.resolve("foo"));
        delete(target);
        if (supportsLinks) {
            Path tmp = createSourceFile(dir1);
            source = dir1.resolve("link");
            createSymbolicLink(source, tmp);
            target = getTargetFile(dir2);
            moveAndVerify(source, target);
            delete(target);
            delete(tmp);
        }
        if (supportsLinks) {
            source = dir1.resolve("link");
            createSymbolicLink(source, dir2);
            target = getTargetFile(dir2);
            moveAndVerify(source, target);
            delete(target);
        }
        if (supportsLinks) {
            Path tmp = Paths.get("doesnotexist");
            source = dir1.resolve("link");
            createSymbolicLink(source, tmp);
            target = getTargetFile(dir2);
            moveAndVerify(source, target);
            delete(target);
        }
        if (supportsLinks) {
            source = dir1.resolve("link");
            createSymbolicLink(source, dir2);
            target = getTargetFile(dir2);
            createFile(target);
            try {
                moveAndVerify(source, target);
                throw new RuntimeException("FileAlreadyExistsException expected");
            } catch (FileAlreadyExistsException x) {
            }
            delete(source);
            delete(target);
        }
        if (supportsLinks) {
            source = dir1.resolve("link");
            createSymbolicLink(source, dir2);
            target = getTargetFile(dir2);
            createFile(target);
            moveAndVerify(source, target, REPLACE_EXISTING);
            delete(target);
        }
        if (supportsLinks) {
            source = dir1.resolve("link");
            createSymbolicLink(source, dir2);
            target = getTargetFile(dir2);
            createDirectory(target);
            moveAndVerify(source, target, REPLACE_EXISTING);
            delete(target);
        }
        if (supportsLinks) {
            source = dir1.resolve("link");
            createSymbolicLink(source, dir2);
            target = getTargetFile(dir2);
            createDirectory(target);
            entry = target.resolve("foo");
            createFile(entry);
            try {
                moveAndVerify(source, target);
                throw new RuntimeException("FileAlreadyExistsException expected");
            } catch (FileAlreadyExistsException x) {
            }
            delete(entry);
            delete(source);
            delete(target);
        }
        if (supportsLinks) {
            source = dir1.resolve("link");
            createSymbolicLink(source, dir1);
            target = getTargetFile(dir2);
            createFile(target);
            moveAndVerify(source, target, REPLACE_EXISTING);
            delete(target);
        }
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        try {
            move(null, target);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException x) { }
        try {
            move(source, null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException x) { }
        try {
            move(source, target, (CopyOption[])null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException x) { }
        try {
            CopyOption[] opts = { REPLACE_EXISTING, null };
            move(source, target, opts);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException x) { }
        delete(source);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        try {
            move(source, target, new CopyOption() { });
        } catch (UnsupportedOperationException x) { }
        try {
            move(source, target, REPLACE_EXISTING,  new CopyOption() { });
        } catch (UnsupportedOperationException x) { }
        delete(source);
    }
    static void copyAndVerify(Path source, Path target, CopyOption... options)
        throws IOException
    {
        Path result = copy(source, target, options);
        assertTrue(result == target);
        boolean followLinks = true;
        LinkOption[] linkOptions = new LinkOption[0];
        boolean copyAttributes = false;
        for (CopyOption opt : options) {
            if (opt == NOFOLLOW_LINKS) {
                followLinks = false;
                linkOptions = new LinkOption[] { NOFOLLOW_LINKS };
            }
            if (opt == COPY_ATTRIBUTES)
                copyAttributes = true;
        }
        BasicFileAttributes basicAttributes =
            readAttributes(source, BasicFileAttributes.class, linkOptions);
        if (basicAttributes.isRegularFile())
            assertTrue(computeHash(source) == computeHash(target));
        if (basicAttributes.isSymbolicLink())
            assert(readSymbolicLink(source).equals(readSymbolicLink(target)));
        if (copyAttributes && followLinks) {
            checkBasicAttributes(basicAttributes,
                readAttributes(source, BasicFileAttributes.class, linkOptions));
            if (source.getFileSystem().provider() == target.getFileSystem().provider()) {
                String os = System.getProperty("os.name");
                if (os.equals("SunOS") || os.equals("Linux")) {
                    checkPosixAttributes(
                        readAttributes(source, PosixFileAttributes.class, linkOptions),
                        readAttributes(target, PosixFileAttributes.class, linkOptions));
                }
                if (os.startsWith("Windows")) {
                    checkDosAttributes(
                        readAttributes(source, DosFileAttributes.class, linkOptions),
                        readAttributes(target, DosFileAttributes.class, linkOptions));
                }
                if (followLinks &&
                    getFileStore(source).supportsFileAttributeView("xattr") &&
                    getFileStore(target).supportsFileAttributeView("xattr"))
                {
                    checkUserDefinedFileAttributes(readUserDefinedFileAttributes(source),
                                                   readUserDefinedFileAttributes(target));
                }
            }
        }
    }
    static void testCopyFileToFile(Path dir1, Path dir2, boolean supportsLinks)
        throws IOException
    {
        Path source, target, link, entry;
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        copyAndVerify(source, target);
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        try {
            copyAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(target);
        createDirectory(target);
        try {
            copyAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        copyAndVerify(source, target, REPLACE_EXISTING);
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        copyAndVerify(source, target, REPLACE_EXISTING);
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        copyAndVerify(source, target, REPLACE_EXISTING);
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        entry = target.resolve("foo");
        createFile(entry);
        try {
            copyAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(entry);
        delete(source);
        delete(target);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        copyAndVerify(source, target, COPY_ATTRIBUTES);
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        copyAndVerify(source, target);
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        try {
            copyAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(target);
        createDirectory(target);
        try {
            copyAndVerify(source, target);
            throw new RuntimeException("FileAlreadyExistsException expected");
        } catch (FileAlreadyExistsException x) {
        }
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        copyAndVerify(source, target, REPLACE_EXISTING);
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createFile(target);
        copyAndVerify(source, target, REPLACE_EXISTING);
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        copyAndVerify(source, target, REPLACE_EXISTING);
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        createDirectory(target);
        entry = target.resolve("foo");
        createFile(entry);
        try {
            copyAndVerify(source, target, REPLACE_EXISTING);
            throw new RuntimeException("DirectoryNotEmptyException expected");
        } catch (DirectoryNotEmptyException x) {
        }
        delete(entry);
        delete(source);
        delete(target);
        source = createSourceDirectory(dir1);
        target = getTargetFile(dir2);
        copyAndVerify(source, target, COPY_ATTRIBUTES);
        delete(source);
        delete(target);
        if (supportsLinks) {
            source = createSourceFile(dir1);
            link = dir1.resolve("link");
            createSymbolicLink(link, source);
            target = getTargetFile(dir2);
            copyAndVerify(link, target);
            delete(link);
            delete(source);
        }
        if (supportsLinks) {
            source = createSourceFile(dir1);
            link = dir1.resolve("link");
            createSymbolicLink(link, source);
            target = getTargetFile(dir2);
            copyAndVerify(link, target, NOFOLLOW_LINKS);
            delete(link);
            delete(source);
        }
        if (supportsLinks) {
            source = dir1.resolve("mydir");
            createDirectory(source);
            link = dir1.resolve("link");
            createSymbolicLink(link, source);
            target = getTargetFile(dir2);
            copyAndVerify(link, target, NOFOLLOW_LINKS);
            delete(link);
            delete(source);
        }
        if (supportsLinks) {
            assertTrue(notExists(source));
            link = dir1.resolve("link");
            createSymbolicLink(link, source);
            target = getTargetFile(dir2);
            copyAndVerify(link, target, NOFOLLOW_LINKS);
            delete(link);
        }
        if (supportsLinks &&
            System.getProperty("os.name").startsWith("Windows"))
        {
            Path unc = Paths.get("\\\\rialto\\share\\file");
            link = dir1.resolve("link");
            createSymbolicLink(link, unc);
            target = getTargetFile(dir2);
            copyAndVerify(link, target, NOFOLLOW_LINKS);
            delete(link);
        }
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        try {
            copy(source, null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException x) { }
        try {
            copy(source, target, (CopyOption[])null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException x) { }
        try {
            CopyOption[] opts = { REPLACE_EXISTING, null };
            copy(source, target, opts);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException x) { }
        delete(source);
        source = createSourceFile(dir1);
        target = getTargetFile(dir2);
        try {
            copy(source, target, new CopyOption() { });
        } catch (UnsupportedOperationException x) { }
        try {
            copy(source, target, REPLACE_EXISTING,  new CopyOption() { });
        } catch (UnsupportedOperationException x) { }
        delete(source);
    }
    static void testCopyInputStreamToFile() throws IOException {
        testCopyInputStreamToFile(0);
        for (int i=0; i<100; i++) {
            testCopyInputStreamToFile(rand.nextInt(32000));
        }
        Path target = createTempFile("blah", null);
        try {
            InputStream in = new ByteArrayInputStream(new byte[0]);
            try {
                copy(in, target);
                throw new RuntimeException("FileAlreadyExistsException expected");
            } catch (FileAlreadyExistsException ignore) { }
        } finally {
            delete(target);
        }
        Path tmpdir = createTempDirectory("blah");
        try {
            if (TestUtil.supportsLinks(tmpdir)) {
                Path link = createSymbolicLink(tmpdir.resolve("link"),
                                                  tmpdir.resolve("target"));
                try {
                    InputStream in = new ByteArrayInputStream(new byte[0]);
                    try {
                        copy(in, link);
                        throw new RuntimeException("FileAlreadyExistsException expected");
                    } catch (FileAlreadyExistsException ignore) { }
                } finally {
                    delete(link);
                }
            }
        } finally {
            delete(tmpdir);
        }
        try {
            copy((InputStream)null, target);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            copy(new ByteArrayInputStream(new byte[0]), (Path)null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
    }
    static void testCopyInputStreamToFile(int size) throws IOException {
        Path tmpdir = createTempDirectory("blah");
        Path source = tmpdir.resolve("source");
        Path target = tmpdir.resolve("target");
        try {
            boolean testReplaceExisting = rand.nextBoolean();
            byte[] b = new byte[size];
            rand.nextBytes(b);
            write(source, b);
            if (testReplaceExisting && rand.nextBoolean()) {
                write(target, new byte[rand.nextInt(512)]);
            }
            InputStream in = new FileInputStream(source.toFile());
            try {
                long n;
                if (testReplaceExisting) {
                    n = copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    n = copy(in, target);
                }
                assertTrue(in.read() == -1);   
                assertTrue(n == size);
                assertTrue(size(target) == size);
            } finally {
                in.close();
            }
            byte[] read = readAllBytes(target);
            assertTrue(Arrays.equals(read, b));
        } finally {
            deleteIfExists(source);
            deleteIfExists(target);
            delete(tmpdir);
        }
    }
    static void testCopyFileToOuputStream() throws IOException {
        testCopyFileToOuputStream(0);
        for (int i=0; i<100; i++) {
            testCopyFileToOuputStream(rand.nextInt(32000));
        }
        try {
            copy((Path)null, new ByteArrayOutputStream());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            Path source = createTempFile("blah", null);
            delete(source);
            copy(source, (OutputStream)null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
    }
    static void testCopyFileToOuputStream(int size) throws IOException {
        Path source = createTempFile("blah", null);
        try {
            byte[] b = new byte[size];
            rand.nextBytes(b);
            write(source, b);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            long n = copy(source, out);
            assertTrue(n == size);
            assertTrue(out.size() == size);
            byte[] read = out.toByteArray();
            assertTrue(Arrays.equals(read, b));
            out.write(0);
            assertTrue(out.size() == size+1);
        } finally {
            delete(source);
        }
    }
    static void assertTrue(boolean value) {
        if (!value)
            throw new RuntimeException("Assertion failed");
    }
    static int computeHash(Path file) throws IOException {
        int h = 0;
        try (InputStream in = newInputStream(file)) {
            byte[] buf = new byte[1024];
            int n;
            do {
                n = in.read(buf);
                for (int i=0; i<n; i++) {
                    h = 31*h + (buf[i] & 0xff);
                }
            } while (n > 0);
        }
        return h;
    }
    static Path createSourceFile(Path dir) throws IOException {
        String name = "source" + Integer.toString(rand.nextInt());
        Path file = dir.resolve(name);
        createFile(file);
        byte[] bytes = new byte[rand.nextInt(128*1024)];
        rand.nextBytes(bytes);
        try (OutputStream out = newOutputStream(file)) {
            out.write(bytes);
        }
        randomizeAttributes(file);
        return file;
    }
    static Path createSourceDirectory(Path dir) throws IOException {
        String name = "sourcedir" + Integer.toString(rand.nextInt());
        Path subdir = dir.resolve(name);
        createDirectory(subdir);
        randomizeAttributes(subdir);
        return subdir;
    }
    static void randomizeAttributes(Path file) throws IOException {
        String os = System.getProperty("os.name");
        boolean isWindows = os.startsWith("Windows");
        boolean isUnix = os.equals("SunOS") || os.equals("Linux");
        boolean isDirectory = isDirectory(file, NOFOLLOW_LINKS);
        if (isUnix) {
            Set<PosixFilePermission> perms =
                getPosixFilePermissions(file, NOFOLLOW_LINKS);
            PosixFilePermission[] toChange = {
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_WRITE,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_WRITE,
                PosixFilePermission.OTHERS_EXECUTE
            };
            for (PosixFilePermission perm: toChange) {
                if (heads()) {
                    perms.add(perm);
                } else {
                    perms.remove(perm);
                }
            }
            setPosixFilePermissions(file, perms);
        }
        if (isWindows) {
            DosFileAttributeView view =
                getFileAttributeView(file, DosFileAttributeView.class, NOFOLLOW_LINKS);
            view.setHidden(heads());
        }
        boolean addUserDefinedFileAttributes = heads() &&
            getFileStore(file).supportsFileAttributeView("xattr");
        if (isWindows && isDirectory) addUserDefinedFileAttributes = false;
        if (addUserDefinedFileAttributes) {
            UserDefinedFileAttributeView view =
                getFileAttributeView(file, UserDefinedFileAttributeView.class);
            int n = rand.nextInt(16);
            while (n > 0) {
                byte[] value = new byte[1 + rand.nextInt(100)];
                view.write("user." + Integer.toString(n), ByteBuffer.wrap(value));
                n--;
            }
        }
    }
    static Path getTargetFile(Path dir) throws IOException {
        String name = "target" + Integer.toString(rand.nextInt());
        return dir.resolve(name);
    }
 }
