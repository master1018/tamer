public class bug6794831 {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    public static void main(String args[]) throws InterruptedException {
        new bug6794831().run();
    }
    private void run() throws InterruptedException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
                    try {
                        UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                    } catch (Exception e) {
                        fail(e.getMessage());
                    }
                    BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
                    JSlider slider = new JSlider(0, Integer.MAX_VALUE - 1, 0);
                    slider.setMajorTickSpacing((Integer.MAX_VALUE - 1) / 4);
                    slider.setPaintTicks(true);
                    ((BasicSliderUI) slider.getUI()).paintTicks(image.getGraphics());
                    slider = new JSlider(0, Integer.MAX_VALUE - 1, 0);
                    slider.setMinorTickSpacing((Integer.MAX_VALUE - 1) / 4);
                    slider.setPaintTicks(true);
                    ((BasicSliderUI) slider.getUI()).paintTicks(image.getGraphics());
                    slider = new JSlider(0, Integer.MAX_VALUE - 1, 0);
                    slider.setOrientation(JSlider.VERTICAL);
                    slider.setMajorTickSpacing((Integer.MAX_VALUE - 1) / 4);
                    slider.setPaintTicks(true);
                    ((BasicSliderUI) slider.getUI()).paintTicks(image.getGraphics());
                    slider = new JSlider(0, Integer.MAX_VALUE - 1, 0);
                    slider.setOrientation(JSlider.VERTICAL);
                    slider.setMinorTickSpacing((Integer.MAX_VALUE - 1) / 4);
                    slider.setPaintTicks(true);
                    ((BasicSliderUI) slider.getUI()).paintTicks(image.getGraphics());
                    countDownLatch.countDown();
                }
            }
        });
        if (countDownLatch.await(3000, TimeUnit.MILLISECONDS)) {
            System.out.println("bug6794831 passed");
        } else {
            fail("bug6794831 failed");
        }
    }
    private static void fail(String msg) {
        throw new RuntimeException(msg);
    }
}
