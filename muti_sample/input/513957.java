	class Checker implements TypeQualifierValidator<CreditCardNumber> {
		public When forConstantValue(CreditCardNumber annotation, Object v) {
			if (!(v instanceof String))
				return When.NEVER;
			String s = (String) v;
			if (LuhnVerification.checkNumber(s))
				return When.ALWAYS;
			return When.NEVER;
		}
	}
}
