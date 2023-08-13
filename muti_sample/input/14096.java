public class MLetContentTest {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> General test for the public class MLetContent.");
        Map<String,String> attributes = new HashMap();
        attributes.put("archive", archive);
        attributes.put("Archive", "hahaha");
        attributes.put("code", code);
        attributes.put("codE", "hihi");
        attributes.put("object", object);
        attributes.put("obJect", "toto");
        attributes.put("name", name);
        attributes.put("NAME", "titi");
        attributes.put("version", version);
        attributes.put("VeRsIoN", "tttt");
        List<String> types = new ArrayList();
        types.add("my type");
        List<String> values = new ArrayList();
        values.add("my values");
        URL url = new URL(baseUrl+myfile);
        MLetContent content = new MLetContent(url, attributes, types, values);
        if (!attributes.equals(content.getAttributes())) {
            throw new RuntimeException("The user specific attributes are changed.");
        }
        if (!url.equals(content.getDocumentBase())) {
            throw new RuntimeException("The user specific document bas is changed.");
        }
        if (!archive.equals(content.getJarFiles())) {
            throw new RuntimeException("The user specific archive files are changed.");
        }
        if (!code.equals(content.getCode())) {
            throw new RuntimeException("The user specific code is changed.");
        }
        if (!object.equals(content.getSerializedObject())) {
            throw new RuntimeException("The user specific object is changed.");
        }
        if (!name.equals(content.getName())) {
            throw new RuntimeException("The user specific name is changed.");
        }
        if (!version.equals(content.getVersion())) {
            throw new RuntimeException("The user specific version is changed.");
        }
        if (!types.equals(content.getParameterTypes())) {
            throw new RuntimeException("The user specific types are changed.");
        }
        if (!values.equals(content.getParameterValues())) {
            throw new RuntimeException("The user specific values are changed.");
        }
        if (!baseUrl.equals(content.getCodeBase().toString())) {
            throw new RuntimeException("The user specific base url are changed.");
        }
        url = new URL(baseUrl);
        attributes.put("codebase", codebase);
        content = new MLetContent(url, attributes, types, values);
        if (!content.getCodeBase().toString().equals(baseUrl+codebase)) {
            throw new RuntimeException("The user specific base url are changed.");
        }
        final MyMLet myMlet = new MyMLet();
        if (myMlet.check(null, null, null, content) != content.getCodeBase()) {
            throw new RuntimeException("Failed to overrid the protected methed check");
        }
        System.out.println(">>> The test is well passed.");
    }
    private static class MyMLet extends MLet {
        public URL check(String version,
                         URL codebase,
                         String jarfile,
                         MLetContent content) {
            return content.getCodeBase();
        }
    }
    private static final String archive = "my jarfile";
    private static final String code = "my code";
    private static final String object = "my object";
    private static final String name = "my name";
    private static final String version = "my version";
    private static final String myfile = "My file";
    private static final String baseUrl = "file:/tmp/test/";
    private final static String codebase = "my code base/";
}
