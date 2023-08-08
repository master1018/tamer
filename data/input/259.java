public class Grammar {
    public static final int ERROR = -2;
    public static final int EOS = -1;
    public static final int COMMENT = 0;
    public static final int WHITESPACE = 1;
    public static final int LOOKAHEAD = 2;
    public static final int NOT = 3;
    public static final int IDENTIFIER = 4;
    public static final int TOKEN = 5;
    public static final int COLON = 6;
    public static final int SEMICOLON = 7;
    public static final int PIPE = 8;
    public static final int OPTION = 9;
    public static final int KLEENE = 10;
    public static final int POSITIVE = 11;
    public static final int LBRACKET = 12;
    public static final int RBRACKET = 13;
    public static final int LCURLY = 14;
    public static final int RCURLY = 15;
    private static final int[] whitespaceSet = new int[] { COMMENT, WHITESPACE };
    private static final int[] alternateSet = new int[] { IDENTIFIER, TOKEN };
    private static final int[] closureSet = new int[] { OPTION, POSITIVE, KLEENE };
    private ILexer _lexer;
    private Lexeme _currentLexeme;
    private Hashtable _rules;
    private String _startRule;
    public String[] getRuleNames() {
        Set nameSet = this._rules.keySet();
        String[] names = (String[]) nameSet.toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }
    public Rule getRule(String ruleName) {
        if (this._rules.containsKey(ruleName) == false) {
            throw new IllegalArgumentException("Grammar does not contain the specified rule: " + ruleName);
        }
        return (Rule) this._rules.get(ruleName);
    }
    public Rule getStartRule() {
        return this.getRule(this._startRule);
    }
    static {
        Arrays.sort(whitespaceSet);
        Arrays.sort(alternateSet);
        Arrays.sort(closureSet);
    }
    public Grammar() {
        this._rules = new Hashtable();
    }
    public void load(String filename) throws LexerInitializationException, LexerException, IOException, ParseException {
        RegexLexerBuilderBase lp = new AsciiLexerBuilder();
        lp.loadXML(new FileInputStream("Parser Files/BNFTokens.xml"));
        ILexer lexer = lp.buildLexer();
        File file = new File(filename);
        FileReader fr = new FileReader(filename);
        char[] chars = new char[(int) file.length()];
        fr.read(chars);
        fr.close();
        this._rules.clear();
        lexer.setSource(chars);
        this._lexer = lexer;
        this.advance();
        while (this._currentLexeme != null) {
            this.parse();
        }
    }
    private Lexeme advance() {
        this._currentLexeme = this._lexer.getNextLexeme();
        while (inSet(whitespaceSet)) {
            this._currentLexeme = this._lexer.getNextLexeme();
        }
        return this._currentLexeme;
    }
    private Lexeme assertAndAdvance(int type) throws ParseException {
        assertType(type);
        return this.advance();
    }
    private void assertType(int type) throws ParseException {
        int nameIndex = this._currentLexeme.typeIndex;
        if (nameIndex != type) {
            throw new ParseException("expected " + type + " but found " + nameIndex, -1);
        }
    }
    private boolean inSet(int[] set) {
        boolean result = false;
        if (this._currentLexeme != null) {
            result = Arrays.binarySearch(set, this._currentLexeme.typeIndex) >= 0;
        }
        return result;
    }
    private void parse() throws ParseException {
        while (this._currentLexeme != null && this._currentLexeme.typeIndex == IDENTIFIER) {
            Rule rule;
            String name = this._currentLexeme.getText();
            this.advance();
            if (this._rules.containsKey(name)) {
                rule = (Rule) this._rules.get(name);
            } else {
                if (this._rules.size() == 0) {
                    this._startRule = name;
                }
                rule = new Rule(this, name);
                this._rules.put(name, rule);
            }
            this.assertAndAdvance(COLON);
            if (this._currentLexeme.typeIndex == LBRACKET) {
                this.parseBracketedExpression();
            }
            if (inSet(alternateSet)) {
                rule.addAlternate(this.parseAlternate());
            } else {
                rule.setEpsilon();
            }
            while (this._currentLexeme.typeIndex == PIPE) {
                this.advance();
                if (inSet(alternateSet)) {
                    rule.addAlternate(this.parseAlternate());
                } else {
                    rule.setEpsilon();
                }
            }
            this.assertAndAdvance(SEMICOLON);
        }
    }
    private Alternate parseAlternate() {
        Alternate result = new Alternate();
        while (inSet(alternateSet)) {
            Element element = null;
            switch(this._currentLexeme.typeIndex) {
                case IDENTIFIER:
                    element = new Element(this._currentLexeme.getText(), Element.RULE);
                    result.addElement(element);
                    this.advance();
                    break;
                case TOKEN:
                    element = new Element(this._currentLexeme.getText(), Element.TOKEN);
                    result.addElement(element);
                    this.advance();
                    break;
                default:
                    break;
            }
            if (inSet(closureSet)) {
                switch(this._currentLexeme.typeIndex) {
                    case OPTION:
                        this.advance();
                        element.setModifier(Element.ZERO_OR_ONE);
                        break;
                    case POSITIVE:
                        this.advance();
                        element.setModifier(Element.ONE_OR_MORE);
                        break;
                    case KLEENE:
                        this.advance();
                        element.setModifier(Element.ZERO_OR_MORE);
                        break;
                    default:
                        break;
                }
            }
            if (this._currentLexeme.typeIndex == LBRACKET) {
                this.parseBracketedExpression();
            }
        }
        return result;
    }
    private void parseBracketedExpression() {
        this.advance();
        while (this._currentLexeme.typeIndex != RBRACKET) {
            this.advance();
        }
        this.advance();
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String[] ruleNames = this.getRuleNames();
        if (ruleNames.length > 0) {
            Rule rule = this.getRule(ruleNames[0]);
            sb.append(rule.toString());
            for (int i = 1; i < ruleNames.length; i++) {
                rule = this.getRule(ruleNames[i]);
                sb.append("\n");
                sb.append(rule.toString());
            }
        }
        return sb.toString();
    }
}
