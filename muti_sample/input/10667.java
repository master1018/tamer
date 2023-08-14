public class NervousText extends Applet implements Runnable, MouseListener {
    String banner;              
    char bannerChars[];         
    char attributes[];          
    Thread runner = null;       
    boolean threadSuspended;    
    static final int REGULAR_WD = 15;
    static final int REGULAR_HT = 36;
    static final int SMALL_WD = 12;
    static final int SMALL_HT = 24;
    Font regularFont = new Font("Serif", Font.BOLD, REGULAR_HT);
    Font smallFont = new Font("Serif", Font.BOLD, SMALL_HT);
    @Override
    public void init() {
        banner = getParameter("text");
        if (banner == null) {
            banner = "HotJava";
        }
        int bannerLength = banner.length();
        StringBuilder bc = new StringBuilder(bannerLength);
        StringBuilder attrs = new StringBuilder(bannerLength);
        int wd = 0;
        for (int i = 0; i < bannerLength; i++) {
            char c = banner.charAt(i);
            char a = 0;
            if (c == '^') {
                i++;
                if (i < bannerLength) {
                    c = banner.charAt(i);
                    a = '^';
                    wd += SMALL_WD - REGULAR_WD;
                } else {
                    break;
                }
            }
            bc.append(c);
            attrs.append(a);
            wd += REGULAR_WD;
        }
        bannerLength = bc.length();
        bannerChars = new char[bannerLength];
        attributes = new char[bannerLength];
        bc.getChars(0, bannerLength, bannerChars, 0);
        attrs.getChars(0, bannerLength, attributes, 0);
        threadSuspended = false;
        resize(wd + 10, 50);
        addMouseListener(this);
    }
    @Override
    public void destroy() {
        removeMouseListener(this);
    }
    @Override
    public void start() {
        runner = new Thread(this);
        runner.start();
    }
    @Override
    public synchronized void stop() {
        runner = null;
        if (threadSuspended) {
            threadSuspended = false;
            notify();
        }
    }
    @Override
    public void run() {
        Thread me = Thread.currentThread();
        while (runner == me) {
            try {
                Thread.sleep(100);
                synchronized (this) {
                    while (threadSuspended) {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
            }
            repaint();
        }
    }
    @Override
    public void paint(Graphics g) {
        int length = bannerChars.length;
        for (int i = 0, x = 0; i < length; i++) {
            int wd, ht;
            if (attributes[i] == '^') {
                wd = SMALL_WD;
                ht = SMALL_HT;
                g.setFont(smallFont);
            } else {
                wd = REGULAR_WD;
                ht = REGULAR_HT;
                g.setFont(regularFont);
            }
            int px = (int) (10 * Math.random() + x);
            int py = (int) (10 * Math.random() + ht);
            g.drawChars(bannerChars, i, 1, px, py);
            x += wd;
        }
    }
    @Override
    public synchronized void mousePressed(MouseEvent e) {
        e.consume();
        threadSuspended = !threadSuspended;
        if (!threadSuspended) {
            notify();
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {
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
    public String getAppletInfo() {
        return "Title: NervousText\nAuthor: Daniel Wyszynski\n"
                + "Displays a text banner that jitters.";
    }
    @Override
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            { "text", "string", "Text to display" }, };
        return pinfo;
    }
}
