public final class BidiLine {
    static void setTrailingWSStart(BidiBase bidiBase)
    {
        byte[] dirProps = bidiBase.dirProps;
        byte[] levels = bidiBase.levels;
        int start = bidiBase.length;
        byte paraLevel = bidiBase.paraLevel;
        if (BidiBase.NoContextRTL(dirProps[start - 1]) == BidiBase.B) {
            bidiBase.trailingWSStart = start;   
            return;
        }
        while (start > 0 &&
                (BidiBase.DirPropFlagNC(dirProps[start - 1]) & BidiBase.MASK_WS) != 0) {
            --start;
        }
        while (start > 0 && levels[start - 1] == paraLevel) {
            --start;
        }
        bidiBase.trailingWSStart=start;
    }
    public static Bidi setLine(Bidi bidi, BidiBase paraBidi,
                               Bidi newBidi, BidiBase newBidiBase,
                               int start, int limit) {
        int length;
        BidiBase lineBidi = newBidiBase;
        length = lineBidi.length = lineBidi.originalLength =
                lineBidi.resultLength = limit - start;
        lineBidi.text = new char[length];
        System.arraycopy(paraBidi.text, start, lineBidi.text, 0, length);
        lineBidi.paraLevel = paraBidi.GetParaLevelAt(start);
        lineBidi.paraCount = paraBidi.paraCount;
        lineBidi.runs = new BidiRun[0];
        if (paraBidi.controlCount > 0) {
            int j;
            for (j = start; j < limit; j++) {
                if (BidiBase.IsBidiControlChar(paraBidi.text[j])) {
                    lineBidi.controlCount++;
                }
            }
            lineBidi.resultLength -= lineBidi.controlCount;
        }
        lineBidi.getDirPropsMemory(length);
        lineBidi.dirProps = lineBidi.dirPropsMemory;
        System.arraycopy(paraBidi.dirProps, start, lineBidi.dirProps, 0,
                         length);
        lineBidi.getLevelsMemory(length);
        lineBidi.levels = lineBidi.levelsMemory;
        System.arraycopy(paraBidi.levels, start, lineBidi.levels, 0,
                         length);
        lineBidi.runCount = -1;
        if (paraBidi.direction != BidiBase.MIXED) {
            lineBidi.direction = paraBidi.direction;
            if (paraBidi.trailingWSStart <= start) {
                lineBidi.trailingWSStart = 0;
            } else if (paraBidi.trailingWSStart < limit) {
                lineBidi.trailingWSStart = paraBidi.trailingWSStart - start;
            } else {
                lineBidi.trailingWSStart = length;
            }
        } else {
            byte[] levels = lineBidi.levels;
            int i, trailingWSStart;
            byte level;
            setTrailingWSStart(lineBidi);
            trailingWSStart = lineBidi.trailingWSStart;
            if (trailingWSStart == 0) {
                lineBidi.direction = (byte)(lineBidi.paraLevel & 1);
            } else {
                level = (byte)(levels[0] & 1);
                if (trailingWSStart < length &&
                    (lineBidi.paraLevel & 1) != level) {
                    lineBidi.direction = BidiBase.MIXED;
                } else {
                    for (i = 1; ; i++) {
                        if (i == trailingWSStart) {
                            lineBidi.direction = level;
                            break;
                        } else if ((levels[i] & 1) != level) {
                            lineBidi.direction = BidiBase.MIXED;
                            break;
                        }
                    }
                }
            }
            switch(lineBidi.direction) {
                case Bidi.DIRECTION_LEFT_TO_RIGHT:
                    lineBidi.paraLevel = (byte)
                        ((lineBidi.paraLevel + 1) & ~1);
                    lineBidi.trailingWSStart = 0;
                    break;
                case Bidi.DIRECTION_RIGHT_TO_LEFT:
                    lineBidi.paraLevel |= 1;
                    lineBidi.trailingWSStart = 0;
                    break;
                default:
                    break;
            }
        }
        newBidiBase.paraBidi = paraBidi; 
        return newBidi;
    }
    static byte getLevelAt(BidiBase bidiBase, int charIndex)
    {
        if (bidiBase.direction != BidiBase.MIXED || charIndex >= bidiBase.trailingWSStart) {
            return bidiBase.GetParaLevelAt(charIndex);
        } else {
            return bidiBase.levels[charIndex];
        }
    }
    static byte[] getLevels(BidiBase bidiBase)
    {
        int start = bidiBase.trailingWSStart;
        int length = bidiBase.length;
        if (start != length) {
            Arrays.fill(bidiBase.levels, start, length, bidiBase.paraLevel);
            bidiBase.trailingWSStart = length;
        }
        if (length < bidiBase.levels.length) {
            byte[] levels = new byte[length];
            System.arraycopy(bidiBase.levels, 0, levels, 0, length);
            return levels;
        }
        return bidiBase.levels;
    }
    static BidiRun getLogicalRun(BidiBase bidiBase, int logicalPosition)
    {
        BidiRun newRun = new BidiRun(), iRun;
        getRuns(bidiBase);
        int runCount = bidiBase.runCount;
        int visualStart = 0, logicalLimit = 0;
        iRun = bidiBase.runs[0];
        for (int i = 0; i < runCount; i++) {
            iRun = bidiBase.runs[i];
            logicalLimit = iRun.start + iRun.limit - visualStart;
            if ((logicalPosition >= iRun.start) &&
                (logicalPosition < logicalLimit)) {
                break;
            }
            visualStart = iRun.limit;
        }
        newRun.start = iRun.start;
        newRun.limit = logicalLimit;
        newRun.level = iRun.level;
        return newRun;
    }
    private static void getSingleRun(BidiBase bidiBase, byte level) {
        bidiBase.runs = bidiBase.simpleRuns;
        bidiBase.runCount = 1;
        bidiBase.runs[0] = new BidiRun(0, bidiBase.length, level);
    }
    private static void reorderLine(BidiBase bidiBase, byte minLevel, byte maxLevel) {
        if (maxLevel<=(minLevel|1)) {
            return;
        }
        BidiRun[] runs;
        BidiRun tempRun;
        byte[] levels;
        int firstRun, endRun, limitRun, runCount;
        ++minLevel;
        runs = bidiBase.runs;
        levels = bidiBase.levels;
        runCount = bidiBase.runCount;
        if (bidiBase.trailingWSStart < bidiBase.length) {
            --runCount;
        }
        while (--maxLevel >= minLevel) {
            firstRun = 0;
            for ( ; ; ) {
                while (firstRun < runCount && levels[runs[firstRun].start] < maxLevel) {
                    ++firstRun;
                }
                if (firstRun >= runCount) {
                    break;  
                }
                for (limitRun = firstRun; ++limitRun < runCount &&
                      levels[runs[limitRun].start]>=maxLevel; ) {}
                endRun = limitRun - 1;
                while (firstRun < endRun) {
                    tempRun = runs[firstRun];
                    runs[firstRun] = runs[endRun];
                    runs[endRun] = tempRun;
                    ++firstRun;
                    --endRun;
                }
                if (limitRun == runCount) {
                    break;  
                } else {
                    firstRun = limitRun + 1;
                }
            }
        }
        if ((minLevel & 1) == 0) {
            firstRun = 0;
            if (bidiBase.trailingWSStart == bidiBase.length) {
                --runCount;
            }
            while (firstRun < runCount) {
                tempRun = runs[firstRun];
                runs[firstRun] = runs[runCount];
                runs[runCount] = tempRun;
                ++firstRun;
                --runCount;
            }
        }
    }
    static int getRunFromLogicalIndex(BidiBase bidiBase, int logicalIndex) {
        BidiRun[] runs = bidiBase.runs;
        int runCount = bidiBase.runCount, visualStart = 0, i, length, logicalStart;
        for (i = 0; i < runCount; i++) {
            length = runs[i].limit - visualStart;
            logicalStart = runs[i].start;
            if ((logicalIndex >= logicalStart) && (logicalIndex < (logicalStart+length))) {
                return i;
            }
            visualStart += length;
        }
        throw new IllegalStateException("Internal ICU error in getRunFromLogicalIndex");
    }
    static void getRuns(BidiBase bidiBase) {
        if (bidiBase.runCount >= 0) {
            return;
        }
        if (bidiBase.direction != BidiBase.MIXED) {
            getSingleRun(bidiBase, bidiBase.paraLevel);
        } else  {
            int length = bidiBase.length, limit;
            byte[] levels = bidiBase.levels;
            int i, runCount;
            byte level = BidiBase.INTERNAL_LEVEL_DEFAULT_LTR;   
            limit = bidiBase.trailingWSStart;
            runCount = 0;
            for (i = 0; i < limit; ++i) {
                if (levels[i] != level) {
                    ++runCount;
                    level = levels[i];
                }
            }
            if (runCount == 1 && limit == length) {
                getSingleRun(bidiBase, levels[0]);
            } else  {
                BidiRun[] runs;
                int runIndex, start;
                byte minLevel = BidiBase.MAX_EXPLICIT_LEVEL + 1;
                byte maxLevel=0;
                if (limit < length) {
                    ++runCount;
                }
                bidiBase.getRunsMemory(runCount);
                runs = bidiBase.runsMemory;
                runIndex = 0;
                i = 0;
                do {
                    start = i;
                    level = levels[i];
                    if (level < minLevel) {
                        minLevel = level;
                    }
                    if (level > maxLevel) {
                        maxLevel = level;
                    }
                    while (++i < limit && levels[i] == level) {}
                    runs[runIndex] = new BidiRun(start, i - start, level);
                    ++runIndex;
                } while (i < limit);
                if (limit < length) {
                    runs[runIndex] = new BidiRun(limit, length - limit, bidiBase.paraLevel);
                    if (bidiBase.paraLevel < minLevel) {
                        minLevel = bidiBase.paraLevel;
                    }
                }
                bidiBase.runs = runs;
                bidiBase.runCount = runCount;
                reorderLine(bidiBase, minLevel, maxLevel);
                limit = 0;
                for (i = 0; i < runCount; ++i) {
                    runs[i].level = levels[runs[i].start];
                    limit = (runs[i].limit += limit);
                }
                if (runIndex < runCount) {
                    int trailingRun = ((bidiBase.paraLevel & 1) != 0)? 0 : runIndex;
                    runs[trailingRun].level = bidiBase.paraLevel;
                }
            }
        }
        if (bidiBase.insertPoints.size > 0) {
            BidiBase.Point point;
            int runIndex, ip;
            for (ip = 0; ip < bidiBase.insertPoints.size; ip++) {
                point = bidiBase.insertPoints.points[ip];
                runIndex = getRunFromLogicalIndex(bidiBase, point.pos);
                bidiBase.runs[runIndex].insertRemove |= point.flag;
            }
        }
        if (bidiBase.controlCount > 0) {
            int runIndex, ic;
            char c;
            for (ic = 0; ic < bidiBase.length; ic++) {
                c = bidiBase.text[ic];
                if (BidiBase.IsBidiControlChar(c)) {
                    runIndex = getRunFromLogicalIndex(bidiBase, ic);
                    bidiBase.runs[runIndex].insertRemove--;
                }
            }
        }
    }
    static int[] prepareReorder(byte[] levels, byte[] pMinLevel, byte[] pMaxLevel)
    {
        int start;
        byte level, minLevel, maxLevel;
        if (levels == null || levels.length <= 0) {
            return null;
        }
        minLevel = BidiBase.MAX_EXPLICIT_LEVEL + 1;
        maxLevel = 0;
        for (start = levels.length; start>0; ) {
            level = levels[--start];
            if (level > BidiBase.MAX_EXPLICIT_LEVEL + 1) {
                return null;
            }
            if (level < minLevel) {
                minLevel = level;
            }
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
        pMinLevel[0] = minLevel;
        pMaxLevel[0] = maxLevel;
        int[] indexMap = new int[levels.length];
        for (start = levels.length; start > 0; ) {
            --start;
            indexMap[start] = start;
        }
        return indexMap;
    }
    static int[] reorderVisual(byte[] levels)
    {
        byte[] aMinLevel = new byte[1];
        byte[] aMaxLevel = new byte[1];
        int start, end, limit, temp;
        byte minLevel, maxLevel;
        int[] indexMap = prepareReorder(levels, aMinLevel, aMaxLevel);
        if (indexMap == null) {
            return null;
        }
        minLevel = aMinLevel[0];
        maxLevel = aMaxLevel[0];
        if (minLevel == maxLevel && (minLevel & 1) == 0) {
            return indexMap;
        }
        minLevel |= 1;
        do {
            start = 0;
            for ( ; ; ) {
                while (start < levels.length && levels[start] < maxLevel) {
                    ++start;
                }
                if (start >= levels.length) {
                    break;  
                }
                for (limit = start; ++limit < levels.length && levels[limit] >= maxLevel; ) {}
                end = limit - 1;
                while (start < end) {
                    temp = indexMap[start];
                    indexMap[start] = indexMap[end];
                    indexMap[end] = temp;
                    ++start;
                    --end;
                }
                if (limit == levels.length) {
                    break;  
                } else {
                    start = limit + 1;
                }
            }
        } while (--maxLevel >= minLevel);
        return indexMap;
    }
    static int[] getVisualMap(BidiBase bidiBase)
    {
        BidiRun[] runs = bidiBase.runs;
        int logicalStart, visualStart, visualLimit;
        int allocLength = bidiBase.length > bidiBase.resultLength ? bidiBase.length
                                                          : bidiBase.resultLength;
        int[] indexMap = new int[allocLength];
        visualStart = 0;
        int idx = 0;
        for (int j = 0; j < bidiBase.runCount; ++j) {
            logicalStart = runs[j].start;
            visualLimit = runs[j].limit;
            if (runs[j].isEvenRun()) {
                do { 
                    indexMap[idx++] = logicalStart++;
                } while (++visualStart < visualLimit);
            } else {
                logicalStart += visualLimit - visualStart;  
                do { 
                    indexMap[idx++] = --logicalStart;
                } while (++visualStart < visualLimit);
            }
        }
        if (bidiBase.insertPoints.size > 0) {
            int markFound = 0, runCount = bidiBase.runCount;
            int insertRemove, i, j, k;
            runs = bidiBase.runs;
            for (i = 0; i < runCount; i++) {
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & (BidiBase.LRM_BEFORE|BidiBase.RLM_BEFORE)) > 0) {
                    markFound++;
                }
                if ((insertRemove & (BidiBase.LRM_AFTER|BidiBase.RLM_AFTER)) > 0) {
                    markFound++;
                }
            }
            k = bidiBase.resultLength;
            for (i = runCount - 1; i >= 0 && markFound > 0; i--) {
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & (BidiBase.LRM_AFTER|BidiBase.RLM_AFTER)) > 0) {
                    indexMap[--k] = BidiBase.MAP_NOWHERE;
                    markFound--;
                }
                visualStart = i > 0 ? runs[i-1].limit : 0;
                for (j = runs[i].limit - 1; j >= visualStart && markFound > 0; j--) {
                    indexMap[--k] = indexMap[j];
                }
                if ((insertRemove & (BidiBase.LRM_BEFORE|BidiBase.RLM_BEFORE)) > 0) {
                    indexMap[--k] = BidiBase.MAP_NOWHERE;
                    markFound--;
                }
            }
        }
        else if (bidiBase.controlCount > 0) {
            int runCount = bidiBase.runCount, logicalEnd;
            int insertRemove, length, i, j, k, m;
            char uchar;
            boolean evenRun;
            runs = bidiBase.runs;
            visualStart = 0;
            k = 0;
            for (i = 0; i < runCount; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if ((insertRemove == 0) && (k == visualStart)) {
                    k += length;
                    continue;
                }
                if (insertRemove == 0) {
                    visualLimit = runs[i].limit;
                    for (j = visualStart; j < visualLimit; j++) {
                        indexMap[k++] = indexMap[j];
                    }
                    continue;
                }
                logicalStart = runs[i].start;
                evenRun = runs[i].isEvenRun();
                logicalEnd = logicalStart + length - 1;
                for (j = 0; j < length; j++) {
                    m = evenRun ? logicalStart + j : logicalEnd - j;
                    uchar = bidiBase.text[m];
                    if (!BidiBase.IsBidiControlChar(uchar)) {
                        indexMap[k++] = m;
                    }
                }
            }
        }
        if (allocLength == bidiBase.resultLength) {
            return indexMap;
        }
        int[] newMap = new int[bidiBase.resultLength];
        System.arraycopy(indexMap, 0, newMap, 0, bidiBase.resultLength);
        return newMap;
    }
}
