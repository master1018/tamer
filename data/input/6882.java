public class DrawTest extends Applet {
    DrawPanel panel;
    DrawControls controls;
    @Override
    public void init() {
        setLayout(new BorderLayout());
        panel = new DrawPanel();
        controls = new DrawControls(panel);
        add("Center", panel);
        add("South", controls);
    }
    @Override
    public void destroy() {
        remove(panel);
        remove(controls);
    }
    public static void main(String args[]) {
        Frame f = new Frame("DrawTest");
        DrawTest drawTest = new DrawTest();
        drawTest.init();
        drawTest.start();
        f.add("Center", drawTest);
        f.setSize(300, 300);
        f.setVisible(true);
    }
    @Override
    public String getAppletInfo() {
        return "A simple drawing program.";
    }
}
@SuppressWarnings("serial")
class DrawPanel extends Panel implements MouseListener, MouseMotionListener {
    public static final int LINES = 0;
    public static final int POINTS = 1;
    int mode = LINES;
    List<Rectangle> lines = new ArrayList<Rectangle>();
    List<Color> colors = new ArrayList<Color>();
    int x1, y1;
    int x2, y2;
    @SuppressWarnings("LeakingThisInConstructor")
    public DrawPanel() {
        setBackground(Color.white);
        addMouseMotionListener(this);
        addMouseListener(this);
    }
    public void setDrawMode(int mode) {
        switch (mode) {
            case LINES:
            case POINTS:
                this.mode = mode;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        e.consume();
        switch (mode) {
            case LINES:
                x2 = e.getX();
                y2 = e.getY();
                break;
            case POINTS:
            default:
                colors.add(getForeground());
                lines.add(new Rectangle(x1, y1, e.getX(), e.getY()));
                x1 = e.getX();
                y1 = e.getY();
                break;
        }
        repaint();
    }
    @Override
    public void mouseMoved(MouseEvent e) {
    }
    @Override
    public void mousePressed(MouseEvent e) {
        e.consume();
        switch (mode) {
            case LINES:
                x1 = e.getX();
                y1 = e.getY();
                x2 = -1;
                break;
            case POINTS:
            default:
                colors.add(getForeground());
                lines.add(new Rectangle(e.getX(), e.getY(), -1, -1));
                x1 = e.getX();
                y1 = e.getY();
                repaint();
                break;
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        e.consume();
        switch (mode) {
            case LINES:
                colors.add(getForeground());
                lines.add(new Rectangle(x1, y1, e.getX(), e.getY()));
                x2 = -1;
                break;
            case POINTS:
            default:
                break;
        }
        repaint();
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    @Override
    public void paint(Graphics g) {
        int np = lines.size();
        g.setColor(getForeground());
        for (int i = 0; i < np; i++) {
            Rectangle p = lines.get(i);
            g.setColor(colors.get(i));
            if (p.width != -1) {
                g.drawLine(p.x, p.y, p.width, p.height);
            } else {
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
        if (mode == LINES) {
            g.setColor(getForeground());
            if (x2 != -1) {
                g.drawLine(x1, y1, x2, y2);
            }
        }
    }
}
@SuppressWarnings("serial")
class DrawControls extends Panel implements ItemListener {
    DrawPanel target;
    @SuppressWarnings("LeakingThisInConstructor")
    public DrawControls(DrawPanel target) {
        this.target = target;
        setLayout(new FlowLayout());
        setBackground(Color.lightGray);
        target.setForeground(Color.red);
        CheckboxGroup group = new CheckboxGroup();
        Checkbox b;
        add(b = new Checkbox(null, group, false));
        b.addItemListener(this);
        b.setForeground(Color.red);
        add(b = new Checkbox(null, group, false));
        b.addItemListener(this);
        b.setForeground(Color.green);
        add(b = new Checkbox(null, group, false));
        b.addItemListener(this);
        b.setForeground(Color.blue);
        add(b = new Checkbox(null, group, false));
        b.addItemListener(this);
        b.setForeground(Color.pink);
        add(b = new Checkbox(null, group, false));
        b.addItemListener(this);
        b.setForeground(Color.orange);
        add(b = new Checkbox(null, group, true));
        b.addItemListener(this);
        b.setForeground(Color.black);
        target.setForeground(b.getForeground());
        Choice shapes = new Choice();
        shapes.addItemListener(this);
        shapes.addItem("Lines");
        shapes.addItem("Points");
        shapes.setBackground(Color.lightGray);
        add(shapes);
    }
    @Override
    public void paint(Graphics g) {
        Rectangle r = getBounds();
        g.setColor(Color.lightGray);
        g.draw3DRect(0, 0, r.width, r.height, false);
        int n = getComponentCount();
        for (int i = 0; i < n; i++) {
            Component comp = getComponent(i);
            if (comp instanceof Checkbox) {
                Point loc = comp.getLocation();
                Dimension d = comp.getSize();
                g.setColor(comp.getForeground());
                g.drawRect(loc.x - 1, loc.y - 1, d.width + 1, d.height + 1);
            }
        }
    }
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof Checkbox) {
            target.setForeground(((Component) e.getSource()).getForeground());
        } else if (e.getSource() instanceof Choice) {
            String choice = (String) e.getItem();
            if (choice.equals("Lines")) {
                target.setDrawMode(DrawPanel.LINES);
            } else if (choice.equals("Points")) {
                target.setDrawMode(DrawPanel.POINTS);
            }
        }
    }
}
