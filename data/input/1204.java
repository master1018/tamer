public class GetPrintJobHeadless {
    public static void main(String[] s) {
        boolean stage1Passed = false;
        boolean stage2Passed = false;
        try {
            Toolkit.getDefaultToolkit().getPrintJob(
                    (Frame) null, "title", new Properties());
        } catch (NullPointerException e) {
            stage1Passed = true;
            e.printStackTrace();
            Sysout.println("Stage 1 passed. getPrintJob(null, String, property) has thrown NPE.");
        }
        if (!stage1Passed) {
            throw new RuntimeException("getPrintJob() should have thrown NPE but didn't.");
        }
        try {
            Toolkit.getDefaultToolkit().getPrintJob(
                    (Frame) null, "title", new JobAttributes(), new PageAttributes());
        } catch (NullPointerException e) {
            stage2Passed = true;
            e.printStackTrace();
            Sysout.println("Stage 2 passed. getPrintJob(null, String, jobAttrs, pageAttr) has thrown  NPE.");
        }
        if (!stage2Passed) {
            throw new RuntimeException("getPrintJob() should have thrown NPE but didn't.");
        }
        Sysout.println("Test PASSED");
    }
}
