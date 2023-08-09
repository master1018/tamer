public class TestLoadFromXML {
    public static void main(String[] argv) throws Exception {
        String registrationDir = System.getProperty("test.classes");
        String servicetagDir = System.getProperty("test.src");
        File inFile = new File(servicetagDir, "registration.xml");
        File outFile = new File(registrationDir, "out.xml");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(inFile));
        RegistrationData regData = RegistrationData.loadFromXML(in);
        boolean closed = false;
        try {
           in.read();
        } catch (IOException e) {
           closed = true;
           System.out.println("*** Expected IOException ***");
           e.printStackTrace();
        }
        if (!closed) {
           throw new RuntimeException("InputStream not closed after " +
               "RegistrationData.loadFromXML() call");
        }
        BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(outFile));
        regData.storeToXML(out);
        out.write(0);
    }
}
