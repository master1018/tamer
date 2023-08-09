public class FuncExtFunction extends Function
{
    static final long serialVersionUID = 5196115554693708718L;
  String m_namespace;
  String m_extensionName;
  Object m_methodKey;
  Vector m_argVec = new Vector();
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    if (null != m_argVec)
    {
      int nArgs = m_argVec.size();
      for (int i = 0; i < nArgs; i++)
      {
        Expression arg = (Expression) m_argVec.elementAt(i);
        arg.fixupVariables(vars, globalsSize);
      }
    }
  }
  public String getNamespace()
  {
    return m_namespace;
  }
  public String getFunctionName()
  {
    return m_extensionName;
  }
  public Object getMethodKey()
  {
    return m_methodKey;
  }
  public Expression getArg(int n) {
    if (n >= 0 && n < m_argVec.size())
      return (Expression) m_argVec.elementAt(n);
    else
      return null;
  }
  public int getArgCount() {
    return m_argVec.size();
  }
  public FuncExtFunction(java.lang.String namespace,
                         java.lang.String extensionName, Object methodKey)
  {
    m_namespace = namespace;
    m_extensionName = extensionName;
    m_methodKey = methodKey;
  }
  public XObject execute(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    if (xctxt.isSecureProcessing())
      throw new javax.xml.transform.TransformerException(
        XPATHMessages.createXPATHMessage(
          XPATHErrorResources.ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
          new Object[] {toString()}));
    XObject result;
    Vector argVec = new Vector();
    int nArgs = m_argVec.size();
    for (int i = 0; i < nArgs; i++)
    {
      Expression arg = (Expression) m_argVec.elementAt(i);
      XObject xobj = arg.execute(xctxt);
      xobj.allowDetachToRelease(false); 
      argVec.addElement(xobj);
    }
    ExtensionsProvider extProvider = (ExtensionsProvider)xctxt.getOwnerObject();
    Object val = extProvider.extFunction(this, argVec);
    if (null != val)
    {
      result = XObject.create(val, xctxt);
    }
    else
    {
      result = new XNull();
    }
    return result;
  }
  public void setArg(Expression arg, int argNum)
          throws WrongNumberArgsException
  {
    m_argVec.addElement(arg);
    arg.exprSetParent(this);
  }
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException{}
  class ArgExtOwner implements ExpressionOwner
  {
    Expression m_exp;
  	ArgExtOwner(Expression exp)
  	{
  		m_exp = exp;
  	}
    public Expression getExpression()
    {
      return m_exp;
    }
    public void setExpression(Expression exp)
    {
    	exp.exprSetParent(FuncExtFunction.this);
    	m_exp = exp;
    }
  }
  public void callArgVisitors(XPathVisitor visitor)
  {
      for (int i = 0; i < m_argVec.size(); i++)
      {
         Expression exp = (Expression)m_argVec.elementAt(i);
         exp.callVisitors(new ArgExtOwner(exp), visitor);
      }
  }
  public void exprSetParent(ExpressionNode n) 
  {
    super.exprSetParent(n);
    int nArgs = m_argVec.size();
    for (int i = 0; i < nArgs; i++)
    {
      Expression arg = (Expression) m_argVec.elementAt(i);
      arg.exprSetParent(n);
    }		
  }
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
    String fMsg = XSLMessages.createXPATHMessage(
        XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
        new Object[]{ "Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called." });
    throw new RuntimeException(fMsg);
  }
  public String toString()
  {
    if (m_namespace != null && m_namespace.length() > 0)
      return "{" + m_namespace + "}" + m_extensionName;
    else
      return m_extensionName;
  }  
}
