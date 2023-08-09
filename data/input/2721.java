public final class JobStateReasons
    extends HashSet<JobStateReason> implements PrintJobAttribute {
    private static final long serialVersionUID = 8849088261264331812L;
    public JobStateReasons() {
        super();
    }
    public JobStateReasons(int initialCapacity) {
        super (initialCapacity);
    }
    public JobStateReasons(int initialCapacity, float loadFactor) {
        super (initialCapacity, loadFactor);
    }
   public JobStateReasons(Collection<JobStateReason> collection) {
       super (collection);
   }
    public boolean add(JobStateReason o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return super.add ((JobStateReason) o);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobStateReasons.class;
    }
    public final String getName() {
        return "job-state-reasons";
    }
}
