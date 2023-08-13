        value = FilenameFilter.class,
        untestedMethods = {
            @TestTargetNew(
                    method = "accept",
                    args = {File.class , String.class},
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "There are no classes in the current core " +
                            "libraries that implement this method."
            )
        }
)
public class FilenameFilterTest extends TestCase {
}
