public class ExchangerTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(ExchangerTest.class);
    }
    public void testExchange() {
        final Exchanger e = new Exchanger();
        Thread t1 = new Thread(new Runnable(){
                public void run(){
                    try {
                        Object v = e.exchange(one);
                        threadAssertEquals(v, two);
                        Object w = e.exchange(v);
                        threadAssertEquals(w, one);
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable(){
                public void run(){
                    try {
                        Object v = e.exchange(two);
                        threadAssertEquals(v, one);
                        Object w = e.exchange(v);
                        threadAssertEquals(w, two);
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        } catch(InterruptedException ex) {
            unexpectedException();
        }
    }
    public void testTimedExchange() {
        final Exchanger e = new Exchanger();
        Thread t1 = new Thread(new Runnable(){
                public void run(){
                    try {
                        Object v = e.exchange(one, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadAssertEquals(v, two);
                        Object w = e.exchange(v, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadAssertEquals(w, one);
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    } catch(TimeoutException toe) {
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable(){
                public void run(){
                    try {
                        Object v = e.exchange(two, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadAssertEquals(v, one);
                        Object w = e.exchange(v, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadAssertEquals(w, two);
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    } catch(TimeoutException toe) {
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        } catch(InterruptedException ex) {
            unexpectedException();
        }
    }
    public void testExchange_InterruptedException(){
        final Exchanger e = new Exchanger();
        Thread t = new Thread(new Runnable() {
                public void run(){
                    try {
                        e.exchange(one);
                        threadShouldThrow();
                    } catch(InterruptedException success){
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException ex) {
            unexpectedException();
        }
    }
    public void testTimedExchange_InterruptedException(){
        final Exchanger e = new Exchanger();
        Thread t = new Thread(new Runnable() {
                public void run(){
                    try {
                        e.exchange(null, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(InterruptedException success){
                    } catch(Exception e2){
                        threadFail("should throw IE");
                    }
                }
            });
        try {
            t.start();
            t.interrupt();
            t.join();
        } catch(InterruptedException ex){
            unexpectedException();
        }
    }
    public void testExchange_TimeOutException(){
        final Exchanger e = new Exchanger();
        Thread t = new Thread(new Runnable() {
                public void run(){
                    try {
                        e.exchange(null, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(InterruptedException e2){
                        threadFail("should throw TOE");
                    }
                }
            });
        try {
            t.start();
            t.join();
        } catch(InterruptedException ex){
            unexpectedException();
        }
    }
    public void testReplacementAfterExchange() {
        final Exchanger e = new Exchanger();
        Thread t1 = new Thread(new Runnable(){
                public void run(){
                    try {
                        Object v = e.exchange(one);
                        threadAssertEquals(v, two);
                        Object w = e.exchange(v);
                        threadShouldThrow();
                    } catch(InterruptedException success){
                    }
                }
            });
        Thread t2 = new Thread(new Runnable(){
                public void run(){
                    try {
                        Object v = e.exchange(two);
                        threadAssertEquals(v, one);
                        Thread.sleep(SMALL_DELAY_MS);
                        Object w = e.exchange(v);
                        threadAssertEquals(w, three);
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t3 = new Thread(new Runnable(){
                public void run(){
                    try {
                        Thread.sleep(SMALL_DELAY_MS);
                        Object w = e.exchange(three);
                        threadAssertEquals(w, one);
                    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t3.start();
            Thread.sleep(SHORT_DELAY_MS);
            t1.interrupt();
            t1.join();
            t2.join();
            t3.join();
        } catch(InterruptedException ex) {
            unexpectedException();
        }
    }
}
