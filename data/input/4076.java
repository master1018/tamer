public class DirPermissionDenied {
    public static void main(String[] args) throws Exception {
        URL url = new URL("file:" + args[0]);
        try {
            URLConnection uc = url.openConnection();
            uc.connect();
        } catch (IOException e) {
        } catch (Exception e) {
            throw new RuntimeException("Failed " + e);
        }
        try {
            URLConnection uc = url.openConnection();
            uc.getInputStream();
        } catch (IOException e) {
        } catch (Exception e) {
            throw new RuntimeException("Failed " + e);
        }
        try {
            URLConnection uc = url.openConnection();
            uc.getContentLengthLong();
        } catch (IOException e) {
        } catch (Exception e) {
            throw new RuntimeException("Failed " + e);
        }
    }
}
