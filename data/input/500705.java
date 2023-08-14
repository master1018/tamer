public class ClassByNameComparator implements
        Comparator<IClassDefinitionDelta> {
    public int compare(IClassDefinitionDelta a, IClassDefinitionDelta b) {
        assert a.getType() == b.getType();
        if (a.getFrom() != null) {
            return a.getFrom().getName().compareTo(b.getFrom().getName());
        } else {
            return a.getTo().getName().compareTo(b.getTo().getName());
        }
    }
}
