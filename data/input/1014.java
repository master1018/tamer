public abstract class SpringDAOTestCase extends AbstractTransactionalDataSourceSpringContextTests {
    protected String[] getConfigLocations() {
        return new String[] { "spring-context.xml", "spring-servlet.xml" };
    }
}
