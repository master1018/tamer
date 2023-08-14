public class FileClientSessionCache {
    static final int MAX_SIZE = 12; 
    static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(
                    FileClientSessionCache.class.getName());
    private FileClientSessionCache() {}
    static class Impl implements SSLClientSessionCache {
        final File directory;
        Map<String, File> accessOrder = newAccessOrder();
        int size;
        String[] initialFiles;
        Impl(File directory) throws IOException {
            boolean exists = directory.exists();
            if (exists && !directory.isDirectory()) {
                throw new IOException(directory
                        + " exists but is not a directory.");
            }
            if (exists) {
                initialFiles = directory.list();
                Arrays.sort(initialFiles);
                size = initialFiles.length;
            } else {
                if (!directory.mkdirs()) {
                    throw new IOException("Creation of " + directory
                            + " directory failed.");
                }
                size = 0;
            }
            this.directory = directory;
        }
        private static Map<String, File> newAccessOrder() {
            return new LinkedHashMap<String, File>(
                    MAX_SIZE, 0.75f, true );
        }
        private static String fileName(String host, int port) {
            if (host == null) {
                throw new NullPointerException("host");
            }
            return host + "." + port;
        }
        public synchronized byte[] getSessionData(String host, int port) {
            String name = fileName(host, port);
            File file = accessOrder.get(name);
            if (file == null) {
                if (initialFiles == null) {
                    return null;
                }
                if (Arrays.binarySearch(initialFiles, name) < 0) {
                    return null;
                }
                file = new File(directory, name);
                accessOrder.put(name, file);
            }
            FileInputStream in;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                logReadError(host, e);
                return null;
            }
            try {
                int size = (int) file.length();
                byte[] data = new byte[size];
                new DataInputStream(in).readFully(data);
                logger.log(Level.FINE, "Read session for " + host + ".");
                return data;
            } catch (IOException e) {
                logReadError(host, e);
                return null;
            } finally {
                try {
                    in.close();
                } catch (IOException e) {  }
            }
        }
        static void logReadError(String host, Throwable t) {
            logger.log(Level.INFO, "Error reading session data for " + host
                    + ".", t);
        }
        public synchronized void putSessionData(SSLSession session,
                byte[] sessionData) {
            String host = session.getPeerHost();
            if (sessionData == null) {
                throw new NullPointerException("sessionData");
            }
            String name = fileName(host, session.getPeerPort());
            File file = new File(directory, name);
            boolean existedBefore = file.exists();
            FileOutputStream out;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                logWriteError(host, e);
                return;
            }
            if (!existedBefore) {
                size++;
                makeRoom();
            }
            boolean writeSuccessful = false;
            try {
                out.write(sessionData);
                writeSuccessful = true;
            } catch (IOException e) {
                logWriteError(host, e);
            } finally {
                boolean closeSuccessful = false;
                try {
                    out.close();
                    closeSuccessful = true;
                } catch (IOException e) {
                    logWriteError(host, e);
                } finally {
                    if (!writeSuccessful || !closeSuccessful) {
                        delete(file);
                    } else {
                        accessOrder.put(name, file);
                        logger.log(Level.FINE, "Stored session for " + host
                                + ".");
                    }
                }
            }
        }
        private void makeRoom() {
            if (size <= MAX_SIZE) {
                return;
            }
            indexFiles();
            int removals = size - MAX_SIZE;
            Iterator<File> i = accessOrder.values().iterator();
            do {
                delete(i.next());
                i.remove();
            } while (--removals > 0);
        }
        private void indexFiles() {
            String[] initialFiles = this.initialFiles;
            if (initialFiles != null) {
                this.initialFiles = null;
                Set<CacheFile> diskOnly = new TreeSet<CacheFile>();
                for (String name : initialFiles) {
                    if (!accessOrder.containsKey(name)) {
                        diskOnly.add(new CacheFile(directory, name));
                    }
                }
                if (!diskOnly.isEmpty()) {
                    Map<String, File> newOrder = newAccessOrder();
                    for (CacheFile cacheFile : diskOnly) {
                        newOrder.put(cacheFile.name, cacheFile);
                    }
                    newOrder.putAll(accessOrder);
                    this.accessOrder = newOrder;
                }
            }
        }
        @SuppressWarnings("ThrowableInstanceNeverThrown")
        private void delete(File file) {
            if (!file.delete()) {
                logger.log(Level.INFO, "Failed to delete " + file + ".",
                        new IOException());
            }
            size--;
        }
        static void logWriteError(String host, Throwable t) {
            logger.log(Level.INFO, "Error writing session data for "
                    + host + ".", t);
        }
    }
    static final Map<File, FileClientSessionCache.Impl> caches
            = new HashMap<File, FileClientSessionCache.Impl>();
    public static synchronized SSLClientSessionCache usingDirectory(
            File directory) throws IOException {
        FileClientSessionCache.Impl cache = caches.get(directory);
        if (cache == null) {
            cache = new FileClientSessionCache.Impl(directory);
            caches.put(directory, cache);
        }
        return cache;
    }
    static synchronized void reset() {
        caches.clear();
    }
    static class CacheFile extends File {
        final String name;
        CacheFile(File dir, String name) {
            super(dir, name);
            this.name = name;
        }
        long lastModified = -1;
        @Override
        public long lastModified() {
            long lastModified = this.lastModified;
            if (lastModified == -1) {
                lastModified = this.lastModified = super.lastModified();
            }
            return lastModified;
        }
        @Override
        public int compareTo(File another) {
            long result = lastModified() - another.lastModified();
            if (result == 0) {
                return super.compareTo(another);
            }
            return result < 0 ? -1 : 1;
        }
    }
}