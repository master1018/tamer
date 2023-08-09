public class SortItem extends java.applet.Applet implements Runnable,
        MouseListener {
    private Thread kicker;
    int arr[];
    int h1 = -1;
    int h2 = -1;
    String algName;
    SortAlgorithm algorithm;
    Dimension initialSize = null;
    void scramble() {
        initialSize = getSize();
        int a[] = new int[initialSize.height / 2];
        double f = initialSize.width / (double) a.length;
        for (int i = a.length; --i >= 0;) {
            a[i] = (int) (i * f);
        }
        for (int i = a.length; --i >= 0;) {
            int j = (int) (i * Math.random());
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
        arr = a;
    }
    void pause() {
        pause(-1, -1);
    }
    void pause(int H1) {
        pause(H1, -1);
    }
    void pause(int H1, int H2) {
        h1 = H1;
        h2 = H2;
        if (kicker != null) {
            repaint();
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
        }
    }
    @Override
    public void init() {
        String at = getParameter("alg");
        if (at == null) {
            at = "BubbleSort";
        }
        algName = at + "Algorithm";
        scramble();
        resize(100, 100);
        addMouseListener(this);
    }
    @Override
    public void start() {
        h1 = h2 = -1;
        scramble();
        repaint();
        showStatus(getParameter("alg"));
    }
    @Override
    public void destroy() {
        removeMouseListener(this);
    }
    @Override
    public void paint(Graphics g) {
        int a[] = arr;
        int y = 0;
        int deltaY = 0, deltaX = 0, evenY = 0;
        Dimension currentSize = getSize();
        int currentHeight = currentSize.height;
        int currentWidth = currentSize.width;
        if (!currentSize.equals(initialSize)) {
            evenY = (currentHeight - initialSize.height) % 2;
            deltaY = (currentHeight - initialSize.height) / 2;
            deltaX = (currentWidth - initialSize.width) / 2;
            if (deltaY < 0) {
                deltaY = 0;
                evenY = 0;
            }
            if (deltaX < 0) {
                deltaX = 0;
            }
        }
        g.setColor(getBackground());
        y = currentHeight - deltaY - 1;
        for (int i = a.length; --i >= 0; y -= 2) {
            g.drawLine(deltaX + arr[i], y, currentWidth, y);
        }
        g.setColor(Color.black);
        y = currentHeight - deltaY - 1;
        for (int i = a.length; --i >= 0; y -= 2) {
            g.drawLine(deltaX, y, deltaX + arr[i], y);
        }
        if (h1 >= 0) {
            g.setColor(Color.red);
            y = deltaY + evenY + h1 * 2 + 1;
            g.drawLine(deltaX, y, deltaX + initialSize.width, y);
        }
        if (h2 >= 0) {
            g.setColor(Color.blue);
            y = deltaY + evenY + h2 * 2 + 1;
            g.drawLine(deltaX, y, deltaX + initialSize.width, y);
        }
    }
    @Override
    public void update(Graphics g) {
        paint(g);
    }
    @Override
    public void run() {
        try {
            if (algorithm == null) {
                algorithm = (SortAlgorithm) Class.forName(algName).newInstance();
                algorithm.setParent(this);
            }
            algorithm.init();
            algorithm.sort(arr);
        } catch (Exception e) {
        }
    }
    @Override
    public synchronized void stop() {
        if (algorithm != null) {
            try {
                algorithm.stop();
            } catch (IllegalThreadStateException e) {
            }
            kicker = null;
        }
    }
    private synchronized void startSort() {
        if (kicker == null || !kicker.isAlive()) {
            kicker = new Thread(this);
            kicker.start();
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        showStatus(getParameter("alg"));
    }
    @Override
    public void mousePressed(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        startSort();
        e.consume();
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public String getAppletInfo() {
        return "Title: SortDemo \nAuthor: James Gosling 1.17f, 10 Apr 1995 \nA simple applet class to demonstrate a sort algorithm.  \nYou can specify a sorting algorithm using the 'alg' attribute.  \nWhen you click on the applet, a thread is forked which animates \nthe sorting algorithm.";
    }
    @Override
    public String[][] getParameterInfo() {
        String[][] info = {
            { "alg", "string",
                "The name of the algorithm to run.  You can choose from the provided algorithms or suppply your own, as long as the classes are runnable as threads and their names end in 'Algorithm.'  BubbleSort is the default.  Example:  Use 'QSort' to run the QSortAlgorithm class." }
        };
        return info;
    }
}
