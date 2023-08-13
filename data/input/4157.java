public class JavacScope implements com.sun.source.tree.Scope {
    protected final Env<AttrContext> env;
    JavacScope(Env<AttrContext> env) {
        env.getClass(); 
        this.env = env;
    }
    public JavacScope getEnclosingScope() {
        if (env.outer != null && env.outer != env)
            return  new JavacScope(env.outer);
        else {
            return new JavacScope(env) {
                public boolean isStarImportScope() {
                    return true;
                }
                public JavacScope getEnclosingScope() {
                    return null;
                }
                public Iterable<? extends Element> getLocalElements() {
                    return env.toplevel.starImportScope.getElements();
                }
            };
        }
    }
    public TypeElement getEnclosingClass() {
        return (env.outer == null || env.outer == env ? null : env.enclClass.sym);
    }
    public ExecutableElement getEnclosingMethod() {
        return (env.enclMethod == null ? null : env.enclMethod.sym);
    }
    public Iterable<? extends Element> getLocalElements() {
        return env.info.getLocalElements();
    }
    public Env<AttrContext> getEnv() {
        return env;
    }
    public boolean isStarImportScope() {
        return false;
    }
    public boolean equals(Object other) {
        if (other instanceof JavacScope) {
            JavacScope s = (JavacScope) other;
            return (env.equals(s.env)
                && isStarImportScope() == s.isStarImportScope());
        } else
            return false;
    }
    public int hashCode() {
        return env.hashCode() + (isStarImportScope() ? 1 : 0);
    }
    public String toString() {
        return "JavacScope[env=" + env + ",starImport=" + isStarImportScope() + "]";
    }
}
