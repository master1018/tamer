public class ManualInstructions extends Applet
{
    final static long SEND_DELAY = 1000;
    public static void main(String s[]){
        ManualInstructions mi = new ManualInstructions();
        mi.init();
        mi.start();
    }
    static Robot robot;
    Point mouseLocation; 
    Panel target = new Panel();
    Button pressOn = new Button("press on ...");
    Button releaseOn = new Button("release on ...");
    Button clickOn = new Button("click on ...");
    Choice buttonNumber = new Choice();
    public void init()
    {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        this.setLayout (new BorderLayout ());
        target.setBackground(Color.green);
        target.setName("GreenBox");
        target.setPreferredSize(new Dimension(100, 100));
        String toolkit = Toolkit.getDefaultToolkit().getClass().getName();
        int buttonsNumber = toolkit.equals("sun.awt.windows.WToolkit")?MouseInfo.getNumberOfButtons():MouseInfo.getNumberOfButtons()-2;
        for (int i = 0; i < 8; i++){
            buttonNumber.add("BUTTON"+(i+1)+"_MASK");
        }
        pressOn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    System.out.println("Now pressing : " + (buttonNumber.getSelectedIndex()+1));
                    Timer timer = new Timer();
                    TimerTask robotInteraction = new TimerTask(){
                            public void run(){
                                robot.mouseMove(updateTargetLocation().x, updateTargetLocation().y);
                                robot.mousePress(getMask(buttonNumber.getSelectedIndex()+1));
                            }
                        };
                    timer.schedule(robotInteraction, SEND_DELAY);
                }
            });
        releaseOn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.out.println("Now releasing : " + (buttonNumber.getSelectedIndex()+1));
                Timer timer = new Timer();
                TimerTask robotInteraction = new TimerTask(){
                        public void run(){
                            robot.mouseMove(updateTargetLocation().x, updateTargetLocation().y);
                            robot.mouseRelease(getMask(buttonNumber.getSelectedIndex()+1));
                        }
                    };
                timer.schedule(robotInteraction, SEND_DELAY);
            }
        });
        clickOn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.out.println("Now clicking : " + (buttonNumber.getSelectedIndex()+1));
                Timer timer = new Timer();
                TimerTask robotInteraction = new TimerTask(){
                        public void run(){
                            robot.mouseMove(updateTargetLocation().x, updateTargetLocation().y);
                            robot.mousePress(getMask(buttonNumber.getSelectedIndex()+1));
                            robot.mouseRelease(getMask(buttonNumber.getSelectedIndex()+1));
                        }
                    };
                timer.schedule(robotInteraction, SEND_DELAY);
            }
        });
        target.addMouseListener(new MouseAdapter(){
           public void mousePressed(MouseEvent e){
                Sysout.println(""+e);
           }
           public void mouseReleased(MouseEvent e){
                Sysout.println(""+e);
           }
           public void mouseClicked(MouseEvent e){
                Sysout.println(""+e);
           }
        });
        String[] instructions =
        {
            "Do provide an instruction to the robot by",
            "choosing the button number to act and ",
            "pressing appropriate java.awt.Button on the left.",
            "Inspect an output in the TextArea below.",
            "Please don't generate non-natural sequences like Release-Release, etc.",
            "If you use keyboard be sure that you released the keyboard shortly.",
            "If events are generated well press Pass, otherwise Fail."
        };
        Sysout.createDialogWithInstructions( instructions );
    }
    private int getMask(int button){
        return InputEvent.getMaskForButton(button);
    }
    private Point updateTargetLocation() {
        return new Point(target.getLocationOnScreen().x + target.getWidth()/2, target.getLocationOnScreen().y + target.getHeight()/2);
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
        Frame f = new Frame ("Set action for Robot here.");
        f.setLayout(new FlowLayout());
        f.add(buttonNumber);
        f.add(pressOn);
        f.add(releaseOn);
        f.add(clickOn);
        f.add(target);
        f.pack();
        f.setVisible(true);
     }
}
class Sysout
{
    private static TestDialog dialog;
    public static void createDialogWithInstructions( String[] instructions )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }
    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }
    public static void printInstructions( String[] instructions )
    {
        dialog.printInstructions( instructions );
    }
    public static void println( String messageIn )
    {
        dialog.displayMessage( messageIn );
    }
}
class TestDialog extends Dialog
{
    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 120;
    public TestDialog( Frame frame, String name )
    {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add( "North", instructionsText );
        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add("Center", messageText);
        pack();
        setVisible(true);
    }
    public void printInstructions( String[] instructions )
    {
        instructionsText.setText( "" );
        String printStr, remainingStr;
        for( int i=0; i < instructions.length; i++ )
        {
            remainingStr = instructions[ i ];
            while( remainingStr.length() > 0 )
            {
                if( remainingStr.length() >= maxStringLength )
                {
                    int posOfSpace = remainingStr.
                        lastIndexOf( ' ', maxStringLength - 1 );
                    if( posOfSpace <= 0 ) posOfSpace = maxStringLength - 1;
                    printStr = remainingStr.substring( 0, posOfSpace + 1 );
                    remainingStr = remainingStr.substring( posOfSpace + 1 );
                }
                else
                {
                    printStr = remainingStr;
                    remainingStr = "";
                }
                instructionsText.append( printStr + "\n" );
            }
        }
    }
    public void displayMessage( String messageIn )
    {
        messageText.append( messageIn + "\n" );
        System.out.println(messageIn);
    }
}
