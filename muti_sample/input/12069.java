public class SmallEnumIteratorRemoveResilience {
    private static enum SmallEnum { e0, e1, e2 }
    public static void main(final String[] args) throws Exception {
        final Set<SmallEnum> set = EnumSet.noneOf(SmallEnum.class);
        set.add(SmallEnum.e0);
        set.add(SmallEnum.e1);
        final Iterator<SmallEnum> iterator = set.iterator();
        int size = set.size();
        SmallEnum element = iterator.next();
        iterator.remove();
        checkSetAfterRemoval(set, size, element);
        size = set.size();
        element = iterator.next();
        set.remove(element);
        checkSetAfterRemoval(set, size, element);
        iterator.remove();
        checkSetAfterRemoval(set, size, element);
    }
    private static void checkSetAfterRemoval(final Set<SmallEnum> set,
            final int origSize, final SmallEnum removedElement)
            throws Exception {
        if (set.size() != (origSize - 1)) {
            throw new Exception("Test FAILED: Unexpected set size after removal; expected '" + (origSize - 1) + "' but found '" + set.size() + "'");
        }
        if (set.contains(removedElement)) {
            throw new Exception("Test FAILED: Element returned from iterator unexpectedly still in set after removal.");
        }
    }
}
