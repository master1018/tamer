public class GetXmlContentType {
    static final String XML_MIME_TYPE = "application/xml";
    static final String XML_HEADER = "<?xml";
    static int passed, failed;
    static final String goodFiles [] = {
        "xml1",         
        "xml2.xml",     
        "xml3",         
        "xml4"          
        };
    static final String badFiles [] = {
        "not-xml1",
        "not-xml2"
        };
    public static void main(String[] args) throws Exception {
        contentTypeFromFile();
        contentTypeFromBOMStream();
        if (failed > 0)
            throw new RuntimeException (
                "Test failed; passed = " + passed + ", failed = " + failed);
    }
    static void contentTypeFromFile() throws Exception {
        for (String goodFile : goodFiles) {
            String result = getUrlContentType(goodFile);
            if (!XML_MIME_TYPE.equals(result)) {
                System.out.println("Wrong MIME type: " + goodFile + " --> " + result);
                failed++;
            } else {
                passed++;
            }
        }
        for (String badFile : badFiles) {
            String result = getUrlContentType(badFile);
            if (XML_MIME_TYPE.equals(result)) {
                System.out.println("Wrong MIME type: " + badFile + " --> " + result);
                failed++;
            } else {
                passed++;
            }
        }
    }
    static String getUrlContentType(String name) throws IOException {
        File file = new File(System.getProperty("test.src", "."), "xml");
        URL u = new URL("file:"
                         + file.getCanonicalPath()
                         + file.separator
                         + name);
        URLConnection conn = u.openConnection();
        return conn.getContentType();
    }
    static void contentTypeFromBOMStream() throws Exception {
        final String[] encodings = new  String[]
                {"UTF-8", "UTF-16BE", "UTF-16LE", "UTF-32BE", "UTF-32LE"};
        for (String encoding : encodings) {
             try (InputStream is = new ByteArrayInputStream(toBOMBytes(encoding))) {
                 String mime = URLConnection.guessContentTypeFromStream(is);
                 if ( !XML_MIME_TYPE.equals(mime) ) {
                     System.out.println("Wrong MIME type: " + encoding + " --> " + mime);
                     failed++;
                 } else {
                     passed++;
                 }
             }
         }
    }
    static byte[] toBOMBytes(String encoding) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        switch (encoding) {
            case "UTF-8" :
                bos.write(new  byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
                break;
            case "UTF-16BE" :
                bos.write(new  byte[] { (byte) 0xFE, (byte) 0xFF });
                break;
            case "UTF-16LE" :
                bos.write(new  byte[] { (byte) 0xFF, (byte) 0xFE });
                break;
            case "UTF-32BE" :
                bos.write(new  byte[] { (byte) 0x00, (byte) 0x00,
                                        (byte) 0xFE, (byte) 0xFF });
                break;
            case "UTF-32LE" :
                bos.write(new  byte[] { (byte) 0xFF, (byte) 0xFE,
                                        (byte) 0x00, (byte) 0x00 });
        }
        bos.write(XML_HEADER.getBytes(encoding));
        return bos.toByteArray();
    }
}
