public class FinalizerInfo extends Tool {
    public static void main(String[] args) {
        FinalizerInfo finfo = new FinalizerInfo();
        finfo.start(args);
        finfo.stop();
    }
    public void run() {
        InstanceKlass ik =
            SystemDictionaryHelper.findInstanceKlass("java.lang.ref.Finalizer");
        final Oop[] queueref = new Oop[1];
        ik.iterateStaticFields(new DefaultOopVisitor() {
            public void doOop(OopField field, boolean isVMField) {
              String name = field.getID().getName();
              if (name.equals("queue")) {
                queueref[0] = field.getValue(getObj());
              }
            }
          });
        Oop queue = queueref[0];
        InstanceKlass k = (InstanceKlass) queue.getKlass();
        LongField queueLengthField = (LongField) k.findField("queueLength", "J");
        long queueLength = queueLengthField.getValue(queue);
        OopField headField =  (OopField) k.findField("head", "Ljava/lang/ref/Reference;");
        Oop head = headField.getValue(queue);
        System.out.println("Number of objects pending for finalization: " + queueLength);
        if (head != null) {
            k = (InstanceKlass) head.getKlass();
            OopField referentField =
                (OopField) k.findField("referent", "Ljava/lang/Object;");
            OopField nextField =
                (OopField) k.findField("next", "Ljava/lang/ref/Reference;");
            HashMap map = new HashMap();
            for (;;) {
                Oop referent = referentField.getValue(head);
                Klass klass = referent.getKlass();
                if (!map.containsKey(klass)) {
                    map.put(klass, new ObjectHistogramElement(klass));
                }
                ((ObjectHistogramElement)map.get(klass)).updateWith(referent);
                Oop next = nextField.getValue(head);
                if (next == null || next.equals(head)) break;
                head = next;
            }
            ArrayList list = new ArrayList();
            list.addAll(map.values());
            Collections.sort(list, new Comparator() {
              public int compare(Object o1, Object o2) {
                  return ((ObjectHistogramElement)o1).compare((ObjectHistogramElement)o2);
              }
            });
            System.out.println("");
            System.out.println("Count" + "\t" + "Class description");
            System.out.println("-------------------------------------------------------");
            for (int i=0; i<list.size(); i++) {
                ObjectHistogramElement e = (ObjectHistogramElement)list.get(i);
                System.out.println(e.getCount() + "\t" + e.getDescription());
            }
       }
   }
}
