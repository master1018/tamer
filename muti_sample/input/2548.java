    public static class Attribute implements Serializable {
        private String name;
        private static final Map instanceMap = new HashMap(7);
        protected Attribute(String name) {
            this.name = name;
            if (this.getClass() == Attribute.class) {
                instanceMap.put(name, this);
            }
        }
        public final boolean equals(Object obj) {
            return super.equals(obj);
        }
        public final int hashCode() {
            return super.hashCode();
        }
        public String toString() {
            return getClass().getName() + "(" + name + ")";
        }
        protected String getName() {
            return name;
        }
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Attribute.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            Attribute instance = (Attribute) instanceMap.get(getName());
            if (instance != null) {
                return instance;
            } else {
                throw new InvalidObjectException("unknown attribute name");
            }
        }
        public static final Attribute LANGUAGE = new Attribute("language");
        public static final Attribute READING = new Attribute("reading");
        public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");
        private static final long serialVersionUID = -9142742483513960612L;
    };
    public int getRunStart();
    public int getRunStart(Attribute attribute);
    public int getRunStart(Set<? extends Attribute> attributes);
    public int getRunLimit();
    public int getRunLimit(Attribute attribute);
    public int getRunLimit(Set<? extends Attribute> attributes);
    public Map<Attribute,Object> getAttributes();
    public Object getAttribute(Attribute attribute);
    public Set<Attribute> getAllAttributeKeys();
};
