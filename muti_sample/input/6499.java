public class DitherTest extends Applet implements Runnable {
    private Thread runner;
    private DitherControls XControls;
    private DitherControls YControls;
    private DitherCanvas canvas;
    public static void main(String args[]) {
        Frame f = new Frame("DitherTest");
        DitherTest ditherTest = new DitherTest();
        ditherTest.init();
        f.add("Center", ditherTest);
        f.pack();
        f.setVisible(true);
        ditherTest.start();
    }
    @Override
    public void init() {
        String xspec = null, yspec = null;
        int xvals[] = new int[2];
        int yvals[] = new int[2];
        try {
            xspec = getParameter("xaxis");
            yspec = getParameter("yaxis");
        } catch (NullPointerException ignored) {
        }
        if (xspec == null) {
            xspec = "red";
        }
        if (yspec == null) {
            yspec = "blue";
        }
        DitherMethod xmethod = colorMethod(xspec, xvals);
        DitherMethod ymethod = colorMethod(yspec, yvals);
        setLayout(new BorderLayout());
        XControls = new DitherControls(this, xvals[0], xvals[1],
                xmethod, false);
        YControls = new DitherControls(this, yvals[0], yvals[1],
                ymethod, true);
        YControls.addRenderButton();
        add("North", XControls);
        add("South", YControls);
        add("Center", canvas = new DitherCanvas());
    }
    private DitherMethod colorMethod(String s, int vals[]) {
        DitherMethod method = DitherMethod.NOOP;
        if (s == null) {
            s = "";
        }
        String lower = s.toLowerCase();
        for (DitherMethod m : DitherMethod.values()) {
            if (lower.startsWith(m.toString().toLowerCase())) {
                method = m;
                lower = lower.substring(m.toString().length());
            }
        }
        if (method == DitherMethod.NOOP) {
            vals[0] = 0;
            vals[1] = 0;
            return method;
        }
        int begval = 0;
        int endval = 255;
        try {
            int dash = lower.indexOf('-');
            if (dash < 0) {
                endval = Integer.parseInt(lower);
            } else {
                begval = Integer.parseInt(lower.substring(0, dash));
                endval = Integer.parseInt(lower.substring(dash + 1));
            }
        } catch (NumberFormatException ignored) {
        }
        if (begval < 0) {
            begval = 0;
        } else if (begval > 255) {
            begval = 255;
        }
        if (endval < 0) {
            endval = 0;
        } else if (endval > 255) {
            endval = 255;
        }
        vals[0] = begval;
        vals[1] = endval;
        return method;
    }
    private Image calculateImage() {
        Thread me = Thread.currentThread();
        int width = canvas.getSize().width;
        int height = canvas.getSize().height;
        int xvals[] = new int[2];
        int yvals[] = new int[2];
        int xmethod = XControls.getParams(xvals);
        int ymethod = YControls.getParams(yvals);
        int pixels[] = new int[width * height];
        int c[] = new int[4];   
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                c[0] = c[1] = c[2] = 0;
                c[3] = 255;
                if (xmethod < ymethod) {
                    applyMethod(c, xmethod, i, width, xvals);
                    applyMethod(c, ymethod, j, height, yvals);
                } else {
                    applyMethod(c, ymethod, j, height, yvals);
                    applyMethod(c, xmethod, i, width, xvals);
                }
                pixels[index++] = ((c[3] << 24) | (c[0] << 16) | (c[1] << 8)
                        | c[2]);
            }
            if (runner != me) {
                return null;
            }
        }
        return createImage(new MemoryImageSource(width, height,
                ColorModel.getRGBdefault(), pixels, 0, width));
    }
    private void applyMethod(int c[], int methodIndex, int step,
            int total, int vals[]) {
        DitherMethod method = DitherMethod.values()[methodIndex];
        if (method == DitherMethod.NOOP) {
            return;
        }
        int val = ((total < 2)
                ? vals[0]
                : vals[0] + ((vals[1] - vals[0]) * step / (total - 1)));
        switch (method) {
            case RED:
                c[0] = val;
                break;
            case GREEN:
                c[1] = val;
                break;
            case BLUE:
                c[2] = val;
                break;
            case ALPHA:
                c[3] = val;
                break;
            case SATURATION:
                int max = Math.max(Math.max(c[0], c[1]), c[2]);
                int min = max * (255 - val) / 255;
                if (c[0] == 0) {
                    c[0] = min;
                }
                if (c[1] == 0) {
                    c[1] = min;
                }
                if (c[2] == 0) {
                    c[2] = min;
                }
                break;
        }
    }
    @Override
    public void start() {
        runner = new Thread(this);
        runner.start();
    }
    @Override
    public void run() {
        canvas.setImage(null);  
        Image img = calculateImage();
        if (img != null && runner == Thread.currentThread()) {
            canvas.setImage(img);
        }
    }
    @Override
    public void stop() {
        runner = null;
    }
    @Override
    public void destroy() {
        remove(XControls);
        remove(YControls);
        remove(canvas);
    }
    @Override
    public String getAppletInfo() {
        return "An interactive demonstration of dithering.";
    }
    @Override
    public String[][] getParameterInfo() {
        String[][] info = {
            { "xaxis", "{RED, GREEN, BLUE, ALPHA, SATURATION}",
                "The color of the Y axis.  Default is RED." },
            { "yaxis", "{RED, GREEN, BLUE, ALPHA, SATURATION}",
                "The color of the X axis.  Default is BLUE." }
        };
        return info;
    }
}
@SuppressWarnings("serial")
class DitherCanvas extends Canvas {
    private Image img;
    private static String calcString = "Calculating...";
    @Override
    public void paint(Graphics g) {
        int w = getSize().width;
        int h = getSize().height;
        if (img == null) {
            super.paint(g);
            g.setColor(Color.black);
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(calcString)) / 2;
            int y = h / 2;
            g.drawString(calcString, x, y);
        } else {
            g.drawImage(img, 0, 0, w, h, this);
        }
    }
    @Override
    public void update(Graphics g) {
        paint(g);
    }
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(20, 20);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }
    public Image getImage() {
        return img;
    }
    public void setImage(Image img) {
        this.img = img;
        repaint();
    }
}
@SuppressWarnings("serial")
class DitherControls extends Panel implements ActionListener {
    private CardinalTextField start;
    private CardinalTextField end;
    private Button button;
    private Choice choice;
    private DitherTest applet;
    private static LayoutManager dcLayout = new FlowLayout(FlowLayout.CENTER,
            10, 5);
    public DitherControls(DitherTest app, int s, int e, DitherMethod type,
            boolean vertical) {
        applet = app;
        setLayout(dcLayout);
        add(new Label(vertical ? "Vertical" : "Horizontal"));
        add(choice = new Choice());
        for (DitherMethod m : DitherMethod.values()) {
            choice.addItem(m.toString().substring(0, 1)
                    + m.toString().substring(1).toLowerCase());
        }
        choice.select(type.ordinal());
        add(start = new CardinalTextField(Integer.toString(s), 4));
        add(end = new CardinalTextField(Integer.toString(e), 4));
    }
    public void addRenderButton() {
        add(button = new Button("New Image"));
        button.addActionListener(this);
    }
    public int getParams(int vals[]) {
        try {
            vals[0] = scale(Integer.parseInt(start.getText()));
        } catch (NumberFormatException nfe) {
            vals[0] = 0;
        }
        try {
            vals[1] = scale(Integer.parseInt(end.getText()));
        } catch (NumberFormatException nfe) {
            vals[1] = 255;
        }
        return choice.getSelectedIndex();
    }
    private int scale(int number) {
        if (number < 0) {
            number = 0;
        } else if (number > 255) {
            number = 255;
        }
        return number;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            applet.start();
        }
    }
}
@SuppressWarnings("serial")
class CardinalTextField extends TextField {
    String oldText = null;
    public CardinalTextField(String text, int columns) {
        super(text, columns);
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.TEXT_EVENT_MASK);
        oldText = getText();
    }
    @Override
    protected void processEvent(AWTEvent evt) {
        int id = evt.getID();
        if (id != KeyEvent.KEY_TYPED) {
            super.processEvent(evt);
            return;
        }
        KeyEvent kevt = (KeyEvent) evt;
        char c = kevt.getKeyChar();
        if (Character.isDigit(c) || (c == '\b') || (c == '\u007f')) {
            super.processEvent(evt);
            return;
        }
        Toolkit.getDefaultToolkit().beep();
        kevt.consume();
    }
    @Override
    protected void processTextEvent(TextEvent te) {
        String newText = getText();
        if (newText.equals("") || textIsCardinal(newText)) {
            oldText = newText;
            super.processTextEvent(te);
            return;
        }
        Toolkit.getDefaultToolkit().beep();
        setText(oldText);
    }
    private boolean textIsCardinal(String textToCheck) {
        try {
            return Integer.parseInt(textToCheck, 10) >= 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
