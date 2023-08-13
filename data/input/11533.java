public class FinalizerObjectsQuery extends QueryHandler {
    public void run() {
        Enumeration objs = snapshot.getFinalizerObjects();
        startHtml("Objects pending finalization");
        out.println("<a href='/finalizerSummary/'>Finalizer summary</a>");
        out.println("<h1>Objects pending finalization</h1>");
        while (objs.hasMoreElements()) {
            printThing((JavaHeapObject)objs.nextElement());
            out.println("<br>");
        }
        endHtml();
    }
}
