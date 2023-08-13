@TestTargetClass(targets.Charsets.GSM0338.class)
@AndroidOnly("gsm specific")
public class Charset_GSM0338 extends Charset_AbstractTest {
    @Override
    protected void setUp() throws Exception {
        charsetName = "x-gsm-03.38-2000";
        testChars = theseChars(new int[]{
10, 13, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 
46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 
62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 
78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 95, 97, 98, 
99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 
115, 116, 117, 118, 119, 120, 121, 122, 161, 163, 164, 165, 167, 191, 196, 197, 
198, 201, 209, 214, 216, 220, 223, 224, 228, 229, 230, 231, 232, 233, 236, 241, 
242, 246, 248, 249, 252, 915, 916
            });
        testBytes = theseBytes(new int[]{
10, 13, 32, 33, 34, 35, 2, 37, 38, 39, 40, 41, 42, 43, 44, 45, 
46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 
62, 63, 0, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 
78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 17, 97, 98, 
99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 
115, 116, 117, 118, 119, 120, 121, 122, 64, 1, 36, 3, 95, 96, 91, 14, 
28, 31, 93, 92, 11, 94, 30, 127, 123, 15, 29, 9, 4, 5, 7, 125, 
8, 124, 12, 6, 126, 19, 16
            });
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Not applicable to this charset.",
        method = "functionalCoDec_REPR",
        args = {}
    )
    @Override
    public void test_CodecDynamic () throws CharacterCodingException {
    }
}
