public class T6341534 extends JavacTestingAbstractProcessor {
    public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv)  {
        messager.printMessage(NOTE,
                              String.valueOf(eltUtils.getPackageElement("no.such.package")));
        PackageElement dir = eltUtils.getPackageElement("dir");
        messager.printMessage(NOTE, dir.getQualifiedName().toString());
        for (Element e : dir.getEnclosedElements())
            messager.printMessage(NOTE, e.toString());
        return true;
    }
}
