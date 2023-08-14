public abstract class BasicTestIterator extends LocPathIterator
{
    static final long serialVersionUID = 3505378079378096623L;
  protected BasicTestIterator()
  {
  }
  protected BasicTestIterator(PrefixResolver nscontext)
  {
    super(nscontext);
  }
  protected BasicTestIterator(Compiler compiler, int opPos, int analysis)
          throws javax.xml.transform.TransformerException
  {
    super(compiler, opPos, analysis, false);
    int firstStepPos = OpMap.getFirstChildPos(opPos);
    int whatToShow = compiler.getWhatToShow(firstStepPos);
    if ((0 == (whatToShow
               & (DTMFilter.SHOW_ATTRIBUTE 
               | DTMFilter.SHOW_NAMESPACE 
               | DTMFilter.SHOW_ELEMENT
               | DTMFilter.SHOW_PROCESSING_INSTRUCTION))) 
               || (whatToShow == DTMFilter.SHOW_ALL))
      initNodeTest(whatToShow);
    else
    {
      initNodeTest(whatToShow, compiler.getStepNS(firstStepPos),
                              compiler.getStepLocalName(firstStepPos));
    }
    initPredicateInfo(compiler, firstStepPos);
  }
  protected BasicTestIterator(
          Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers)
            throws javax.xml.transform.TransformerException
  {
    super(compiler, opPos, analysis, shouldLoadWalkers);
  }
  protected abstract int getNextNode();
  public int nextNode()
  {      
  	if(m_foundLast)
  	{
  		m_lastFetched = DTM.NULL;
  		return DTM.NULL;
  	}
    if(DTM.NULL == m_lastFetched)
    {
      resetProximityPositions();
    }
    int next;
    org.apache.xpath.VariableStack vars;
    int savedStart;
    if (-1 != m_stackFrame)
    {
      vars = m_execContext.getVarStack();
      savedStart = vars.getStackFrame();
      vars.setStackFrame(m_stackFrame);
    }
    else
    {
      vars = null;
      savedStart = 0;
    }
    try
    {
      do
      {
        next = getNextNode();
        if (DTM.NULL != next)
        {
          if(DTMIterator.FILTER_ACCEPT == acceptNode(next))
            break;
          else
            continue;
        }
        else
          break;
      }
      while (next != DTM.NULL);
      if (DTM.NULL != next)
      {
      	m_pos++;
        return next;
      }
      else
      {
        m_foundLast = true;
        return DTM.NULL;
      }
    }
    finally
    {
      if (-1 != m_stackFrame)
      {
        vars.setStackFrame(savedStart);
      }
    }
  }
  public DTMIterator cloneWithReset() throws CloneNotSupportedException
  {
    ChildTestIterator clone = (ChildTestIterator) super.cloneWithReset();
    clone.resetProximityPositions();
    return clone;
  }
}
