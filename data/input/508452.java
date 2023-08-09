public class VarNameCollector extends XPathVisitor
{
	Vector m_refs = new Vector();
	public void reset()
	{
		m_refs.removeAllElements(); 
	}
	public int getVarCount()
	{
		return m_refs.size();
	}
	boolean doesOccur(QName refName)
	{
		return m_refs.contains(refName);
	}
	public boolean visitVariableRef(ExpressionOwner owner, Variable var)
	{
		m_refs.addElement(var.getQName());
		return true;
	}
}
