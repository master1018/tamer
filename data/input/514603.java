public class Throw {
    public void throwNullPointerException() {
        throw new NullPointerException("npe!");
    }
    public void throwArithmeticException() {
        throw new ArithmeticException();
    }
    public void one() {
        System.out.println("Throw.one");
        try {
            throwNullPointerException();
            assert(false);
        } catch (Exception ex) {
            return;
        }
        assert(false);
    }
    public void twoA() {
        System.out.println("Throw.twoA");
        boolean gotN = false;
        boolean gotA = false;
        boolean gotWeird = false;
        try {
            try {
                throwArithmeticException();
                gotWeird = true;
            } catch (ArithmeticException ae) {
                gotA = true;
            }
        } catch (NullPointerException npe) {
            gotN = true;
        }
        assert(gotA);
        assert(!gotN);
        assert(!gotWeird);
    }
    public void twoN() {
        System.out.println("Throw.twoN");
        boolean gotN = false;
        boolean gotA = false;
        boolean gotWeird = false;
        try {
            try {
                throwNullPointerException();
                gotWeird = true;
            } catch (ArithmeticException ae) {
                gotA = true;
            }
        } catch (NullPointerException npe) {
            gotN = true;
        }
        assert(!gotA);
        assert(gotN);
        assert(!gotWeird);
    }
    public void rethrow() {
        System.out.println("Throw.rethrow");
        boolean caught = false;
        boolean lly = false;
        boolean second = false;
        try {
            try {
                throwNullPointerException();
                assert(false);
            } catch (Exception ex) {
                if (ex instanceof ArithmeticException) {
                    assert(false);
                }
                if (ex instanceof NullPointerException) {
                    caught = true;
                    throw (NullPointerException) ex;
                }
            } finally {
                lly = true;
            }
        } catch (Exception ex) {
            second = true;
        }
        assert(caught);
        assert(lly);
        assert(second);
    }
    public static void run() {
        Throw th = new Throw();
        th.one();
        th.twoA();
        th.twoN();
        th.rethrow();
    }
}
