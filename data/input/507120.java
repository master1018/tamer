public class SimSmsTest extends TestCase {
    @MediumTest
    @Suppress 
    public void testBasic() throws Exception {
        ISms sms = ISms.Stub.asInterface(ServiceManager.getService("isms"));
        assertNotNull(sms);
        List<SmsRawData> records = sms.getAllMessagesFromIccEf();
        assertNotNull(records);
        assertTrue(records.size() >= 0);
        int firstNullIndex = -1;
        int firstValidIndex = -1;
        byte[] pdu = null;
        for (int i = 0; i < records.size(); i++) {
            SmsRawData data = records.get(i);
            if (data != null && firstValidIndex == -1) {
                firstValidIndex = i;
                pdu = data.getBytes();
            }
            if (data == null && firstNullIndex == -1) {
                firstNullIndex = i;
            }
            if (firstNullIndex != -1 && firstValidIndex != -1) {
                break;
            }
        }
        if (firstNullIndex == -1 || firstValidIndex == -1)
            return;
        assertNotNull(pdu);
    }
}
