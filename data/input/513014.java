public class PredicatesTest extends TestCase {
    private static final Predicate<Object> TRUE = new Predicate<Object>() {
        public boolean apply(Object o) {
            return true;
        }
    };
    private static final Predicate<Object> FALSE = new Predicate<Object>() {
        public boolean apply(Object o) {
            return false;
        }
    };
    public void testAndPredicate_AllConditionsTrue() throws Exception {
        assertTrue(Predicates.and(newArrayList(TRUE)).apply(null));
        assertTrue(Predicates.and(newArrayList(TRUE, TRUE)).apply(null));
    }
    public void testAndPredicate_AtLeastOneConditionIsFalse() throws Exception {
        assertFalse(Predicates.and(newArrayList(FALSE, TRUE, TRUE)).apply(null));
        assertFalse(Predicates.and(newArrayList(TRUE, FALSE, TRUE)).apply(null));
        assertFalse(Predicates.and(newArrayList(TRUE, TRUE, FALSE)).apply(null));
    }
    public void testOrPredicate_AllConditionsTrue() throws Exception {
        assertTrue(Predicates.or(newArrayList(TRUE, TRUE, TRUE)).apply(null));
    }
    public void testOrPredicate_AllConditionsFalse() throws Exception {
        assertFalse(Predicates.or(newArrayList(FALSE, FALSE, FALSE)).apply(null));
    }
    public void testOrPredicate_AtLeastOneConditionIsTrue() throws Exception {
        assertTrue(Predicates.or(newArrayList(TRUE, FALSE, FALSE)).apply(null));
        assertTrue(Predicates.or(newArrayList(FALSE, TRUE, FALSE)).apply(null));
        assertTrue(Predicates.or(newArrayList(FALSE, FALSE, TRUE)).apply(null));
    }
    public void testNotPredicate() throws Exception {
        assertTrue(Predicates.not(FALSE).apply(null));
        assertFalse(Predicates.not(TRUE).apply(null));
    }
    private static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<E>();
        Collections.addAll(list, elements);
        return list;
    }
}
