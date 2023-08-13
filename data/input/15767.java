public class Preprocessor
{
  Preprocessor ()
  {
  } 
  void init (Parser p)
  {
    parser  = p;
    symbols = p.symbols;
    macros  = p.macros;
  } 
  protected Object clone ()
  {
    return new Preprocessor ();
  } 
  Token process (Token t) throws IOException, ParseException
  {
    token   = t;
    scanner = parser.scanner;
    scanner.escapedOK = false;
    try
    {
      switch (token.type)
      {
        case Token.Include:
          include ();
          break;
        case Token.If:
          ifClause ();
          break;
        case Token.Ifdef:
          ifdef (false);
          break;
        case Token.Ifndef:
          ifdef (true);
          break;
        case Token.Else:
          if (alreadyProcessedABranch.empty ())
            throw ParseException.elseNoIf (scanner);
          else if (((Boolean)alreadyProcessedABranch.peek ()).booleanValue ())
            skipToEndif ();
          else
          {
            alreadyProcessedABranch.pop ();
            alreadyProcessedABranch.push (new Boolean (true));
            token = scanner.getToken ();
          }
          break;
        case Token.Elif:
          elif ();
          break;
        case Token.Endif:
          if (alreadyProcessedABranch.empty ())
            throw ParseException.endNoIf (scanner);
          else
          {
            alreadyProcessedABranch.pop ();
            token = scanner.getToken ();
            break;
          }
        case Token.Define:
          define ();
          break;
        case Token.Undef:
          undefine ();
          break;
        case Token.Pragma:
          pragma ();
          break;
        case Token.Unknown:
          if (!parser.noWarn)
            ParseException.warning (scanner, Util.getMessage ("Preprocessor.unknown", token.name));
        case Token.Error:
        case Token.Line:
        case Token.Null:
        default:
          scanner.skipLineComment ();
          token = scanner.getToken ();
      }
    }
    catch (IOException e)
    {
      scanner.escapedOK = true;
      throw e;
    }
    catch (ParseException e)
    {
      scanner.escapedOK = true;
      throw e;
    }
    scanner.escapedOK = true;
    return token;
  } 
  private void include () throws IOException, ParseException
  {
    match (Token.Include);
    IncludeEntry include = parser.stFactory.includeEntry (parser.currentModule);
    include.sourceFile (scanner.fileEntry ());
    scanner.fileEntry ().addInclude (include);
    if (token.type == Token.StringLiteral)
      include2 (include);
    else if (token.type == Token.LessThan)
      include3 (include);
    else
    {
      int[] expected = {Token.StringLiteral, Token.LessThan};
      throw ParseException.syntaxError (scanner, expected, token.type);
    }
    if (parser.currentModule instanceof ModuleEntry)
      ((ModuleEntry)parser.currentModule).addContained (include);
    else if (parser.currentModule instanceof InterfaceEntry)
      ((InterfaceEntry)parser.currentModule).addContained (include);
  } 
  private void include2 (IncludeEntry include) throws IOException, ParseException
  {
    include.name ('"' + token.name + '"');
    include4 (include, token.name);
    match (Token.StringLiteral);
  } 
  private void include3 (IncludeEntry include) throws IOException, ParseException
  {
    if (token.type != Token.LessThan)
      match (Token.LessThan);
    else
    {
      try
      {
        String includeFile = getUntil ('>');
        token = scanner.getToken ();
        include.name ('<' + includeFile + '>');
        include4 (include, includeFile);
        match (Token.GreaterThan);
      }
      catch (IOException e)
      {
        throw ParseException.syntaxError (scanner, ">", "EOF");
      }
    }
  } 
  private void include4 (IncludeEntry include, String filename) throws IOException, ParseException
  {
    try
    {
      boolean includeIsImport = parser.currentModule == parser.topLevelModule;
      include.absFilename (Util.getAbsolutePath (filename, parser.paths));
      scanner.scanIncludedFile (include, getFilename (filename), includeIsImport);
    }
    catch (IOException e)
    {
      ParseException.generic (scanner, e.toString ());
    }
  } 
  private void define () throws IOException, ParseException
  {
    match (Token.Define);
    if (token.equals (Token.Identifier))
    {
      String symbol = scanner.getStringToEOL ();
      symbols.put (token.name, symbol.trim ());
      match (Token.Identifier);
    }
    else if (token.equals (Token.MacroIdentifier))
    {
      symbols.put (token.name, '(' + scanner.getStringToEOL () . trim ());
      macros.addElement (token.name);
      match (Token.MacroIdentifier);
    }
    else
      throw ParseException.syntaxError (scanner, Token.Identifier, token.type);
  } 
  private void undefine () throws IOException, ParseException
  {
    match (Token.Undef);
    if (token.equals (Token.Identifier))
    {
      symbols.remove (token.name);
      macros.removeElement (token.name);
      match (Token.Identifier);
    }
    else
      throw ParseException.syntaxError (scanner, Token.Identifier, token.type);
  } 
  private void ifClause () throws IOException, ParseException
  {
    match (Token.If);
    constExpr ();
  } 
  private void constExpr () throws IOException, ParseException
  {
    SymtabEntry dummyEntry = new SymtabEntry (parser.currentModule);
    dummyEntry.container (parser.currentModule);
    parser.parsingConditionalExpr = true;
    Expression boolExpr = booleanConstExpr (dummyEntry);
    parser.parsingConditionalExpr = false;
    boolean expr;
    if (boolExpr.value () instanceof Boolean)
      expr = ((Boolean)boolExpr.value ()).booleanValue ();
    else
      expr = ((Number)boolExpr.value ()).longValue () != 0;
    alreadyProcessedABranch.push (new Boolean (expr));
    if (!expr)
      skipToEndiforElse ();
  } 
  Expression booleanConstExpr (SymtabEntry entry) throws IOException, ParseException
  {
    Expression expr = orExpr (null, entry);
    try
    {
      expr.evaluate ();
    }
    catch (EvaluationException e)
    {
      ParseException.evaluationError (scanner, e.toString ());
    }
    return expr;
  } 
  private Expression orExpr (Expression e, SymtabEntry entry) throws IOException, ParseException
  {
    if (e == null)
      e = andExpr (null, entry);
    else
    {
      BinaryExpr b = (BinaryExpr)e;
      b.right (andExpr (null, entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (Token.DoubleBar))
    {
      match (token.type);
      BooleanOr or = parser.exprFactory.booleanOr (e, null);
      or.rep (e.rep () + " || ");
      return orExpr (or, entry);
    }
    else
      return e;
  } 
  private Expression andExpr (Expression e, SymtabEntry entry) throws IOException, ParseException
  {
    if (e == null)
      e = notExpr (entry);
    else
    {
      BinaryExpr b = (BinaryExpr)e;
      b.right (notExpr (entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (Token.DoubleAmpersand))
    {
      match (token.type);
      BooleanAnd and = parser.exprFactory.booleanAnd (e, null);
      and.rep (e.rep () + " && ");
      return andExpr (and, entry);
    }
    else
      return e;
  } 
  private Expression notExpr (SymtabEntry entry) throws IOException, ParseException
  {
    Expression e;
    if (token.equals (Token.Exclamation))
    {
      match (Token.Exclamation);
      e = parser.exprFactory.booleanNot (definedExpr (entry));
      e.rep ("!" + ((BooleanNot)e).operand ().rep ());
    }
    else
      e = definedExpr (entry);
    return e;
  } 
  private Expression definedExpr (SymtabEntry entry) throws IOException, ParseException
  {
    if (token.equals (Token.Identifier) && token.name.equals ("defined"))
      match (Token.Identifier);
    return equalityExpr (null, entry);
  } 
  private Expression equalityExpr (Expression e, SymtabEntry entry) throws IOException, ParseException
  {
    if (e == null)
    {
      parser.token = token; 
      e = parser.constExp (entry);
      token = parser.token; 
    }
    else
    {
      BinaryExpr b = (BinaryExpr)e;
      parser.token = token; 
      Expression constExpr = parser.constExp (entry);
      token = parser.token; 
      b.right (constExpr);
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (Token.DoubleEqual))
    {
      match (token.type);
      Equal eq = parser.exprFactory.equal (e, null);
      eq.rep (e.rep () + " == ");
      return equalityExpr (eq, entry);
    }
    else if (token.equals (Token.NotEqual))
    {
      match (token.type);
      NotEqual n = parser.exprFactory.notEqual (e, null);
      n.rep (e.rep () + " != ");
      return equalityExpr (n, entry);
    }
    else if (token.equals (Token.GreaterThan))
    {
      match (token.type);
      GreaterThan g = parser.exprFactory.greaterThan (e, null);
      g.rep (e.rep () + " > ");
      return equalityExpr (g, entry);
    }
    else if (token.equals (Token.GreaterEqual))
    {
      match (token.type);
      GreaterEqual g = parser.exprFactory.greaterEqual (e, null);
      g.rep (e.rep () + " >= ");
      return equalityExpr (g, entry);
    }
    else if (token.equals (Token.LessThan))
    {
      match (token.type);
      LessThan l = parser.exprFactory.lessThan (e, null);
      l.rep (e.rep () + " < ");
      return equalityExpr (l, entry);
    }
    else if (token.equals (Token.LessEqual))
    {
      match (token.type);
      LessEqual l = parser.exprFactory.lessEqual (e, null);
      l.rep (e.rep () + " <= ");
      return equalityExpr (l, entry);
    }
    else
      return e;
  } 
  Expression primaryExpr (SymtabEntry entry) throws IOException, ParseException
  {
    Expression primary = null;
    switch (token.type)
    {
      case Token.Identifier:
        primary = parser.exprFactory.terminal ("0", BigInteger.valueOf (0));
        token = scanner.getToken ();
        break;
      case Token.BooleanLiteral:
      case Token.CharacterLiteral:
      case Token.IntegerLiteral:
      case Token.FloatingPointLiteral:
      case Token.StringLiteral:
        primary = parser.literal (entry);
        token = parser.token;
        break;
      case Token.LeftParen:
        match (Token.LeftParen);
        primary = booleanConstExpr (entry);
        match (Token.RightParen);
        primary.rep ('(' + primary.rep () + ')');
        break;
      default:
        int[] expected = {Token.Literal, Token.LeftParen};
        throw ParseException.syntaxError (scanner, expected, token.type);
    }
    return primary;
  } 
  private void ifDefine (boolean inParens, boolean not) throws IOException, ParseException
  {
    if (token.equals (Token.Identifier))
      if ((not && symbols.containsKey (token.name)) || (!not && !symbols.containsKey (token.name)))
      {
        alreadyProcessedABranch.push (new Boolean (false));
        skipToEndiforElse ();
      }
      else
      {
        alreadyProcessedABranch.push (new Boolean (true));
        match (Token.Identifier);
        if (inParens)
          match (Token.RightParen);
      }
    else
      throw ParseException.syntaxError (scanner, Token.Identifier, token.type);
  } 
  private void ifdef (boolean not) throws IOException, ParseException
  {
    if (not)
      match (Token.Ifndef);
    else
      match (Token.Ifdef);
    if (token.equals (Token.Identifier))
      if ((not && symbols.containsKey (token.name)) || (!not && !symbols.containsKey (token.name)))
      {
        alreadyProcessedABranch.push (new Boolean (false));
        skipToEndiforElse ();
      }
      else
      {
        alreadyProcessedABranch.push (new Boolean (true));
        match (Token.Identifier);
      }
    else
      throw ParseException.syntaxError (scanner, Token.Identifier, token.type);
  } 
  private void elif () throws IOException, ParseException
  {
    if (alreadyProcessedABranch.empty ())
      throw ParseException.elseNoIf (scanner);
    else if (((Boolean)alreadyProcessedABranch.peek ()).booleanValue ())
      skipToEndif ();
    else
    {
      match (Token.Elif);
      constExpr ();
    }
  } 
  private void skipToEndiforElse () throws IOException, ParseException
  {
    while (!token.equals (Token.Endif) && !token.equals (Token.Else) && !token.equals (Token.Elif))
    {
      if (token.equals (Token.Ifdef) || token.equals (Token.Ifndef))
      {
        alreadyProcessedABranch.push (new Boolean (true));
        skipToEndif ();
      }
      else
        token = scanner.skipUntil ('#');
    }
    process (token);
  } 
  private void skipToEndif () throws IOException, ParseException
  {
    while (!token.equals (Token.Endif))
    {
      token = scanner.skipUntil ('#');
      if (token.equals (Token.Ifdef) || token.equals (Token.Ifndef))
      {
        alreadyProcessedABranch.push (new Boolean (true));
        skipToEndif ();
      }
    }
    alreadyProcessedABranch.pop ();
    match (Token.Endif);
  } 
  private void pragma () throws IOException, ParseException
  {
    match (Token.Pragma);
    String pragmaType = token.name;
    scanner.escapedOK = true;
    match (Token.Identifier);
    PragmaEntry pragmaEntry = parser.stFactory.pragmaEntry (parser.currentModule);
    pragmaEntry.name (pragmaType);
    pragmaEntry.sourceFile (scanner.fileEntry ());
    pragmaEntry.data (scanner.currentLine ());
    if (parser.currentModule instanceof ModuleEntry)
      ((ModuleEntry)parser.currentModule).addContained (pragmaEntry);
    else if (parser.currentModule instanceof InterfaceEntry)
      ((InterfaceEntry)parser.currentModule).addContained (pragmaEntry);
    if (pragmaType.equals ("ID"))
      idPragma ();
    else if (pragmaType.equals ("prefix"))
      prefixPragma ();
    else if (pragmaType.equals ("version"))
      versionPragma ();
    else if (pragmaType.equals ("sun_local"))
      localPragma();
    else if (pragmaType.equals ("sun_localservant"))
      localServantPragma();
    else
    {
      otherPragmas (pragmaType, tokenToString ());
      token = scanner.getToken ();
    }
    scanner.escapedOK = false; 
  } 
  private Vector PragmaIDs = new Vector ();
  private void localPragma () throws IOException, ParseException
  {
    parser.token = token;
    SymtabEntry anErrorOccurred = new SymtabEntry ();
    SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);
    if (entry == anErrorOccurred)
    {
        System.out.println("Error occured ");
      scanner.skipLineComment ();
      token = scanner.getToken ();
    }
    else
    {
      if (entry instanceof InterfaceEntry) {
          InterfaceEntry ent = (InterfaceEntry) entry;
          ent.setInterfaceType (InterfaceEntry.LOCAL_SIGNATURE_ONLY);
      }
      token = parser.token;
      String string = token.name;
      match (Token.StringLiteral);
    }
  } 
  private void localServantPragma () throws IOException, ParseException
  {
    parser.token = token;
    SymtabEntry anErrorOccurred = new SymtabEntry ();
    SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);
    if (entry == anErrorOccurred)
    {
      scanner.skipLineComment ();
      token = scanner.getToken ();
        System.out.println("Error occured ");
    }
    else
    {
      if (entry instanceof InterfaceEntry) {
          InterfaceEntry ent = (InterfaceEntry) entry;
          ent.setInterfaceType (InterfaceEntry.LOCALSERVANT);
      }
      token = parser.token;
      String string = token.name;
      match (Token.StringLiteral);
    }
  } 
  private void idPragma () throws IOException, ParseException
  {
    parser.token = token;
    parser.isModuleLegalType (true);
    SymtabEntry anErrorOccurred = new SymtabEntry ();
    SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);
    parser.isModuleLegalType (false);  
    if (entry == anErrorOccurred)
    {
      scanner.skipLineComment ();
      token = scanner.getToken ();
    }
    else
    {
      token = parser.token;
      String string = token.name;
      if (PragmaIDs.contains (entry)) 
      {
        ParseException.badRepIDAlreadyAssigned (scanner, entry.name ());
      }
      else if (!RepositoryID.hasValidForm (string)) 
      {
        ParseException.badRepIDForm (scanner, string);
      }
      else
      {
        entry.repositoryID (new RepositoryID (string));
        PragmaIDs.addElement (entry); 
      }
      match (Token.StringLiteral);
    }
  } 
  private void prefixPragma () throws IOException, ParseException
  {
    String string = token.name;
    match (Token.StringLiteral);
    ((IDLID)parser.repIDStack.peek ()).prefix (string);
    ((IDLID)parser.repIDStack.peek ()).name ("");
  } 
  private void versionPragma () throws IOException, ParseException
  {
    parser.token = token;
    parser.isModuleLegalType (true);
    SymtabEntry anErrorOccurred = new SymtabEntry ();
    SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);
    parser.isModuleLegalType (false);
    if (entry == anErrorOccurred)
    {
      scanner.skipLineComment ();
      token = scanner.getToken ();
    }
    else
    {
      token = parser.token;
      String string = token.name;
      match (Token.FloatingPointLiteral);
      if (entry.repositoryID () instanceof IDLID)
        ((IDLID)entry.repositoryID ()).version (string);
    }
  } 
  private Vector pragmaHandlers = new Vector ();
  void registerPragma (PragmaHandler handler)
  {
    pragmaHandlers.addElement (handler);
  } 
  private void otherPragmas (String pragmaType, String currentToken) throws IOException
  {
    for (int i = pragmaHandlers.size () - 1; i >= 0; --i)
    {
      PragmaHandler handler = (PragmaHandler)pragmaHandlers.elementAt (i);
      if (handler.process (pragmaType, currentToken))
                break;
    }
  } 
  String currentToken ()
  {
    return tokenToString ();
  } 
  SymtabEntry getEntryForName (String string)
  {
    boolean partialScope = false;
    boolean globalScope  = false;
    if (string.startsWith ("::"))
    {
      globalScope = true;
      string = string.substring (2);
    }
    int index = string.indexOf ("::");
    while (index >= 0)
    {
      partialScope = true;
      string = string.substring (0, index) + '/' + string.substring (index + 2);
      index = string.indexOf ("::");
    }
    SymtabEntry entry = null;
    if (globalScope)
      entry = parser.recursiveQualifiedEntry (string);
    else if (partialScope)
      entry = parser.recursivePQEntry (string, parser.currentModule);
    else
      entry = parser.unqualifiedEntryWMod (string, parser.currentModule);
    return entry;
  } 
  String getStringToEOL () throws IOException
  {
    return scanner.getStringToEOL ();
  } 
  String getUntil (char c) throws IOException
  {
    return scanner.getUntil (c);
  } 
  private boolean lastWasMacroID = false;
  private String tokenToString ()
  {
    if (token.equals (Token.MacroIdentifier))
    {
      lastWasMacroID = true;
      return token.name;
    }
    else if (token.equals (Token.Identifier))
      return token.name;
    else
      return token.toString ();
  } 
  String nextToken () throws IOException
  {
    if (lastWasMacroID)
    {
      lastWasMacroID = false;
      return "(";
    }
    else
    {
      token = scanner.getToken ();
      return tokenToString ();
    }
  } 
  SymtabEntry scopedName () throws IOException
  {
    boolean     globalScope  = false;
    boolean     partialScope = false;
    String      name         = null;
    SymtabEntry entry        = null;
    try
    {
      if (token.equals (Token.DoubleColon))
        globalScope = true;
      else
      {
        if (token.equals (Token.Object))
        {
          name = "Object";
          match (Token.Object);
        }
        else if (token.type == Token.ValueBase)
        {
          name = "ValueBase";
          match (Token.ValueBase);
        }
        else
        {
          name = token.name;
          match (Token.Identifier);
        }
      }
      while (token.equals (Token.DoubleColon))
      {
        match (Token.DoubleColon);
        partialScope = true;
        if (name != null)
          name = name + '/' + token.name;
        else
          name = token.name;
        match (Token.Identifier);
      }
      if (globalScope)
        entry = parser.recursiveQualifiedEntry (name);
      else if (partialScope)
        entry = parser.recursivePQEntry (name, parser.currentModule);
      else
        entry = parser.unqualifiedEntryWMod (name, parser.currentModule);
    }
    catch (ParseException e)
    {
      entry = null;
    }
    return entry;
  } 
  void skipToEOL () throws IOException
  {
    scanner.skipLineComment ();
  } 
  String skipUntil (char c) throws IOException
  {
    if (!(lastWasMacroID && c == '('))
      token = scanner.skipUntil (c);
    return tokenToString ();
  } 
  void parseException (String message)
  {
    if (!parser.noWarn)
      ParseException.warning (scanner, message);
  } 
  String expandMacro (String macroDef, Token t) throws IOException, ParseException
  {
    token = t;
    Vector parmValues = getParmValues ();
    scanner.scanString (macroDef + '\n');
    Vector parmNames = new Vector ();
    macro (parmNames);
    if (parmValues.size () < parmNames.size ())
      throw ParseException.syntaxError (scanner, Token.Comma, Token.RightParen);
    else if (parmValues.size () > parmNames.size ())
      throw ParseException.syntaxError (scanner, Token.RightParen, Token.Comma);
    macroDef = scanner.getStringToEOL ();
    for (int i = 0; i < parmNames.size (); ++i)
      macroDef = replaceAll (macroDef, (String)parmNames.elementAt (i), (String)parmValues.elementAt (i));
    return removeDoublePound (macroDef);
  } 
  private void miniMatch (int type) throws ParseException
  {
    if (!token.equals (type))
      throw ParseException.syntaxError (scanner, type, token.type);
  } 
  private Vector getParmValues () throws IOException, ParseException
  {
    Vector values = new Vector ();
    if (token.equals (Token.Identifier))
    {
      match (Token.Identifier);
      miniMatch (Token.LeftParen);
    }
    else if (!token.equals (Token.MacroIdentifier))
      throw ParseException.syntaxError (scanner, Token.Identifier, token.type);
    if (!token.equals (Token.RightParen))
    {
      values.addElement (scanner.getUntil (',', ')').trim ());
      token = scanner.getToken ();
      macroParmValues (values);
    }
    return values;
  } 
  private void macroParmValues (Vector values) throws IOException, ParseException
  {
    while (!token.equals (Token.RightParen))
    {
      miniMatch (Token.Comma);
      values.addElement (scanner.getUntil (',', ')').trim ());
      token = scanner.getToken ();
    }
  } 
  private void macro (Vector parmNames) throws IOException, ParseException
  {
    match (token.type);
    match (Token.LeftParen);
    macroParms (parmNames);
    miniMatch (Token.RightParen);
  } 
  private void macroParms (Vector parmNames) throws IOException, ParseException
  {
    if (!token.equals (Token.RightParen))
    {
      parmNames.addElement (token.name);
      match (Token.Identifier);
      macroParms2 (parmNames);
    }
  } 
  private void macroParms2 (Vector parmNames) throws IOException, ParseException
  {
    while (!token.equals (Token.RightParen))
    {
      match (Token.Comma);
      parmNames.addElement (token.name);
      match (Token.Identifier);
    }
  } 
  private String replaceAll (String string, String from, String to)
  {
    int index = 0;
    while (index != -1)
    {
      index = string.indexOf (from, index);
      if (index != -1)
      {
        if (!embedded (string, index, index + from.length ()))
          if (index > 0 && string.charAt(index) == '#')
            string = string.substring (0, index) + '"' + to + '"' + string.substring (index + from.length ());
          else
            string = string.substring (0, index) + to + string.substring (index + from.length ());
        index += to.length ();
      }
    }
    return string;
  } 
  private boolean embedded (String string, int index, int endIndex)
  {
    boolean ret    = false;
    char    preCh  = index == 0 ? ' ' : string.charAt (index - 1);
    char    postCh = endIndex >= string.length () - 1 ? ' ' : string.charAt (endIndex);
    if ((preCh >= 'a' && preCh <= 'z') || (preCh >= 'A' && preCh <= 'Z'))
      ret = true;
    else if ((postCh >= 'a' && postCh <= 'z') || (postCh >= 'A' && postCh <= 'Z') || (postCh >= '0' && postCh <= '9') || postCh == '_')
      ret = true;
    else
      ret = inQuotes (string, index);
    return ret;
  } 
  private boolean inQuotes (String string, int index)
  {
    int quoteCount = 0;
    for (int i = 0; i < index; ++i)
      if (string.charAt (i) == '"') ++quoteCount;
    return quoteCount % 2 != 0;
  } 
  private String removeDoublePound (String string)
  {
    int index = 0;
    while (index != -1)
    {
      index = string.indexOf ("##", index);
      if (index != -1)
      {
        int startSkip = index - 1;
        int stopSkip  = index + 2;
        if (startSkip < 0)
          startSkip = 0;
        if (stopSkip >= string.length ())
          stopSkip = string.length () - 1;
        while (startSkip > 0 &&
               (string.charAt (startSkip) == ' ' ||
               string.charAt (startSkip) == '\t'))
          --startSkip;
        while (stopSkip < string.length () - 1 &&
               (string.charAt (stopSkip) == ' ' ||
               string.charAt (stopSkip) == '\t'))
          ++stopSkip;
        string = string.substring (0, startSkip + 1) + string.substring (stopSkip);
      }
    }
    return string;
  } 
  private String getFilename (String name) throws FileNotFoundException
  {
    String fullName = null;
    File file = new File (name);
    if (file.canRead ())
      fullName = name;
    else
    {
      Enumeration pathList = parser.paths.elements ();
      while (!file.canRead () && pathList.hasMoreElements ())
      {
        fullName = (String)pathList.nextElement () + File.separatorChar + name;
        file = new File (fullName);
      }
      if (!file.canRead ())
        throw new FileNotFoundException (name);
    }
    return fullName;
  } 
  private void match (int type) throws IOException, ParseException
  {
    if (!token.equals (type))
      throw ParseException.syntaxError (scanner, type, token.type);
    token = scanner.getToken ();
    if (token.equals (Token.Identifier) || token.equals (Token.MacroIdentifier))
    {
      String string = (String)symbols.get (token.name);
      if (string != null && !string.equals (""))
        if (macros.contains (token.name))
        {
          scanner.scanString (expandMacro (string, token));
          token = scanner.getToken ();
        }
        else
        {
          scanner.scanString (string);
          token = scanner.getToken ();
        }
    }
  } 
  private void issueTokenWarnings ()
  {
    if (parser.noWarn)
      return;
  } 
  void openScope (SymtabEntry entry)
  {
    for (int i = pragmaHandlers.size () - 1; i >= 0; --i)
    {
      PragmaHandler handler = (PragmaHandler)pragmaHandlers.elementAt (i);
      handler.openScope (entry);
    }
  } 
  void closeScope (SymtabEntry entry)
  {
    for (int i = pragmaHandlers.size () - 1; i >= 0; --i)
    {
      PragmaHandler handler = (PragmaHandler)pragmaHandlers.elementAt (i);
      handler.closeScope (entry);
    }
  } 
  private Parser    parser;
  private Scanner   scanner;
  private Hashtable symbols;
  private Vector    macros;
  private        Stack  alreadyProcessedABranch = new Stack ();
                 Token  token;
  private static String indent = "";
}
