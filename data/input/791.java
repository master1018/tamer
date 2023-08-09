public class SAEditorPane extends JEditorPane {
  public SAEditorPane() {
    setEditable(false);
    setContentType("text/html");
  }
  public String getSelectedText() {
    StringBuffer result = new StringBuffer();
    Document doc = getDocument();
    int start = getSelectionStart();
    int end = getSelectionEnd();
    try {
      ElementIterator it = new ElementIterator(doc.getDefaultRootElement());
      Element e;
      String separator = System.getProperty("line.separator");
      while ((e = it.next()) != null) {
        if (e.isLeaf()) {
          int rangeStart = e.getStartOffset();
          int rangeEnd = e.getEndOffset();
          if (rangeEnd < start || rangeStart > end) continue;
          if (end < rangeEnd) rangeEnd = end;
          if (start > rangeStart) rangeStart = start;
          try {
            String line = getText(rangeStart, rangeEnd-rangeStart);
            if (e.getName().equals("br"))
              result.append(separator);
            else
              result.append(line);
          } catch (BadLocationException ex) {
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result.toString();
  }
  public void setText(String text) {
    super.setText(text);
    setCaretPosition(0);
  }
}
