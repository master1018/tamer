public abstract class PredicatedNodeTest extends NodeTest implements SubContextList
{
    static final long serialVersionUID = -6193530757296377351L;
  PredicatedNodeTest(LocPathIterator locPathIterator)
  {
    m_lpi = locPathIterator;
  }
  PredicatedNodeTest()
  {
  }
  private void readObject(java.io.ObjectInputStream stream)
          throws java.io.IOException, javax.xml.transform.TransformerException
  {
    try
    {
      stream.defaultReadObject();
      m_predicateIndex = -1;
      resetProximityPositions();
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new javax.xml.transform.TransformerException(cnfe);
    }
  }
  public Object clone() throws CloneNotSupportedException
  {
    PredicatedNodeTest clone = (PredicatedNodeTest) super.clone();
    if ((null != this.m_proximityPositions)
            && (this.m_proximityPositions == clone.m_proximityPositions))
    {
      clone.m_proximityPositions = new int[this.m_proximityPositions.length];
      System.arraycopy(this.m_proximityPositions, 0,
                       clone.m_proximityPositions, 0,
                       this.m_proximityPositions.length);
    }
    if(clone.m_lpi == this)
      clone.m_lpi = (LocPathIterator)clone;
    return clone;
  }
  protected int m_predCount = -1;
  public int getPredicateCount()
  {
    if(-1 == m_predCount)
      return (null == m_predicates) ? 0 : m_predicates.length;
    else
      return m_predCount;
  }
  public void setPredicateCount(int count)
  {
    if(count > 0)
    {
      Expression[] newPredicates = new Expression[count];
      for (int i = 0; i < count; i++) 
      {
        newPredicates[i] = m_predicates[i];
      }
      m_predicates = newPredicates;
    }
    else
      m_predicates = null;
  }
  protected void initPredicateInfo(Compiler compiler, int opPos)
          throws javax.xml.transform.TransformerException
  {
    int pos = compiler.getFirstPredicateOpPos(opPos);
    if(pos > 0)
    {
      m_predicates = compiler.getCompiledPredicates(pos);
      if(null != m_predicates)
      {
      	for(int i = 0; i < m_predicates.length; i++)
      	{
      		m_predicates[i].exprSetParent(this);
      	}
      }
    }
  }
  public Expression getPredicate(int index)
  {
    return m_predicates[index];
  }
  public int getProximityPosition()
  {
    return getProximityPosition(m_predicateIndex);
  }
  public int getProximityPosition(XPathContext xctxt)
  {
    return getProximityPosition();
  }
  public abstract int getLastPos(XPathContext xctxt);
  protected int getProximityPosition(int predicateIndex)
  {
    return (predicateIndex >= 0) ? m_proximityPositions[predicateIndex] : 0;
  }
  public void resetProximityPositions()
  {
    int nPredicates = getPredicateCount();
    if (nPredicates > 0)
    {
      if (null == m_proximityPositions)
        m_proximityPositions = new int[nPredicates];
      for (int i = 0; i < nPredicates; i++)
      {
        try
        {
          initProximityPosition(i);
        }
        catch(Exception e)
        {
          throw new org.apache.xml.utils.WrappedRuntimeException(e);
        }
      }
    }
  }
  public void initProximityPosition(int i) throws javax.xml.transform.TransformerException
  {
    m_proximityPositions[i] = 0;
  }
  protected void countProximityPosition(int i)
  {
  	int[] pp = m_proximityPositions;
    if ((null != pp) && (i < pp.length))
      pp[i]++;
  }
  public boolean isReverseAxes()
  {
    return false;
  }
  public int getPredicateIndex()
  {
    return m_predicateIndex;
  }
  boolean executePredicates(int context, XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    int nPredicates = getPredicateCount();
    if (nPredicates == 0)
      return true;
    PrefixResolver savedResolver = xctxt.getNamespaceContext();
    try
    {
      m_predicateIndex = 0;
      xctxt.pushSubContextList(this);
      xctxt.pushNamespaceContext(m_lpi.getPrefixResolver());
      xctxt.pushCurrentNode(context);
      for (int i = 0; i < nPredicates; i++)
      {
        XObject pred = m_predicates[i].execute(xctxt);
        if (XObject.CLASS_NUMBER == pred.getType())
        {
          if (DEBUG_PREDICATECOUNTING)
          {
            System.out.flush();
            System.out.println("\n===== start predicate count ========");
            System.out.println("m_predicateIndex: " + m_predicateIndex);
            System.out.println("pred.num(): " + pred.num());
          }
          int proxPos = this.getProximityPosition(m_predicateIndex);
          int predIndex = (int) pred.num();
          if (proxPos != predIndex)
          {
            if (DEBUG_PREDICATECOUNTING)
            {
              System.out.println("\nnode context: "+nodeToString(context));
              System.out.println("index predicate is false: "+proxPos);
              System.out.println("\n===== end predicate count ========");
            }
            return false;
          }
          else if (DEBUG_PREDICATECOUNTING)
          {
            System.out.println("\nnode context: "+nodeToString(context));
            System.out.println("index predicate is true: "+proxPos);
            System.out.println("\n===== end predicate count ========");
          }
          if(m_predicates[i].isStableNumber() && i == nPredicates - 1)
          {
            m_foundLast = true;
          }
        }
        else if (!pred.bool())
          return false;
        countProximityPosition(++m_predicateIndex);
      }
    }
    finally
    {
      xctxt.popCurrentNode();
      xctxt.popNamespaceContext();
      xctxt.popSubContextList();
      m_predicateIndex = -1;
    }
    return true;
  }
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    super.fixupVariables(vars, globalsSize);
    int nPredicates = getPredicateCount();
    for (int i = 0; i < nPredicates; i++)
    {
      m_predicates[i].fixupVariables(vars, globalsSize);
    }
  }
  protected String nodeToString(int n)
  {
    if(DTM.NULL != n)
    {
      DTM dtm = m_lpi.getXPathContext().getDTM(n);
      return dtm.getNodeName(n) + "{" + (n+1) + "}";
    }
    else
    {
      return "null";
    }
  }
  public short acceptNode(int n)
  {
    XPathContext xctxt = m_lpi.getXPathContext();
    try
    {
      xctxt.pushCurrentNode(n);
      XObject score = execute(xctxt, n);
      if (score != NodeTest.SCORE_NONE)
      {
        if (getPredicateCount() > 0)
        {
          countProximityPosition(0);
          if (!executePredicates(n, xctxt))
            return DTMIterator.FILTER_SKIP;
        }
        return DTMIterator.FILTER_ACCEPT;
      }
    }
    catch (javax.xml.transform.TransformerException se)
    {
      throw new RuntimeException(se.getMessage());
    }
    finally
    {
      xctxt.popCurrentNode();
    }
    return DTMIterator.FILTER_SKIP;
  }
  public LocPathIterator getLocPathIterator()
  {
    return m_lpi;
  }
  public void setLocPathIterator(LocPathIterator li)
  {
    m_lpi = li;
    if(this != li)
      li.exprSetParent(this);
  }
   public boolean canTraverseOutsideSubtree()
   {
    int n = getPredicateCount();
    for (int i = 0; i < n; i++) 
    {
      if(getPredicate(i).canTraverseOutsideSubtree())
        return true;
    }
    return false;
   }
	public void callPredicateVisitors(XPathVisitor visitor)
	{
	  if (null != m_predicates)
	    {
	    int n = m_predicates.length;
	    for (int i = 0; i < n; i++)
	      {
	      ExpressionOwner predOwner = new PredOwner(i);
	      if (visitor.visitPredicate(predOwner, m_predicates[i]))
	        {
	        m_predicates[i].callVisitors(predOwner, visitor);
	      }
	    }
	  }
	} 
    public boolean deepEquals(Expression expr)
    {
      if (!super.deepEquals(expr))
            return false;
      PredicatedNodeTest pnt = (PredicatedNodeTest) expr;
      if (null != m_predicates)
      {
        int n = m_predicates.length;
        if ((null == pnt.m_predicates) || (pnt.m_predicates.length != n))
              return false;
        for (int i = 0; i < n; i++)
        {
          if (!m_predicates[i].deepEquals(pnt.m_predicates[i]))
          	return false; 
        }
      }
      else if (null != pnt.m_predicates)
              return false; 
      return true; 
    }
  transient protected boolean m_foundLast = false;
  protected LocPathIterator m_lpi;
  transient int m_predicateIndex = -1;
  private Expression[] m_predicates;
  transient protected int[] m_proximityPositions;
  static final boolean DEBUG_PREDICATECOUNTING = false;
  class PredOwner implements ExpressionOwner
  {
  	int m_index;
  	PredOwner(int index)
  	{
  		m_index = index;
  	}
    public Expression getExpression()
    {
      return m_predicates[m_index];
    }
    public void setExpression(Expression exp)
    {
    	exp.exprSetParent(PredicatedNodeTest.this);
    	m_predicates[m_index] = exp;
    }
  }
}
