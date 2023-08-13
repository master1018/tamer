    public static final class EventType {
        private EventType(String s) {
            typeString = s;
        }
        public static final EventType INSERT = new EventType("INSERT");
        public static final EventType REMOVE = new EventType("REMOVE");
        public static final EventType CHANGE = new EventType("CHANGE");
        public String toString() {
            return typeString;
        }
        private String typeString;
    }
    public interface ElementChange {
        public Element getElement();
        public int getIndex();
        public Element[] getChildrenRemoved();
        public Element[] getChildrenAdded();
    }
}
