public class NullConstructorParamsTest {
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main(String[] args) throws Exception {
        echo("SUMMARY: Test IAE is thrown when typeName or description parameter is null " +
             "for TabularType and CompositeType constructors");
        echo(">>> Create CompositeType with non null params: should be ok");
        CompositeType ctok =
            new CompositeType(
                              "typeNameOK",
                              "for test",
                              new String[] {"a", "b"},
                              new String[] {"a_desc", "b_desc"},
                              new OpenType[] {SimpleType.BOOLEAN,SimpleType.STRING});
        echo("+++ CompositeType created ok");
        echo("");
        echo(">>> Create TabularType with non null params: should be ok");
        TabularType tabok =
            new TabularType(
                            "typeNameOK",
                            "for test",
                            ctok,
                            new String[] {"a"});
        echo("+++ TabularType created ok");
        echo("");
        int IAENotThrown = 0;
        try {
            echo(">>> Create CompositeType with null typeName: expect IAE");
            CompositeType ctnull1 =
                new CompositeType(
                                  null,
                                  "for test",
                                  new String[] {"a", "b"},
                                  new String[] {"a_desc", "b_desc"},
                                  new OpenType[] {SimpleType.BOOLEAN, SimpleType.STRING});
            IAENotThrown++;
            echo("*** IAE not thrown as expected ***");
            echo("*** Test will FAIL ***");
            echo("");
        } catch (IllegalArgumentException iae) {
            echo("+++ IAE thrown as expected");
            echo("");
        } catch (Exception e) {
            IAENotThrown++;
            echo("*** Did not get IAE as expected, but instead: ");
            e.printStackTrace();
            echo("*** Test will FAIL ***");
            echo("");
        }
        try {
            echo(">>> Create TabularType with null typeName: expect IAE");
            TabularType tabnull1 =
                new TabularType(
                                null,
                                "for test",
                                ctok,
                                new String[] {"a"});
            IAENotThrown++;
            echo("*** IAE not thrown as expected ***");
            echo("*** Test will FAIL ***");
            echo("");
        } catch (IllegalArgumentException iae) {
            echo("+++ IAE thrown as expected");
            echo("");
        } catch (Exception e) {
            IAENotThrown++;
            echo("*** Did not get IAE as expected, but instead: ");
            e.printStackTrace();
            echo("*** Test will FAIL ***");
            echo("");
        }
        try {
            echo(">>> Create CompositeType with null description: expect IAE");
            CompositeType ctnull2 =
                new CompositeType(
                                  "test",
                                  null,
                                  new String[] {"a", "b"},
                                  new String[] {"a_desc", "b_desc"},
                                  new OpenType[] {SimpleType.BOOLEAN, SimpleType.STRING});
            IAENotThrown++;
            echo("*** IAE not thrown as expected ***");
            echo("*** Test will FAIL ***");
            echo("");
        } catch (IllegalArgumentException iae) {
            echo("+++ IAE thrown as expected");
            echo("");
        } catch (Exception e) {
            IAENotThrown++;
            echo("*** Did not get IAE as expected, but instead: ");
            e.printStackTrace();
            echo("*** Test will FAIL ***");
            echo("");
        }
        try {
            echo(">>> Create TabularType with null description: expect IAE");
            TabularType tabnull2 =
                new TabularType(
                                "test",
                                null,
                                ctok,
                                new String[] {"a"});
            IAENotThrown++;
            echo("*** IAE not thrown as expected ***");
            echo("*** Test will FAIL ***");
            echo("");
        } catch (IllegalArgumentException iae) {
            echo("+++ IAE thrown as expected");
            echo("");
        } catch (Exception e) {
            IAENotThrown++;
            echo("*** Did not get IAE as expected, but instead: ");
            e.printStackTrace();
            echo("*** Test will FAIL ***");
            echo("");
        }
        if (IAENotThrown != 0 ) {
            echo("*** Test FAILED: IAE not thrown as expected ***");
            echo("");
            throw new RuntimeException("IAE not thrown as expected");
        }
        echo("+++ Test PASSED");
        echo("");
    }
}
