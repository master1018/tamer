public class ElemUnknown extends ElemLiteralResult
{
    static final long serialVersionUID = -4573981712648730168L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_UNDEFINED;
  }
  private void executeFallbacks(
          TransformerImpl transformer)
            throws TransformerException
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
      {
        try
        {
          transformer.pushElemTemplateElement(child);
          ((ElemFallback) child).executeFallback(transformer);
        }
        finally
        {
          transformer.popElemTemplateElement();
        }
      }
    }
  }
  private boolean hasFallbackChildren()
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
        return true;
    }
    return false;
  }
  public void execute(TransformerImpl transformer)
            throws TransformerException
  {
	try {
		if (hasFallbackChildren()) {
			executeFallbacks(transformer);
		} else {
		}
	} catch (TransformerException e) {
		transformer.getErrorListener().fatalError(e);
	}
  }
}
