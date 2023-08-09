public class ConstructDeflaterInput {
    static class MyDeflater extends Deflater {
        private boolean ended = false;
        boolean getEnded() { return ended; }
        public void end() {
            fail("MyDeflater had end() called");
            super.end();
        }
    }
    public static void realMain(String[] args) throws Throwable {
        ByteArrayInputStream bais = new ByteArrayInputStream(
            "hello, world".getBytes());
        MyDeflater def = new MyDeflater();
        DeflaterInputStream dis = null;
        byte[] b = new byte[512];
        try {
            dis = new DeflaterInputStream(null);
            fail();
        } catch (NullPointerException ex) {
            pass();
        }
        try {
            dis = new DeflaterInputStream(bais, null);
            fail();
        } catch (NullPointerException ex) {
            pass();
        }
        try {
            dis = new DeflaterInputStream(bais, def, 0);
            fail();
        } catch (IllegalArgumentException ex) {
            pass();
        }
        dis = new DeflaterInputStream(bais, def);
        try {
            dis.read(null, 5, 2);
            fail();
        } catch (NullPointerException ex) {
            pass();
        }
        try {
            dis.read(b, -1, 0);
            fail();
        } catch (IndexOutOfBoundsException ex) {
            pass();
        }
        try {
            dis.read(b, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {
            pass();
        }
        try {
            dis.read(b, 0, 600);
            fail();
        } catch (IndexOutOfBoundsException ex) {
            pass();
        }
        int len = 0;
        try {
            len = dis.read(b, 0, 0);
            check(len == 0);
        } catch (IndexOutOfBoundsException ex) {
            fail("Read of length 0 should return 0, but returned " + len);
        }
        try {
            dis.skip(-1);
            fail();
        } catch (IllegalArgumentException ex) {
            pass();
        }
        check(!dis.markSupported());
        check(dis.available() == 1);
        check(!def.getEnded());
        try {
            dis.reset();
            fail();
        } catch (IOException ex) {
            pass();
        }
        dis.close();
        check(!def.getEnded());
        try {
            dis.available();
            fail();
        } catch (IOException ex) {
            pass();
        }
        try {
            int x = dis.read();
            fail();
        } catch (IOException ex) {
            pass();
        }
        try {
            dis.skip(1);
            fail();
        } catch (IOException ex) {
            pass();
        }
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
