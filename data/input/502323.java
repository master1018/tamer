        value = FileFilter.class,
        untestedMethods = {
            @TestTargetNew(
                    method = "accept",
                    args = {File.class},
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "There are no classes in the current core " +
                            "libraries that implement this method."
            )
        }
)
public class FileFilterTest extends TestCase {
}
