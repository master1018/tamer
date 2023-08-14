public class TestTrailingEscapesISO2022JP {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        InputStream in =
            new FileInputStream(new File(System.getProperty("test.src", "."),
                                        "ISO2022JP.trailEsc"));
        try {
            byte[] b = new byte[4096];
            while ( ( len = in.read( b, 0, b.length ) ) != -1 ) {
                out.write(b, 0, len);
            }
        } finally {
            in.close();
        }
        Reader inR = new InputStreamReader(new ByteArrayInputStream(
                                                        out.toByteArray()),
                                                       "iso-2022-jp");
        try {
            char[] c = new char[4096];
            while ( ( len = inR.read( c, 0, c.length ) ) != -1 ) {
                System.out.println(len);
                if (len == 0)
                    throw new Exception("Read returned zero!");
            }
        } finally {
            inR.close();
        }
    }
}
