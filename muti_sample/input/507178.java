public final class FloatingPointParser {
	private static final class StringExponentPair {
		String s;
		int e;
		boolean negative;
		StringExponentPair(String s, int e, boolean negative) {
			this.s = s;
			this.e = e;
			this.negative = negative;
		}
	}
	private static native double parseDblImpl(String s, int e);
	private static native float parseFltImpl(String s, int e);
	private static StringExponentPair initialParse(String s, int length) {
		boolean negative = false;
		char c;
		int start, end, decimal;
		int e = 0;
		start = 0;
		if (length == 0)
			throw new NumberFormatException(s);
		c = s.charAt(length - 1);
		if (c == 'D' || c == 'd' || c == 'F' || c == 'f') {
			length--;
			if (length == 0)
				throw new NumberFormatException(s);
		}
		end = Math.max(s.indexOf('E'), s.indexOf('e'));
		if (end > -1) {
			if (end + 1 == length)
				throw new NumberFormatException(s);
                        int exponent_offset = end + 1;
                        if (s.charAt(exponent_offset) == '+') {
                                if (s.charAt(exponent_offset + 1) == '-') {
                                        throw new NumberFormatException(s);
                                }
                                exponent_offset++; 
                        }
			try {
				e = Integer.parseInt(s.substring(exponent_offset,
                                                                 length));
                        } catch (NumberFormatException ex) {
				throw new NumberFormatException(s);
                        }                            
		} else {
			end = length;
		}
		if (length == 0)
			throw new NumberFormatException(s);
		c = s.charAt(start);
		if (c == '-') {
			++start;
			--length;
			negative = true;
		} else if (c == '+') {
			++start;
			--length;
		}
		if (length == 0)
			throw new NumberFormatException(s);
		decimal = s.indexOf('.');
		if (decimal > -1) {
			e -= end - decimal - 1;
			s = s.substring(start, decimal) + s.substring(decimal + 1, end);
		} else {
			s = s.substring(start, end);
		}
		if ((length = s.length()) == 0)
			throw new NumberFormatException();
		end = length;
		while (end > 1 && s.charAt(end - 1) == '0')
			--end;
		start = 0;
		while (start < end - 1 && s.charAt(start) == '0')
			start++;
		if (end != length || start != 0) {
			e += length - end;
			s = s.substring(start, end);
		}
        final int APPROX_MIN_MAGNITUDE = -359;
        final int MAX_DIGITS = 52;
        length = s.length();
        if (length > MAX_DIGITS && e < APPROX_MIN_MAGNITUDE) {
            int d = Math.min(APPROX_MIN_MAGNITUDE - e, length - 1);
            s = s.substring(0, length - d);
            e += d;
        }
		return new StringExponentPair(s, e, negative);
	}
	private static double parseDblName(String namedDouble, int length) {
		if ((length != 3) && (length != 4) && (length != 8) && (length != 9)) {
			throw new NumberFormatException();
		}
		boolean negative = false;
		int cmpstart = 0;
		switch (namedDouble.charAt(0)) {
		case '-':
			negative = true; 
		case '+':
			cmpstart = 1;
		default:
		}
		if (namedDouble.regionMatches(false, cmpstart, "Infinity", 0, 8)) {
			return negative ? Double.NEGATIVE_INFINITY
					: Float.POSITIVE_INFINITY;
		}
		if (namedDouble.regionMatches(false, cmpstart, "NaN", 0, 3)) {
			return Double.NaN;
		}
		throw new NumberFormatException();
	}
	private static float parseFltName(String namedFloat, int length) {
		if ((length != 3) && (length != 4) && (length != 8) && (length != 9)) {
			throw new NumberFormatException();
		}
		boolean negative = false;
		int cmpstart = 0;
		switch (namedFloat.charAt(0)) {
		case '-':
			negative = true; 
		case '+':
			cmpstart = 1;
		default:
		}
		if (namedFloat.regionMatches(false, cmpstart, "Infinity", 0, 8)) {
			return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		}
		if (namedFloat.regionMatches(false, cmpstart, "NaN", 0, 3)) {
			return Float.NaN;
		}
		throw new NumberFormatException();
	}
	public static double parseDouble(String s) {
		s = s.trim();
		int length = s.length();
		if (length == 0) {
			throw new NumberFormatException(s);
		}
		char last = s.charAt(length - 1);
		if ((last == 'y') || (last == 'N')) {
			return parseDblName(s, length);
		}
        if (s.toLowerCase().indexOf("0x") != -1) { 
            return HexStringParser.parseDouble(s);
        }
		StringExponentPair info = initialParse(s, length);
		double result = parseDblImpl(info.s, info.e);
		if (info.negative)
			result = -result;
		return result;
	}
	public static float parseFloat(String s) {
		s = s.trim();
		int length = s.length();
		if (length == 0) {
			throw new NumberFormatException(s);
		}
		char last = s.charAt(length - 1);
		if ((last == 'y') || (last == 'N')) {
			return parseFltName(s, length);
		}
        if (s.toLowerCase().indexOf("0x") != -1) { 
            return HexStringParser.parseFloat(s);
        }
		StringExponentPair info = initialParse(s, length);
		float result = parseFltImpl(info.s, info.e);
		if (info.negative)
			result = -result;
		return result;
	}
}
