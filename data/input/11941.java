public class BeanContextMembershipEvent extends BeanContextEvent {
    private static final long serialVersionUID = 3499346510334590959L;
    public BeanContextMembershipEvent(BeanContext bc, Collection changes) {
        super(bc);
        if (changes == null) throw new NullPointerException(
            "BeanContextMembershipEvent constructor:  changes is null.");
        children = changes;
    }
    public BeanContextMembershipEvent(BeanContext bc, Object[] changes) {
        super(bc);
        if (changes == null) throw new NullPointerException(
            "BeanContextMembershipEvent:  changes is null.");
        children = Arrays.asList(changes);
    }
    public int size() { return children.size(); }
    public boolean contains(Object child) {
        return children.contains(child);
    }
    public Object[] toArray() { return children.toArray(); }
    public Iterator iterator() { return children.iterator(); }
    protected Collection children;
}
