@TestTargetClass(ViewDebug.class)
public class ViewDebugTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ViewDebug",
        args = {}
    )
    public void testConstructor() {
        new ViewDebug();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "startRecyclerTracing",
            args = {String.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "trace",
            args = {View.class, RecyclerTraceType.class, int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "stopRecyclerTracing",
            args = {}
        )
    })
    @ToBeFixed(bug = "1852451", explanation = "compiling error when set TRACE_RECYCLER to true")
    public void testRecyclerTracing() {
        final String recyclerTracePrefix = "ViewDebugTest";
        View ownerView = new View(getContext());
        View view = new View(getContext());
        assertFalse(ViewDebug.TRACE_RECYCLER);
        ViewDebug.startRecyclerTracing(recyclerTracePrefix, ownerView);
        ViewDebug.trace(view, RecyclerTraceType.NEW_VIEW, 0, 1);
        ViewDebug.stopRecyclerTracing();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "startHierarchyTracing",
            args = {String.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "trace",
            args = {View.class, HierarchyTraceType.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "stopHierarchyTracing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "dumpCapturedView",
            args = {String.class, Object.class}
        )
    })
    @ToBeFixed(bug = "1852451", explanation = "throw IllegalStateException when set"
              + "TRACE_HIERARCHY to true")
    public void testHierarchyTracing() {
        final String hierarchyTracePrefix = "ViewDebugTest";
        View v1 = new View(getContext());
        View v2 = new View(getContext());
        assertFalse(ViewDebug.TRACE_HIERARCHY);
        ViewDebug.startHierarchyTracing(hierarchyTracePrefix, v1);
        ViewDebug.trace(v2, HierarchyTraceType.INVALIDATE);
        ViewDebug.stopHierarchyTracing();
        ViewDebug.dumpCapturedView("TAG", new TextView(getContext()));
    }
}
