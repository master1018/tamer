public final class Test4950122 {
    public static void main(String[] args) {
        TestBean bean = new TestBean(true, 11);
        Encoder encoder = new Encoder();
        encoder.setExceptionListener(bean);
        new TestDPD().instantiate(bean, encoder);
    }
    public static class TestDPD extends DefaultPersistenceDelegate {
        public TestDPD() {
            super(new String[] {"boolean", "integer"});
        }
        public Expression instantiate(Object oldInstance, Encoder out) {
            return super.instantiate(oldInstance, out);
        }
    }
    public static class TestBean implements ExceptionListener {
        private boolean b;
        private int i;
        public TestBean(boolean b, int i) {
            this.b = b;
            this.i = i;
        }
        public boolean isBoolean() {
            return this.b;
        }
        public int getInteger() {
            return this.i;
        }
        public void exceptionThrown(Exception exception) {
            throw new Error("unexpected exception", exception);
        }
    }
}
