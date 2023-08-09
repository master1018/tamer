public final class StringBuffer extends AbstractStringBuilder implements
        Appendable, Serializable, CharSequence {
    private static final long serialVersionUID = 3388685877147921107L;
    private static final ObjectStreamField serialPersistentFields[] = {
            new ObjectStreamField("count", int.class), 
            new ObjectStreamField("shared", boolean.class), 
            new ObjectStreamField("value", char[].class), }; 
    public StringBuffer() {
        super();
    }
    public StringBuffer(int capacity) {
        super(capacity);
    }
    public StringBuffer(String string) {
        super(string);
    }
    public StringBuffer(CharSequence cs) {
        super(cs.toString());
    }
    public StringBuffer append(boolean b) {
        return append(b ? "true" : "false"); 
    }
    public synchronized StringBuffer append(char ch) {
        append0(ch);
        return this;
    }
    public StringBuffer append(double d) {
        return append(Double.toString(d));
    }
    public StringBuffer append(float f) {
        return append(Float.toString(f));
    }
    public StringBuffer append(int i) {
        return append(Integer.toString(i));
    }
    public StringBuffer append(long l) {
        return append(Long.toString(l));
    }
    public synchronized StringBuffer append(Object obj) {
        if (obj == null) {
            appendNull();
        } else {
            append0(obj.toString());
        }
        return this;
    }
    public synchronized StringBuffer append(String string) {
        append0(string);
        return this;
    }
    public synchronized StringBuffer append(StringBuffer sb) {
        if (sb == null) {
            appendNull();
        } else {
            synchronized (sb) {
                append0(sb.getValue(), 0, sb.length());
            }
        }
        return this;
    }
    public synchronized StringBuffer append(char chars[]) {
        append0(chars);
        return this;
    }
    public synchronized StringBuffer append(char chars[], int start, int length) {
        append0(chars, start, length);
        return this;
    }
    public synchronized StringBuffer append(CharSequence s) {
        if (s == null) {
            appendNull();
        } else {
            append0(s.toString());
        }
        return this;
    }
    public synchronized StringBuffer append(CharSequence s, int start, int end) {
        append0(s, start, end);
        return this;
    }
    public StringBuffer appendCodePoint(int codePoint) {
        return append(Character.toChars(codePoint));
    }
    @Override
    public synchronized char charAt(int index) {
        return super.charAt(index);
    }
    @Override
    public synchronized int codePointAt(int index) {
        return super.codePointAt(index);
    }
    @Override
    public synchronized int codePointBefore(int index) {
        return super.codePointBefore(index);
    }
    @Override
    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }
    public synchronized StringBuffer delete(int start, int end) {
        delete0(start, end);
        return this;
    }
    public synchronized StringBuffer deleteCharAt(int location) {
        deleteCharAt0(location);
        return this;
    }
    @Override
    public synchronized void ensureCapacity(int min) {
        super.ensureCapacity(min);
    }
    @Override
    public synchronized void getChars(int start, int end, char[] buffer, int idx) {
        super.getChars(start, end, buffer, idx);
    }
    @Override
    public synchronized int indexOf(String subString, int start) {
        return super.indexOf(subString, start);
    }
    public synchronized StringBuffer insert(int index, char ch) {
        insert0(index, ch);
        return this;
    }
    public StringBuffer insert(int index, boolean b) {
        return insert(index, b ? "true" : "false"); 
    }
    public StringBuffer insert(int index, int i) {
        return insert(index, Integer.toString(i));
    }
    public StringBuffer insert(int index, long l) {
        return insert(index, Long.toString(l));
    }
    public StringBuffer insert(int index, double d) {
        return insert(index, Double.toString(d));
    }
    public StringBuffer insert(int index, float f) {
        return insert(index, Float.toString(f));
    }
    public StringBuffer insert(int index, Object obj) {
        return insert(index, obj == null ? "null" : obj.toString()); 
    }
    public synchronized StringBuffer insert(int index, String string) {
        insert0(index, string);
        return this;
    }
    public synchronized StringBuffer insert(int index, char[] chars) {
        insert0(index, chars);
        return this;
    }
    public synchronized StringBuffer insert(int index, char chars[], int start,
            int length) {
        insert0(index, chars, start, length);
        return this;
    }
    public synchronized StringBuffer insert(int index, CharSequence s) {
        insert0(index, s == null ? "null" : s.toString()); 
        return this;
    }
    public synchronized StringBuffer insert(int index, CharSequence s,
            int start, int end) {
        insert0(index, s, start, end);
        return this;
    }
    @Override
    public synchronized int lastIndexOf(String subString, int start) {
        return super.lastIndexOf(subString, start);
    }
    @Override
    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }
    public synchronized StringBuffer replace(int start, int end, String string) {
        replace0(start, end, string);
        return this;
    }
    public synchronized StringBuffer reverse() {
        reverse0();
        return this;
    }
    @Override
    public synchronized void setCharAt(int index, char ch) {
        super.setCharAt(index, ch);
    }
    @Override
    public synchronized void setLength(int length) {
        super.setLength(length);
    }
    @Override
    public synchronized CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }
    @Override
    public synchronized String substring(int start) {
        return super.substring(start);
    }
    @Override
    public synchronized String substring(int start, int end) {
        return super.substring(start, end);
    }
    @Override
    public synchronized String toString() {
        return super.toString();
    }
    @Override
    public synchronized void trimToSize() {
        super.trimToSize();
    }
    private synchronized void writeObject(ObjectOutputStream out)
            throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("count", length()); 
        fields.put("shared", false); 
        fields.put("value", getValue()); 
        out.writeFields();
    }
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        int count = fields.get("count", 0); 
        char[] value = (char[]) fields.get("value", null); 
        set(value, count);
    }
}
