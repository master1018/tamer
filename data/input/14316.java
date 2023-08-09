class GraphicsPanel extends Panel {
    ActionListener al;
    ItemListener il;
    public GraphicsCards cards;
    GraphicsPanel(EventListener listener) {
        al = (ActionListener) listener;
        il = (ItemListener) listener;
        setLayout(new BorderLayout());
        add("Center", cards = new GraphicsCards());
        Panel p = new Panel();
        Button b = new Button("next");
        b.addActionListener(al);
        p.add(b);
        b = new Button("previous");
        b.addActionListener(al);
        p.add(b);
        p.add(new Label("go to:", Label.RIGHT));
        Choice c = new Choice();
        c.addItemListener(il);
        p.add(c);
        c.addItem("Arc");
        c.addItem("Oval");
        c.addItem("Polygon");
        c.addItem("Rect");
        c.addItem("RoundRect");
        add("North", p);
        setSize(400, 400);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 100);
    }
}
@SuppressWarnings("serial")
public class GraphicsTest extends Applet
        implements ActionListener, ItemListener {
    GraphicsPanel mainPanel;
    @Override
    public void init() {
        setLayout(new BorderLayout());
        add("Center", mainPanel = new GraphicsPanel(this));
    }
    @Override
    public void destroy() {
        remove(mainPanel);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        String arg = e.getActionCommand();
        if ("next".equals(arg)) {
            ((CardLayout) mainPanel.cards.getLayout()).next(mainPanel.cards);
        } else if ("previous".equals(arg)) {
            ((CardLayout) mainPanel.cards.getLayout()).previous(mainPanel.cards);
        }
    }
    @Override
    public void itemStateChanged(ItemEvent e) {
        ((CardLayout) mainPanel.cards.getLayout()).show(mainPanel.cards,
                (String) e.getItem());
    }
    public static void main(String args[]) {
        AppletFrame.startApplet("GraphicsTest", "Graphics Test", args);
    }
    @Override
    public String getAppletInfo() {
        return "An interactive demonstration of some graphics.";
    }
}   
@SuppressWarnings("serial")
class GraphicsCards extends Panel {
    public GraphicsCards() {
        setLayout(new CardLayout());
        add("Arc", new ArcCard());
        add("Oval", new ShapeTest(new OvalShape()));
        add("Polygon", new ShapeTest(new PolygonShape()));
        add("Rect", new ShapeTest(new RectShape()));
        add("RoundRect", new ShapeTest(new RoundRectShape()));
    }
}   
@SuppressWarnings("serial")
class ArcCard extends Panel {
    public ArcCard() {
        setLayout(new GridLayout(0, 2));
        add(new ArcPanel(true));
        add(new ArcPanel(false));
        add(new ArcDegreePanel(true));
        add(new ArcDegreePanel(false));
    }
}   
@SuppressWarnings("serial")
class ArcDegreePanel extends Panel {
    boolean filled;
    public ArcDegreePanel(boolean filled) {
        this.filled = filled;
    }
    void arcSteps(Graphics g,
            int step,
            int x,
            int y,
            int w,
            int h,
            Color c1,
            Color c2) {
        int a1 = 0;
        int a2 = step;
        int progress = 0;
        g.setColor(c1);
        for (; (a1 + a2) <= 360; a1 = a1 + a2, a2 += 1) {
            if (g.getColor() == c1) {
                g.setColor(c2);
            } else {
                g.setColor(c1);
            }
            if (filled) {
                g.fillArc(x, y, w, h, a1, a2);
            } else {
                g.drawArc(x, y, w, h, a1, a2);
            }
            progress = a1 + a2;
        }  
        if (progress != 360) {
            if (filled) {
                g.fillArc(x, y, w, h, a1, 360 - progress);
            } else {
                g.drawArc(x, y, w, h, a1, 360 - progress);
            }
        }  
    }  
    @Override
    public void paint(Graphics g) {
        Rectangle r = getBounds();
        arcSteps(g, 3, 0, 0, r.width, r.height, Color.orange, Color.blue);
        arcSteps(g,
                2,
                r.width / 4,
                r.height / 4,
                r.width / 2,
                r.height / 2,
                Color.yellow,
                Color.green);
        arcSteps(g,
                1,
                (r.width * 3) / 8,
                (r.height * 3) / 8,
                r.width / 4,
                r.height / 4,
                Color.magenta,
                Color.white);
    }  
}   
@SuppressWarnings("serial")
class ArcPanel extends Panel {
    boolean filled;
    public ArcPanel(boolean filled) {
        this.filled = filled;
    }
    @Override
    public void paint(Graphics g) {
        Rectangle r = getBounds();
        g.setColor(Color.yellow);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, 0, 45);
        } else {
            g.drawArc(0, 0, r.width, r.height, 0, 45);
        }
        g.setColor(Color.green);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, 90, -45);
        } else {
            g.drawArc(0, 0, r.width, r.height, 90, -45);
        }
        g.setColor(Color.orange);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, 135, -45);
        } else {
            g.drawArc(0, 0, r.width, r.height, 135, -45);
        }
        g.setColor(Color.magenta);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, -225, 45);
        } else {
            g.drawArc(0, 0, r.width, r.height, -225, 45);
        }
        g.setColor(Color.yellow);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, 225, -45);
        } else {
            g.drawArc(0, 0, r.width, r.height, 225, -45);
        }
        g.setColor(Color.green);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, -135, 45);
        } else {
            g.drawArc(0, 0, r.width, r.height, -135, 45);
        }
        g.setColor(Color.orange);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, -45, -45);
        } else {
            g.drawArc(0, 0, r.width, r.height, -45, -45);
        }
        g.setColor(Color.magenta);
        if (filled) {
            g.fillArc(0, 0, r.width, r.height, 315, 45);
        } else {
            g.drawArc(0, 0, r.width, r.height, 315, 45);
        }
    }  
}   
abstract class Shape {
    abstract void draw(Graphics g, int x, int y, int w, int h);
    abstract void fill(Graphics g, int x, int y, int w, int h);
}
class RectShape extends Shape {
    @Override
    void draw(Graphics g, int x, int y, int w, int h) {
        g.drawRect(x, y, w, h);
    }
    @Override
    void fill(Graphics g, int x, int y, int w, int h) {
        g.fillRect(x, y, w, h);
    }
}
class OvalShape extends Shape {
    @Override
    void draw(Graphics g, int x, int y, int w, int h) {
        g.drawOval(x, y, w, h);
    }
    @Override
    void fill(Graphics g, int x, int y, int w, int h) {
        g.fillOval(x, y, w, h);
    }
}
class RoundRectShape extends Shape {
    @Override
    void draw(Graphics g, int x, int y, int w, int h) {
        g.drawRoundRect(x, y, w, h, 10, 10);
    }
    @Override
    void fill(Graphics g, int x, int y, int w, int h) {
        g.fillRoundRect(x, y, w, h, 10, 10);
    }
}
class PolygonShape extends Shape {
    Polygon p;
    Polygon pBase;
    public PolygonShape() {
        pBase = new Polygon();
        pBase.addPoint(0, 0);
        pBase.addPoint(10, 0);
        pBase.addPoint(5, 15);
        pBase.addPoint(10, 20);
        pBase.addPoint(5, 20);
        pBase.addPoint(0, 10);
        pBase.addPoint(0, 0);
    }
    void scalePolygon(float w, float h) {
        p = new Polygon();
        for (int i = 0; i < pBase.npoints; ++i) {
            p.addPoint((int) (pBase.xpoints[i] * w),
                    (int) (pBase.ypoints[i] * h));
        }
    }
    @Override
    void draw(Graphics g, int x, int y, int w, int h) {
        Graphics ng = g.create();
        try {
            ng.translate(x, y);
            scalePolygon(((float) w / 10f), ((float) h / 20f));
            ng.drawPolygon(p);
        } finally {
            ng.dispose();
        }
    }
    @Override
    void fill(Graphics g, int x, int y, int w, int h) {
        Graphics ng = g.create();
        try {
            ng.translate(x, y);
            scalePolygon(((float) w / 10f), ((float) h / 20f));
            ng.fillPolygon(p);
        } finally {
            ng.dispose();
        }
    }
}
@SuppressWarnings("serial")
class ShapeTest extends Panel {
    Shape shape;
    int step;
    public ShapeTest(Shape shape, int step) {
        this.shape = shape;
        this.step = step;
    }
    public ShapeTest(Shape shape) {
        this(shape, 10);
    }
    @Override
    public void paint(Graphics g) {
        Rectangle bounds = getBounds();
        int cx, cy, cw, ch;
        Color color;
        for (color = Color.red, cx = bounds.x, cy = bounds.y,
                cw = bounds.width / 2, ch = bounds.height;
                cw > 0 && ch > 0;
                cx += step, cy += step, cw -= (step * 2), ch -= (step * 2),
                color = ColorUtils.darker(color, 0.9)) {
            g.setColor(color);
            shape.draw(g, cx, cy, cw, ch);
        }
        for (cx = bounds.x + bounds.width / 2, cy = bounds.y,
                cw = bounds.width / 2, ch = bounds.height;
                cw > 0 && ch > 0;
                cx += step, cy += step, cw -= (step * 2), ch -= (step * 2)) {
            if (g.getColor() == Color.red) {
                g.setColor(Color.blue);
            } else {
                g.setColor(Color.red);
            }
            shape.fill(g, cx, cy, cw, ch);
        }  
    }  
}   
class ColorUtils {
    static Color brighter(Color c, double factor) {
        return new Color(Math.min((int) (c.getRed() * (1 / factor)), 255),
                Math.min((int) (c.getGreen() * (1 / factor)), 255),
                Math.min((int) (c.getBlue() * (1 / factor)), 255));
    }
    static Color darker(Color c, double factor) {
        return new Color(Math.max((int) (c.getRed() * factor), 0),
                Math.max((int) (c.getGreen() * factor), 0),
                Math.max((int) (c.getBlue() * factor), 0));
    }
}
