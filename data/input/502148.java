public abstract class AbstractComparatorTest extends AbstractConvertTest{
    private IApiComparator comparator;
    @Before
    public void setupComparator() {
        comparator = new ApiComparator();
    }
    public IApiDelta compare(IApi from, IApi to){
        return comparator.compare(from, to);
    }
    public IPackageDelta getSinglePackageDelta(IApiDelta apiDelta){
        assertNotNull(apiDelta);
        assertEquals(1, apiDelta.getPackageDeltas().size());
        return apiDelta.getPackageDeltas().iterator().next();
    }
    public IClassDefinitionDelta getSingleClassDelta(IApiDelta apiDelta){
        IPackageDelta packageDelta = getSinglePackageDelta(apiDelta);
        assertEquals(1, packageDelta.getClassDeltas().size());
        return packageDelta.getClassDeltas().iterator().next();
    }
}
