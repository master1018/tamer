public class ObjectPool implements java.io.Serializable
{
    static final long serialVersionUID = -8519013691660936643L;
  private final Class objectType;
  private final ArrayList freeStack;
  public ObjectPool(Class type)
  {
    objectType = type;
    freeStack = new ArrayList();
  }
  public ObjectPool(String className)
  {
    try
    {
      objectType = ObjectFactory.findProviderClass(
        className, ObjectFactory.findClassLoader(), true);
    }
    catch(ClassNotFoundException cnfe)
    {
      throw new WrappedRuntimeException(cnfe);
    }
    freeStack = new ArrayList();
  }
  public ObjectPool(Class type, int size)
  {
    objectType = type;
    freeStack = new ArrayList(size);
  }
  public ObjectPool()
  {
    objectType = null;
    freeStack = new ArrayList();
  }
  public synchronized Object getInstanceIfFree()
  {
    if (!freeStack.isEmpty())
    {
      Object result = freeStack.remove(freeStack.size() - 1);
      return result;
    }
    return null;
  }
  public synchronized Object getInstance()
  {
    if (freeStack.isEmpty())
    {
      try
      {
        return objectType.newInstance();
      }
      catch (InstantiationException ex){}
      catch (IllegalAccessException ex){}
      throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_EXCEPTION_CREATING_POOL, null)); 
    }
    else
    {
      Object result = freeStack.remove(freeStack.size() - 1);
      return result;
    }
  }
  public synchronized void freeInstance(Object obj)
  {
    freeStack.add(obj);
  }
}
