public class EnvironmentHelper {
    private EnvironmentHelper() {
    }
    public static InputStream PropertiesToInputStream(Properties p) {
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            p.store(bos, "");
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bos.close();
            } catch (Exception ex) {
            }
        }
    }
}
