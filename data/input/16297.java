public class LayoutExtraGaps extends Frame {
    final static int compCount = 30;
    public LayoutExtraGaps() {
        super("GridLayoutTest");
        Panel yellowPanel = new Panel(new GridLayout(compCount, 1, 3, 3));
        yellowPanel.setBackground(Color.yellow);
        for(int i = 0; i < compCount ; i++) {
            Label redLabel = new Label(""+i);
            redLabel.setBackground(Color.red);
            yellowPanel.add(redLabel);
        }
        Panel bluePanel = new Panel(new GridLayout(1, compCount, 3, 3));
        bluePanel.setBackground(Color.blue);
        for(int i = 0; i < compCount; i++) {
            Label greenLabel = new Label(""+i);
            greenLabel.setBackground(Color.green);
            bluePanel.add(greenLabel);
        }
        Panel blackPanel = new Panel(new GridLayout(compCount, 1, 3, 3));
        blackPanel.setBackground(Color.black);
        blackPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        for(int i = 0; i < compCount ; i++) {
            Label redLabel = new Label(""+i);
            redLabel.setBackground(Color.red);
            blackPanel.add(redLabel);
        }
        Panel redPanel = new Panel(new GridLayout(1, compCount, 3, 3));
        redPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        redPanel.setBackground(Color.red);
        for(int i = 0; i < compCount; i++) {
            Label greenLabel = new Label(""+i);
            greenLabel.setBackground(Color.green);
            redPanel.add(greenLabel);
        }
        setLayout(new GridLayout(2, 2, 20, 20));
        add(yellowPanel);
        add(bluePanel);
        add(redPanel);
        add(blackPanel);
        pack();
        setSize((int)getPreferredSize().getWidth() + 50, (int)getPreferredSize().getHeight() + 50);
        setVisible(true);
        Util.waitForIdle(Util.createRobot());
        if (isComponentCentredLTR(yellowPanel) && isComponentCentredLTR(bluePanel)
                && isComponentCentredLTR(blackPanel) && isComponentCentredRTL(redPanel))
        {
            System.out.println("Test passed.");
        } else {
            throw new RuntimeException("Test failed. GridLayout doesn't center component.");
        }
    }
    public static boolean isComponentCentredLTR(Panel p) {
        double borderLeft;
        double borderRight;
        double borderTop;
        double borderBottom;
        Rectangle firstRec = p.getComponent(0).getBounds();
        Rectangle lastRec = p.getComponent(compCount - 1).getBounds();
        System.out.println("bounds of the first rectangle in "+ p.getName() + " = " + firstRec);
        System.out.println("bounds of the last rectangle in "+ p.getName() + " = " + lastRec);
        borderLeft = firstRec.getX();
        borderRight = p.getWidth() - lastRec.getWidth() - lastRec.getX();
        borderTop = firstRec.getY();
        borderBottom = p.getHeight() - lastRec.getHeight() - lastRec.getY();
        return areBordersEqual(borderLeft, borderRight) &&
                areBordersEqual(borderTop, borderBottom);
    }
    public static boolean isComponentCentredRTL(Panel p) {
        double borderLeft;
        double borderRight;
        double borderTop;
        double borderBottom;
        Rectangle firstRec = p.getComponent(0).getBounds();
        Rectangle lastRec = p.getComponent(compCount - 1).getBounds();
        System.out.println("bounds of the first rectangle in "+ p.getName() + " = " + firstRec);
        System.out.println("bounds of the last rectangle in "+ p.getName() + " = " + lastRec);
        borderLeft = lastRec.getX();
        borderRight = p.getWidth() - firstRec.getWidth() - firstRec.getX();
        borderTop = lastRec.getY();
        borderBottom = p.getHeight() - firstRec.getHeight() - firstRec.getY();
        return areBordersEqual(borderLeft, borderRight) &&
                areBordersEqual(borderTop, borderBottom);
    }
    public static boolean areBordersEqual(double border1, double border2) {
        return Math.abs(border1 - border2) <= 1;
    }
    public static void main(String[] args) {
        new LayoutExtraGaps();
    }
}
