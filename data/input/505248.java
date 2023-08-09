class Java6Arrays {
    static <T> T[] copyOf(T[] original, int newLength) {
        if (null == original) {
            throw new NullPointerException();
        }
        if (0 <= newLength) {
            return copyOfRange(original, 0, newLength);
        }
        throw new NegativeArraySizeException();
    }
    static <T, U> T[] copyOf(U[] original, int newLength,
            Class<? extends T[]> newType) {
        if (0 <= newLength) {
            return copyOfRange(original, 0, newLength, newType);
        }
        throw new NegativeArraySizeException();
    }
    @SuppressWarnings("unchecked")
    static <T> T[] copyOfRange(T[] original, int start, int end) {
        if (original.length >= start && 0 <= start) {
            if (start <= end) {
                int length = end - start;
                int copyLength = Math.min(length, original.length - start);
                T[] copy = (T[]) Array.newInstance(original.getClass().getComponentType(), length);
                System.arraycopy(original, start, copy, 0, copyLength);
                return copy;
            }
            throw new IllegalArgumentException();
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    @SuppressWarnings("unchecked")
    static <T, U> T[] copyOfRange(U[] original, int start, int end,
            Class<? extends T[]> newType) {
        if (start <= end) {
            if (original.length >= start && 0 <= start) {
                int length = end - start;
                int copyLength = Math.min(length, original.length - start);
                T[] copy = (T[]) Array.newInstance(newType.getComponentType(),
                        length);
                System.arraycopy(original, start, copy, 0, copyLength);
                return copy;
            }
            throw new ArrayIndexOutOfBoundsException();
        }
        throw new IllegalArgumentException();
    }
}
