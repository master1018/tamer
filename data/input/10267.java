public class SensitivityModifier {
    static final Random rand = new Random();
    static void register(Path[] dirs, WatchService watcher) throws IOException {
        SensitivityWatchEventModifier[] sensitivtives =
            SensitivityWatchEventModifier.values();
        for (int i=0; i<dirs.length; i++) {
            SensitivityWatchEventModifier sensivity =
                sensitivtives[ rand.nextInt(sensitivtives.length) ];
            Path dir = dirs[i];
            dir.register(watcher, new WatchEvent.Kind<?>[]{ ENTRY_MODIFY }, sensivity);
        }
    }
    @SuppressWarnings("unchecked")
    static void doTest(Path top) throws Exception {
        FileSystem fs = top.getFileSystem();
        WatchService watcher = fs.newWatchService();
        int nDirs = 5 + rand.nextInt(20);
        int nFiles = 50 + rand.nextInt(50);
        Path[] dirs = new Path[nDirs];
        Path[] files = new Path[nFiles];
        for (int i=0; i<nDirs; i++) {
            dirs[i] = Files.createDirectory(top.resolve("dir" + i));
        }
        for (int i=0; i<nFiles; i++) {
            Path dir = dirs[rand.nextInt(nDirs)];
            files[i] = Files.createFile(dir.resolve("file" + i));
        }
        register(dirs, watcher);
        try { Thread.sleep(1000); } catch (InterruptedException e) { }
        for (int i=0; i<10; i++) {
            Path file = files[rand.nextInt(nFiles)];
            System.out.println("Modify: " + file);
            try (OutputStream out = Files.newOutputStream(file)) {
                out.write(new byte[100]);
            }
            System.out.println("Waiting for event...");
            WatchKey key = watcher.take();
            WatchEvent<?> event = key.pollEvents().iterator().next();
            if (event.kind() != ENTRY_MODIFY)
                throw new RuntimeException("Unexpected event: " + event);
            Path name = ((WatchEvent<Path>)event).context();
            if (!name.equals(file.getFileName()))
                throw new RuntimeException("Unexpected context: " + name);
            System.out.println("Event OK");
            do {
                key.reset();
                key = watcher.poll(1, TimeUnit.SECONDS);
            } while (key != null);
            register(dirs, watcher);
        }
        watcher.close();
    }
    public static void main(String[] args) throws Exception {
        Path dir = TestUtil.createTemporaryDirectory();
        try {
            doTest(dir);
        } finally {
            TestUtil.removeAll(dir);
        }
    }
}
