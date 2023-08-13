public class GetMessage {
    private static volatile boolean failed = false;
    public static void main(String[] args) throws Exception {
        Throwable[] causes = {
                new Throwable(),
                new Throwable("message"),
                new Throwable("message", new Throwable()) };
        for (Throwable cause: causes) {
            CertPathValidatorException cpve =
                new CertPathValidatorException(cause);
            String expMsg = (cause == null ? null : cause.toString());
            String actualMsg = cpve.getMessage();
            boolean msgsEqual =
                (expMsg == null ? actualMsg == null : expMsg.equals(actualMsg));
            if (!msgsEqual) {
                System.out.println("expected message:" + expMsg);
                System.out.println("getMessage():" + actualMsg);
                failed = true;
            }
        }
        if (failed) {
            throw new Exception("Some tests FAILED");
        }
    }
}
