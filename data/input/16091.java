class AllRootsQuery extends QueryHandler {
    public AllRootsQuery() {
    }
    public void run() {
        startHtml("All Members of the Rootset");
        Root[] roots = snapshot.getRootsArray();
        ArraySorter.sort(roots, new Comparer() {
            public int compare(Object lhs, Object rhs) {
                Root left = (Root) lhs;
                Root right = (Root) rhs;
                int d = left.getType() - right.getType();
                if (d != 0) {
                    return -d;  
                }
                return left.getDescription().compareTo(right.getDescription());
            }
        });
        int lastType = Root.INVALID_TYPE;
        for (int i= 0; i < roots.length; i++) {
            Root root = roots[i];
            if (root.getType() != lastType) {
                lastType = root.getType();
                out.print("<h2>");
                print(root.getTypeName() + " References");
                out.println("</h2>");
            }
            printRoot(root);
            if (root.getReferer() != null) {
                out.print("<small> (from ");
                printThingAnchorTag(root.getReferer().getId());
                print(root.getReferer().toString());
                out.print(")</a></small>");
            }
            out.print(" :<br>");
            JavaThing t = snapshot.findThing(root.getId());
            if (t != null) {    
                print("--> ");
                printThing(t);
                out.println("<br>");
            }
        }
        out.println("<h2>Other Queries</h2>");
        out.println("<ul>");
        out.println("<li>");
        printAnchorStart();
        out.print("\">");
        print("Show All Classes");
        out.println("</a>");
        out.println("</ul>");
        endHtml();
    }
}
