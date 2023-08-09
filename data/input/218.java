public class bug6579827 {
    public static void main(String[] args) throws Exception {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS ||
                OSInfo.getWindowsVersion() != OSInfo.WINDOWS_VISTA) {
            System.out.println("This test is only for Windows Vista. Skipped.");
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
                Dimension prefferdSize = slider.getPreferredSize();
                slider.setPaintTrack(false);
                slider.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
                if (prefferdSize.equals(slider.getPreferredSize())) {
                    throw new RuntimeException();
                }
            }
        });
    }
}
