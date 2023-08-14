public class TestPlan extends XMLResourceHandler{
    public static final String EXCLUDE_SEPARATOR = ";";
    public interface Tag {
        public static final String TEST_SUITE = "TestSuite";
        public static final String ENTRY = "Entry";
        public static final String TEST_PLAN = "TestPlan";
        public static final String PLAN_SETTING = "PlanSettings";
        public static final String REQUIRED_DEVICE = "RequiredDevice";
        public static final String TEST_CASE = "TestCase";
    }
    public interface Attribute {
        public static final String NAME = "name";
        public static final String URI = "uri";
        public static final String EXCLUDE = "exclude";
        public static final String AMOUNT = "amount";
    }
    public static Collection<String> getEntries(String planPath,
            ArrayList<String> removedPkgList)
            throws SAXException, IOException, ParserConfigurationException {
        ArrayList<String> entries = new ArrayList<String>();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        File planFile = new File(planPath);
        Document doc = builder.parse(planFile);
        NodeList pkgEntries = doc.getElementsByTagName(TestPlan.Tag.ENTRY);
        for (int i = 0; i < pkgEntries.getLength(); i++) {
            Node pkgEntry = pkgEntries.item(i);
            String uri = getStringAttributeValue(pkgEntry, TestPlan.Attribute.URI);
            String packageBinaryName = HostConfig.getInstance().getPackageBinaryName(uri);
            if (packageBinaryName != null) {
                entries.add(getStringAttributeValue(pkgEntry, TestPlan.Attribute.URI));
            } else {
                removedPkgList.add(uri);
            }
        }
        return entries;
    }
    public static boolean isValidPackageName(String pkgName) {
        String xmlPath = HostConfig.getInstance().getCaseRepository().getXmlPath(pkgName);
        String apkPath = HostConfig.getInstance().getCaseRepository().getApkPath(pkgName);
        File xmlFile = new File(xmlPath);
        File apkFile = new File(apkPath);
        if (xmlFile.exists() && apkFile.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
