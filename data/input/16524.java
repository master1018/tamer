public class RequestProcessor implements Runnable {
    private static Queue requestQueue;
    private static Thread dispatcher;
    public static void postRequest(Request req) {
        lazyInitialize();
        requestQueue.enqueue(req);
    }
    public void run() {
        lazyInitialize();
        while (true) {
            try {
                Object obj = requestQueue.dequeue();
                if (obj instanceof Request) { 
                    Request req = (Request)obj;
                    try {
                        req.execute();
                    } catch (Throwable t) {
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }
    public static synchronized void startProcessing() {
        if (dispatcher == null) {
            dispatcher = new Thread(new RequestProcessor(), "Request Processor");
            dispatcher.setPriority(Thread.NORM_PRIORITY + 2);
            dispatcher.start();
        }
    }
    private static synchronized void lazyInitialize() {
        if (requestQueue == null) {
            requestQueue = new Queue();
        }
    }
}
