public class GetRemoteClass {
    public interface ExtendRemote extends Remote {
    }
    public static class TestRemote implements ExtendRemote {
    }
    public static class OnlySerializable implements Serializable {
    }
    public static void main(String[] argv) {
        System.err.println("\nregression test for 4152295\n");
        Method utilGetRemoteClass = null;
        try {
            Class[] args = new Class[1];
            args[0] = Class.class;
            utilGetRemoteClass =
                Util.class.getDeclaredMethod("getRemoteClass", args);
            utilGetRemoteClass.setAccessible(true);
            utilGetRemoteClass.invoke
                 (null , new Object [] {TestRemote.class});
            System.err.println("remote class flagged as remote");
            ClassNotFoundException cnfe = null;
            try {
                utilGetRemoteClass.invoke
                    (null , new Object [] {OnlySerializable.class});
            } catch (InvocationTargetException e) {
                System.err.println("got ClassNotFoundException; remote " +
                                   "class flagged as nonremote");
                cnfe = (ClassNotFoundException) e.getTargetException();
            }
            if (cnfe == null) {
                TestLibrary.bomb("Serializable class flagged as remote?");
            }
            System.err.println("Test passed.");
        } catch (Exception e) {
            TestLibrary.bomb("Unexpected exception", e);
        }
    }
}
