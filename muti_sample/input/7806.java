public class SecurityTest2
{
    public static void main(String[] args)
    {
        System.setSecurityManager(new SecurityManager());
        try
        {
            Frame f = new Frame();
            f.setVisible(true);
            f.dispose();
        }
        catch (Exception z)
        {
            throw new RuntimeException("Test FAILED because of some Exception thrown", z);
        }
    }
}
