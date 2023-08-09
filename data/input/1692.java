class Token
{
  static final int                
      Any                  =   0, 
      Attribute            =   1, 
      Boolean              =   2, 
      Case                 =   3, 
      Char                 =   4, 
      Const                =   5,
      Context              =   6,
      Default              =   7,
      Double               =   8,
      Enum                 =   9,
      Exception            =  10,
      FALSE                =  11,
      Fixed                =  12, 
      Float                =  13,
      In                   =  14,
      Inout                =  15,
      Interface            =  16,
      Long                 =  17,
      Module               =  18,
      Native               =  19, 
      Object               =  20,
      Octet                =  21,
      Oneway               =  22,
      Out                  =  23,
      Raises               =  24,
      Readonly             =  25,
      Sequence             =  26,
      Short                =  27,
      String               =  28,
      Struct               =  29,
      Switch               =  30,
      TRUE                 =  31,
      Typedef              =  32,
      Unsigned             =  33, 
      Union                =  34, 
      Void                 =  35, 
      Wchar                =  36, 
      Wstring              =  37, 
      Init                 =  38, 
      Abstract             =  39, 
      Custom               =  40, 
      Private              =  41, 
      Public               =  42, 
      Supports             =  43, 
      Truncatable          =  44, 
      ValueBase            =  45, 
      Valuetype            =  46, 
      Factory              =  47, 
      Component            =  48,
      Consumes             =  49,
      Emits                =  50,
      Finder               =  51,
      GetRaises            =  52,
      Home                 =  53,
      Import               =  54,
      Local                =  55,
      Manages              =  56,
      Multiple             =  57,
      PrimaryKey           =  58,
      Provides             =  59,
      Publishes            =  60,
      SetRaises            =  61,
      TypeId               =  62,
      TypePrefix           =  63,
      Uses                 =  64,
      Identifier           =  80, 
      MacroIdentifier      =  81, 
      Semicolon            = 100, 
      LeftBrace            = 101,
      RightBrace           = 102,
      Colon                = 103,
      Comma                = 104,
      Equal                = 105,
      Plus                 = 106,
      Minus                = 107,
      LeftParen            = 108,
      RightParen           = 109,
      LessThan             = 110,
      GreaterThan          = 111,
      LeftBracket          = 112,
      RightBracket         = 113,
      Apostrophe           = 114,
      Quote                = 115,
      Backslash            = 116,
      Bar                  = 117,
      Carat                = 118,
      Ampersand            = 119,
      Star                 = 120,
      Slash                = 121,
      Percent              = 122,
      Tilde                = 123,
      DoubleColon          = 124,
      ShiftLeft            = 125,
      ShiftRight           = 126,
      Period               = 127,
      Hash                 = 128,
      Exclamation          = 129,
      DoubleEqual          = 130,
      NotEqual             = 131,
      GreaterEqual         = 132,
      LessEqual            = 133,
      DoubleBar            = 134,
      DoubleAmpersand      = 135,
      BooleanLiteral       = 200, 
      CharacterLiteral     = 201,
      IntegerLiteral       = 202,
      FloatingPointLiteral = 203,
      StringLiteral        = 204,
      Literal              = 205,
      Define               = 300, 
      Undef                = 301,
      If                   = 302,
      Ifdef                = 303,
      Ifndef               = 304,
      Else                 = 305,
      Elif                 = 306,
      Include              = 307,
      Endif                = 308,
      Line                 = 309,
      Error                = 310,
      Pragma               = 311,
      Null                 = 312,
      Unknown              = 313,
      Defined              = 400,
      EOF                  = 999; 
  static final String [] Keywords = {
      "any",         "attribute",    "boolean",
      "case",        "char",         "const",
      "context",     "default",      "double",
      "enum",        "exception",    "FALSE",      "fixed",
      "float",       "in",           "inout",
      "interface",   "long",         "module",     "native",
      "Object",      "octet",        "oneway",
      "out",         "raises",       "readonly",
      "sequence",    "short",        "string",
      "struct",      "switch",       "TRUE",
      "typedef",     "unsigned",     "union",
      "void",        "wchar",        "wstring",
      "init", 
      "abstract",     "custom",      "private",      
      "public",       "supports",    "truncatable",
      "ValueBase",    "valuetype",
      "factory",  
      "component",      "consumes",     "emits",
      "finder",         "getRaises",    "home",
      "import",         "local",        "manages",
      "multiple",       "primaryKey",   "provides",
      "publishes",      "setRaises",    "supports",
      "typeId",         "typePrefix",   "uses" } ;
  boolean isKeyword ()
  {
    return type >= FirstKeyword && type <= LastKeyword;
  } 
  private static final int
      FirstKeyword = Any, 
      LastKeyword  = Uses;
  private static final int
      First22Keyword = Any, 
      Last22Keyword  = Wstring;
  private static final int
      First23Keyword = Init,
      Last23Keyword  = Valuetype;
  private static final int
      First24rtfKeyword = Abstract,
      Last24rtfKeyword  = Factory;
  private static final int
      First30Keyword    = Component,
      Last30Keyword     = Uses;
  private static final int CORBA_LEVEL_22 = 0 ;
  private static final int CORBA_LEVEL_23 = 1 ;
  private static final int CORBA_LEVEL_24RTF = 2 ;
  private static final int CORBA_LEVEL_30 = 3 ;
  private static int getLevel( float cLevel )
  {
    if (cLevel < 2.3f)
        return CORBA_LEVEL_22 ;
    if (Util.absDelta( cLevel, 2.3f ) < 0.001f)
        return CORBA_LEVEL_23 ;
    if (cLevel < 3.0f)
        return CORBA_LEVEL_24RTF ;
    return CORBA_LEVEL_30 ;
  }
  private static int getLastKeyword( int level )
  {
    if (level == CORBA_LEVEL_22)
        return Last22Keyword ;
    if (level == CORBA_LEVEL_23)
        return Last23Keyword ;
    if (level == CORBA_LEVEL_24RTF)
        return Last24rtfKeyword ;
    return Last30Keyword ;
  }
  public static Token makeKeywordToken(
    String string, float corbaLevel, boolean escapedOK, boolean[] collision )
  {
    int level = getLevel( corbaLevel ) ;
    int lastKeyword = getLastKeyword( level ) ;
    boolean deprecated = false ;
    collision[0] = false ;
    for (int i = Token.FirstKeyword; i <= Token.LastKeyword; ++i) {
        if (string.equals (Token.Keywords[i])) {
            if (i == Token.Init) {
                if (level == CORBA_LEVEL_23)
                    deprecated = true ;
                else
                    break ;
            }
            if (i > lastKeyword) {
                collision[0] |= escapedOK; 
                break ;
            }
            if (string.equals ("TRUE") || string.equals ("FALSE"))
                return new Token (Token.BooleanLiteral, string) ;
            else
                return new Token (i, deprecated);
        } else if (string.equalsIgnoreCase (Token.Keywords[i])) {
            collision[0] |= true;
            break;
        }
    } 
    return null ;
  } 
  static final int
      FirstSymbol = 100,
      LastSymbol  = 199;
  static final String [] Symbols = {
      ";",  "{",  "}",  ":", ",", "=", "+",  "-",
      "(",  ")",  "<",  ">", "[", "]", "'",  "\"",
      "\\", "|",  "^",  "&", "*", "/", "%",  "~",
      "::", "<<", ">>", ".", "#", "!", "==", "!=",
      ">=", "<=", "||", "&&"};
  static final int
      FirstLiteral = 200,
      LastLiteral  = 299;
  static final String [] Literals = {
      Util.getMessage ("Token.boolLit"),
      Util.getMessage ("Token.charLit"),
      Util.getMessage ("Token.intLit"),
      Util.getMessage ("Token.floatLit"),
      Util.getMessage ("Token.stringLit"),
      Util.getMessage ("Token.literal")};
  boolean isDirective ()
  {
    return type >= FirstDirective && type <= LastDirective;
  } 
  static final int
      FirstDirective = 300,
      LastDirective  = 399;
  static final String [] Directives = {
      "define", "undef",  "if",
      "ifdef",  "ifndef", "else",
      "elif",   "include","endif",
      "line",   "error",  "pragma",
      ""};
  static final int
      FirstSpecial = 400,
      LastSpecial  = 499;
  static final String [] Special = {
      "defined"};
  Token (int tokenType)
  {
    type = tokenType;
  } 
  Token (int tokenType, boolean deprecated)
  {
    this.type = tokenType;
    this.isDeprecated = deprecated;
  } 
  Token (int tokenType, String tokenName)
  {
    type = tokenType;
    name = tokenName;
  } 
  Token (int tokenType, String tokenName, boolean isWide)
  {
    this (tokenType, tokenName);
    this.isWide = isWide;
  } 
  Token (int tokenType, String tokenName, boolean escaped,
      boolean collision, boolean deprecated)
  {
    this (tokenType, tokenName);
    this.isEscaped = escaped;
    this.collidesWithKeyword = collision;
    this.isDeprecated = deprecated;
  } 
  public String toString ()
  {
    if (type == Identifier)
      return name;
    if (type == MacroIdentifier)
      return name + '(';
    return Token.toString (type);
  } 
  static String toString (int type)
  {
    if (type <= LastKeyword)
      return Keywords[type];
    if (type == Identifier || type == MacroIdentifier)
      return Util.getMessage ("Token.identifier");
    if (type <= LastSymbol)
      return Symbols[type - FirstSymbol];
    if (type <= LastLiteral)
      return Literals[type - FirstLiteral];
    if (type <= LastDirective)
      return Directives[type - FirstDirective];
    if (type <= LastSpecial)
      return Special[type - FirstSpecial];
    if (type == EOF)
      return Util.getMessage ("Token.endOfFile");
    return Util.getMessage ("Token.unknown");
  } 
  boolean equals (Token that)
  {
    if (this.type == that.type)
      if (this.name == null)
        return that.name == null;
      else
        return this.name.equals (that.name);
    return false;
  } 
  boolean equals (int type)
  {
    return this.type == type;
  } 
  boolean equals (String name)
  {
    return (this.type == Identifier && this.name.equals (name));
  } 
  public boolean isEscaped ()
  {
    return type == Identifier && isEscaped;
  } 
  public boolean collidesWithKeyword ()
  {
    return collidesWithKeyword;
  } 
  public boolean isDeprecated ()
  {
    return isDeprecated;
  }
  public boolean isWide()
  {
      return isWide ;
  }
  int type;
  String name = null;
  Comment comment = null;
  boolean isEscaped = false; 
  boolean collidesWithKeyword = false;  
  boolean isDeprecated = false;  
  boolean isWide = false ;  
} 
