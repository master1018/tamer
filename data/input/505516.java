@TypeQualifier(applicableTo=CharSequence.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashedClassName {
	When when() default When.ALWAYS;
}
