public class DialogSizeOverflowTest
{
    public static void main(String [] s) {
        Robot robot;
        Frame f = new Frame("a frame");
        final Dialog dlg = new Dialog(f, false);
        f.setVisible(true);
        try {
            robot = new Robot();
        } catch(AWTException e){
            throw new RuntimeException("Test interrupted.", e);
        }
        Util.waitForIdle(robot);
        dlg.setLocation(100, 100);
        dlg.setResizable(false);
        dlg.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    Dimension size = dlg.getSize();
                    System.out.println("size.width : size.height "+size.width + " : "+ size.height);
                    if (size.width > 1000 || size.height > 1000 || size.width < 0 || size.height < 0) {
                        throw new RuntimeException("Test failed. Size is too large.");
                    }
                }
            });
        dlg.toBack();
        dlg.setVisible(true);
        Util.waitForIdle(robot);
        System.out.println("Test passed.");
    }
}
