public class InvalidRegistrationData {
    public static void main(String[] argv) throws Exception {
        String servicetagDir = System.getProperty("test.src");
        checkRegistrationData(servicetagDir, "missing-environ-field.xml");
        checkRegistrationData(servicetagDir, "newer-registry-version.xml");
    }
    private static void checkRegistrationData(String parent, String filename)
           throws Exception {
        boolean thrown = false;
        File f = new File(parent, filename);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        try {
            RegistrationData regData = RegistrationData.loadFromXML(in);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + " thrown expected");
            thrown = true;
        }
        if (!thrown) {
            throw new RuntimeException("ERROR: No IllegalArgumentException thrown");
        }
    }
}
