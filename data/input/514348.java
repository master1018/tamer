public class TestDefaults {
	public void foo(String c) {}
	public void foo2(@DottedClassName String c) {
		foo(c);
	}
	public void foo3(@SlashedClassName(when=When.UNKNOWN) String c) {
		foo(c);
	}
}
