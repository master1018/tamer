class ObjectQuery extends ClassQuery {
    public ObjectQuery() {
    }
    public void run() {
        startHtml("Object at " + query);
        long id = parseHex(query);
        JavaHeapObject thing = snapshot.findThing(id);
        if (thing == null) {
            error("object not found");
        } else if (thing instanceof JavaClass) {
            printFullClass((JavaClass) thing);
        } else if (thing instanceof JavaValueArray) {
            print(((JavaValueArray) thing).valueString(true));
            printAllocationSite(thing);
            printReferencesTo(thing);
        } else if (thing instanceof JavaObjectArray) {
            printFullObjectArray((JavaObjectArray) thing);
            printAllocationSite(thing);
            printReferencesTo(thing);
        } else if (thing instanceof JavaObject) {
            printFullObject((JavaObject) thing);
            printAllocationSite(thing);
            printReferencesTo(thing);
        } else {
            print(thing.toString());
            printReferencesTo(thing);
        }
        endHtml();
    }
    private void printFullObject(JavaObject obj) {
        out.print("<h1>instance of ");
        print(obj.toString());
        out.print(" <small>(" + obj.getSize() + " bytes)</small>");
        out.println("</h1>\n");
        out.println("<h2>Class:</h2>");
        printClass(obj.getClazz());
        out.println("<h2>Instance data members:</h2>");
        final JavaThing[] things = obj.getFields();
        final JavaField[] fields = obj.getClazz().getFieldsForInstance();
        Integer[] hack = new Integer[things.length];
        for (int i = 0; i < things.length; i++) {
            hack[i] = new Integer(i);
        }
        ArraySorter.sort(hack, new Comparer() {
            public int compare(Object lhs, Object rhs) {
                JavaField left = fields[((Integer) lhs).intValue()];
                JavaField right = fields[((Integer) rhs).intValue()];
                return left.getName().compareTo(right.getName());
            }
        });
        for (int i = 0; i < things.length; i++) {
            int index = hack[i].intValue();
            printField(fields[index]);
            out.print(" : ");
            printThing(things[index]);
            out.println("<br>");
        }
    }
    private void printFullObjectArray(JavaObjectArray arr) {
        JavaThing[] elements = arr.getElements();
        out.println("<h1>Array of " + elements.length + " objects</h1>");
        out.println("<h2>Class:</h2>");
        printClass(arr.getClazz());
        out.println("<h2>Values</h2>");
        for (int i = 0; i < elements.length; i++) {
            out.print("" + i + " : ");
            printThing(elements[i]);
            out.println("<br>");
        }
    }
    private void printAllocationSite(JavaHeapObject obj) {
        StackTrace trace = obj.getAllocatedFrom();
        if (trace == null || trace.getFrames().length == 0) {
            return;
        }
        out.println("<h2>Object allocated from:</h2>");
        printStackTrace(trace);
    }
}
