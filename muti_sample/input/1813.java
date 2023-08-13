class ImageFetcher extends Thread {
    static final int HIGH_PRIORITY = 8;
    static final int LOW_PRIORITY = 3;
    static final int ANIM_PRIORITY = 2;
    static final int TIMEOUT = 5000; 
    private ImageFetcher(ThreadGroup threadGroup, int index) {
        super(threadGroup, "Image Fetcher " + index);
        setDaemon(true);
    }
    public static boolean add(ImageFetchable src) {
        final FetcherInfo info = FetcherInfo.getFetcherInfo();
        synchronized(info.waitList) {
            if (!info.waitList.contains(src)) {
                info.waitList.addElement(src);
                if (info.numWaiting == 0 &&
                            info.numFetchers < info.fetchers.length) {
                    createFetchers(info);
                }
                if (info.numFetchers > 0) {
                    info.waitList.notify();
                } else {
                    info.waitList.removeElement(src);
                    return false;
                }
            }
        }
        return true;
    }
    public static void remove(ImageFetchable src) {
        final FetcherInfo info = FetcherInfo.getFetcherInfo();
        synchronized(info.waitList) {
            if (info.waitList.contains(src)) {
                info.waitList.removeElement(src);
            }
        }
    }
    public static boolean isFetcher(Thread t) {
        final FetcherInfo info = FetcherInfo.getFetcherInfo();
        synchronized(info.waitList) {
            for (int i = 0; i < info.fetchers.length; i++) {
                if (info.fetchers[i] == t) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean amFetcher() {
        return isFetcher(Thread.currentThread());
    }
    private static ImageFetchable nextImage() {
        final FetcherInfo info = FetcherInfo.getFetcherInfo();
        synchronized(info.waitList) {
            ImageFetchable src = null;
            long end = System.currentTimeMillis() + TIMEOUT;
            while (src == null) {
                while (info.waitList.size() == 0) {
                    long now = System.currentTimeMillis();
                    if (now >= end) {
                        return null;
                    }
                    try {
                        info.numWaiting++;
                        info.waitList.wait(end - now);
                    } catch (InterruptedException e) {
                        return null;
                    } finally {
                        info.numWaiting--;
                    }
                }
                src = (ImageFetchable) info.waitList.elementAt(0);
                info.waitList.removeElement(src);
            }
            return src;
        }
    }
    public void run() {
        final FetcherInfo info = FetcherInfo.getFetcherInfo();
        try {
            fetchloop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized(info.waitList) {
                Thread me = Thread.currentThread();
                for (int i = 0; i < info.fetchers.length; i++) {
                    if (info.fetchers[i] == me) {
                        info.fetchers[i] = null;
                        info.numFetchers--;
                    }
                }
            }
        }
    }
    private void fetchloop() {
        Thread me = Thread.currentThread();
        while (isFetcher(me)) {
            me.interrupted();
            me.setPriority(HIGH_PRIORITY);
            ImageFetchable src = nextImage();
            if (src == null) {
                return;
            }
            try {
                src.doFetch();
            } catch (Exception e) {
                System.err.println("Uncaught error fetching image:");
                e.printStackTrace();
            }
            stoppingAnimation(me);
        }
    }
    static void startingAnimation() {
        final FetcherInfo info = FetcherInfo.getFetcherInfo();
        Thread me = Thread.currentThread();
        synchronized(info.waitList) {
            for (int i = 0; i < info.fetchers.length; i++) {
                if (info.fetchers[i] == me) {
                    info.fetchers[i] = null;
                    info.numFetchers--;
                    me.setName("Image Animator " + i);
                    if(info.waitList.size() > info.numWaiting) {
                       createFetchers(info);
                    }
                    return;
                }
            }
        }
        me.setPriority(ANIM_PRIORITY);
        me.setName("Image Animator");
    }
    private static void stoppingAnimation(Thread me) {
        final FetcherInfo info = FetcherInfo.getFetcherInfo();
        synchronized(info.waitList) {
            int index = -1;
            for (int i = 0; i < info.fetchers.length; i++) {
                if (info.fetchers[i] == me) {
                    return;
                }
                if (info.fetchers[i] == null) {
                    index = i;
                }
            }
            if (index >= 0) {
                info.fetchers[index] = me;
                info.numFetchers++;
                me.setName("Image Fetcher " + index);
                return;
            }
        }
    }
    private static void createFetchers(final FetcherInfo info) {
       final AppContext appContext = AppContext.getAppContext();
       ThreadGroup threadGroup = appContext.getThreadGroup();
       ThreadGroup fetcherThreadGroup;
       try {
          if (threadGroup.getParent() != null) {
             fetcherThreadGroup = threadGroup;
          } else {
             threadGroup = Thread.currentThread().getThreadGroup();
             ThreadGroup parent = threadGroup.getParent();
             while ((parent != null)
                  && (parent.getParent() != null)) {
                  threadGroup = parent;
                  parent = threadGroup.getParent();
             }
             fetcherThreadGroup = threadGroup;
         }
       } catch (SecurityException e) {
         fetcherThreadGroup = appContext.getThreadGroup();
       }
       final ThreadGroup fetcherGroup = fetcherThreadGroup;
       java.security.AccessController.doPrivileged(
         new java.security.PrivilegedAction() {
         public Object run() {
             for (int i = 0; i < info.fetchers.length; i++) {
               if (info.fetchers[i] == null) {
                   ImageFetcher f = new ImageFetcher(
                           fetcherGroup, i);
                   try {
                       f.start();
                       info.fetchers[i] = f;
                       info.numFetchers++;
                       break;
                   } catch (Error e) {
                   }
               }
             }
          return null;
        }
       });
      return;
    }
}
class FetcherInfo {
    static final int MAX_NUM_FETCHERS_PER_APPCONTEXT = 4;
    Thread[] fetchers;
    int numFetchers;
    int numWaiting;
    Vector waitList;
    private FetcherInfo() {
        fetchers = new Thread[MAX_NUM_FETCHERS_PER_APPCONTEXT];
        numFetchers = 0;
        numWaiting = 0;
        waitList = new Vector();
    }
    private static final Object FETCHER_INFO_KEY =
                                        new StringBuffer("FetcherInfo");
    static FetcherInfo getFetcherInfo() {
        AppContext appContext = AppContext.getAppContext();
        synchronized(appContext) {
            FetcherInfo info = (FetcherInfo)appContext.get(FETCHER_INFO_KEY);
            if (info == null) {
                info = new FetcherInfo();
                appContext.put(FETCHER_INFO_KEY, info);
            }
            return info;
        }
    }
}
