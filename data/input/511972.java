public class lalr_state {
  public lalr_state(lalr_item_set itms) throws internal_error
   {
     if (itms == null)
       throw new internal_error(
     "Attempt to construct an LALR state from a null item set");
     if (find_state(itms) != null)
       throw new internal_error(
     "Attempt to construct a duplicate LALR state");
     _index = next_index++;
     _items = itms;
     _all.put(_items,this);
   }
  protected static Hashtable _all = new Hashtable();
  public static Enumeration all() {return _all.elements();}
  public static int number() {return _all.size();}
  protected static Hashtable _all_kernels = new Hashtable();
  public static lalr_state find_state(lalr_item_set itms)
    {
      if (itms == null) 
      return null;
      else
      return (lalr_state)_all.get(itms);
    }
  protected static int next_index = 0;
  protected lalr_item_set _items;
  public lalr_item_set items() {return _items;}
  protected lalr_transition _transitions = null;
  public lalr_transition transitions() {return _transitions;}
  protected int _index;
  public int index() {return _index;}
  protected static void dump_state(lalr_state st) throws internal_error
    {
      lalr_item_set itms;
      lalr_item itm;
      production_part part;
      if (st == null) 
    {
      System.out.println("NULL lalr_state");
      return;
    }
      System.out.println("lalr_state [" + st.index() + "] {");
      itms = st.items();
      for (Enumeration e = itms.all(); e.hasMoreElements(); )
    {
      itm = (lalr_item)e.nextElement();
      System.out.print("  [");
      System.out.print(itm.the_production().lhs().the_symbol().name());
      System.out.print(" ::= ");
      for (int i = 0; i<itm.the_production().rhs_length(); i++)
        {
          if (i == itm.dot_pos()) System.out.print("(*) ");
          part = itm.the_production().rhs(i);
          if (part.is_action()) 
        System.out.print("{action} ");
          else
        System.out.print(((symbol_part)part).the_symbol().name() + " ");
        }
      if (itm.dot_at_end()) System.out.print("(*) ");
      System.out.println("]");
    }
      System.out.println("}");
    }
  protected static void propagate_all_lookaheads() throws internal_error
    {
      for (Enumeration st = all(); st.hasMoreElements(); )
    {
      ((lalr_state)st.nextElement()).propagate_lookaheads();
    }
    }
  public void add_transition(symbol on_sym, lalr_state to_st) 
    throws internal_error
    {
      lalr_transition trans;
      trans = new lalr_transition(on_sym, to_st, _transitions);
      _transitions = trans;
    }
  public static lalr_state build_machine(production start_prod) 
    throws internal_error
    {
      lalr_state    start_state;
      lalr_item_set start_items;
      lalr_item_set new_items;
      lalr_item_set linked_items;
      lalr_item_set kernel;
      Stack         work_stack = new Stack();
      lalr_state    st, new_st;
      symbol_set    outgoing;
      lalr_item     itm, new_itm, existing, fix_itm;
      symbol        sym, sym2;
      Enumeration   i, s, fix;
      if (start_prod == null)
    throw new internal_error(
       "Attempt to build viable prefix recognizer using a null production");
      start_items = new lalr_item_set();
      itm = new lalr_item(start_prod);
      itm.lookahead().add(terminal.EOF);
      start_items.add(itm);
      kernel = new lalr_item_set(start_items);
      start_items.compute_closure();
      start_state = new lalr_state(start_items);
      work_stack.push(start_state);
      _all_kernels.put(kernel, start_state);
      while (!work_stack.empty())
    {
      st = (lalr_state)work_stack.pop();
      outgoing = new symbol_set();
      for (i = st.items().all(); i.hasMoreElements(); )
        {
          itm = (lalr_item)i.nextElement();
          sym = itm.symbol_after_dot();
          if (sym != null) outgoing.add(sym);
        }
      for (s = outgoing.all(); s.hasMoreElements(); )
        {
          sym = (symbol)s.nextElement();
          linked_items = new lalr_item_set();
          new_items = new lalr_item_set();
          for (i = st.items().all(); i.hasMoreElements();)
        {
          itm = (lalr_item)i.nextElement();
          sym2 = itm.symbol_after_dot();
          if (sym.equals(sym2))
            {
              new_items.add(itm.shift());
              linked_items.add(itm);
            }
        }
          kernel = new lalr_item_set(new_items);
          new_st = (lalr_state)_all_kernels.get(kernel);
          if (new_st == null)
        {
              new_items.compute_closure();
          new_st = new lalr_state(new_items);
          work_stack.push(new_st);
          _all_kernels.put(kernel, new_st);
        }
          else 
        {
          for (fix = linked_items.all(); fix.hasMoreElements(); )
            {
              fix_itm = (lalr_item)fix.nextElement();
              for (int l =0; l < fix_itm.propagate_items().size(); l++)
            {
              new_itm = 
                (lalr_item)fix_itm.propagate_items().elementAt(l);
              existing = new_st.items().find(new_itm);
              if (existing != null)
                fix_itm.propagate_items().setElementAt(existing ,l);
            }
            }
        }
          st.add_transition(sym, new_st);
        }
    }
      propagate_all_lookaheads();
      return start_state;
    }
  protected void propagate_lookaheads() throws internal_error
    {
      for (Enumeration itm = items().all(); itm.hasMoreElements(); )
    ((lalr_item)itm.nextElement()).propagate_lookaheads(null);
    }
  public void build_table_entries(
    parse_action_table act_table, 
    parse_reduce_table reduce_table)
    throws internal_error
    {
      parse_action_row our_act_row;
      parse_reduce_row our_red_row;
      lalr_item        itm;
      parse_action     act, other_act;
      symbol           sym;
      boolean          conflicted = false;
      our_act_row = act_table.under_state[index()];
      our_red_row = reduce_table.under_state[index()];
      for (Enumeration i = items().all(); i.hasMoreElements(); )
    {
      itm = (lalr_item)i.nextElement();
      if (itm.dot_at_end())
        {
          act = new reduce_action(itm.the_production());
          for (int t = 0; t < terminal.number(); t++)
        {
          if (!itm.lookahead().contains(t)) continue;
              if (our_act_row.under_term[t].kind() == 
              parse_action.ERROR)
            {
                  our_act_row.under_term[t] = act;
            }
              else
            {
              conflicted = true;
              other_act = our_act_row.under_term[t];
              if (other_act.kind() != parse_action.SHIFT)
                {
                  if (itm.the_production().index() < 
                  ((reduce_action)other_act).reduce_with().index())
                {
                  our_act_row.under_term[t] = act;
                }
                }
            }
        }
        }
    }
      for (lalr_transition trans=transitions(); trans!=null; trans=trans.next())
    {
      sym = trans.on_symbol();
      if (!sym.is_non_term())
        {
          act = new shift_action(trans.to_state());
          if ( our_act_row.under_term[sym.index()].kind() == 
           parse_action.ERROR)
        {
              our_act_row.under_term[sym.index()] = act;
        }
          else
        {
          conflicted = true;
          our_act_row.under_term[sym.index()] = act;
        }
        }
      else
        {
          our_red_row.under_non_term[sym.index()] = trans.to_state();
        }
    }
      if (conflicted)
        report_conflicts();
    }
  protected void report_conflicts()
    throws internal_error
    {
      lalr_item    itm, compare;
      symbol       shift_sym;
      terminal_set conflict_set;
      boolean      after_itm;
      for (Enumeration itms = items().all(); itms.hasMoreElements(); )
    {
      itm = (lalr_item)itms.nextElement();
      conflict_set = new terminal_set();
      if (itm.dot_at_end())
        {
          after_itm = false;
          for (Enumeration comps = items().all(); comps.hasMoreElements(); )
        {
          compare = (lalr_item)comps.nextElement();
          if (itm == compare) after_itm = true;
          if (itm != compare)
            {
              if (compare.dot_at_end())
            {
              if (after_itm)
                            if (compare.lookahead().intersects(itm.lookahead()))
                              report_reduce_reduce(itm, compare);
            }
              else 
            {
              shift_sym = compare.symbol_after_dot();
              if (!shift_sym.is_non_term()) 
                {
                  if (itm.lookahead().contains((terminal)shift_sym))
                    conflict_set.add((terminal)shift_sym);
                }
            }
            }
        }
          for (int t = 0; t < terminal.number(); t++)
        if (conflict_set.contains(t))
          report_shift_reduce(itm,t);
        }
    }
    }
  protected void report_reduce_reduce(lalr_item itm1, lalr_item itm2)
    throws internal_error
    {
      boolean comma_flag = false;
      System.err.println("*** Reduce/Reduce conflict found in state #"+index());
      System.err.print  ("  between ");
      System.err.println(itm1.to_simple_string());
      System.err.print  ("  and     ");
      System.err.println(itm2.to_simple_string());
      System.err.print("  under symbols: {" );
      for (int t = 0; t < terminal.number(); t++)
    {
      if (itm1.lookahead().contains(t) && itm2.lookahead().contains(t))
        {
          if (comma_flag) System.err.print(", "); else comma_flag = true;
          System.err.print(terminal.find(t).name());
        }
    }
      System.err.println("}");
      System.err.print("  Resolved in favor of ");
      if (itm1.the_production().index() < itm2.the_production().index())
    System.err.println("the first production.\n");
      else
    System.err.println("the second production.\n");
      emit.num_conflicts++;
      lexer.warning_count++;
    }
  protected void report_shift_reduce(
    lalr_item red_itm, 
    int       conflict_sym)
    throws internal_error
    {
      lalr_item    itm;
      symbol       shift_sym;
      System.err.println("*** Shift/Reduce conflict found in state #"+index());
      System.err.print  ("  between ");
      System.err.println(red_itm.to_simple_string());
      for (Enumeration itms = items().all(); itms.hasMoreElements(); )
    {
      itm = (lalr_item)itms.nextElement();
      if (itm != red_itm && !itm.dot_at_end())
        {
          shift_sym = itm.symbol_after_dot();
          if (!shift_sym.is_non_term() && shift_sym.index() == conflict_sym)
            {
                  System.err.println("  and     " + itm.to_simple_string());
        }
        }
    }
      System.err.println("  under symbol "+ terminal.find(conflict_sym).name());
      System.err.println("  Resolved in favor of shifting.\n");
      emit.num_conflicts++;
      lexer.warning_count++;
    }
  public boolean equals(lalr_state other)
    {
      return other != null && items().equals(other.items());
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof lalr_state))
    return false;
      else
    return equals((lalr_state)other);
    }
  public int hashCode()
    {
      return items().hashCode();
    }
  public String toString()
    {
      String result;
      lalr_transition tr;
      result = "lalr_state [" + index() + "]: " + _items + "\n";
      for (tr = transitions(); tr != null; tr = tr.next())
    {
      result += tr;
      result += "\n";
    }
      return result;
    }
};
