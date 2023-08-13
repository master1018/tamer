public class ScannerFactory {
    public static final Context.Key<ScannerFactory> scannerFactoryKey =
        new Context.Key<ScannerFactory>();
    public static ScannerFactory instance(Context context) {
        ScannerFactory instance = context.get(scannerFactoryKey);
        if (instance == null)
            instance = new ScannerFactory(context);
        return instance;
    }
    final Log log;
    final Names names;
    final Source source;
    final Keywords keywords;
    protected ScannerFactory(Context context) {
        context.put(scannerFactoryKey, this);
        this.log = Log.instance(context);
        this.names = Names.instance(context);
        this.source = Source.instance(context);
        this.keywords = Keywords.instance(context);
    }
    public Scanner newScanner(CharSequence input, boolean keepDocComments) {
        if (input instanceof CharBuffer) {
            CharBuffer buf = (CharBuffer) input;
            if (keepDocComments)
                return new DocCommentScanner(this, buf);
            else
                return new Scanner(this, buf);
        } else {
            char[] array = input.toString().toCharArray();
            return newScanner(array, array.length, keepDocComments);
        }
    }
    public Scanner newScanner(char[] input, int inputLength, boolean keepDocComments) {
        if (keepDocComments)
            return new DocCommentScanner(this, input, inputLength);
        else
            return new Scanner(this, input, inputLength);
    }
}
