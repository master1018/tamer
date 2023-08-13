public class SearchControls implements java.io.Serializable {
    public final static int OBJECT_SCOPE = 0;
    public final static int ONELEVEL_SCOPE = 1;
    public final static int SUBTREE_SCOPE = 2;
    private int searchScope;
    private int timeLimit;
    private boolean derefLink;
    private boolean returnObj;
    private long countLimit;
    private String[] attributesToReturn;
    public SearchControls() {
        searchScope = ONELEVEL_SCOPE;
        timeLimit = 0; 
        countLimit = 0; 
        derefLink = false;
        returnObj = false;
        attributesToReturn = null; 
    }
    public SearchControls(int scope,
                             long countlim,
                             int timelim,
                             String[] attrs,
                             boolean retobj,
                             boolean deref) {
        searchScope = scope;
        timeLimit = timelim; 
        derefLink = deref;
        returnObj = retobj;
        countLimit = countlim; 
        attributesToReturn = attrs; 
    }
    public int getSearchScope() {
        return searchScope;
    }
    public int getTimeLimit() {
        return timeLimit;
    }
    public boolean getDerefLinkFlag() {
        return derefLink;
    }
    public boolean getReturningObjFlag() {
        return returnObj;
    }
    public long getCountLimit() {
        return countLimit;
    }
    public String[] getReturningAttributes() {
        return attributesToReturn;
    }
    public void setSearchScope(int scope) {
        searchScope = scope;
    }
    public void setTimeLimit(int ms) {
        timeLimit = ms;
    }
    public void setDerefLinkFlag(boolean on) {
        derefLink = on;
    }
    public void setReturningObjFlag(boolean on) {
        returnObj = on;
    }
    public void setCountLimit(long limit) {
        countLimit = limit;
    }
    public void setReturningAttributes(String[] attrs) {
        attributesToReturn = attrs;
    }
    private static final long serialVersionUID = -2480540967773454797L;
}
