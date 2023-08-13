public class OversynchronizedTest extends Thread {
    private static PrintWriter writer = new PrintWriter(System.out);
    private static TestObj testObj = new TestObj("This is a test.", writer);
    private static int loopNum = 100;
    public void run() {
        for(int i=0; i<loopNum; i++) {
            testObj.test();
           writer.println(testObj);
        }
    }
    public static void main(String args[]) throws Exception {
        writer.println((Object)null);
        int num = 5;
        OversynchronizedTest[] t = new OversynchronizedTest[num];
        for(int i=0; i<num; i++) {
            t[i] = new OversynchronizedTest();
            t[i].start();
        }
        for(int i=0; i <num; i++) {
            t[i].join();
        }
        System.out.println("Test completed");
    }
}
class TestObj {
    String mStr;
    TestObj(String str, PrintWriter writer) {
        mStr = str;
        this.writer = writer;
    }
    synchronized void test() {
        try {
            long t = Math.round(Math.random()*10);
            Thread.currentThread().sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        writer.println("In test().");
    }
    synchronized public String toString() {
        writer.println("Calling toString\n");
        return mStr;
    }
    private PrintWriter writer;
}
