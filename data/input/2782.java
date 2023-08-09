class ServiceNotifier extends Thread {
    private PrintService service;
    private Vector listeners;
    private boolean stop = false;
    private PrintServiceAttributeSet lastSet;
    ServiceNotifier(PrintService service) {
        super(service.getName() + " notifier");
        this.service = service;
        listeners = new Vector();
        try {
              setPriority(Thread.NORM_PRIORITY-1);
              setDaemon(true);
              start();
        } catch (SecurityException e) {
        }
    }
    void addListener(PrintServiceAttributeListener listener) {
        synchronized (this) {
            if (listener == null || listeners == null) {
                return;
            }
            listeners.add(listener);
        }
    }
    void removeListener(PrintServiceAttributeListener listener) {
         synchronized (this) {
            if (listener == null || listeners == null) {
                return;
            }
            listeners.remove(listener);
        }
    }
   boolean isEmpty() {
     return (listeners == null || listeners.isEmpty());
   }
   void stopNotifier() {
      stop = true;
   }
    void wake() {
        try {
            interrupt();
        } catch (SecurityException e) {
        }
    }
    public void run() {
       long minSleepTime = 15000;
       long sleepTime = 2000;
       HashPrintServiceAttributeSet attrs;
       PrintServiceAttributeEvent attrEvent;
       PrintServiceAttributeListener listener;
       PrintServiceAttributeSet psa;
       while (!stop) {
           try {
                Thread.sleep(sleepTime);
           } catch (InterruptedException e) {
           }
           synchronized (this) {
               if (listeners == null) {
                   continue;
               }
               long startTime = System.currentTimeMillis();
               if (listeners != null) {
                    if (service instanceof AttributeUpdater) {
                       psa =
                          ((AttributeUpdater)service).getUpdatedAttributes();
                    } else {
                       psa = service.getAttributes();
                    }
                    if (psa != null && !psa.isEmpty()) {
                        for (int i = 0; i < listeners.size() ; i++) {
                            listener = (PrintServiceAttributeListener)
                                listeners.elementAt(i);
                            attrs =
                                new HashPrintServiceAttributeSet(psa);
                            attrEvent =
                                new PrintServiceAttributeEvent(service, attrs);
                            listener.attributeUpdate(attrEvent);
                        }
                    }
               }
               sleepTime = (System.currentTimeMillis()-startTime)*10;
               if (sleepTime < minSleepTime) {
                   sleepTime = minSleepTime;
               }
           }
       }
    }
}
