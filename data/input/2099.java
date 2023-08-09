public class MediaMappingsTest {
    public static void main(String args[]) {
        MediaSize sizeA = MediaSize.getMediaSizeForName(MediaSizeName.A);
        new MediaSize(1.0f, 2.0f, MediaSize.MM, MediaSizeName.A);
        if (!sizeA.equals(MediaSize.getMediaSizeForName(MediaSizeName.A))) {
             throw new RuntimeException("mapping changed");
        }
        MediaSize sizeB = MediaSize.getMediaSizeForName(MediaSizeName.B);
        new MediaSize(1, 2, MediaSize.MM, MediaSizeName.B);
        if (!sizeB.equals(MediaSize.getMediaSizeForName(MediaSizeName.B))) {
             throw new RuntimeException("mapping changed");
        }
    }
}
