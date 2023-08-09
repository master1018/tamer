public class emit {
  private emit() { }
  public static String input_file_name;
  public static String prefix = "CUP$";
  public static String package_name = null;
  public static String symbol_const_class_name = "sym";
  public static String parser_class_name = "parser";
  public static String action_code = null;
  public static String parser_code = null;
  public static String init_code = null;
  public static String scan_code = null;
  public static production start_production = null;
  public static Stack import_list = new Stack();
  public static int num_conflicts = 0;
  public static boolean nowarn = false;
  public static int not_reduced = 0;
  public static int unused_term = 0;
  public static int unused_non_term = 0;
  public static long symbols_time          = 0;
  public static long parser_time           = 0;
  public static long action_code_time      = 0;
  public static long production_table_time = 0;
  public static long action_table_time     = 0;
  public static long goto_table_time       = 0;
  public static String debug_grammar = null;
  protected static String pre(String str) {return prefix + str;}
  protected static void emit_package(PrintStream out)
    {
      if (package_name != null)
    out.println("package " + package_name + ";\n");
    }
  public static void symbols(PrintStream out, boolean emit_non_terms)
    {
      terminal term;
      non_terminal nt;
      long start_time = System.currentTimeMillis();
      out.println();
      out.println("
      out.println("
                               version.title_str);
      out.println("
      out.println("
      out.println();
      emit_package(out);
      out.println(
        "");
      out.println("public class " + symbol_const_class_name + " {");
      out.println("  ");
      for (Enumeration e = terminal.all(); e.hasMoreElements(); )
    {
      term = (terminal)e.nextElement();
      out.println("  static final int " + term.name() + " = " +
              term.index() + ";");
    }
      if (emit_non_terms)
    {
          out.println("\n  ");
          for (Enumeration e = non_terminal.all(); e.hasMoreElements(); )
        {
          nt = (non_terminal)e.nextElement();
          out.println("  static final int " + nt.name() + " = " +
                  nt.index() + ";");
        }
    }
      out.println("};\n");
      symbols_time = System.currentTimeMillis() - start_time;
    }
  protected static void emit_action_code(PrintStream out, production start_prod)
    throws internal_error
    {
      production prod;
      long start_time = System.currentTimeMillis();
      out.println();
      out.println(
       ""
      );
      out.println("class " +  pre("actions") + " {");
      if (action_code != null)
    {
      out.println();
          out.println(action_code);
    }
      out.println();
      out.println("  ");
      out.println("  " + pre("actions") + "() { }");
      out.println();
      out.println("  ");
      out.println("  public final java_cup.runtime.symbol " +
             pre("do_action") + "(");
      out.println("    int                        " + pre("act_num,"));
      out.println("    java_cup.runtime.lr_parser " + pre("parser,"));
      out.println("    java.util.Stack            " + pre("stack,"));
      out.println("    int                        " + pre("top)"));
      out.println("    throws java.lang.Exception");
      out.println("    {");
      out.println("      ");
      out.println("      java_cup.runtime.symbol " + pre("result") + ";");
      out.println();
      out.println("      ");
      out.println("      switch (" + pre("act_num") + ")");
      out.println("        {");
      for (Enumeration p = production.all(); p.hasMoreElements(); )
    {
      prod = (production)p.nextElement();
          out.println("          ");
          out.println("          case " + prod.index() + ": 
                      prod.to_simple_string());
      out.println("            {");
          if (debug_grammar != null)
            out.println("             " +debug_grammar+ "(\"" +
                        prod.to_simple_string() + "\");");
      out.println("              " + pre("result") + " = new " +
        prod.lhs().the_symbol().stack_type() + "(" +
        prod.lhs().the_symbol().index() + ");");
      if (prod.action() != null && prod.action().code_string() != null &&
          !prod.action().equals(""))
        out.println("              " + prod.action().code_string());
      out.println("            }");
      if (prod == start_prod)
        {
          out.println("          ");
          out.println("          " + pre("parser") + ".done_parsing();");
        }
      out.println("          return " + pre("result") + ";");
      out.println();
    }
      out.println("          ");
      out.println("          default:");
      out.println("            throw new Exception(");
      out.println("               \"Invalid action number found in " +
                  "internal parse table\");");
      out.println();
      out.println("        }");
      out.println("    }");
      out.println("};\n");
      action_code_time = System.currentTimeMillis() - start_time;
    }
  protected static void emit_production_table(PrintStream out)
    {
      production all_prods[];
      production prod;
      long start_time = System.currentTimeMillis();
      out.println();
      out.println("  ");
      out.println("  protected static final short _production_table[][] = {");
      all_prods = new production[production.number()];
      for (Enumeration p = production.all(); p.hasMoreElements(); )
    {
      prod = (production)p.nextElement();
      all_prods[prod.index()] = prod;
    }
      out.print("    ");
      for (int i = 0; i<production.number(); i++)
    {
      prod = all_prods[i];
      out.print("    {");
      out.print( prod.lhs().the_symbol().index() + ", ");
      out.print(     prod.rhs_length() + "}");
      if (i < production.number()-1) out.print(", ");
      if ((i+1) % 5 == 0)
        {
          out.println();
          out.print("    ");
        }
    }
      out.println("  };");
      out.println();
      out.println("  ");
      out.println("  public short[][] production_table() " +
                         "{return _production_table;}");
      production_table_time = System.currentTimeMillis() - start_time;
    }
  protected static void do_action_table(
    PrintStream        out,
    parse_action_table act_tab,
    boolean            compact_reduces)
    throws internal_error
    {
      parse_action_row row;
      parse_action     act;
      int              red;
      long start_time = System.currentTimeMillis();
      out.println();
      out.println("  ");
      out.println("  protected static final short[][] _action_table = {");
      for (int i = 0; i < act_tab.num_states(); i++)
    {
      row = act_tab.under_state[i];
      if (compact_reduces)
        row.compute_default();
      else
        row.default_reduce = -1;
      out.print("    {");
      for (int j = 0; j < row.size(); j++)
        {
          act = row.under_term[j];
          if (act.kind() != parse_action.ERROR)
        {
          if (act.kind() == parse_action.SHIFT)
            {
            out.print(j + "," +
                (((shift_action)act).shift_to().index() + 1) + ",");
            }
          else if (act.kind() == parse_action.REDUCE)
            {
              red = ((reduce_action)act).reduce_with().index();
              if (red != row.default_reduce)
                out.print(j + "," + (-(red+1)) + ",");
            }
          else
            throw new internal_error("Unrecognized action code " +
              act.kind() + " found in parse table");
        }
        }
      if (row.default_reduce != -1)
        out.println("-1," + (-(row.default_reduce+1)) + "},");
      else
        out.println("-1,0},");
    }
      out.println("  };");
      out.println();
      out.println("  ");
      out.println("  public short[][] action_table() {return _action_table;}");
      action_table_time = System.currentTimeMillis() - start_time;
    }
  protected static void do_reduce_table(
    PrintStream out,
    parse_reduce_table red_tab)
    {
      lalr_state       goto_st;
      parse_action     act;
      long start_time = System.currentTimeMillis();
      out.println();
      out.println("  ");
      out.println("  protected static final short[][] _reduce_table = {");
      for (int i=0; i<red_tab.num_states(); i++)
    {
      out.print("    {");
      for (int j=0; j<red_tab.under_state[i].size(); j++)
        {
          goto_st = red_tab.under_state[i].under_non_term[j];
          if (goto_st != null)
        {
          out.print(j + "," + goto_st.index() + ",");
        }
        }
      out.println("-1,-1},");
    }
      out.println("  };");
      out.println();
      out.println("  ");
      out.println("  public short[][] reduce_table() {return _reduce_table;}");
      out.println();
      goto_table_time = System.currentTimeMillis() - start_time;
    }
  public static void parser(
    PrintStream        out,
    parse_action_table action_table,
    parse_reduce_table reduce_table,
    int                start_st,
    production         start_prod,
    boolean            compact_reduces)
    throws internal_error
    {
      long start_time = System.currentTimeMillis();
      out.println();
      out.println("
      out.println("
                            version.title_str);
      out.println("
      out.println("
      out.println();
      emit_package(out);
      for (int i = 0; i < import_list.size(); i++)
    out.println("import " + import_list.elementAt(i) + ";");
      out.println();
      out.println("public class " + parser_class_name +
          " extends java_cup.runtime.lr_parser {");
      out.println();
      out.println("  ");
      out.println("  public " + parser_class_name + "() {super();}");
      emit_production_table(out);
      do_action_table(out, action_table, compact_reduces);
      do_reduce_table(out, reduce_table);
      out.println("  ");
      out.println("  protected " + pre("actions") + " action_obj;");
      out.println();
      out.println("  ");
      out.println("  protected void init_actions()");
      out.println("    {");
      out.println("      action_obj = new " + pre("actions") + "();");
      out.println("    }");
      out.println();
      out.println("  ");
      out.println("  public java_cup.runtime.symbol do_action(");
      out.println("    int                        act_num,");
      out.println("    java_cup.runtime.lr_parser parser,");
      out.println("    java.util.Stack            stack,");
      out.println("    int                        top)");
      out.println("    throws java.lang.Exception");
      out.println("  {");
      out.println("    ");
      out.println("    return action_obj." + pre("do_action(") +
                  "act_num, parser, stack, top);");
      out.println("  }");
      out.println("");
      out.println("  ");
      out.println("  public int start_state() {return " + start_st + ";}");
      out.println("  ");
      out.println("  public int start_production() {return " +
             start_production.index() + ";}");
      out.println();
      out.println("  ");
      out.println("  public int EOF_sym() {return " + terminal.EOF.index() +
                      ";}");
      out.println();
      out.println("  ");
      out.println("  public int error_sym() {return " + terminal.error.index() +
                      ";}");
      out.println();
      if (init_code != null)
    {
          out.println();
      out.println("  ");
      out.println("  public void user_init() throws java.lang.Exception");
      out.println("    {");
      out.println(init_code);
      out.println("    }");
    }
      if (scan_code != null)
    {
          out.println();
      out.println("  ");
      out.println("  public java_cup.runtime.token scan()");
      out.println("    throws java.lang.Exception");
      out.println("    {");
      out.println(scan_code);
      out.println("    }");
    }
      if (parser_code != null)
    {
      out.println();
          out.println(parser_code);
    }
      out.println("};");
      emit_action_code(out, start_prod);
      parser_time = System.currentTimeMillis() - start_time;
    }
};
