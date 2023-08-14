public class TestMasterSecret extends Utils {
    private static int PREFIX_LENGTH = "m-premaster:  ".length();
    public static void main(String[] args) throws Exception {
        Provider provider = Security.getProvider("SunJCE");
        InputStream in = new FileInputStream(new File(BASE, "masterdata.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        int n = 0;
        int lineNumber = 0;
        String algorithm = null;
        byte[] premaster = null;
        byte[] clientRandom = null;
        byte[] serverRandom = null;
        int protoMajor = 0;
        int protoMinor = 0;
        int preMajor = 0;
        int preMinor = 0;
        byte[] master = null;
        while (true) {
            String line = reader.readLine();
            lineNumber++;
            if (line == null) {
                break;
            }
            if (line.startsWith("m-") == false) {
                continue;
            }
            String data = line.substring(PREFIX_LENGTH);
            if (line.startsWith("m-algorithm:")) {
                algorithm = data;
            } else if (line.startsWith("m-premaster:")) {
                premaster = parse(data);
            } else if (line.startsWith("m-crandom:")) {
                clientRandom = parse(data);
            } else if (line.startsWith("m-srandom:")) {
                serverRandom = parse(data);
            } else if (line.startsWith("m-protomajor:")) {
                protoMajor = Integer.parseInt(data);
            } else if (line.startsWith("m-protominor:")) {
                protoMinor = Integer.parseInt(data);
            } else if (line.startsWith("m-premajor:")) {
                preMajor = Integer.parseInt(data);
            } else if (line.startsWith("m-preminor:")) {
                preMinor = Integer.parseInt(data);
            } else if (line.startsWith("m-master:")) {
                master = parse(data);
                System.out.print(".");
                n++;
                KeyGenerator kg =
                    KeyGenerator.getInstance("SunTlsMasterSecret", provider);
                SecretKey premasterKey =
                    new SecretKeySpec(premaster, algorithm);
                TlsMasterSecretParameterSpec spec =
                    new TlsMasterSecretParameterSpec(premasterKey, protoMajor,
                        protoMinor, clientRandom, serverRandom,
                        null, -1, -1);
                kg.init(spec);
                TlsMasterSecret key = (TlsMasterSecret)kg.generateKey();
                byte[] enc = key.getEncoded();
                if (Arrays.equals(master, enc) == false) {
                    throw new Exception("mismatch line: " + lineNumber);
                }
                if ((preMajor != key.getMajorVersion()) ||
                        (preMinor != key.getMinorVersion())) {
                    throw new Exception("version mismatch line: " + lineNumber);
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
