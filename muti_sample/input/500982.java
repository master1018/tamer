public class virtual_parse_stack {
  public virtual_parse_stack(Stack shadowing_stack) throws java.lang.Exception
    {
      if (shadowing_stack == null)
    throw new Exception(
      "Internal parser error: attempt to create null virtual stack");
      real_stack = shadowing_stack;
      vstack     = new Stack();
      real_next  = 0;
      get_from_real();
    }
  protected Stack real_stack;
  protected int real_next;
  protected Stack vstack;
  protected void get_from_real()
    {
      symbol stack_sym;
      if (real_next >= real_stack.size()) return;
      stack_sym = (symbol)real_stack.elementAt(real_stack.size()-1-real_next);
      real_next++;
      vstack.push(new Integer(stack_sym.parse_state));
    }
  public boolean empty()
    {
      return vstack.empty();
    }
  public int top() throws java.lang.Exception
    {
      if (vstack.empty())
    throw new Exception(
          "Internal parser error: top() called on empty virtual stack");
      return ((Integer)vstack.peek()).intValue();
    }
  public void pop() throws java.lang.Exception
    {
      if (vstack.empty())
    throw new Exception(
          "Internal parser error: pop from empty virtual stack");
      vstack.pop();
      if (vstack.empty())
        get_from_real();
    }
  public void push(int state_num)
    {
      vstack.push(new Integer(state_num));
    }
};
