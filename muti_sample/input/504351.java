public class InputDevice {
    static final boolean DEBUG_POINTERS = false;
    static final boolean DEBUG_HACKS = false;
    static final int TRACKBALL_MOVEMENT_THRESHOLD = 6;
    static final int MAX_POINTERS = 10;
    private static final int JUMPY_EPSILON_DIVISOR = 212;
    private static final int JUMPY_TRANSITION_DROPS = 3;
    private static final int JUMPY_DROP_LIMIT = 3;
    final int id;
    final int classes;
    final String name;
    final AbsoluteInfo absX;
    final AbsoluteInfo absY;
    final AbsoluteInfo absPressure;
    final AbsoluteInfo absSize;
    long mKeyDownTime = 0;
    int mMetaKeysState = 0;
    final int[] curTouchVals = new int[MotionEvent.NUM_SAMPLE_DATA * 2];
    final MotionState mAbs = new MotionState(0, 0);
    final MotionState mRel = new MotionState(TRACKBALL_MOVEMENT_THRESHOLD,
            TRACKBALL_MOVEMENT_THRESHOLD);
    static class MotionState {
        int xPrecision;
        int yPrecision;
        float xMoveScale;
        float yMoveScale;
        MotionEvent currentMove = null;
        boolean changed = false;
        boolean everChanged = false;
        long mDownTime = 0;
        int[] mPointerIds = new int[MAX_POINTERS];
        boolean mSkipLastPointers;
        int mLastNumPointers = 0;
        final int[] mLastData = new int[MotionEvent.NUM_SAMPLE_DATA * MAX_POINTERS];
        int mNextNumPointers = 0;
        final int[] mNextData = new int[(MotionEvent.NUM_SAMPLE_DATA * MAX_POINTERS)
                                        + MotionEvent.NUM_SAMPLE_DATA];
        final boolean[] mDroppedBadPoint = new boolean[MAX_POINTERS];
        private int mJumpyPointsDropped = 0;
        static final int HISTORY_SIZE = 5;
        int[] mHistoryDataStart = new int[MAX_POINTERS];
        int[] mHistoryDataEnd = new int[MAX_POINTERS];
        final int[] mHistoryData = new int[(MotionEvent.NUM_SAMPLE_DATA * MAX_POINTERS)
                                        * HISTORY_SIZE];
        final int[] mAveragedData = new int[MotionEvent.NUM_SAMPLE_DATA * MAX_POINTERS];
        final int[] mLast2Next = new int[MAX_POINTERS];
        final int[] mNext2Last = new int[MAX_POINTERS];
        final long[] mNext2LastDistance = new long[MAX_POINTERS];
        final float[] mReportData = new float[MotionEvent.NUM_SAMPLE_DATA * MAX_POINTERS];
        int mAddingPointerOffset = 0;
        final boolean[] mDown = new boolean[MAX_POINTERS];
        void dumpIntArray(PrintWriter pw, int[] array) {
            pw.print("[");
            for (int i=0; i<array.length; i++) {
                if (i > 0) pw.print(", ");
                pw.print(array[i]);
            }
            pw.print("]");
        }
        void dumpBooleanArray(PrintWriter pw, boolean[] array) {
            pw.print("[");
            for (int i=0; i<array.length; i++) {
                if (i > 0) pw.print(", ");
                pw.print(array[i] ? "true" : "false");
            }
            pw.print("]");
        }
        void dump(PrintWriter pw, String prefix) {
            pw.print(prefix); pw.print("xPrecision="); pw.print(xPrecision);
                    pw.print(" yPrecision="); pw.println(yPrecision);
            pw.print(prefix); pw.print("xMoveScale="); pw.print(xMoveScale);
                    pw.print(" yMoveScale="); pw.println(yMoveScale);
            if (currentMove != null) {
                pw.print(prefix); pw.print("currentMove="); pw.println(currentMove);
            }
            if (changed || mDownTime != 0) {
                pw.print(prefix); pw.print("changed="); pw.print(changed);
                        pw.print(" mDownTime="); pw.println(mDownTime);
            }
            pw.print(prefix); pw.print("mPointerIds="); dumpIntArray(pw, mPointerIds);
                    pw.println("");
            if (mSkipLastPointers || mLastNumPointers != 0) {
                pw.print(prefix); pw.print("mSkipLastPointers="); pw.print(mSkipLastPointers);
                        pw.print(" mLastNumPointers="); pw.println(mLastNumPointers);
                pw.print(prefix); pw.print("mLastData="); dumpIntArray(pw, mLastData);
                        pw.println("");
            }
            if (mNextNumPointers != 0) {
                pw.print(prefix); pw.print("mNextNumPointers="); pw.println(mNextNumPointers);
                pw.print(prefix); pw.print("mNextData="); dumpIntArray(pw, mNextData);
                        pw.println("");
            }
            pw.print(prefix); pw.print("mDroppedBadPoint=");
                    dumpBooleanArray(pw, mDroppedBadPoint); pw.println("");
            pw.print(prefix); pw.print("mAddingPointerOffset="); pw.println(mAddingPointerOffset);
            pw.print(prefix); pw.print("mDown=");
                    dumpBooleanArray(pw, mDown); pw.println("");
        }
        MotionState(int mx, int my) {
            xPrecision = mx;
            yPrecision = my;
            xMoveScale = mx != 0 ? (1.0f/mx) : 1.0f;
            yMoveScale = my != 0 ? (1.0f/my) : 1.0f;
            for (int i=0; i<MAX_POINTERS; i++) {
                mPointerIds[i] = i;
            }
        }
        void dropBadPoint(InputDevice dev) {
            if (dev.absY == null) {
                return;
            }
            if (mNextNumPointers != mLastNumPointers) {
                return;
            }
            final int maxDy = ((dev.absY.maxValue-dev.absY.minValue)*7)/16;
            for (int i=mNextNumPointers-1; i>=0; i--) {
                final int ioff = i * MotionEvent.NUM_SAMPLE_DATA;
                final int y = mNextData[ioff + MotionEvent.SAMPLE_Y];
                if (DEBUG_HACKS) Slog.v("InputDevice", "Looking at next point #" + i + ": y=" + y);
                boolean dropped = false;
                if (!mDroppedBadPoint[i] && mLastNumPointers > 0) {
                    dropped = true;
                    int closestDy = -1;
                    int closestY = -1;
                    for (int j=mLastNumPointers-1; j>=0; j--) {
                        final int joff = j * MotionEvent.NUM_SAMPLE_DATA;
                        int dy = y - mLastData[joff + MotionEvent.SAMPLE_Y];
                        if (dy < 0) dy = -dy;
                        if (DEBUG_HACKS) Slog.v("InputDevice", "Comparing with last point #" + j
                                + ": y=" + mLastData[joff] + " dy=" + dy);
                        if (dy < maxDy) {
                            dropped = false;
                            break;
                        } else if (closestDy < 0 || dy < closestDy) {
                            closestDy = dy;
                            closestY = mLastData[joff + MotionEvent.SAMPLE_Y];
                        }
                    }
                    if (dropped) {
                        dropped = true;
                        Slog.i("InputDevice", "Dropping bad point #" + i
                                + ": newY=" + y + " closestDy=" + closestDy
                                + " maxDy=" + maxDy);
                        mNextData[ioff + MotionEvent.SAMPLE_Y] = closestY;
                        break;
                    }
                }
                mDroppedBadPoint[i] = dropped;
            }
        }
        void dropJumpyPoint(InputDevice dev) {
            if (dev.absY == null) {
                return;
            }
            final int jumpyEpsilon = dev.absY.range / JUMPY_EPSILON_DIVISOR;
            final int nextNumPointers = mNextNumPointers;
            final int lastNumPointers = mLastNumPointers;
            final int[] nextData = mNextData;
            final int[] lastData = mLastData;
            if (nextNumPointers != mLastNumPointers) {
                if (DEBUG_HACKS) {
                    Slog.d("InputDevice", "Different pointer count " + lastNumPointers + 
                            " -> " + nextNumPointers);
                    for (int i = 0; i < nextNumPointers; i++) {
                        int ioff = i * MotionEvent.NUM_SAMPLE_DATA;
                        Slog.d("InputDevice", "Pointer " + i + " (" + 
                                mNextData[ioff + MotionEvent.SAMPLE_X] + ", " +
                                mNextData[ioff + MotionEvent.SAMPLE_Y] + ")");
                    }
                }
                if (lastNumPointers == 1 && nextNumPointers == 2
                        && mJumpyPointsDropped < JUMPY_TRANSITION_DROPS) {
                    mNextNumPointers = 1;
                    mJumpyPointsDropped++;
                } else if (lastNumPointers == 2 && nextNumPointers == 1
                        && mJumpyPointsDropped < JUMPY_TRANSITION_DROPS) {
                    System.arraycopy(lastData, 0, nextData, 0, 
                            lastNumPointers * MotionEvent.NUM_SAMPLE_DATA);
                    mNextNumPointers = lastNumPointers;
                    mJumpyPointsDropped++;
                    if (DEBUG_HACKS) {
                        for (int i = 0; i < mNextNumPointers; i++) {
                            int ioff = i * MotionEvent.NUM_SAMPLE_DATA;
                            Slog.d("InputDevice", "Pointer " + i + " replaced (" + 
                                    mNextData[ioff + MotionEvent.SAMPLE_X] + ", " +
                                    mNextData[ioff + MotionEvent.SAMPLE_Y] + ")");
                        }
                    }
                } else {
                    mJumpyPointsDropped = 0;
                    if (DEBUG_HACKS) {
                        Slog.d("InputDevice", "Transition - drop limit reset");
                    }
                }
                return;
            }
            if (nextNumPointers < 2) {
                return;
            }
            int badPointerIndex = -1;
            int badPointerReplaceXWith = 0;
            int badPointerReplaceYWith = 0;
            int badPointerDistance = Integer.MIN_VALUE;
            for (int i = nextNumPointers - 1; i >= 0; i--) {
                boolean dropx = false;
                boolean dropy = false;
                if (mJumpyPointsDropped < JUMPY_DROP_LIMIT) {
                    final int ioff = i * MotionEvent.NUM_SAMPLE_DATA;
                    final int x = nextData[ioff + MotionEvent.SAMPLE_X];
                    final int y = nextData[ioff + MotionEvent.SAMPLE_Y];
                    if (DEBUG_HACKS) {
                        Slog.d("InputDevice", "Point " + i + " (" + x + ", " + y + ")");
                    }
                    for (int j = 0; j < nextNumPointers && !dropx && !dropy; j++) {
                        if (j == i) {
                            continue;
                        }
                        final int joff = j * MotionEvent.NUM_SAMPLE_DATA;
                        final int xOther = nextData[joff + MotionEvent.SAMPLE_X];
                        final int yOther = nextData[joff + MotionEvent.SAMPLE_Y];
                        dropx = Math.abs(x - xOther) <= jumpyEpsilon;
                        dropy = Math.abs(y - yOther) <= jumpyEpsilon;
                    }
                    if (dropx) {
                        int xreplace = lastData[MotionEvent.SAMPLE_X];
                        int yreplace = lastData[MotionEvent.SAMPLE_Y];
                        int distance = Math.abs(yreplace - y);
                        for (int j = 1; j < lastNumPointers; j++) {
                            final int joff = j * MotionEvent.NUM_SAMPLE_DATA;
                            int lasty = lastData[joff + MotionEvent.SAMPLE_Y];   
                            int currDist = Math.abs(lasty - y);
                            if (currDist < distance) {
                                xreplace = lastData[joff + MotionEvent.SAMPLE_X];
                                yreplace = lasty;
                                distance = currDist;
                            }
                        }
                        int badXDelta = Math.abs(xreplace - x);
                        if (badXDelta > badPointerDistance) {
                            badPointerDistance = badXDelta;
                            badPointerIndex = i;
                            badPointerReplaceXWith = xreplace;
                            badPointerReplaceYWith = yreplace;
                        }
                    } else if (dropy) {
                        int xreplace = lastData[MotionEvent.SAMPLE_X];
                        int yreplace = lastData[MotionEvent.SAMPLE_Y];
                        int distance = Math.abs(xreplace - x);
                        for (int j = 1; j < lastNumPointers; j++) {
                            final int joff = j * MotionEvent.NUM_SAMPLE_DATA;
                            int lastx = lastData[joff + MotionEvent.SAMPLE_X];   
                            int currDist = Math.abs(lastx - x);
                            if (currDist < distance) {
                                xreplace = lastx;
                                yreplace = lastData[joff + MotionEvent.SAMPLE_Y];
                                distance = currDist;
                            }
                        }
                        int badYDelta = Math.abs(yreplace - y);
                        if (badYDelta > badPointerDistance) {
                            badPointerDistance = badYDelta;
                            badPointerIndex = i;
                            badPointerReplaceXWith = xreplace;
                            badPointerReplaceYWith = yreplace;
                        }
                    }
                }
            }
            if (badPointerIndex >= 0) {
                if (DEBUG_HACKS) {
                    Slog.d("InputDevice", "Replacing bad pointer " + badPointerIndex +
                            " with (" + badPointerReplaceXWith + ", " + badPointerReplaceYWith +
                            ")");
                }
                final int offset = badPointerIndex * MotionEvent.NUM_SAMPLE_DATA;
                nextData[offset + MotionEvent.SAMPLE_X] = badPointerReplaceXWith;
                nextData[offset + MotionEvent.SAMPLE_Y] = badPointerReplaceYWith;
                mJumpyPointsDropped++;
            } else {
                mJumpyPointsDropped = 0;
            }
        }
        int[] generateAveragedData(int upOrDownPointer, int lastNumPointers,
                int nextNumPointers) {
            final int numPointers = mLastNumPointers;
            final int[] rawData = mLastData;
            if (DEBUG_HACKS) Slog.v("InputDevice", "lastNumPointers=" + lastNumPointers
                    + " nextNumPointers=" + nextNumPointers
                    + " numPointers=" + numPointers);
            for (int i=0; i<numPointers; i++) {
                final int ioff = i * MotionEvent.NUM_SAMPLE_DATA;
                final int p = mPointerIds[i];
                final int poff = p * MotionEvent.NUM_SAMPLE_DATA * HISTORY_SIZE;
                if (i == upOrDownPointer && lastNumPointers != nextNumPointers) {
                    if (lastNumPointers < nextNumPointers) {
                        if (DEBUG_HACKS) Slog.v("InputDevice", "Pointer down @ index "
                                + upOrDownPointer + " id " + mPointerIds[i]);
                        mHistoryDataStart[i] = 0;
                        mHistoryDataEnd[i] = 0;
                        System.arraycopy(rawData, ioff, mHistoryData, poff,
                                MotionEvent.NUM_SAMPLE_DATA);
                        System.arraycopy(rawData, ioff, mAveragedData, ioff,
                                MotionEvent.NUM_SAMPLE_DATA);
                        continue;
                    } else {
                        if (DEBUG_HACKS) Slog.v("InputDevice", "Pointer up @ index "
                                + upOrDownPointer + " id " + mPointerIds[i]);
                    }
                } else {
                    int end = mHistoryDataEnd[i];
                    int eoff = poff + (end*MotionEvent.NUM_SAMPLE_DATA);
                    int oldX = mHistoryData[eoff + MotionEvent.SAMPLE_X];
                    int oldY = mHistoryData[eoff + MotionEvent.SAMPLE_Y];
                    int newX = rawData[ioff + MotionEvent.SAMPLE_X];
                    int newY = rawData[ioff + MotionEvent.SAMPLE_Y];
                    int dx = newX-oldX;
                    int dy = newY-oldY;
                    int delta = dx*dx + dy*dy;
                    if (DEBUG_HACKS) Slog.v("InputDevice", "Delta from last: " + delta);
                    if (delta >= (75*75)) {
                        mHistoryDataStart[i] = 0;
                        mHistoryDataEnd[i] = 0;
                        System.arraycopy(rawData, ioff, mHistoryData, poff,
                                MotionEvent.NUM_SAMPLE_DATA);
                        System.arraycopy(rawData, ioff, mAveragedData, ioff,
                                MotionEvent.NUM_SAMPLE_DATA);
                        continue;
                    } else {
                        end++;
                        if (end >= HISTORY_SIZE) {
                            end -= HISTORY_SIZE;
                        }
                        mHistoryDataEnd[i] = end;
                        int noff = poff + (end*MotionEvent.NUM_SAMPLE_DATA);
                        mHistoryData[noff + MotionEvent.SAMPLE_X] = newX;
                        mHistoryData[noff + MotionEvent.SAMPLE_Y] = newY;
                        mHistoryData[noff + MotionEvent.SAMPLE_PRESSURE]
                                = rawData[ioff + MotionEvent.SAMPLE_PRESSURE];
                        int start = mHistoryDataStart[i];
                        if (end == start) {
                            start++;
                            if (start >= HISTORY_SIZE) {
                                start -= HISTORY_SIZE;
                            }
                            mHistoryDataStart[i] = start;
                        }
                    }
                }
                int start = mHistoryDataStart[i];
                int end = mHistoryDataEnd[i];
                int x=0, y=0;
                int totalPressure = 0;
                while (start != end) {
                    int soff = poff + (start*MotionEvent.NUM_SAMPLE_DATA);
                    int pressure = mHistoryData[soff + MotionEvent.SAMPLE_PRESSURE];
                    if (pressure <= 0) pressure = 1;
                    x += mHistoryData[soff + MotionEvent.SAMPLE_X] * pressure;
                    y += mHistoryData[soff + MotionEvent.SAMPLE_Y] * pressure;
                    totalPressure += pressure;
                    start++;
                    if (start >= HISTORY_SIZE) start = 0;
                }
                int eoff = poff + (end*MotionEvent.NUM_SAMPLE_DATA);
                int pressure = mHistoryData[eoff + MotionEvent.SAMPLE_PRESSURE];
                if (pressure <= 0) pressure = 1;
                x += mHistoryData[eoff + MotionEvent.SAMPLE_X] * pressure;
                y += mHistoryData[eoff + MotionEvent.SAMPLE_Y] * pressure;
                totalPressure += pressure;
                x /= totalPressure;
                y /= totalPressure;
                if (DEBUG_HACKS) Slog.v("InputDevice", "Averaging " + totalPressure
                        + " weight: (" + x + "," + y + ")");
                mAveragedData[ioff + MotionEvent.SAMPLE_X] = x;
                mAveragedData[ioff + MotionEvent.SAMPLE_Y] = y;
                mAveragedData[ioff + MotionEvent.SAMPLE_PRESSURE] =
                        rawData[ioff + MotionEvent.SAMPLE_PRESSURE];
                mAveragedData[ioff + MotionEvent.SAMPLE_SIZE] =
                        rawData[ioff + MotionEvent.SAMPLE_SIZE];
            }
            return mAveragedData;
        }
        private boolean assignPointer(int nextIndex, boolean allowOverlap) {
            final int lastNumPointers = mLastNumPointers;
            final int[] next2Last = mNext2Last;
            final long[] next2LastDistance = mNext2LastDistance;
            final int[] last2Next = mLast2Next;
            final int[] lastData = mLastData;
            final int[] nextData = mNextData;
            final int id = nextIndex * MotionEvent.NUM_SAMPLE_DATA;
            if (DEBUG_POINTERS) Slog.v("InputDevice", "assignPointer: nextIndex="
                    + nextIndex + " dataOff=" + id);
            final int x1 = nextData[id + MotionEvent.SAMPLE_X];
            final int y1 = nextData[id + MotionEvent.SAMPLE_Y];
            long bestDistance = -1;
            int bestIndex = -1;
            for (int j=0; j<lastNumPointers; j++) {
                if (!allowOverlap && last2Next[j] < -1) {
                    continue;
                }
                final int jd = j * MotionEvent.NUM_SAMPLE_DATA;
                final int xd = lastData[jd + MotionEvent.SAMPLE_X] - x1;
                final int yd = lastData[jd + MotionEvent.SAMPLE_Y] - y1;
                final long distance = xd*(long)xd + yd*(long)yd;
                if (bestDistance == -1 || distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = j;
                }
            }
            if (DEBUG_POINTERS) Slog.v("InputDevice", "New index " + nextIndex
                    + " best old index=" + bestIndex + " (distance="
                    + bestDistance + ")");
            next2Last[nextIndex] = bestIndex;
            next2LastDistance[nextIndex] = bestDistance;
            if (bestIndex < 0) {
                return true;
            }
            if (last2Next[bestIndex] == -1) {
                last2Next[bestIndex] = nextIndex;
                return false;
            }
            if (DEBUG_POINTERS) Slog.v("InputDevice", "Old index " + bestIndex
                    + " has multiple best new pointers!");
            last2Next[bestIndex] = -2;
            return true;
        }
        private int updatePointerIdentifiers() {
            final int[] lastData = mLastData;
            final int[] nextData = mNextData;
            final int nextNumPointers = mNextNumPointers;
            final int lastNumPointers = mLastNumPointers;
            if (nextNumPointers == 1 && lastNumPointers == 1) {
                System.arraycopy(nextData, 0, lastData, 0,
                        MotionEvent.NUM_SAMPLE_DATA);
                return -1;
            }
            final int[] last2Next = mLast2Next;
            for (int i=0; i<lastNumPointers; i++) {
                last2Next[i] = -1;
            }
            if (DEBUG_POINTERS) Slog.v("InputDevice",
                    "Update pointers: lastNumPointers=" + lastNumPointers
                    + " nextNumPointers=" + nextNumPointers);
            final int[] next2Last = mNext2Last;
            final long[] next2LastDistance = mNext2LastDistance;
            boolean conflicts = false;
            for (int i=0; i<nextNumPointers; i++) {
                conflicts |= assignPointer(i, true);
            }
            if (conflicts) {
                if (DEBUG_POINTERS) Slog.v("InputDevice", "Resolving conflicts");
                for (int i=0; i<lastNumPointers; i++) {
                    if (last2Next[i] != -2) {
                        continue;
                    }
                    if (DEBUG_POINTERS) Slog.v("InputDevice",
                            "Resolving last index #" + i);
                    int numFound;
                    do {
                        numFound = 0;
                        long worstDistance = 0;
                        int worstJ = -1;
                        for (int j=0; j<nextNumPointers; j++) {
                            if (next2Last[j] != i) {
                                continue;
                            }
                            numFound++;
                            if (worstDistance < next2LastDistance[j]) {
                                worstDistance = next2LastDistance[j];
                                worstJ = j;
                            }
                        }
                        if (worstJ >= 0) {
                            if (DEBUG_POINTERS) Slog.v("InputDevice",
                                    "Worst new pointer: " + worstJ
                                    + " (distance=" + worstDistance + ")");
                            if (assignPointer(worstJ, false)) {
                                next2Last[worstJ] = -1;
                            }
                        }
                    } while (numFound > 2);
                }
            }
            int retIndex = -1;
            if (lastNumPointers < nextNumPointers) {
                if (DEBUG_POINTERS) Slog.v("InputDevice", "Adding new pointer");
                int nextId = 0;
                int i=0;
                while (i < lastNumPointers) {
                    if (mPointerIds[i] > nextId) {
                        if (DEBUG_POINTERS) Slog.v("InputDevice",
                                "Inserting new pointer at hole " + i);
                        System.arraycopy(mPointerIds, i, mPointerIds,
                                i+1, lastNumPointers-i);
                        System.arraycopy(lastData, i*MotionEvent.NUM_SAMPLE_DATA,
                                lastData, (i+1)*MotionEvent.NUM_SAMPLE_DATA,
                                (lastNumPointers-i)*MotionEvent.NUM_SAMPLE_DATA);
                        System.arraycopy(next2Last, i, next2Last,
                                i+1, lastNumPointers-i);
                        break;
                    }
                    i++;
                    nextId++;
                }
                if (DEBUG_POINTERS) Slog.v("InputDevice",
                        "New pointer id " + nextId + " at index " + i);
                mLastNumPointers++;
                retIndex = i;
                mPointerIds[i] = nextId;
                for (int j=0; j<nextNumPointers; j++) {
                    if (next2Last[j] < 0) {
                        if (DEBUG_POINTERS) Slog.v("InputDevice",
                                "Assigning new id to new pointer index " + j);
                        next2Last[j] = i;
                        break;
                    }
                }
            }
            for (int i=0; i<nextNumPointers; i++) {
                int lastIndex = next2Last[i];
                if (lastIndex >= 0) {
                    if (DEBUG_POINTERS) Slog.v("InputDevice",
                            "Copying next pointer index " + i
                            + " to last index " + lastIndex);
                    System.arraycopy(nextData, i*MotionEvent.NUM_SAMPLE_DATA,
                            lastData, lastIndex*MotionEvent.NUM_SAMPLE_DATA,
                            MotionEvent.NUM_SAMPLE_DATA);
                }
            }
            if (lastNumPointers > nextNumPointers) {
                if (DEBUG_POINTERS) Slog.v("InputDevice", "Removing old pointer");
                for (int i=0; i<lastNumPointers; i++) {
                    if (last2Next[i] == -1) {
                        if (DEBUG_POINTERS) Slog.v("InputDevice",
                                "Removing old pointer at index " + i);
                        retIndex = i;
                        break;
                    }
                }
            }
            return retIndex;
        }
        void removeOldPointer(int index) {
            final int lastNumPointers = mLastNumPointers;
            if (index >= 0 && index < lastNumPointers) {
                System.arraycopy(mPointerIds, index+1, mPointerIds,
                        index, lastNumPointers-index-1);
                System.arraycopy(mLastData, (index+1)*MotionEvent.NUM_SAMPLE_DATA,
                        mLastData, (index)*MotionEvent.NUM_SAMPLE_DATA,
                        (lastNumPointers-index-1)*MotionEvent.NUM_SAMPLE_DATA);
                mLastNumPointers--;
            }
        }
        MotionEvent generateAbsMotion(InputDevice device, long curTime,
                long curTimeNano, Display display, int orientation,
                int metaState) {
            if (mSkipLastPointers) {
                mSkipLastPointers = false;
                mLastNumPointers = 0;
            }
            if (mNextNumPointers <= 0 && mLastNumPointers <= 0) {
                return null;
            }
            final int lastNumPointers = mLastNumPointers;
            final int nextNumPointers = mNextNumPointers;
            if (mNextNumPointers > MAX_POINTERS) {
                Slog.w("InputDevice", "Number of pointers " + mNextNumPointers
                        + " exceeded maximum of " + MAX_POINTERS);
                mNextNumPointers = MAX_POINTERS;
            }
            int upOrDownPointer = updatePointerIdentifiers();
            final float[] reportData = mReportData;
            final int[] rawData;
            if (KeyInputQueue.BAD_TOUCH_HACK) {
                rawData = generateAveragedData(upOrDownPointer, lastNumPointers,
                        nextNumPointers);
            } else {
                rawData = mLastData;
            }
            final int numPointers = mLastNumPointers;
            if (DEBUG_POINTERS) Slog.v("InputDevice", "Processing "
                    + numPointers + " pointers (going from " + lastNumPointers
                    + " to " + nextNumPointers + ")");
            for (int i=0; i<numPointers; i++) {
                final int pos = i * MotionEvent.NUM_SAMPLE_DATA;
                reportData[pos + MotionEvent.SAMPLE_X] = rawData[pos + MotionEvent.SAMPLE_X];
                reportData[pos + MotionEvent.SAMPLE_Y] = rawData[pos + MotionEvent.SAMPLE_Y];
                reportData[pos + MotionEvent.SAMPLE_PRESSURE] = rawData[pos + MotionEvent.SAMPLE_PRESSURE];
                reportData[pos + MotionEvent.SAMPLE_SIZE] = rawData[pos + MotionEvent.SAMPLE_SIZE];
            }
            int action;
            int edgeFlags = 0;
            if (nextNumPointers != lastNumPointers) {
                if (nextNumPointers > lastNumPointers) {
                    if (lastNumPointers == 0) {
                        action = MotionEvent.ACTION_DOWN;
                        mDownTime = curTime;
                    } else {
                        action = MotionEvent.ACTION_POINTER_DOWN
                                | (upOrDownPointer << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
                    }
                } else {
                    if (numPointers == 1) {
                        action = MotionEvent.ACTION_UP;
                    } else {
                        action = MotionEvent.ACTION_POINTER_UP
                                | (upOrDownPointer << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
                    }
                }
                currentMove = null;
            } else {
                action = MotionEvent.ACTION_MOVE;
            }
            final int dispW = display.getWidth()-1;
            final int dispH = display.getHeight()-1;
            int w = dispW;
            int h = dispH;
            if (orientation == Surface.ROTATION_90
                    || orientation == Surface.ROTATION_270) {
                int tmp = w;
                w = h;
                h = tmp;
            }
            final AbsoluteInfo absX = device.absX;
            final AbsoluteInfo absY = device.absY;
            final AbsoluteInfo absPressure = device.absPressure;
            final AbsoluteInfo absSize = device.absSize;
            for (int i=0; i<numPointers; i++) {
                final int j = i * MotionEvent.NUM_SAMPLE_DATA;
                if (absX != null) {
                    reportData[j + MotionEvent.SAMPLE_X] =
                            ((reportData[j + MotionEvent.SAMPLE_X]-absX.minValue)
                                / absX.range) * w;
                }
                if (absY != null) {
                    reportData[j + MotionEvent.SAMPLE_Y] =
                            ((reportData[j + MotionEvent.SAMPLE_Y]-absY.minValue)
                                / absY.range) * h;
                }
                if (absPressure != null) {
                    reportData[j + MotionEvent.SAMPLE_PRESSURE] = 
                            ((reportData[j + MotionEvent.SAMPLE_PRESSURE]-absPressure.minValue)
                                / (float)absPressure.range);
                }
                if (absSize != null) {
                    reportData[j + MotionEvent.SAMPLE_SIZE] = 
                            ((reportData[j + MotionEvent.SAMPLE_SIZE]-absSize.minValue)
                                / (float)absSize.range);
                }
                switch (orientation) {
                    case Surface.ROTATION_90: {
                        final float temp = reportData[j + MotionEvent.SAMPLE_X];
                        reportData[j + MotionEvent.SAMPLE_X] = reportData[j + MotionEvent.SAMPLE_Y];
                        reportData[j + MotionEvent.SAMPLE_Y] = w-temp;
                        break;
                    }
                    case Surface.ROTATION_180: {
                        reportData[j + MotionEvent.SAMPLE_X] = w-reportData[j + MotionEvent.SAMPLE_X];
                        reportData[j + MotionEvent.SAMPLE_Y] = h-reportData[j + MotionEvent.SAMPLE_Y];
                        break;
                    }
                    case Surface.ROTATION_270: {
                        final float temp = reportData[j + MotionEvent.SAMPLE_X];
                        reportData[j + MotionEvent.SAMPLE_X] = h-reportData[j + MotionEvent.SAMPLE_Y];
                        reportData[j + MotionEvent.SAMPLE_Y] = temp;
                        break;
                    }
                }
            }
            if (action == MotionEvent.ACTION_DOWN) {
                if (reportData[MotionEvent.SAMPLE_X] <= 0) {
                    edgeFlags |= MotionEvent.EDGE_LEFT;
                } else if (reportData[MotionEvent.SAMPLE_X] >= dispW) {
                    edgeFlags |= MotionEvent.EDGE_RIGHT;
                }
                if (reportData[MotionEvent.SAMPLE_Y] <= 0) {
                    edgeFlags |= MotionEvent.EDGE_TOP;
                } else if (reportData[MotionEvent.SAMPLE_Y] >= dispH) {
                    edgeFlags |= MotionEvent.EDGE_BOTTOM;
                }
            }
            if (currentMove != null) {
                if (false) Slog.i("InputDevice", "Adding batch x="
                        + reportData[MotionEvent.SAMPLE_X]
                        + " y=" + reportData[MotionEvent.SAMPLE_Y]
                        + " to " + currentMove);
                currentMove.addBatch(curTime, reportData, metaState);
                if (WindowManagerPolicy.WATCH_POINTER) {
                    Slog.i("KeyInputQueue", "Updating: " + currentMove);
                }
                return null;
            }
            MotionEvent me = MotionEvent.obtainNano(mDownTime, curTime,
                    curTimeNano, action, numPointers, mPointerIds, reportData,
                    metaState, xPrecision, yPrecision, device.id, edgeFlags);
            if (action == MotionEvent.ACTION_MOVE) {
                currentMove = me;
            }
            if (nextNumPointers < lastNumPointers) {
                removeOldPointer(upOrDownPointer);
            }
            return me;
        }
        boolean hasMore() {
            return mLastNumPointers != mNextNumPointers;
        }
        void finish() {
            mNextNumPointers = mAddingPointerOffset = 0;
            mNextData[MotionEvent.SAMPLE_PRESSURE] = 0;
        }
        MotionEvent generateRelMotion(InputDevice device, long curTime,
                long curTimeNano, int orientation, int metaState) {
            final float[] scaled = mReportData;
            scaled[MotionEvent.SAMPLE_X] = mNextData[MotionEvent.SAMPLE_X];
            scaled[MotionEvent.SAMPLE_Y] = mNextData[MotionEvent.SAMPLE_Y];
            scaled[MotionEvent.SAMPLE_PRESSURE] = 1.0f;
            scaled[MotionEvent.SAMPLE_SIZE] = 0;
            int edgeFlags = 0;
            int action;
            if (mNextNumPointers != mLastNumPointers) {
                mNextData[MotionEvent.SAMPLE_X] =
                        mNextData[MotionEvent.SAMPLE_Y] = 0;
                if (mNextNumPointers > 0 && mLastNumPointers == 0) {
                    action = MotionEvent.ACTION_DOWN;
                    mDownTime = curTime;
                } else if (mNextNumPointers == 0) {
                    action = MotionEvent.ACTION_UP;
                } else {
                    action = MotionEvent.ACTION_MOVE;
                }
                mLastNumPointers = mNextNumPointers;
                currentMove = null;
            } else {
                action = MotionEvent.ACTION_MOVE;
            }
            scaled[MotionEvent.SAMPLE_X] *= xMoveScale;
            scaled[MotionEvent.SAMPLE_Y] *= yMoveScale;
            switch (orientation) {
                case Surface.ROTATION_90: {
                    final float temp = scaled[MotionEvent.SAMPLE_X];
                    scaled[MotionEvent.SAMPLE_X] = scaled[MotionEvent.SAMPLE_Y];
                    scaled[MotionEvent.SAMPLE_Y] = -temp;
                    break;
                }
                case Surface.ROTATION_180: {
                    scaled[MotionEvent.SAMPLE_X] = -scaled[MotionEvent.SAMPLE_X];
                    scaled[MotionEvent.SAMPLE_Y] = -scaled[MotionEvent.SAMPLE_Y];
                    break;
                }
                case Surface.ROTATION_270: {
                    final float temp = scaled[MotionEvent.SAMPLE_X];
                    scaled[MotionEvent.SAMPLE_X] = -scaled[MotionEvent.SAMPLE_Y];
                    scaled[MotionEvent.SAMPLE_Y] = temp;
                    break;
                }
            }
            if (currentMove != null) {
                if (false) Slog.i("InputDevice", "Adding batch x="
                        + scaled[MotionEvent.SAMPLE_X]
                        + " y=" + scaled[MotionEvent.SAMPLE_Y]
                        + " to " + currentMove);
                currentMove.addBatch(curTime, scaled, metaState);
                if (WindowManagerPolicy.WATCH_POINTER) {
                    Slog.i("KeyInputQueue", "Updating: " + currentMove);
                }
                return null;
            }
            MotionEvent me = MotionEvent.obtainNano(mDownTime, curTime,
                    curTimeNano, action, 1, mPointerIds, scaled, metaState,
                    xPrecision, yPrecision, device.id, edgeFlags);
            if (action == MotionEvent.ACTION_MOVE) {
                currentMove = me;
            }
            return me;
        }
    }
    static class AbsoluteInfo {
        int minValue;
        int maxValue;
        int range;
        int flat;
        int fuzz;
        final void dump(PrintWriter pw) {
            pw.print("minValue="); pw.print(minValue);
            pw.print(" maxValue="); pw.print(maxValue);
            pw.print(" range="); pw.print(range);
            pw.print(" flat="); pw.print(flat);
            pw.print(" fuzz="); pw.print(fuzz);
        }
    };
    InputDevice(int _id, int _classes, String _name,
            AbsoluteInfo _absX, AbsoluteInfo _absY,
            AbsoluteInfo _absPressure, AbsoluteInfo _absSize) {
        id = _id;
        classes = _classes;
        name = _name;
        absX = _absX;
        absY = _absY;
        absPressure = _absPressure;
        absSize = _absSize;
    }
};
