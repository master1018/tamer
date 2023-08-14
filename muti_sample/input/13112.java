public final class BidiUtils {
    public static void getLevels(Bidi bidi, byte[] levels, int start) {
        int limit = start + bidi.getLength();
        if (start < 0 || limit > levels.length) {
            throw new IndexOutOfBoundsException("levels.length = " + levels.length +
                " start: " + start + " limit: " + limit);
        }
        int runCount = bidi.getRunCount();
        int p = start;
        for (int i = 0; i < runCount; ++i) {
            int rlimit = start + bidi.getRunLimit(i);
            byte rlevel = (byte)bidi.getRunLevel(i);
            while (p < rlimit) {
                levels[p++] = rlevel;
            }
        }
    }
    public static byte[] getLevels(Bidi bidi) {
        byte[] levels = new byte[bidi.getLength()];
        getLevels(bidi, levels, 0);
        return levels;
    }
    static final char NUMLEVELS = 62;
    public static int[] createVisualToLogicalMap(byte[] levels) {
        int len = levels.length;
        int[] mapping = new int[len];
        byte lowestOddLevel = (byte)(NUMLEVELS + 1);
        byte highestLevel = 0;
        for (int i = 0; i < len; i++) {
            mapping[i] = i;
            byte level = levels[i];
            if (level > highestLevel) {
                highestLevel = level;
            }
            if ((level & 0x01) != 0 && level < lowestOddLevel) {
                lowestOddLevel = level;
            }
        }
        while (highestLevel >= lowestOddLevel) {
            int i = 0;
            for (;;) {
                while (i < len && levels[i] < highestLevel) {
                    i++;
                }
                int begin = i++;
                if (begin == levels.length) {
                    break; 
                }
                while (i < len && levels[i] >= highestLevel) {
                    i++;
                }
                int end = i - 1;
                while (begin < end) {
                    int temp = mapping[begin];
                    mapping[begin] = mapping[end];
                    mapping[end] = temp;
                    ++begin;
                    --end;
                }
            }
            --highestLevel;
        }
        return mapping;
    }
    public static int[] createInverseMap(int[] values) {
        if (values == null) {
            return null;
        }
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[values[i]] = i;
        }
        return result;
    }
    public static int[] createContiguousOrder(int[] values) {
        if (values != null) {
            return computeContiguousOrder(values, 0, values.length);
        }
        return null;
    }
    private static int[] computeContiguousOrder(int[] values, int start,
                                                int limit) {
        int[] result = new int[limit-start];
        for (int i=0; i < result.length; i++) {
            result[i] = i + start;
        }
        for (int i=0; i < result.length-1; i++) {
            int minIndex = i;
            int currentValue = values[result[minIndex]];
            for (int j=i; j < result.length; j++) {
                if (values[result[j]] < currentValue) {
                    minIndex = j;
                    currentValue = values[result[minIndex]];
                }
            }
            int temp = result[i];
            result[i] = result[minIndex];
            result[minIndex] = temp;
        }
        if (start != 0) {
            for (int i=0; i < result.length; i++) {
                result[i] -= start;
            }
        }
        int k;
        for (k=0; k < result.length; k++) {
            if (result[k] != k) {
                break;
            }
        }
        if (k == result.length) {
            return null;
        }
        return createInverseMap(result);
    }
    public static int[] createNormalizedMap(int[] values, byte[] levels,
                                           int start, int limit) {
        if (values != null) {
            if (start != 0 || limit != values.length) {
                boolean copyRange, canonical;
                byte primaryLevel;
                if (levels == null) {
                    primaryLevel = (byte) 0x0;
                    copyRange = true;
                    canonical = true;
                }
                else {
                    if (levels[start] == levels[limit-1]) {
                        primaryLevel = levels[start];
                        canonical = (primaryLevel & (byte)0x1) == 0;
                        int i;
                        for (i=start; i < limit; i++) {
                            if (levels[i] < primaryLevel) {
                                break;
                            }
                            if (canonical) {
                                canonical = levels[i] == primaryLevel;
                            }
                        }
                        copyRange = (i == limit);
                    }
                    else {
                        copyRange = false;
                        primaryLevel = (byte) 0x0;
                        canonical = false;
                    }
                }
                if (copyRange) {
                    if (canonical) {
                        return null;
                    }
                    int[] result = new int[limit-start];
                    int baseValue;
                    if ((primaryLevel & (byte)0x1) != 0) {
                        baseValue = values[limit-1];
                    } else {
                        baseValue = values[start];
                    }
                    if (baseValue == 0) {
                        System.arraycopy(values, start, result, 0, limit-start);
                    }
                    else {
                        for (int j=0; j < result.length; j++) {
                            result[j] = values[j+start] - baseValue;
                        }
                    }
                    return result;
                }
                else {
                    return computeContiguousOrder(values, start, limit);
                }
            }
            else {
                return values;
            }
        }
        return null;
    }
    public static void reorderVisually(byte[] levels, Object[] objects) {
        int len = levels.length;
        byte lowestOddLevel = (byte)(NUMLEVELS + 1);
        byte highestLevel = 0;
        for (int i = 0; i < len; i++) {
            byte level = levels[i];
            if (level > highestLevel) {
                highestLevel = level;
            }
            if ((level & 0x01) != 0 && level < lowestOddLevel) {
                lowestOddLevel = level;
            }
        }
        while (highestLevel >= lowestOddLevel) {
            int i = 0;
            for (;;) {
                while (i < len && levels[i] < highestLevel) {
                    i++;
                }
                int begin = i++;
                if (begin == levels.length) {
                    break; 
                }
                while (i < len && levels[i] >= highestLevel) {
                    i++;
                }
                int end = i - 1;
                while (begin < end) {
                    Object temp = objects[begin];
                    objects[begin] = objects[end];
                    objects[end] = temp;
                    ++begin;
                    --end;
                }
            }
            --highestLevel;
        }
    }
}
