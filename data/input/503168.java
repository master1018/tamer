public class EmailTest extends TestCase {
    public void testGetColorIndexFromAccountId() {
        assertEquals(0, Email.getColorIndexFromAccountId(1));
        assertEquals(1, Email.getColorIndexFromAccountId(2));
        assertTrue(Email.getColorIndexFromAccountId(-5) >= 0);
        for (int i = -100; i < 100; i++) {
            Email.getAccountColorResourceId(i);
            Email.getAccountColor(i);
        }
    }
}
