public final class ArgumentToString {
    private ArgumentToString() {
    }
    public static void appendArgument(Object value, StringBuffer buffer) {
        if (value == null) {
            buffer.append("null");
        } else if (value instanceof String) {
            buffer.append("\"");
            buffer.append(value);
            buffer.append("\"");
        } else if (value instanceof Character) {
            buffer.append("'");
            buffer.append(value);
            buffer.append("'");
        } else if (value.getClass().isArray()) {
            buffer.append("[");
            for (int i = 0; i < Array.getLength(value); i++) {
                if (i > 0) {
                    buffer.append(", ");   
                }
                appendArgument(Array.get(value, i), buffer);
            }
            buffer.append("]");
        } else {
            buffer.append(value);
        }
    }
}
