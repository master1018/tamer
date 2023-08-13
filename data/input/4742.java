public class PreserveDispatchThread {
    private static volatile Frame f;
    private static volatile Dialog d;
    private static volatile boolean isEDT = true;
    public static void main(String[] args) throws Exception {
        f = new Frame("F");
        f.setSize(320, 340);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        try {
            test1();
            if (!isEDT) {
                throw new RuntimeException("Test FAILED (test1): event dispatch thread is changed");
            }
            test2();
            if (!isEDT) {
                throw new RuntimeException("Test FAILED (test2): event dispatch thread is changed");
            }
            test3();
            if (!isEDT) {
                throw new RuntimeException("Test FAILED (test3): event dispatch thread is changed");
            }
        } finally {
            if (d != null) {
                d.dispose();
            }
            f.dispose();
        }
    }
    private static void test1() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                TestEventQueue teq = new TestEventQueue();
                EventQueue seq = Toolkit.getDefaultToolkit().getSystemEventQueue();
                try {
                    seq.push(teq);
                    d = new TestDialog();
                    d.setVisible(true);
                    checkEDT();
                } finally {
                    teq.pop();
                }
                checkEDT();
            }
        });
    }
    private static void test2() throws Exception {
        TestEventQueue teq = new TestEventQueue();
        EventQueue seq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        try {
            seq.push(teq);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    checkEDT();
                    d = new TestDialog();
                    d.setVisible(true);
                    checkEDT();
                }
            });
        } finally {
            teq.pop();
        }
    }
    private static final Object test3Lock = new Object();
    private static boolean test3Sync = false;
    private static void test3() throws Exception {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                d = new Dialog(f, true);
                d.setSize(240, 180);
                d.setLocationRelativeTo(f);
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        d.setVisible(true);
                        checkEDT();
                    }
                });
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        TestEventQueue teq = new TestEventQueue();
                        EventQueue seq = Toolkit.getDefaultToolkit().getSystemEventQueue();
                        try {
                            seq.push(teq);
                            checkEDT();
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    d.dispose();
                                    checkEDT();
                                    synchronized (test3Lock) {
                                        test3Sync = true;
                                        test3Lock.notify();
                                    }
                                }
                            });
                        } finally {
                            teq.pop();
                        }
                        checkEDT();
                    }
                });
                checkEDT();
            }
        });
        synchronized (test3Lock) {
            while (!test3Sync) {
                try {
                    test3Lock.wait();
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
    private static void checkEDT() {
        isEDT = isEDT && EventQueue.isDispatchThread();
    }
    private static class TestEventQueue extends EventQueue {
        public TestEventQueue() {
            super();
        }
        public void pop() {
            super.pop();
        }
    }
    private static class TestDialog extends Dialog {
        private volatile boolean dialogShown = false;
        private volatile boolean paintCalled = false;
        public TestDialog() {
            super(f, true);
            setSize(240, 180);
            setLocationRelativeTo(f);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    if (paintCalled) {
                        dispose();
                    }
                    dialogShown = true;
                }
            });
        }
        @Override
        public void paint(Graphics g) {
            if (dialogShown) {
                dispose();
            }
            paintCalled = true;
        }
    }
}
