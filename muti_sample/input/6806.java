public class ParserFactory {
    protected static final Context.Key<ParserFactory> parserFactoryKey = new Context.Key<ParserFactory>();
    public static ParserFactory instance(Context context) {
        ParserFactory instance = context.get(parserFactoryKey);
        if (instance == null) {
            instance = new ParserFactory(context);
        }
        return instance;
    }
    final TreeMaker F;
    final Log log;
    final Keywords keywords;
    final Source source;
    final Names names;
    final Options options;
    final ScannerFactory scannerFactory;
    protected ParserFactory(Context context) {
        super();
        context.put(parserFactoryKey, this);
        this.F = TreeMaker.instance(context);
        this.log = Log.instance(context);
        this.names = Names.instance(context);
        this.keywords = Keywords.instance(context);
        this.source = Source.instance(context);
        this.options = Options.instance(context);
        this.scannerFactory = ScannerFactory.instance(context);
    }
    public Parser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap) {
        Lexer lexer = scannerFactory.newScanner(input, keepDocComments);
        if (keepEndPos) {
            return new EndPosParser(this, lexer, keepDocComments, keepLineMap);
        } else {
            return new JavacParser(this, lexer, keepDocComments, keepLineMap);
        }
    }
}
