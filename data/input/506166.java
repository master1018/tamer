public class FuncFormatNumb extends Function3Args
{
    static final long serialVersionUID = -8869935264870858636L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    ElemTemplateElement templElem =
      (ElemTemplateElement) xctxt.getNamespaceContext();
    StylesheetRoot ss = templElem.getStylesheetRoot();
    java.text.DecimalFormat formatter = null;
    java.text.DecimalFormatSymbols dfs = null;
    double num = getArg0().execute(xctxt).num();
    String patternStr = getArg1().execute(xctxt).str();
    if (patternStr.indexOf(0x00A4) > 0)
      ss.error(XSLTErrorResources.ER_CURRENCY_SIGN_ILLEGAL);  
    try
    {
      Expression arg2Expr = getArg2();
      if (null != arg2Expr)
      {
        String dfName = arg2Expr.execute(xctxt).str();
        QName qname = new QName(dfName, xctxt.getNamespaceContext());
        dfs = ss.getDecimalFormatComposed(qname);
        if (null == dfs)
        {
          warn(xctxt, XSLTErrorResources.WG_NO_DECIMALFORMAT_DECLARATION,
               new Object[]{ dfName });  
        }
        else
        {
          formatter = new java.text.DecimalFormat();
          formatter.setDecimalFormatSymbols(dfs);
          formatter.applyLocalizedPattern(patternStr);
        }
      }
      if (null == formatter)
      {
        dfs = ss.getDecimalFormatComposed(new QName(""));
        if (dfs != null)
        {
          formatter = new java.text.DecimalFormat();
          formatter.setDecimalFormatSymbols(dfs);
          formatter.applyLocalizedPattern(patternStr);
        }
        else
        {
          dfs = new java.text.DecimalFormatSymbols(java.util.Locale.US);
          dfs.setInfinity(Constants.ATTRVAL_INFINITY);
          dfs.setNaN(Constants.ATTRVAL_NAN);
          formatter = new java.text.DecimalFormat();
          formatter.setDecimalFormatSymbols(dfs);
          if (null != patternStr)
            formatter.applyLocalizedPattern(patternStr);
        }
      }
      return new XString(formatter.format(num));
    }
    catch (Exception iae)
    {
      templElem.error(XSLTErrorResources.ER_MALFORMED_FORMAT_STRING,
                      new Object[]{ patternStr });
      return XString.EMPTYSTRING;
    }
  }
  public void warn(XPathContext xctxt, String msg, Object args[])
          throws javax.xml.transform.TransformerException
  {
    String formattedMsg = XSLMessages.createWarning(msg, args);
    ErrorListener errHandler = xctxt.getErrorListener();
    errHandler.warning(new TransformerException(formattedMsg,
                                             (SAXSourceLocator)xctxt.getSAXLocator()));
  }
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException
  {
    if ((argNum > 3) || (argNum < 2))
      reportWrongNumberArgs();
  }
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
      throw new WrongNumberArgsException(XSLMessages.createMessage(XSLTErrorResources.ER_TWO_OR_THREE, null)); 
  }
}
