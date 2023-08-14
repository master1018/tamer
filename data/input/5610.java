public class ArgumentSizeComputer extends SignatureInfo {
  protected void set(int size, int type)        { if (!isReturnType()) this.size += size; }
  public ArgumentSizeComputer(Symbol signature) { super(signature); }
}
