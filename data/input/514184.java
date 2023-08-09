public class MockToolPackage extends ToolPackage {
    public MockToolPackage(int revision) {
        super(
            null, 
            null, 
            revision,
            null, 
            "desc", 
            "url", 
            Os.getCurrentOs(), 
            Arch.getCurrentArch(), 
            "foo" 
            );
    }
}
