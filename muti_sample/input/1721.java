public class GridBagLayoutIpadXYTest extends Applet
{
    Frame frame = new Frame();
    TextField jtf = null;
    final int customIpadx = 300;
    final int customIpady = 40;
    public void init()
    {
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            "This is an AUTOMATIC test",
            "simply wait until it is done"
        };
    }
    public void start ()
    {
        validate();
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        Insets fieldInsets = new Insets(0,5,5,0);
        gc.anchor = gc.NORTH;
        gc.fill = gc.HORIZONTAL;
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.ipadx = customIpadx;
        gc.ipady = customIpady;
        gc.insets = fieldInsets;
        jtf = new TextField();
        frame.add(jtf, gc);
        frame.pack();
        frame.setVisible(true);
        ((sun.awt.SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Dimension minSize = jtf.getMinimumSize();
        if ( minSize.width + customIpadx != jtf.getSize().width ||
             minSize.height + customIpady != jtf.getSize().height ){
            System.out.println("TextField originally has min size = " + jtf.getMinimumSize());
            System.out.println("TextField supplied with ipadx =  300, ipady =40");
            System.out.println("Frame size: " + frame.getSize());
            System.out.println(" Fields's size is "+jtf.getSize());
            throw new RuntimeException("Test Failed. TextField has incorrect width. ");
        }
        System.out.println("Test Passed.");
    }
}
