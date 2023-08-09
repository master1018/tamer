public class LookupswitchPair extends Bytecode {
  LookupswitchPair(Method method, int bci) {
    super(method, bci);
  }
  public int match() {
    return javaSignedWordAt(0 * jintSize);
  }
  public int offset() {
    return javaSignedWordAt(1 * jintSize);
  }
}
