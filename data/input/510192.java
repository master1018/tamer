public class ContactLookupKey {
    public static final int LOOKUP_TYPE_SOURCE_ID = 0;
    public static final int LOOKUP_TYPE_DISPLAY_NAME = 1;
    public static final int LOOKUP_TYPE_RAW_CONTACT_ID = 2;
    public static class LookupKeySegment implements Comparable<LookupKeySegment> {
        public int accountHashCode;
        public int lookupType;
        public String rawContactId;
        public String key;
        public long contactId;
        public int compareTo(LookupKeySegment another) {
            if (contactId > another.contactId) {
                return -1;
            }
            if (contactId < another.contactId) {
                return 1;
            }
            return 0;
        }
    }
    public static int getAccountHashCode(String accountType, String accountName) {
        if (accountType == null || accountName == null) {
            return 0;
        }
        return (accountType.hashCode() ^ accountName.hashCode()) & 0xFFF;
    }
    public static void appendToLookupKey(StringBuilder lookupKey, String accountType,
            String accountName, long rawContactId, String sourceId, String displayName) {
        if (displayName == null) {
            displayName = "";
        }
        if (lookupKey.length() != 0) {
            lookupKey.append(".");
        }
        lookupKey.append(getAccountHashCode(accountType, accountName));
        if (sourceId == null) {
            lookupKey.append('r').append(rawContactId).append('-').append(
                    NameNormalizer.normalize(displayName));
        } else {
            int pos = lookupKey.length();
            lookupKey.append('i');
            if (appendEscapedSourceId(lookupKey, sourceId)) {
                lookupKey.setCharAt(pos, 'e');
            }
        }
    }
    private static boolean appendEscapedSourceId(StringBuilder sb, String sourceId) {
        boolean escaped = false;
        int start = 0;
        while (true) {
            int index = sourceId.indexOf('.', start);
            if (index == -1) {
                sb.append(sourceId, start, sourceId.length());
                break;
            }
            escaped = true;
            sb.append(sourceId, start, index);
            sb.append("..");
            start = index + 1;
        }
        return escaped;
    }
    public ArrayList<LookupKeySegment> parse(String lookupKey) {
        ArrayList<LookupKeySegment> list = new ArrayList<LookupKeySegment>();
        String string = Uri.decode(lookupKey);
        int offset = 0;
        int length = string.length();
        int hashCode = 0;
        int lookupType = -1;
        boolean escaped = false;
        String rawContactId = null;
        String key;
        while (offset < length) {
            char c = 0;
            hashCode = 0;
            while (offset < length) {
                c = string.charAt(offset++);
                if (c < '0' || c > '9') {
                    break;
                }
                hashCode = hashCode * 10 + (c - '0');
            }
            if (c == 'i') {
                lookupType = LOOKUP_TYPE_SOURCE_ID;
                escaped = false;
            } else if (c == 'e') {
                lookupType = LOOKUP_TYPE_SOURCE_ID;
                escaped = true;
            } else if (c == 'n') {
                lookupType = LOOKUP_TYPE_DISPLAY_NAME;
            } else if (c == 'r') {
                lookupType = LOOKUP_TYPE_RAW_CONTACT_ID;
            } else {
                throw new IllegalArgumentException("Invalid lookup id: " + lookupKey);
            }
            switch (lookupType) {
                case LOOKUP_TYPE_SOURCE_ID: {
                    if (escaped) {
                        StringBuffer sb = new StringBuffer();
                        while (offset < length) {
                            c = string.charAt(offset++);
                            if (c == '.') {
                                if (offset == length) {
                                    throw new IllegalArgumentException("Invalid lookup id: " +
                                            lookupKey);
                                }
                                c = string.charAt(offset);
                                if (c == '.') {
                                    sb.append('.');
                                    offset++;
                                } else {
                                    break;
                                }
                            } else {
                                sb.append(c);
                            }
                        }
                        key = sb.toString();
                    } else {
                        int start = offset;
                        while (offset < length) {
                            c = string.charAt(offset++);
                            if (c == '.') {
                                break;
                            }
                        }
                        if (offset == length) {
                            key = string.substring(start);
                        } else {
                            key = string.substring(start, offset - 1);
                        }
                    }
                    break;
                }
                case LOOKUP_TYPE_DISPLAY_NAME: {
                    int start = offset;
                    while (offset < length) {
                        c = string.charAt(offset++);
                        if (c == '.') {
                            break;
                        }
                    }
                    if (offset == length) {
                        key = string.substring(start);
                    } else {
                        key = string.substring(start, offset - 1);
                    }
                    break;
                }
                case LOOKUP_TYPE_RAW_CONTACT_ID: {
                    int dash = -1;
                    int start = offset;
                    while (offset < length) {
                        c = string.charAt(offset);
                        if (c == '-' && dash == -1) {
                            dash = offset;
                        }
                        offset++;
                        if (c == '.') {
                            break;
                        }
                    }
                    if (dash != -1) {
                        rawContactId = string.substring(start, dash);
                        start = dash + 1;
                    }
                    if (offset == length) {
                        key = string.substring(start);
                    } else {
                        key = string.substring(start, offset - 1);
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
            LookupKeySegment segment = new LookupKeySegment();
            segment.accountHashCode = hashCode;
            segment.lookupType = lookupType;
            segment.rawContactId = rawContactId;
            segment.key = key;
            segment.contactId = -1;
            list.add(segment);
        }
        return list;
    }
}
