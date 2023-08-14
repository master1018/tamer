public class ContextEnumerator implements NamingEnumeration {
    private static boolean debug = false;
    private NamingEnumeration children = null;
    private Binding currentChild = null;
    private boolean currentReturned = false;
    private Context root;
    private ContextEnumerator currentChildEnum = null;
    private boolean currentChildExpanded = false;
    private boolean rootProcessed = false;
    private int scope = SearchControls.SUBTREE_SCOPE;
    private String contextName = "";
    public ContextEnumerator(Context context) throws NamingException {
        this(context, SearchControls.SUBTREE_SCOPE);
    }
    public ContextEnumerator(Context context, int scope)
        throws NamingException {
        this(context, scope, "", scope != SearchControls.ONELEVEL_SCOPE);
   }
    protected ContextEnumerator(Context context, int scope, String contextName,
                             boolean returnSelf)
        throws NamingException {
        if(context == null) {
            throw new IllegalArgumentException("null context passed");
        }
        root = context;
        if (scope != SearchControls.OBJECT_SCOPE) {
            children = getImmediateChildren(context);
        }
        this.scope = scope;
        this.contextName = contextName;
        rootProcessed = !returnSelf;
        prepNextChild();
    }
    protected NamingEnumeration getImmediateChildren(Context ctx)
        throws NamingException {
            return ctx.listBindings("");
    }
    protected ContextEnumerator newEnumerator(Context ctx, int scope,
        String contextName, boolean returnSelf) throws NamingException {
            return new ContextEnumerator(ctx, scope, contextName, returnSelf);
    }
    public boolean hasMore() throws NamingException {
        return !rootProcessed ||
            (scope != SearchControls.OBJECT_SCOPE && hasMoreDescendants());
    }
    public boolean hasMoreElements() {
        try {
            return hasMore();
        } catch (NamingException e) {
            return false;
        }
    }
    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            throw new NoSuchElementException(e.toString());
        }
    }
    public Object next() throws NamingException {
        if (!rootProcessed) {
            rootProcessed = true;
            return new Binding("", root.getClass().getName(),
                               root, true);
        }
        if (scope != SearchControls.OBJECT_SCOPE && hasMoreDescendants()) {
            return getNextDescendant();
        }
        throw new NoSuchElementException();
    }
    public void close() throws NamingException {
        root = null;
    }
    private boolean hasMoreChildren() throws NamingException {
        return children != null && children.hasMore();
    }
    private Binding getNextChild() throws NamingException {
        Binding oldBinding = ((Binding)children.next());
        Binding newBinding = null;
        if(oldBinding.isRelative() && !contextName.equals("")) {
            NameParser parser = root.getNameParser("");
            Name newName = parser.parse(contextName);
            newName.add(oldBinding.getName());
            if(debug) {
                System.out.println("ContextEnumerator: adding " + newName);
            }
            newBinding = new Binding(newName.toString(),
                                     oldBinding.getClassName(),
                                     oldBinding.getObject(),
                                     oldBinding.isRelative());
        } else {
            if(debug) {
                System.out.println("ContextEnumerator: using old binding");
            }
            newBinding = oldBinding;
        }
        return newBinding;
    }
    private boolean hasMoreDescendants() throws NamingException {
        if (!currentReturned) {
            if(debug) {System.out.println("hasMoreDescendants returning " +
                                          (currentChild != null) ); }
            return currentChild != null;
        } else if (currentChildExpanded && currentChildEnum.hasMore()) {
            if(debug) {System.out.println("hasMoreDescendants returning " +
                "true");}
            return true;
        } else {
            if(debug) {System.out.println("hasMoreDescendants returning " +
                "hasMoreChildren");}
            return hasMoreChildren();
        }
    }
    private Binding getNextDescendant() throws NamingException {
        if (!currentReturned) {
            if(debug) {System.out.println("getNextDescedant: simple case");}
            currentReturned = true;
            return currentChild;
        } else if (currentChildExpanded && currentChildEnum.hasMore()) {
            if(debug) {System.out.println("getNextDescedant: expanded case");}
            return (Binding)currentChildEnum.next();
        } else {
            if(debug) {System.out.println("getNextDescedant: next case");}
            prepNextChild();
            return getNextDescendant();
        }
    }
    private void prepNextChild() throws NamingException {
        if(hasMoreChildren()) {
            try {
                currentChild = getNextChild();
                currentReturned = false;
            } catch (NamingException e){
                if (debug) System.out.println(e);
                if (debug) e.printStackTrace();
            }
        } else {
            currentChild = null;
            return;
        }
        if(scope == SearchControls.SUBTREE_SCOPE &&
           currentChild.getObject() instanceof Context) {
            currentChildEnum = newEnumerator(
                                          (Context)(currentChild.getObject()),
                                          scope, currentChild.getName(),
                                          false);
            currentChildExpanded = true;
            if(debug) {System.out.println("prepNextChild: expanded");}
        } else {
            currentChildExpanded = false;
            currentChildEnum = null;
            if(debug) {System.out.println("prepNextChild: normal");}
        }
    }
}
