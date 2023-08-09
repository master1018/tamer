public class DefaultEditorKit extends EditorKit {
    public DefaultEditorKit() {
    }
    public String getContentType() {
        return "text/plain";
    }
    public ViewFactory getViewFactory() {
        return null;
    }
    public Action[] getActions() {
        return defaultActions;
    }
    public Caret createCaret() {
        return null;
    }
    public Document createDefaultDocument() {
        return new PlainDocument();
    }
    public void read(InputStream in, Document doc, int pos)
        throws IOException, BadLocationException {
        read(new InputStreamReader(in), doc, pos);
    }
    public void write(OutputStream out, Document doc, int pos, int len)
        throws IOException, BadLocationException {
        OutputStreamWriter osw = new OutputStreamWriter(out);
        write(osw, doc, pos, len);
        osw.flush();
    }
    MutableAttributeSet getInputAttributes() {
        return null;
    }
    public void read(Reader in, Document doc, int pos)
        throws IOException, BadLocationException {
        char[] buff = new char[4096];
        int nch;
        boolean lastWasCR = false;
        boolean isCRLF = false;
        boolean isCR = false;
        int last;
        boolean wasEmpty = (doc.getLength() == 0);
        AttributeSet attr = getInputAttributes();
        while ((nch = in.read(buff, 0, buff.length)) != -1) {
            last = 0;
            for(int counter = 0; counter < nch; counter++) {
                switch(buff[counter]) {
                case '\r':
                    if (lastWasCR) {
                        isCR = true;
                        if (counter == 0) {
                            doc.insertString(pos, "\n", attr);
                            pos++;
                        }
                        else {
                            buff[counter - 1] = '\n';
                        }
                    }
                    else {
                        lastWasCR = true;
                    }
                    break;
                case '\n':
                    if (lastWasCR) {
                        if (counter > (last + 1)) {
                            doc.insertString(pos, new String(buff, last,
                                            counter - last - 1), attr);
                            pos += (counter - last - 1);
                        }
                        lastWasCR = false;
                        last = counter;
                        isCRLF = true;
                    }
                    break;
                default:
                    if (lastWasCR) {
                        isCR = true;
                        if (counter == 0) {
                            doc.insertString(pos, "\n", attr);
                            pos++;
                        }
                        else {
                            buff[counter - 1] = '\n';
                        }
                        lastWasCR = false;
                    }
                    break;
                }
            }
            if (last < nch) {
                if(lastWasCR) {
                    if (last < (nch - 1)) {
                        doc.insertString(pos, new String(buff, last,
                                         nch - last - 1), attr);
                        pos += (nch - last - 1);
                    }
                }
                else {
                    doc.insertString(pos, new String(buff, last,
                                     nch - last), attr);
                    pos += (nch - last);
                }
            }
        }
        if (lastWasCR) {
            doc.insertString(pos, "\n", attr);
            isCR = true;
        }
        if (wasEmpty) {
            if (isCRLF) {
                doc.putProperty(EndOfLineStringProperty, "\r\n");
            }
            else if (isCR) {
                doc.putProperty(EndOfLineStringProperty, "\r");
            }
            else {
                doc.putProperty(EndOfLineStringProperty, "\n");
            }
        }
    }
    public void write(Writer out, Document doc, int pos, int len)
        throws IOException, BadLocationException {
        if ((pos < 0) || ((pos + len) > doc.getLength())) {
            throw new BadLocationException("DefaultEditorKit.write", pos);
        }
        Segment data = new Segment();
        int nleft = len;
        int offs = pos;
        Object endOfLineProperty = doc.getProperty(EndOfLineStringProperty);
        if (endOfLineProperty == null) {
            try {
                endOfLineProperty = System.getProperty("line.separator");
            } catch (SecurityException se) { }
        }
        String endOfLine;
        if (endOfLineProperty instanceof String) {
            endOfLine = (String)endOfLineProperty;
        }
        else {
            endOfLine = null;
        }
        if (endOfLineProperty != null && !endOfLine.equals("\n")) {
            while (nleft > 0) {
                int n = Math.min(nleft, 4096);
                doc.getText(offs, n, data);
                int last = data.offset;
                char[] array = data.array;
                int maxCounter = last + data.count;
                for (int counter = last; counter < maxCounter; counter++) {
                    if (array[counter] == '\n') {
                        if (counter > last) {
                            out.write(array, last, counter - last);
                        }
                        out.write(endOfLine);
                        last = counter + 1;
                    }
                }
                if (maxCounter > last) {
                    out.write(array, last, maxCounter - last);
                }
                offs += n;
                nleft -= n;
            }
        }
        else {
            while (nleft > 0) {
                int n = Math.min(nleft, 4096);
                doc.getText(offs, n, data);
                out.write(data.array, data.offset, data.count);
                offs += n;
                nleft -= n;
            }
        }
        out.flush();
    }
    public static final String EndOfLineStringProperty = "__EndOfLine__";
    public static final String insertContentAction = "insert-content";
    public static final String insertBreakAction = "insert-break";
    public static final String insertTabAction = "insert-tab";
    public static final String deletePrevCharAction = "delete-previous";
    public static final String deleteNextCharAction = "delete-next";
    public static final String deleteNextWordAction = "delete-next-word";
    public static final String deletePrevWordAction = "delete-previous-word";
    public static final String readOnlyAction = "set-read-only";
    public static final String writableAction = "set-writable";
    public static final String cutAction = "cut-to-clipboard";
    public static final String copyAction = "copy-to-clipboard";
    public static final String pasteAction = "paste-from-clipboard";
    public static final String beepAction = "beep";
    public static final String pageUpAction = "page-up";
    public static final String pageDownAction = "page-down";
     static final String selectionPageUpAction = "selection-page-up";
     static final String selectionPageDownAction = "selection-page-down";
     static final String selectionPageLeftAction = "selection-page-left";
     static final String selectionPageRightAction = "selection-page-right";
    public static final String forwardAction = "caret-forward";
    public static final String backwardAction = "caret-backward";
    public static final String selectionForwardAction = "selection-forward";
    public static final String selectionBackwardAction = "selection-backward";
    public static final String upAction = "caret-up";
    public static final String downAction = "caret-down";
    public static final String selectionUpAction = "selection-up";
    public static final String selectionDownAction = "selection-down";
    public static final String beginWordAction = "caret-begin-word";
    public static final String endWordAction = "caret-end-word";
    public static final String selectionBeginWordAction = "selection-begin-word";
    public static final String selectionEndWordAction = "selection-end-word";
    public static final String previousWordAction = "caret-previous-word";
    public static final String nextWordAction = "caret-next-word";
    public static final String selectionPreviousWordAction = "selection-previous-word";
    public static final String selectionNextWordAction = "selection-next-word";
    public static final String beginLineAction = "caret-begin-line";
    public static final String endLineAction = "caret-end-line";
    public static final String selectionBeginLineAction = "selection-begin-line";
    public static final String selectionEndLineAction = "selection-end-line";
    public static final String beginParagraphAction = "caret-begin-paragraph";
    public static final String endParagraphAction = "caret-end-paragraph";
    public static final String selectionBeginParagraphAction = "selection-begin-paragraph";
    public static final String selectionEndParagraphAction = "selection-end-paragraph";
    public static final String beginAction = "caret-begin";
    public static final String endAction = "caret-end";
    public static final String selectionBeginAction = "selection-begin";
    public static final String selectionEndAction = "selection-end";
    public static final String selectWordAction = "select-word";
    public static final String selectLineAction = "select-line";
    public static final String selectParagraphAction = "select-paragraph";
    public static final String selectAllAction = "select-all";
     static final String unselectAction = "unselect";
     static final String toggleComponentOrientationAction
        = "toggle-componentOrientation";
    public static final String defaultKeyTypedAction = "default-typed";
    private static final Action[] defaultActions = {
        new InsertContentAction(), new DeletePrevCharAction(),
        new DeleteNextCharAction(), new ReadOnlyAction(),
        new DeleteWordAction(deletePrevWordAction),
        new DeleteWordAction(deleteNextWordAction),
        new WritableAction(), new CutAction(),
        new CopyAction(), new PasteAction(),
        new VerticalPageAction(pageUpAction, -1, false),
        new VerticalPageAction(pageDownAction, 1, false),
        new VerticalPageAction(selectionPageUpAction, -1, true),
        new VerticalPageAction(selectionPageDownAction, 1, true),
        new PageAction(selectionPageLeftAction, true, true),
        new PageAction(selectionPageRightAction, false, true),
        new InsertBreakAction(), new BeepAction(),
        new NextVisualPositionAction(forwardAction, false,
                                     SwingConstants.EAST),
        new NextVisualPositionAction(backwardAction, false,
                                     SwingConstants.WEST),
        new NextVisualPositionAction(selectionForwardAction, true,
                                     SwingConstants.EAST),
        new NextVisualPositionAction(selectionBackwardAction, true,
                                     SwingConstants.WEST),
        new NextVisualPositionAction(upAction, false,
                                     SwingConstants.NORTH),
        new NextVisualPositionAction(downAction, false,
                                     SwingConstants.SOUTH),
        new NextVisualPositionAction(selectionUpAction, true,
                                     SwingConstants.NORTH),
        new NextVisualPositionAction(selectionDownAction, true,
                                     SwingConstants.SOUTH),
        new BeginWordAction(beginWordAction, false),
        new EndWordAction(endWordAction, false),
        new BeginWordAction(selectionBeginWordAction, true),
        new EndWordAction(selectionEndWordAction, true),
        new PreviousWordAction(previousWordAction, false),
        new NextWordAction(nextWordAction, false),
        new PreviousWordAction(selectionPreviousWordAction, true),
        new NextWordAction(selectionNextWordAction, true),
        new BeginLineAction(beginLineAction, false),
        new EndLineAction(endLineAction, false),
        new BeginLineAction(selectionBeginLineAction, true),
        new EndLineAction(selectionEndLineAction, true),
        new BeginParagraphAction(beginParagraphAction, false),
        new EndParagraphAction(endParagraphAction, false),
        new BeginParagraphAction(selectionBeginParagraphAction, true),
        new EndParagraphAction(selectionEndParagraphAction, true),
        new BeginAction(beginAction, false),
        new EndAction(endAction, false),
        new BeginAction(selectionBeginAction, true),
        new EndAction(selectionEndAction, true),
        new DefaultKeyTypedAction(), new InsertTabAction(),
        new SelectWordAction(), new SelectLineAction(),
        new SelectParagraphAction(), new SelectAllAction(),
        new UnselectAction(), new ToggleComponentOrientationAction(),
        new DumpModelAction()
    };
    public static class DefaultKeyTypedAction extends TextAction {
        public DefaultKeyTypedAction() {
            super(defaultKeyTypedAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if ((target != null) && (e != null)) {
                if ((! target.isEditable()) || (! target.isEnabled())) {
                    return;
                }
                String content = e.getActionCommand();
                int mod = e.getModifiers();
                if ((content != null) && (content.length() > 0) &&
                    ((mod & ActionEvent.ALT_MASK) == (mod & ActionEvent.CTRL_MASK))) {
                    char c = content.charAt(0);
                    if ((c >= 0x20) && (c != 0x7F)) {
                        target.replaceSelection(content);
                    }
                }
            }
        }
    }
    public static class InsertContentAction extends TextAction {
        public InsertContentAction() {
            super(insertContentAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if ((target != null) && (e != null)) {
                if ((! target.isEditable()) || (! target.isEnabled())) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                String content = e.getActionCommand();
                if (content != null) {
                    target.replaceSelection(content);
                } else {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }
    public static class InsertBreakAction extends TextAction {
        public InsertBreakAction() {
            super(insertBreakAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                if ((! target.isEditable()) || (! target.isEnabled())) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                target.replaceSelection("\n");
            }
        }
    }
    public static class InsertTabAction extends TextAction {
        public InsertTabAction() {
            super(insertTabAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                if ((! target.isEditable()) || (! target.isEnabled())) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                target.replaceSelection("\t");
            }
        }
    }
    static class DeletePrevCharAction extends TextAction {
        DeletePrevCharAction() {
            super(deletePrevCharAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            boolean beep = true;
            if ((target != null) && (target.isEditable())) {
                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    } else if (dot > 0) {
                        int delChars = 1;
                        if (dot > 1) {
                            String dotChars = doc.getText(dot - 2, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);
                            if (c0 >= '\uD800' && c0 <= '\uDBFF' &&
                                c1 >= '\uDC00' && c1 <= '\uDFFF') {
                                delChars = 2;
                            }
                        }
                        doc.remove(dot - delChars, delChars);
                        beep = false;
                    }
                } catch (BadLocationException bl) {
                }
            }
            if (beep) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }
    static class DeleteNextCharAction extends TextAction {
        DeleteNextCharAction() {
            super(deleteNextCharAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            boolean beep = true;
            if ((target != null) && (target.isEditable())) {
                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    } else if (dot < doc.getLength()) {
                        int delChars = 1;
                        if (dot < doc.getLength() - 1) {
                            String dotChars = doc.getText(dot, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);
                            if (c0 >= '\uD800' && c0 <= '\uDBFF' &&
                                c1 >= '\uDC00' && c1 <= '\uDFFF') {
                                delChars = 2;
                            }
                        }
                        doc.remove(dot, delChars);
                        beep = false;
                    }
                } catch (BadLocationException bl) {
                }
            }
            if (beep) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }
    static class DeleteWordAction extends TextAction {
        DeleteWordAction(String name) {
            super(name);
            assert (name == deletePrevWordAction)
                || (name == deleteNextWordAction);
        }
        public void actionPerformed(ActionEvent e) {
            final JTextComponent target = getTextComponent(e);
            if ((target != null) && (e != null)) {
                if ((! target.isEditable()) || (! target.isEnabled())) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                boolean beep = true;
                try {
                    final int start = target.getSelectionStart();
                    final Element line =
                        Utilities.getParagraphElement(target, start);
                    int end;
                    if (deleteNextWordAction == getValue(Action.NAME)) {
                        end = Utilities.
                            getNextWordInParagraph(target, line, start, false);
                        if (end == java.text.BreakIterator.DONE) {
                            final int endOfLine = line.getEndOffset();
                            if (start == endOfLine - 1) {
                                end = endOfLine;
                            } else {
                                end = endOfLine - 1;
                            }
                        }
                    } else {
                        end = Utilities.
                            getPrevWordInParagraph(target, line, start);
                        if (end == java.text.BreakIterator.DONE) {
                            final int startOfLine = line.getStartOffset();
                            if (start == startOfLine) {
                                end = startOfLine - 1;
                            } else {
                                end = startOfLine;
                            }
                        }
                    }
                    int offs = Math.min(start, end);
                    int len = Math.abs(end - start);
                    if (offs >= 0) {
                        target.getDocument().remove(offs, len);
                        beep = false;
                    }
                } catch (BadLocationException ignore) {
                }
                if (beep) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
    }
    static class ReadOnlyAction extends TextAction {
        ReadOnlyAction() {
            super(readOnlyAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.setEditable(false);
            }
        }
    }
    static class WritableAction extends TextAction {
        WritableAction() {
            super(writableAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.setEditable(true);
            }
        }
    }
    public static class CutAction extends TextAction {
        public CutAction() {
            super(cutAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.cut();
            }
        }
    }
    public static class CopyAction extends TextAction {
        public CopyAction() {
            super(copyAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.copy();
            }
        }
    }
    public static class PasteAction extends TextAction {
        public PasteAction() {
            super(pasteAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.paste();
            }
        }
    }
    public static class BeepAction extends TextAction {
        public BeepAction() {
            super(beepAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            UIManager.getLookAndFeel().provideErrorFeedback(target);
        }
    }
    static class VerticalPageAction extends TextAction {
        public VerticalPageAction(String nm, int direction, boolean select) {
            super(nm);
            this.select = select;
            this.direction = direction;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Rectangle visible = target.getVisibleRect();
                Rectangle newVis = new Rectangle(visible);
                int selectedIndex = target.getCaretPosition();
                int scrollAmount = direction *
                        target.getScrollableBlockIncrement(
                                  visible, SwingConstants.VERTICAL, direction);
                int initialY = visible.y;
                Caret caret = target.getCaret();
                Point magicPosition = caret.getMagicCaretPosition();
                if (selectedIndex != -1) {
                    try {
                        Rectangle dotBounds = target.modelToView(
                                                     selectedIndex);
                        int x = (magicPosition != null) ? magicPosition.x :
                                                          dotBounds.x;
                        int h = dotBounds.height;
                        if (h > 0) {
                            scrollAmount = scrollAmount / h * h;
                        }
                        newVis.y = constrainY(target,
                                initialY + scrollAmount, visible.height);
                        int newIndex;
                        if (visible.contains(dotBounds.x, dotBounds.y)) {
                            newIndex = target.viewToModel(
                                new Point(x, constrainY(target,
                                          dotBounds.y + scrollAmount, 0)));
                        }
                        else {
                            if (direction == -1) {
                                newIndex = target.viewToModel(new Point(
                                    x, newVis.y));
                            }
                            else {
                                newIndex = target.viewToModel(new Point(
                                    x, newVis.y + visible.height));
                            }
                        }
                        newIndex = constrainOffset(target, newIndex);
                        if (newIndex != selectedIndex) {
                            int newY = getAdjustedY(target, newVis, newIndex);
                            if (direction == -1 && newY <= initialY || direction == 1 && newY >= initialY) {
                                newVis.y = newY;
                                if (select) {
                                    target.moveCaretPosition(newIndex);
                                } else {
                                    target.setCaretPosition(newIndex);
                                }
                            }
                        }
                    } catch (BadLocationException ble) { }
                } else {
                    newVis.y = constrainY(target,
                            initialY + scrollAmount, visible.height);
                }
                if (magicPosition != null) {
                    caret.setMagicCaretPosition(magicPosition);
                }
                target.scrollRectToVisible(newVis);
            }
        }
        private int constrainY(JTextComponent target, int y, int vis) {
            if (y < 0) {
                y = 0;
            }
            else if (y + vis > target.getHeight()) {
                y = Math.max(0, target.getHeight() - vis);
            }
            return y;
        }
        private int constrainOffset(JTextComponent text, int offset) {
            Document doc = text.getDocument();
            if ((offset != 0) && (offset > doc.getLength())) {
                offset = doc.getLength();
            }
            if (offset  < 0) {
                offset = 0;
            }
            return offset;
        }
        private int getAdjustedY(JTextComponent text, Rectangle visible, int index) {
            int result = visible.y;
            try {
                Rectangle dotBounds = text.modelToView(index);
                if (dotBounds.y < visible.y) {
                    result = dotBounds.y;
                } else {
                    if ((dotBounds.y > visible.y + visible.height) ||
                            (dotBounds.y + dotBounds.height > visible.y + visible.height)) {
                        result = dotBounds.y + dotBounds.height - visible.height;
                    }
                }
            } catch (BadLocationException ble) {
            }
            return result;
        }
        private boolean select;
        private int direction;
    }
    static class PageAction extends TextAction {
        public PageAction(String nm, boolean left, boolean select) {
            super(nm);
            this.select = select;
            this.left = left;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                int selectedIndex;
                Rectangle visible = new Rectangle();
                target.computeVisibleRect(visible);
                if (left) {
                    visible.x = Math.max(0, visible.x - visible.width);
                }
                else {
                    visible.x += visible.width;
                }
                selectedIndex = target.getCaretPosition();
                if(selectedIndex != -1) {
                    if (left) {
                        selectedIndex = target.viewToModel
                            (new Point(visible.x, visible.y));
                    }
                    else {
                        selectedIndex = target.viewToModel
                            (new Point(visible.x + visible.width - 1,
                                       visible.y + visible.height - 1));
                    }
                    Document doc = target.getDocument();
                    if ((selectedIndex != 0) &&
                        (selectedIndex  > (doc.getLength()-1))) {
                        selectedIndex = doc.getLength()-1;
                    }
                    else if(selectedIndex  < 0) {
                        selectedIndex = 0;
                    }
                    if (select)
                        target.moveCaretPosition(selectedIndex);
                    else
                        target.setCaretPosition(selectedIndex);
                }
            }
        }
        private boolean select;
        private boolean left;
    }
    static class DumpModelAction extends TextAction {
        DumpModelAction() {
            super("dump-model");
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Document d = target.getDocument();
                if (d instanceof AbstractDocument) {
                    ((AbstractDocument) d).dump(System.err);
                }
            }
        }
    }
    static class NextVisualPositionAction extends TextAction {
        NextVisualPositionAction(String nm, boolean select, int direction) {
            super(nm);
            this.select = select;
            this.direction = direction;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Caret caret = target.getCaret();
                DefaultCaret bidiCaret = (caret instanceof DefaultCaret) ?
                                              (DefaultCaret)caret : null;
                int dot = caret.getDot();
                Position.Bias[] bias = new Position.Bias[1];
                Point magicPosition = caret.getMagicCaretPosition();
                try {
                    if(magicPosition == null &&
                       (direction == SwingConstants.NORTH ||
                        direction == SwingConstants.SOUTH)) {
                        Rectangle r = (bidiCaret != null) ?
                                target.getUI().modelToView(target, dot,
                                                      bidiCaret.getDotBias()) :
                                target.modelToView(dot);
                        magicPosition = new Point(r.x, r.y);
                    }
                    NavigationFilter filter = target.getNavigationFilter();
                    if (filter != null) {
                        dot = filter.getNextVisualPositionFrom
                                     (target, dot, (bidiCaret != null) ?
                                      bidiCaret.getDotBias() :
                                      Position.Bias.Forward, direction, bias);
                    }
                    else {
                        dot = target.getUI().getNextVisualPositionFrom
                                     (target, dot, (bidiCaret != null) ?
                                      bidiCaret.getDotBias() :
                                      Position.Bias.Forward, direction, bias);
                    }
                    if(bias[0] == null) {
                        bias[0] = Position.Bias.Forward;
                    }
                    if(bidiCaret != null) {
                        if (select) {
                            bidiCaret.moveDot(dot, bias[0]);
                        } else {
                            bidiCaret.setDot(dot, bias[0]);
                        }
                    }
                    else {
                        if (select) {
                            caret.moveDot(dot);
                        } else {
                            caret.setDot(dot);
                        }
                    }
                    if(magicPosition != null &&
                       (direction == SwingConstants.NORTH ||
                        direction == SwingConstants.SOUTH)) {
                        target.getCaret().setMagicCaretPosition(magicPosition);
                    }
                } catch (BadLocationException ex) {
                }
            }
        }
        private boolean select;
        private int direction;
    }
    static class BeginWordAction extends TextAction {
        BeginWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int begOffs = Utilities.getWordStart(target, offs);
                    if (select) {
                        target.moveCaretPosition(begOffs);
                    } else {
                        target.setCaretPosition(begOffs);
                    }
                } catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
        private boolean select;
    }
    static class EndWordAction extends TextAction {
        EndWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int endOffs = Utilities.getWordEnd(target, offs);
                    if (select) {
                        target.moveCaretPosition(endOffs);
                    } else {
                        target.setCaretPosition(endOffs);
                    }
                } catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
        private boolean select;
    }
    static class PreviousWordAction extends TextAction {
        PreviousWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                boolean failed = false;
                try {
                    Element curPara =
                            Utilities.getParagraphElement(target, offs);
                    offs = Utilities.getPreviousWord(target, offs);
                    if(offs < curPara.getStartOffset()) {
                        offs = Utilities.getParagraphElement(target, offs).
                                getEndOffset() - 1;
                    }
                } catch (BadLocationException bl) {
                    if (offs != 0) {
                        offs = 0;
                    }
                    else {
                        failed = true;
                    }
                }
                if (!failed) {
                    if (select) {
                        target.moveCaretPosition(offs);
                    } else {
                        target.setCaretPosition(offs);
                    }
                }
                else {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
        private boolean select;
    }
    static class NextWordAction extends TextAction {
        NextWordAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                boolean failed = false;
                int oldOffs = offs;
                Element curPara =
                        Utilities.getParagraphElement(target, offs);
                try {
                    offs = Utilities.getNextWord(target, offs);
                    if(offs >= curPara.getEndOffset() &&
                            oldOffs != curPara.getEndOffset() - 1) {
                        offs = curPara.getEndOffset() - 1;
                    }
                } catch (BadLocationException bl) {
                    int end = target.getDocument().getLength();
                    if (offs != end) {
                        if(oldOffs != curPara.getEndOffset() - 1) {
                            offs = curPara.getEndOffset() - 1;
                        } else {
                        offs = end;
                    }
                    }
                    else {
                        failed = true;
                    }
                }
                if (!failed) {
                    if (select) {
                        target.moveCaretPosition(offs);
                    } else {
                        target.setCaretPosition(offs);
                    }
                }
                else {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
        private boolean select;
    }
    static class BeginLineAction extends TextAction {
        BeginLineAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int begOffs = Utilities.getRowStart(target, offs);
                    if (select) {
                        target.moveCaretPosition(begOffs);
                    } else {
                        target.setCaretPosition(begOffs);
                    }
                } catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
        private boolean select;
    }
    static class EndLineAction extends TextAction {
        EndLineAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int endOffs = Utilities.getRowEnd(target, offs);
                    if (select) {
                        target.moveCaretPosition(endOffs);
                    } else {
                        target.setCaretPosition(endOffs);
                    }
                } catch (BadLocationException bl) {
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }
            }
        }
        private boolean select;
    }
    static class BeginParagraphAction extends TextAction {
        BeginParagraphAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                Element elem = Utilities.getParagraphElement(target, offs);
                offs = elem.getStartOffset();
                if (select) {
                    target.moveCaretPosition(offs);
                } else {
                    target.setCaretPosition(offs);
                }
            }
        }
        private boolean select;
    }
    static class EndParagraphAction extends TextAction {
        EndParagraphAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                int offs = target.getCaretPosition();
                Element elem = Utilities.getParagraphElement(target, offs);
                offs = Math.min(target.getDocument().getLength(),
                                elem.getEndOffset());
                if (select) {
                    target.moveCaretPosition(offs);
                } else {
                    target.setCaretPosition(offs);
                }
            }
        }
        private boolean select;
    }
    static class BeginAction extends TextAction {
        BeginAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                if (select) {
                    target.moveCaretPosition(0);
                } else {
                    target.setCaretPosition(0);
                }
            }
        }
        private boolean select;
    }
    static class EndAction extends TextAction {
        EndAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                int dot = doc.getLength();
                if (select) {
                    target.moveCaretPosition(dot);
                } else {
                    target.setCaretPosition(dot);
                }
            }
        }
        private boolean select;
    }
    static class SelectWordAction extends TextAction {
        SelectWordAction() {
            super(selectWordAction);
            start = new BeginWordAction("pigdog", false);
            end = new EndWordAction("pigdog", true);
        }
        public void actionPerformed(ActionEvent e) {
            start.actionPerformed(e);
            end.actionPerformed(e);
        }
        private Action start;
        private Action end;
    }
    static class SelectLineAction extends TextAction {
        SelectLineAction() {
            super(selectLineAction);
            start = new BeginLineAction("pigdog", false);
            end = new EndLineAction("pigdog", true);
        }
        public void actionPerformed(ActionEvent e) {
            start.actionPerformed(e);
            end.actionPerformed(e);
        }
        private Action start;
        private Action end;
    }
    static class SelectParagraphAction extends TextAction {
        SelectParagraphAction() {
            super(selectParagraphAction);
            start = new BeginParagraphAction("pigdog", false);
            end = new EndParagraphAction("pigdog", true);
        }
        public void actionPerformed(ActionEvent e) {
            start.actionPerformed(e);
            end.actionPerformed(e);
        }
        private Action start;
        private Action end;
    }
    static class SelectAllAction extends TextAction {
        SelectAllAction() {
            super(selectAllAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                target.setCaretPosition(0);
                target.moveCaretPosition(doc.getLength());
            }
        }
    }
    static class UnselectAction extends TextAction {
        UnselectAction() {
            super(unselectAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                target.setCaretPosition(target.getCaretPosition());
            }
        }
    }
    static class ToggleComponentOrientationAction extends TextAction {
        ToggleComponentOrientationAction() {
            super(toggleComponentOrientationAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                ComponentOrientation last = target.getComponentOrientation();
                ComponentOrientation next;
                if( last == ComponentOrientation.RIGHT_TO_LEFT )
                    next = ComponentOrientation.LEFT_TO_RIGHT;
                else
                    next = ComponentOrientation.RIGHT_TO_LEFT;
                target.setComponentOrientation(next);
                target.repaint();
            }
        }
    }
}
