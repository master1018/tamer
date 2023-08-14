public class MethodCallable implements Callable {
  private Object target;
  private Method method;
  private boolean wrapArgs;
  public MethodCallable(Object target, Method method, boolean wrapArgs) {
    this.method = method;
    this.target = target;
    this.wrapArgs = wrapArgs;
  }
  public MethodCallable(Object target, Method method) {
    this(target, method, true);
  }
  public Object call(Object[] args) throws ScriptException {
    try {
      if (wrapArgs) {
        return method.invoke(target, new Object[] { args });
      } else {
        return method.invoke(target, args);
      }
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception exp) {
      throw new ScriptException(exp);
    }
  }
}
