public class HtmlDeltaExternalizer implements IApiDeltaExternalizer {
    private static final String OVERVIEW_PAGE_NAME = "changes.html";
    private static final String STYLE_SHEET_NAME = "styles.css";
    private static final String DELTA_FOLDER = "changes" + File.separator;
    public void externalize(String location, IApiDelta apiDelta)
            throws IOException {
        if (!location.endsWith(File.separator)) {
            location += File.separator;
        }
        File directory = new File(location);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        copyStyleSheet(location);
        Map<String, String> commonInfos = new HashMap<String, String>();
        commonInfos.put("creation_time", DateFormat.getDateTimeInstance()
                .format(new Date()));
        commonInfos.put("from_desc", apiDelta.getFrom().getName());
        commonInfos.put("to_desc", apiDelta.getTo().getName());
        StringBuilder content = new StringBuilder();
        ApiOverviewPage apiOverviewPage = new ApiOverviewPage(apiDelta,
                commonInfos);
        apiOverviewPage.writeTo(content);
        writeToFile(location + OVERVIEW_PAGE_NAME, content.toString());
        Set<IPackageDelta> changedPackages = SigDelta.getChanged(apiDelta
                .getPackageDeltas());
        if (!changedPackages.isEmpty()) {
            File file = new File(location + DELTA_FOLDER);
            if (!file.exists()) {
                file.mkdir();
            }
            for (IPackageDelta packageDelta : changedPackages) {
                content = new StringBuilder();
                PackageOverviewPage packagePage = new PackageOverviewPage(
                        packageDelta, commonInfos);
                packagePage.writeTo(content);
                IPackage aPackage = getAnElement(packageDelta);
                String packageOverviewFileName = location + DELTA_FOLDER
                        + "pkg_" + aPackage.getName() + ".html";
                writeToFile(packageOverviewFileName, content.toString());
                for (IClassDefinitionDelta classDelta : packageDelta
                        .getClassDeltas()) {
                    content = new StringBuilder();
                    ClassOverviewPage classPage = new ClassOverviewPage(
                            classDelta, commonInfos);
                    classPage.writeTo(content);
                    IClassDefinition aClass = getAnElement(classDelta);
                    String classOverviewFileName = location + DELTA_FOLDER
                            + aPackage.getName() + "." + aClass.getName()
                            + ".html";
                    writeToFile(classOverviewFileName, content.toString());
                }
            }
        }
    }
    private static <T> T getAnElement(IDelta<T> delta) {
        if (delta.getFrom() != null) {
            return delta.getFrom();
        } else {
            return delta.getTo();
        }
    }
    private void copyStyleSheet(String directory) throws IOException {
        StringTemplate template = TemplateStore.getStringTemplate("Styles");
        template.setAttribute("version", Version.VERSION);
        writeToFile(directory + STYLE_SHEET_NAME, template.toString());
    }
    private void writeToFile(String fileName, String content)
            throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.write(content.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
