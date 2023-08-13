public class lexer {
  private lexer() { }
  protected static int next_char; 
  protected static int next_char2;
  protected static final int EOF_CHAR = -1;
  protected static Hashtable keywords = new Hashtable(23);
  protected static Hashtable char_symbols = new Hashtable(11);
  protected static int current_line = 1;
  protected static int current_position = 1;
  public static int error_count = 0;
  public static int warning_count = 0;
  public static void init() throws java.io.IOException
    {
      keywords.put("package",  new Integer(sym.PACKAGE));
      keywords.put("import",   new Integer(sym.IMPORT));
      keywords.put("code",     new Integer(sym.CODE));
      keywords.put("action",   new Integer(sym.ACTION));
      keywords.put("parser",   new Integer(sym.PARSER));
      keywords.put("terminal", new Integer(sym.TERMINAL));
      keywords.put("non",      new Integer(sym.NON));
      keywords.put("init",     new Integer(sym.INIT));
      keywords.put("scan",     new Integer(sym.SCAN));
      keywords.put("with",     new Integer(sym.WITH));
      keywords.put("start",    new Integer(sym.START));
      keywords.put("debug",    new Integer(sym.DEBUG));
      char_symbols.put(new Integer(';'), new Integer(sym.SEMI));
      char_symbols.put(new Integer(','), new Integer(sym.COMMA));
      char_symbols.put(new Integer('*'), new Integer(sym.STAR));
      char_symbols.put(new Integer('.'), new Integer(sym.DOT));
      char_symbols.put(new Integer('|'), new Integer(sym.BAR));
      next_char = System.in.read();
      if (next_char == EOF_CHAR) 
    next_char2 = EOF_CHAR;
      else
    next_char2 = System.in.read();
    }
  protected static void advance() throws java.io.IOException
    {
      int old_char;
      old_char = next_char;
      next_char = next_char2;
      if (next_char == EOF_CHAR)
    next_char2 = EOF_CHAR;
      else
    next_char2 = System.in.read();
      current_position++;
      if (old_char == '\n')
    {
      current_line++;
      current_position = 1;
    }
    }
  public static void emit_error(String message)
    {
      System.err.println("Error at " + current_line + "(" + current_position +
             "): " + message);
      error_count++;
    }
  public static void emit_warn(String message)
    {
      System.err.println("Warning at " + current_line + "(" + current_position +
             "): " + message);
      warning_count++;
    }
  protected static boolean id_start_char(int ch)
    {
      return (ch >= 'a' &&  ch <= 'z') || (ch >= 'A' && ch <= 'Z') || 
         (ch == '_');
    }
  protected static boolean id_char(int ch)
    {
      return id_start_char(ch) || (ch >= '0' && ch <= '9');
    }
  protected static int find_single_char(int ch)
    {
      Integer result;
      result = (Integer)char_symbols.get(new Integer((char)ch));
      if (result == null) 
    return -1;
      else
    return result.intValue();
    }
  protected static void swallow_comment() throws java.io.IOException
    {
      if (next_char2 == '*')
    {
      advance(); advance();
      for (;;)
        {
          if (next_char == EOF_CHAR)
        {
          emit_error("Specification file ends inside a comment");
          return;
        }
          if (next_char == '*' && next_char2 == '/')
        {
          advance();
          advance();
          return;
        }
          advance();
        }
    }
      if (next_char2 == '/')
    {
      advance(); advance();
      while (next_char != '\n' && next_char != '\f' && next_char!=EOF_CHAR)
        advance();
      return;
    }
      emit_error("Malformed comment in specification -- ignored");
      advance();
    }
  protected static token do_code_string() throws java.io.IOException
    {
      StringBuffer result = new StringBuffer();
      advance(); advance();
      while (!(next_char == ':' && next_char2 == '}'))
    {
      if (next_char == EOF_CHAR)
        {
          emit_error("Specification file ends inside a code string");
          break;
        }
      result.append(new Character((char)next_char));
      advance();
    }
      advance(); advance();
      return new str_token(sym.CODE_STRING, result.toString());
    }
  protected static token do_id() throws java.io.IOException
    {
      StringBuffer result = new StringBuffer();
      String       result_str;
      Integer      keyword_num;
      char         buffer[] = new char[1];
      buffer[0] = (char)next_char;
      result.append(buffer,0,1);
      advance();
      while(id_char(next_char))
    {
          buffer[0] = (char)next_char;
      result.append(buffer,0,1);
      advance();
    }
      result_str = result.toString();
      keyword_num = (Integer)keywords.get(result_str);
      if (keyword_num != null)
    return new token(keyword_num.intValue());
      return new str_token(sym.ID, result_str);
    }
  public static token next_token() throws java.io.IOException
    {
      return real_next_token();
    }
  public static token debug_next_token() throws java.io.IOException
    {
      token result = real_next_token();
      System.out.println("# next_token() => " + result.sym);
      return result;
    }
  protected static token real_next_token() throws java.io.IOException
    {
      int sym_num;
      for (;;)
    {
      if (next_char == ' ' || next_char == '\t' || next_char == '\n' ||
          next_char == '\f' ||  next_char == '\r')
        {
          advance();
          continue;
        }
      sym_num = find_single_char(next_char);
      if (sym_num != -1)
        {
          advance();
          return new token(sym_num);
        }
      if (next_char == ':')
        {
          if (next_char2 != ':') 
        {
          advance();
          return new token(sym.COLON);
        }
          advance();
          if (next_char2 == '=') 
        {
          advance(); advance();
          return new token(sym.COLON_COLON_EQUALS);
        }
          else
        {
          return new token(sym.COLON);
        }
        }
      if (next_char == '/' && (next_char2 == '*' || next_char2 == '/'))
        {
          swallow_comment();
          continue;
        }
      if (next_char == '{' && next_char2 == ':')
        return do_code_string();
      if (id_start_char(next_char)) return do_id();
      if (next_char == EOF_CHAR) return new token(sym.EOF);
      emit_warn("Unrecognized character '" + 
        new Character((char)next_char) + "'(" + next_char + 
        ") -- ignored");
      advance();
    }
    }
};
