public class Support_TestResource extends java.util.ListResourceBundle {
    final String array[] = {"Str1", "Str2", "Str3"};
    @Override
    protected Object[][] getContents() {
        Object[][] contents = { { "parent1", "parentValue1" },
                { "parent2", "parentValue2" }, { "parent3", "parentValue3" },
                { "parent4", "parentValue4" }, {"IntegerVal", 1}, {"StringArray", array}};
        return contents;
    }
}
