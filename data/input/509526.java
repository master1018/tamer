public abstract class Control {
    public static class Type {
        private String name;
        public Type(String name) {
            this.name = name;
        }
        @Override
        public boolean equals(Object another) {
            if (this == another) {
                return true;
            }
            if (another == null || !(another instanceof Type)) {
                return false;
            }
            Type obj = (Type) another;
            return name == null ? obj.name == null : name.equals(obj.name);
        }
        @Override
        public final int hashCode() {
            return name == null ? 0 : name.hashCode();
        }
        @Override
        public final String toString() {
            return name;
        }
    }
    private Type type;
    protected Control(Type type) {
        this.type = type;
    }
    public Type getType() {
        return type;
    }
    public String toString() {
        return type + " Control"; 
    }
}
