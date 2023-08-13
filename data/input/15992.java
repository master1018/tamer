public class SourceCodePanel extends JPanel {
  private JTextArea source;
  private RowHeader header;
  private String filename;
  private static final int LINE_NO_SPACE = 4;
  private static final int ICON_SIZE = 12;
  private static Icon topFrameCurLine;
  private static Icon lowerFrameCurLine;
  private static Icon breakpoint;
  private int highlightedLine = -1;
  private Set breakpoints = new HashSet(); 
  private EditorCommands comm;
  private Editor parent;
  class RowHeader extends JPanel {
    private JViewport view;
    private boolean   showLineNumbers;
    private int       width;
    private int       rowHeight;
    private boolean   initted;
    public RowHeader() {
      super();
      initted = true;
      addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
          public void ancestorResized(HierarchyEvent e) {
            recomputeSize();
          }
        });
    }
    public void paint(Graphics g) {
      super.paint(g);
      if (getShowLineNumbers()) {
        Rectangle clip = g.getClipBounds();
        int start = clip.y / rowHeight;
        int end   = start + (clip.height + (rowHeight - 1)) / rowHeight;
        FontMetrics fm = getFontMetrics(getFont());
        int ascent = fm.getMaxAscent(); 
        for (int i = start; i <= end; i++) {
          String str = Integer.toString(i + 1);
          int strWidth = GraphicsUtilities.getStringWidth(str, fm);
          g.drawString(str, width - strWidth - LINE_NO_SPACE, ascent + rowHeight * i);
          if (breakpoints.contains(new Integer(i))) {
            breakpoint.paintIcon(this, g, LINE_NO_SPACE, rowHeight * i);
          }
          if (i == highlightedLine) {
            topFrameCurLine.paintIcon(this, g, LINE_NO_SPACE, rowHeight * i);
          }
        }
      }
    }
    public boolean getShowLineNumbers() {
      return showLineNumbers;
    }
    public void setShowLineNumbers(boolean val) {
      if (val != showLineNumbers) {
        showLineNumbers = val;
        recomputeSize();
        invalidate();
        validate();
      }
    }
    public void setFont(Font f) {
      super.setFont(f);
      rowHeight = getFontMetrics(f).getHeight();
      recomputeSize();
    }
    void setViewport(JViewport view) {
      this.view = view;
    }
    void recomputeSize() {
      if (!initted) return;
      if (view == null) return;
      width = ICON_SIZE + 2 * LINE_NO_SPACE;
      try {
        int numLines = 1 + source.getLineOfOffset(source.getDocument().getEndPosition().getOffset() - 1);
        String str = Integer.toString(numLines);
        if (getShowLineNumbers()) {
          width += GraphicsUtilities.getStringWidth(str, getFontMetrics(getFont())) + LINE_NO_SPACE;
        }
        Dimension d = new Dimension(width, numLines * getFontMetrics(getFont()).getHeight());
        setSize(d);
        setPreferredSize(d);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
    }
  }
  public SourceCodePanel() {
    maybeLoadIcons();
    setLayout(new BorderLayout());
    source = new JTextArea();
    source.setEditable(false);
    source.getCaret().setVisible(true);
    header = new RowHeader();
    header.setShowLineNumbers(true);
    JScrollPane scroller = new JScrollPane(source);
    JViewport rowView = new JViewport();
    rowView.setView(header);
    header.setViewport(rowView);
    rowView.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    scroller.setRowHeader(rowView);
    add(scroller, BorderLayout.CENTER);
    setFont(getFont());
    source.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          source.getCaret().setVisible(true);
        }
      });
    source.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_F9) {
            int lineNo = getCurrentLineNumber();
            comm.toggleBreakpointAtLine(parent, lineNo);
          }
        }
      });
  }
  public void setFont(Font f) {
    super.setFont(f);
    if (source != null) {
      source.setFont(f);
    }
    if (header != null) {
      header.setFont(f);
    }
  }
  public boolean getShowLineNumbers() {
    return header.getShowLineNumbers();
  }
  public void setShowLineNumbers(boolean val) {
    header.setShowLineNumbers(val);
  }
  public boolean openFile(String filename) {
    try {
      this.filename = filename;
      File file = new File(filename);
      int len = (int) file.length();
      StringBuffer buf = new StringBuffer(len); 
      char[] tmp = new char[4096];
      FileReader in = new FileReader(file);
      int res = 0;
      do {
        res = in.read(tmp, 0, tmp.length);
        if (res >= 0) {
          buf.append(tmp, 0, res);
        }
      } while (res != -1);
      in.close();
      String text = buf.toString();
      source.setText(text);
      header.recomputeSize();
      return true;
    } catch (IOException e) {
      return false;
    }
  }
  public String getSourceFileName() {
    return filename;
  }
  public int getCurrentLineNumber() {
    try {
      return 1 + source.getLineOfOffset(source.getCaretPosition());
    } catch (BadLocationException e) {
      return 0;
    }
  }
  public void showLineNumber(int lineNo) {
    try {
      int offset = source.getLineStartOffset(lineNo - 1);
      Rectangle rect = source.modelToView(offset);
      if (rect == null) {
        return;
      }
      source.scrollRectToVisible(rect);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }
  public void highlightLineNumber(int lineNo) {
    highlightedLine = lineNo - 1;
  }
  public void showBreakpointAtLine(int lineNo)  { breakpoints.add(new Integer(lineNo - 1));    repaint(); }
  public boolean hasBreakpointAtLine(int lineNo){ return breakpoints.contains(new Integer(lineNo - 1));   }
  public void clearBreakpointAtLine(int lineNo) { breakpoints.remove(new Integer(lineNo - 1)); repaint(); }
  public void clearBreakpoints()                { breakpoints.clear();                         repaint(); }
  public void setEditorCommands(EditorCommands comm, Editor parent) {
    this.comm = comm;
    this.parent = parent;
  }
  public void requestFocus() {
    source.requestFocus();
  }
  private void maybeLoadIcons() {
    if (topFrameCurLine == null) {
      topFrameCurLine   = loadIcon("resources/arrow.png");
      lowerFrameCurLine = loadIcon("resources/triangle.png");
      breakpoint        = loadIcon("resources/breakpoint.png");
    }
  }
  private Icon loadIcon(String which) {
    URL url = getClass().getResource(which);
    return new ImageIcon(url);
  }
}
