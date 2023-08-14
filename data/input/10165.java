public class DeclarationFilter {
    public static final DeclarationFilter FILTER_PUBLIC =
            new AccessFilter(PUBLIC);
    public static final DeclarationFilter FILTER_PROTECTED =
            new AccessFilter(PROTECTED);
    public static final DeclarationFilter FILTER_PUBLIC_OR_PROTECTED =
            new AccessFilter(PUBLIC, PROTECTED);
    public static final DeclarationFilter FILTER_PACKAGE =
            new AccessFilter();
    public static final DeclarationFilter FILTER_PRIVATE =
            new AccessFilter(PRIVATE);
    public DeclarationFilter() {
    }
    public static DeclarationFilter getFilter(
                                             final Collection<Modifier> mods) {
        return new DeclarationFilter() {
            public boolean matches(Declaration d) {
                return d.getModifiers().containsAll(mods);
            }
        };
    }
    public static DeclarationFilter getFilter(
                                     final Class<? extends Declaration> kind) {
        return new DeclarationFilter() {
            public boolean matches(Declaration d) {
                return kind.isInstance(d);
            }
        };
    }
    public DeclarationFilter and(DeclarationFilter f) {
        final DeclarationFilter f1 = this;
        final DeclarationFilter f2 = f;
        return new DeclarationFilter() {
            public boolean matches(Declaration d) {
                return f1.matches(d) && f2.matches(d);
            }
        };
    }
    public DeclarationFilter or(DeclarationFilter f) {
        final DeclarationFilter f1 = this;
        final DeclarationFilter f2 = f;
        return new DeclarationFilter() {
            public boolean matches(Declaration d) {
                return f1.matches(d) || f2.matches(d);
            }
        };
    }
    public DeclarationFilter not() {
        return new DeclarationFilter() {
            public boolean matches(Declaration d) {
                return !DeclarationFilter.this.matches(d);
            }
        };
    }
    public boolean matches(Declaration decl) {
        return true;
    }
    public <D extends Declaration> Collection<D> filter(Collection<D> decls) {
        ArrayList<D> res = new ArrayList<D>(decls.size());
        for (D d : decls) {
            if (matches(d)) {
                res.add(d);
            }
        }
        return res;
    }
    public <D extends Declaration> Collection<D>
            filter(Collection<? extends Declaration> decls, Class<D> resType) {
        ArrayList<D> res = new ArrayList<D>(decls.size());
        for (Declaration d : decls) {
            if (resType.isInstance(d) && matches(d)) {
                res.add(resType.cast(d));
            }
        }
        return res;
    }
    private static class AccessFilter extends DeclarationFilter {
        private Modifier mod1 = null;
        private Modifier mod2 = null;
        AccessFilter() {
        }
        AccessFilter(Modifier m) {
            mod1 = m;
        }
        AccessFilter(Modifier m1, Modifier m2) {
            mod1 = m1;
            mod2 = m2;
        }
        public boolean matches(Declaration d) {
            Collection<Modifier> mods = d.getModifiers();
            if (mod1 == null) { 
                return !(mods.contains(PUBLIC) ||
                         mods.contains(PROTECTED) ||
                         mods.contains(PRIVATE));
            }
            return mods.contains(mod1) &&
                   (mod2 == null || mods.contains(mod2));
        }
    }
}
