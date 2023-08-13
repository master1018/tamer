@TestTargetClass(DName.class)
public class SslCertificate_DNameTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test DName.",
            method = "SslCertificate.DName",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test DName.",
            method = "getCName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test DName.",
            method = "getDName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test DName.",
            method = "getOName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test DName.",
            method = "getUName",
            args = {}
        )
    })
    public void testDName() {
        final String TO = "c=ccc,o=testOName,ou=testUName,cn=testCName";
        final String BY = "e=aeei,c=adb,o=testOName,ou=testUName,cn=testCName";
        Date date1 = new Date(System.currentTimeMillis() - 1000);
        Date date2 = new Date(System.currentTimeMillis());
        SslCertificate ssl = new SslCertificate(TO, BY, DateFormat.getInstance().format(
                date1), DateFormat.getInstance().format(date2));
        DName issuedTo = ssl.getIssuedTo();
        assertNotNull(issuedTo);
        assertEquals("testCName", issuedTo.getCName());
        assertEquals(TO, issuedTo.getDName());
        assertEquals("testOName", issuedTo.getOName());
        assertEquals("testUName", issuedTo.getUName());
    }
}
