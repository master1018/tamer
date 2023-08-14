public class bug6923305 {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new SynthLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JSlider slider = new JSlider();
                slider.setUI(new SynthSliderUI(slider) {
                    @Override
                    protected void paintTrack(SynthContext context, Graphics g, Rectangle trackBounds) {
                        throw new RuntimeException("Test failed: the SynthSliderUI.paintTrack was invoked");
                    }
                });
                slider.setPaintTrack(false);
                slider.setSize(slider.getPreferredSize());
                BufferedImage bufferedImage = new BufferedImage(slider.getWidth(), slider.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                slider.paint(bufferedImage.getGraphics());
            }
        });
    }
}
