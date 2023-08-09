public abstract class AbstractTestSourceConverter implements ITestSourceConverter {
    public IApi convert(Set<CompilationUnit> units) throws IOException {
        return convert(Visibility.PROTECTED, units);
    }
    public IApi convert(CompilationUnit... units) throws IOException {
        return convert(Visibility.PROTECTED, new HashSet<CompilationUnit>(Arrays.asList(units)));
    }
    public IApi convert(Visibility visibility, CompilationUnit... units) throws IOException {
        return convert(visibility, new HashSet<CompilationUnit>(Arrays.asList(units)));
    }
}
