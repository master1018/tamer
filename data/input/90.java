class InstancesQuery extends QueryHandler {
    private boolean includeSubclasses;
    private boolean newObjects;
    public InstancesQuery(boolean includeSubclasses) {
        this.includeSubclasses = includeSubclasses;
    }
    public InstancesQuery(boolean includeSubclasses, boolean newObjects) {
        this.includeSubclasses = includeSubclasses;
        this.newObjects = newObjects;
    }
    public void run() {
        JavaClass clazz = snapshot.findClass(query);
        String instancesOf;
        if (newObjects)
            instancesOf = "New instances of ";
        else
            instancesOf = "Instances of ";
        if (includeSubclasses) {
            startHtml(instancesOf + query + " (including subclasses)");
        } else {
            startHtml(instancesOf + query);
        }
        if (clazz == null) {
            error("Class not found");
        } else {
            out.print("<strong>");
            printClass(clazz);
            out.print("</strong><br><br>");
            Enumeration objects = clazz.getInstances(includeSubclasses);
            long totalSize = 0;
            long instances = 0;
            while (objects.hasMoreElements()) {
                JavaHeapObject obj = (JavaHeapObject) objects.nextElement();
                if (newObjects && !obj.isNew())
                    continue;
                printThing(obj);
                out.println("<br>");
                totalSize += obj.getSize();
                instances++;
            }
            out.println("<h2>Total of " + instances + " instances occupying " + totalSize + " bytes.</h2>");
        }
        endHtml();
    }
}
