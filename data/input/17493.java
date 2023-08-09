public class NotCompliantCauseTest {
    private static final Logger LOG =
            Logger.getLogger(NotCompliantCauseTest.class.getName());
    public NotCompliantCauseTest() {
    }
    public static void main(String[] args) {
        NotCompliantCauseTest instance = new NotCompliantCauseTest();
        instance.test1();
    }
    public static class RuntimeTestException extends RuntimeException {
        public RuntimeTestException(String msg) {
            super(msg);
        }
        public RuntimeTestException(String msg, Throwable cause) {
            super(msg,cause);
        }
        public RuntimeTestException(Throwable cause) {
            super(cause);
        }
    }
    void test1() {
        try {
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            ObjectName oname = new ObjectName("domain:type=test");
            mbs.createMBean(NotCompliant.class.getName(), oname);
            System.err.println("ERROR: expected " +
                    "NotCompliantMBeanException not thrown");
            throw new RuntimeTestException("NotCompliantMBeanException not thrown");
        } catch (RuntimeTestException e) {
            throw e;
        } catch (NotCompliantMBeanException e) {
            Throwable cause = e.getCause();
            if (cause == null)
                throw new RuntimeTestException("NotCompliantMBeanException " +
                        "doesn't have any cause.", e);
            while (cause.getCause() != null) {
                if (cause instanceof OpenDataException) break;
                cause = cause.getCause();
            }
            if (! (cause instanceof OpenDataException))
                throw new RuntimeTestException("NotCompliantMBeanException " +
                        "doesn't have expected cause ("+
                        OpenDataException.class.getName()+"): "+cause, e);
            System.err.println("SUCCESS: Found expected cause: " + cause);
        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e);
            throw new RuntimeException("Unexpected exception: " + e,e);
        }
    }
    public interface NotCompliantMXBean {
        Random returnRandom();
    }
    public static class NotCompliant implements NotCompliantMXBean {
        public Random returnRandom() {
            return new Random();
        }
    }
}
