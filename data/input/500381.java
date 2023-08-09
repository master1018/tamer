public final class StringUtils {
    private StringUtils() {
    }
    public static String combineStrings(Object[] list) {
        int listLength = list.length;
        switch (listLength) {
            case 0: {
                return "";
            }
            case 1: {
                return (String) list[0];
            }
        }
        int strLength = 0;
        for (int i = 0; i < listLength; i++) {
            strLength += ((String) list[i]).length();
        }
        StringBuilder sb = new StringBuilder(strLength);
        for (int i = 0; i < listLength; i++) {
            sb.append(list[i]);
        }
        return sb.toString();
    }
}
