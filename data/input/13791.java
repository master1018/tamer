class RootStackQuery extends QueryHandler {
    public RootStackQuery() {
    }
    public void run() {
        int index = (int) parseHex(query);
        Root root = snapshot.getRootAt(index);
        if (root == null) {
            error("Root at " + index + " not found");
            return;
        }
        StackTrace st = root.getStackTrace();
        if (st == null || st.getFrames().length == 0) {
            error("No stack trace for " + root.getDescription());
            return;
        }
        startHtml("Stack Trace for " + root.getDescription());
        out.println("<p>");
        printStackTrace(st);
        out.println("</p>");
        endHtml();
    }
}
