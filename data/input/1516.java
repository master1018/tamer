class Scanner
{
  Scanner (IncludeEntry file, String[] keywords, boolean vbose,
      boolean emitAllIncludes, float cLevel, boolean debug) throws IOException
  {
    readFile (file);
    verbose  = vbose;
    emitAll  = emitAllIncludes;
    sortKeywords (keywords);
    corbaLevel = cLevel;
    this.debug = debug ;
  } 
  void sortKeywords (String[] keywords)
  {
    for (int i = 0; i < keywords.length; ++i)
      if (wildcardAtEitherEnd (keywords[i]))
        this.openEndedKeywords.addElement (keywords[i]);
      else if (wildcardsInside (keywords[i]))
        this.wildcardKeywords.addElement (keywords[i]);
      else
        this.keywords.addElement (keywords[i]);
  } 
  private boolean wildcardAtEitherEnd (String string)
  {
    return string.startsWith ("*") ||
           string.startsWith ("+") ||
           string.startsWith (".") ||
           string.endsWith ("*") ||
           string.endsWith ("+") ||
           string.endsWith (".");
  } 
  private boolean wildcardsInside (String string)
  {
    return string.indexOf ("*") > 0 ||
           string.indexOf ("+") > 0 ||
           string.indexOf (".") > 0;
  } 
  void readFile (IncludeEntry file) throws IOException
  {
    String filename = file.name ();
    filename = filename.substring (1, filename.length () - 1);
    readFile (file, filename);
  } 
  void readFile (IncludeEntry file, String filename) throws IOException
  {
    data.fileEntry = file;
    data.filename = filename;
    File idlFile = new File (data.filename);
    int len = (int)idlFile.length ();
    FileReader fileReader = new FileReader (idlFile);
    final String EOL = System.getProperty ("line.separator");
    data.fileBytes = new char [len + EOL.length ()];
    fileReader.read (data.fileBytes, 0, len);
    fileReader.close ();
    for (int i = 0; i < EOL.length (); i++)
      data.fileBytes[len + i] = EOL.charAt (i);
    readChar ();
  } 
  Token getToken () throws IOException
  {
    Token token = null;
    String commentText = new String ("");
    while (token == null)
      try
      {
        data.oldIndex = data.fileIndex;
        data.oldLine  = data.line;
        if (data.ch <= ' ') {
          skipWhiteSpace ();
          continue;
        }
        if (data.ch == 'L') {
            readChar();
            if (data.ch == '\'') {
                token = getCharacterToken(true);
                readChar();
                continue;
            } else
            if (data.ch == '"') {
                readChar ();
                token = new Token (Token.StringLiteral, getUntil ('"'), true);
                readChar ();
                continue;
            } else {
                unread(data.ch);
                unread('L');
                readChar();
            }
        }
        if ((data.ch >= 'a' && data.ch <= 'z') ||
            (data.ch >= 'A' && data.ch <= 'Z') ||
            (data.ch == '_')   ||
            Character.isLetter (data.ch)) {
            token = getString ();
        } else
        if ((data.ch >= '0' && data.ch <= '9') || data.ch == '.') {
            token = getNumber ();
        } else {
          switch (data.ch)
          {
            case ';':
              token = new Token (Token.Semicolon);
              break;
            case '{':
              token = new Token (Token.LeftBrace);
              break;
            case '}':
              token = new Token (Token.RightBrace);
              break;
            case ':':
              readChar ();
              if (data.ch == ':')
                token = new Token (Token.DoubleColon);
              else
              {
                unread (data.ch);
                token = new Token (Token.Colon);
              }
              break;
            case ',':
              token = new Token (Token.Comma);
              break;
            case '=':
              readChar ();
              if (data.ch == '=')
                token = new Token (Token.DoubleEqual);
              else
              {
                unread (data.ch);
                token = new Token (Token.Equal);
              }
              break;
            case '+':
              token = new Token (Token.Plus);
              break;
            case '-':
              token = new Token (Token.Minus);
              break;
            case '(':
              token = new Token (Token.LeftParen);
              break;
            case ')':
              token = new Token (Token.RightParen);
              break;
            case '<':
              readChar ();
              if (data.ch == '<')
                token = new Token (Token.ShiftLeft);
              else if (data.ch == '=')
                token = new Token (Token.LessEqual);
              else
              {
                unread (data.ch);
                token = new Token (Token.LessThan);
              }
              break;
            case '>':
              readChar ();
              if (data.ch == '>')
                token = new Token (Token.ShiftRight);
              else if (data.ch == '=')
                token = new Token (Token.GreaterEqual);
              else
              {
                unread (data.ch);
                token = new Token (Token.GreaterThan);
              }
              break;
            case '[':
              token = new Token (Token.LeftBracket);
              break;
            case ']':
              token = new Token (Token.RightBracket);
              break;
            case '\'':
              token = getCharacterToken(false);
              break;
            case '"':
              readChar ();
              token = new Token (Token.StringLiteral, getUntil ('"', false, false, false));
              break;
            case '\\':
              readChar ();
              if (data.ch == '\n' || data.ch == '\r')
                token = null;
              else
                token = new Token (Token.Backslash);
              break;
            case '|':
              readChar ();
              if (data.ch == '|')
                token = new Token (Token.DoubleBar);
              else
              {
                unread (data.ch);
                token = new Token (Token.Bar);
              }
              break;
            case '^':
              token = new Token (Token.Carat);
              break;
            case '&':
              readChar ();
              if (data.ch == '&')
                token = new Token (Token.DoubleAmpersand);
              else
              {
                unread (data.ch);
                token = new Token (Token.Ampersand);
              }
              break;
            case '*':
              token = new Token (Token.Star);
              break;
            case '/':
              readChar ();
              if (data.ch == '/')
                commentText = getLineComment();
              else if (data.ch == '*')
                commentText = getBlockComment();
              else
              {
                unread (data.ch);
                token = new Token (Token.Slash);
              }
              break;
            case '%':
              token = new Token (Token.Percent);
              break;
            case '~':
              token = new Token (Token.Tilde);
              break;
            case '#':
              token = getDirective ();
              break;
            case '!':
              readChar ();
              if (data.ch == '=')
                token = new Token (Token.NotEqual);
              else
              {
                unread (data.ch);
                token = new Token (Token.Exclamation);
              }
              break;
            case '?':
              try
              {
                token = replaceTrigraph ();
                break;
              }
              catch (InvalidCharacter e) {}
            default:
              throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
          }
          readChar ();
        }
      }
      catch (EOFException e)
      {
        token = new Token (Token.EOF);
      }
    token.comment = new Comment( commentText );
    if (debug)
        System.out.println( "Token: " + token ) ;
    return token;
  } 
  void scanString (String string)
  {
    dataStack.push (data);
    data = new ScannerData (data);
    data.fileIndex = 0;
    data.oldIndex  = 0;
    int strLen = string.length();
    data.fileBytes = new char[strLen];
    string.getChars (0, strLen, data.fileBytes, 0);
    data.macrodata = true;
    try {readChar ();} catch (IOException e) {}
  } 
  void scanIncludedFile (IncludeEntry file, String filename, boolean includeIsImport) throws IOException
  {
    dataStack.push (data);
    data = new ScannerData ();
    data.indent = ((ScannerData)dataStack.peek ()).indent + ' ';
    data.includeIsImport = includeIsImport;
    try
    {
      readFile (file, filename);
      if (!emitAll && includeIsImport)
        SymtabEntry.enteringInclude ();
      Parser.enteringInclude ();
      if (verbose)
        System.out.println (data.indent + Util.getMessage ("Compile.parsing", filename));
    }
    catch (IOException e)
    {
      data = (ScannerData)dataStack.pop ();
      throw e;
    }
  } 
  private void unread (char ch)
  {
    if (ch == '\n' && !data.macrodata) --data.line;
    --data.fileIndex;
  } 
  void readChar () throws IOException
  {
    if (data.fileIndex >= data.fileBytes.length)
      if (dataStack.empty ())
        throw new EOFException ();
      else
      {
        if (!data.macrodata)
        {
            if (!emitAll && data.includeIsImport)
                SymtabEntry.exitingInclude();
            Parser.exitingInclude();
        } 
        if (verbose && !data.macrodata)
          System.out.println (data.indent + Util.getMessage ("Compile.parseDone", data.filename));
        data = (ScannerData)dataStack.pop ();
      }
    else
    {
      data.ch = (char)(data.fileBytes[data.fileIndex++] & 0x00ff);
      if (data.ch == '\n' && !data.macrodata) ++data.line;
    }
  } 
  private String getWString() throws IOException
  {
      readChar();
      StringBuffer result = new StringBuffer();
      while (data.ch != '"') {
          if (data.ch == '\\') {
              readChar();
              if (data.ch == 'u') {
                  int num = getNDigitHexNumber(4);
                  System.out.println("Got num: " + num);
                  System.out.println("Which is: " + (int)(char)num);
                  result.append((char)num);
                  continue;
              } else
              if (data.ch >= '0' && data.ch <= '7') {
                  result.append((char)get3DigitOctalNumber());
                  continue;
              } else {
                  result.append('\\');
                  result.append(data.ch);
              }
          } else {
              result.append(data.ch);
          }
          readChar();
      }
      return result.toString();
  }
  private Token getCharacterToken(boolean isWide) throws IOException
  {
    Token token = null;
    readChar ();
    if ( data.ch == '\\' )
    {
      readChar ();
      if ((data.ch == 'x') || (data.ch == 'u'))
      {
        char charType = data.ch;
        int hexNum = getNDigitHexNumber ((charType == 'x') ? 2 : 4);
        return new Token (Token.CharacterLiteral,
            ((char)hexNum) + "\\" + charType + Integer.toString (hexNum, 16), isWide );
      }
      if ((data.ch >= '0') && (data.ch <= '7'))
      {
        int octNum = get3DigitOctalNumber ();
        return new Token (Token.CharacterLiteral,
            ((char)octNum) + "\\" + Integer.toString (octNum, 8), isWide );
      }
      return singleCharEscapeSequence (isWide);
    }
    token = new Token (Token.CharacterLiteral, "" + data.ch + data.ch, isWide );
    readChar ();
    return token;
  } 
  private Token singleCharEscapeSequence (boolean isWide) throws IOException
  {
    Token token;
    if (data.ch == 'n')
      token = new Token (Token.CharacterLiteral, "\n\\n", isWide);
    else if (data.ch == 't')
      token = new Token (Token.CharacterLiteral, "\t\\t", isWide);
    else if (data.ch == 'v')
      token = new Token (Token.CharacterLiteral, "\013\\v", isWide);
    else if (data.ch == 'b')
      token = new Token (Token.CharacterLiteral, "\b\\b", isWide);
    else if (data.ch == 'r')
      token = new Token (Token.CharacterLiteral, "\r\\r", isWide);
    else if (data.ch == 'f')
      token = new Token (Token.CharacterLiteral, "\f\\f", isWide);
    else if (data.ch == 'a')
      token = new Token (Token.CharacterLiteral, "\007\\a", isWide);
    else if (data.ch == '\\')
      token = new Token (Token.CharacterLiteral, "\\\\\\", isWide);
    else if (data.ch == '?')
      token = new Token (Token.CharacterLiteral, "?\\?", isWide);
    else if (data.ch == '\'')
      token = new Token (Token.CharacterLiteral, "'\\'", isWide);
    else if (data.ch == '"')
      token = new Token (Token.CharacterLiteral, "\"\\\"", isWide);
    else
      throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
    readChar ();
    return token;
  } 
  private Token getString () throws IOException
  {
    StringBuffer sbuf = new StringBuffer() ;
    boolean escaped = false;  
    boolean[] collidesWithKeyword = { false } ;  
    if (data.ch == '_') {
        sbuf.append( data.ch ) ;
        readChar ();
        if (escaped = escapedOK)
            if (data.ch == '_')
                throw new InvalidCharacter (data.filename, currentLine (),
                    currentLineNumber (), currentLinePosition (), data.ch);
    }
    while (Character.isLetterOrDigit( data.ch ) || (data.ch == '_')) {
        sbuf.append( data.ch ) ;
        readChar() ;
    }
    String string = sbuf.toString() ;
    if (!escaped) { 
        Token result = Token.makeKeywordToken( string, corbaLevel, escapedOK,
            collidesWithKeyword ) ;
        if (result != null)
            return result ;
    }
    string = getIdentifier (string);
    if (data.ch == '(') {
        readChar ();
        return new Token (Token.MacroIdentifier, string, escaped,
            collidesWithKeyword[0], false);
    } else
        return new Token (Token.Identifier, string, escaped,
            collidesWithKeyword[0], false);
  }
  static final int Star = 0, Plus = 1, Dot = 2, None = 3;
  private boolean matchesClosedWildKeyword (String string)
  {
    boolean     found     = true;
    String      tmpString = string;
    Enumeration e         = wildcardKeywords.elements ();
    while (e.hasMoreElements ())
    {
      int             wildcard = None;
      StringTokenizer tokens   = new StringTokenizer ((String)e.nextElement (), "*+.", true);
      if (tokens.hasMoreTokens ())
      {
        String token = tokens.nextToken ();
        if (tmpString.startsWith (token))
        {
          tmpString = tmpString.substring (token.length ());
          while (tokens.hasMoreTokens () && found)
          {
            token = tokens.nextToken ();
            if (token.equals ("*"))
              wildcard = Star;
            else if (token.equals ("+"))
              wildcard = Plus;
            else if (token.equals ("."))
              wildcard = Dot;
            else if (wildcard == Star)
            {
              int index = tmpString.indexOf (token);
              if (index >= 0)
                tmpString = tmpString.substring (index + token.length ());
              else
                found = false;
            }
            else if (wildcard == Plus)
            {
              int index = tmpString.indexOf (token);
              if (index > 0)
                tmpString = tmpString.substring (index + token.length ());
              else
                found = false;
            }
            else if (wildcard == Dot)
            {
              int index = tmpString.indexOf (token);
              if (index == 1)
                tmpString = tmpString.substring (1 + token.length ());
              else
                found = false;
            }
          }
          if (found && tmpString.equals (""))
            break;
        }
      }
    }
    return found && tmpString.equals ("");
  } 
  private String matchesOpenWildcard (String string)
  {
    Enumeration e = openEndedKeywords.elements ();
    String prepend = "";
    while (e.hasMoreElements ())
    {
      int             wildcard  = None;
      boolean         found     = true;
      String          tmpString = string;
      StringTokenizer tokens    = new StringTokenizer ((String)e.nextElement (), "*+.", true);
      while (tokens.hasMoreTokens () && found)
      {
        String token = tokens.nextToken ();
        if (token.equals ("*"))
          wildcard = Star;
        else if (token.equals ("+"))
          wildcard = Plus;
        else if (token.equals ("."))
          wildcard = Dot;
        else if (wildcard == Star)
        {
          wildcard = None;
          int index = tmpString.lastIndexOf (token);
          if (index >= 0)
            tmpString = blankOutMatch (tmpString, index, token.length ());
          else
            found = false;
        }
        else if (wildcard == Plus)
        {
          wildcard = None;
          int index = tmpString.lastIndexOf (token);
          if (index > 0)
            tmpString = blankOutMatch (tmpString, index, token.length ());
          else
            found = false;
        }
        else if (wildcard == Dot)
        {
          wildcard = None;
          int index = tmpString.lastIndexOf (token);
          if (index == 1)
            tmpString = blankOutMatch (tmpString, 1, token.length ());
          else
            found = false;
        }
        else if (wildcard == None)
          if (tmpString.startsWith (token))
            tmpString = blankOutMatch (tmpString, 0, token.length ());
          else
            found = false;
      }
      if (found)
      {
        if (wildcard == Star)
          ;
        else if (wildcard == Plus && tmpString.lastIndexOf (' ') != tmpString.length () - 1)
          ;
        else if (wildcard == Dot && tmpString.lastIndexOf (' ') == tmpString.length () - 2)
          ;
        else if (wildcard == None && tmpString.lastIndexOf (' ') == tmpString.length () - 1)
          ;
        else
          found = false;
      }
      if (found)
      {
        prepend = prepend + "_" + matchesOpenWildcard (tmpString.trim ());
        break;
      }
    }
    return prepend;
  } 
  private String blankOutMatch (String string, int start, int length)
  {
    char[] blanks = new char [length];
    for (int i = 0; i < length; ++i)
      blanks[i] = ' ';
    return string.substring (0, start) + new String (blanks) + string.substring (start + length);
  } 
  private String getIdentifier (String string)
  {
    if (keywords.contains (string))
      string = '_' + string;
    else
    {
      String prepend = "";
      if (matchesClosedWildKeyword (string))
        prepend = "_";
      else
        prepend = matchesOpenWildcard (string);
      string = prepend + string;
    }
    return string;
  } 
  private Token getDirective () throws IOException
  {
    readChar ();
    String string = new String ();
    while ((data.ch >= 'a' && data.ch <= 'z') || (data.ch >= 'A' && data.ch <= 'Z'))
    {
      string = string + data.ch;
      readChar ();
    }
    unread (data.ch);
    for (int i = 0; i < Token.Directives.length; ++i)
      if (string.equals (Token.Directives[i]))
        return new Token (Token.FirstDirective + i);
    return new Token (Token.Unknown, string);
  } 
  private Token getNumber () throws IOException
  {
    if (data.ch == '.')
      return getFractionNoInteger ();
    else if (data.ch == '0')
      return isItHex ();
    else 
      return getInteger ();
  } 
  private Token getFractionNoInteger () throws IOException
  {
    readChar ();
    if (data.ch >= '0' && data.ch <= '9')
      return getFraction (".");
    else
      return new Token (Token.Period);
  } 
  private Token getFraction (String string) throws IOException
  {
    while (data.ch >= '0' && data.ch <= '9')
    {
      string = string + data.ch;
      readChar ();
    }
    if (data.ch == 'e' || data.ch == 'E')
      return getExponent (string + 'E');
    else
      return new Token (Token.FloatingPointLiteral, string);
  } 
  private Token getExponent (String string) throws IOException
  {
    readChar ();
    if (data.ch == '+' || data.ch == '-')
    {
      string = string + data.ch;
      readChar ();
    }
    else if (data.ch < '0' || data.ch > '9')
      throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
    while (data.ch >= '0' && data.ch <= '9')
    {
      string = string + data.ch;
      readChar ();
    }
    return new Token (Token.FloatingPointLiteral, string);
  } 
  private Token isItHex () throws IOException
  {
    readChar ();
    if (data.ch == '.')
    {
      readChar ();
      return getFraction ("0.");
    }
    else if (data.ch == 'x' || data.ch == 'X')
      return getHexNumber ("0x");
    else if (data.ch == '8' || data.ch == '9')
      throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
    else if (data.ch >= '0' && data.ch <= '7')
      return getOctalNumber ();
    else if (data.ch == 'e' || data.ch == 'E')
      return getExponent ("0E");
    else
      return new Token (Token.IntegerLiteral, "0");
  } 
  private Token getOctalNumber () throws IOException
  {
    String string = "0" + data.ch;
    readChar ();
    while ((data.ch >= '0' && data.ch <= '9'))
    {
      if (data.ch == '8' || data.ch == '9')
        throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
      string = string + data.ch;
      readChar ();
    }
    return new Token (Token.IntegerLiteral, string);
  } 
  private Token getHexNumber (String string) throws IOException
  {
    readChar ();
    if ((data.ch < '0' || data.ch > '9') && (data.ch < 'a' || data.ch > 'f') && (data.ch < 'A' || data.ch > 'F'))
      throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
    else
      while ((data.ch >= '0' && data.ch <= '9') || (data.ch >= 'a' && data.ch <= 'f') || (data.ch >= 'A' && data.ch <= 'F'))
      {
        string = string + data.ch;
        readChar ();
      }
    return new Token (Token.IntegerLiteral, string);
  } 
  private int getNDigitHexNumber (int n) throws IOException
  {
    readChar ();
    if (!isHexChar (data.ch))
      throw new InvalidCharacter (data.filename, currentLine (),
          currentLineNumber (), currentLinePosition (), data.ch);
    String string = "" + data.ch;
    readChar ();
    for (int i = 2; i <= n; i++)
    {
      if (!isHexChar( data.ch))
        break;
      string += data.ch;
      readChar ();
    }
    try
    {
      return Integer.parseInt (string, 16);
    }
    catch (NumberFormatException e)
    {
    }
    return 0;
  } 
  private boolean isHexChar ( char hex )
  {
    return ((data.ch >= '0') && (data.ch <= '9')) ||
        ((data.ch >= 'a') && (data.ch <= 'f')) ||
        ((data.ch >= 'A') && (data.ch <= 'F'));
  }
  private int get3DigitOctalNumber () throws IOException
  {
    char firstDigit = data.ch;
    String string = "" + data.ch;
    readChar ();
    if (data.ch >= '0' && data.ch <= '7')
    {
      string = string + data.ch;
      readChar ();
      if (data.ch >= '0' && data.ch <= '7')
      {
        string = string + data.ch;
        if (firstDigit > '3')
          throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), firstDigit);
        readChar ();
      }
    }
    int ret = 0;
    try
    {
      ret = Integer.parseInt (string, 8);
    }
    catch (NumberFormatException e)
    {
      throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), string.charAt (0));
    }
    return ret;
  } 
  private Token getInteger () throws IOException
  {
    String string = "" + data.ch;
    readChar ();
    if (data.ch == '.')
    {
      readChar ();
      return getFraction (string + '.');
    }
    else  if (data.ch == 'e' || data.ch == 'E')
      return getExponent (string + 'E');
    else if (data.ch >= '0' && data.ch <= '9')
      while (data.ch >= '0' && data.ch <= '9')
      {
        string = string + data.ch;
        readChar ();
        if (data.ch == '.')
        {
          readChar ();
          return getFraction (string + '.');
        }
      }
    return new Token (Token.IntegerLiteral, string);
  } 
  private Token replaceTrigraph () throws IOException
  {
    readChar ();
    if (data.ch == '?')
    {
      readChar ();
      if (data.ch == '=')
        data.ch = '#';
      else if (data.ch == '/')
        data.ch = '\\';
      else if (data.ch == '\'')
        data.ch = '^';
      else if (data.ch == '(')
        data.ch = '[';
      else if (data.ch == ')')
        data.ch = ']';
      else if (data.ch == '!')
        data.ch = '|';
      else if (data.ch == '<')
        data.ch = '{';
      else if (data.ch == '>')
        data.ch = '}';
      else if (data.ch == '-')
        data.ch = '~';
      else
      {
        unread (data.ch);
        unread ('?');
        throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
      }
      return getToken ();
    }
    else
    {
      unread ('?');
      throw new InvalidCharacter (data.filename, currentLine (), currentLineNumber (), currentLinePosition (), data.ch);
    }
  } 
  void skipWhiteSpace () throws IOException
  {
    while (data.ch <= ' ')
      readChar ();
  } 
  private void skipBlockComment () throws IOException
  {
    try
    {
      boolean done = false;
      readChar ();
      while (!done)
      {
        while (data.ch != '*')
          readChar ();
        readChar ();
        if (data.ch == '/')
          done = true;
      }
    }
    catch (EOFException e)
    {
      ParseException.unclosedComment (data.filename);
      throw e;
    }
  } 
  void skipLineComment () throws IOException
  {
    while (data.ch != '\n')
      readChar ();
  } 
  private String getLineComment () throws IOException
  {
    StringBuffer sb = new StringBuffer( "/" );
    while (data.ch != '\n')
    {
      if (data.ch != '\r')
        sb.append (data.ch);
      readChar ();
    }
    return sb.toString();
  } 
  private String getBlockComment () throws IOException
  {
    StringBuffer sb = new StringBuffer ("
  Token skipUntil (char c) throws IOException
  {
    while (data.ch != c)
    {
      if (data.ch == '/')
      {
        readChar ();
        if (data.ch == '/')
        {
          skipLineComment ();
          if (c == '\n') break;
        }
        else if (data.ch == '*')
          skipBlockComment ();
      }
      else
        readChar ();
    }
    return getToken ();
  } 
  String getUntil (char c) throws IOException
  {
      return getUntil (c, true, true, true);
  }
  String getUntil (char c, boolean allowQuote, boolean allowCharLit, boolean allowComment) throws IOException
  {
    String string = "";
    while (data.ch != c)
      string = appendToString (string, allowQuote, allowCharLit, allowComment);
    return string;
  } 
  String getUntil (char c1, char c2) throws IOException
  {
    String string = "";
    while (data.ch != c1 && data.ch != c2)
      string = appendToString (string, false, false, false);
    return string;
  } 
  private String appendToString (String string, boolean allowQuote, boolean allowCharLit, boolean allowComment) throws IOException
  {
    if (allowComment && data.ch == '/')
    {
      readChar ();
      if (data.ch == '/')
        skipLineComment ();
      else if (data.ch == '*')
        skipBlockComment ();
      else
        string = string + '/';
    }
    else if (data.ch == '\\')
    {
      readChar ();
      if (data.ch == '\n')
        readChar ();
      else if (data.ch == '\r')
      {
        readChar ();
        if (data.ch == '\n')
          readChar ();
      }
      else
      {
        string = string + '\\' + data.ch;
        readChar ();
      }
    }
    else
    {
      if (allowCharLit && data.ch == '"')
      {
        readChar ();
        string = string + '"';
        while (data.ch != '"')
          string = appendToString (string, true, false, allowComment);
      }
      else if (allowQuote && allowCharLit && data.ch == '(')
      {
        readChar ();
        string = string + '(';
        while (data.ch != ')')
          string = appendToString (string, false, false, allowComment);
      }
      else if (allowQuote && data.ch == '\'')
      {
        readChar ();
        string = string + "'";
        while (data.ch != '\'')
          string = appendToString (string, false, true, allowComment);
      }
      string = string + data.ch;
      readChar ();
    }
    return string;
  } 
  String getStringToEOL () throws IOException
  {
    String string = new String ();
    while (data.ch != '\n')
    {
      if (data.ch == '\\')
      {
        readChar ();
        if (data.ch == '\n')
          readChar ();
        else if (data.ch == '\r')
        {
          readChar ();
          if (data.ch == '\n')
            readChar ();
        }
        else
        {
          string = string + data.ch;
          readChar ();
        }
      }
      else
      {
        string = string + data.ch;
        readChar ();
      }
    }
    return string;
  } 
  String filename ()
  {
    return data.filename;
  } 
  IncludeEntry fileEntry ()
  {
    return data.fileEntry;
  } 
  int currentLineNumber ()
  {
    return data.line;
  } 
  int lastTokenLineNumber ()
  {
    return data.oldLine;
  } 
  private int BOL; 
  String currentLine ()
  {
    BOL = data.fileIndex - 1;
    try
    {
      if (data.fileBytes[BOL - 1] == '\r' && data.fileBytes[BOL] == '\n')
        BOL -= 2;
      else if (data.fileBytes[BOL] == '\n')
        --BOL;
      while (data.fileBytes[BOL] != '\n')
        --BOL;
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      BOL = -1;
    }
    ++BOL; 
    int EOL = data.fileIndex - 1;
    try
    {
      while (data.fileBytes[EOL] != '\n' && data.fileBytes[EOL] != '\r')
        ++EOL;
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      EOL = data.fileBytes.length;
    }
    if (BOL < EOL)
      return new String (data.fileBytes, BOL, EOL - BOL);
    else
      return "";
  } 
  String lastTokenLine ()
  {
    int saveFileIndex = data.fileIndex;
    data.fileIndex = data.oldIndex;
    String ret = currentLine ();
    data.fileIndex = saveFileIndex;
    return ret;
  } 
  int currentLinePosition ()
  {
    return data.fileIndex - BOL;
  } 
  int lastTokenLinePosition ()
  {
    return data.oldIndex - BOL;
  } 
  private ScannerData data              = new ScannerData ();
  private Stack       dataStack         = new Stack ();
  private Vector      keywords          = new Vector ();
  private Vector      openEndedKeywords = new Vector ();
  private Vector      wildcardKeywords  = new Vector ();
  private boolean     verbose;
          boolean     escapedOK         = true;
  private boolean     emitAll;
  private float       corbaLevel;
  private boolean     debug ;
} 
class ScannerData
{
  public ScannerData ()
  {
  } 
  public ScannerData (ScannerData that)
  {
    indent          = that.indent;
    fileEntry       = that.fileEntry;
    filename        = that.filename;
    fileBytes       = that.fileBytes;
    fileIndex       = that.fileIndex;
    oldIndex        = that.oldIndex;
    ch              = that.ch;
    line            = that.line;
    oldLine         = that.oldLine;
    macrodata       = that.macrodata;
    includeIsImport = that.includeIsImport;
  } 
  String       indent          = "";
  IncludeEntry fileEntry       = null;
  String       filename        = "";
  char[]       fileBytes       = null;
  int          fileIndex       = 0;
  int          oldIndex        = 0;
  char         ch;
  int          line            = 1;
  int          oldLine         = 1;
  boolean      macrodata       = false;
  boolean      includeIsImport = false;
} 
