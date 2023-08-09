public class HasPositionalPredChecker extends XPathVisitor
{
	private boolean m_hasPositionalPred = false;
	private int m_predDepth = 0;
	public static boolean check(LocPathIterator path)
	{
		HasPositionalPredChecker hppc = new HasPositionalPredChecker();
		path.callVisitors(null, hppc);
		return hppc.m_hasPositionalPred;
	}
	public boolean visitFunction(ExpressionOwner owner, Function func)
	{
		if((func instanceof FuncPosition) ||
		   (func instanceof FuncLast))
			m_hasPositionalPred = true;
		return true;
	}
  public boolean visitPredicate(ExpressionOwner owner, Expression pred)
  {
    m_predDepth++;
    if(m_predDepth == 1)
    {
      if((pred instanceof Variable) || 
         (pred instanceof XNumber) ||
         (pred instanceof Div) ||
         (pred instanceof Plus) ||
         (pred instanceof Minus) ||
         (pred instanceof Mod) ||
         (pred instanceof Quo) ||
         (pred instanceof Mult) ||
         (pred instanceof org.apache.xpath.operations.Number) ||
         (pred instanceof Function))
          m_hasPositionalPred = true;
      else
      	pred.callVisitors(owner, this);
    }
    m_predDepth--;
    return false;
  }
}
