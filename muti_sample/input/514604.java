public class ImageLoader extends Thread {
    static class ImageLoadersStorage {
        private static final int MAX_THREADS = 5;
        private static final int TIMEOUT = 4000;
        static ImageLoadersStorage instance;
        List<DecodingImageSource> queue = new LinkedList<DecodingImageSource>();
        List<Thread> loaders = new ArrayList<Thread>(MAX_THREADS);
        private int freeLoaders;
        private ImageLoadersStorage() {}
        static ImageLoadersStorage getStorage() {
            if (instance == null) {
                instance = new ImageLoadersStorage();
            }
            return instance;
        }
    }
    ImageLoader() {
        super();
        setDaemon(true);
    }
    private static void createLoader() {
        final ImageLoadersStorage storage = ImageLoadersStorage.getStorage();
        synchronized(storage.loaders) {
            if (storage.loaders.size() < ImageLoadersStorage.MAX_THREADS) {
                AccessController.doPrivileged(
                        new PrivilegedAction<Void>() {
                            public Void run() {
                                ImageLoader loader = new ImageLoader();
                                storage.loaders.add(loader);
                                loader.start();
                                return null;
                            }
                        });
            }
        }
    }
    public static void addImageSource(DecodingImageSource imgSrc) {
        ImageLoadersStorage storage = ImageLoadersStorage.getStorage();
        synchronized(storage.queue) {
            if (!storage.queue.contains(imgSrc)) {
                storage.queue.add(imgSrc);
            }
            if (storage.freeLoaders == 0) {
                createLoader();
            }
            storage.queue.notify();
        }
    }
    private static DecodingImageSource getWaitingImageSource() {
        ImageLoadersStorage storage = ImageLoadersStorage.getStorage();
        synchronized(storage.queue) {
            DecodingImageSource isrc = null;
            if (storage.queue.size() == 0) {
                try {
                    storage.freeLoaders++;
                    storage.queue.wait(ImageLoadersStorage.TIMEOUT);
                } catch (InterruptedException e) {
                    return null;
                } finally {
                    storage.freeLoaders--;
                }
            }
            if (storage.queue.size() > 0) {
                isrc = storage.queue.get(0);
                storage.queue.remove(0);
            }
            return isrc;
        }
    }
    @Override
    public void run() {
        ImageLoadersStorage storage = ImageLoadersStorage.getStorage();
        try {
            while (storage.loaders.contains(this)) {
                Thread.interrupted(); 
                DecodingImageSource isrc = getWaitingImageSource();
                if (isrc != null) {
                    try {
                        isrc.load();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    break; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized(storage.loaders) {
                storage.loaders.remove(Thread.currentThread());
            }
        }
    }
    static void beginAnimation() {
        ImageLoadersStorage storage = ImageLoadersStorage.getStorage();
        Thread currThread = Thread.currentThread();
        synchronized(storage) {
            storage.loaders.remove(currThread);
            if (storage.freeLoaders < storage.queue.size()) {
                createLoader();
            }
        }
        currThread.setPriority(Thread.MIN_PRIORITY);
    }
    static void endAnimation() {
        ImageLoadersStorage storage = ImageLoadersStorage.getStorage();
        Thread currThread = Thread.currentThread();
        synchronized(storage) {
            if (storage.loaders.size() < ImageLoadersStorage.MAX_THREADS &&
                    !storage.loaders.contains(currThread)
            ) {
                storage.loaders.add(currThread);
            }
        }
        currThread.setPriority(Thread.NORM_PRIORITY);
    }
}