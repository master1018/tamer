public class HierarchyViewer {
    private static final CharSequence OS_WINDOWS = "Windows";
    private static final CharSequence OS_MACOSX = "Mac OS X";
    private static void initUserInterface() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.brushMetalLook", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "HierarchyViewer");
        final String os = System.getProperty("os.name");
        try {
            if (os.contains(OS_WINDOWS) || os.contains(OS_MACOSX)) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());                
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        initUserInterface();
        DeviceBridge.initDebugBridge();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Workspace workspace = new Workspace();
                workspace.setDefaultCloseOperation(Workspace.EXIT_ON_CLOSE);
                workspace.setLocationRelativeTo(null);
                workspace.setVisible(true);
            }
        });
    }
}
