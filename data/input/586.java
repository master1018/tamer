public class LotsOfEvents {
    static final Random rand = new Random();
    public static void main(String[] args) throws Exception {
        Path dir = TestUtil.createTemporaryDirectory();
        try {
            testOverflowEvent(dir);
            testModifyEventsQueuing(dir);
        } finally {
            TestUtil.removeAll(dir);
        }
    }
    static void testOverflowEvent(Path dir)
        throws IOException, InterruptedException
    {
        try (WatchService watcher = dir.getFileSystem().newWatchService()) {
            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);
            int n = 1024;
            Path[] files = new Path[n];
            for (int i=0; i<n; i++) {
                files[i] = Files.createFile(dir.resolve("foo" + i));
            }
            Thread.sleep(1000);
            drainAndCheckOverflowEvents(watcher, ENTRY_CREATE, n);
            for (int i=0; i<n; i++) {
                Files.delete(files[i]);
            }
            Thread.sleep(1000);
            drainAndCheckOverflowEvents(watcher, ENTRY_DELETE, n);
        }
    }
    static void drainAndCheckOverflowEvents(WatchService watcher,
                                            WatchEvent.Kind<?> expectedKind,
                                            int count)
        throws IOException, InterruptedException
    {
        WatchKey key = watcher.poll(15, TimeUnit.SECONDS);
        if (key != null && count == 0)
            throw new RuntimeException("Key was signalled (unexpected)");
        if (key == null && count > 0)
            throw new RuntimeException("Key not signalled (unexpected)");
        int nread = 0;
        boolean gotOverflow = false;
        while (key != null) {
            List<WatchEvent<?>> events = key.pollEvents();
            for (WatchEvent<?> event: events) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == expectedKind) {
                    if (++nread > count)
                        throw new RuntimeException("More events than expected!!");
                } else if (kind == OVERFLOW) {
                    if (events.size() > 1)
                        throw new RuntimeException("Overflow retrieved with other events");
                    gotOverflow = true;
                } else {
                    throw new RuntimeException("Unexpected event '" + kind + "'");
                }
            }
            if (!key.reset())
                throw new RuntimeException("Key is no longer valid");
            key = watcher.poll(2, TimeUnit.SECONDS);
        }
        if (nread < count && !gotOverflow)
            throw new RuntimeException("Insufficient events");
    }
    static void testModifyEventsQueuing(Path dir)
        throws IOException, InterruptedException
    {
        final int nfiles = 5 + rand.nextInt(10);
        DirectoryEntry[] entries = new DirectoryEntry[nfiles];
        for (int i=0; i<nfiles; i++) {
            entries[i] = new DirectoryEntry(dir.resolve("foo" + i));
            entries[i].deleteIfExists();
            if (rand.nextBoolean())
                entries[i].create();
        }
        try (WatchService watcher = dir.getFileSystem().newWatchService()) {
            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            for (int round=0; round<10; round++) {
                for (int i=0; i<100; i++) {
                    DirectoryEntry entry = entries[rand.nextInt(nfiles)];
                    int action = rand.nextInt(10);
                    switch (action) {
                        case 0 : entry.create(); break;
                        case 1 : entry.deleteIfExists(); break;
                        default: entry.modifyIfExists();
                    }
                }
                WatchKey key = watcher.poll(15, TimeUnit.SECONDS);
                while (key != null) {
                    Set<Path> modified = new HashSet<>();
                    for (WatchEvent<?> event: key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path file = (kind == OVERFLOW) ? null : (Path)event.context();
                        if (kind == ENTRY_MODIFY) {
                            boolean added = modified.add(file);
                            if (!added) {
                                throw new RuntimeException(
                                    "ENTRY_MODIFY events not queued efficiently");
                            }
                        } else {
                            if (file != null) modified.remove(file);
                        }
                    }
                    if (!key.reset())
                        throw new RuntimeException("Key is no longer valid");
                    key = watcher.poll(2, TimeUnit.SECONDS);
                }
            }
        }
    }
    static class DirectoryEntry {
        private final Path file;
        DirectoryEntry(Path file) {
            this.file = file;
        }
        void create() throws IOException {
            if (Files.notExists(file))
                Files.createFile(file);
        }
        void deleteIfExists() throws IOException {
            Files.deleteIfExists(file);
        }
        void modifyIfExists() throws IOException {
            if (Files.exists(file)) {
                try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.APPEND)) {
                    out.write("message".getBytes());
                }
            }
        }
    }
}
