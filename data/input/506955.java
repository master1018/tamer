public class NavTree {
    public static void writeNavTree(String dir) {
        ArrayList<Node> children = new ArrayList();
        for (PackageInfo pkg: DroidDoc.choosePackages()) {
            children.add(makePackageNode(pkg));
        }
        Node node = new Node("Reference", dir + "packages.html", children, null);
        StringBuilder buf = new StringBuilder();
        if (false) {
            buf.append("[");
            node.render(buf);
            buf.append("]");
        } else {
            node.renderChildren(buf);
        }
        HDF data = DroidDoc.makeHDF();
        data.setValue("reference_tree", buf.toString());
        ClearPage.write(data, "navtree_data.cs", "navtree_data.js");
    }
    private static Node makePackageNode(PackageInfo pkg) {
        ArrayList<Node> children = new ArrayList();
        children.add(new Node("Description", pkg.fullDescriptionHtmlPage(), null, null));
        addClassNodes(children, "Interfaces", pkg.interfaces());
        addClassNodes(children, "Classes", pkg.ordinaryClasses());
        addClassNodes(children, "Enums", pkg.enums());
        addClassNodes(children, "Exceptions", pkg.exceptions());
        addClassNodes(children, "Errors", pkg.errors());
        return new Node(pkg.name(), pkg.htmlPage(), children, pkg.getSince());
    }
    private static void addClassNodes(ArrayList<Node> parent, String label, ClassInfo[] classes) {
        ArrayList<Node> children = new ArrayList();
        for (ClassInfo cl: classes) {
            if (cl.checkLevel()) {
                children.add(new Node(cl.name(), cl.htmlPage(), null, cl.getSince()));
            }
        }
        if (children.size() > 0) {
            parent.add(new Node(label, null, children, null));
        }
    }
    private static class Node {
        private String mLabel;
        private String mLink;
        ArrayList<Node> mChildren;
        private String mSince;
        Node(String label, String link, ArrayList<Node> children, String since) {
            mLabel = label;
            mLink = link;
            mChildren = children;
            mSince = since;
        }
        static void renderString(StringBuilder buf, String s) {
            if (s == null) {
                buf.append("null");
            } else {
                buf.append('"');
                final int N = s.length();
                for (int i=0; i<N; i++) {
                    char c = s.charAt(i);
                    if (c >= ' ' && c <= '~' && c != '"' && c != '\\') {
                        buf.append(c);
                    } else {
                        buf.append("\\u");
                        for (int j=0; i<4; i++) {
                            char x = (char)(c & 0x000f);
                            if (x > 10) {
                                x = (char)(x - 10 + 'a');
                            } else {
                                x = (char)(x + '0');
                            }
                            buf.append(x);
                            c >>= 4;
                        }
                    }
                }
                buf.append('"');
            }
        }
        void renderChildren(StringBuilder buf) {
            ArrayList<Node> list = mChildren;
            if (list == null || list.size() == 0) {
                buf.append("null");
            } else {
                buf.append("[ ");
                final int N = list.size();
                for (int i=0; i<N; i++) {
                    list.get(i).render(buf);
                    if (i != N-1) {
                        buf.append(", ");
                    }
                }
                buf.append(" ]\n");
            }
        }
        void render(StringBuilder buf) {
            buf.append("[ ");
            renderString(buf, mLabel);
            buf.append(", ");
            renderString(buf, mLink);
            buf.append(", ");
            renderChildren(buf);
            buf.append(", ");
            renderString(buf, mSince);
            buf.append(" ]");
        }
    }
}
