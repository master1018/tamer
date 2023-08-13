        value = ObjectInputValidation.class,
        untestedMethods = {
            @TestTargetNew(
                    method = "validateObject",
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "There are no classes in the current core " +
                            "libraries that implement this method."
            )
        }
)
public class ObjectInputValidationTest extends TestCase {
}
