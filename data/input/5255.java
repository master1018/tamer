public class AttributedString {
    private static final int ARRAY_SIZE_INCREMENT = 10;
    String text;
    int runArraySize;               
    int runCount;                   
    int runStarts[];                
    Vector runAttributes[];         
    Vector runAttributeValues[];    
    AttributedString(AttributedCharacterIterator[] iterators) {
        if (iterators == null) {
            throw new NullPointerException("Iterators must not be null");
        }
        if (iterators.length == 0) {
            text = "";
        }
        else {
            StringBuffer buffer = new StringBuffer();
            for (int counter = 0; counter < iterators.length; counter++) {
                appendContents(buffer, iterators[counter]);
            }
            text = buffer.toString();
            if (text.length() > 0) {
                int offset = 0;
                Map last = null;
                for (int counter = 0; counter < iterators.length; counter++) {
                    AttributedCharacterIterator iterator = iterators[counter];
                    int start = iterator.getBeginIndex();
                    int end = iterator.getEndIndex();
                    int index = start;
                    while (index < end) {
                        iterator.setIndex(index);
                        Map attrs = iterator.getAttributes();
                        if (mapsDiffer(last, attrs)) {
                            setAttributes(attrs, index - start + offset);
                        }
                        last = attrs;
                        index = iterator.getRunLimit();
                    }
                    offset += (end - start);
                }
            }
        }
    }
    public AttributedString(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
    }
    public AttributedString(String text,
                            Map<? extends Attribute, ?> attributes)
    {
        if (text == null || attributes == null) {
            throw new NullPointerException();
        }
        this.text = text;
        if (text.length() == 0) {
            if (attributes.isEmpty())
                return;
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        int attributeCount = attributes.size();
        if (attributeCount > 0) {
            createRunAttributeDataVectors();
            Vector newRunAttributes = new Vector(attributeCount);
            Vector newRunAttributeValues = new Vector(attributeCount);
            runAttributes[0] = newRunAttributes;
            runAttributeValues[0] = newRunAttributeValues;
            Iterator iterator = attributes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                newRunAttributes.addElement(entry.getKey());
                newRunAttributeValues.addElement(entry.getValue());
            }
        }
    }
    public AttributedString(AttributedCharacterIterator text) {
        this(text, text.getBeginIndex(), text.getEndIndex(), null);
    }
    public AttributedString(AttributedCharacterIterator text,
                            int beginIndex,
                            int endIndex) {
        this(text, beginIndex, endIndex, null);
    }
    public AttributedString(AttributedCharacterIterator text,
                            int beginIndex,
                            int endIndex,
                            Attribute[] attributes) {
        if (text == null) {
            throw new NullPointerException();
        }
        int textBeginIndex = text.getBeginIndex();
        int textEndIndex = text.getEndIndex();
        if (beginIndex < textBeginIndex || endIndex > textEndIndex || beginIndex > endIndex)
            throw new IllegalArgumentException("Invalid substring range");
        StringBuffer textBuffer = new StringBuffer();
        text.setIndex(beginIndex);
        for (char c = text.current(); text.getIndex() < endIndex; c = text.next())
            textBuffer.append(c);
        this.text = textBuffer.toString();
        if (beginIndex == endIndex)
            return;
        HashSet keys = new HashSet();
        if (attributes == null) {
            keys.addAll(text.getAllAttributeKeys());
        } else {
            for (int i = 0; i < attributes.length; i++)
                keys.add(attributes[i]);
            keys.retainAll(text.getAllAttributeKeys());
        }
        if (keys.isEmpty())
            return;
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            Attribute attributeKey = (Attribute)itr.next();
            text.setIndex(textBeginIndex);
            while (text.getIndex() < endIndex) {
                int start = text.getRunStart(attributeKey);
                int limit = text.getRunLimit(attributeKey);
                Object value = text.getAttribute(attributeKey);
                if (value != null) {
                    if (value instanceof Annotation) {
                        if (start >= beginIndex && limit <= endIndex) {
                            addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                        } else {
                            if (limit > endIndex)
                                break;
                        }
                    } else {
                        if (start >= endIndex)
                            break;
                        if (limit > beginIndex) {
                            if (start < beginIndex)
                                start = beginIndex;
                            if (limit > endIndex)
                                limit = endIndex;
                            if (start != limit) {
                                addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                            }
                        }
                    }
                }
                text.setIndex(limit);
            }
        }
    }
    public void addAttribute(Attribute attribute, Object value) {
        if (attribute == null) {
            throw new NullPointerException();
        }
        int len = length();
        if (len == 0) {
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        addAttributeImpl(attribute, value, 0, len);
    }
    public void addAttribute(Attribute attribute, Object value,
            int beginIndex, int endIndex) {
        if (attribute == null) {
            throw new NullPointerException();
        }
        if (beginIndex < 0 || endIndex > length() || beginIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        addAttributeImpl(attribute, value, beginIndex, endIndex);
    }
    public void addAttributes(Map<? extends Attribute, ?> attributes,
                              int beginIndex, int endIndex)
    {
        if (attributes == null) {
            throw new NullPointerException();
        }
        if (beginIndex < 0 || endIndex > length() || beginIndex > endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        if (beginIndex == endIndex) {
            if (attributes.isEmpty())
                return;
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        if (runCount == 0) {
            createRunAttributeDataVectors();
        }
        int beginRunIndex = ensureRunBreak(beginIndex);
        int endRunIndex = ensureRunBreak(endIndex);
        Iterator iterator = attributes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            addAttributeRunData((Attribute) entry.getKey(), entry.getValue(), beginRunIndex, endRunIndex);
        }
    }
    private synchronized void addAttributeImpl(Attribute attribute, Object value,
            int beginIndex, int endIndex) {
        if (runCount == 0) {
            createRunAttributeDataVectors();
        }
        int beginRunIndex = ensureRunBreak(beginIndex);
        int endRunIndex = ensureRunBreak(endIndex);
        addAttributeRunData(attribute, value, beginRunIndex, endRunIndex);
    }
    private final void createRunAttributeDataVectors() {
        int newRunStarts[] = new int[ARRAY_SIZE_INCREMENT];
        Vector newRunAttributes[] = new Vector[ARRAY_SIZE_INCREMENT];
        Vector newRunAttributeValues[] = new Vector[ARRAY_SIZE_INCREMENT];
        runStarts = newRunStarts;
        runAttributes = newRunAttributes;
        runAttributeValues = newRunAttributeValues;
        runArraySize = ARRAY_SIZE_INCREMENT;
        runCount = 1; 
    }
    private final int ensureRunBreak(int offset) {
        return ensureRunBreak(offset, true);
    }
    private final int ensureRunBreak(int offset, boolean copyAttrs) {
        if (offset == length()) {
            return runCount;
        }
        int runIndex = 0;
        while (runIndex < runCount && runStarts[runIndex] < offset) {
            runIndex++;
        }
        if (runIndex < runCount && runStarts[runIndex] == offset) {
            return runIndex;
        }
        if (runCount == runArraySize) {
            int newArraySize = runArraySize + ARRAY_SIZE_INCREMENT;
            int newRunStarts[] = new int[newArraySize];
            Vector newRunAttributes[] = new Vector[newArraySize];
            Vector newRunAttributeValues[] = new Vector[newArraySize];
            for (int i = 0; i < runArraySize; i++) {
                newRunStarts[i] = runStarts[i];
                newRunAttributes[i] = runAttributes[i];
                newRunAttributeValues[i] = runAttributeValues[i];
            }
            runStarts = newRunStarts;
            runAttributes = newRunAttributes;
            runAttributeValues = newRunAttributeValues;
            runArraySize = newArraySize;
        }
        Vector newRunAttributes = null;
        Vector newRunAttributeValues = null;
        if (copyAttrs) {
            Vector oldRunAttributes = runAttributes[runIndex - 1];
            Vector oldRunAttributeValues = runAttributeValues[runIndex - 1];
            if (oldRunAttributes != null) {
                newRunAttributes = (Vector) oldRunAttributes.clone();
            }
            if (oldRunAttributeValues != null) {
                newRunAttributeValues = (Vector) oldRunAttributeValues.clone();
            }
        }
        runCount++;
        for (int i = runCount - 1; i > runIndex; i--) {
            runStarts[i] = runStarts[i - 1];
            runAttributes[i] = runAttributes[i - 1];
            runAttributeValues[i] = runAttributeValues[i - 1];
        }
        runStarts[runIndex] = offset;
        runAttributes[runIndex] = newRunAttributes;
        runAttributeValues[runIndex] = newRunAttributeValues;
        return runIndex;
    }
    private void addAttributeRunData(Attribute attribute, Object value,
            int beginRunIndex, int endRunIndex) {
        for (int i = beginRunIndex; i < endRunIndex; i++) {
            int keyValueIndex = -1; 
            if (runAttributes[i] == null) {
                Vector newRunAttributes = new Vector();
                Vector newRunAttributeValues = new Vector();
                runAttributes[i] = newRunAttributes;
                runAttributeValues[i] = newRunAttributeValues;
            } else {
                keyValueIndex = runAttributes[i].indexOf(attribute);
            }
            if (keyValueIndex == -1) {
                int oldSize = runAttributes[i].size();
                runAttributes[i].addElement(attribute);
                try {
                    runAttributeValues[i].addElement(value);
                }
                catch (Exception e) {
                    runAttributes[i].setSize(oldSize);
                    runAttributeValues[i].setSize(oldSize);
                }
            } else {
                runAttributeValues[i].set(keyValueIndex, value);
            }
        }
    }
    public AttributedCharacterIterator getIterator() {
        return getIterator(null, 0, length());
    }
    public AttributedCharacterIterator getIterator(Attribute[] attributes) {
        return getIterator(attributes, 0, length());
    }
    public AttributedCharacterIterator getIterator(Attribute[] attributes, int beginIndex, int endIndex) {
        return new AttributedStringIterator(attributes, beginIndex, endIndex);
    }
    int length() {
        return text.length();
    }
    private char charAt(int index) {
        return text.charAt(index);
    }
    private synchronized Object getAttribute(Attribute attribute, int runIndex) {
        Vector currentRunAttributes = runAttributes[runIndex];
        Vector currentRunAttributeValues = runAttributeValues[runIndex];
        if (currentRunAttributes == null) {
            return null;
        }
        int attributeIndex = currentRunAttributes.indexOf(attribute);
        if (attributeIndex != -1) {
            return currentRunAttributeValues.elementAt(attributeIndex);
        }
        else {
            return null;
        }
    }
    private Object getAttributeCheckRange(Attribute attribute, int runIndex, int beginIndex, int endIndex) {
        Object value = getAttribute(attribute, runIndex);
        if (value instanceof Annotation) {
            if (beginIndex > 0) {
                int currIndex = runIndex;
                int runStart = runStarts[currIndex];
                while (runStart >= beginIndex &&
                        valuesMatch(value, getAttribute(attribute, currIndex - 1))) {
                    currIndex--;
                    runStart = runStarts[currIndex];
                }
                if (runStart < beginIndex) {
                    return null;
                }
            }
            int textLength = length();
            if (endIndex < textLength) {
                int currIndex = runIndex;
                int runLimit = (currIndex < runCount - 1) ? runStarts[currIndex + 1] : textLength;
                while (runLimit <= endIndex &&
                        valuesMatch(value, getAttribute(attribute, currIndex + 1))) {
                    currIndex++;
                    runLimit = (currIndex < runCount - 1) ? runStarts[currIndex + 1] : textLength;
                }
                if (runLimit > endIndex) {
                    return null;
                }
            }
        }
        return value;
    }
    private boolean attributeValuesMatch(Set attributes, int runIndex1, int runIndex2) {
        Iterator iterator = attributes.iterator();
        while (iterator.hasNext()) {
            Attribute key = (Attribute) iterator.next();
           if (!valuesMatch(getAttribute(key, runIndex1), getAttribute(key, runIndex2))) {
                return false;
            }
        }
        return true;
    }
    private final static boolean valuesMatch(Object value1, Object value2) {
        if (value1 == null) {
            return value2 == null;
        } else {
            return value1.equals(value2);
        }
    }
    private final void appendContents(StringBuffer buf,
                                      CharacterIterator iterator) {
        int index = iterator.getBeginIndex();
        int end = iterator.getEndIndex();
        while (index < end) {
            iterator.setIndex(index++);
            buf.append(iterator.current());
        }
    }
    private void setAttributes(Map attrs, int offset) {
        if (runCount == 0) {
            createRunAttributeDataVectors();
        }
        int index = ensureRunBreak(offset, false);
        int size;
        if (attrs != null && (size = attrs.size()) > 0) {
            Vector runAttrs = new Vector(size);
            Vector runValues = new Vector(size);
            Iterator iterator = attrs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                runAttrs.add(entry.getKey());
                runValues.add(entry.getValue());
            }
            runAttributes[index] = runAttrs;
            runAttributeValues[index] = runValues;
        }
    }
    private static boolean mapsDiffer(Map last, Map attrs) {
        if (last == null) {
            return (attrs != null && attrs.size() > 0);
        }
        return (!last.equals(attrs));
    }
    final private class AttributedStringIterator implements AttributedCharacterIterator {
        private int beginIndex;
        private int endIndex;
        private Attribute[] relevantAttributes;
        private int currentIndex;
        private int currentRunIndex;
        private int currentRunStart;
        private int currentRunLimit;
        AttributedStringIterator(Attribute[] attributes, int beginIndex, int endIndex) {
            if (beginIndex < 0 || beginIndex > endIndex || endIndex > length()) {
                throw new IllegalArgumentException("Invalid substring range");
            }
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.currentIndex = beginIndex;
            updateRunInfo();
            if (attributes != null) {
                relevantAttributes = (Attribute[]) attributes.clone();
            }
        }
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AttributedStringIterator)) {
                return false;
            }
            AttributedStringIterator that = (AttributedStringIterator) obj;
            if (AttributedString.this != that.getString())
                return false;
            if (currentIndex != that.currentIndex || beginIndex != that.beginIndex || endIndex != that.endIndex)
                return false;
            return true;
        }
        public int hashCode() {
            return text.hashCode() ^ currentIndex ^ beginIndex ^ endIndex;
        }
        public Object clone() {
            try {
                AttributedStringIterator other = (AttributedStringIterator) super.clone();
                return other;
            }
            catch (CloneNotSupportedException e) {
                throw new InternalError();
            }
        }
        public char first() {
            return internalSetIndex(beginIndex);
        }
        public char last() {
            if (endIndex == beginIndex) {
                return internalSetIndex(endIndex);
            } else {
                return internalSetIndex(endIndex - 1);
            }
        }
        public char current() {
            if (currentIndex == endIndex) {
                return DONE;
            } else {
                return charAt(currentIndex);
            }
        }
        public char next() {
            if (currentIndex < endIndex) {
                return internalSetIndex(currentIndex + 1);
            }
            else {
                return DONE;
            }
        }
        public char previous() {
            if (currentIndex > beginIndex) {
                return internalSetIndex(currentIndex - 1);
            }
            else {
                return DONE;
            }
        }
        public char setIndex(int position) {
            if (position < beginIndex || position > endIndex)
                throw new IllegalArgumentException("Invalid index");
            return internalSetIndex(position);
        }
        public int getBeginIndex() {
            return beginIndex;
        }
        public int getEndIndex() {
            return endIndex;
        }
        public int getIndex() {
            return currentIndex;
        }
        public int getRunStart() {
            return currentRunStart;
        }
        public int getRunStart(Attribute attribute) {
            if (currentRunStart == beginIndex || currentRunIndex == -1) {
                return currentRunStart;
            } else {
                Object value = getAttribute(attribute);
                int runStart = currentRunStart;
                int runIndex = currentRunIndex;
                while (runStart > beginIndex &&
                        valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex - 1))) {
                    runIndex--;
                    runStart = runStarts[runIndex];
                }
                if (runStart < beginIndex) {
                    runStart = beginIndex;
                }
                return runStart;
            }
        }
        public int getRunStart(Set<? extends Attribute> attributes) {
            if (currentRunStart == beginIndex || currentRunIndex == -1) {
                return currentRunStart;
            } else {
                int runStart = currentRunStart;
                int runIndex = currentRunIndex;
                while (runStart > beginIndex &&
                        AttributedString.this.attributeValuesMatch(attributes, currentRunIndex, runIndex - 1)) {
                    runIndex--;
                    runStart = runStarts[runIndex];
                }
                if (runStart < beginIndex) {
                    runStart = beginIndex;
                }
                return runStart;
            }
        }
        public int getRunLimit() {
            return currentRunLimit;
        }
        public int getRunLimit(Attribute attribute) {
            if (currentRunLimit == endIndex || currentRunIndex == -1) {
                return currentRunLimit;
            } else {
                Object value = getAttribute(attribute);
                int runLimit = currentRunLimit;
                int runIndex = currentRunIndex;
                while (runLimit < endIndex &&
                        valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex + 1))) {
                    runIndex++;
                    runLimit = runIndex < runCount - 1 ? runStarts[runIndex + 1] : endIndex;
                }
                if (runLimit > endIndex) {
                    runLimit = endIndex;
                }
                return runLimit;
            }
        }
        public int getRunLimit(Set<? extends Attribute> attributes) {
            if (currentRunLimit == endIndex || currentRunIndex == -1) {
                return currentRunLimit;
            } else {
                int runLimit = currentRunLimit;
                int runIndex = currentRunIndex;
                while (runLimit < endIndex &&
                        AttributedString.this.attributeValuesMatch(attributes, currentRunIndex, runIndex + 1)) {
                    runIndex++;
                    runLimit = runIndex < runCount - 1 ? runStarts[runIndex + 1] : endIndex;
                }
                if (runLimit > endIndex) {
                    runLimit = endIndex;
                }
                return runLimit;
            }
        }
        public Map<Attribute,Object> getAttributes() {
            if (runAttributes == null || currentRunIndex == -1 || runAttributes[currentRunIndex] == null) {
                return new Hashtable();
            }
            return new AttributeMap(currentRunIndex, beginIndex, endIndex);
        }
        public Set<Attribute> getAllAttributeKeys() {
            if (runAttributes == null) {
                return new HashSet();
            }
            synchronized (AttributedString.this) {
                Set keys = new HashSet();
                int i = 0;
                while (i < runCount) {
                    if (runStarts[i] < endIndex && (i == runCount - 1 || runStarts[i + 1] > beginIndex)) {
                        Vector currentRunAttributes = runAttributes[i];
                        if (currentRunAttributes != null) {
                            int j = currentRunAttributes.size();
                            while (j-- > 0) {
                                keys.add(currentRunAttributes.get(j));
                            }
                        }
                    }
                    i++;
                }
                return keys;
            }
        }
        public Object getAttribute(Attribute attribute) {
            int runIndex = currentRunIndex;
            if (runIndex < 0) {
                return null;
            }
            return AttributedString.this.getAttributeCheckRange(attribute, runIndex, beginIndex, endIndex);
        }
        private AttributedString getString() {
            return AttributedString.this;
        }
        private char internalSetIndex(int position) {
            currentIndex = position;
            if (position < currentRunStart || position >= currentRunLimit) {
                updateRunInfo();
            }
            if (currentIndex == endIndex) {
                return DONE;
            } else {
                return charAt(position);
            }
        }
        private void updateRunInfo() {
            if (currentIndex == endIndex) {
                currentRunStart = currentRunLimit = endIndex;
                currentRunIndex = -1;
            } else {
                synchronized (AttributedString.this) {
                    int runIndex = -1;
                    while (runIndex < runCount - 1 && runStarts[runIndex + 1] <= currentIndex)
                        runIndex++;
                    currentRunIndex = runIndex;
                    if (runIndex >= 0) {
                        currentRunStart = runStarts[runIndex];
                        if (currentRunStart < beginIndex)
                            currentRunStart = beginIndex;
                    }
                    else {
                        currentRunStart = beginIndex;
                    }
                    if (runIndex < runCount - 1) {
                        currentRunLimit = runStarts[runIndex + 1];
                        if (currentRunLimit > endIndex)
                            currentRunLimit = endIndex;
                    }
                    else {
                        currentRunLimit = endIndex;
                    }
                }
            }
        }
    }
    final private class AttributeMap extends AbstractMap<Attribute,Object> {
        int runIndex;
        int beginIndex;
        int endIndex;
        AttributeMap(int runIndex, int beginIndex, int endIndex) {
            this.runIndex = runIndex;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }
        public Set entrySet() {
            HashSet set = new HashSet();
            synchronized (AttributedString.this) {
                int size = runAttributes[runIndex].size();
                for (int i = 0; i < size; i++) {
                    Attribute key = (Attribute) runAttributes[runIndex].get(i);
                    Object value = runAttributeValues[runIndex].get(i);
                    if (value instanceof Annotation) {
                        value = AttributedString.this.getAttributeCheckRange(key,
                                                             runIndex, beginIndex, endIndex);
                        if (value == null) {
                            continue;
                        }
                    }
                    Map.Entry entry = new AttributeEntry(key, value);
                    set.add(entry);
                }
            }
            return set;
        }
        public Object get(Object key) {
            return AttributedString.this.getAttributeCheckRange((Attribute) key, runIndex, beginIndex, endIndex);
        }
    }
}
class AttributeEntry implements Map.Entry {
    private Attribute key;
    private Object value;
    AttributeEntry(Attribute key, Object value) {
        this.key = key;
        this.value = value;
    }
    public boolean equals(Object o) {
        if (!(o instanceof AttributeEntry)) {
            return false;
        }
        AttributeEntry other = (AttributeEntry) o;
        return other.key.equals(key) &&
            (value == null ? other.value == null : other.value.equals(value));
    }
    public Object getKey() {
        return key;
    }
    public Object getValue() {
        return value;
    }
    public Object setValue(Object newValue) {
        throw new UnsupportedOperationException();
    }
    public int hashCode() {
        return key.hashCode() ^ (value==null ? 0 : value.hashCode());
    }
    public String toString() {
        return key.toString()+"="+value.toString();
    }
}
