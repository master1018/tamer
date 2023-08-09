public final class ContentModel implements Serializable {
    public int type;
    public Object content;
    public ContentModel next;
    public ContentModel() {
    }
    public ContentModel(Element content) {
        this(0, content, null);
    }
    public ContentModel(int type, ContentModel content) {
        this(type, content, null);
    }
    public ContentModel(int type, Object content, ContentModel next) {
        this.type = type;
        this.content = content;
        this.next = next;
    }
    public boolean empty() {
        switch (type) {
          case '*':
          case '?':
            return true;
          case '+':
          case '|':
            for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
                if (m.empty()) {
                    return true;
                }
            }
            return false;
          case ',':
          case '&':
            for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
                if (!m.empty()) {
                    return false;
                }
            }
            return true;
          default:
            return false;
        }
    }
     public void getElements(Vector<Element> elemVec) {
         switch (type) {
         case '*':
         case '?':
         case '+':
             ((ContentModel)content).getElements(elemVec);
             break;
         case ',':
         case '|':
         case '&':
             for (ContentModel m=(ContentModel)content; m != null; m=m.next){
                 m.getElements(elemVec);
             }
             break;
         default:
             elemVec.addElement((Element)content);
         }
     }
     private boolean valSet[];
     private boolean val[];
    public boolean first(Object token) {
        switch (type) {
          case '*':
          case '?':
          case '+':
            return ((ContentModel)content).first(token);
          case ',':
            for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
                if (m.first(token)) {
                    return true;
                }
                if (!m.empty()) {
                    return false;
                }
            }
            return false;
          case '|':
          case '&': {
            Element e = (Element) token;
            if (valSet == null) {
                valSet = new boolean[Element.maxIndex + 1];
                val = new boolean[Element.maxIndex + 1];
            }
            if (valSet[e.index]) {
                return val[e.index];
            }
            for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
                if (m.first(token)) {
                    val[e.index] = true;
                    break;
                }
            }
            valSet[e.index] = true;
            return val[e.index];
          }
          default:
            return (content == token);
        }
    }
    public Element first() {
        switch (type) {
          case '&':
          case '|':
          case '*':
          case '?':
            return null;
          case '+':
          case ',':
            return ((ContentModel)content).first();
          default:
            return (Element)content;
        }
    }
    public String toString() {
        switch (type) {
          case '*':
            return content + "*";
          case '?':
            return content + "?";
          case '+':
            return content + "+";
          case ',':
          case '|':
          case '&':
            char data[] = {' ', (char)type, ' '};
            String str = "";
            for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
                str = str + m;
                if (m.next != null) {
                    str += new String(data);
                }
            }
            return "(" + str + ")";
          default:
            return content.toString();
        }
    }
}
