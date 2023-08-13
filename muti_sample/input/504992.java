@TypeQualifier(applicableTo=String.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface FixedLengthString {
	int value();
	class Checker implements TypeQualifierValidator<FixedLengthString> {
		public When forConstantValue(FixedLengthString annotation, Object v) {
			if (!(v instanceof String))
				return When.NEVER;
			String s = (String) v;
			if (s.length() == annotation.value())
				return When.ALWAYS;
			return When.NEVER;
		}
	}
}