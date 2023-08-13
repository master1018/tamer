public class NodeSequence extends XObject
  implements DTMIterator, Cloneable, PathComponent
{
    static final long serialVersionUID = 3866261934726581044L;
  protected int m_last = -1;
  protected int m_next = 0;
  private IteratorCache m_cache;
  protected NodeVector getVector() {
      NodeVector nv = (m_cache != null) ?  m_cache.getVector() : null;
      return nv;
  }
  private IteratorCache getCache() {
      return m_cache;
  }
  protected void SetVector(NodeVector v)
  {
  	setObject(v);
  }
  public boolean hasCache()
  {
    final NodeVector nv = getVector();
  	return (nv != null);
  }
  private boolean cacheComplete() {
      final boolean complete;
      if (m_cache != null) {
          complete = m_cache.isComplete();
      } else {
          complete = false;
      }
      return complete;
  }
  private void markCacheComplete() {
      NodeVector nv = getVector();
      if (nv != null) {
          m_cache.setCacheComplete(true);
      }      
  }
  protected DTMIterator m_iter;
  public final void setIter(DTMIterator iter)
  {
  	m_iter = iter;
  }
  public final DTMIterator getContainedIter()
  {
  	return m_iter;
  }
  protected DTMManager m_dtmMgr;
  private NodeSequence(DTMIterator iter, int context, XPathContext xctxt, boolean shouldCacheNodes)
  {
  	setIter(iter);
  	setRoot(context, xctxt);
  	setShouldCacheNodes(shouldCacheNodes);
  }
  public NodeSequence(Object nodeVector)
  {
  	super(nodeVector);
    if (nodeVector instanceof NodeVector) {
        SetVector((NodeVector) nodeVector);
    }
  	if(null != nodeVector)
  	{
  		assertion(nodeVector instanceof NodeVector, 
  			"Must have a NodeVector as the object for NodeSequence!");
  		if(nodeVector instanceof DTMIterator)
  		{
  			setIter((DTMIterator)nodeVector);
  			m_last = ((DTMIterator)nodeVector).getLength();
  		}
  	}
  }
  private NodeSequence(DTMManager dtmMgr)
  {
    super(new NodeVector());
    m_last = 0;
    m_dtmMgr = dtmMgr;
  }
  public NodeSequence()
  {
      return;
  }
  public DTM getDTM(int nodeHandle)
  {
  	DTMManager mgr = getDTMManager();
  	if(null != mgr)
    	return getDTMManager().getDTM(nodeHandle);
    else
    {
    	assertion(false, "Can not get a DTM Unless a DTMManager has been set!");
    	return null;
    }
  }
  public DTMManager getDTMManager()
  {
    return m_dtmMgr;
  }
  public int getRoot()
  {
  	if(null != m_iter)
    	return m_iter.getRoot();
  	else
  	{
  		return DTM.NULL;
  	}
  }
  public void setRoot(int nodeHandle, Object environment)
  {
  	if(null != m_iter)
  	{
  		XPathContext xctxt = (XPathContext)environment;
  		m_dtmMgr = xctxt.getDTMManager();
  		m_iter.setRoot(nodeHandle, environment);
  		if(!m_iter.isDocOrdered())
  		{
  			if(!hasCache())
  				setShouldCacheNodes(true);
  			runTo(-1);
  			m_next=0;
  		}
  	}
  	else
  		assertion(false, "Can not setRoot on a non-iterated NodeSequence!");
  }
  public void reset()
  {
  	m_next = 0;
  }
  public int getWhatToShow()
  {
    return hasCache() ? (DTMFilter.SHOW_ALL & ~DTMFilter.SHOW_ENTITY_REFERENCE) 
    	: m_iter.getWhatToShow();
  }
  public boolean getExpandEntityReferences()
  {
  	if(null != m_iter)
  		return m_iter.getExpandEntityReferences();
  	else
    	return true;
  }
  public int nextNode()
  {
    NodeVector vec = getVector();
    if (null != vec)
    {	
    	if(m_next < vec.size())
    	{
			int next = vec.elementAt(m_next);
	    	m_next++;
	    	return next;
    	}
    	else if(cacheComplete() || (-1 != m_last) || (null == m_iter))
    	{
    		m_next++;
    		return DTM.NULL;
    	}
    }
  if (null == m_iter)
    return DTM.NULL;
 	int next = m_iter.nextNode();
    if(DTM.NULL != next)
    {
    	if(hasCache())
    	{
    		if(m_iter.isDocOrdered())
    	    {
    			getVector().addElement(next);
    			m_next++;
    		}
    		else
    		{
    			int insertIndex = addNodeInDocOrder(next);
    			if(insertIndex >= 0)
    				m_next++;
    		}
    	}
    	else
    		m_next++;
    }
    else
    {
        markCacheComplete();
    	m_last = m_next;
    	m_next++;
    }
    return next;
  }
  public int previousNode()
  {
  	if(hasCache())
  	{
  		if(m_next <= 0)
  			return DTM.NULL;
  		else
  		{
  			m_next--;
  			return item(m_next);
  		}
  	}
  	else
  	{
	    int n = m_iter.previousNode();
	    m_next = m_iter.getCurrentPos();
	    return m_next;
  	}
  }
  public void detach()
  {
  	if(null != m_iter)
  		m_iter.detach();
  	super.detach();
  }
  public void allowDetachToRelease(boolean allowRelease)
  {
  	if((false == allowRelease) && !hasCache())
  	{
  		setShouldCacheNodes(true);
  	}
  	if(null != m_iter)
  		m_iter.allowDetachToRelease(allowRelease);
  	super.allowDetachToRelease(allowRelease);
  }
  public int getCurrentNode()
  {
  	if(hasCache())
  	{
  		int currentIndex = m_next-1;
  		NodeVector vec = getVector();
  		if((currentIndex >= 0) && (currentIndex < vec.size()))
  			return vec.elementAt(currentIndex);
  		else
  			return DTM.NULL;
  	}
  	if(null != m_iter)
  	{
    	return m_iter.getCurrentNode();
  	}
  	else
  		return DTM.NULL;
  }
  public boolean isFresh()
  {
    return (0 == m_next);
  }
  public void setShouldCacheNodes(boolean b)
  {
    if (b)
    {
      if(!hasCache())
      {
        SetVector(new NodeVector());
      }
    }
    else
      SetVector(null);
  }
  public boolean isMutable()
  {
    return hasCache(); 
  }
  public int getCurrentPos()
  {
    return m_next;
  }
  public void runTo(int index)
  {
    int n;
    if (-1 == index)
    {
      int pos = m_next;
      while (DTM.NULL != (n = nextNode()));
      m_next = pos;
    }
    else if(m_next == index)
    {
      return;
    }
    else if(hasCache() && m_next < getVector().size())
    {
      m_next = index;
    }
    else if((null == getVector()) && (index < m_next))
    {
      while ((m_next >= index) && DTM.NULL != (n = previousNode()));
    }
    else
    {   
      while ((m_next < index) && DTM.NULL != (n = nextNode()));
    }
  }
  public void setCurrentPos(int i)
  {
  	runTo(i);
  }
  public int item(int index)
  {
  	setCurrentPos(index);
  	int n = nextNode();
  	m_next = index;
  	return n;
  }
  public void setItem(int node, int index)
  {
  	NodeVector vec = getVector();
  	if(null != vec)
  	{
        int oldNode = vec.elementAt(index);
        if (oldNode != node && m_cache.useCount() > 1) {
            IteratorCache newCache = new IteratorCache();
            final NodeVector nv;
            try {
                nv = (NodeVector) vec.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                RuntimeException rte = new RuntimeException(e.getMessage()); 
                throw rte;
            }
            newCache.setVector(nv);
            newCache.setCacheComplete(true);
            m_cache = newCache;
            vec = nv;
            super.setObject(nv); 
        }
  		vec.setElementAt(node, index);
  		m_last = vec.size();
  	}
  	else
  		m_iter.setItem(node, index);
  }
  public int getLength()
  {
    IteratorCache cache = getCache();
  	if(cache != null)
  	{
        if (cache.isComplete()) {
            NodeVector nv = cache.getVector();
            return nv.size();
        }
        if (m_iter instanceof NodeSetDTM)
        {
            return m_iter.getLength();
        }    
	  	if(-1 == m_last)
	  	{
	  		int pos = m_next;
	  		runTo(-1);
	  		m_next = pos;
	  	}
	    return m_last;
  	}
  	else
  	{
  		return (-1 == m_last) ? (m_last = m_iter.getLength()) : m_last;
  	}
  }
  public DTMIterator cloneWithReset() throws CloneNotSupportedException
  {
  	NodeSequence seq = (NodeSequence)super.clone();
    seq.m_next = 0;
    if (m_cache != null) {
        m_cache.increaseUseCount();
    }
    return seq;
  }
  public Object clone() throws CloneNotSupportedException
  {
          NodeSequence clone = (NodeSequence) super.clone();
          if (null != m_iter) clone.m_iter = (DTMIterator) m_iter.clone();
          if (m_cache != null) {
              m_cache.increaseUseCount();
          }
          return clone;
  }
  public boolean isDocOrdered()
  {
  	if(null != m_iter)
  		return m_iter.isDocOrdered();
  	else
    	return true; 
  }
  public int getAxis()
  {
  	if(null != m_iter)
    	return m_iter.getAxis();
    else
    {
    	assertion(false, "Can not getAxis from a non-iterated node sequence!");
    	return 0;
    }
  }
  public int getAnalysisBits()
  {
  	if((null != m_iter) && (m_iter instanceof PathComponent))
    	return ((PathComponent)m_iter).getAnalysisBits();
    else
    	return 0;
  }
  public void fixupVariables(Vector vars, int globalsSize)
  {
  	super.fixupVariables(vars, globalsSize);
  }  
   protected int addNodeInDocOrder(int node)
   {
      assertion(hasCache(), "addNodeInDocOrder must be done on a mutable sequence!");
      int insertIndex = -1;
      NodeVector vec = getVector();
      int size = vec.size(), i;
      for (i = size - 1; i >= 0; i--)
      {
        int child = vec.elementAt(i);
        if (child == node)
        {
          i = -2; 
          break;
        }
        DTM dtm = m_dtmMgr.getDTM(node);
        if (!dtm.isNodeAfter(node, child))
        {
          break;
        }
      }
      if (i != -2)
      {
        insertIndex = i + 1;
        vec.insertElementAt(node, insertIndex);
      }
      return insertIndex;
    } 
   protected void setObject(Object obj) {
       if (obj instanceof NodeVector) {
           super.setObject(obj);
           NodeVector v = (NodeVector)obj;
           if (m_cache != null) {
               m_cache.setVector(v);
           } else if (v!=null) {
               m_cache = new IteratorCache();
               m_cache.setVector(v);
           }
       } else if (obj instanceof IteratorCache) {
           IteratorCache cache = (IteratorCache) obj;
           m_cache = cache;
           m_cache.increaseUseCount();
           super.setObject(cache.getVector());
       } else {
           super.setObject(obj);
       }
   }
   private final static class IteratorCache {
        private NodeVector m_vec2;
        private boolean m_isComplete2;
        private int m_useCount2;
        IteratorCache() {
            m_vec2 = null;
            m_isComplete2 = false;
            m_useCount2 = 1;
            return;
        }
        private int useCount() {
            return m_useCount2;
        }
        private void increaseUseCount() {
            if (m_vec2 != null)
                m_useCount2++;
        }
        private void setVector(NodeVector nv) {
            m_vec2 = nv;
            m_useCount2 = 1;
        }
        private NodeVector getVector() {
            return m_vec2;
        }
        private void setCacheComplete(boolean b) {
            m_isComplete2 = b;
        }
        private boolean isComplete() {
            return m_isComplete2;
        }
    }
    protected IteratorCache getIteratorCache() {
        return m_cache;
    }
}
