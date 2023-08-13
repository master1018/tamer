public class DexTestConverter extends AbstractTestSourceConverter {
    public IApi convert(Visibility visibility, Set<CompilationUnit> units) throws IOException {
        JavaSourceToDexUtil toDexUtil = new JavaSourceToDexUtil();
        DexToSigConverter converter = new DexToSigConverter();
        Set<JavaSource> sources = new HashSet<JavaSource>();
        for (CompilationUnit unit : units) {
            sources.add(new JavaSource(unit.getName(), unit.getSource()));
        }
        DexFile dexFile = toDexUtil.getAllFrom(sources);
        return converter.convertApi("Dex Tests", Collections.singleton(dexFile), visibility);
    }
}
