public class SplashPanel extends JPanel
{
    private final MyAnimator  animator  = new MyAnimator();
    private final MyRepainter repainter = new MyRepainter();
    private final Sprite sprite;
    private final double sleepFactor;
    private long   startTime = Long.MAX_VALUE;
    private final long   stopTime;
    private volatile Thread animationThread;
    public SplashPanel(Sprite sprite, double processorLoad)
    {
        this(sprite, processorLoad, (long)Integer.MAX_VALUE);
    }
    public SplashPanel(Sprite sprite, double processorLoad, long stopTime)
    {
        this.sprite      = sprite;
        this.sleepFactor = (1.0-processorLoad) / processorLoad;
        this.stopTime    = stopTime;
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                SplashPanel.this.start();
            }
        });
    }
    public void start()
    {
        startTime = System.currentTimeMillis();
        if (animationThread == null)
        {
            animationThread = new Thread(animator);
            animationThread.start();
        }
    }
    public void stop()
    {
        startTime = 0L;
        animationThread = null;
        try
        {
            SwingUtil.invokeAndWait(repainter);
        }
        catch (InterruptedException ex)
        {
        }
        catch (InvocationTargetException ex)
        {
        }
    }
    public void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        sprite.paint(graphics, System.currentTimeMillis() - startTime);
    }
    private class MyAnimator implements Runnable
    {
        public void run()
        {
            try
            {
                while (animationThread != null)
                {
                    long time = System.currentTimeMillis();
                    if (time > startTime + stopTime)
                    {
                        animationThread = null;
                    }
                    SwingUtil.invokeAndWait(repainter);
                    long repaintTime = System.currentTimeMillis() - time;
                    long sleepTime   = (long)(sleepFactor * repaintTime);
                    if (sleepTime < 10L)
                    {
                        sleepTime = 10L;
                    }
                    Thread.sleep(sleepTime);
                }
            }
            catch (InterruptedException ex)
            {
            }
            catch (InvocationTargetException ex)
            {
            }
        }
    }
    private class MyRepainter implements Runnable
    {
        public void run()
        {
            SplashPanel.this.repaint();
        }
    }
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setTitle("Animation");
        frame.setSize(800, 600);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize  = frame.getSize();
        frame.setLocation((screenSize.width - frameSize.width)   / 2,
                          (screenSize.height - frameSize.height) / 2);
        Sprite sprite =
            new ClipSprite(
            new ConstantColor(Color.white),
            new ConstantColor(Color.lightGray),
            new CircleSprite(true,
                             new LinearInt(200, 600, new SineTiming(2345L, 0L)),
                             new LinearInt(200, 400, new SineTiming(3210L, 0L)),
                             new ConstantInt(150)),
            new ColorSprite(new ConstantColor(Color.gray),
            new FontSprite(new ConstantFont(new Font("sansserif", Font.BOLD, 90)),
            new TextSprite(new ConstantString("ProGuard"),
                           new ConstantInt(200),
                           new ConstantInt(300)))));
        SplashPanel panel = new SplashPanel(sprite, 0.5);
        panel.setBackground(Color.white);
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.start();
    }
}
