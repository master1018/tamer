public class AptEnv {
    public Names names;                 
    public Symtab symtab;               
    public Types jctypes;               
    public Enter enter;                 
    public Attr attr;                   
    public TypeMaker typeMaker;         
    public DeclarationMaker declMaker;  
    private static final Context.Key<AptEnv> aptEnvKey =
            new Context.Key<AptEnv>();
    public static AptEnv instance(Context context) {
        AptEnv instance = context.get(aptEnvKey);
        if (instance == null) {
            instance = new AptEnv(context);
        }
        return instance;
    }
    private AptEnv(Context context) {
        context.put(aptEnvKey, this);
        names = Names.instance(context);
        symtab = Symtab.instance(context);
        jctypes = Types.instance(context);
        enter = Enter.instance(context);
        attr = Attr.instance(context);
        typeMaker = TypeMaker.instance(context);
        declMaker = DeclarationMaker.instance(context);
    }
    public static boolean hasFlag(Symbol sym, long flag) {
        return (getFlags(sym) & flag) != 0;
    }
    public static long getFlags(Symbol sym) {
        complete(sym);
        return sym.flags();
    }
    private static void complete(Symbol sym) {
        while (true) {
            try {
                sym.complete();
                return;
            } catch (CompletionFailure e) {
            }
        }
    }
}
