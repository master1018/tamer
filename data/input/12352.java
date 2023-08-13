public class TestOAEP_KAT {
    private final static String BASE = System.getProperty("test.src", ".");
    private static BigInteger n, e, d, p, q, pe, qe, coeff;
    private static byte[] plainText, seed, cipherText, cipherText2;
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Provider provider = Security.getProvider("SunJCE");
        Provider kfProvider = Security.getProvider("SunRsaSign");
        System.out.println("Testing provider " + provider.getName() + "...");
        Cipher c = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding", provider);
        KeyFactory kf = KeyFactory.getInstance("RSA", kfProvider);
        InputStream in = new FileInputStream(new File(BASE, "oaep-vect.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF8"));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.equals("# RSA modulus n:")) {
                n = parseNumber(reader);
            } else if (line.equals("# RSA public exponent e:")) {
                e = parseNumber(reader);
            } else if (line.equals("# RSA private exponent d:")) {
                d = parseNumber(reader);
            } else if (line.equals("# Prime p:")) {
                p = parseNumber(reader);
            } else if (line.equals("# Prime q:")) {
                q = parseNumber(reader);
            } else if (line.equals("# p's CRT exponent dP:")) {
                pe = parseNumber(reader);
            } else if (line.equals("# q's CRT exponent dQ:")) {
                qe = parseNumber(reader);
            } else if (line.equals("# CRT coefficient qInv:")) {
                coeff = parseNumber(reader);
            } else if (line.equals("# Message to be encrypted:")) {
                plainText = parseBytes(reader);
            } else if (line.equals("# Seed:")) {
                seed = parseBytes(reader);
            } else if (line.equals("# Encryption:")) {
                cipherText = parseBytes(reader);
                KeySpec pubSpec = new RSAPublicKeySpec(n, e);
                PublicKey pubKey = kf.generatePublic(pubSpec);
                c.init(Cipher.ENCRYPT_MODE, pubKey, new MyRandom(seed));
                cipherText2 = c.doFinal(plainText);
                if (Arrays.equals(cipherText2, cipherText) == false) {
                    throw new Exception("Encryption mismatch");
                }
                KeySpec privSpec = new RSAPrivateCrtKeySpec(n, e, d, p, q, pe, qe, coeff);
                PrivateKey privKey = kf.generatePrivate(privSpec);
                c.init(Cipher.DECRYPT_MODE, privKey);
                byte[] dec = c.doFinal(cipherText);
                if (Arrays.equals(plainText, dec) == false) {
                    throw new Exception("Decryption mismatch");
                }
            } else if (line.startsWith("# ------------------------------")) {
            } else {
                System.out.println(": " + line);
            }
        }
        long stop = System.currentTimeMillis();
        System.out.println("Done (" + (stop - start) + " ms).");
    }
    private static BigInteger parseNumber(BufferedReader reader) throws IOException {
        return new BigInteger(1, parseBytes(reader));
    }
    private static byte[] parseBytes(BufferedReader reader) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                throw new EOFException("Unexpected EOF");
            }
            line = line.trim();
            if (line.length() == 0) {
                break;
            }
            buffer.write(parse(line));
        }
        return buffer.toByteArray();
    }
    public static byte[] parse(String s) {
        try {
            int n = s.length();
            ByteArrayOutputStream out = new ByteArrayOutputStream(n / 3);
            StringReader r = new StringReader(s);
            while (true) {
                int b1 = nextNibble(r);
                if (b1 < 0) {
                    break;
                }
                int b2 = nextNibble(r);
                if (b2 < 0) {
                    throw new RuntimeException("Invalid string " + s);
                }
                int b = (b1 << 4) | b2;
                out.write(b);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static byte[] b(String s) {
        return parse(s);
    }
    private static int nextNibble(StringReader r) throws IOException {
        while (true) {
            int ch = r.read();
            if (ch == -1) {
                return -1;
            } else if ((ch >= '0') && (ch <= '9')) {
                return ch - '0';
            } else if ((ch >= 'a') && (ch <= 'f')) {
                return ch - 'a' + 10;
            } else if ((ch >= 'A') && (ch <= 'F')) {
                return ch - 'A' + 10;
            }
        }
    }
}
class MyRandom extends SecureRandom {
    private byte[] source;
    private int count;
    MyRandom(byte[] source) {
        this.source = (byte[]) source.clone();
        count = 0;
    }
    public void nextBytes(byte[] bytes) {
        if (bytes.length > source.length - count) {
            throw new RuntimeException("Insufficient random data");
        }
        System.arraycopy(source, count, bytes, 0, bytes.length);
        count += bytes.length;
    }
}
