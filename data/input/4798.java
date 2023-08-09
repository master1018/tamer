public class DeclarationsImpl implements Declarations {
    private final AptEnv env;
    private static final Context.Key<Declarations> declarationsKey =
            new Context.Key<Declarations>();
    public static Declarations instance(Context context) {
        Declarations instance = context.get(declarationsKey);
        if (instance == null) {
            instance = new DeclarationsImpl(context);
        }
        return instance;
    }
    private DeclarationsImpl(Context context) {
        context.put(declarationsKey, this);
        env = AptEnv.instance(context);
    }
    public boolean hides(MemberDeclaration sub, MemberDeclaration sup) {
        Symbol hider = ((DeclarationImpl) sub).sym;
        Symbol hidee = ((DeclarationImpl) sup).sym;
        if (hider == hidee ||
                hider.kind != hidee.kind ||
                hider.name != hidee.name) {
            return false;
        }
        if (hider.kind == MTH) {
            if ((hider.flags() & Flags.STATIC) == 0 ||
                        !env.jctypes.isSubSignature(hider.type, hidee.type)) {
                return false;
            }
        }
        ClassSymbol hiderClass = hider.owner.enclClass();
        ClassSymbol hideeClass = hidee.owner.enclClass();
        if (hiderClass == null || hideeClass == null ||
                !hiderClass.isSubClass(hideeClass, env.jctypes)) {
            return false;
        }
        return hidee.isInheritedIn(hiderClass, env.jctypes);
    }
    public boolean overrides(MethodDeclaration sub, MethodDeclaration sup) {
        MethodSymbol overrider = ((MethodDeclarationImpl) sub).sym;
        MethodSymbol overridee = ((MethodDeclarationImpl) sup).sym;
        ClassSymbol origin = (ClassSymbol) overrider.owner;
        return overrider.name == overridee.name &&
               overrider != overridee &&
               !overrider.isStatic() &&
               env.jctypes.asSuper(origin.type, overridee.owner) != null &&
               overrider.overrides(overridee, origin, env.jctypes, false);
    }
}
