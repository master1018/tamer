public class Test6857159 extends Thread {
    static class ct0 extends Test6857159 {
        public void message() {
        }
        public void run() {
             message();
             ct0 ct = (ct0) Thread.currentThread();
             ct.message();
        }
    }
    static class ct1 extends ct0 {
        public void message() {
        }
    }
    static class ct2 extends ct0 {
        public void message() {
        }
    }
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 20000; i++) {
            Thread t = null;
            switch (i % 3) {
              case 0: t = new ct0(); break;
              case 1: t = new ct1(); break;
              case 2: t = new ct2(); break;
            }
            t.start();
            t.join();
        }
    }
}
