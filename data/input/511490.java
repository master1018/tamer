public class production {
  public production(
    non_terminal    lhs_sym, 
    production_part rhs_parts[], 
    int             rhs_l,
    String          action_str)
    throws internal_error
    {
      int         i;
      action_part tail_action;
      if (rhs_l >= 0)
    _rhs_length = rhs_l;
      else if (rhs_parts != null)
    _rhs_length = rhs_parts.length;
      else
    _rhs_length = 0;
      if (lhs_sym == null) 
    throw new internal_error(
      "Attempt to construct a production with a null LHS");
      action_str = translate_labels(
             rhs_parts, rhs_l, action_str, lhs_sym.stack_type());
      lhs_sym.note_use();
      _lhs = new symbol_part(lhs_sym);
      _rhs_length = merge_adjacent_actions(rhs_parts, _rhs_length);
      tail_action = strip_trailing_action(rhs_parts, _rhs_length);
      if (tail_action != null) _rhs_length--;
      _rhs = new production_part[_rhs_length];
      for (i=0; i<_rhs_length; i++)
    _rhs[i] = rhs_parts[i];
      for (i=0; i<_rhs_length; i++)
    if (!_rhs[i].is_action())
      ((symbol_part)_rhs[i]).the_symbol().note_use();
      if (action_str == null) action_str = "";
      if (tail_action != null && tail_action.code_string() != null)
    action_str = tail_action.code_string() + action_str;
      _action = new action_part(action_str);
      remove_embedded_actions();
      _index = next_index++;
      _all.put(new Integer(_index),this);
      lhs_sym.add_production(this);
    }
  public production(
    non_terminal    lhs_sym, 
    production_part rhs_parts[], 
    int             rhs_l)
    throws internal_error
    {
      this(lhs_sym,rhs_parts,rhs_l,null);
    }
  protected static Hashtable _all = new Hashtable();
  public static Enumeration all() {return _all.elements();};
  public static int number() {return _all.size();};
  protected static int next_index;
  protected symbol_part _lhs;
  public symbol_part lhs() {return _lhs;}
  protected production_part _rhs[];
  public production_part rhs(int indx) throws internal_error
    {
      if (indx >= 0 && indx < _rhs_length)
    return _rhs[indx];
      else
    throw new internal_error(
      "Index out of range for right hand side of production");
    }
  protected int _rhs_length;
  public int rhs_length() {return _rhs_length;}
  protected action_part _action;
  public action_part action() {return _action;}
  protected int _index;
  public int index() {return _index;}
  protected int _num_reductions = 0;
  public int num_reductions() {return _num_reductions;}
  public void note_reduction_use() {_num_reductions++;}
  protected boolean _nullable_known = false;
  public boolean nullable_known() {return _nullable_known;}
  protected boolean _nullable = false;
  public boolean nullable() {return _nullable;}
  protected terminal_set _first_set = new terminal_set();
  public terminal_set first_set() {return _first_set;}
  protected static boolean is_id_start(char c)
    {
      return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }
  protected static boolean is_id_char(char c)
    {
      return is_id_start(c) || (c >= '0' && c <= '9');
    }
  protected String label_translate(
    String    id_str,     
    int       act_pos,    
    Hashtable label_map,  
    Hashtable label_types)
    {
      Integer label_pos;
      String  label_type;
      int     offset;
      label_pos  = (Integer)label_map.get(id_str);
      if (label_pos == null) return id_str;
      label_type = (String)label_types.get(id_str);
      if (label_pos.intValue() == -1)
        {
      return "((" + label_type + ")" + emit.pre("result") + ")";
         }
       if (label_pos.intValue() > act_pos)
         {
       System.err.println("*** Label \"" + id_str + 
         "\" appears in action before it appears in production");
        lexer.error_count++;
          return id_str;
      }
      offset = (act_pos - label_pos.intValue())-1;
      return "(("+label_type+")" + 
       emit.pre("stack") + ".elementAt(" + emit.pre("top") +"-"+ offset + "))";
    }
  protected String action_translate(
    String    act_string,  
    int       act_pos,     
    Hashtable label_map,   
    Hashtable label_types) 
    {
      int          id_start;
      int          pos;
      int          len;
      String       id_str;
      boolean      in_id;
      StringBuffer result;
      char         buffer[];
      if (act_string == null || act_string.length()== 0) return act_string;
      len = act_string.length();
      result = new StringBuffer(len + 50);
      buffer = new char[len + 1];
      act_string.getChars(0, len, buffer, 0);
      buffer[len] = '\0';
      in_id = false;
      for (pos = id_start = 0; pos <= len; pos++)
    {
      if (in_id)
        {
          if (!is_id_char(buffer[pos]))
        {
          id_str = new String(buffer, id_start, pos - id_start);
          result.append(
              label_translate(id_str, act_pos, label_map,label_types));
          if (buffer[pos] != '\0')
                result.append(buffer, pos, 1);
          in_id = false;
        }
          else
        {
        }
        }
      else 
        {
          if (is_id_start(buffer[pos]))
            {
              in_id = true;
              id_start = pos;
            }
         else
           {
         if (buffer[pos] != '\0')
               result.append(buffer, pos, 1);
           }
        }
    }
      return result.toString();
    }
  protected String translate_labels(
    production_part  rhs[], 
    int              rhs_len, 
    String           final_action,
    String           lhs_type)
    {
      Hashtable   label_map   = new Hashtable(11);
      Hashtable   label_types = new Hashtable(11);
      symbol_part part;
      action_part act_part;
      int         pos;
      for (pos = 0; pos < rhs_len; pos++)
    {
      if (!rhs[pos].is_action())
        {
          part = (symbol_part)rhs[pos];
          if (part.label() != null)
        {
          label_map.put(part.label(), new Integer(pos));
          label_types.put(part.label(), part.the_symbol().stack_type());
        }
        }
    }
      label_map.put("RESULT", new Integer(-1));
      label_types.put("RESULT", lhs_type);
      for (pos = 0; pos < rhs_len; pos++)
    {
      if (rhs[pos].is_action())
        {
           act_part = (action_part)rhs[pos];
           act_part.set_code_string(
         action_translate(
           act_part.code_string(), pos, label_map, label_types));
        }
    }
      return action_translate(final_action, rhs_len, label_map, label_types);
    }
  protected int merge_adjacent_actions(production_part rhs_parts[], int len)
    {
      int from_loc, to_loc, merge_cnt;
      if (rhs_parts == null || len == 0) return 0;
      merge_cnt = 0;
      to_loc = -1;
      for (from_loc=0; from_loc<len; from_loc++)
    {
      if (to_loc < 0 || !rhs_parts[to_loc].is_action() 
             || !rhs_parts[from_loc].is_action())
        {
          to_loc++;
          if (to_loc != from_loc) rhs_parts[to_loc] = null;
        }
      if (to_loc != from_loc)
        {
          if (rhs_parts[to_loc] != null && rhs_parts[to_loc].is_action() && 
          rhs_parts[from_loc].is_action())
          {
            rhs_parts[to_loc] = new action_part(
          ((action_part)rhs_parts[to_loc]).code_string() +
          ((action_part)rhs_parts[from_loc]).code_string());
            merge_cnt++;
          }
        else
          {
            rhs_parts[to_loc] = rhs_parts[from_loc];
          }
        }
    }
      return len - merge_cnt;
    }
  protected action_part strip_trailing_action(
    production_part rhs_parts[],
    int             len)
    {
      action_part result;
      if (rhs_parts == null || len == 0) return null;
      if (rhs_parts[len-1].is_action())
    {
      result = (action_part)rhs_parts[len-1];
      rhs_parts[len-1] = null;
      return result;
    }
      else
    return null;
    }
  protected void remove_embedded_actions() throws internal_error
    {
      non_terminal new_nt;
      production   new_prod;
      for (int act_loc = 0; act_loc < rhs_length(); act_loc++)
    if (rhs(act_loc).is_action())
      {
        new_nt = non_terminal.create_new();
        new_prod = new action_production(this, new_nt, null, 0, 
            ((action_part)rhs(act_loc)).code_string());
        _rhs[act_loc] = new symbol_part(new_nt);
      }
    }
  public boolean check_nullable() throws internal_error
    {
      production_part part;
      symbol          sym;
      int             pos;
      if (nullable_known()) return nullable();
      if (rhs_length() == 0)
    {
      return set_nullable(true);
    }
      for (pos=0; pos<rhs_length(); pos++)
    {
      part = rhs(pos);
      if (!part.is_action())
        {
          sym = ((symbol_part)part).the_symbol();
          if (!sym.is_non_term()) 
        return set_nullable(false);
          else if (!((non_terminal)sym).nullable())
            return false;
        }
    }
      return set_nullable(true);
    }
  boolean set_nullable(boolean v)
    {
      _nullable_known = true;
      _nullable = v;
      return v;
    }
  public terminal_set check_first_set() throws internal_error
    {
      int    part;
      symbol sym;
      for (part=0; part<rhs_length(); part++)
    {
      if (!rhs(part).is_action())
        {
          sym = ((symbol_part)rhs(part)).the_symbol();
          if (sym.is_non_term())
        {
          _first_set.add(((non_terminal)sym).first_set());
          if (!((non_terminal)sym).nullable())
            break;
        }
          else
        {
          _first_set.add((terminal)sym);
          break;
        }
        }
    }
      return first_set();
    }
  public boolean equals(production other)
    {
      if (other == null) return false;
      return other._index == _index;
    }
  public boolean equals(Object other)
    {
      if (!(other instanceof production))
    return false;
      else
    return equals((production)other);
    }
  public int hashCode()
    {
      return _index*13;
    }
  public String toString() 
    {
      String result;
      try {
        result = "production [" + index() + "]: "; 
        result += ((lhs() != null) ? lhs().toString() : "$$NULL-LHS$$");
        result += " :: = ";
        for (int i = 0; i<rhs_length(); i++)
      result += rhs(i) + " ";
        result += ";";
        if (action()  != null && action().code_string() != null) 
      result += " {" + action().code_string() + "}";
        if (nullable_known())
      if (nullable())
        result += "[NULLABLE]";
      else
        result += "[NOT NULLABLE]";
      } catch (internal_error e) {
    e.crash();
    result = null;
      }
      return result;
    }
  public String to_simple_string() throws internal_error
    {
      String result;
      result = ((lhs() != null) ? lhs().the_symbol().name() : "NULL_LHS");
      result += " ::= ";
      for (int i = 0; i < rhs_length(); i++)
    if (!rhs(i).is_action())
      result += ((symbol_part)rhs(i)).the_symbol().name() + " ";
      return result;
    }
};
