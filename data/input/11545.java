public class RmicDefault {
    public static void main(String args[]) throws Exception {
        String javahome = System.getProperty("java.home");
        String testclasses = System.getProperty("test.classes");
        String userDir = System.getProperty("user.dir");
        if (javahome.regionMatches(true, javahome.length() - 4,
                                   File.separator + "jre", 0, 4))
        {
            javahome = javahome.substring(0, javahome.length() - 4);
        }
        Process javacProcess = Runtime.getRuntime().exec(
            javahome + File.separator + "bin" + File.separator +
            "javac -d " + testclasses + " " +
            System.getProperty("test.src") + File.separator + "packagedir" +
            File.separator + "RmicMeImpl.java");
        StreamPipe.plugTogether(javacProcess.getInputStream(), System.out);
        StreamPipe.plugTogether(javacProcess.getErrorStream(), System.out);
        javacProcess.waitFor();
        Process rmicProcess = Runtime.getRuntime().exec(
            javahome + File.separator + "bin" + File.separator +
            "rmic -classpath " + testclasses + " packagedir.RmicMeImpl");
        StreamPipe.plugTogether(rmicProcess.getInputStream(), System.out);
        StreamPipe.plugTogether(rmicProcess.getErrorStream(), System.err);
        rmicProcess.waitFor();
        File stub = new File(userDir + File.separator + "packagedir" +
                             File.separator + "RmicMeImpl_Stub.class");
        if (!stub.exists()) {
            throw new RuntimeException("TEST FAILED: could not find stub");
        }
        System.err.println("TEST PASSED");
    }
}
