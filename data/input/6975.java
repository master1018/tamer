public class HeaderClosure implements Closure {
    private static final char ALIGN_CHAR = '^';
    private StringBuilder header = new StringBuilder();
    public void visit(Object o, boolean hasNext) throws MonitorException {
        if (! (o instanceof ColumnFormat)) {
            return;
        }
        ColumnFormat c = (ColumnFormat)o;
        String h = c.getHeader();
        if (h.indexOf(ALIGN_CHAR) >= 0) {
            int len = h.length();
            if ((h.charAt(0) == ALIGN_CHAR)
                    && (h.charAt(len-1) == ALIGN_CHAR)) {
                c.setWidth(Math.max(c.getWidth(),
                                    Math.max(c.getFormat().length(), len-2)));
                h = h.substring(1, len-1);
                h = Alignment.CENTER.align(h, c.getWidth());
            } else if (h.charAt(0) == ALIGN_CHAR) {
                c.setWidth(Math.max(c.getWidth(),
                                    Math.max(c.getFormat().length(), len-1)));
                h = h.substring(1, len);
                h = Alignment.LEFT.align(h, c.getWidth());
            } else if (h.charAt(len-1) == ALIGN_CHAR) {
                c.setWidth(Math.max(c.getWidth(),
                           Math.max(c.getFormat().length(), len-1)));
                h = h.substring(0, len-1);
                h = Alignment.RIGHT.align(h, c.getWidth());
            } else {
            }
        } else {
        }
        header.append(h);
        if (hasNext) {
            header.append(" ");
        }
    }
    public String getHeader() {
        return header.toString();
    }
}
