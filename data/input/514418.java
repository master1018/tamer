public class LineEvent extends EventObject {
    private static final long serialVersionUID = -1274246333383880410L;
    private LineEvent.Type type;
    private long position;
    public static class Type {
        public static final Type CLOSE = new Type("Close"); 
        public static final Type OPEN = new Type("Open"); 
        public static final Type START = new Type("Start"); 
        public static final Type STOP = new Type("Stop"); 
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
        public String toString() {
            return name;
        }
    }
    public LineEvent(Line line, LineEvent.Type type, long position) {
        super(line);
        this.type = type;
        this.position = position;
    }
    public final Line getLine() {
        return (Line)getSource();
    }
    public final LineEvent.Type getType() {
        return type;
    }
    public final long getFramePosition() {
        return position;
    }
    public String toString() {
        return type + " event from line " + getLine(); 
    }
}
