public class RequestOnCompWithNullParent1
{
    private static void init() {
        String[] instructions =
        {
            "This is an AUTOMATIC test, simply wait until it is done.",
            "The result (passed or failed) will be shown in the",
            "message window below."
        };
        Sysout.createDialog( );
        Sysout.printInstructions( instructions );
        Frame frame = new Frame("test for 6418028");
        frame.setLayout(new FlowLayout());
        Button btn1 = new Button("Button1");
        frame.add(btn1);
        TestButton btn2 = new TestButton("Button2");
        frame.add(btn2);
        frame.pack();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                we.getWindow().dispose();
            }
        });
        frame.setVisible(true);
        Util.waitForIdle(null);
        btn2.instrumentPeer();
        btn2.requestFocusInWindow();
        btn2.restorePeer();
        frame.dispose();
        RequestOnCompWithNullParent1.pass();
    }
    private static boolean theTestPassed = false;
    private static boolean testGeneratedInterrupt = false;
    private static String failureMessage = "";
    private static Thread mainThread = null;
    private static int sleepTime = 300000;
    public static void main( String args[] ) throws InterruptedException
    {
        mainThread = Thread.currentThread();
        try
        {
            init();
        }
        catch( TestPassedException e )
        {
            return;
        }
        try
        {
            Thread.sleep( sleepTime );
            throw new RuntimeException( "Timed out after " + sleepTime/1000 + " seconds" );
        }
        catch (InterruptedException e)
        {
            if( ! testGeneratedInterrupt ) throw e;
            testGeneratedInterrupt = false;
            if ( theTestPassed == false )
            {
                throw new RuntimeException( failureMessage );
            }
        }
    }
    public static synchronized void setTimeoutTo( int seconds )
    {
        sleepTime = seconds * 1000;
    }
    public static synchronized void pass()
    {
        Sysout.println( "The test passed." );
        Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
        if ( mainThread == Thread.currentThread() )
        {
            theTestPassed = true;
            throw new TestPassedException();
        }
        theTestPassed = true;
        testGeneratedInterrupt = true;
        mainThread.interrupt();
    }
    public static synchronized void fail()
    {
        fail( "it just plain failed! :-)" );
    }
    public static synchronized void fail( String whyFailed )
    {
        Sysout.println( "The test failed: " + whyFailed );
        Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
        if ( mainThread == Thread.currentThread() )
        {
            throw new RuntimeException( whyFailed );
        }
        theTestPassed = false;
        testGeneratedInterrupt = true;
        failureMessage = whyFailed;
        mainThread.interrupt();
    }
}
class TestPassedException extends RuntimeException
{
}
class TestButton extends Button {
    ButtonPeer origPeer;
    ButtonPeer proxiedPeer;
    public TestButton(String text) {
        super(text);
    }
    public void instrumentPeer() {
        origPeer = (ButtonPeer) getPeer();
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if (method.getName().equals("requestFocus")) {
                    Container parent = getParent();
                    parent.remove(TestButton.this);
                    System.err.println("parent = " + parent);
                    System.err.println("target = " + TestButton.this);
                    System.err.println("new parent = " + TestButton.this.getParent());
                }
                Object ret = null;
                try {
                    ret = method.invoke(origPeer, args);
                } catch (IllegalAccessException iae) {
                    throw new Error("Test error.", iae);
                } catch (InvocationTargetException ita) {
                    throw new Error("Test error.", ita);
                }
                return ret;
            }
        };
        proxiedPeer = (ButtonPeer) Proxy.newProxyInstance(ButtonPeer.class.getClassLoader(), new Class[] {ButtonPeer.class}, handler);
        setPeer(proxiedPeer);
    }
    private void setPeer(final ComponentPeer newPeer) {
        try {
            Field peer_field = Component.class.getDeclaredField("peer");
            peer_field.setAccessible(true);
            peer_field.set(this, newPeer);
        } catch (IllegalArgumentException ex) {
            throw new Error("Test error.", ex);
        } catch (SecurityException ex) {
            throw new Error("Test error.", ex);
        } catch (IllegalAccessException ex) {
            throw new Error("Test error.", ex);
        } catch (NoSuchFieldException ex) {
            throw new Error("Test error.", ex);
        }
    }
    public void restorePeer() {
        if (origPeer != null) {
            setPeer(origPeer);
            proxiedPeer = null;
        }
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
        System.out.println(messageIn);
    }
}
class TestDialog extends Dialog
{
    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;
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
