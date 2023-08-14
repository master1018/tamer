public class CheckNamesProcessor extends AbstractProcessor {
    private NameChecker nameChecker;
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            for (Element element : roundEnv.getRootElements() )
                nameChecker.checkNames(element);
        }
        return false; 
    }
    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        nameChecker = new NameChecker(processingEnv);
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
    private static class NameChecker {
        private final Messager messager;
        private final Types typeUtils;
        NameCheckScanner nameCheckScanner = new NameCheckScanner();
        NameChecker(ProcessingEnvironment processsingEnv) {
            this.messager  = processsingEnv.getMessager();
            this.typeUtils = processsingEnv.getTypeUtils();
        }
        public void checkNames(Element element) {
            nameCheckScanner.scan(element);
        }
        private class NameCheckScanner extends ElementScanner7<Void, Void> {
            @Override
            public Void visitType(TypeElement e, Void p) {
                scan(e.getTypeParameters(), p); 
                checkCamelCase(e, true);        
                super.visitType(e, p);          
                return null;
            }
            @Override
            public Void visitExecutable(ExecutableElement e, Void p) {
                scan(e.getTypeParameters(), p); 
                if (e.getKind() == METHOD) {
                    Name name = e.getSimpleName();
                    if (name.contentEquals(e.getEnclosingElement().getSimpleName()))
                        messager.printMessage(WARNING,
                                              "A method should not have the same name as its enclosing type, ``" +
                                              name + "''." , e);
                    checkCamelCase(e, false);
                }
                super.visitExecutable(e, p);
                return null;
            }
            @Override
            public Void visitVariable(VariableElement e, Void p) {
                if (!checkForSerial(e)) { 
                    if (e.getKind() == ENUM_CONSTANT ||
                        e.getConstantValue() != null ||
                        heuristicallyConstant(e) )
                        checkAllCaps(e); 
                    else
                        checkCamelCase(e, false);
                }
                return null;
            }
            @Override
            public Void visitTypeParameter(TypeParameterElement e, Void p) {
                checkAllCaps(e);
                return null;
            }
            @Override
            public Void visitPackage(PackageElement e, Void p) {
                return null;
            }
            @Override
            public Void visitUnknown(Element e, Void p) {
                messager.printMessage(WARNING,
                                      "Unknown kind of element, " + e.getKind() +
                                      ", no name checking performed.", e);
                return null;
            }
            private boolean checkForSerial(VariableElement e) {
                if (e.getKind() == FIELD &&
                    e.getSimpleName().contentEquals("serialVersionUID")) {
                    if (!(e.getModifiers().containsAll(EnumSet.of(STATIC, FINAL)) &&
                            typeUtils.isSameType(e.asType(), typeUtils.getPrimitiveType(LONG)) &&
                            e.getEnclosingElement().getKind() == CLASS )) 
                        messager.printMessage(WARNING,
                                              "Field named ``serialVersionUID'' is not acting as such.", e);
                    return true;
                }
                return false;
            }
            private boolean heuristicallyConstant(VariableElement e) {
                if (e.getEnclosingElement().getKind() == INTERFACE)
                    return true;
                else if (e.getKind() == FIELD &&
                         e.getModifiers().containsAll(EnumSet.of(PUBLIC, STATIC, FINAL)))
                    return true;
                else {
                    return false;
                }
            }
            private void checkCamelCase(Element e, boolean initialCaps) {
                String name = e.getSimpleName().toString();
                boolean previousUpper = false;
                boolean conventional = true;
                int firstCodePoint = name.codePointAt(0);
                if (Character.isUpperCase(firstCodePoint)) {
                    previousUpper = true;
                    if (!initialCaps) {
                        messager.printMessage(WARNING,
                                              "Name, ``" + name + "'', should start in lowercase.", e);
                        return;
                    }
                } else if (Character.isLowerCase(firstCodePoint)) {
                    if (initialCaps) {
                        messager.printMessage(WARNING,
                                              "Name, ``" + name + "'', should start in uppercase.", e);
                        return;
                    }
                } else 
                    conventional = false;
                if (conventional) {
                    int cp = firstCodePoint;
                    for (int i = Character.charCount(cp);
                         i < name.length();
                         i += Character.charCount(cp)) {
                        cp = name.codePointAt(i);
                        if (Character.isUpperCase(cp)){
                            if (previousUpper) {
                                conventional = false;
                                break;
                            }
                            previousUpper = true;
                        } else
                            previousUpper = false;
                    }
                }
                if (!conventional)
                    messager.printMessage(WARNING,
                                          "Name, ``" + name + "'', should be in camel case.", e);
            }
            private void checkAllCaps(Element e) {
                String name = e.getSimpleName().toString();
                if (e.getKind() == TYPE_PARAMETER) { 
                    if (name.codePointCount(0, name.length()) > 1 ||
                        !Character.isUpperCase(name.codePointAt(0)))
                        messager.printMessage(WARNING,
                                              "A type variable's name,``" + name +
                                              "'', should be a single uppercace character.",
                                              e);
                } else {
                    boolean conventional = true;
                    int firstCodePoint = name.codePointAt(0);
                    if (!Character.isUpperCase(firstCodePoint))
                        conventional = false;
                    else {
                        boolean previousUnderscore = false;
                        int cp = firstCodePoint;
                        for (int i = Character.charCount(cp);
                             i < name.length();
                             i += Character.charCount(cp)) {
                            cp = name.codePointAt(i);
                            if (cp == (int) '_') {
                                if (previousUnderscore) {
                                    conventional = false;
                                    break;
                                }
                                previousUnderscore = true;
                            } else {
                                previousUnderscore = false;
                                if (!Character.isUpperCase(cp) && !Character.isDigit(cp) ) {
                                    conventional = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (!conventional)
                        messager.printMessage(WARNING,
                                              "A constant's name, ``" + name + "'', should be ALL_CAPS.",
                                              e);
                }
            }
        }
    }
}
class BADLY_NAMED_CODE {
    enum colors {
        red,
        blue,
        green;
    }
    static final int _FORTY_TWO = 42;
    public static int NOT_A_CONSTANT = _FORTY_TWO;
    private static final int serialVersionUID = _FORTY_TWO;
    protected void BADLY_NAMED_CODE() {
        return;
    }
    public void NOTcamelCASEmethodNAME() {
        return;
    }
}
