public class FileTreeModifier {
    static void checkExpectedEvent(WatchService watcher,
                                   WatchEvent.Kind<?> expectedType,
                                   Object expectedContext)
    {
        WatchKey key;
        try {
            key = watcher.take();
        } catch (InterruptedException x) {
            throw new RuntimeException(x);
        }
        WatchEvent<?> event = key.pollEvents().iterator().next();
        System.out.format("Event: type=%s, count=%d, context=%s\n",
            event.kind(), event.count(), event.context());
        if (event.kind() != expectedType)
            throw new RuntimeException("unexpected event");
        if (!expectedContext.equals(event.context()))
            throw new RuntimeException("unexpected context");
    }
    static void doTest(Path top) throws IOException {
        FileSystem fs = top.getFileSystem();
        WatchService watcher = fs.newWatchService();
        Path subdir = Files.createDirectories(top.resolve("a").resolve("b").resolve("c"));
        WatchKey key = top.register(watcher,
            new WatchEvent.Kind<?>[]{ ENTRY_CREATE }, FILE_TREE);
        Path file = Files.createFile(subdir.resolve("foo"));
        checkExpectedEvent(watcher, ENTRY_CREATE, top.relativize(file));
        key.reset();
        WatchKey k = top.register(watcher,
            new WatchEvent.Kind<?>[]{ ENTRY_DELETE }, FILE_TREE);
        if (k != key)
            throw new RuntimeException("Existing key not returned");
        Files.delete(file);
        checkExpectedEvent(watcher, ENTRY_DELETE, top.relativize(file));
        key.reset();
        k = top.register(watcher, new WatchEvent.Kind<?>[]{ ENTRY_CREATE });
        if (k != key)
            throw new RuntimeException("Existing key not returned");
        Files.createFile(file);
        WatchKey nextKey;
        try {
            nextKey = watcher.poll(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        if (nextKey != null)
            throw new RuntimeException("WatchKey not expected to be polled");
        file = Files.createFile(top.resolve("bar"));
        checkExpectedEvent(watcher, ENTRY_CREATE, top.relativize(file));
        key.reset();
        k = top.register(watcher,
            new WatchEvent.Kind<?>[]{ ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY },
            FILE_TREE);
        if (k != key)
            throw new RuntimeException("Existing key not returned");
        try (OutputStream out = Files.newOutputStream(file)) {
            out.write("Double shot expresso please".getBytes("UTF-8"));
        }
        checkExpectedEvent(watcher, ENTRY_MODIFY, top.relativize(file));
        key.reset();
    }
    public static void main(String[] args) throws IOException {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("This is Windows-only test at this time!");
            return;
        }
        Path dir = TestUtil.createTemporaryDirectory();
        try {
            doTest(dir);
        } finally {
            TestUtil.removeAll(dir);
        }
    }
}
