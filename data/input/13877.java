public class Blink extends java.applet.Applet {
    private static final long serialVersionUID = -775844794477507646L;
    private Timer timer;              
    private String labelString;       
    private int delay;                
    @Override
    public void init() {
        String blinkFrequency = getParameter("speed");
        delay = (blinkFrequency == null) ? 400
                : (1000 / Integer.parseInt(blinkFrequency));
        labelString = getParameter("lbl");
        if (labelString == null) {
            labelString = "Blink";
        }
        Font font = new java.awt.Font("Serif", Font.PLAIN, 24);
        setFont(font);
    }
    @Override
    public void start() {
        timer = new Timer();     
        timer.schedule(new TimerTask() {      
            @Override
            public void run() {
                repaint();
            }
        }, delay, delay);
    }
    @Override
    public void paint(Graphics g) {
        int fontSize = g.getFont().getSize();
        int x = 0, y = fontSize, space;
        int red = (int) (50 * Math.random());
        int green = (int) (50 * Math.random());
        int blue = (int) (256 * Math.random());
        Dimension d = getSize();
        g.setColor(Color.black);
        FontMetrics fm = g.getFontMetrics();
        space = fm.stringWidth(" ");
        for (StringTokenizer t = new StringTokenizer(labelString);
                t.hasMoreTokens();) {
            String word = t.nextToken();
            int w = fm.stringWidth(word) + space;
            if (x + w > d.width) {
                x = 0;
                y += fontSize;  
            }
            if (Math.random() < 0.5) {
                g.setColor(new java.awt.Color((red + y * 30) % 256,
                        (green + x / 3) % 256, blue));
            } else {
                g.setColor(getBackground());
            }
            g.drawString(word, x, y);
            x += w;  
        }
    }
    @Override
    public void stop() {
        timer.cancel();  
    }
    @Override
    public String getAppletInfo() {
        return "Title: Blinker\n"
                + "Author: Arthur van Hoff\n"
                + "Displays multicolored blinking text.";
    }
    @Override
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            { "speed", "string", "The blink frequency" },
            { "lbl", "string", "The text to blink." }, };
        return pinfo;
    }
}
