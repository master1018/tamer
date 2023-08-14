public class ApiDemosApplicationTests extends ApplicationTestCase<ApiDemosApplication> {
    public ApiDemosApplicationTests() {
        super(ApiDemosApplication.class);
      }
      @Override
      protected void setUp() throws Exception {
          super.setUp();
      }
      @SmallTest
      public void testPreconditions() {
      }
      @MediumTest
      public void testSimpleCreate() {
          createApplication(); 
      }
}
