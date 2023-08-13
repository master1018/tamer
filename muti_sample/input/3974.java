public class TranslucencyThrowsExceptionWhenFullScreen
{
    public static void main(String[] args)
        throws InvocationTargetException, InterruptedException
    {
        EventQueue.invokeAndWait(
            new Runnable(){
                public void run() {
                    Frame frame = new Frame();
                    frame.setBounds(100,100,100,100);
                    frame.setVisible(true);
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice[] devices = ge.getScreenDevices();
                    for (GraphicsDevice device : devices) {
                        testGraphicsDevice(device, frame);
                    }
                    frame.dispose();
                }
            }
        );
    }
    private static void testGraphicsDevice(GraphicsDevice device, Frame frame) {
        device.setFullScreenWindow(frame);
        try {
            frame.setOpacity(0.5f);
            throw new RuntimeException("Test fails, there's no exception for device="+device);
        } catch(IllegalComponentStateException e) {
            device.setFullScreenWindow(null);
        }
    }
}
