public class TokenTracker {
    static final int MAX_INTERVALS = 5;
    private int initNumber;
    private int windowStart;
    private int expectedNumber;
    private int windowStartIndex = 0;
    private LinkedList<Entry> list = new LinkedList<Entry>();
    public TokenTracker(int initNumber) {
        this.initNumber = initNumber;
        this.windowStart = initNumber;
        this.expectedNumber = initNumber;
        Entry entry = new Entry(initNumber-1);
        list.add(entry);
    }
    private int getIntervalIndex(int number) {
        Entry entry = null;
        int i;
        for (i = list.size() - 1; i >= 0; i--) {
            entry = list.get(i);
            if (entry.compareTo(number) <= 0)
                break;
        }
        return i;
    }
    synchronized public final void getProps(int number, MessageProp prop) {
        boolean gap = false;
        boolean old = false;
        boolean unsequenced = false;
        boolean duplicate = false;
        int pos = getIntervalIndex(number);
        Entry entry = null;
        if (pos != -1)
            entry = list.get(pos);
        if (number == expectedNumber) {
            expectedNumber++;
        } else {
            if (entry != null && entry.contains(number))
                duplicate = true;
            else {
                if (expectedNumber >= initNumber) {
                    if (number > expectedNumber) {
                        gap = true;
                    } else if (number >= windowStart) {
                        unsequenced = true;
                    } else if (number >= initNumber) {
                        old = true;
                    } else {
                        gap = true;
                    }
                } else {
                    if (number > expectedNumber) {
                        if (number < initNumber) {
                            gap = true;
                        } else if (windowStart >= initNumber) {
                            if (number >= windowStart) {
                               unsequenced = true;
                            } else
                                old = true;
                        } else {
                            old = true;
                        }
                    } else if (windowStart > expectedNumber) {
                        unsequenced = true;
                    } else if (number < windowStart) {
                        old = true;
                    } else
                        unsequenced = true;
                }
            }
        }
        if (!duplicate && !old)
            add(number, pos);
        if (gap)
            expectedNumber = number+1;
        prop.setSupplementaryStates(duplicate, old, unsequenced, gap,
                                    0, null);
    }
    private void add(int number, int prevEntryPos) {
        Entry entry;
        Entry entryBefore = null;
        Entry entryAfter = null;
        boolean appended = false;
        boolean prepended = false;
        if (prevEntryPos != -1) {
            entryBefore = list.get(prevEntryPos);
            if (number == (entryBefore.getEnd() + 1)) {
                entryBefore.setEnd(number);
                appended = true;
            }
        }
        int nextEntryPos = prevEntryPos + 1;
        if ((nextEntryPos) < list.size()) {
            entryAfter = list.get(nextEntryPos);
            if (number == (entryAfter.getStart() - 1)) {
                if (!appended) {
                    entryAfter.setStart(number);
                } else {
                    entryAfter.setStart(entryBefore.getStart());
                    list.remove(prevEntryPos);
                    if (windowStartIndex > prevEntryPos)
                        windowStartIndex--;
                }
                prepended = true;
            }
        }
        if (prepended || appended)
            return;
        if (list.size() < MAX_INTERVALS) {
            entry = new Entry(number);
            if (prevEntryPos  < windowStartIndex)
                windowStartIndex++; 
        } else {
            int oldWindowStartIndex = windowStartIndex;
            if (windowStartIndex == (list.size() - 1))
                windowStartIndex = 0;
            entry = list.remove(oldWindowStartIndex);
            windowStart = list.get(windowStartIndex).getStart();
            entry.setStart(number);
            entry.setEnd(number);
            if (prevEntryPos >= oldWindowStartIndex) {
                prevEntryPos--; 
            } else {
                if (oldWindowStartIndex != windowStartIndex) {
                    if (prevEntryPos == -1)
                        windowStart = number;
                } else {
                    windowStartIndex++;
                }
            }
        }
        list.add(prevEntryPos+1, entry);
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("TokenTracker: ");
        buf.append(" initNumber=").append(initNumber);
        buf.append(" windowStart=").append(windowStart);
        buf.append(" expectedNumber=").append(expectedNumber);
        buf.append(" windowStartIndex=").append(windowStartIndex);
        buf.append("\n\tIntervals are: {");
        for (int i = 0; i < list.size(); i++) {
            if (i != 0)
                buf.append(", ");
            buf.append(list.get(i).toString());
        }
        buf.append('}');
        return buf.toString();
    }
    class Entry {
        private int start;
        private int end;
        Entry(int number) {
            start = number;
            end = number;
        }
        final int compareTo(int number) {
            if (start > number)
                return 1;
            else if (end < number)
                return -1;
            else
                return 0;
        }
        final boolean contains(int number) {
            return (number >= start &&
                    number <= end);
        }
        final void append(int number) {
            if (number == (end + 1))
                end = number;
        }
        final void setInterval(int start, int end) {
            this.start = start;
            this.end = end;
        }
        final void setEnd(int end) {
            this.end = end;
        }
        final void setStart(int start) {
            this.start = start;
        }
        final int getStart() {
            return start;
        }
        final int getEnd() {
            return end;
        }
        public String toString() {
            return ("[" + start + ", " + end + "]");
        }
    }
}
