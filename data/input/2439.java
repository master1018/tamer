public class test {
    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
            }
        } else {
        }
    }
}
