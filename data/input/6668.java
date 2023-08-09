class AllClassesQuery extends QueryHandler {
    boolean excludePlatform;
    boolean oqlSupported;
    public AllClassesQuery(boolean excludePlatform, boolean oqlSupported) {
        this.excludePlatform = excludePlatform;
        this.oqlSupported = oqlSupported;
    }
    public void run() {
        if (excludePlatform) {
            startHtml("All Classes (excluding platform)");
        } else {
            startHtml("All Classes (including platform)");
        }
        Iterator classes = snapshot.getClasses();
        String lastPackage = null;
        while (classes.hasNext()) {
            JavaClass clazz = (JavaClass) classes.next();
            if (excludePlatform && PlatformClasses.isPlatformClass(clazz)) {
                continue;
            }
            String name = clazz.getName();
            int pos = name.lastIndexOf(".");
            String pkg;
            if (name.startsWith("[")) {         
                pkg = "<Arrays>";
            } else if (pos == -1) {
                pkg = "<Default Package>";
            } else {
                pkg = name.substring(0, pos);
            }
            if (!pkg.equals(lastPackage)) {
                out.print("<h2>Package ");
                print(pkg);
                out.println("</h2>");
            }
            lastPackage = pkg;
            printClass(clazz);
            if (clazz.getId() != -1) {
                out.print(" [" + clazz.getIdString() + "]");
            }
            out.println("<br>");
        }
        out.println("<h2>Other Queries</h2>");
        out.println("<ul>");
        out.println("<li>");
        printAnchorStart();
        if (excludePlatform) {
            out.print("allClassesWithPlatform/\">");
            print("All classes including platform");
        } else {
            out.print("\">");
            print("All classes excluding platform");
        }
        out.println("</a>");
        out.println("<li>");
        printAnchorStart();
        out.print("showRoots/\">");
        print("Show all members of the rootset");
        out.println("</a>");
        out.println("<li>");
        printAnchorStart();
        out.print("showInstanceCounts/includePlatform/\">");
        print("Show instance counts for all classes (including platform)");
        out.println("</a>");
        out.println("<li>");
        printAnchorStart();
        out.print("showInstanceCounts/\">");
        print("Show instance counts for all classes (excluding platform)");
        out.println("</a>");
        out.println("<li>");
        printAnchorStart();
        out.print("histo/\">");
        print("Show heap histogram");
        out.println("</a>");
        out.println("<li>");
        printAnchorStart();
        out.print("finalizerSummary/\">");
        print("Show finalizer summary");
        out.println("</a>");
        if (oqlSupported) {
            out.println("<li>");
            printAnchorStart();
            out.print("oql/\">");
            print("Execute Object Query Language (OQL) query");
            out.println("</a>");
        }
        out.println("</ul>");
        endHtml();
    }
}
