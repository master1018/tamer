public class CNCtxFactory implements InitialContextFactory {
  public Context getInitialContext(Hashtable<?,?> env) throws NamingException {
      return new CNCtx(env);
  }
}
