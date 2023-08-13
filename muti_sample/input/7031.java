public class RedefineClassWithNativeMethodApp {
    public static void main(String[] args) throws Exception {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
        }
        System.out.println("Creating instance of " +
            RedefineClassWithNativeMethodAgent.clz);
        RedefineClassWithNativeMethodAgent.clz.newInstance();
        System.exit(0);
    }
}
