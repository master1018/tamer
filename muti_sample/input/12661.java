public class TestMissingElement extends JavacTestingAbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement te: ElementFilter.typesIn(roundEnv.getRootElements())) {
            if (isSimpleName(te, "InvalidSource")) {
                for (Element c: te.getEnclosedElements()) {
                    for (AnnotationMirror am: c.getAnnotationMirrors()) {
                        Element ate = am.getAnnotationType().asElement();
                        if (isSimpleName(ate, "ExpectInterfaces")) {
                            checkInterfaces((TypeElement) c, getValue(am));
                        } else if (isSimpleName(ate, "ExpectSupertype")) {
                            checkSupertype((TypeElement) c, getValue(am));
                        }
                    }
                }
            }
        }
        return true;
    }
    private boolean isSimpleName(Element e, String name) {
        return e.getSimpleName().contentEquals(name);
    }
    private String getValue(AnnotationMirror am) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> map = am.getElementValues();
        if (map.size() != 1) throw new IllegalArgumentException();
        AnnotationValue v = map.values().iterator().next();
        return (String) v.getValue();
    }
    private void checkInterfaces(TypeElement te, String expect) {
        System.err.println("check interfaces: " + te + " -- " + expect);
        String found = asString(te.getInterfaces(), ", ");
        checkEqual("interfaces", te, found, expect);
    }
    private void checkSupertype(TypeElement te, String expect) {
        System.err.println("check supertype: " + te + " -- " + expect);
        String found = asString(te.getSuperclass());
        checkEqual("supertype", te, found, expect);
    }
    private void checkEqual(String label, TypeElement te, String found, String expect) {
        if (found.equals(expect)) {
        } else {
            System.err.println("unexpected " + label + ": " + te + "\n"
                    + " found: " + found + "\n"
                    + "expect: " + expect);
            messager.printMessage(ERROR, "unexpected " + label + " found: " + found + "; expected: " + expect, te);
        }
    }
    private String asString(List<? extends TypeMirror> ts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (TypeMirror t: ts) {
            if (sb.length() != 0) sb.append(sep);
            sb.append(asString(t));
        }
        return sb.toString();
    }
    private String asString(TypeMirror t) {
        if (t == null)
            return "[typ:null]";
        return t.accept(new SimpleTypeVisitor7<String, Void>() {
            @Override
            public String defaultAction(TypeMirror t, Void ignore) {
                return "[typ:" + t.toString() + "]";
            }
            @Override
            public String visitDeclared(DeclaredType t, Void ignore) {
                checkEqual(t.asElement(), types.asElement(t));
                String s = asString(t.asElement());
                List<? extends TypeMirror> args = t.getTypeArguments();
                if (!args.isEmpty())
                    s += "<" + asString(args, ",") + ">";
                return s;
            }
            @Override
            public String visitTypeVariable(TypeVariable t, Void ignore) {
                return "tvar " + t;
            }
            @Override
            public String visitError(ErrorType t, Void ignore) {
                return "!:" + visitDeclared(t, ignore);
            }
        }, null);
    }
    private String asString(Element e) {
        if (e == null)
            return "[elt:null]";
        return e.accept(new SimpleElementVisitor7<String, Void>() {
            @Override
            public String defaultAction(Element e, Void ignore) {
                return "[elt:" + e.getKind() + " " + e.toString() + "]";
            }
            @Override
            public String visitPackage(PackageElement e, Void ignore) {
                return "pkg " + e.getQualifiedName();
            }
            @Override
            public String visitType(TypeElement e, Void ignore) {
                StringBuilder sb = new StringBuilder();
                if (e.getEnclosedElements().isEmpty())
                    sb.append("empty ");
                ElementKind ek = e.getKind();
                switch (ek) {
                    case CLASS:
                        sb.append("clss");
                        break;
                    case INTERFACE:
                        sb.append("intf");
                        break;
                    default:
                        sb.append(ek);
                        break;
                }
                sb.append(" ");
                Element encl = e.getEnclosingElement();
                if (!isUnnamedPackage(encl) && encl.asType().getKind() != TypeKind.NONE) {
                    sb.append("(");
                    sb.append(asString(encl));
                    sb.append(")");
                    sb.append(".");
                }
                sb.append(e.getSimpleName());
                if (e.asType().getKind() == TypeKind.ERROR) sb.append("!");
                return sb.toString();
            }
        }, null);
    }
    boolean isUnnamedPackage(Element e) {
        return (e != null && e.getKind() == ElementKind.PACKAGE
                && ((PackageElement) e).isUnnamed());
    }
    void checkEqual(Element e1, Element e2) {
        if (e1 != e2) {
            throw new AssertionError("elements not equal as expected: "
                + e1 + ", " + e2);
        }
    }
}
