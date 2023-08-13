public final class PrinterStateReasons
    extends HashMap<PrinterStateReason,Severity>
    implements PrintServiceAttribute
{
    private static final long serialVersionUID = -3731791085163619457L;
    public PrinterStateReasons() {
        super();
    }
    public PrinterStateReasons(int initialCapacity) {
        super (initialCapacity);
    }
    public PrinterStateReasons(int initialCapacity, float loadFactor) {
        super (initialCapacity, loadFactor);
    }
    public PrinterStateReasons(Map<PrinterStateReason,Severity> map) {
        this();
        for (Map.Entry<PrinterStateReason,Severity> e : map.entrySet())
            put(e.getKey(), e.getValue());
    }
    public Severity put(PrinterStateReason reason, Severity severity) {
        if (reason == null) {
            throw new NullPointerException("reason is null");
        }
        if (severity == null) {
            throw new NullPointerException("severity is null");
        }
        return super.put((PrinterStateReason) reason,
                         (Severity) severity);
    }
    public final Class<? extends Attribute> getCategory() {
        return PrinterStateReasons.class;
    }
    public final String getName() {
        return "printer-state-reasons";
    }
    public Set<PrinterStateReason> printerStateReasonSet(Severity severity) {
        if (severity == null) {
            throw new NullPointerException("severity is null");
        }
        return new PrinterStateReasonSet (severity, entrySet());
    }
    private class PrinterStateReasonSet
        extends AbstractSet<PrinterStateReason>
    {
        private Severity mySeverity;
        private Set myEntrySet;
        public PrinterStateReasonSet(Severity severity, Set entrySet) {
            mySeverity = severity;
            myEntrySet = entrySet;
        }
        public int size() {
            int result = 0;
            Iterator iter = iterator();
            while (iter.hasNext()) {
                iter.next();
                ++ result;
            }
            return result;
        }
        public Iterator iterator() {
            return new PrinterStateReasonSetIterator(mySeverity,
                                                     myEntrySet.iterator());
        }
    }
    private class PrinterStateReasonSetIterator implements Iterator {
        private Severity mySeverity;
        private Iterator myIterator;
        private Map.Entry myEntry;
        public PrinterStateReasonSetIterator(Severity severity,
                                             Iterator iterator) {
            mySeverity = severity;
            myIterator = iterator;
            goToNext();
        }
        private void goToNext() {
            myEntry = null;
            while (myEntry == null && myIterator.hasNext()) {
                myEntry = (Map.Entry) myIterator.next();
                if ((Severity) myEntry.getValue() != mySeverity) {
                    myEntry = null;
                }
            }
        }
        public boolean hasNext() {
            return myEntry != null;
        }
        public Object next() {
            if (myEntry == null) {
                throw new NoSuchElementException();
            }
            Object result = myEntry.getKey();
            goToNext();
            return result;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
