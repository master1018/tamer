public class InvocableCallable implements Callable {
  private Object target;
  private String name;
  private Invocable invocable;
  public InvocableCallable(Object target, String name,
    Invocable invocable) {
    this.target = target;
    this.name = name;
    this.invocable = invocable;
  }
  public Object call(Object[] args) throws ScriptException {
    try {
      if (target == null) {
        return invocable.invokeFunction(name, args);
      } else {
        return invocable.invokeMethod(target, name, args);
      }
    } catch (NoSuchMethodException nme) {
      throw new ScriptException(nme);
    }
  }
}
