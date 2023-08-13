public class SimTonesTest extends ActivityInstrumentationTestCase<MediaFrameworkTest> {    
    private String TAG = "SimTonesTest";
    Context mContext;
    public SimTonesTest() {
        super("com.android.mediaframeworktest", MediaFrameworkTest.class);
      }
       protected void setUp() throws Exception {
         super.setUp();
     }
   @LargeTest    
   public void testDtmfTones() throws Exception {
       boolean result = TonesAutoTest.tonesDtmfTest();
     assertTrue("DTMF Tones", result);  
   }
   @LargeTest
   public void testSupervisoryTones() throws Exception {
       boolean result = TonesAutoTest.tonesSupervisoryTest();
     assertTrue("Supervisory Tones", result);  
   }
   @LargeTest
   public void testProprietaryTones() throws Exception {
       boolean result = TonesAutoTest.tonesProprietaryTest();
     assertTrue("Proprietary Tones", result);  
   }
   @LargeTest
   public void testSimultaneousTones() throws Exception {
       boolean result = TonesAutoTest.tonesSimultaneousTest();
     assertTrue("Simultaneous Tones", result);  
   }
   @LargeTest
   public void testStressTones() throws Exception {
       boolean result = TonesAutoTest.tonesStressTest();
     assertTrue("Stress Tones", result);  
   }
}
