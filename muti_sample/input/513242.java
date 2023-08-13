public class MimeHeader {
    public static final String HEADER_ANDROID_ATTACHMENT_STORE_DATA = "X-Android-Attachment-StoreData";
    public static final String HEADER_ANDROID_BODY_QUOTED_PART = "X-Android-Body-Quoted-Part";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_CONTENT_ID = "Content-ID";
    private static final String[] writeOmitFields = {
        HEADER_ANDROID_ATTACHMENT_STORE_DATA
    };
    protected ArrayList<Field> mFields = new ArrayList<Field>();
    public void clear() {
        mFields.clear();
    }
    public String getFirstHeader(String name) throws MessagingException {
        String[] header = getHeader(name);
        if (header == null) {
            return null;
        }
        return header[0];
    }
    public void addHeader(String name, String value) throws MessagingException {
        mFields.add(new Field(name, value));
    }
    public void setHeader(String name, String value) throws MessagingException {
        if (name == null || value == null) {
            return;
        }
        removeHeader(name);
        addHeader(name, value);
    }
    public String[] getHeader(String name) throws MessagingException {
        ArrayList<String> values = new ArrayList<String>();
        for (Field field : mFields) {
            if (field.name.equalsIgnoreCase(name)) {
                values.add(field.value);
            }
        }
        if (values.size() == 0) {
            return null;
        }
        return values.toArray(new String[] {});
    }
    public void removeHeader(String name) throws MessagingException {
        ArrayList<Field> removeFields = new ArrayList<Field>();
        for (Field field : mFields) {
            if (field.name.equalsIgnoreCase(name)) {
                removeFields.add(field);
            }
        }
        mFields.removeAll(removeFields);
    }
    public String writeToString() {
        if (mFields.size() == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Field field : mFields) {
            if (!Utility.arrayContains(writeOmitFields, field.name)) {
                builder.append(field.name + ": " + field.value + "\r\n");
            }
        }
        return builder.toString();
    }
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);
        for (Field field : mFields) {
            if (!Utility.arrayContains(writeOmitFields, field.name)) {
                writer.write(field.name + ": " + field.value + "\r\n");
            }
        }
        writer.flush();
    }
    private static class Field {
        final String name;
        final String value;
        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }
        @Override
        public String toString() {
            return name + "=" + value;
        }
    }
    @Override
    public String toString() {
        return (mFields == null) ? null : mFields.toString();
    }
}
