public class TestPRF extends Utils {
    private static int PREFIX_LENGTH = "prf-output: ".length();
    public static void main(String[] args) throws Exception {
        Provider provider = Security.getProvider("SunJCE");
        InputStream in = new FileInputStream(new File(BASE, "prfdata.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        int n = 0;
        int lineNumber = 0;
        byte[] secret = null;
        String label = null;
        byte[] seed = null;
        int length = 0;
        byte[] output = null;
        while (true) {
            String line = reader.readLine();
            lineNumber++;
            if (line == null) {
                break;
            }
            if (line.startsWith("prf-") == false) {
                continue;
            }
            String data = line.substring(PREFIX_LENGTH);
            if (line.startsWith("prf-secret:")) {
                secret = parse(data);
            } else if (line.startsWith("prf-label:")) {
                label = data;
            } else if (line.startsWith("prf-seed:")) {
                seed = parse(data);
            } else if (line.startsWith("prf-length:")) {
                length = Integer.parseInt(data);
            } else if (line.startsWith("prf-output:")) {
                output = parse(data);
                System.out.print(".");
                n++;
                KeyGenerator kg =
                    KeyGenerator.getInstance("SunTlsPrf", provider);
                SecretKey inKey;
                if (secret == null) {
                    inKey = null;
                } else {
                    inKey = new SecretKeySpec(secret, "Generic");
                }
                TlsPrfParameterSpec spec =
                    new TlsPrfParameterSpec(inKey, label, seed, length,
                        null, -1, -1);
                kg.init(spec);
                SecretKey key = kg.generateKey();
                byte[] enc = key.getEncoded();
                if (Arrays.equals(output, enc) == false) {
                    throw new Exception("mismatch line: " + lineNumber);
                }
            } else {
                throw new Exception("Unknown line: " + line);
            }
        }
        if (n == 0) {
            throw new Exception("no tests");
        }
        in.close();
        System.out.println();
        System.out.println("OK: " + n + " tests");
    }
}
