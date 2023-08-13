public class bug4252173 {
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                JComponent component = new JLabel();
                Icon horizontalThumbIcon = UIManager.getIcon("Slider.horizontalThumbIcon");
                Icon verticalThumbIcon = UIManager.getIcon("Slider.verticalThumbIcon");
                Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR).getGraphics();
                horizontalThumbIcon.paintIcon(component, g, 0, 0);
                verticalThumbIcon.paintIcon(component, g, 0, 0);
            }
        });
    }
}
