class EmptyImmutableSetMultimap extends ImmutableSetMultimap<Object, Object> {
  static final EmptyImmutableSetMultimap INSTANCE
      = new EmptyImmutableSetMultimap();
  private EmptyImmutableSetMultimap() {
    super(ImmutableMap.<Object, ImmutableSet<Object>>of(), 0);
  }
  private Object readResolve() {
    return INSTANCE; 
  }
  private static final long serialVersionUID = 0;
}
