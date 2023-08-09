    public static class Attribute implements Serializable {
        private static final long serialVersionUID = -9142742483513960612L;
        public static final Attribute INPUT_METHOD_SEGMENT = new Attribute(
                "input_method_segment"); 
        public static final Attribute LANGUAGE = new Attribute("language"); 
        public static final Attribute READING = new Attribute("reading"); 
        private String name;
        protected Attribute(String name) {
            this.name = name;
        }
        @Override
        public final boolean equals(Object object) {
            return this == object;
        }
        protected String getName() {
            return name;
        }
        @Override
        public final int hashCode() {
            return super.hashCode();
        }
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Attribute.class) {
                throw new InvalidObjectException(Messages.getString("text.0C")); 
            }
            String name = this.getName();
            if (name.equals(INPUT_METHOD_SEGMENT.getName())) {
                return INPUT_METHOD_SEGMENT;
            }
            if (name.equals(LANGUAGE.getName())) {
                return LANGUAGE;
            }
            if (name.equals(READING.getName())) {
                return READING;
            }
            throw new InvalidObjectException(Messages.getString("text.02")); 
        }
        @Override
        public String toString() {
            return getClass().getName() + '(' + getName() + ')';
        }
    }
    public Set<Attribute> getAllAttributeKeys();
    public Object getAttribute(Attribute attribute);
    public Map<Attribute, Object> getAttributes();
    public int getRunLimit();
    public int getRunLimit(Attribute attribute);
    public int getRunLimit(Set<? extends Attribute> attributes);
    public int getRunStart();
    public int getRunStart(Attribute attribute);
    public int getRunStart(Set<? extends Attribute> attributes);
}
