public class PosKeyboardFocusManager extends DefaultKeyboardFocusManager implements ActionListener {
    public PosKeyboardFocusManager() {
        super();
    }
    private LinkedList<KeyEvent> m_fifo = new LinkedList<KeyEvent>();
    private long m_lastWhen = 0;
    private javax.swing.Timer m_timer = null;
    private static CLogger log = CLogger.getCLogger(PosKeyboardFocusManager.class);
    public void dispose() {
        if (m_timer != null) m_timer.stop();
        m_timer = null;
        if (m_fifo != null) m_fifo.clear();
        m_fifo = null;
    }
    public void start() {
        int delay = 200;
        log.fine("PosKeyboardFocusManager.start - " + delay);
        if (m_timer == null) m_timer = new javax.swing.Timer(delay, this);
        if (!m_timer.isRunning()) m_timer.start();
    }
    public void stop() {
        log.fine("PosKeyboardFocusManager.stop - " + m_timer);
        if (m_timer != null) m_timer.stop();
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            m_lastWhen = event.getWhen();
        }
        if (m_timer == null) super.dispatchKeyEvent(event); else m_fifo.add(event);
        return true;
    }
    public void actionPerformed(ActionEvent e) {
        if (m_timer == null) return;
        while (m_fifo.size() > 0) {
            KeyEvent event = (KeyEvent) m_fifo.removeFirst();
            super.dispatchKeyEvent(event);
        }
    }
}
