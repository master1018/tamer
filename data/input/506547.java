public class MonitorTest extends TestCase {
    @MediumTest
    public void testWaitArgumentsTest() throws Exception {
            try {
                synchronized (this) {
                    wait(1);
                    wait(10);
                    wait(0, 1);
                    wait(0, 999999);
                    wait(1, 1);
                    wait(1, 999999);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException("good Object.wait() interrupted",
                        ex);
            } catch (Exception ex) {
                throw new RuntimeException("Unexpected exception when calling" +
                        "Object.wait() with good arguments", ex);
            }
            boolean sawException = false;
            try {
                synchronized (this) {
                    wait(-1);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException("bad Object.wait() interrupted", ex);
            } catch (IllegalArgumentException ex) {
                sawException = true;
            } catch (Exception ex) {
                throw new RuntimeException("Unexpected exception when calling" +
                        "Object.wait() with bad arguments", ex);
            }
            if (!sawException) {
                throw new RuntimeException("bad call to Object.wait() should " +
                        "have thrown IllegalArgumentException");
            }
            sawException = false;
            try {
                synchronized (this) {
                    wait(0, -1);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException("bad Object.wait() interrupted", ex);
            } catch (IllegalArgumentException ex) {
                sawException = true;
            } catch (Exception ex) {
                throw new RuntimeException("Unexpected exception when calling" +
                        "Object.wait() with bad arguments", ex);
            }
            if (!sawException) {
                throw new RuntimeException("bad call to Object.wait() should " +
                        "have thrown IllegalArgumentException");
            }
            sawException = false;
            try {
                synchronized (this) {
                    wait(0, 1000000);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException("bad Object.wait() interrupted", ex);
            } catch (IllegalArgumentException ex) {
                sawException = true;
            } catch (Exception ex) {
                throw new RuntimeException("Unexpected exception when calling" +
                        "Object.wait() with bad arguments", ex);
            }
            if (!sawException) {
                throw new RuntimeException("bad call to Object.wait() should " +
                        "have thrown IllegalArgumentException");
            }
    }
    private class Interrupter extends Thread {
            Waiter waiter;
            Interrupter(String name, Waiter waiter) {
                super(name);
                this.waiter = waiter;
            }
            public void run() {
                try {
                    run_inner();
                } catch (Throwable t) {
                    MonitorTest.errorException = t;
                    MonitorTest.testThread.interrupt();
                }
            }
            void run_inner() {
                waiter.spin = true;
                waiter.start();
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException ex) {
                    throw new RuntimeException("Test sleep interrupted.", ex);
                }
                waiter.interrupt();
                waiter.spin = false;
                for (int i = 0; i < 3; i++) {
                    synchronized (waiter.interrupterLock) {
                        try {
                            waiter.interrupterLock.wait();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException("Test wait interrupted.", ex);
                        }
                    }
                    synchronized (waiter) {
                        waiter.interrupt();
                    }
                }
                try {
                    waiter.join();
                } catch (InterruptedException ex) {
                    throw new RuntimeException("Test join interrupted.", ex);
                }
            }
        }
    private class Waiter extends Thread {
            Object interrupterLock = new Object();
            Boolean spin = false;
            Waiter(String name) {
                super(name);
            }
            public void run() {
                try {
                    run_inner();
                } catch (Throwable t) {
                    MonitorTest.errorException = t;
                    MonitorTest.testThread.interrupt();
                }
            }
            void run_inner() {
                while (spin) {
                }
                if (interrupted()) {
                } else {
                    throw new RuntimeException("Thread not interrupted " +
                                               "during spin");
                }
                synchronized (this) {
                    Boolean sawEx = false;
                    try {
                        synchronized (interrupterLock) {
                            interrupterLock.notify();
                        }
                        this.wait();
                    } catch (InterruptedException ex) {
                        sawEx = true;
                    }
                    if (!sawEx) {
                        throw new RuntimeException("Thread not interrupted " +
                                                   "during wait()");
                    }
                }
                synchronized (this) {
                    Boolean sawEx = false;
                    try {
                        synchronized (interrupterLock) {
                            interrupterLock.notify();
                        }
                        this.wait(1000);
                    } catch (InterruptedException ex) {
                        sawEx = true;
                    }
                    if (!sawEx) {
                        throw new RuntimeException("Thread not interrupted " +
                                                   "during wait(1000)");
                    }
                }
                synchronized (this) {
                    Boolean sawEx = false;
                    try {
                        synchronized (interrupterLock) {
                            interrupterLock.notify();
                        }
                        this.wait(1000, 5000);
                    } catch (InterruptedException ex) {
                        sawEx = true;
                    }
                    if (!sawEx) {
                        throw new RuntimeException("Thread not interrupted " +
                                                   "during wait(1000, 5000)");
                    }
                }
            }
        }
    private static Throwable errorException;
    private static Thread testThread;
    public void testInterruptTest() throws Exception {
            testThread = Thread.currentThread();
            errorException = null;
            Waiter waiter = new Waiter("InterruptTest Waiter");
            Interrupter interrupter =
                    new Interrupter("InterruptTest Interrupter", waiter);
            interrupter.start();
            try {
                interrupter.join();
                waiter.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException("Test join interrupted.", ex);
            }
            if (errorException != null) {
                throw new RuntimeException("InterruptTest failed",
                                           errorException);
            }
    }
     private static void deepWait(int depth, Object lock) {
            synchronized (lock) {
                if (depth > 0) {
                    deepWait(depth - 1, lock);
                } else {
                    String threadName = Thread.currentThread().getName();
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        private class Worker extends Thread {
            Object lock;
            int id;
            Worker(int id, Object lock) {
                super("Worker(" + id + ")");
                this.id = id;
                this.lock = lock;
            }
            public void run() {
                int iterations = 0;
                while (MonitorTest.running) {
                    MonitorTest.deepWait(id, lock);
                    iterations++;
                }
            }
        }
    private static Object commonLock = new Object();
        private static Boolean running = false;
    @LargeTest
    public void testNestedMonitors() throws Exception {
        final int NUM_WORKERS = 5;
            Worker w[] = new Worker[NUM_WORKERS];
            int i;
            for (i = 0; i < NUM_WORKERS; i++) {
                w[i] = new Worker(i * 2 - 1, new Object());
            }
            running = true;
            for (i = 0; i < NUM_WORKERS; i++) {
                w[i].start();
            }
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ex) {
            }
            for (i = 0; i < 100; i++) {
                for (int j = 0; j < NUM_WORKERS; j++) {
                    synchronized (w[j].lock) {
                        w[j].lock.notify();
                    }
                }
            }
            running = false;
            for (i = 0; i < NUM_WORKERS; i++) {
                synchronized (w[i].lock) {
                    w[i].lock.notifyAll();
                }
            }
    }
    private static class CompareAndExchange extends Thread {
        static Object toggleLock = null;
        static int toggle = -1;
        static Boolean running = false;
        public void run() {
            toggleLock = new Object();
            toggle = -1;
            Worker w1 = new Worker(0, 1);
            Worker w2 = new Worker(2, 3);
            Worker w3 = new Worker(4, 5);
            Worker w4 = new Worker(6, 7);
            running = true;
            w1.start();
            w2.start();
            w3.start();
            w4.start();
            try {
                this.sleep(10000);
            } catch (InterruptedException ex) {
            }
            running = false;
            toggleLock = null;
        }
        class Worker extends Thread {
            int i1;
            int i2;
            Worker(int i1, int i2) {
                super("Worker(" + i1 + ", " + i2 + ")");
                this.i1 = i1;
                this.i2 = i2;
            }
            public void run() {
                int iterations = 0;
                Object toggleLock = CompareAndExchange.toggleLock;
                try {
                    while (CompareAndExchange.running) {
                        synchronized (toggleLock) {
                            int test;
                            int check;
                            if (CompareAndExchange.toggle == i1) {
                                this.sleep(5 + i2);
                                CompareAndExchange.toggle = test = i2;
                            } else {
                                this.sleep(5 + i1);
                                CompareAndExchange.toggle = test = i1;
                            }
                            if ((check = CompareAndExchange.toggle) != test) {
                                throw new RuntimeException(
                                        "locked value changed");
                            }
                        }
                        iterations++;
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
