public class LogAlignmentTest
    extends LogHandler
    implements Serializable 
{
    static public void main (String[] argv)
    {
        LogAlignmentTest test = new LogAlignmentTest();
        test.run (argv, System.err, System.out);
    }
    public void run (String argv[], PrintStream log, PrintStream out)
    {
        try {
            regtest(130, "./logalign_tmp", log, out);
            regtest(131, "./logalign_tmp", log, out);
        } catch (Exception e) {
            e.printStackTrace (log);
            throw (new RuntimeException
                    ("Exception in regression test for bugid 4094889"));
        }
    }
    static private void regtest(int updatesz, String dir, PrintStream lg, PrintStream out)
        throws Exception
    {
        try {
            LogAlignmentTest handler = new LogAlignmentTest();
            ReliableLog log = new ReliableLog (dir, handler, false);
            String c = "[";
            handler.basicUpdate (c);
            log.update (c, true);
            char[] up = new char[updatesz];
            int i;
            for (i = 0; i < updatesz; i++) {
                up[i] = (char)(65 + (i % 26));
            }
            c = new String (up);
            handler.basicUpdate (c);
            log.update (c, true);
            handler.basicUpdate (c);
            log.update (c, true);
            c = "]";
            handler.basicUpdate (c);
            log.update (c, true);
            LogAlignmentTest handler2 = new LogAlignmentTest();
            ReliableLog carbon = new ReliableLog (dir, handler2, false);
            Object thingy = carbon.recover();
            String orig = handler.contents;
            String news = ((LogAlignmentTest)thingy).contents;
            lg.println ("Original as saved: " + orig);
            lg.println ("As restored      : " + news);
            if (orig.compareTo (news) != 0) {
                throw new RuntimeException ("Restored string was different from saved string");
            } else {
                lg.println ("Matched OK.  Test element passed.");
            }
        } finally {
            try {
                File vs = new File (dir, "Version_Number");
                vs.delete();
            } catch (Exception e) {
            }
            try {
                File vs = new File (dir, "New_Version_Number");
                vs.delete();
            } catch (Exception e) {
            }
        }
    }
    private String contents;
    public LogAlignmentTest()
    {
        super();
        this.contents = "?";
    }
    public Object initialSnapshot()
        throws Exception
    {
        this.contents = "";
        return (this);
    }
    public Object applyUpdate (Object update, Object state)
        throws Exception
    {
        ((LogAlignmentTest)state).basicUpdate ((String)update);
        return (state);
    }
    public void basicUpdate (String extra)
    {
        this.contents = this.contents + extra;
    }
}
