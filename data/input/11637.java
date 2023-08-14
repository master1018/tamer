public class FilenameFilterTest extends Applet
{
    volatile boolean filter_was_called = false;
    FileDialog fd;
    public void init()
    {
        setLayout (new BorderLayout ());
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    fd = new FileDialog(new Frame(""), "hello world", FileDialog.LOAD);
                    fd.setFilenameFilter(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                filter_was_called = true;
                                System.out.println(Thread.currentThread() + " name = " + name );
                                return true;
                            }
                        });
                    fd.setDirectory(System.getProperty("test.src"));
                    fd.setVisible(true);
                }
            });
        Util.waitForIdle(null);
        if (fd == null) {
            throw new RuntimeException("fd is null (very unexpected thing :(");
        }
        try {
            Thread.sleep(5000);
        } catch (Exception ex) {
        }
        fd.dispose();
        if (!filter_was_called) {
            throw new RuntimeException("Filter was not called");
        }
    }
}
