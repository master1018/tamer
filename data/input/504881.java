class EmptyImmutableListMultimap extends ImmutableListMultimap<Object, Object> {
  static final EmptyImmutableListMultimap INSTANCE
      = new EmptyImmutableListMultimap();
  private EmptyImmutableListMultimap() {
    super(ImmutableMap.<Object, ImmutableList<Object>>of(), 0);
  }
  private Object readResolve() {
    return INSTANCE; 
  }
  private static final long serialVersionUID = 0;
}
