class Record {
    private static final String[] REPLACE_CLASSES = {
            "com.google.android.apps.maps:FriendService",
            "com.google.android.apps.maps\\u003AFriendService",
            "com.google.android.apps.maps:driveabout",
            "com.google.android.apps.maps\\u003Adriveabout",
            "com.google.android.apps.maps:LocationFriendService",
            "com.google.android.apps.maps\\u003ALocationFriendService",
    };
    enum Type {
        START_LOAD,
        END_LOAD,
        START_INIT,
        END_INIT
    }
    final int ppid;
    final int pid;
    final int tid;
    final String processName;
    final int classLoader;
    final Type type;
    final String className;
    final long time;
    int sourceLineNumber;
    Record(String line, int lineNum) {
        char typeChar = line.charAt(0);
        switch (typeChar) {
            case '>': type = Type.START_LOAD; break;
            case '<': type = Type.END_LOAD; break;
            case '+': type = Type.START_INIT; break;
            case '-': type = Type.END_INIT; break;
            default: throw new AssertionError("Bad line: " + line);
        }
        sourceLineNumber = lineNum;
        for (int i = 0; i < REPLACE_CLASSES.length; i+= 2) {
            line = line.replace(REPLACE_CLASSES[i], REPLACE_CLASSES[i+1]);
        }
        line = line.substring(1);
        String[] parts = line.split(":");
        ppid = Integer.parseInt(parts[0]);
        pid = Integer.parseInt(parts[1]);
        tid = Integer.parseInt(parts[2]);
        processName = decode(parts[3]).intern();
        classLoader = Integer.parseInt(parts[4]);
        className = vmTypeToLanguage(decode(parts[5])).intern();
        time = Long.parseLong(parts[6]);
    }
    String decode(String rawField) {
        String result = rawField;
        int offset = result.indexOf("\\u");
        while (offset >= 0) {
            String before = result.substring(0, offset);
            String escaped = result.substring(offset+2, offset+6);
            String after = result.substring(offset+6);
            result = String.format("%s%c%s", before, Integer.parseInt(escaped, 16), after);
            offset = result.indexOf("\\u", offset + 1);          
        }
        return result;
    }
    String vmTypeToLanguage(String typeName) {
        if ("(null)".equals(typeName)) {
            return typeName;
        }
        if (!typeName.startsWith("L") || !typeName.endsWith(";") ) {
            throw new AssertionError("Bad name: " + typeName + " in line " + sourceLineNumber);
        }
        typeName = typeName.substring(1, typeName.length() - 1);
        return typeName.replace("/", ".");
    }
}
