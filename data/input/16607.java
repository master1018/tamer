public class TestFrameSize {
    static Dimension desiredDimensions = new Dimension(200, 200);
    static Frame mainWindow;
    private static Dimension getClientSize(Frame window) {
        Dimension size = window.getSize();
        Insets insets = window.getInsets();
        System.out.println("getClientSize() for " + window);
        System.out.println("   size: " + size);
        System.out.println("   insets: " + insets);
        return new Dimension(
                size.width - insets.left - insets.right,
                size.height - insets.top - insets.bottom);
    }
    public static void drawGui() {
        mainWindow = new Frame("");
        mainWindow.setPreferredSize(desiredDimensions);
        mainWindow.pack();
        Dimension actualDimensions = mainWindow.getSize();
        System.out.println("Desired dimensions: " + desiredDimensions.toString());
        System.out.println("Actual dimensions:  " + actualDimensions.toString());
        if (!actualDimensions.equals(desiredDimensions)) {
            throw new RuntimeException("Incorrect widow size");
        }
        Dimension clientSize1 = getClientSize(mainWindow);
        System.out.println("Client size before showing: " + clientSize1);
        mainWindow.setVisible(true);
        ((sun.awt.SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Dimension clientSize2 = getClientSize(mainWindow);
        System.out.println("Client size after showing: " + clientSize2);
        if (!clientSize2.equals(clientSize1)) {
            throw new RuntimeException("Incorrect client area size.");
        }
    }
    public static void main(String[] args) {
        try {
            drawGui();
        } finally {
            if (mainWindow != null) {
                mainWindow.dispose();
            }
        }
    }
}
