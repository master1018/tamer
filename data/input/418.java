public abstract class Control {
    private final Type type;
    protected Control(Type type) {
        this.type = type;
    }
    public Type getType() {
        return type;
    }
    public String toString() {
        return new String(getType() + " Control");
    }
    public static class Type {
        private String name;
        protected Type(String name) {
            this.name = name;
        }
        public final boolean equals(Object obj) {
            return super.equals(obj);
        }
        public final int hashCode() {
            return super.hashCode();
        }
        public final String toString() {
            return name;
        }
    } 
} 
