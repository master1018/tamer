public class CompoundEnumeration<E> implements Enumeration<E> {
    private Enumeration[] enums;
    private int index = 0;
    public CompoundEnumeration(Enumeration[] enums) {
        this.enums = enums;
    }
    private boolean next() {
        while (index < enums.length) {
            if (enums[index] != null && enums[index].hasMoreElements()) {
                return true;
            }
            index++;
        }
        return false;
    }
    public boolean hasMoreElements() {
        return next();
    }
    public E nextElement() {
        if (!next()) {
            throw new NoSuchElementException();
        }
        return (E)enums[index].nextElement();
    }
}
