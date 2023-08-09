class Util {
    private static final String TAG = "certinstaller.Util";
    static byte[] toBytes(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject(object);
            os.close();
        } catch (Exception e) {
            Log.w(TAG, "toBytes(): " + e + ": " + object);
        } finally {
            return baos.toByteArray();
        }
    }
    static <T> T fromBytes(byte[] bytes) {
        if (bytes == null) return null;
        try {
            ObjectInputStream is =
                    new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) is.readObject();
        } catch (Exception e) {
            Log.w(TAG, "fromBytes(): " + e);
            return null;
        }
    }
    static String toMd5(byte[] bytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            return toHexString(algorithm.digest(), "");
        } catch(NoSuchAlgorithmException e){
            Log.w(TAG, "toMd5(): " + e);
            throw new RuntimeException(e);
        }
    }
    private static String toHexString(byte[] bytes, String separator) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(Integer.toHexString(0xFF & b)).append(separator);
        }
        return hexString.toString();
    }
    static byte[] readFile(File file) {
        try {
            byte[] data = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(data);
            fis.close();
            return data;
        } catch (Exception e) {
            Log.w(TAG, "cert file read error: " + e);
            return null;
        }
    }
    static boolean deleteFile(File file) {
        if ((file != null) && !file.delete()) {
            Log.w(TAG, "cannot delete cert: " + file);
            return false;
        } else {
            return true;
        }
    }
    private Util() {
    }
}
