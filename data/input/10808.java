public class ExtraMouseClick extends Applet
{
    Frame frame = new Frame("Extra Click After MouseDrag");
    final int TRIALS = 10;
    final int SMUDGE_WIDTH = 4;
    final int SMUDGE_HEIGHT = 4;
    Robot robot;
    Point fp; 
    boolean dragged = false;
    boolean clicked = false;
    boolean pressed = false;
    boolean released = false;
    public void init()
    {
        this.setLayout (new BorderLayout ());
        frame.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    System.out.println("MousePressed");
                    pressed = true;
                }
                public void mouseReleased(MouseEvent e) {
                    System.out.println("MouseReleased");
                    released = true;
                }
                public void mouseClicked(MouseEvent e) {
                    System.out.println("MouseClicked!!!!");
                    clicked = true;
                }
            });
        frame.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    System.out.println("MouseDragged--"+e);
                    dragged = true;
                }
                public void mouseMoved(MouseEvent e) {
                }
            });
    }
    public void start ()
    {
        frame.setSize(480, 300);
        frame.setVisible(true);
        try{
            robot = new Robot();
        }catch(AWTException e){
            throw new RuntimeException(e);
        }
        Util.waitForIdle(robot);  
        fp = frame.getLocationOnScreen();
        for (int i = 0; i< TRIALS; i++){
            checkClicked();
            clearFlags();
        }
        for (int i = 0; i< TRIALS; i++){
            oneDrag(2);
            clearFlags();
        }
        for (int i = 0; i< TRIALS; i++){
            oneDrag(5);
            clearFlags();
        }
        for (int i = 0; i< TRIALS; i++){
            oneDrag(70);
            clearFlags();
        }
        String sToolkit = Toolkit.getDefaultToolkit().getClass().getName();
        System.out.println("Toolkit == "+sToolkit);
        if ("sun.awt.windows.WToolkit".equals(sToolkit)){
            int dragWidth = ((Integer)Toolkit.getDefaultToolkit().
                             getDesktopProperty("win.drag.width")).intValue();
            int dragHeight = ((Integer)Toolkit.getDefaultToolkit().
                            getDesktopProperty("win.drag.height")).intValue();
            System.out.println("dragWidth=="+dragWidth+":: dragHeight=="+dragHeight);
            dragWidth = dragWidth > 1? dragWidth/2:1;
            dragHeight = dragHeight > 1? dragHeight/2:1;
            for (int i = 0; i< TRIALS; i++){
                smallWin32Drag(dragWidth, dragHeight);
                clearFlags();
            }
        }else{
            for (int i = 0; i< TRIALS; i++){
                smallDrag(SMUDGE_WIDTH - 1, SMUDGE_HEIGHT - 1); 
                clearFlags();
            }
        }
        System.out.println("Test passed.");
    }
    public void oneDrag(int pixels){
        robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 );
        robot.mousePress(InputEvent.BUTTON1_MASK );
        for (int i = 1; i<pixels;i++){
            robot.mouseMove(fp.x + frame.getWidth()/2 + i, fp.y + frame.getHeight()/2 );
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK );
        robot.delay(1000);
        if (dragged && clicked){
            throw new RuntimeException("Test failed. Clicked event follows by Dragged. Dragged = "+dragged +". Clicked = "+clicked + " : distance = "+pixels);
        }
    }
  public void smallDrag(int pixelsX, int pixelsY){
        robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 );
        robot.mousePress(InputEvent.BUTTON1_MASK );
        for (int i = 1; i<pixelsX;i++){
            robot.mouseMove(fp.x + frame.getWidth()/2 + i, fp.y + frame.getHeight()/2 );
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK );
        robot.delay(1000);
        if (dragged){
            throw new RuntimeException("Test failed. Dragged event (by the X-axis) occured in SMUDGE area. Dragged = "+dragged +". Clicked = "+clicked);
        }
        robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 );
        robot.mousePress(InputEvent.BUTTON1_MASK );
        for (int i = 1; i<pixelsY;i++){
            robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 + i );
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK );
        robot.delay(1000);
        if (dragged){
            throw new RuntimeException("Test failed. Dragged event (by the Y-axis) occured in SMUDGE area. Dragged = "+dragged +". Clicked = "+clicked);
        }
    }
    public void smallWin32Drag(int pixelsX, int pixelsY){
        robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 );
        robot.mousePress(InputEvent.BUTTON1_MASK );
        System.out.println(" pixelsX = "+ pixelsX +" pixelsY = " +pixelsY);
        for (int i = 1; i<=pixelsX;i++){
            System.out.println("Moving a mouse by X");
            robot.mouseMove(fp.x + frame.getWidth()/2 + i, fp.y + frame.getHeight()/2 );
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK );
        robot.delay(1000);
        if (!dragged){
            throw new RuntimeException("Test failed. Dragged event (by the X-axis) didn't occur in the SMUDGE area. Dragged = "+dragged);
        }
        robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 );
        robot.mousePress(InputEvent.BUTTON1_MASK );
        for (int i = 1; i<=pixelsY;i++){
            System.out.println("Moving a mouse by Y");
            robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 + i );
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK );
        robot.delay(1000);
        if (!dragged){
            throw new RuntimeException("Test failed. Dragged event (by the Y-axis) didn't occur in the SMUDGE area. Dragged = "+dragged);
        }
    }
    public void checkClicked(){
        robot.mouseMove(fp.x + frame.getWidth()/2, fp.y + frame.getHeight()/2 );
        robot.mousePress(InputEvent.BUTTON1_MASK );
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON1_MASK );
        robot.delay(1000);
        if (!clicked || !pressed || !released || dragged){
            throw new RuntimeException("Test failed. Some of Pressed/Released/Clicked events are missed or dragged occured. Pressed/Released/Clicked/Dragged = "+pressed + ":"+released+":"+clicked +":" +dragged);
        }
    }
    public void clearFlags(){
        clicked = false;
        pressed = false;
        released = false;
        dragged = false;
    }
}
