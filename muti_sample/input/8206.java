class SunSDK {
    static String home() {
        File jreHome = new File(System.getProperty("java.home"));
        File jreParent = new File(jreHome.getParent());
        String jdwpLibName = "bin" + File.separator +
                             System.mapLibraryName("jdwp");
        File jdwpLib = new File(jreParent, jdwpLibName);
        return jdwpLib.exists() ? jreParent.getAbsolutePath() : null;
    }
}
