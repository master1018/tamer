public class ProvisionParserTests extends SyncAdapterTestCase {
    private final ByteArrayInputStream mTestInputStream =
        new ByteArrayInputStream("ABCDEFG".getBytes());
    private String mWapProvisioningDoc1 =
        "<wap-provisioningdoc>" +
            "<characteristic type=\"SecurityPolicy\"><parm name=\"4131\" value=\"0\"/>" +
            "</characteristic>" +
            "<characteristic type=\"Registry\">" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\AE\\" +
                        "{50C13377-C66D-400C-889E-C316FC4AB374}\">" +
                    "<parm name=\"AEFrequencyType\" value=\"1\"/>" +
                    "<parm name=\"AEFrequencyValue\" value=\"5\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\">" +
                    "<parm name=\"DeviceWipeThreshold\" value=\"20\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\">" +
                    "<parm name=\"CodewordFrequency\" value=\"5\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\LAP\\lap_pw\">" +
                    "<parm name=\"MinimumPasswordLength\" value=\"8\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\LAP\\lap_pw\">" +
                    "<parm name=\"PasswordComplexity\" value=\"0\"/>" +
                "</characteristic>" +
            "</characteristic>" +
        "</wap-provisioningdoc>";
    private String mWapProvisioningDoc2 =
        "<wap-provisioningdoc>" +
            "<characteristic type=\"SecurityPolicy\"><parm name=\"4131\" value=\"1\"/>" +
            "</characteristic>" +
            "<characteristic type=\"Registry\">" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\AE\\" +
                        "{50C13377-C66D-400C-889E-C316FC4AB374}\">" +
                    "<parm name=\"AEFrequencyType\" value=\"0\"/>" +
                    "<parm name=\"AEFrequencyValue\" value=\"5\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\">" +
                    "<parm name=\"DeviceWipeThreshold\" value=\"20\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\">" +
                    "<parm name=\"CodewordFrequency\" value=\"5\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\LAP\\lap_pw\">" +
                    "<parm name=\"MinimumPasswordLength\" value=\"8\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\LAP\\lap_pw\">" +
                    "<parm name=\"PasswordComplexity\" value=\"0\"/>" +
                "</characteristic>" +
            "</characteristic>" +
        "</wap-provisioningdoc>";
    private String mWapProvisioningDoc3 =
        "<wap-provisioningdoc>" +
            "<characteristic type=\"SecurityPolicy\"><parm name=\"4131\" value=\"0\"/>" +
            "</characteristic>" +
            "<characteristic type=\"Registry\">" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\AE\\" +
                        "{50C13377-C66D-400C-889E-C316FC4AB374}\">" +
                    "<parm name=\"AEFrequencyType\" value=\"1\"/>" +
                    "<parm name=\"AEFrequencyValue\" value=\"2\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\">" +
                    "<parm name=\"DeviceWipeThreshold\" value=\"5\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\">" +
                    "<parm name=\"CodewordFrequency\" value=\"5\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\LAP\\lap_pw\">" +
                    "<parm name=\"MinimumPasswordLength\" value=\"4\"/>" +
                "</characteristic>" +
                "<characteristic type=\"HKLM\\Comm\\Security\\Policy\\LASSD\\LAP\\lap_pw\">" +
                    "<parm name=\"PasswordComplexity\" value=\"1\"/>" +
                "</characteristic>" +
            "</characteristic>" +
        "</wap-provisioningdoc>";
    public void testWapProvisionParser1() throws IOException {
         ProvisionParser parser = new ProvisionParser(mTestInputStream, getTestService());
        parser.parseProvisionDocXml(mWapProvisioningDoc1);
        PolicySet ps = parser.getPolicySet();
        assertNotNull(ps);
        assertEquals(5*60, ps.getMaxScreenLockTime());  
        assertEquals(8, ps.getMinPasswordLength());
        assertEquals(PolicySet.PASSWORD_MODE_STRONG, ps.getPasswordMode());
        assertEquals(20, ps.getMaxPasswordFails());
        assertTrue(ps.isRequireRemoteWipe());
    }
    public void testWapProvisionParser2() throws IOException {
        ProvisionParser parser = new ProvisionParser(mTestInputStream, getTestService());
        parser.parseProvisionDocXml(mWapProvisioningDoc2);
        PolicySet ps = parser.getPolicySet();
        assertNotNull(ps);
        assertEquals(PolicySet.PASSWORD_MODE_NONE, ps.getPasswordMode());
    }
    public void testWapProvisionParser3() throws IOException {
        ProvisionParser parser = new ProvisionParser(mTestInputStream, getTestService());
        parser.parseProvisionDocXml(mWapProvisioningDoc3);
        PolicySet ps = parser.getPolicySet();
        assertNotNull(ps);
        assertEquals(2*60, ps.getMaxScreenLockTime());  
        assertEquals(4, ps.getMinPasswordLength());
        assertEquals(PolicySet.PASSWORD_MODE_SIMPLE, ps.getPasswordMode());
        assertEquals(5, ps.getMaxPasswordFails());
        assertTrue(ps.isRequireRemoteWipe());
    }
}
