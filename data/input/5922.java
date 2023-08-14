public class OutputViewer {
    private static JFrame frame;
    private static JTextArea ta;
    static {
        System.setOut(PipeListener.create("System.out"));
        System.setErr(PipeListener.create("System.err"));
    }
    public static void init() { }
    private static void append(String s) {
        if (frame == null) {
            frame = new JFrame("JConsole: Output");
            ta = new JTextArea();
            ta.setEditable(false);
            frame.getContentPane().add(new JScrollPane(ta));
            ta.setFont(new Font("Monospaced", Font.BOLD, 14));
            frame.setSize(500, 600);
            frame.setLocation(1024-500, 768-600);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (JFrame.getFrames().length == 1) {
                        System.exit(0);
                    }
                }
            });
        }
        ta.append(s);
        ta.setCaretPosition(ta.getText().length());
        frame.setVisible(true);
    }
    private static void appendln(String s) {
        append(s+"\n");
    }
    private static class PipeListener extends Thread {
        public PrintStream ps;
        private String name;
        private PipedInputStream inPipe;
        private BufferedReader br;
        public static PrintStream create(String name) {
            return new PipeListener(name).ps;
        }
        private PipeListener(String name) {
            this.name = name;
            try {
                inPipe = new PipedInputStream();
                ps = new PrintStream(new PipedOutputStream(inPipe));
                br = new BufferedReader(new InputStreamReader(inPipe));
            } catch (IOException e) {
                appendln("PipeListener<init>("+name+"): " + e);
            }
            start();
        }
        public void run() {
            try {
                String str;
                while ((str = br.readLine()) != null) {
                    appendln(str);
                    try {
                        java.lang.reflect.Field f =
                            PipedInputStream.class.getDeclaredField("writeSide");
                        f.setAccessible(true);
                        f.set(inPipe, this);
                    } catch (Exception e) {
                        appendln("PipeListener("+name+").run: "+e);
                    }
                }
                appendln("-- "+name+" closed --");
                br.close();
            } catch (IOException e) {
                appendln("PipeListener("+name+").run: "+e);
            }
        }
    }
}
