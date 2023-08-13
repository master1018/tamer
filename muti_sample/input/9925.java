public class FinalizerSummaryQuery extends QueryHandler {
    public void run() {
        Enumeration objs = snapshot.getFinalizerObjects();
        startHtml("Finalizer Summary");
        out.println("<p align='center'>");
        out.println("<b><a href='/'>All Classes (excluding platform)</a></b>");
        out.println("</p>");
        printFinalizerSummary(objs);
        endHtml();
    }
    private static class HistogramElement {
        public HistogramElement(JavaClass clazz) {
            this.clazz = clazz;
        }
        public void updateCount() {
            this.count++;
        }
        public int compare(HistogramElement other) {
            long diff = other.count - count;
            return (diff == 0L)? 0 : ((diff > 0L)? +1 : -1);
        }
        public JavaClass getClazz() {
            return clazz;
        }
        public long getCount() {
            return count;
        }
        private JavaClass clazz;
        private long count;
    }
    private void printFinalizerSummary(Enumeration objs) {
        int count = 0;
        Map<JavaClass, HistogramElement> map = new HashMap<JavaClass, HistogramElement>();
        while (objs.hasMoreElements()) {
            JavaHeapObject obj = (JavaHeapObject) objs.nextElement();
            count++;
            JavaClass clazz = obj.getClazz();
            if (! map.containsKey(clazz)) {
                map.put(clazz, new HistogramElement(clazz));
            }
            HistogramElement element = map.get(clazz);
            element.updateCount();
        }
        out.println("<p align='center'>");
        out.println("<b>");
        out.println("Total ");
        if (count != 0) {
            out.print("<a href='/finalizerObjects/'>instances</a>");
        } else {
            out.print("instances");
        }
        out.println(" pending finalization: ");
        out.print(count);
        out.println("</b></p><hr>");
        if (count == 0) {
            return;
        }
        HistogramElement[] elements = new HistogramElement[map.size()];
        map.values().toArray(elements);
        Arrays.sort(elements, new Comparator<HistogramElement>() {
                    public int compare(HistogramElement o1, HistogramElement o2) {
                        return o1.compare(o2);
                    }
                });
        out.println("<table border=1 align=center>");
        out.println("<tr><th>Count</th><th>Class</th></tr>");
        for (int j = 0; j < elements.length; j++) {
            out.println("<tr><td>");
            out.println(elements[j].getCount());
            out.println("</td><td>");
            printClass(elements[j].getClazz());
            out.println("</td><tr>");
        }
        out.println("</table>");
    }
}
