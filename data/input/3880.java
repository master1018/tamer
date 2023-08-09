public class bug6840086 {
    private static final String[] KEYS = {
        "fileChooserIcon ListView",
        "fileChooserIcon ViewMenu",
        "fileChooserIcon DetailsView",
        "fileChooserIcon UpFolder",
        "fileChooserIcon NewFolder",
    };
    public static void main(String[] args) {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS) {
            System.out.println("The test was skipped because it is sensible only for Windows.");
            return;
        }
        for (String key : KEYS) {
            Image image = (Image) ShellFolder.get(key);
            if (image == null) {
                throw new RuntimeException("The image '" + key + "' not found.");
            }
            if (image != ShellFolder.get(key)) {
                throw new RuntimeException("The image '" + key + "' is not cached.");
            }
        }
        System.out.println("The test passed.");
    }
}
