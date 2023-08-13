public class Continuation extends ResolveResult {
    protected Name starter;
    protected Object followingLink = null;
    protected Hashtable environment = null;
    protected boolean continuing = false;
    protected Context resolvedContext = null;
    protected Name relativeResolvedName = null;
    public Continuation() {
    }
    public Continuation(Name top, Hashtable environment) {
        super();
        starter = top;
        this.environment = environment;
    }
    public boolean isContinue() {
        return continuing;
    }
    public void setSuccess() {
        continuing = false;
    }
    public NamingException fillInException(NamingException e) {
        e.setRemainingName(remainingName);
        e.setResolvedObj(resolvedObj);
        if (starter == null || starter.isEmpty())
            e.setResolvedName(null);
        else if (remainingName == null)
            e.setResolvedName(starter);
        else
            e.setResolvedName(
                starter.getPrefix(starter.size() -
                                  remainingName.size()));
        if ((e instanceof CannotProceedException)) {
            CannotProceedException cpe = (CannotProceedException)e;
            Hashtable env = (environment == null ?
                new Hashtable(11) : (Hashtable)environment.clone());
            cpe.setEnvironment(env);
            cpe.setAltNameCtx(resolvedContext);
            cpe.setAltName(relativeResolvedName);
        }
        return e;
    }
    public void setErrorNNS(Object resObj, Name remain) {
        Name nm = (Name)(remain.clone());
        try {
            nm.add("");
        } catch (InvalidNameException e) {
        }
        setErrorAux(resObj, nm);
    }
    public void setErrorNNS(Object resObj, String remain) {
        CompositeName rname = new CompositeName();
        try {
            if (remain != null && !remain.equals(""))
                rname.add(remain);
            rname.add("");
        } catch (InvalidNameException e) {
        }
        setErrorAux(resObj, rname);
    }
    public void setError(Object resObj, Name remain) {
        if (remain != null)
            remainingName = (Name)(remain.clone());
        else
            remainingName = null;
        setErrorAux(resObj, remainingName);
    }
    public void setError(Object resObj, String remain) {
        CompositeName rname = new CompositeName();
        if (remain != null && !remain.equals("")) {
            try {
                rname.add(remain);
            } catch (InvalidNameException e) {
            }
        }
        setErrorAux(resObj, rname);
    }
    private void setErrorAux(Object resObj, Name rname) {
        remainingName = rname;
        resolvedObj = resObj;
        continuing = false;
    }
    private void setContinueAux(Object resObj,
        Name relResName, Context currCtx,  Name remain) {
        if (resObj instanceof LinkRef) {
            setContinueLink(resObj, relResName, currCtx, remain);
        } else {
            remainingName = remain;
            resolvedObj = resObj;
            relativeResolvedName = relResName;
            resolvedContext = currCtx;
            continuing = true;
        }
    }
    public void setContinueNNS(Object resObj, Name relResName, Context currCtx) {
        CompositeName rname = new CompositeName();
        setContinue(resObj, relResName, currCtx, PartialCompositeContext._NNS_NAME);
    }
    public void setContinueNNS(Object resObj, String relResName, Context currCtx) {
        CompositeName relname = new CompositeName();
        try {
            relname.add(relResName);
        } catch (NamingException e) {}
        setContinue(resObj, relname, currCtx, PartialCompositeContext._NNS_NAME);
    }
    public void setContinue(Object obj, Name relResName, Context currCtx) {
        setContinueAux(obj, relResName, currCtx,
            (Name)PartialCompositeContext._EMPTY_NAME.clone());
    }
    public void setContinue(Object obj, Name relResName, Context currCtx, Name remain) {
        if (remain != null)
            this.remainingName = (Name)(remain.clone());
        else
            this.remainingName = new CompositeName();
        setContinueAux(obj, relResName, currCtx, remainingName);
    }
    public void setContinue(Object obj, String relResName,
        Context currCtx, String remain) {
        CompositeName relname = new CompositeName();
        if (!relResName.equals("")) {
            try {
                relname.add(relResName);
            } catch (NamingException e){}
        }
        CompositeName rname = new CompositeName();
        if (!remain.equals("")) {
            try {
                rname.add(remain);
            } catch (NamingException e) {
            }
        }
        setContinueAux(obj, relname, currCtx, rname);
    }
    @Deprecated
    public void setContinue(Object obj, Object currCtx) {
        setContinue(obj, null, (Context)currCtx);
    }
    private void setContinueLink(Object linkRef, Name relResName,
        Context resolvedCtx, Name rname) {
        this.followingLink = linkRef;
        this.remainingName = rname;
        this.resolvedObj = resolvedCtx;
        this.relativeResolvedName = PartialCompositeContext._EMPTY_NAME;
        this.resolvedContext = resolvedCtx;
        this.continuing = true;
    }
    public String toString() {
        if (remainingName != null)
            return starter.toString() + "; remainingName: '" + remainingName + "'";
        else
            return starter.toString();
    }
    public String toString(boolean detail) {
        if (!detail || this.resolvedObj == null)
                return this.toString();
        return this.toString() + "; resolvedObj: " + this.resolvedObj +
            "; relativeResolvedName: " + relativeResolvedName +
            "; resolvedContext: " + resolvedContext;
    }
    private static final long serialVersionUID = 8162530656132624308L;
}
