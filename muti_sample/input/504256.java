public class TimeLineView extends Composite implements Observer {
    private HashMap<String, RowData> mRowByName;
    private double mTotalElapsed;
    private RowData[] mRows;
    private Segment[] mSegments;
    private ArrayList<Segment> mSegmentList = new ArrayList<Segment>();
    private HashMap<Integer, String> mThreadLabels;
    private Timescale mTimescale;
    private Surface mSurface;
    private RowLabels mLabels;
    private SashForm mSashForm;
    private int mScrollOffsetY;
    public static final int PixelsPerTick = 50;
    private TickScaler mScaleInfo = new TickScaler(0, 0, 0, PixelsPerTick);
    private static final int LeftMargin = 10; 
    private static final int RightMargin = 60; 
    private Color mColorBlack;
    private Color mColorGray;
    private Color mColorDarkGray;
    private Color mColorForeground;
    private Color mColorRowBack;
    private Color mColorZoomSelection;
    private FontRegistry mFontRegistry;
    private static final int rowHeight = 20;
    private static final int rowYMargin = 12;
    private static final int rowYMarginHalf = rowYMargin / 2;
    private static final int rowYSpace = rowHeight + rowYMargin;
    private static final int majorTickLength = 8;
    private static final int minorTickLength = 4;
    private static final int timeLineOffsetY = 38;
    private static final int tickToFontSpacing = 2;
    private static final int topMargin = 70;
    private int mMouseRow = -1;
    private int mNumRows;
    private int mStartRow;
    private int mEndRow;
    private TraceUnits mUnits;
    private int mSmallFontWidth;
    private int mSmallFontHeight;
    private int mMediumFontWidth;
    private SelectionController mSelectionController;
    private MethodData mHighlightMethodData;
    private Call mHighlightCall;
    private static final int MinInclusiveRange = 3;
    private boolean mSetFonts = false;
    public static interface Block {
        public String getName();
        public MethodData getMethodData();
        public long getStartTime();
        public long getEndTime();
        public Color getColor();
        public double addWeight(int x, int y, double weight);
        public void clearWeight();
    }
    public static interface Row {
        public int getId();
        public String getName();
    }
    public static class Record {
        Row row;
        Block block;
        public Record(Row row, Block block) {
            this.row = row;
            this.block = block;
        }
    }
    public TimeLineView(Composite parent, TraceReader reader,
            SelectionController selectionController) {
        super(parent, SWT.NONE);
        mRowByName = new HashMap<String, RowData>();
        this.mSelectionController = selectionController;
        selectionController.addObserver(this);
        mUnits = reader.getTraceUnits();
        mThreadLabels = reader.getThreadLabels();
        Display display = getDisplay();
        mColorGray = display.getSystemColor(SWT.COLOR_GRAY);
        mColorDarkGray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        mColorBlack = display.getSystemColor(SWT.COLOR_BLACK);
        mColorForeground = display.getSystemColor(SWT.COLOR_BLACK);
        mColorRowBack = new Color(display, 240, 240, 255);
        mColorZoomSelection = new Color(display, 230, 230, 230);
        mFontRegistry = new FontRegistry(display);
        mFontRegistry.put("small",  
                new FontData[] { new FontData("Arial", 8, SWT.NORMAL) });  
        mFontRegistry.put("courier8",  
                new FontData[] { new FontData("Courier New", 8, SWT.BOLD) });  
        mFontRegistry.put("medium",  
                new FontData[] { new FontData("Courier New", 10, SWT.NORMAL) });  
        Image image = new Image(display, new Rectangle(100, 100, 100, 100));
        GC gc = new GC(image);
        if (mSetFonts) {
            gc.setFont(mFontRegistry.get("small"));  
        }
        mSmallFontWidth = gc.getFontMetrics().getAverageCharWidth();
        mSmallFontHeight = gc.getFontMetrics().getHeight();
        if (mSetFonts) {
            gc.setFont(mFontRegistry.get("medium"));  
        }
        mMediumFontWidth = gc.getFontMetrics().getAverageCharWidth();
        image.dispose();
        gc.dispose();
        setLayout(new FillLayout());
        mSashForm = new SashForm(this, SWT.HORIZONTAL);
        mSashForm.setBackground(mColorGray);
        mSashForm.SASH_WIDTH = 3;
        Composite composite = new Composite(mSashForm, SWT.NONE);
        GridLayout layout = new GridLayout(1, true );
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 1;
        composite.setLayout(layout);
        BlankCorner corner = new BlankCorner(composite);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = topMargin;
        corner.setLayoutData(gridData);
        mLabels = new RowLabels(composite);
        gridData = new GridData(GridData.FILL_BOTH);
        mLabels.setLayoutData(gridData);
        composite = new Composite(mSashForm, SWT.NONE);
        layout = new GridLayout(1, true );
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 1;
        composite.setLayout(layout);
        mTimescale = new Timescale(composite);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = topMargin;
        mTimescale.setLayoutData(gridData);
        mSurface = new Surface(composite);
        gridData = new GridData(GridData.FILL_BOTH);
        mSurface.setLayoutData(gridData);
        mSashForm.setWeights(new int[] { 1, 5 });
        final ScrollBar vBar = mSurface.getVerticalBar();
        vBar.addListener(SWT.Selection, new Listener() {
           public void handleEvent(Event e) {
               mScrollOffsetY = vBar.getSelection();
               Point dim = mSurface.getSize();
               int newScrollOffsetY = computeVisibleRows(dim.y);
               if (newScrollOffsetY != mScrollOffsetY) {
                   mScrollOffsetY = newScrollOffsetY;
                   vBar.setSelection(newScrollOffsetY);
               }
               mLabels.redraw();
               mSurface.redraw();
           }
        });
        mSurface.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {
                Point dim = mSurface.getSize();
                if (dim.y >= mNumRows * rowYSpace) {
                    vBar.setVisible(false);
                } else {
                    vBar.setVisible(true);
                }
                int newScrollOffsetY = computeVisibleRows(dim.y);
                if (newScrollOffsetY != mScrollOffsetY) {
                    mScrollOffsetY = newScrollOffsetY;
                    vBar.setSelection(newScrollOffsetY);
                }
                int spaceNeeded = mNumRows * rowYSpace;
                vBar.setMaximum(spaceNeeded);
                vBar.setThumb(dim.y);
                mLabels.redraw();
                mSurface.redraw();
            }
        });
        mSurface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent me) {
                mSurface.mouseUp(me);
            }
            @Override
            public void mouseDown(MouseEvent me) {
                mSurface.mouseDown(me);
            }
            @Override
            public void mouseDoubleClick(MouseEvent me) {
                mSurface.mouseDoubleClick(me);
            }
        });
        mSurface.addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent me) {
                mSurface.mouseMove(me);
            }
        });
        mTimescale.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent me) {
                mTimescale.mouseUp(me);
            }
            @Override
            public void mouseDown(MouseEvent me) {
                mTimescale.mouseDown(me);
            }
            @Override
            public void mouseDoubleClick(MouseEvent me) {
                mTimescale.mouseDoubleClick(me);
            }
        });
        mTimescale.addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent me) {
                mTimescale.mouseMove(me);
            }
        });
        mLabels.addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent me) {
                mLabels.mouseMove(me);
            }
        });
        setData(reader.getThreadTimeRecords());
    }
    public void update(Observable objservable, Object arg) {
        if (arg == "TimeLineView")  
            return;
        boolean foundHighlight = false;
        ArrayList<Selection> selections;
        selections = mSelectionController.getSelections();
        for (Selection selection : selections) {
            Selection.Action action = selection.getAction();
            if (action != Selection.Action.Highlight)
                continue;
            String name = selection.getName();
            if (name == "MethodData") {  
                foundHighlight = true;
                mHighlightMethodData = (MethodData) selection.getValue();
                mHighlightCall = null;
                startHighlighting();
            } else if (name == "Call") {  
                foundHighlight = true;
                mHighlightCall = (Call) selection.getValue();
                mHighlightMethodData = null;
                startHighlighting();
            }
        }
        if (foundHighlight == false)
            mSurface.clearHighlights();
    }
    public void setData(ArrayList<Record> records) {
        if (records == null)
            records = new ArrayList<Record>();
        if (false) {
            System.out.println("TimelineView() list of records:");  
            for (Record r : records) {
                System.out.printf("row '%s' block '%s' [%d, %d]\n", r.row  
                        .getName(), r.block.getName(), r.block.getStartTime(),
                        r.block.getEndTime());
                if (r.block.getStartTime() > r.block.getEndTime()) {
                    System.err.printf("Error: block startTime > endTime\n");  
                    System.exit(1);
                }
            }
        }
        Collections.sort(records, new Comparator<Record>() {
            public int compare(Record r1, Record r2) {
                long start1 = r1.block.getStartTime();
                long start2 = r2.block.getStartTime();
                if (start1 > start2)
                    return 1;
                if (start1 < start2)
                    return -1;
                long end1 = r1.block.getEndTime();
                long end2 = r2.block.getEndTime();
                if (end1 > end2)
                    return -1;
                if (end1 < end2)
                    return 1;
                return 0;
            }
        });
        double minVal = 0;
        if (records.size() > 0)
            minVal = records.get(0).block.getStartTime();
        double maxVal = 0;
        for (Record rec : records) {
            Row row = rec.row;
            Block block = rec.block;
            String rowName = row.getName();
            RowData rd = mRowByName.get(rowName);
            if (rd == null) {
                rd = new RowData(row);
                mRowByName.put(rowName, rd);
            }
            long blockStartTime = block.getStartTime();
            long blockEndTime = block.getEndTime();
            if (blockEndTime > rd.mEndTime) {
                long start = Math.max(blockStartTime, rd.mEndTime);
                rd.mElapsed += blockEndTime - start;
                mTotalElapsed += blockEndTime - start;
                rd.mEndTime = blockEndTime;
            }
            if (blockEndTime > maxVal)
                maxVal = blockEndTime;
            Block top = rd.top();
            if (top == null) {
                rd.push(block);
                continue;
            }
            long topStartTime = top.getStartTime();
            long topEndTime = top.getEndTime();
            if (topEndTime >= blockStartTime) {
                if (topStartTime < blockStartTime) {
                    Segment segment = new Segment(rd, top, topStartTime,
                            blockStartTime);
                    mSegmentList.add(segment);
                }
                if (topEndTime == blockStartTime)
                    rd.pop();
                rd.push(block);
            } else {
                popFrames(rd, top, blockStartTime);
                rd.push(block);
            }
        }
        for (RowData rd : mRowByName.values()) {
            Block top = rd.top();
            popFrames(rd, top, Integer.MAX_VALUE);
        }
        mSurface.setRange(minVal, maxVal);
        mSurface.setLimitRange(minVal, maxVal);
        Collection<RowData> rv = mRowByName.values();
        mRows = rv.toArray(new RowData[rv.size()]);
        Arrays.sort(mRows, new Comparator<RowData>() {
            public int compare(RowData rd1, RowData rd2) {
                return (int) (rd2.mElapsed - rd1.mElapsed);
            }
        });
        for (int ii = 0; ii < mRows.length; ++ii) {
            mRows[ii].mRank = ii;
        }
        mNumRows = 0;
        for (int ii = 0; ii < mRows.length; ++ii) {
            if (mRows[ii].mElapsed == 0)
                break;
            mNumRows += 1;
        }
        mSegments = mSegmentList.toArray(new Segment[mSegmentList.size()]);
        Arrays.sort(mSegments, new Comparator<Segment>() {
            public int compare(Segment bd1, Segment bd2) {
                RowData rd1 = bd1.mRowData;
                RowData rd2 = bd2.mRowData;
                int diff = rd1.mRank - rd2.mRank;
                if (diff == 0) {
                    long timeDiff = bd1.mStartTime - bd2.mStartTime;
                    if (timeDiff == 0)
                        timeDiff = bd1.mEndTime - bd2.mEndTime;
                    return (int) timeDiff;
                }
                return diff;
            }
        });
        if (false) {
            for (Segment segment : mSegments) {
                System.out.printf("seg '%s' [%6d, %6d] %s\n",
                        segment.mRowData.mName, segment.mStartTime,
                        segment.mEndTime, segment.mBlock.getName());
                if (segment.mStartTime > segment.mEndTime) {
                    System.err.printf("Error: segment startTime > endTime\n");
                    System.exit(1);
                }
            }
        }
    }
    private void popFrames(RowData rd, Block top, long startTime) {
        long topEndTime = top.getEndTime();
        long lastEndTime = top.getStartTime();
        while (topEndTime <= startTime) {
            if (topEndTime > lastEndTime) {
                Segment segment = new Segment(rd, top, lastEndTime, topEndTime);
                mSegmentList.add(segment);
                lastEndTime = topEndTime;
            }
            rd.pop();
            top = rd.top();
            if (top == null)
                return;
            topEndTime = top.getEndTime();
        }
        if (lastEndTime < startTime) {
            Segment bd = new Segment(rd, top, lastEndTime, startTime);
            mSegmentList.add(bd);
        }
    }
    private class RowLabels extends Canvas {
        private static final int labelMarginX = 2;
        public RowLabels(Composite parent) {
            super(parent, SWT.NO_BACKGROUND);
            addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent pe) {
                    draw(pe.display, pe.gc);
                }
            });
        }
        private void mouseMove(MouseEvent me) {
            int rownum = (me.y + mScrollOffsetY) / rowYSpace;
            if (mMouseRow != rownum) {
                mMouseRow = rownum;
                redraw();
                mSurface.redraw();
            }
        }
        private void draw(Display display, GC gc) {
            if (mSegments.length == 0) {
                return;
            }
            Point dim = getSize();
            Image image = new Image(display, getBounds());
            GC gcImage = new GC(image);
            if (mSetFonts)
                gcImage.setFont(mFontRegistry.get("medium"));  
            if (mNumRows > 2) {
                gcImage.setBackground(mColorRowBack);
                for (int ii = 1; ii < mNumRows; ii += 2) {
                    RowData rd = mRows[ii];
                    int y1 = rd.mRank * rowYSpace - mScrollOffsetY;
                    gcImage.fillRectangle(0, y1, dim.x, rowYSpace);
                }
            }
            int offsetY = rowYMarginHalf - mScrollOffsetY;
            for (int ii = mStartRow; ii <= mEndRow; ++ii) {
                RowData rd = mRows[ii];
                int y1 = rd.mRank * rowYSpace + offsetY;
                Point extent = gcImage.stringExtent(rd.mName);
                int x1 = dim.x - extent.x - labelMarginX;
                gcImage.drawString(rd.mName, x1, y1, true);
            }
            if (mMouseRow >= mStartRow && mMouseRow <= mEndRow) {
                gcImage.setForeground(mColorGray);
                int y1 = mMouseRow * rowYSpace - mScrollOffsetY;
                gcImage.drawRectangle(0, y1, dim.x, rowYSpace);
            }
            gc.drawImage(image, 0, 0);
            image.dispose();
            gcImage.dispose();
        }
    }
    private class BlankCorner extends Canvas {
        public BlankCorner(Composite parent) {
            super(parent, SWT.NONE);
            addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent pe) {
                    draw(pe.display, pe.gc);
                }
            });
        }
        private void draw(Display display, GC gc) {
            Image image = new Image(display, getBounds());
            gc.drawImage(image, 0, 0);
            image.dispose();
        }
    }
    private class Timescale extends Canvas {
        private Point mMouse = new Point(LeftMargin, 0);
        private Cursor mZoomCursor;
        private String mMethodName = null;
        private Color mMethodColor = null;
        private int mMethodStartY;
        private int mMarkStartX;
        private int mMarkEndX;
        private static final int METHOD_BLOCK_MARGIN = 10;
        public Timescale(Composite parent) {
            super(parent, SWT.NONE);
            Display display = getDisplay();
            mZoomCursor = new Cursor(display, SWT.CURSOR_SIZEWE);
            setCursor(mZoomCursor);
            mMethodStartY = mSmallFontHeight + 1;
            addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent pe) {
                    draw(pe.display, pe.gc);
                }
            });
        }
        public void setVbarPosition(int x) {
            mMouse.x = x;
        }
        public void setMarkStart(int x) {
            mMarkStartX = x;
        }
        public void setMarkEnd(int x) {
            mMarkEndX = x;
        }
        public void setMethodName(String name) {
            mMethodName = name;
        }
        public void setMethodColor(Color color) {
            mMethodColor = color;
        }
        private void mouseMove(MouseEvent me) {
            me.y = -1;
            mSurface.mouseMove(me);
        }
        private void mouseDown(MouseEvent me) {
            mSurface.startScaling(me.x);
            mSurface.redraw();
        }
        private void mouseUp(MouseEvent me) {
            mSurface.stopScaling(me.x);
        }
        private void mouseDoubleClick(MouseEvent me) {
            mSurface.resetScale();
            mSurface.redraw();
        }
        private void draw(Display display, GC gc) {
            Point dim = getSize();
            Image image = new Image(display, getBounds());
            GC gcImage = new GC(image);
            if (mSetFonts)
                gcImage.setFont(mFontRegistry.get("medium"));  
            if (mSurface.drawingSelection()) {
                drawSelection(display, gcImage);
            }
            drawTicks(display, gcImage);
            gcImage.setForeground(mColorDarkGray);
            gcImage.drawLine(mMouse.x, timeLineOffsetY, mMouse.x, dim.y);
            drawTickLegend(display, gcImage);
            drawMethod(display, gcImage);
            gc.drawImage(image, 0, 0);
            image.dispose();
            gcImage.dispose();
        }
        private void drawSelection(Display display, GC gc) {
            Point dim = getSize();
            gc.setForeground(mColorGray);
            gc.drawLine(mMarkStartX, timeLineOffsetY, mMarkStartX, dim.y);
            gc.setBackground(mColorZoomSelection);
            int x, width;
            if (mMarkStartX < mMarkEndX) {
                x = mMarkStartX;
                width = mMarkEndX - mMarkStartX;
            } else {
                x = mMarkEndX;
                width = mMarkStartX - mMarkEndX;
            }
            if (width > 1) {
                gc.fillRectangle(x, timeLineOffsetY, width, dim.y);
            }
        }
        private void drawTickLegend(Display display, GC gc) {
            int mouseX = mMouse.x - LeftMargin;
            double mouseXval = mScaleInfo.pixelToValue(mouseX);
            String info = mUnits.labelledString(mouseXval);
            gc.setForeground(mColorForeground);
            gc.drawString(info, LeftMargin + 2, 1, true);
            double maxVal = mScaleInfo.getMaxVal();
            info = mUnits.labelledString(maxVal);
            info = String.format(" max %s ", info);  
            Point extent = gc.stringExtent(info);
            Point dim = getSize();
            int x1 = dim.x - RightMargin - extent.x;
            gc.drawString(info, x1, 1, true);
        }
        private void drawMethod(Display display, GC gc) {
            if (mMethodName == null) {
                return;
            }
            int x1 = LeftMargin;
            int y1 = mMethodStartY;
            gc.setBackground(mMethodColor);
            int width = 2 * mSmallFontWidth;
            gc.fillRectangle(x1, y1, width, mSmallFontHeight);
            x1 += width + METHOD_BLOCK_MARGIN;
            gc.drawString(mMethodName, x1, y1, true);
        }
        private void drawTicks(Display display, GC gc) {
            Point dim = getSize();
            int y2 = majorTickLength + timeLineOffsetY;
            int y3 = minorTickLength + timeLineOffsetY;
            int y4 = y2 + tickToFontSpacing;
            gc.setForeground(mColorForeground);
            gc.drawLine(LeftMargin, timeLineOffsetY, dim.x - RightMargin,
                    timeLineOffsetY);
            double minVal = mScaleInfo.getMinVal();
            double maxVal = mScaleInfo.getMaxVal();
            double minMajorTick = mScaleInfo.getMinMajorTick();
            double tickIncrement = mScaleInfo.getTickIncrement();
            double minorTickIncrement = tickIncrement / 5;
            double pixelsPerRange = mScaleInfo.getPixelsPerRange();
            if (minVal < minMajorTick) {
                gc.setForeground(mColorGray);
                double xMinor = minMajorTick;
                for (int ii = 1; ii <= 4; ++ii) {
                    xMinor -= minorTickIncrement;
                    if (xMinor < minVal)
                        break;
                    int x1 = LeftMargin
                            + (int) (0.5 + (xMinor - minVal) * pixelsPerRange);
                    gc.drawLine(x1, timeLineOffsetY, x1, y3);
                }
            }
            if (tickIncrement <= 10) {
                return;
            }
            for (double x = minMajorTick; x <= maxVal; x += tickIncrement) {
                int x1 = LeftMargin
                        + (int) (0.5 + (x - minVal) * pixelsPerRange);
                gc.setForeground(mColorForeground);
                gc.drawLine(x1, timeLineOffsetY, x1, y2);
                if (x > maxVal)
                    break;
                String tickString = mUnits.valueOf(x);
                gc.drawString(tickString, x1, y4, true);
                gc.setForeground(mColorGray);
                double xMinor = x;
                for (int ii = 1; ii <= 4; ii++) {
                    xMinor += minorTickIncrement;
                    if (xMinor > maxVal)
                        break;
                    x1 = LeftMargin
                            + (int) (0.5 + (xMinor - minVal) * pixelsPerRange);
                    gc.drawLine(x1, timeLineOffsetY, x1, y3);
                }
            }
        }
    }
    private static enum GraphicsState {
        Normal, Marking, Scaling, Animating
    };
    private class Surface extends Canvas {
        public Surface(Composite parent) {
            super(parent, SWT.NO_BACKGROUND | SWT.V_SCROLL);
            Display display = getDisplay();
            mNormalCursor = new Cursor(display, SWT.CURSOR_CROSS);
            mIncreasingCursor = new Cursor(display, SWT.CURSOR_SIZEE);
            mDecreasingCursor = new Cursor(display, SWT.CURSOR_SIZEW);
            initZoomFractionsWithExp();
            addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent pe) {
                    draw(pe.display, pe.gc);
                }
            });
            mZoomAnimator = new Runnable() {
                public void run() {
                    animateZoom();
                }
            };
            mHighlightAnimator = new Runnable() {
                public void run() {
                    animateHighlight();
                }
            };
        }
        private void initZoomFractionsWithExp() {
            mZoomFractions = new double[ZOOM_STEPS];
            int next = 0;
            for (int ii = 0; ii < ZOOM_STEPS / 2; ++ii, ++next) {
                mZoomFractions[next] = (double) (1 << ii)
                        / (double) (1 << (ZOOM_STEPS / 2));
            }
            for (int ii = 2; ii < 2 + ZOOM_STEPS / 2; ++ii, ++next) {
                mZoomFractions[next] = (double) ((1 << ii) - 1)
                        / (double) (1 << ii);
            }
        }
        @SuppressWarnings("unused")
        private void initZoomFractionsWithSinWave() {
            mZoomFractions = new double[ZOOM_STEPS];
            for (int ii = 0; ii < ZOOM_STEPS; ++ii) {
                double offset = Math.PI * (double) ii / (double) ZOOM_STEPS;
                mZoomFractions[ii] = (Math.sin((1.5 * Math.PI + offset)) + 1.0) / 2.0;
            }
        }
        public void setRange(double minVal, double maxVal) {
            mMinDataVal = minVal;
            mMaxDataVal = maxVal;
            mScaleInfo.setMinVal(minVal);
            mScaleInfo.setMaxVal(maxVal);
        }
        public void setLimitRange(double minVal, double maxVal) {
            mLimitMinVal = minVal;
            mLimitMaxVal = maxVal;
        }
        public void resetScale() {
            mScaleInfo.setMinVal(mLimitMinVal);
            mScaleInfo.setMaxVal(mLimitMaxVal);
        }
        private void draw(Display display, GC gc) {
            if (mSegments.length == 0) {
                return;
            }
            Image image = new Image(display, getBounds());
            GC gcImage = new GC(image);
            if (mSetFonts)
                gcImage.setFont(mFontRegistry.get("small"));  
            if (mGraphicsState == GraphicsState.Scaling) {
                double diff = mMouse.x - mMouseMarkStartX;
                if (diff > 0) {
                    double newMinVal = mScaleMinVal - diff / mScalePixelsPerRange;
                    if (newMinVal < mLimitMinVal)
                        newMinVal = mLimitMinVal;
                    mScaleInfo.setMinVal(newMinVal);
                } else if (diff < 0) {
                    double newMaxVal = mScaleMaxVal - diff / mScalePixelsPerRange;
                    if (newMaxVal > mLimitMaxVal)
                        newMaxVal = mLimitMaxVal;
                    mScaleInfo.setMaxVal(newMaxVal);
                }
            }
            Point dim = getSize();
            if (mStartRow != mCachedStartRow || mEndRow != mCachedEndRow 
                    || mScaleInfo.getMinVal() != mCachedMinVal
                    || mScaleInfo.getMaxVal() != mCachedMaxVal) {
                mCachedStartRow = mStartRow;
                mCachedEndRow = mEndRow;
                int xdim = dim.x - TotalXMargin;
                mScaleInfo.setNumPixels(xdim);
                boolean forceEndPoints = (mGraphicsState == GraphicsState.Scaling
                        || mGraphicsState == GraphicsState.Animating);
                mScaleInfo.computeTicks(forceEndPoints);
                mCachedMinVal = mScaleInfo.getMinVal();
                mCachedMaxVal = mScaleInfo.getMaxVal();
                if (mLimitMinVal > mScaleInfo.getMinVal())
                    mLimitMinVal = mScaleInfo.getMinVal();
                if (mLimitMaxVal < mScaleInfo.getMaxVal())
                    mLimitMaxVal = mScaleInfo.getMaxVal();
                computeStrips();
            }
            if (mNumRows > 2) {
                gcImage.setBackground(mColorRowBack);
                for (int ii = 1; ii < mNumRows; ii += 2) {
                    RowData rd = mRows[ii];
                    int y1 = rd.mRank * rowYSpace - mScrollOffsetY;
                    gcImage.fillRectangle(0, y1, dim.x, rowYSpace);
                }
            }
            if (drawingSelection()) {
                drawSelection(display, gcImage);
            }
            String blockName = null;
            Color blockColor = null;
            if (mDebug) {
                double pixelsPerRange = mScaleInfo.getPixelsPerRange();
                System.out
                        .printf(
                                "dim.x %d pixels %d minVal %f, maxVal %f ppr %f rpp %f\n",
                                dim.x, dim.x - TotalXMargin, mScaleInfo
                                        .getMinVal(), mScaleInfo.getMaxVal(),
                                pixelsPerRange, 1.0 / pixelsPerRange);
            }
            Block selectBlock = null;
            for (Strip strip : mStripList) {
                if (strip.mColor == null) {
                    continue;
                }
                gcImage.setBackground(strip.mColor);
                gcImage.fillRectangle(strip.mX, strip.mY - mScrollOffsetY, strip.mWidth,
                        strip.mHeight);
                if (mMouseRow == strip.mRowData.mRank) {
                    if (mMouse.x >= strip.mX
                            && mMouse.x < strip.mX + strip.mWidth) {
                        blockName = strip.mSegment.mBlock.getName();
                        blockColor = strip.mColor;
                    }
                    if (mMouseSelect.x >= strip.mX
                            && mMouseSelect.x < strip.mX + strip.mWidth) {
                        selectBlock = strip.mSegment.mBlock;
                    }
                }
            }
            mMouseSelect.x = 0;
            mMouseSelect.y = 0;
            if (selectBlock != null) {
                ArrayList<Selection> selections = new ArrayList<Selection>();
                RowData rd = mRows[mMouseRow];
                selections.add(Selection.highlight("Thread", rd.mName));  
                selections.add(Selection.highlight("Call", selectBlock));  
                int mouseX = mMouse.x - LeftMargin;
                double mouseXval = mScaleInfo.pixelToValue(mouseX);
                selections.add(Selection.highlight("Time", mouseXval));  
                mSelectionController.change(selections, "TimeLineView");  
                mHighlightMethodData = null;
                mHighlightCall = (Call) selectBlock;
                startHighlighting();
            }
            if (mMouseRow >= 0 && mMouseRow < mNumRows && mHighlightStep == 0) {
                gcImage.setForeground(mColorGray);
                int y1 = mMouseRow * rowYSpace - mScrollOffsetY;
                gcImage.drawLine(0, y1, dim.x, y1);
                gcImage.drawLine(0, y1 + rowYSpace, dim.x, y1 + rowYSpace);
            }
            drawHighlights(gcImage, dim);
            gcImage.setForeground(mColorDarkGray);
            int lineEnd = Math.min(dim.y, mNumRows * rowYSpace);
            gcImage.drawLine(mMouse.x, 0, mMouse.x, lineEnd);
            if (blockName != null) {
                mTimescale.setMethodName(blockName);
                mTimescale.setMethodColor(blockColor);
                mShowHighlightName = false;
            } else if (mShowHighlightName) {
                MethodData md = mHighlightMethodData;
                if (md == null && mHighlightCall != null)
                    md = mHighlightCall.getMethodData();
                if (md == null)
                    System.out.printf("null highlight?\n");  
                if (md != null) {
                    mTimescale.setMethodName(md.getProfileName());
                    mTimescale.setMethodColor(md.getColor());
                }
            } else {
                mTimescale.setMethodName(null);
                mTimescale.setMethodColor(null);
            }
            mTimescale.redraw();
            gc.drawImage(image, 0, 0);
            image.dispose();
            gcImage.dispose();
        }
        private void drawHighlights(GC gc, Point dim) {
            int height = highlightHeight;
            if (height <= 0)
                return;
            for (Range range : mHighlightExclusive) {
                gc.setBackground(range.mColor);
                int xStart = range.mXdim.x;
                int width = range.mXdim.y;
                gc.fillRectangle(xStart, range.mY - height - mScrollOffsetY, width, height);
            }
            height -= 1;
            if (height <= 0)
                height = 1;
            gc.setForeground(mColorDarkGray);
            gc.setBackground(mColorDarkGray);
            for (Range range : mHighlightInclusive) {
                int x1 = range.mXdim.x;
                int x2 = range.mXdim.y;
                boolean drawLeftEnd = false;
                boolean drawRightEnd = false;
                if (x1 >= LeftMargin)
                    drawLeftEnd = true;
                else
                    x1 = LeftMargin;
                if (x2 >= LeftMargin)
                    drawRightEnd = true;
                else
                    x2 = dim.x - RightMargin;
                int y1 = range.mY + rowHeight + 2 - mScrollOffsetY;
                if (x2 - x1 < MinInclusiveRange) {
                    int width = x2 - x1;
                    if (width < 2)
                        width = 2;
                    gc.fillRectangle(x1, y1, width, height);
                    continue;
                }
                if (drawLeftEnd) {
                    if (drawRightEnd) {
                        int[] points = { x1, y1, x1, y1 + height, x2,
                                y1 + height, x2, y1 };
                        gc.drawPolyline(points);
                    } else {
                        int[] points = { x1, y1, x1, y1 + height, x2,
                                y1 + height };
                        gc.drawPolyline(points);
                    }
                } else {
                    if (drawRightEnd) {
                        int[] points = { x1, y1 + height, x2, y1 + height, x2,
                                y1 };
                        gc.drawPolyline(points);
                    } else {
                        int[] points = { x1, y1 + height, x2, y1 + height };
                        gc.drawPolyline(points);
                    }
                }
                if (drawLeftEnd == false) {
                    int[] points = { x1 + 7, y1 + height - 4, x1, y1 + height,
                            x1 + 7, y1 + height + 4 };
                    gc.fillPolygon(points);
                }
                if (drawRightEnd == false) {
                    int[] points = { x2 - 7, y1 + height - 4, x2, y1 + height,
                            x2 - 7, y1 + height + 4 };
                    gc.fillPolygon(points);
                }
            }
        }
        private boolean drawingSelection() {
            return mGraphicsState == GraphicsState.Marking
                    || mGraphicsState == GraphicsState.Animating;
        }
        private void drawSelection(Display display, GC gc) {
            Point dim = getSize();
            gc.setForeground(mColorGray);
            gc.drawLine(mMouseMarkStartX, 0, mMouseMarkStartX, dim.y);
            gc.setBackground(mColorZoomSelection);
            int width;
            int mouseX = (mGraphicsState == GraphicsState.Animating) ? mMouseMarkEndX : mMouse.x;
            int x;
            if (mMouseMarkStartX < mouseX) {
                x = mMouseMarkStartX;
                width = mouseX - mMouseMarkStartX;
            } else {
                x = mouseX;
                width = mMouseMarkStartX - mouseX;
            }
            gc.fillRectangle(x, 0, width, dim.y);
        }
        private void computeStrips() {
            double minVal = mScaleInfo.getMinVal();
            double maxVal = mScaleInfo.getMaxVal();
            Pixel[] pixels = new Pixel[mNumRows];
            for (int ii = 0; ii < mNumRows; ++ii)
                pixels[ii] = new Pixel();
            for (int ii = 0; ii < mSegments.length; ++ii) {
                mSegments[ii].mBlock.clearWeight();
            }
            mStripList.clear();
            mHighlightExclusive.clear();
            mHighlightInclusive.clear();
            MethodData callMethod = null;
            long callStart = 0;
            long callEnd = -1;
            RowData callRowData = null;
            int prevMethodStart = -1;
            int prevCallStart = -1;
            if (mHighlightCall != null) {
                int callPixelStart = -1;
                int callPixelEnd = -1;
                callStart = mHighlightCall.mGlobalStartTime;
                callEnd = mHighlightCall.mGlobalEndTime;
                callMethod = mHighlightCall.mMethodData;
                if (callStart >= minVal)
                    callPixelStart = mScaleInfo.valueToPixel(callStart);
                if (callEnd <= maxVal)
                    callPixelEnd = mScaleInfo.valueToPixel(callEnd);
                int threadId = mHighlightCall.getThreadId();
                String threadName = mThreadLabels.get(threadId);
                callRowData = mRowByName.get(threadName);
                int y1 = callRowData.mRank * rowYSpace + rowYMarginHalf;
                Color color = callMethod.getColor();
                mHighlightInclusive.add(new Range(callPixelStart + LeftMargin,
                        callPixelEnd + LeftMargin, y1, color));
            }
            for (Segment segment : mSegments) {
                if (segment.mEndTime <= minVal)
                    continue;
                if (segment.mStartTime >= maxVal)
                    continue;
                Block block = segment.mBlock;
                Color color = block.getColor();
                if (color == null)
                    continue;
                double recordStart = Math.max(segment.mStartTime, minVal);
                double recordEnd = Math.min(segment.mEndTime, maxVal);
                if (recordStart == recordEnd)
                    continue;
                int pixelStart = mScaleInfo.valueToPixel(recordStart);
                int pixelEnd = mScaleInfo.valueToPixel(recordEnd);
                int width = pixelEnd - pixelStart;
                RowData rd = segment.mRowData;
                MethodData md = block.getMethodData();
                int y1 = rd.mRank * rowYSpace + rowYMarginHalf;
                if (rd.mRank > mEndRow)
                    break;
                if (mHighlightMethodData != null) {
                    if (mHighlightMethodData == md) {
                        if (prevMethodStart != pixelStart) {
                            prevMethodStart = pixelStart;
                            int rangeWidth = width;
                            if (rangeWidth == 0)
                                rangeWidth = 1;
                            mHighlightExclusive.add(new Range(pixelStart
                                    + LeftMargin, rangeWidth, y1, color));
                            Call call = (Call) block;
                            callStart = call.mGlobalStartTime;
                            int callPixelStart = -1;
                            if (callStart >= minVal)
                                callPixelStart = mScaleInfo.valueToPixel(callStart);
                            if (prevCallStart != callPixelStart) {
                                prevCallStart = callPixelStart;
                                int callPixelEnd = -1;
                                callEnd = call.mGlobalEndTime;
                                if (callEnd <= maxVal)
                                    callPixelEnd = mScaleInfo.valueToPixel(callEnd);
                                mHighlightInclusive.add(new Range(
                                        callPixelStart + LeftMargin,
                                        callPixelEnd + LeftMargin, y1, color));
                            }
                        }
                    } else if (mFadeColors) {
                        color = md.getFadedColor();
                    }
                } else if (mHighlightCall != null) {
                    if (segment.mStartTime >= callStart
                            && segment.mEndTime <= callEnd && callMethod == md
                            && callRowData == rd) {
                        if (prevMethodStart != pixelStart) {
                            prevMethodStart = pixelStart;
                            int rangeWidth = width;
                            if (rangeWidth == 0)
                                rangeWidth = 1;
                            mHighlightExclusive.add(new Range(pixelStart
                                    + LeftMargin, rangeWidth, y1, color));
                        }
                    } else if (mFadeColors) {
                        color = md.getFadedColor();
                    }
                }
                Pixel pix = pixels[rd.mRank];
                if (pix.mStart != pixelStart) {
                    if (pix.mSegment != null) {
                        emitPixelStrip(rd, y1, pix);
                    }
                    if (width == 0) {
                        double weight = computeWeight(recordStart, recordEnd,
                                pixelStart);
                        weight = block.addWeight(pixelStart, rd.mRank, weight);
                        if (weight > pix.mMaxWeight) {
                            pix.setFields(pixelStart, weight, segment, color,
                                    rd);
                        }
                    } else {
                        int x1 = pixelStart + LeftMargin;
                        Strip strip = new Strip(x1, y1, width, rowHeight, rd,
                                segment, color);
                        mStripList.add(strip);
                    }
                } else {
                    double weight = computeWeight(recordStart, recordEnd,
                            pixelStart);
                    weight = block.addWeight(pixelStart, rd.mRank, weight);
                    if (weight > pix.mMaxWeight) {
                        pix.setFields(pixelStart, weight, segment, color, rd);
                    }
                    if (width == 1) {
                        emitPixelStrip(rd, y1, pix);
                        pixelStart += 1;
                        weight = computeWeight(recordStart, recordEnd,
                                pixelStart);
                        weight = block.addWeight(pixelStart, rd.mRank, weight);
                        pix.setFields(pixelStart, weight, segment, color, rd);
                    } else if (width > 1) {
                        emitPixelStrip(rd, y1, pix);
                        pixelStart += 1;
                        width -= 1;
                        int x1 = pixelStart + LeftMargin;
                        Strip strip = new Strip(x1, y1, width, rowHeight, rd,
                                segment, color);
                        mStripList.add(strip);
                    }
                }
            }
            for (int ii = 0; ii < mNumRows; ++ii) {
                Pixel pix = pixels[ii];
                if (pix.mSegment != null) {
                    RowData rd = pix.mRowData;
                    int y1 = rd.mRank * rowYSpace + rowYMarginHalf;
                    emitPixelStrip(rd, y1, pix);
                }
            }
            if (false) {
                System.out.printf("computeStrips()\n");
                for (Strip strip : mStripList) {
                    System.out.printf("%3d, %3d width %3d height %d %s\n",
                            strip.mX, strip.mY, strip.mWidth, strip.mHeight,
                            strip.mSegment.mBlock.getName());
                }
            }
        }
        private double computeWeight(double start, double end, int pixel) {
            double pixelStartFraction = mScaleInfo.valueToPixelFraction(start);
            double pixelEndFraction = mScaleInfo.valueToPixelFraction(end);
            double leftEndPoint = Math.max(pixelStartFraction, pixel - 0.5);
            double rightEndPoint = Math.min(pixelEndFraction, pixel + 0.5);
            double weight = rightEndPoint - leftEndPoint;
            return weight;
        }
        private void emitPixelStrip(RowData rd, int y, Pixel pixel) {
            Strip strip;
            if (pixel.mSegment == null)
                return;
            int x = pixel.mStart + LeftMargin;
            int height = (int) (pixel.mMaxWeight * rowHeight * 0.75);
            if (height < mMinStripHeight)
                height = mMinStripHeight;
            int remainder = rowHeight - height;
            if (remainder > 0) {
                strip = new Strip(x, y, 1, remainder, rd, pixel.mSegment,
                        mFadeColors ? mColorGray : mColorBlack);
                mStripList.add(strip);
            }
            strip = new Strip(x, y + remainder, 1, height, rd, pixel.mSegment,
                    pixel.mColor);
            mStripList.add(strip);
            pixel.mSegment = null;
            pixel.mMaxWeight = 0.0;
        }
        private void mouseMove(MouseEvent me) {
            if (false) {
                if (mHighlightMethodData != null) {
                    mHighlightMethodData = null;
                    mCachedEndRow = -1;
                }
            }
            Point dim = mSurface.getSize();
            int x = me.x;
            if (x < LeftMargin)
                x = LeftMargin;
            if (x > dim.x - RightMargin)
                x = dim.x - RightMargin;
            mMouse.x = x;
            mMouse.y = me.y;
            mTimescale.setVbarPosition(x);
            if (mGraphicsState == GraphicsState.Marking) {
                mTimescale.setMarkEnd(x);
            }
            if (mGraphicsState == GraphicsState.Normal) {
                mSurface.setCursor(mNormalCursor);
            } else if (mGraphicsState == GraphicsState.Marking) {
                if (mMouse.x >= mMouseMarkStartX)
                    mSurface.setCursor(mIncreasingCursor);
                else
                    mSurface.setCursor(mDecreasingCursor);
            }
            int rownum = (mMouse.y + mScrollOffsetY) / rowYSpace;
            if (me.y < 0 || me.y >= dim.y) {
                rownum = -1;
            }
            if (mMouseRow != rownum) {
                mMouseRow = rownum;
                mLabels.redraw();
            }
            redraw();
        }
        private void mouseDown(MouseEvent me) {
            Point dim = mSurface.getSize();
            int x = me.x;
            if (x < LeftMargin)
                x = LeftMargin;
            if (x > dim.x - RightMargin)
                x = dim.x - RightMargin;
            mMouseMarkStartX = x;
            mGraphicsState = GraphicsState.Marking;
            mSurface.setCursor(mIncreasingCursor);
            mTimescale.setMarkStart(mMouseMarkStartX);
            mTimescale.setMarkEnd(mMouseMarkStartX);
            redraw();
        }
        private void mouseUp(MouseEvent me) {
            mSurface.setCursor(mNormalCursor);
            if (mGraphicsState != GraphicsState.Marking) {
                mGraphicsState = GraphicsState.Normal;
                return;
            }
            mGraphicsState = GraphicsState.Animating;
            Point dim = mSurface.getSize();
            if (me.y <= 0 || me.y >= dim.y) {
                mGraphicsState = GraphicsState.Normal;
                redraw();
                return;
            }
            int x = me.x;
            if (x < LeftMargin)
                x = LeftMargin;
            if (x > dim.x - RightMargin)
                x = dim.x - RightMargin;
            mMouseMarkEndX = x;
            int dist = mMouseMarkEndX - mMouseMarkStartX;
            if (dist < 0)
                dist = -dist;
            if (dist <= 2) {
                mGraphicsState = GraphicsState.Normal;
                mMouseSelect.x = mMouseMarkStartX;
                mMouseSelect.y = me.y;
                redraw();
                return;
            }
            if (mMouseMarkEndX < mMouseMarkStartX) {
                int temp = mMouseMarkEndX;
                mMouseMarkEndX = mMouseMarkStartX;
                mMouseMarkStartX = temp;
            }
            if (mMouseMarkStartX <= LeftMargin + MinZoomPixelMargin
                    && mMouseMarkEndX >= dim.x - RightMargin - MinZoomPixelMargin) {
                mGraphicsState = GraphicsState.Normal;
                redraw();
                return;
            }
            double minVal = mScaleInfo.getMinVal();
            double maxVal = mScaleInfo.getMaxVal();
            double ppr = mScaleInfo.getPixelsPerRange();
            mZoomMin = minVal + ((mMouseMarkStartX - LeftMargin) / ppr);
            mZoomMax = minVal + ((mMouseMarkEndX - LeftMargin) / ppr);
            if (mZoomMin < mMinDataVal)
                mZoomMin = mMinDataVal;
            if (mZoomMax > mMaxDataVal)
                mZoomMax = mMaxDataVal;
            int xdim = dim.x - TotalXMargin;
            TickScaler scaler = new TickScaler(mZoomMin, mZoomMax, xdim,
                    PixelsPerTick);
            scaler.computeTicks(false);
            mZoomMin = scaler.getMinVal();
            mZoomMax = scaler.getMaxVal();
            mMouseMarkStartX = (int) ((mZoomMin - minVal) * ppr + LeftMargin);
            mMouseMarkEndX = (int) ((mZoomMax - minVal) * ppr + LeftMargin);
            mTimescale.setMarkStart(mMouseMarkStartX);
            mTimescale.setMarkEnd(mMouseMarkEndX);
            mMouseEndDistance = dim.x - RightMargin - mMouseMarkEndX;
            mMouseStartDistance = mMouseMarkStartX - LeftMargin;
            mZoomMouseStart = mMouseMarkStartX;
            mZoomMouseEnd = mMouseMarkEndX;
            mZoomStep = 0;
            mMin2ZoomMin = mZoomMin - minVal;
            mZoomMax2Max = maxVal - mZoomMax;
            mZoomFixed = mZoomMin + (mZoomMax - mZoomMin) * mMin2ZoomMin
                    / (mMin2ZoomMin + mZoomMax2Max);
            mZoomFixedPixel = (mZoomFixed - minVal) * ppr + LeftMargin;
            mFixedPixelStartDistance = mZoomFixedPixel - LeftMargin;
            mFixedPixelEndDistance = dim.x - RightMargin - mZoomFixedPixel;
            mZoomMin2Fixed = mZoomFixed - mZoomMin;
            mFixed2ZoomMax = mZoomMax - mZoomFixed;
            getDisplay().timerExec(ZOOM_TIMER_INTERVAL, mZoomAnimator);
            redraw();
            update();
        }
        private void mouseDoubleClick(MouseEvent me) {
        }
        public void startScaling(int mouseX) {
            Point dim = mSurface.getSize();
            int x = mouseX;
            if (x < LeftMargin)
                x = LeftMargin;
            if (x > dim.x - RightMargin)
                x = dim.x - RightMargin;
            mMouseMarkStartX = x;
            mGraphicsState = GraphicsState.Scaling;
            mScalePixelsPerRange = mScaleInfo.getPixelsPerRange();
            mScaleMinVal = mScaleInfo.getMinVal();
            mScaleMaxVal = mScaleInfo.getMaxVal();
        }
        public void stopScaling(int mouseX) {
            mGraphicsState = GraphicsState.Normal;
        }
        private void animateHighlight() {
            mHighlightStep += 1;
            if (mHighlightStep >= HIGHLIGHT_STEPS) {
                mFadeColors = false;
                mHighlightStep = 0;
                mCachedEndRow = -1;
            } else {
                mFadeColors = true;
                mShowHighlightName = true;
                highlightHeight = highlightHeights[mHighlightStep];
                getDisplay().timerExec(HIGHLIGHT_TIMER_INTERVAL, mHighlightAnimator);
            }
            redraw();
        }
        private void clearHighlights() {
            mShowHighlightName = false;
            highlightHeight = 0;
            mHighlightMethodData = null;
            mHighlightCall = null;
            mFadeColors = false;
            mHighlightStep = 0;
            mCachedEndRow = -1;
            redraw();
        }
        private void animateZoom() {
            mZoomStep += 1;
            if (mZoomStep > ZOOM_STEPS) {
                mGraphicsState = GraphicsState.Normal;
                mCachedMinVal = mScaleInfo.getMinVal() + 1;
            } else if (mZoomStep == ZOOM_STEPS) {
                mScaleInfo.setMinVal(mZoomMin);
                mScaleInfo.setMaxVal(mZoomMax);
                mMouseMarkStartX = LeftMargin;
                Point dim = getSize();
                mMouseMarkEndX = dim.x - RightMargin;
                mTimescale.setMarkStart(mMouseMarkStartX);
                mTimescale.setMarkEnd(mMouseMarkEndX);
                getDisplay().timerExec(ZOOM_TIMER_INTERVAL, mZoomAnimator);
            } else {
                double fraction = mZoomFractions[mZoomStep];
                mMouseMarkStartX = (int) (mZoomMouseStart - fraction * mMouseStartDistance);
                mMouseMarkEndX = (int) (mZoomMouseEnd + fraction * mMouseEndDistance);
                mTimescale.setMarkStart(mMouseMarkStartX);
                mTimescale.setMarkEnd(mMouseMarkEndX);
                double ppr;
                if (mZoomMin2Fixed >= mFixed2ZoomMax)
                    ppr = (mZoomFixedPixel - mMouseMarkStartX) / mZoomMin2Fixed;
                else
                    ppr = (mMouseMarkEndX - mZoomFixedPixel) / mFixed2ZoomMax;
                double newMin = mZoomFixed - mFixedPixelStartDistance / ppr;
                double newMax = mZoomFixed + mFixedPixelEndDistance / ppr;
                mScaleInfo.setMinVal(newMin);
                mScaleInfo.setMaxVal(newMax);
                getDisplay().timerExec(ZOOM_TIMER_INTERVAL, mZoomAnimator);
            }
            redraw();
        }
        private static final int TotalXMargin = LeftMargin + RightMargin;
        private static final int yMargin = 1; 
        private static final int MinZoomPixelMargin = 10;
        private GraphicsState mGraphicsState = GraphicsState.Normal;
        private Point mMouse = new Point(LeftMargin, 0);
        private int mMouseMarkStartX;
        private int mMouseMarkEndX;
        private boolean mDebug = false;
        private ArrayList<Strip> mStripList = new ArrayList<Strip>();
        private ArrayList<Range> mHighlightExclusive = new ArrayList<Range>();
        private ArrayList<Range> mHighlightInclusive = new ArrayList<Range>();
        private int mMinStripHeight = 2;
        private double mCachedMinVal;
        private double mCachedMaxVal;
        private int mCachedStartRow;
        private int mCachedEndRow;
        private double mScalePixelsPerRange;
        private double mScaleMinVal;
        private double mScaleMaxVal;
        private double mLimitMinVal;
        private double mLimitMaxVal;
        private double mMinDataVal;
        private double mMaxDataVal;
        private Cursor mNormalCursor;
        private Cursor mIncreasingCursor;
        private Cursor mDecreasingCursor;
        private static final int ZOOM_TIMER_INTERVAL = 10;
        private static final int HIGHLIGHT_TIMER_INTERVAL = 50;
        private static final int ZOOM_STEPS = 8; 
        private int highlightHeight = 4;
        private final int[] highlightHeights = { 0, 2, 4, 5, 6, 5, 4, 2, 4, 5,
                6 };
        private final int HIGHLIGHT_STEPS = highlightHeights.length;
        private boolean mFadeColors;
        private boolean mShowHighlightName;
        private double[] mZoomFractions;
        private int mZoomStep;
        private int mZoomMouseStart;
        private int mZoomMouseEnd;
        private int mMouseStartDistance;
        private int mMouseEndDistance;
        private Point mMouseSelect = new Point(0, 0);
        private double mZoomFixed;
        private double mZoomFixedPixel;
        private double mFixedPixelStartDistance;
        private double mFixedPixelEndDistance;
        private double mZoomMin2Fixed;
        private double mMin2ZoomMin;
        private double mFixed2ZoomMax;
        private double mZoomMax2Max;
        private double mZoomMin;
        private double mZoomMax;
        private Runnable mZoomAnimator;
        private Runnable mHighlightAnimator;
        private int mHighlightStep;
    }
    private int computeVisibleRows(int ydim) {
        int offsetY = mScrollOffsetY;
        int spaceNeeded = mNumRows * rowYSpace;
        if (offsetY + ydim > spaceNeeded) {
            offsetY = spaceNeeded - ydim;
            if (offsetY < 0) {
                offsetY = 0;
            }
        }
        mStartRow = offsetY / rowYSpace;
        mEndRow = (offsetY + ydim) / rowYSpace;
        if (mEndRow >= mNumRows) {
            mEndRow = mNumRows - 1;
        }
        return offsetY;
    }
    private void startHighlighting() {
        mSurface.mHighlightStep = 0;
        mSurface.mFadeColors = true;
        mSurface.mCachedEndRow = -1;
        getDisplay().timerExec(0, mSurface.mHighlightAnimator);
    }
    private static class RowData {
        RowData(Row row) {
            mName = row.getName();
            mStack = new ArrayList<Block>();
        }
        public void push(Block block) {
            mStack.add(block);
        }
        public Block top() {
            if (mStack.size() == 0)
                return null;
            return mStack.get(mStack.size() - 1);
        }
        public void pop() {
            if (mStack.size() == 0)
                return;
            mStack.remove(mStack.size() - 1);
        }
        private String mName;
        private int mRank;
        private long mElapsed;
        private long mEndTime;
        private ArrayList<Block> mStack;
    }
    private static class Segment {
        Segment(RowData rowData, Block block, long startTime, long endTime) {
            mRowData = rowData;
            mBlock = block;
            mStartTime = startTime;
            mEndTime = endTime;
        }
        private RowData mRowData;
        private Block mBlock;
        private long mStartTime;
        private long mEndTime;
    }
    private static class Strip {
        Strip(int x, int y, int width, int height, RowData rowData,
                Segment segment, Color color) {
            mX = x;
            mY = y;
            mWidth = width;
            mHeight = height;
            mRowData = rowData;
            mSegment = segment;
            mColor = color;
        }
        int mX;
        int mY;
        int mWidth;
        int mHeight;
        RowData mRowData;
        Segment mSegment;
        Color mColor;
    }
    private static class Pixel {
        public void setFields(int start, double weight, Segment segment,
                Color color, RowData rowData) {
            mStart = start;
            mMaxWeight = weight;
            mSegment = segment;
            mColor = color;
            mRowData = rowData;
        }
        int mStart = -2; 
        double mMaxWeight;
        Segment mSegment;
        Color mColor; 
        RowData mRowData;
    }
    private static class Range {
        Range(int xStart, int width, int y, Color color) {
            mXdim.x = xStart;
            mXdim.y = width;
            mY = y;
            mColor = color;
        }
        Point mXdim = new Point(0, 0);
        int mY;
        Color mColor;
    }
}
