public abstract class PackageCompareTest extends AbstractComparatorTest{
    @Test
    public void compareEqualPackageTest0() throws IOException{
         CompilationUnit from = new CompilationUnit("a.A", 
                    "package a; " +
                    "public class A {}");
          IApi fromApi = convert(from);
          IApi toApi = convert(from);
          IApiDelta delta = compare(fromApi, toApi);
          assertNull(delta);
    }
    @Test
    public void compareEqualPackageTest1() throws IOException{
         CompilationUnit from0 = new CompilationUnit("a.A", 
                    "package a; " +
                    "public class A {}");
         CompilationUnit from1 = new CompilationUnit("a.b.A", 
                    "package a.b; " +
                    "public class A {}");
          IApi fromApi = convert(from0, from1);
          IApi toApi = convert(from0, from1);
          assertNull(compare(fromApi, toApi));
    }
    @Test
    public void compareRemovedPackagePackageTest1() throws IOException{
         CompilationUnit packageA = new CompilationUnit("a.A", 
                    "package a; " +
                    "public class A {}");
         CompilationUnit packageB = new CompilationUnit("a.b.A", 
                    "package a.b; " +
                    "public class A {}");
          IApi fromApi = convert(packageA, packageB);
          IApi toApi = convert(packageA);
          IApiDelta apiDelta = compare(fromApi, toApi);
          assertNotNull(apiDelta);
          assertEquals(1, apiDelta.getPackageDeltas().size());
          IPackageDelta packageDelta = apiDelta.getPackageDeltas().iterator().next();
          assertEquals(DeltaType.REMOVED, packageDelta.getType());
    }
    @Test
    public void compareAddedPackagePackageTest1() throws IOException{
         CompilationUnit packageA = new CompilationUnit("a.A", 
                    "package a; " +
                    "public class A {}");
         CompilationUnit packageB = new CompilationUnit("a.b.A", 
                    "package a.b; " +
                    "public class A {}");
          IApi fromApi = convert(packageA);
          IApi toApi = convert(packageA, packageB);
          IApiDelta apiDelta = compare(fromApi, toApi);
          assertNotNull(apiDelta);
          assertEquals(1, apiDelta.getPackageDeltas().size());
          IPackageDelta packageDelta = apiDelta.getPackageDeltas().iterator().next();
          assertEquals(DeltaType.ADDED, packageDelta.getType());
    }
}
