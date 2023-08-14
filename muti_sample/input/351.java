public class TextComponentPrintable implements CountingPrintable {
    private static final int LIST_SIZE = 1000;
    private boolean isLayouted = false;
    private final JTextComponent textComponentToPrint;
    private final AtomicReference<FontRenderContext> frc =
        new AtomicReference<FontRenderContext>(null);
    private final JTextComponent printShell;
    private final MessageFormat headerFormat;
    private final MessageFormat footerFormat;
    private static final float HEADER_FONT_SIZE = 18.0f;
    private static final float FOOTER_FONT_SIZE = 12.0f;
    private final Font headerFont;
    private final Font footerFont;
    private final List<IntegerSegment> rowsMetrics;
    private final List<IntegerSegment> pagesMetrics;
    public static Printable getPrintable(final JTextComponent textComponent,
            final MessageFormat headerFormat,
            final MessageFormat footerFormat) {
        if (textComponent instanceof JEditorPane
                && isFrameSetDocument(textComponent.getDocument())) {
            List<JEditorPane> frames = getFrames((JEditorPane) textComponent);
            List<CountingPrintable> printables =
                new ArrayList<CountingPrintable>();
            for (JEditorPane frame : frames) {
                printables.add((CountingPrintable)
                               getPrintable(frame, headerFormat, footerFormat));
            }
            return new CompoundPrintable(printables);
        } else {
            return new TextComponentPrintable(textComponent,
               headerFormat, footerFormat);
        }
    }
    private static boolean isFrameSetDocument(final Document document) {
        boolean ret = false;
        if (document instanceof HTMLDocument) {
            HTMLDocument htmlDocument = (HTMLDocument)document;
            if (htmlDocument.getIterator(HTML.Tag.FRAME).isValid()) {
                ret = true;
            }
        }
        return ret;
    }
    private static List<JEditorPane> getFrames(final JEditorPane editor) {
        List<JEditorPane> list = new ArrayList<JEditorPane>();
        getFrames(editor, list);
        if (list.size() == 0) {
            createFrames(editor);
            getFrames(editor, list);
        }
        return list;
    }
    private static void getFrames(final Container container, List<JEditorPane> list) {
        for (Component c : container.getComponents()) {
            if (c instanceof FrameEditorPaneTag
                && c instanceof JEditorPane ) { 
                list.add((JEditorPane) c);
            } else {
                if (c instanceof Container) {
                    getFrames((Container) c, list);
                }
            }
        }
    }
    private static void createFrames(final JEditorPane editor) {
        Runnable doCreateFrames =
            new Runnable() {
                public void run() {
                    final int WIDTH = 500;
                    final int HEIGHT = 500;
                    CellRendererPane rendererPane = new CellRendererPane();
                    rendererPane.add(editor);
                    rendererPane.setSize(WIDTH, HEIGHT);
                };
            };
        if (SwingUtilities.isEventDispatchThread()) {
            doCreateFrames.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(doCreateFrames);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private TextComponentPrintable(JTextComponent textComponent,
            MessageFormat headerFormat,
            MessageFormat footerFormat) {
        this.textComponentToPrint = textComponent;
        this.headerFormat = headerFormat;
        this.footerFormat = footerFormat;
        headerFont = textComponent.getFont().deriveFont(Font.BOLD,
            HEADER_FONT_SIZE);
        footerFont = textComponent.getFont().deriveFont(Font.PLAIN,
            FOOTER_FONT_SIZE);
        this.pagesMetrics =
            Collections.synchronizedList(new ArrayList<IntegerSegment>());
        this.rowsMetrics = new ArrayList<IntegerSegment>(LIST_SIZE);
        this.printShell = createPrintShell(textComponent);
    }
    private JTextComponent createPrintShell(final JTextComponent textComponent) {
        if (SwingUtilities.isEventDispatchThread()) {
            return createPrintShellOnEDT(textComponent);
        } else {
            FutureTask<JTextComponent> futureCreateShell =
                new FutureTask<JTextComponent>(
                    new Callable<JTextComponent>() {
                        public JTextComponent call() throws Exception {
                            return createPrintShellOnEDT(textComponent);
                        }
                    });
            SwingUtilities.invokeLater(futureCreateShell);
            try {
                return futureCreateShell.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw new AssertionError(cause);
            }
        }
    }
    private JTextComponent createPrintShellOnEDT(final JTextComponent textComponent) {
        assert SwingUtilities.isEventDispatchThread();
        JTextComponent ret = null;
        if (textComponent instanceof JTextField) {
            ret =
                new JTextField() {
                    {
                        setHorizontalAlignment(
                            ((JTextField) textComponent).getHorizontalAlignment());
                    }
                    @Override
                    public FontMetrics getFontMetrics(Font font) {
                        return (frc.get() == null)
                            ? super.getFontMetrics(font)
                            : FontDesignMetrics.getMetrics(font, frc.get());
                    }
                };
        } else if (textComponent instanceof JTextArea) {
            ret =
                new JTextArea() {
                    {
                        JTextArea textArea = (JTextArea) textComponent;
                        setLineWrap(textArea.getLineWrap());
                        setWrapStyleWord(textArea.getWrapStyleWord());
                        setTabSize(textArea.getTabSize());
                    }
                    @Override
                    public FontMetrics getFontMetrics(Font font) {
                        return (frc.get() == null)
                            ? super.getFontMetrics(font)
                            : FontDesignMetrics.getMetrics(font, frc.get());
                    }
                };
        } else if (textComponent instanceof JTextPane) {
            ret =
                new JTextPane() {
                    @Override
                    public FontMetrics getFontMetrics(Font font) {
                        return (frc.get() == null)
                            ? super.getFontMetrics(font)
                            : FontDesignMetrics.getMetrics(font, frc.get());
                    }
                    @Override
                    public EditorKit getEditorKit() {
                        if (getDocument() == textComponent.getDocument()) {
                            return ((JTextPane) textComponent).getEditorKit();
                        } else {
                            return super.getEditorKit();
                        }
                    }
                };
        } else if (textComponent instanceof JEditorPane) {
            ret =
                new JEditorPane() {
                    @Override
                    public FontMetrics getFontMetrics(Font font) {
                        return (frc.get() == null)
                            ? super.getFontMetrics(font)
                            : FontDesignMetrics.getMetrics(font, frc.get());
                    }
                    @Override
                    public EditorKit getEditorKit() {
                        if (getDocument() == textComponent.getDocument()) {
                            return ((JEditorPane) textComponent).getEditorKit();
                        } else {
                            return super.getEditorKit();
                        }
                    }
                };
        }
        ret.setBorder(null);
        ret.setOpaque(textComponent.isOpaque());
        ret.setEditable(textComponent.isEditable());
        ret.setEnabled(textComponent.isEnabled());
        ret.setFont(textComponent.getFont());
        ret.setBackground(textComponent.getBackground());
        ret.setForeground(textComponent.getForeground());
        ret.setComponentOrientation(
            textComponent.getComponentOrientation());
        if (ret instanceof JEditorPane) {
            ret.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                textComponent.getClientProperty(
                JEditorPane.HONOR_DISPLAY_PROPERTIES));
            ret.putClientProperty(JEditorPane.W3C_LENGTH_UNITS,
                textComponent.getClientProperty(JEditorPane.W3C_LENGTH_UNITS));
            ret.putClientProperty("charset",
                textComponent.getClientProperty("charset"));
        }
        ret.setDocument(textComponent.getDocument());
        return ret;
    }
    public int getNumberOfPages() {
        return pagesMetrics.size();
    }
    public int print(final Graphics graphics,
            final PageFormat pf,
            final int pageIndex) throws PrinterException {
        if (!isLayouted) {
            if (graphics instanceof Graphics2D) {
                frc.set(((Graphics2D)graphics).getFontRenderContext());
            }
            layout((int)Math.floor(pf.getImageableWidth()));
            calculateRowsMetrics();
        }
        int ret;
        if (!SwingUtilities.isEventDispatchThread()) {
            Callable<Integer> doPrintOnEDT = new Callable<Integer>() {
                public Integer call() throws Exception {
                    return printOnEDT(graphics, pf, pageIndex);
                }
            };
            FutureTask<Integer> futurePrintOnEDT =
                new FutureTask<Integer>(doPrintOnEDT);
            SwingUtilities.invokeLater(futurePrintOnEDT);
            try {
                ret = futurePrintOnEDT.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof PrinterException) {
                    throw (PrinterException)cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        } else {
            ret = printOnEDT(graphics, pf, pageIndex);
        }
        return ret;
    }
    private int printOnEDT(final Graphics graphics,
            final PageFormat pf,
            final int pageIndex) throws PrinterException {
        assert SwingUtilities.isEventDispatchThread();
        Border border = BorderFactory.createEmptyBorder();
        if (headerFormat != null || footerFormat != null) {
            Object[] formatArg = new Object[]{Integer.valueOf(pageIndex + 1)};
            if (headerFormat != null) {
                border = new TitledBorder(border,
                    headerFormat.format(formatArg),
                    TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
                    headerFont, printShell.getForeground());
            }
            if (footerFormat != null) {
                border = new TitledBorder(border,
                    footerFormat.format(formatArg),
                    TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM,
                    footerFont, printShell.getForeground());
            }
        }
        Insets borderInsets = border.getBorderInsets(printShell);
        updatePagesMetrics(pageIndex,
            (int)Math.floor(pf.getImageableHeight()) - borderInsets.top
                           - borderInsets.bottom);
        if (pagesMetrics.size() <= pageIndex) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D)graphics.create();
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        border.paintBorder(printShell, g2d, 0, 0,
            (int)Math.floor(pf.getImageableWidth()),
            (int)Math.floor(pf.getImageableHeight()));
        g2d.translate(0, borderInsets.top);
        Rectangle clip = new Rectangle(0, 0,
            (int) pf.getWidth(),
            pagesMetrics.get(pageIndex).end
                - pagesMetrics.get(pageIndex).start + 1);
        g2d.clip(clip);
        int xStart = 0;
        if (ComponentOrientation.RIGHT_TO_LEFT ==
                printShell.getComponentOrientation()) {
            xStart = (int) pf.getImageableWidth() - printShell.getWidth();
        }
        g2d.translate(xStart, - pagesMetrics.get(pageIndex).start);
        printShell.print(g2d);
        g2d.dispose();
        return Printable.PAGE_EXISTS;
    }
    private boolean needReadLock = false;
    private void releaseReadLock() {
        assert ! SwingUtilities.isEventDispatchThread();
        Document document = textComponentToPrint.getDocument();
        if (document instanceof AbstractDocument) {
            try {
                ((AbstractDocument) document).readUnlock();
                needReadLock = true;
            } catch (Error ignore) {
            }
        }
    }
    private void acquireReadLock() {
        assert ! SwingUtilities.isEventDispatchThread();
        if (needReadLock) {
            try {
                SwingUtilities.invokeAndWait(
                    new Runnable() {
                        public void run() {
                        }
                    });
            } catch (InterruptedException ignore) {
            } catch (java.lang.reflect.InvocationTargetException ignore) {
            }
            Document document = textComponentToPrint.getDocument();
            ((AbstractDocument) document).readLock();
            needReadLock = false;
        }
    }
    private void layout(final int width) {
        if (!SwingUtilities.isEventDispatchThread()) {
            Callable<Object> doLayoutOnEDT = new Callable<Object>() {
                public Object call() throws Exception {
                    layoutOnEDT(width);
                    return null;
                }
            };
            FutureTask<Object> futureLayoutOnEDT = new FutureTask<Object>(
                doLayoutOnEDT);
            releaseReadLock();
            SwingUtilities.invokeLater(futureLayoutOnEDT);
            try {
                futureLayoutOnEDT.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            } finally {
                acquireReadLock();
            }
        } else {
            layoutOnEDT(width);
        }
        isLayouted = true;
    }
    private void layoutOnEDT(final int width) {
        assert SwingUtilities.isEventDispatchThread();
        final int HUGE_INTEGER = Integer.MAX_VALUE - 1000;
        CellRendererPane rendererPane = new CellRendererPane();
        JViewport viewport = new JViewport();
        viewport.setBorder(null);
        Dimension size = new Dimension(width, HUGE_INTEGER);
        if (printShell instanceof JTextField) {
            size =
                new Dimension(size.width, printShell.getPreferredSize().height);
        }
        printShell.setSize(size);
        viewport.setComponentOrientation(printShell.getComponentOrientation());
        viewport.setSize(size);
        viewport.add(printShell);
        rendererPane.add(viewport);
    }
    private void updatePagesMetrics(final int pageIndex, final int pageHeight) {
        while (pageIndex >= pagesMetrics.size() && !rowsMetrics.isEmpty()) {
            int lastPage = pagesMetrics.size() - 1;
            int pageStart = (lastPage >= 0)
               ? pagesMetrics.get(lastPage).end + 1
               : 0;
            int rowIndex;
            for (rowIndex = 0;
                   rowIndex < rowsMetrics.size()
                   && (rowsMetrics.get(rowIndex).end - pageStart + 1)
                     <= pageHeight;
                   rowIndex++) {
            }
            if (rowIndex == 0) {
                pagesMetrics.add(
                    new IntegerSegment(pageStart, pageStart + pageHeight - 1));
            } else {
                rowIndex--;
                pagesMetrics.add(new IntegerSegment(pageStart,
                    rowsMetrics.get(rowIndex).end));
                for (int i = 0; i <= rowIndex; i++) {
                    rowsMetrics.remove(0);
                }
            }
        }
    }
    private void calculateRowsMetrics() {
        final int documentLength = printShell.getDocument().getLength();
        List<IntegerSegment> documentMetrics = new ArrayList<IntegerSegment>(LIST_SIZE);
        Rectangle rect;
        for (int i = 0, previousY = -1, previousHeight = -1; i < documentLength;
                 i++) {
            try {
                rect = printShell.modelToView(i);
                if (rect != null) {
                    int y = (int) rect.getY();
                    int height = (int) rect.getHeight();
                    if (height != 0
                            && (y != previousY || height != previousHeight)) {
                        previousY = y;
                        previousHeight = height;
                        documentMetrics.add(new IntegerSegment(y, y + height - 1));
                    }
                }
            } catch (BadLocationException e) {
                assert false;
            }
        }
        Collections.sort(documentMetrics);
        int yStart = Integer.MIN_VALUE;
        int yEnd = Integer.MIN_VALUE;
        for (IntegerSegment segment : documentMetrics) {
            if (yEnd < segment.start) {
                if (yEnd != Integer.MIN_VALUE) {
                    rowsMetrics.add(new IntegerSegment(yStart, yEnd));
                }
                yStart = segment.start;
                yEnd = segment.end;
            } else {
                yEnd = segment.end;
            }
        }
        if (yEnd != Integer.MIN_VALUE) {
            rowsMetrics.add(new IntegerSegment(yStart, yEnd));
        }
    }
    private static class IntegerSegment implements Comparable<IntegerSegment> {
        final int start;
        final int end;
        IntegerSegment(int start, int end) {
            this.start = start;
            this.end = end;
        }
        public int compareTo(IntegerSegment object) {
            int startsDelta = start - object.start;
            return (startsDelta != 0) ? startsDelta : end - object.end;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IntegerSegment) {
                return compareTo((IntegerSegment) obj) == 0;
            } else {
                return false;
            }
        }
        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + start;
            result = 37 * result + end;
            return result;
        }
        @Override
        public String toString() {
            return "IntegerSegment [" + start + ", " + end + "]";
        }
    }
}
