public class GetResource {
    CyclicBarrier go = new CyclicBarrier(2);
    CyclicBarrier done = new CyclicBarrier(2);
    Thread t1, t2;
    public GetResource() {
        t1 = new Thread() {
            public void run() {
                Properties prop = System.getProperties();
                synchronized (prop) {
                    System.out.println("Thread 1 ready");
                    try {
                        go.await();
                        prop.put("property", "value");
                        prop.store(System.out, "");
                        done.await();   
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Thread 1 exits");
            }
        };
        t2 = new Thread()  {
            public void run() {
                System.out.println("Thread 2 ready");
                try {
                    go.await();  
                    URL u1 = Thread.currentThread().getContextClassLoader().getResource("unknownresource");
                    URL u2 = Thread.currentThread().getContextClassLoader().getResource("sun/util/resources/CalendarData.class");
                    if (u2 == null) {
                        throw new RuntimeException("Test failed: resource not found");
                    }
                    done.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Thread 2 exits");
            }
        };
    }
    public void run() throws Exception {
        t1.start();
        t2.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
    }
    public static void main(String[] args) throws Exception {
        new GetResource().run();
    }
}
