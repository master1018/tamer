public final class Test6505888 extends AbstractTest {
    public static void main(String[] args) {
        new Test6505888().test(true);
    }
    protected ListBean getObject() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(Integer.valueOf(26));
        list.add(Integer.valueOf(10));
        list.add(Integer.valueOf(74));
        return new ListBean(list);
    }
    protected ListBean getAnotherObject() {
        return null; 
    }
    public static final class ListBean {
        private List<Integer> list;
        @ConstructorProperties("list")
        public ListBean(List<Integer> list) {
            this.list = new ArrayList<Integer>(list);
        }
        public List<Integer> getList() {
            return Collections.unmodifiableList(this.list);
        }
    }
}
