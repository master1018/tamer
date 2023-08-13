public class ShapeNotSetSometimes {
    private Frame backgroundFrame;
    private Frame window;
    private static final Color BACKGROUND_COLOR = Color.BLUE;
    private Shape shape;
    private int[][] pointsToCheck;
    private static Robot robot;
    public ShapeNotSetSometimes() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                initializeGUI();
            }
        });
    }
    private void initializeGUI() {
        backgroundFrame = new BackgroundFrame();
        backgroundFrame.setUndecorated(true);
        backgroundFrame.setSize(300, 300);
        backgroundFrame.setLocation(20, 400);
        backgroundFrame.setVisible(true);
        shape = null;
        String shape_name = null;
        Area a;
        GeneralPath gp;
        shape_name = "Rounded-corners";
        a = new Area();
        a.add(new Area(new Rectangle2D.Float(50, 0, 100, 150)));
        a.add(new Area(new Rectangle2D.Float(0, 50, 200, 50)));
        a.add(new Area(new Ellipse2D.Float(0, 0, 100, 100)));
        a.add(new Area(new Ellipse2D.Float(0, 50, 100, 100)));
        a.add(new Area(new Ellipse2D.Float(100, 0, 100, 100)));
        a.add(new Area(new Ellipse2D.Float(100, 50, 100, 100)));
        shape = a;
        pointsToCheck = new int[][] {
            {106, 86}, {96, 38}, {76, 107}, {180, 25}, {24, 105},
            {196, 77}, {165, 50}, {14, 113}, {89, 132}, {167, 117},
            {165, 196}, {191, 163}, {146, 185}, {61, 170}, {148, 171},
            {82, 172}, {186, 11}, {199, 141}, {13, 173}, {187, 3}
        };
        window = new TestFrame();
        window.setUndecorated(true);
        window.setSize(200, 200);
        window.setLocation(70, 450);
        window.setShape(shape);
        window.setVisible(true);
        System.out.println("Checking " + window.getClass().getSuperclass().getName() + " with " + shape_name + " shape (" + window.getShape() + ")...");
    }
    class BackgroundFrame extends Frame {
        @Override
        public void paint(Graphics g) {
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, 300, 300);
            super.paint(g);
        }
    }
    class TestFrame extends Frame {
        @Override
        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 200, 200);
            super.paint(g);
        }
    }
    public static void main(String[] args) throws Exception {
        robot = new Robot();
        for(int i = 0; i < 100; i++) {
            System.out.println("Attempt " + i);
            new ShapeNotSetSometimes().doTest();
        }
    }
    private void doTest() throws Exception {
        Point wls = backgroundFrame.getLocationOnScreen();
        robot.mouseMove(wls.x + 5, wls.y + 5);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(500);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                window.requestFocus();
            }
        });
        robot.waitForIdle();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
        }
        final int COUNT_TARGET = 10;
        for(int i = COUNT_TARGET; i < COUNT_TARGET * 2; i++) {
            int x = pointsToCheck[i][0];
            int y = pointsToCheck[i][1];
            boolean inside = i < COUNT_TARGET;
            Color c = robot.getPixelColor(window.getX() + x, window.getY() + y);
            System.out.println("checking " + x + ", " + y + ", color = " + c);
            if (inside && BACKGROUND_COLOR.equals(c) || !inside && !BACKGROUND_COLOR.equals(c)) {
                System.out.println("window.getX() = " + window.getX() + ", window.getY() = " + window.getY());
                System.err.println("Checking for transparency failed: point: " +
                        (window.getX() + x) + ", " + (window.getY() + y) +
                        ", color = " + c + (inside ? " is of un" : " is not of ") +
                        "expected background color " + BACKGROUND_COLOR);
                throw new RuntimeException("Test failed. The shape has not been applied.");
            }
        }
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                backgroundFrame.dispose();
                window.dispose();
            }
        });
    }
}
