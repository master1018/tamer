class PackedIntVector {
    private final int mColumns;
    private int mRows;
    private int mRowGapStart;
    private int mRowGapLength;
    private int[] mValues;
    private int[] mValueGap; 
    public PackedIntVector(int columns) {
        mColumns = columns;
        mRows = 0;
        mRowGapStart = 0;
        mRowGapLength = mRows;
        mValues = null;
        mValueGap = new int[2 * columns];
    }
    public int getValue(int row, int column) {
        final int columns = mColumns;
        if (((row | column) < 0) || (row >= size()) || (column >= columns)) {
            throw new IndexOutOfBoundsException(row + ", " + column);
        }
        if (row >= mRowGapStart) {
            row += mRowGapLength;
        }
        int value = mValues[row * columns + column];
        int[] valuegap = mValueGap;
        if (row >= valuegap[column]) {
            value += valuegap[column + columns];
        }
        return value;
    }
    public void setValue(int row, int column, int value) {
        if (((row | column) < 0) || (row >= size()) || (column >= mColumns)) {
            throw new IndexOutOfBoundsException(row + ", " + column);
        }
        if (row >= mRowGapStart) {
            row += mRowGapLength;
        }
        int[] valuegap = mValueGap;
        if (row >= valuegap[column]) {
            value -= valuegap[column + mColumns];
        }
        mValues[row * mColumns + column] = value;
    }
    private void setValueInternal(int row, int column, int value) {
        if (row >= mRowGapStart) {
            row += mRowGapLength;
        }
        int[] valuegap = mValueGap;
        if (row >= valuegap[column]) {
            value -= valuegap[column + mColumns];
        }
        mValues[row * mColumns + column] = value;
    }
    public void adjustValuesBelow(int startRow, int column, int delta) {
        if (((startRow | column) < 0) || (startRow > size()) ||
                (column >= width())) {
            throw new IndexOutOfBoundsException(startRow + ", " + column);
        }
        if (startRow >= mRowGapStart) {
            startRow += mRowGapLength;
        }
        moveValueGapTo(column, startRow);
        mValueGap[column + mColumns] += delta;
    }
    public void insertAt(int row, int[] values) {
        if ((row < 0) || (row > size())) {
            throw new IndexOutOfBoundsException("row " + row);
        }
        if ((values != null) && (values.length < width())) {
            throw new IndexOutOfBoundsException("value count " + values.length);
        }
        moveRowGapTo(row);
        if (mRowGapLength == 0) {
            growBuffer();
        }
        mRowGapStart++;
        mRowGapLength--;
        if (values == null) {
            for (int i = mColumns - 1; i >= 0; i--) {
                setValueInternal(row, i, 0);
            }
        } else {
            for (int i = mColumns - 1; i >= 0; i--) {
                setValueInternal(row, i, values[i]);
            }
        }
    }
    public void deleteAt(int row, int count) {
        if (((row | count) < 0) || (row + count > size())) {
            throw new IndexOutOfBoundsException(row + ", " + count);
        }
        moveRowGapTo(row + count);
        mRowGapStart -= count;
        mRowGapLength += count;
    }
    public int size() {
        return mRows - mRowGapLength;
    }
    public int width() {
        return mColumns;
    }
    private final void growBuffer() {
        final int columns = mColumns;
        int newsize = size() + 1;
        newsize = ArrayUtils.idealIntArraySize(newsize * columns) / columns;
        int[] newvalues = new int[newsize * columns];
        final int[] valuegap = mValueGap;
        final int rowgapstart = mRowGapStart;
        int after = mRows - (rowgapstart + mRowGapLength);
        if (mValues != null) {
            System.arraycopy(mValues, 0, newvalues, 0, columns * rowgapstart);
            System.arraycopy(mValues, (mRows - after) * columns,
                             newvalues, (newsize - after) * columns,
                             after * columns);
        }
        for (int i = 0; i < columns; i++) {
            if (valuegap[i] >= rowgapstart) {
                valuegap[i] += newsize - mRows;
                if (valuegap[i] < rowgapstart) {
                    valuegap[i] = rowgapstart;
                }
            }
        }
        mRowGapLength += newsize - mRows;
        mRows = newsize;
        mValues = newvalues;
    }
    private final void moveValueGapTo(int column, int where) {
        final int[] valuegap = mValueGap;
        final int[] values = mValues;
        final int columns = mColumns;
        if (where == valuegap[column]) {
            return;
        } else if (where > valuegap[column]) {
            for (int i = valuegap[column]; i < where; i++) {
                values[i * columns + column] += valuegap[column + columns];
            }
        } else  {
            for (int i = where; i < valuegap[column]; i++) {
                values[i * columns + column] -= valuegap[column + columns];
            }
        }
        valuegap[column] = where;
    }
    private final void moveRowGapTo(int where) {
        if (where == mRowGapStart) {
            return;
        } else if (where > mRowGapStart) {
            int moving = where + mRowGapLength - (mRowGapStart + mRowGapLength);
            final int columns = mColumns;
            final int[] valuegap = mValueGap;
            final int[] values = mValues;
            final int gapend = mRowGapStart + mRowGapLength;
            for (int i = gapend; i < gapend + moving; i++) {
                int destrow = i - gapend + mRowGapStart;
                for (int j = 0; j < columns; j++) {
                    int val = values[i * columns+ j];
                    if (i >= valuegap[j]) {
                        val += valuegap[j + columns];
                    }
                    if (destrow >= valuegap[j]) {
                        val -= valuegap[j + columns];
                    }
                    values[destrow * columns + j] = val;
                }
            }
        } else  {
            int moving = mRowGapStart - where;
            final int columns = mColumns;
            final int[] valuegap = mValueGap;
            final int[] values = mValues;
            final int gapend = mRowGapStart + mRowGapLength;
            for (int i = where + moving - 1; i >= where; i--) {
                int destrow = i - where + gapend - moving;
                for (int j = 0; j < columns; j++) {
                    int val = values[i * columns+ j];
                    if (i >= valuegap[j]) {
                        val += valuegap[j + columns];
                    }
                    if (destrow >= valuegap[j]) {
                        val -= valuegap[j + columns];
                    }
                    values[destrow * columns + j] = val;
                }
            }
        }
        mRowGapStart = where;
    }
}
