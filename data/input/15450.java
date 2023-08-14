public class bug6875153 {
    private static void createGui() {
        JLayer layer = new JLayer();
        layer.setGlassPane(null);
        layer.isOptimizedDrawingEnabled();
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                bug6875153.createGui();
            }
        });
    }
}
