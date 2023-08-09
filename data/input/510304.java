@TestTargetClass(Character.Subset.class) 
public class Character_SubsetTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Subset",
        args = {java.lang.String.class}
    )
    public void test_Ctor() {
        try {
            new Character.Subset(null) {
            };
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        String name = "name";
        Character.Subset subset = new Character.Subset(name) {
        };
        assertSame(name, subset.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() {
      Character.Subset subset1 = new Character.Subset("name") { };
      assertTrue(subset1.equals(subset1));
      assertFalse(subset1.equals(new Character.Subset("name") {}));      
      assertFalse(subset1.equals(new Character.Subset("name1") {}));
      assertFalse(subset1.equals(new Integer(0)));     
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
      Character.Subset subset1 = new Character.Subset("name") {};
      Character.Subset subset2 = new Character.Subset("name") {};
      Character.Subset subset3 = new Character.Subset("name1") {};
      assertFalse(subset1.hashCode() == subset2.hashCode());      
      assertFalse(subset1.hashCode() == subset3.hashCode());
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(Character_SubsetTest.class);
    }
}
