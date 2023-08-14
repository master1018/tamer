public final class PageRanges   extends SetOfIntegerSyntax
        implements DocAttribute, PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = 8639895197656148392L;
    public PageRanges(int[][] members) {
        super (members);
        if (members == null) {
            throw new NullPointerException("members is null");
        }
        myPageRanges();
    }
    public PageRanges(String members) {
        super(members);
        if (members == null) {
            throw new NullPointerException("members is null");
        }
        myPageRanges();
    }
    private void myPageRanges() {
        int[][] myMembers = getMembers();
        int n = myMembers.length;
        if (n == 0) {
            throw new IllegalArgumentException("members is zero-length");
        }
        int i;
        for (i = 0; i < n; ++ i) {
          if (myMembers[i][0] < 1) {
            throw new IllegalArgumentException("Page value < 1 specified");
          }
        }
    }
    public PageRanges(int member) {
        super (member);
        if (member < 1) {
            throw new IllegalArgumentException("Page value < 1 specified");
        }
    }
    public PageRanges(int lowerBound, int upperBound) {
        super (lowerBound, upperBound);
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("Null range specified");
        } else if (lowerBound < 1) {
            throw new IllegalArgumentException("Page value < 1 specified");
        }
    }
    public boolean equals(Object object) {
        return (super.equals(object) && object instanceof PageRanges);
    }
    public final Class<? extends Attribute> getCategory() {
        return PageRanges.class;
    }
    public final String getName() {
        return "page-ranges";
    }
}
