    public static class Result {
        private Object obj;
        private Attributes attrs;
        public Result(Object obj, Attributes outAttrs) {
            this.obj = obj;
            this.attrs = outAttrs;
        }
        public Object getObject() { return obj; };
        public Attributes getAttributes() { return attrs; };
    }
}
