public abstract class DOMService
{
    public static DOMService getService(Object obj)
                  throws DOMUnsupportedException
    {
        try
        {
            String provider = (String) java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction("com.sun.java.browser.dom.DOMServiceProvider"));
            Class clazz = DOMService.class.forName("sun.plugin.dom.DOMService");
            return (DOMService) clazz.newInstance();
        }
        catch (Throwable e)
        {
            throw new DOMUnsupportedException(e.toString());
        }
    }
    public DOMService()
    {
    }
    public abstract Object invokeAndWait(DOMAction action) throws DOMAccessException;
    public abstract void invokeLater(DOMAction action);
}
