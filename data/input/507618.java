public class LayoutAnalysis {
    static final LayoutAnalysis ERROR = new LayoutAnalysis("");
    static {
        ERROR.mAnalyzed = false;
        ERROR.addIssue("The layout could not be analyzed. Check if you specified a valid "
                + "XML layout, if the specified file exists, etc.");
    }
    private final List<Issue> mIssues = new ArrayList<Issue>();
    private String mName;
    private boolean mAnalyzed;
    private Node mNode;
    LayoutAnalysis(String name) {
        mName = name;
    }
    public String getName() {
        return mName;
    }
    void setName(String name) {
        mName = name;
    }
    public void addIssue(Issue issue) {
        mIssues.add(issue);
    }
    public void addIssue(String description) {
        mIssues.add(new Issue(mNode, description));
    }
    public void addIssue(Node node, String description) {
        mIssues.add(new Issue(node, description));
    }
    public Issue[] getIssues() {
        return mIssues.toArray(new Issue[mIssues.size()]);
    }
    public boolean isValid() {
        return mAnalyzed;
    }
    void validate() {
        mAnalyzed = true;
        mNode = null;
    }
    void setCurrentNode(Node node) {
        mNode = node;
    }
    public static class Issue {
        private final String mDescription;
        private final Node mNode;
        public Issue(String description) {
            mNode = null;
            if (description == null) {
                throw new IllegalArgumentException("The description must be non-null");
            }
            mDescription = description;
        }
        public Issue(Node node, String description) {
            mNode = node;
            if (description == null) {
                throw new IllegalArgumentException("The description must be non-null");
            }
            mDescription = description;
        }
        public String getDescription() {
            return mDescription;
        }
        public int getStartLine() {
            return LayoutAnalysisCategory.getStartLine(mNode);
        }
        public int getEndLine() {
            return LayoutAnalysisCategory.getEndLine(mNode);
        }
    }
}
