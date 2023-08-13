public class OwnedWindowsLeak
{
    public static void main(String[] args)
    {
        Frame owner = new Frame("F");
        Vector<WeakReference<Window>> children =
            new Vector<WeakReference<Window>>();
        for (int i = 0; i < 1000; i++)
        {
            Window child = new Window(owner);
            children.add(new WeakReference<Window>(child));
        }
        Vector garbage = new Vector();
        while (true)
        {
            try
            {
                garbage.add(new byte[1000]);
            }
            catch (OutOfMemoryError e)
            {
                break;
            }
        }
        garbage = null;
        for (WeakReference<Window> ref : children)
        {
            if (ref.get() != null)
            {
                throw new RuntimeException("Test FAILED: some of child windows are not GCed");
            }
        }
        try
        {
            Field f = Window.class.getDeclaredField("ownedWindowList");
            f.setAccessible(true);
            Vector ownersChildren = (Vector)f.get(owner);
            if (ownersChildren.size() > 0)
            {
                throw new RuntimeException("Test FAILED: some of the child windows are not removed from owner's children list");
            }
        }
        catch (NoSuchFieldException z)
        {
            System.out.println("Test PASSED: no 'ownedWindowList' field in Window class");
            return;
        }
        catch (Exception z)
        {
            throw new RuntimeException("Test FAILED: unexpected exception", z);
        }
        System.out.println("Test PASSED");
        owner.dispose();
    }
}
