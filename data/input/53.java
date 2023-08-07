public class ImageLoader {
    private static String PICS_PATH = Application.getProperty("dir.pics");
    public static ImageData loadImage(String dir, String imagename) {
        String path = System.getProperty("user.dir") + File.separator + PICS_PATH + File.separator + dir + File.separator + imagename;
        ImageData data = null;
        try {
            data = new ImageData(path);
        } catch (Exception e) {
        }
        return data;
    }
}
