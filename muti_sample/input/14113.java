public class NamingException extends Exception {
    protected Name resolvedName;
    protected Object resolvedObj;
    protected Name remainingName;
    protected Throwable rootException = null;
    public NamingException(String explanation) {
        super(explanation);
        resolvedName = remainingName = null;
        resolvedObj = null;
    }
    public NamingException() {
        super();
        resolvedName = remainingName = null;
        resolvedObj = null;
    }
    public Name getResolvedName() {
        return resolvedName;
    }
    public Name getRemainingName() {
        return remainingName;
    }
    public Object getResolvedObj() {
        return resolvedObj;
    }
    public String getExplanation() {
        return getMessage();
    }
    public void setResolvedName(Name name) {
        if (name != null)
            resolvedName = (Name)(name.clone());
        else
            resolvedName = null;
    }
    public void setRemainingName(Name name) {
        if (name != null)
            remainingName = (Name)(name.clone());
        else
            remainingName = null;
    }
    public void setResolvedObj(Object obj) {
        resolvedObj = obj;
    }
    public void appendRemainingComponent(String name) {
        if (name != null) {
            try {
                if (remainingName == null) {
                    remainingName = new CompositeName();
                }
                remainingName.add(name);
            } catch (NamingException e) {
                throw new IllegalArgumentException(e.toString());
            }
        }
    }
    public void appendRemainingName(Name name) {
        if (name == null) {
            return;
        }
        if (remainingName != null) {
            try {
                remainingName.addAll(name);
            } catch (NamingException e) {
                throw new IllegalArgumentException(e.toString());
            }
        } else {
            remainingName = (Name)(name.clone());
        }
    }
    public Throwable getRootCause() {
        return rootException;
    }
    public void setRootCause(Throwable e) {
        if (e != this) {
            rootException = e;
        }
    }
    public Throwable getCause() {
        return getRootCause();
    }
    public Throwable initCause(Throwable cause) {
        super.initCause(cause);
        setRootCause(cause);
        return this;
    }
    public String toString() {
        String answer = super.toString();
        if (rootException != null) {
            answer += " [Root exception is " + rootException + "]";
        }
        if (remainingName != null) {
            answer += "; remaining name '" + remainingName + "'";
        }
        return answer;
    }
    public String toString(boolean detail) {
        if (!detail || resolvedObj == null) {
            return toString();
        } else {
            return (toString() + "; resolved object " + resolvedObj);
        }
    }
    private static final long serialVersionUID = -1299181962103167177L;
};
