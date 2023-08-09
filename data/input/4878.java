public final class Test6176120 extends AbstractTest {
    public static void main(String[] args) {
        new Test6176120().test(true);
    }
    protected ImmutableBean getObject() {
        return new ImmutableBean(1, -1);
    }
    protected ImmutableBean getAnotherObject() {
        return null; 
    }
    public static final class ImmutableBean {
        private final int x;
        private final int y;
        @ConstructorProperties({"x", "y"})
        public ImmutableBean( int x, int y ) {
            this.x = x;
            this.y = y;
        }
        public int getX() {
            return this.x;
        }
        public int getY() {
            return this.y;
        }
    }
}
