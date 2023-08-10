public class Md5Util {
    private static MessageDigest algorithm = null;
    public static String getMd5(byte[] input) throws NoSuchAlgorithmException {
        if (algorithm == null) {
            algorithm = MessageDigest.getInstance("MD5");
        }
        algorithm.reset();
        algorithm.update(input);
        byte messageDigest[] = algorithm.digest();
        BigInteger number = new BigInteger(1, messageDigest);
        String md5val = number.toString(16);
        int prefixZeros = 32 - md5val.length();
        for (int i = 0; i < prefixZeros; ++i) {
            md5val = "0" + md5val;
        }
        return md5val.toUpperCase();
    }
    public static String getMd5(String input) throws NoSuchAlgorithmException {
        return getMd5(input.getBytes());
    }
    public static String getMd5FromFile(String fileName) throws FileNotFoundException, NoSuchAlgorithmException {
        try {
            InputStream in = new FileInputStream(new File(fileName));
            byte[] data = new byte[in.available()];
            in.read(data);
            return getMd5(data);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                System.out.println(Md5Util.getMd5FromFile(args[0]));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }
}
