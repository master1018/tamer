public class LockSupportTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(LockSupportTest.class);
    }
    public void testPark() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        LockSupport.park();
                    } catch(Exception e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            LockSupport.unpark(t);
            t.join();
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testPark2() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(SHORT_DELAY_MS);
                        LockSupport.park();
                    } catch(Exception e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            LockSupport.unpark(t);
            t.join();
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testPark3() {
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			LockSupport.park();
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testPark4() {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        lock.lock();
                        LockSupport.park();
                    } catch(Exception e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            t.interrupt();
            lock.unlock();
            t.join();
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testParkNanos() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        LockSupport.parkNanos(1000);
                    } catch(Exception e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            t.join();
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testParkUntil() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        long d = new Date().getTime() + 100;
                        LockSupport.parkUntil(d);
                    } catch(Exception e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            t.join();
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
}
