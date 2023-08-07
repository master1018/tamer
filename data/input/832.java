public class ManifestationTest extends AbstractionTest {
    public static void main(String[] args) {
        TestRunner.run(ManifestationTest.class);
    }
    public ManifestationTest(String name) {
        super(name);
    }
    @Override
    protected Manifestation getFixture() {
        return (Manifestation) fixture;
    }
    @Override
    protected void setUp() throws Exception {
        setFixture(UMLFactory.eINSTANCE.createManifestation());
    }
    @Override
    protected void tearDown() throws Exception {
        setFixture(null);
    }
}
