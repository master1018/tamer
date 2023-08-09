public class ElemVariablePsuedo extends ElemVariable
{
    static final long serialVersionUID = 692295692732588486L;
  XUnresolvedVariableSimple m_lazyVar;
  public void setSelect(XPath v)
  {
    super.setSelect(v);
    m_lazyVar = new XUnresolvedVariableSimple(this);
  }
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    transformer.getXPathContext().getVarStack().setLocalVariable(m_index, m_lazyVar);
  }
}
