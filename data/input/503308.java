public class AttributedString {
    String text;
    Map<AttributedCharacterIterator.Attribute, List<Range>> attributeMap;
    static class Range {
        int start;
        int end;
        Object value;
        Range(int s, int e, Object v) {
            start = s;
            end = e;
            value = v;
        }
    }
    static class AttributedIterator implements AttributedCharacterIterator {
        private int begin, end, offset;
        private AttributedString attrString;
        private HashSet<Attribute> attributesAllowed;
        AttributedIterator(AttributedString attrString) {
            this.attrString = attrString;
            begin = 0;
            end = attrString.text.length();
            offset = 0;
        }
        AttributedIterator(AttributedString attrString,
                AttributedCharacterIterator.Attribute[] attributes, int begin,
                int end) {
            if (begin < 0 || end > attrString.text.length() || begin > end) {
                throw new IllegalArgumentException();
            }
            this.begin = begin;
            this.end = end;
            offset = begin;
            this.attrString = attrString;
            if (attributes != null) {
                HashSet<Attribute> set = new HashSet<Attribute>(
                        (attributes.length * 4 / 3) + 1);
                for (int i = attributes.length; --i >= 0;) {
                    set.add(attributes[i]);
                }
                attributesAllowed = set;
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public Object clone() {
            try {
                AttributedIterator clone = (AttributedIterator) super.clone();
                if (attributesAllowed != null) {
                    clone.attributesAllowed = (HashSet<Attribute>) attributesAllowed
                            .clone();
                }
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e); 
            }
        }
        public char current() {
            if (offset == end) {
                return DONE;
            }
            return attrString.text.charAt(offset);
        }
        public char first() {
            if (begin == end) {
                return DONE;
            }
            offset = begin;
            return attrString.text.charAt(offset);
        }
        public int getBeginIndex() {
            return begin;
        }
        public int getEndIndex() {
            return end;
        }
        public int getIndex() {
            return offset;
        }
        private boolean inRange(Range range) {
            if (!(range.value instanceof Annotation)) {
                return true;
            }
            return range.start >= begin && range.start < end
                    && range.end > begin && range.end <= end;
        }
        private boolean inRange(List<Range> ranges) {
            Iterator<Range> it = ranges.iterator();
            while (it.hasNext()) {
                Range range = it.next();
                if (range.start >= begin && range.start < end) {
                    return !(range.value instanceof Annotation)
                            || (range.end > begin && range.end <= end);
                } else if (range.end > begin && range.end <= end) {
                    return !(range.value instanceof Annotation)
                            || (range.start >= begin && range.start < end);
                }
            }
            return false;
        }
        public Set<AttributedIterator.Attribute> getAllAttributeKeys() {
            if (begin == 0 && end == attrString.text.length()
                    && attributesAllowed == null) {
                return attrString.attributeMap.keySet();
            }
            Set<AttributedIterator.Attribute> result = new HashSet<Attribute>(
                    (attrString.attributeMap.size() * 4 / 3) + 1);
            Iterator<Map.Entry<Attribute, List<Range>>> it = attrString.attributeMap
                    .entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Attribute, List<Range>> entry = it.next();
                if (attributesAllowed == null
                        || attributesAllowed.contains(entry.getKey())) {
                    List<Range> ranges = entry.getValue();
                    if (inRange(ranges)) {
                        result.add(entry.getKey());
                    }
                }
            }
            return result;
        }
        private Object currentValue(List<Range> ranges) {
            Iterator<Range> it = ranges.iterator();
            while (it.hasNext()) {
                Range range = it.next();
                if (offset >= range.start && offset < range.end) {
                    return inRange(range) ? range.value : null;
                }
            }
            return null;
        }
        public Object getAttribute(
                AttributedCharacterIterator.Attribute attribute) {
            if (attributesAllowed != null
                    && !attributesAllowed.contains(attribute)) {
                return null;
            }
            ArrayList<Range> ranges = (ArrayList<Range>) attrString.attributeMap
                    .get(attribute);
            if (ranges == null) {
                return null;
            }
            return currentValue(ranges);
        }
        public Map<Attribute, Object> getAttributes() {
            Map<Attribute, Object> result = new HashMap<Attribute, Object>(
                    (attrString.attributeMap.size() * 4 / 3) + 1);
            Iterator<Map.Entry<Attribute, List<Range>>> it = attrString.attributeMap
                    .entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Attribute, List<Range>> entry = it.next();
                if (attributesAllowed == null
                        || attributesAllowed.contains(entry.getKey())) {
                    Object value = currentValue(entry.getValue());
                    if (value != null) {
                        result.put(entry.getKey(), value);
                    }
                }
            }
            return result;
        }
        public int getRunLimit() {
            return getRunLimit(getAllAttributeKeys());
        }
        private int runLimit(List<Range> ranges) {
            int result = end;
            ListIterator<Range> it = ranges.listIterator(ranges.size());
            while (it.hasPrevious()) {
                Range range = it.previous();
                if (range.end <= begin) {
                    break;
                }
                if (offset >= range.start && offset < range.end) {
                    return inRange(range) ? range.end : result;
                } else if (offset >= range.end) {
                    break;
                }
                result = range.start;
            }
            return result;
        }
        public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
            if (attributesAllowed != null
                    && !attributesAllowed.contains(attribute)) {
                return end;
            }
            ArrayList<Range> ranges = (ArrayList<Range>) attrString.attributeMap
                    .get(attribute);
            if (ranges == null) {
                return end;
            }
            return runLimit(ranges);
        }
        public int getRunLimit(Set<? extends Attribute> attributes) {
            int limit = end;
            Iterator<? extends Attribute> it = attributes.iterator();
            while (it.hasNext()) {
                AttributedCharacterIterator.Attribute attribute = it.next();
                int newLimit = getRunLimit(attribute);
                if (newLimit < limit) {
                    limit = newLimit;
                }
            }
            return limit;
        }
        public int getRunStart() {
            return getRunStart(getAllAttributeKeys());
        }
        private int runStart(List<Range> ranges) {
            int result = begin;
            Iterator<Range> it = ranges.iterator();
            while (it.hasNext()) {
                Range range = it.next();
                if (range.start >= end) {
                    break;
                }
                if (offset >= range.start && offset < range.end) {
                    return inRange(range) ? range.start : result;
                } else if (offset < range.start) {
                    break;
                }
                result = range.end;
            }
            return result;
        }
        public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
            if (attributesAllowed != null
                    && !attributesAllowed.contains(attribute)) {
                return begin;
            }
            ArrayList<Range> ranges = (ArrayList<Range>) attrString.attributeMap
                    .get(attribute);
            if (ranges == null) {
                return begin;
            }
            return runStart(ranges);
        }
        public int getRunStart(Set<? extends Attribute> attributes) {
            int start = begin;
            Iterator<? extends Attribute> it = attributes.iterator();
            while (it.hasNext()) {
                AttributedCharacterIterator.Attribute attribute = it.next();
                int newStart = getRunStart(attribute);
                if (newStart > start) {
                    start = newStart;
                }
            }
            return start;
        }
        public char last() {
            if (begin == end) {
                return DONE;
            }
            offset = end - 1;
            return attrString.text.charAt(offset);
        }
        public char next() {
            if (offset >= (end - 1)) {
                offset = end;
                return DONE;
            }
            return attrString.text.charAt(++offset);
        }
        public char previous() {
            if (offset == begin) {
                return DONE;
            }
            return attrString.text.charAt(--offset);
        }
        public char setIndex(int location) {
            if (location < begin || location > end) {
                throw new IllegalArgumentException();
            }
            offset = location;
            if (offset == end) {
                return DONE;
            }
            return attrString.text.charAt(offset);
        }
    }
    public AttributedString(AttributedCharacterIterator iterator) {
        if (iterator.getBeginIndex() > iterator.getEndIndex()) {
            throw new IllegalArgumentException(Messages.getString("text.0A")); 
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = iterator.getBeginIndex(); i < iterator.getEndIndex(); i++) {
            buffer.append(iterator.current());
            iterator.next();
        }
        text = buffer.toString();
        Set<AttributedCharacterIterator.Attribute> attributes = iterator
                .getAllAttributeKeys();
        if (attributes == null) {
            return;
        }
        attributeMap = new HashMap<Attribute, List<Range>>(
                (attributes.size() * 4 / 3) + 1);
        Iterator<Attribute> it = attributes.iterator();
        while (it.hasNext()) {
            AttributedCharacterIterator.Attribute attribute = it.next();
            iterator.setIndex(0);
            while (iterator.current() != CharacterIterator.DONE) {
                int start = iterator.getRunStart(attribute);
                int limit = iterator.getRunLimit(attribute);
                Object value = iterator.getAttribute(attribute);
                if (value != null) {
                    addAttribute(attribute, value, start, limit);
                }
                iterator.setIndex(limit);
            }
        }
    }
    private AttributedString(AttributedCharacterIterator iterator, int start,
            int end, Set<Attribute> attributes) {
        if (start < iterator.getBeginIndex() || end > iterator.getEndIndex()
                || start > end) {
            throw new IllegalArgumentException();
        }
        if (attributes == null) {
            return;
        }
        StringBuilder buffer = new StringBuilder();
        iterator.setIndex(start);
        while (iterator.getIndex() < end) {
            buffer.append(iterator.current());
            iterator.next();
        }
        text = buffer.toString();
        attributeMap = new HashMap<Attribute, List<Range>>(
                (attributes.size() * 4 / 3) + 1);
        Iterator<Attribute> it = attributes.iterator();
        while (it.hasNext()) {
            AttributedCharacterIterator.Attribute attribute = it.next();
            iterator.setIndex(start);
            while (iterator.getIndex() < end) {
                Object value = iterator.getAttribute(attribute);
                int runStart = iterator.getRunStart(attribute);
                int limit = iterator.getRunLimit(attribute);
                if ((value instanceof Annotation && runStart >= start && limit <= end)
                        || (value != null && !(value instanceof Annotation))) {
                    addAttribute(attribute, value, (runStart < start ? start
                            : runStart)
                            - start, (limit > end ? end : limit) - start);
                }
                iterator.setIndex(limit);
            }
        }
    }
    public AttributedString(AttributedCharacterIterator iterator, int start,
            int end) {
        this(iterator, start, end, iterator.getAllAttributeKeys());
    }
    public AttributedString(AttributedCharacterIterator iterator, int start,
            int end, AttributedCharacterIterator.Attribute[] attributes) {
        this(iterator, start, end, (attributes == null
                ? new HashSet<Attribute>()
                : new HashSet<Attribute>(Arrays.asList(attributes))));
    }
    public AttributedString(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        text = value;
        attributeMap = new HashMap<Attribute, List<Range>>(11);
    }
    public AttributedString(String value,
            Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (value.length() == 0 && !attributes.isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("text.0B")); 
        }
        text = value;
        attributeMap = new HashMap<Attribute, List<Range>>(
                (attributes.size() * 4 / 3) + 1);
        Iterator<?> it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
            ArrayList<Range> ranges = new ArrayList<Range>(1);
            ranges.add(new Range(0, text.length(), entry.getValue()));
            attributeMap.put((AttributedCharacterIterator.Attribute) entry
                    .getKey(), ranges);
        }
    }
    public void addAttribute(AttributedCharacterIterator.Attribute attribute,
            Object value) {
        if (null == attribute) {
            throw new NullPointerException();
        }
        if (text.length() == 0) {
            throw new IllegalArgumentException();
        }
        List<Range> ranges = attributeMap.get(attribute);
        if (ranges == null) {
            ranges = new ArrayList<Range>(1);
            attributeMap.put(attribute, ranges);
        } else {
            ranges.clear();
        }
        ranges.add(new Range(0, text.length(), value));
    }
    public void addAttribute(AttributedCharacterIterator.Attribute attribute,
            Object value, int start, int end) {
        if (null == attribute) {
            throw new NullPointerException();
        }
        if (start < 0 || end > text.length() || start >= end) {
            throw new IllegalArgumentException();
        }
        if (value == null) {
            return;
        }
        List<Range> ranges = attributeMap.get(attribute);
        if (ranges == null) {
            ranges = new ArrayList<Range>(1);
            ranges.add(new Range(start, end, value));
            attributeMap.put(attribute, ranges);
            return;
        }
        ListIterator<Range> it = ranges.listIterator();
        while (it.hasNext()) {
            Range range = it.next();
            if (end <= range.start) {
                it.previous();
                break;
            } else if (start < range.end
                    || (start == range.end && value.equals(range.value))) {
                Range r1 = null, r3;
                it.remove();
                r1 = new Range(range.start, start, range.value);
                r3 = new Range(end, range.end, range.value);
                while (end > range.end && it.hasNext()) {
                    range = it.next();
                    if (end <= range.end) {
                        if (end > range.start
                                || (end == range.start && value.equals(range.value))) {
                            it.remove();
                            r3 = new Range(end, range.end, range.value);
                            break;
                        }
                    } else {
                        it.remove();
                    }
                }
                if (value.equals(r1.value)) {
                    if (value.equals(r3.value)) {
                        it.add(new Range(r1.start < start ? r1.start : start,
                                r3.end > end ? r3.end : end, r1.value));
                    } else {
                        it.add(new Range(r1.start < start ? r1.start : start,
                                end, r1.value));
                        if (r3.start < r3.end) {
                            it.add(r3);
                        }
                    }
                } else {
                    if (value.equals(r3.value)) {
                        if (r1.start < r1.end) {
                            it.add(r1);
                        }
                        it.add(new Range(start, r3.end > end ? r3.end : end,
                                r3.value));
                    } else {
                        if (r1.start < r1.end) {
                            it.add(r1);
                        }
                        it.add(new Range(start, end, value));
                        if (r3.start < r3.end) {
                            it.add(r3);
                        }
                    }
                }
                return;
            }
        }
        it.add(new Range(start, end, value));
    }
    public void addAttributes(
            Map<? extends AttributedCharacterIterator.Attribute, ?> attributes,
            int start, int end) {
        Iterator<?> it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
            addAttribute(
                    (AttributedCharacterIterator.Attribute) entry.getKey(),
                    entry.getValue(), start, end);
        }
    }
    public AttributedCharacterIterator getIterator() {
        return new AttributedIterator(this);
    }
    public AttributedCharacterIterator getIterator(
            AttributedCharacterIterator.Attribute[] attributes) {
        return new AttributedIterator(this, attributes, 0, text.length());
    }
    public AttributedCharacterIterator getIterator(
            AttributedCharacterIterator.Attribute[] attributes, int start,
            int end) {
        return new AttributedIterator(this, attributes, start, end);
    }
}
