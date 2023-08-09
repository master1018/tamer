class PollingWatchService
    extends AbstractWatchService
{
    private final Map<Object,PollingWatchKey> map =
        new HashMap<Object,PollingWatchKey>();
    private final ScheduledExecutorService scheduledExecutor;
    PollingWatchService() {
        scheduledExecutor = Executors
            .newSingleThreadScheduledExecutor(new ThreadFactory() {
                 @Override
                 public Thread newThread(Runnable r) {
                     Thread t = new Thread(r);
                     t.setDaemon(true);
                     return t;
                 }});
    }
    @Override
    WatchKey register(final Path path,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifiers)
         throws IOException
    {
        if (events.length == 0)
            throw new IllegalArgumentException("No events to register");
        final Set<WatchEvent.Kind<?>> eventSet =
            new HashSet<WatchEvent.Kind<?>>(events.length);
        for (WatchEvent.Kind<?> event: events) {
            if (event == StandardWatchEventKinds.ENTRY_CREATE ||
                event == StandardWatchEventKinds.ENTRY_MODIFY ||
                event == StandardWatchEventKinds.ENTRY_DELETE)
            {
                eventSet.add(event);
                continue;
            }
            if (event == StandardWatchEventKinds.OVERFLOW) {
                if (events.length == 1)
                    throw new IllegalArgumentException("No events to register");
                continue;
            }
            if (event == null)
                throw new NullPointerException("An element in event set is 'null'");
            throw new UnsupportedOperationException(event.name());
        }
        SensitivityWatchEventModifier sensivity = SensitivityWatchEventModifier.MEDIUM;
        if (modifiers.length > 0) {
            for (WatchEvent.Modifier modifier: modifiers) {
                if (modifier == null)
                    throw new NullPointerException();
                if (modifier instanceof SensitivityWatchEventModifier) {
                    sensivity = (SensitivityWatchEventModifier)modifier;
                    continue;
                }
                throw new UnsupportedOperationException("Modifier not supported");
            }
        }
        if (!isOpen())
            throw new ClosedWatchServiceException();
        try {
            final SensitivityWatchEventModifier s = sensivity;
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<PollingWatchKey>() {
                    @Override
                    public PollingWatchKey run() throws IOException {
                        return doPrivilegedRegister(path, eventSet, s);
                    }
                });
        } catch (PrivilegedActionException pae) {
            Throwable cause = pae.getCause();
            if (cause != null && cause instanceof IOException)
                throw (IOException)cause;
            throw new AssertionError(pae);
        }
    }
    private PollingWatchKey doPrivilegedRegister(Path path,
                                                 Set<? extends WatchEvent.Kind<?>> events,
                                                 SensitivityWatchEventModifier sensivity)
        throws IOException
    {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        if (!attrs.isDirectory()) {
            throw new NotDirectoryException(path.toString());
        }
        Object fileKey = attrs.fileKey();
        if (fileKey == null)
            throw new AssertionError("File keys must be supported");
        synchronized (closeLock()) {
            if (!isOpen())
                throw new ClosedWatchServiceException();
            PollingWatchKey watchKey;
            synchronized (map) {
                watchKey = map.get(fileKey);
                if (watchKey == null) {
                    watchKey = new PollingWatchKey(path, this, fileKey);
                    map.put(fileKey, watchKey);
                } else {
                    watchKey.disable();
                }
            }
            watchKey.enable(events, sensivity.sensitivityValueInSeconds());
            return watchKey;
        }
    }
    @Override
    void implClose() throws IOException {
        synchronized (map) {
            for (Map.Entry<Object,PollingWatchKey> entry: map.entrySet()) {
                PollingWatchKey watchKey = entry.getValue();
                watchKey.disable();
                watchKey.invalidate();
            }
            map.clear();
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                scheduledExecutor.shutdown();
                return null;
            }
         });
    }
    private static class CacheEntry {
        private long lastModified;
        private int lastTickCount;
        CacheEntry(long lastModified, int lastTickCount) {
            this.lastModified = lastModified;
            this.lastTickCount = lastTickCount;
        }
        int lastTickCount() {
            return lastTickCount;
        }
        long lastModified() {
            return lastModified;
        }
        void update(long lastModified, int tickCount) {
            this.lastModified = lastModified;
            this.lastTickCount = tickCount;
        }
    }
    private class PollingWatchKey extends AbstractWatchKey {
        private final Object fileKey;
        private Set<? extends WatchEvent.Kind<?>> events;
        private ScheduledFuture<?> poller;
        private volatile boolean valid;
        private int tickCount;
        private Map<Path,CacheEntry> entries;
        PollingWatchKey(Path dir, PollingWatchService watcher, Object fileKey)
            throws IOException
        {
            super(dir, watcher);
            this.fileKey = fileKey;
            this.valid = true;
            this.tickCount = 0;
            this.entries = new HashMap<Path,CacheEntry>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry: stream) {
                    long lastModified =
                        Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
                    entries.put(entry.getFileName(), new CacheEntry(lastModified, tickCount));
                }
            } catch (DirectoryIteratorException e) {
                throw e.getCause();
            }
        }
        Object fileKey() {
            return fileKey;
        }
        @Override
        public boolean isValid() {
            return valid;
        }
        void invalidate() {
            valid = false;
        }
        void enable(Set<? extends WatchEvent.Kind<?>> events, long period) {
            synchronized (this) {
                this.events = events;
                Runnable thunk = new Runnable() { public void run() { poll(); }};
                this.poller = scheduledExecutor
                    .scheduleAtFixedRate(thunk, period, period, TimeUnit.SECONDS);
            }
        }
        void disable() {
            synchronized (this) {
                if (poller != null)
                    poller.cancel(false);
            }
        }
        @Override
        public void cancel() {
            valid = false;
            synchronized (map) {
                map.remove(fileKey());
            }
            disable();
        }
        synchronized void poll() {
            if (!valid) {
                return;
            }
            tickCount++;
            DirectoryStream<Path> stream = null;
            try {
                stream = Files.newDirectoryStream(watchable());
            } catch (IOException x) {
                cancel();
                signal();
                return;
            }
            try {
                for (Path entry: stream) {
                    long lastModified = 0L;
                    try {
                        lastModified =
                            Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
                    } catch (IOException x) {
                        continue;
                    }
                    CacheEntry e = entries.get(entry.getFileName());
                    if (e == null) {
                        entries.put(entry.getFileName(),
                                     new CacheEntry(lastModified, tickCount));
                        if (events.contains(StandardWatchEventKinds.ENTRY_CREATE)) {
                            signalEvent(StandardWatchEventKinds.ENTRY_CREATE, entry.getFileName());
                            continue;
                        } else {
                            if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
                            }
                        }
                        continue;
                    }
                    if (e.lastModified != lastModified) {
                        if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                            signalEvent(StandardWatchEventKinds.ENTRY_MODIFY,
                                        entry.getFileName());
                        }
                    }
                    e.update(lastModified, tickCount);
                }
            } catch (DirectoryIteratorException e) {
            } finally {
                try {
                    stream.close();
                } catch (IOException x) {
                }
            }
            Iterator<Map.Entry<Path,CacheEntry>> i = entries.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Path,CacheEntry> mapEntry = i.next();
                CacheEntry entry = mapEntry.getValue();
                if (entry.lastTickCount() != tickCount) {
                    Path name = mapEntry.getKey();
                    i.remove();
                    if (events.contains(StandardWatchEventKinds.ENTRY_DELETE)) {
                        signalEvent(StandardWatchEventKinds.ENTRY_DELETE, name);
                    }
                }
            }
        }
    }
}
