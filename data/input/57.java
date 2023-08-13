public class VMObjectFactory {
  public static Object newObject(Class clazz, Address addr)
    throws ConstructionException {
    try {
      if (addr == null) {
        return null;
      }
      Constructor c = clazz.getConstructor(new Class[] {
        Address.class
      });
      return c.newInstance(new Object[] { addr });
    }
    catch (Exception e) {
      throw new ConstructionException(e);
    }
  }
}
