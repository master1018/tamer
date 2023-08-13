public class IdleHandlerTest extends TestCase {
    private static class BaseTestHandler extends TestHandlerThread {
        Handler mHandler;
        public BaseTestHandler() {
        }
        public void go() {
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    BaseTestHandler.this.handleMessage(msg);
                }
            };
        }
        public void addIdleHandler() {
            Looper.myQueue().addIdleHandler(new IdleHandler() {
                public boolean queueIdle() {
                    return BaseTestHandler.this.queueIdle();
                }
            });
        }
        public void handleMessage(Message msg) {
        }
        public boolean queueIdle() {
            return false;
        }
    }
    @MediumTest
    public void testOneShotFirst() throws Exception {
        TestHandlerThread tester = new BaseTestHandler() {
            int mCount;
            public void go() {
                super.go();
                mCount = 0;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(0), 100);
                addIdleHandler();
            }
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 100);
                } else if (msg.what == 1) {
                    if (mCount == 1) {
                        success();
                    } else {
                        failure(new RuntimeException(
                                "Idle handler called " + mCount + " times"));
                    }
                }
            }
            public boolean queueIdle() {
                mCount++;
                return false;
            }
        };
        tester.doTest(1000);
    }
    @MediumTest
    public void testOneShotLater() throws Exception {
        TestHandlerThread tester = new BaseTestHandler() {
            int mCount;
            public void go() {
                super.go();
                mCount = 0;
                mHandler.sendMessage(mHandler.obtainMessage(0));
            }
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    addIdleHandler();
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 100);
                } else if (msg.what == 1) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(2), 100);
                } else if (msg.what == 2) {
                    if (mCount == 1) {
                        success();
                    } else {
                        failure(new RuntimeException(
                                "Idle handler called " + mCount + " times"));
                    }
                }
            }
            public boolean queueIdle() {
                mCount++;
                return false;
            }
        };
        tester.doTest(1000);
    }
    @MediumTest
    public void testRepeatedFirst() throws Exception {
        TestHandlerThread tester = new BaseTestHandler() {
            int mCount;
            public void go() {
                super.go();
                mCount = 0;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(0), 100);
                addIdleHandler();
            }
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 100);
                } else if (msg.what == 1) {
                    if (mCount == 2) {
                        success();
                    } else {
                        failure(new RuntimeException(
                                "Idle handler called " + mCount + " times"));
                    }
                }
            }
            public boolean queueIdle() {
                mCount++;
                return true;
            }
        };
        tester.doTest(1000);
    }
    @MediumTest
    public void testRepeatedLater() throws Exception {
        TestHandlerThread tester = new BaseTestHandler() {
            int mCount;
            public void go() {
                super.go();
                mCount = 0;
                mHandler.sendMessage(mHandler.obtainMessage(0));
            }
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    addIdleHandler();
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 100);
                } else if (msg.what == 1) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(2), 100);
                } else if (msg.what == 2) {
                    if (mCount == 2) {
                        success();
                    } else {
                        failure(new RuntimeException(
                                "Idle handler called " + mCount + " times"));
                    }
                }
            }
            public boolean queueIdle() {
                mCount++;
                return true;
            }
        };
        tester.doTest(1000);
    }
}
