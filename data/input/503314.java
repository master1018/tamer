public class BroadcasterTest extends TestCase {
    private static final int MESSAGE_A = 23234;
    private static final int MESSAGE_B = 3;
    private static final int MESSAGE_C = 14;
    private static final int MESSAGE_D = 95;
    @MediumTest
    public void test1() throws Exception {
        HandlerTester tester = new HandlerTester() {
            Handler h;
            public void go() {
                Broadcaster b = new Broadcaster();
                h = new H();
                b.request(MESSAGE_A, h, MESSAGE_B);
                Message msg = new Message();
                msg.what = MESSAGE_A;
                b.broadcast(msg);
            }
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_B) {
                    success();
                } else {
                    failure();
                }
            }
        };
        tester.doTest(1000);
    }
    private static class Tests2and3 extends HandlerTester {
        Tests2and3(int n) {
            N = n;
        }
        int N;
        Handler mHandlers[];
        boolean mSuccess[];
        public void go() {
            Broadcaster b = new Broadcaster();
            mHandlers = new Handler[N];
            mSuccess = new boolean[N];
            for (int i = 0; i < N; i++) {
                mHandlers[i] = new H();
                mSuccess[i] = false;
                b.request(MESSAGE_A, mHandlers[i], MESSAGE_B + i);
            }
            Message msg = new Message();
            msg.what = MESSAGE_A;
            b.broadcast(msg);
        }
        public void handleMessage(Message msg) {
            int index = msg.what - MESSAGE_B;
            if (index < 0 || index >= N) {
                failure();
            } else {
                if (msg.getTarget() == mHandlers[index]) {
                    mSuccess[index] = true;
                }
            }
            boolean winner = true;
            for (int i = 0; i < N; i++) {
                if (!mSuccess[i]) {
                    winner = false;
                }
            }
            if (winner) {
                success();
            }
        }
    }
    @MediumTest
    public void test2() throws Exception {
        HandlerTester tester = new Tests2and3(2);
        tester.doTest(1000);
    }
    @MediumTest
    public void test3() throws Exception {
        HandlerTester tester = new Tests2and3(10);
        tester.doTest(1000);
    }
    @MediumTest
    public void test4() throws Exception {
        HandlerTester tester = new HandlerTester() {
            Handler h1;
            Handler h2;
            public void go() {
                Broadcaster b = new Broadcaster();
                h1 = new H();
                h2 = new H();
                b.request(MESSAGE_A, h1, MESSAGE_C);
                b.request(MESSAGE_B, h2, MESSAGE_D);
                Message msg = new Message();
                msg.what = MESSAGE_A;
                b.broadcast(msg);
            }
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_C && msg.getTarget() == h1) {
                    success();
                } else {
                    failure();
                }
            }
        };
        tester.doTest(1000);
    }
    @MediumTest
    public void test5() throws Exception {
        HandlerTester tester = new HandlerTester() {
            Handler h1;
            Handler h2;
            public void go() {
                Broadcaster b = new Broadcaster();
                h1 = new H();
                h2 = new H();
                b.request(MESSAGE_A, h1, MESSAGE_C);
                b.request(MESSAGE_B, h2, MESSAGE_D);
                Message msg = new Message();
                msg.what = MESSAGE_B;
                b.broadcast(msg);
            }
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_D && msg.getTarget() == h2) {
                    success();
                } else {
                    failure();
                }
            }
        };
        tester.doTest(1000);
    }
    @MediumTest
    public void test6() throws Exception {
        HandlerTester tester = new HandlerTester() {
            Handler h1;
            Handler h2;
            public void go() {
                Broadcaster b = new Broadcaster();
                h1 = new H();
                h2 = new H();
                b.request(MESSAGE_A, h1, MESSAGE_C);
                b.request(MESSAGE_A, h2, MESSAGE_D);
                b.cancelRequest(MESSAGE_A, h2, MESSAGE_D);
                Message msg = new Message();
                msg.what = MESSAGE_A;
                b.broadcast(msg);
            }
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_C && msg.getTarget() == h1) {
                    success();
                } else {
                    failure();
                }
            }
        };
        tester.doTest(1000);
    }
}
