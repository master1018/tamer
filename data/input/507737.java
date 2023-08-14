public abstract class AbstractConvertTest extends AbstractTestSourceConverter {
    private ITestSourceConverter converter;
    public abstract ITestSourceConverter createConverter();
    @Before
    public void setupConverter(){
        converter = createConverter();
    }
    public IApi convert(Visibility visibility, Set<CompilationUnit> units) throws IOException {
        return converter.convert(visibility, units);
    }
}
