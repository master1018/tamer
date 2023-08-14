public abstract class MemberDocImpl
    extends ProgramElementDocImpl
    implements MemberDoc {
    public MemberDocImpl(DocEnv env, Symbol sym, String doc, JCTree tree, Position.LineMap lineMap) {
        super(env, sym, doc, tree, lineMap);
    }
    public abstract boolean isSynthetic();
}
