    value = Pipe.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "Pipe",
            args = {}
        )
    }
)
public class PipeTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "open",
        args = {}
    )
    public void test_open() throws IOException{
        Pipe pipe = Pipe.open();
        assertNotNull(pipe);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sink",
        args = {}
    )
    public void test_sink() throws IOException {
        Pipe pipe = Pipe.open();
        SinkChannel sink = pipe.sink();
        assertTrue(sink.isBlocking());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "source",
        args = {}
    )
    public void test_source() throws IOException {
        Pipe pipe = Pipe.open();
        SourceChannel source = pipe.source();
        assertTrue(source.isBlocking());
    }
}
