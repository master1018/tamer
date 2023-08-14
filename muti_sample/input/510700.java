public class IntelliJ {
    private static final String IDEA_IML = "android.iml";
    private static final String IDEA_IPR = "android.ipr";
    public static void generateFrom(Configuration c) throws IOException {
        File templatesDirectory = new File(c.toolDirectory, "templates");
        String ipr = Files.toString(new File(templatesDirectory, IDEA_IPR));
        Files.toFile(ipr, new File(IDEA_IPR));
        String iml = Files.toString(new File(templatesDirectory, IDEA_IML));
        StringBuilder sourceRootsXml = new StringBuilder();
        for (File sourceRoot : c.sourceRoots) {
            sourceRootsXml.append("<sourceFolder url=\"file:
                .append(sourceRoot.getPath())
                .append("\" isTestSource=\"").append(isTests(sourceRoot))
                .append("\"/>\n");
        }
        StringBuilder excludeXml = new StringBuilder();
        for (File excludedDir : c.excludesUnderSourceRoots()) {
            sourceRootsXml.append("<excludeFolder url=\"file:
                .append(excludedDir.getPath())
                .append("\"/>\n");
        }
        sourceRootsXml.append("<excludeFolder "
                + "url=\"file:
        StringBuilder jarsXml = new StringBuilder();
        for (File jar : c.jarFiles) {
            jarsXml.append("<orderEntry type=\"module-library\">"
                    + "<library><CLASSES><root url=\"jar:
                .append(jar.getPath())
            .append("!/\"/></CLASSES><JAVADOC/><SOURCES/></library>"
                    + "</orderEntry>\n");
        }
        iml = iml.replace("SOURCE_FOLDERS",
                sourceRootsXml.toString() + excludeXml.toString());
        iml = iml.replace("JAR_ENTRIES", jarsXml.toString());
        Files.toFile(iml, new File(IDEA_IML));
    }
    private static boolean isTests(File file) {
        String path = file.getPath();
        if (path.contains("test-runner")) {
            return false;
        }
        return path.toUpperCase().contains("TEST");
    }
}