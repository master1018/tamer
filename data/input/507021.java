public class FieldByNameComparator<T extends IMemberDelta<?>> implements
        Comparator<T> {
    public int compare(T a, T b) {
        assert a.getType() == b.getType();
        IField aField = null;
        IField bField = null;
        if (a.getFrom() != null) {
            aField = (IField) a.getFrom();
            bField = (IField) b.getFrom();
        } else {
            aField = (IField) a.getTo();
            bField = (IField) b.getTo();
        }
        return aField.getName().compareTo(bField.getName());
    }
}
