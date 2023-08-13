public class Keywords {
    public static final Context.Key<Keywords> keywordsKey =
        new Context.Key<Keywords>();
    public static Keywords instance(Context context) {
        Keywords instance = context.get(keywordsKey);
        if (instance == null)
            instance = new Keywords(context);
        return instance;
    }
    private final Names names;
    protected Keywords(Context context) {
        context.put(keywordsKey, this);
        names = Names.instance(context);
        for (Token t : Token.values()) {
            if (t.name != null)
                enterKeyword(t.name, t);
            else
                tokenName[t.ordinal()] = null;
        }
        key = new Token[maxKey+1];
        for (int i = 0; i <= maxKey; i++) key[i] = IDENTIFIER;
        for (Token t : Token.values()) {
            if (t.name != null)
                key[tokenName[t.ordinal()].getIndex()] = t;
        }
    }
    public Token key(Name name) {
        return (name.getIndex() > maxKey) ? IDENTIFIER : key[name.getIndex()];
    }
    private final Token[] key;
    private int maxKey = 0;
    private Name[] tokenName = new Name[Token.values().length];
    private void enterKeyword(String s, Token token) {
        Name n = names.fromString(s);
        tokenName[token.ordinal()] = n;
        if (n.getIndex() > maxKey) maxKey = n.getIndex();
    }
}
