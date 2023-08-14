public class Support_Exec extends TestCase {
    private static final boolean againstDalvik
            = System.getProperty("java.vendor").contains("Android");
    public static ProcessBuilder javaProcessBuilder()
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command().add(againstDalvik ? "dalvikvm" : "java");
        String testVMArgs = System.getProperty("hy.test.vmargs");
        if (testVMArgs != null) {
            builder.command().addAll(Arrays.asList(testVMArgs.split("\\s+")));
        }
        return builder;
    }
    public static String createPath(String... elements) {
        StringBuilder result = new StringBuilder();
        for (String element : elements) {
            result.append(File.pathSeparator);
            result.append(element);
        }
        return result.toString();
    }
    public static String execAndGetOutput(ProcessBuilder builder) throws IOException {
        Process process = builder.start();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<String> errFuture = executorService.submit(
                    streamToStringCallable(process.getErrorStream()));
            Future<String> outFuture = executorService.submit(
                    streamToStringCallable(process.getInputStream()));
            Throwable failure;
            String out = "";
            try {
                out = outFuture.get(10, TimeUnit.SECONDS);
                String err = errFuture.get(10, TimeUnit.SECONDS);
                failure = err.length() > 0
                        ? new AssertionFailedError("Unexpected err stream data:\n" + err)
                        : null;
            } catch (Exception e) {
                failure = e;
            }
            if (failure != null) {
                AssertionFailedError error = new AssertionFailedError(
                        "Failed to execute " + builder.command() + "; output was:\n" + out);
                error.initCause(failure);
                throw error;
            } else {
                return out;
            }
        } finally {
            executorService.shutdown();
        }
    }
    public static void execAndCheckOutput(ProcessBuilder builder,
            String expectedOut, String expectedErr) throws Exception {
        Process process = builder.start();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<String> errFuture =
                    executorService.submit(streamToStringCallable(process.getErrorStream()));
            Future<String> outFuture =
                    executorService.submit(streamToStringCallable(process.getInputStream()));
            assertEquals(expectedOut, outFuture.get(10, TimeUnit.SECONDS));
            assertEquals(expectedErr, errFuture.get(10, TimeUnit.SECONDS));
        } finally {
            executorService.shutdown();
            process.waitFor();
        }
    }
    private static Callable<String> streamToStringCallable(final InputStream in) {
        return new Callable<String>() {
            public String call() throws Exception {
                StringWriter writer = new StringWriter();
                Reader reader = new InputStreamReader(in);
                int c;
                while ((c = reader.read()) != -1) {
                    writer.write(c);
                }
                return writer.toString();
            }
        };
    }
}
