public class TestX11JIS0201 {
    public static void main(String args[])
        throws Exception
    {
        test();
    }
    private static void test()
        throws Exception
    {
        Class cl = null;
        try {
            cl = Class.forName("sun.awt.motif.X11JIS0201");
        } catch (Exception e){
            return;
        }
        Charset cs = (Charset)cl.newInstance();
        if (! cs.name().equals("X11JIS0201")){
            throw new Exception("X11JIS0201 does not work correctly");
        }
    }
}
