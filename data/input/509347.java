@TestTargetClass(UrlQuerySanitizer.ParameterValuePair.class)
public class UrlQuerySanitizer_ParameterValuePairTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link ParameterValuePair}",
        method = "ParameterValuePair",
        args = {String.class, String.class}
    )
    public void testConstructor() {
        final String parameter = "name";
        final String vaule = "Joe_user";
        UrlQuerySanitizer uqs = new UrlQuerySanitizer();
        ParameterValuePair parameterValuePair = uqs.new ParameterValuePair(parameter, vaule);
        assertEquals(parameter, parameterValuePair.mParameter);
        assertEquals(vaule, parameterValuePair.mValue);
    }
}
