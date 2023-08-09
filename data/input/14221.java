public class Test4935607 extends AbstractTest<Test4935607.TransientBean> {
    public static void main(String[] args) {
        new Test4935607().test(true);
    }
    @Override
    protected TransientBean getObject() {
        TransientBean bean = new TransientBean();
        bean.setName("some string"); 
        return bean;
    }
    @Override
    protected TransientBean getAnotherObject() {
        TransientBean bean = new TransientBean();
        bean.setName("another string"); 
        bean.setComment("some comment"); 
        return bean;
    }
    @Override
    protected void validate(TransientBean before, TransientBean after) {
        if (!before.getName().equals(after.getName()))
            throw new Error("the name property incorrectly encoded");
        if (null != after.getComment())
            throw new Error("the comment property should be encoded");
    }
    public static class TransientBean {
        private String name;
        private String comment;
        public String getName() {
            return this.name;
        }
        public void setName(String name) {
            this.name = name;
        }
        @Transient
        public String getComment() {
            return this.comment;
        }
        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
