final class CNBindingEnumeration implements NamingEnumeration {
    private static final int DEFAULT_BATCHSIZE = 100;
    private BindingListHolder _bindingList; 
    private BindingIterator _bindingIter;   
    private int counter;                    
    private int batchsize = DEFAULT_BATCHSIZE;  
    private CNCtx _ctx;                     
    private Hashtable _env;                 
    private boolean more = false;           
    private boolean isLookedUpCtx = false;  
  CNBindingEnumeration(CNCtx ctx, boolean isLookedUpCtx, Hashtable env) {
    String batch = (env != null ?
        (String)env.get(javax.naming.Context.BATCHSIZE) : null);
    if (batch != null) {
        try {
            batchsize = Integer.parseInt(batch);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Batch size not numeric: " + batch);
        }
    }
    _ctx = ctx;
    _ctx.incEnumCount();
    this.isLookedUpCtx = isLookedUpCtx;
    _env = env;
    _bindingList = new BindingListHolder();
    BindingIteratorHolder _bindingIterH = new BindingIteratorHolder();
    _ctx._nc.list(0, _bindingList, _bindingIterH);
    _bindingIter = _bindingIterH.value;
    if (_bindingIter != null) {
        more = _bindingIter.next_n(batchsize, _bindingList);
    } else {
        more = false;
    }
    counter = 0;
  }
  public java.lang.Object next() throws NamingException {
      if (more && counter >= _bindingList.value.length) {
          getMore();
      }
      if (more && counter < _bindingList.value.length) {
          org.omg.CosNaming.Binding bndg = _bindingList.value[counter];
          counter++;
          return mapBinding(bndg);
      } else {
          throw new NoSuchElementException();
      }
  }
  public boolean hasMore() throws NamingException {
      return more ? (counter < _bindingList.value.length || getMore()) : false;
  }
  public boolean hasMoreElements() {
      try {
          return hasMore();
      } catch (NamingException e) {
          return false;
      }
  }
    public java.lang.Object nextElement() {
        try {
            return next();
        } catch (NamingException ne) {
            throw new NoSuchElementException();
        }
  }
    public void close() throws NamingException {
        more = false;
        if (_bindingIter != null) {
            _bindingIter.destroy();
            _bindingIter = null;
        }
        if (_ctx != null) {
            _ctx.decEnumCount();
            if (isLookedUpCtx) {
                _ctx.close();
            }
            _ctx = null;
        }
    }
    protected void finalize() {
        try {
            close();
        } catch (NamingException e) {
        }
    }
    private boolean getMore() throws NamingException {
        try {
            more = _bindingIter.next_n(batchsize, _bindingList);
            counter = 0; 
        } catch (Exception e) {
            more = false;
            NamingException ne = new NamingException(
                "Problem getting binding list");
            ne.setRootCause(e);
            throw ne;
        }
        return more;
    }
    private javax.naming.Binding mapBinding(org.omg.CosNaming.Binding bndg)
                throws NamingException {
        java.lang.Object obj = _ctx.callResolve(bndg.binding_name);
        Name cname = CNNameParser.cosNameToName(bndg.binding_name);
        try {
            obj = NamingManager.getObjectInstance(obj, cname, _ctx, _env);
        } catch (NamingException e) {
            throw e;
        } catch (Exception e) {
            NamingException ne = new NamingException(
                        "problem generating object using object factory");
            ne.setRootCause(e);
            throw ne;
        }
        String cnameStr = cname.toString();
        javax.naming.Binding jbndg = new javax.naming.Binding(cnameStr, obj);
        NameComponent[] comps = _ctx.makeFullName(bndg.binding_name);
        String fullName = CNNameParser.cosNameToInsString(comps);
        jbndg.setNameInNamespace(fullName);
        return jbndg;
  }
}
