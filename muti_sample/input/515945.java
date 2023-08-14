final class EmptyImmutableMultiset extends ImmutableMultiset<Object> {
  static final EmptyImmutableMultiset INSTANCE = new EmptyImmutableMultiset();
  private EmptyImmutableMultiset() {
    super(ImmutableMap.<Object, Integer>of(), 0);
  }
  Object readResolve() {
    return INSTANCE; 
  }
  private static final long serialVersionUID = 0;
}
