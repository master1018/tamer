public class Inet6Util {
    public static boolean isIP6AddressInFullForm(String ipAddress) {
        if (isValidIP6Address(ipAddress)) {
            int doubleColonIndex = ipAddress.indexOf("::");
            if (doubleColonIndex >= 0) {
                return false;
            }
            return true;
        }
        return false;
    }
	public static boolean isValidIP6Address(String ipAddress) {
		int length = ipAddress.length();
		boolean doubleColon = false;
		int numberOfColons = 0;
		int numberOfPeriods = 0;
		int numberOfPercent = 0;
		String word = "";
		char c = 0;
		char prevChar = 0;
		int offset = 0; 
		if (length < 2) {
            return false;
        }
		for (int i = 0; i < length; i++) {
			prevChar = c;
			c = ipAddress.charAt(i);
			switch (c) {
			case '[':
				if (i != 0) {
                    return false; 
                }
				if (ipAddress.charAt(length - 1) != ']') {
                    return false; 
                }
				offset = 1;
				if (length < 4) {
                    return false;
                }
				break;
			case ']':
				if (i != length - 1) {
                    return false; 
                }
				if (ipAddress.charAt(0) != '[') {
                    return false; 
                }
				break;
			case '.':
				numberOfPeriods++;
				if (numberOfPeriods > 3) {
                    return false;
                }
				if (!isValidIP4Word(word)) {
                    return false;
                }
				if (numberOfColons != 6 && !doubleColon) {
                    return false;
                }
				if (numberOfColons == 7 && ipAddress.charAt(0 + offset) != ':'
						&& ipAddress.charAt(1 + offset) != ':') {
                    return false;
                }
				word = "";
				break;
			case ':':
				numberOfColons++;
				if (numberOfColons > 7) {
                    return false;
                }
				if (numberOfPeriods > 0) {
                    return false;
                }
				if (prevChar == ':') {
					if (doubleColon) {
                        return false;
                    }
					doubleColon = true;
				}
				word = "";
				break;
			case '%':
				if (numberOfColons == 0) {
                    return false;
                }
				numberOfPercent++;
				if ((i + 1) >= length) {
					return false;
				}
				try {
					Integer.parseInt(ipAddress.substring(i + 1));
				} catch (NumberFormatException e) {
					return false;
				}
				break;
			default:
				if (numberOfPercent == 0) {
					if (word.length() > 3) {
                        return false;
                    }
					if (!isValidHexChar(c)) {
                        return false;
                    }
				}
				word += c;
			}
		}
		if (numberOfPeriods > 0) {
			if (numberOfPeriods != 3 || !isValidIP4Word(word)) {
                return false;
            }
		} else {
			if (numberOfColons != 7 && !doubleColon) {
				return false;
			}
			if (numberOfPercent == 0) {
				if (word == "" && ipAddress.charAt(length - 1 - offset) == ':'
						&& ipAddress.charAt(length - 2 - offset) != ':') {
					return false;
				}
			}
		}
		return true;
	}
	public static boolean isValidIP4Word(String word) {
		char c;
		if (word.length() < 1 || word.length() > 3) {
            return false;
        }
		for (int i = 0; i < word.length(); i++) {
			c = word.charAt(i);
			if (!(c >= '0' && c <= '9')) {
                return false;
            }
		}
		if (Integer.parseInt(word) > 255) {
            return false;
        }
		return true;
	}
	static boolean isValidHexChar(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')
				|| (c >= 'a' && c <= 'f');
	}
	public static boolean isValidIPV4Address(String value) {
            if (!value.matches("\\p{Digit}+(\\.\\p{Digit}+)*")) {
                return false;
            }
            String[] parts = value.split("\\.");
            int length = parts.length;
            if (length < 1 || length > 4) {
                return false;
            }
            if (length == 1) {
                long longValue = Long.parseLong(parts[0]);
                return longValue <= 0xFFFFFFFFL;
            } else {
                for (int i = 0; i < length; i++) {
                    int max = 0xff;
                    if ((length == 2) && (i == 1)) {
                        max = 0xffffff;
                    } else if ((length == 3) && (i == 2)) {
                        max = 0xffff;
                    }
                    if (Integer.parseInt(parts[i]) > max) {
                        return false;
                    }
                }
                return true;
            }
	}
}
