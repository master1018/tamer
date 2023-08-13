public class ResultTypeFinder extends SignatureInfo {
  protected void set(int size, int type)    { if (isReturnType()) this.type = type; }
  public ResultTypeFinder(Symbol signature) { super(signature); }
}
