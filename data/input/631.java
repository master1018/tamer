public class TestGetPackageOf extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            TypeElement    stringElt   = eltUtils.getTypeElement("java.lang.String");
            PackageElement javaLangPkg = eltUtils.getPackageElement("java.lang");
            PackageElement unnamedPkg  = eltUtils.getPackageElement("");
            PackageElement pkg = null;
            if (!javaLangPkg.equals(pkg=eltUtils.getPackageOf(stringElt) ) )
                throw new RuntimeException("Unexpected package for String: " + pkg);
            if (!javaLangPkg.equals(pkg=eltUtils.getPackageOf(javaLangPkg) ) )
                throw new RuntimeException("Unexpected package for java.lang: " + pkg);
            if (!unnamedPkg.equals(pkg=eltUtils.getPackageOf(unnamedPkg) ) )
                throw new RuntimeException("Unexpected package for unnamed pkg: " + pkg);
        }
        return true;
    }
}
