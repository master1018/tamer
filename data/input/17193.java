class ContentModelState {
    ContentModel model;
    long value;
    ContentModelState next;
    public ContentModelState(ContentModel model) {
        this(model, null, 0);
    }
    ContentModelState(Object content, ContentModelState next) {
        this(content, next, 0);
    }
    ContentModelState(Object content, ContentModelState next, long value) {
        this.model = (ContentModel)content;
        this.next = next;
        this.value = value;
    }
    public ContentModel getModel() {
        ContentModel m = model;
        for (int i = 0; i < value; i++) {
            if (m.next != null) {
                m = m.next;
            } else {
                return null;
            }
        }
        return m;
    }
    public boolean terminate() {
        switch (model.type) {
          case '+':
            if ((value == 0) && !(model).empty()) {
                return false;
            }
          case '*':
          case '?':
            return (next == null) || next.terminate();
          case '|':
            for (ContentModel m = (ContentModel)model.content ; m != null ; m = m.next) {
                if (m.empty()) {
                    return (next == null) || next.terminate();
                }
            }
            return false;
          case '&': {
            ContentModel m = (ContentModel)model.content;
            for (int i = 0 ; m != null ; i++, m = m.next) {
                if ((value & (1L << i)) == 0) {
                    if (!m.empty()) {
                        return false;
                    }
                }
            }
            return (next == null) || next.terminate();
          }
          case ',': {
            ContentModel m = (ContentModel)model.content;
            for (int i = 0 ; i < value ; i++, m = m.next);
            for (; (m != null) && m.empty() ; m = m.next);
            if (m != null) {
                return false;
            }
            return (next == null) || next.terminate();
          }
        default:
          return false;
        }
    }
    public Element first() {
        switch (model.type) {
          case '*':
          case '?':
          case '|':
          case '&':
            return null;
          case '+':
            return model.first();
          case ',': {
              ContentModel m = (ContentModel)model.content;
              for (int i = 0 ; i < value ; i++, m = m.next);
              return m.first();
          }
          default:
            return model.first();
        }
    }
    public ContentModelState advance(Object token) {
        switch (model.type) {
          case '+':
            if (model.first(token)) {
                return new ContentModelState(model.content,
                        new ContentModelState(model, next, value + 1)).advance(token);
            }
            if (value != 0) {
                if (next != null) {
                    return next.advance(token);
                } else {
                    return null;
                }
            }
            break;
          case '*':
            if (model.first(token)) {
                return new ContentModelState(model.content, this).advance(token);
            }
            if (next != null) {
                return next.advance(token);
            } else {
                return null;
            }
          case '?':
            if (model.first(token)) {
                return new ContentModelState(model.content, next).advance(token);
            }
            if (next != null) {
                return next.advance(token);
            } else {
                return null;
            }
          case '|':
            for (ContentModel m = (ContentModel)model.content ; m != null ; m = m.next) {
                if (m.first(token)) {
                    return new ContentModelState(m, next).advance(token);
                }
            }
            break;
          case ',': {
            ContentModel m = (ContentModel)model.content;
            for (int i = 0 ; i < value ; i++, m = m.next);
            if (m.first(token) || m.empty()) {
                if (m.next == null) {
                    return new ContentModelState(m, next).advance(token);
                } else {
                    return new ContentModelState(m,
                            new ContentModelState(model, next, value + 1)).advance(token);
                }
            }
            break;
          }
          case '&': {
            ContentModel m = (ContentModel)model.content;
            boolean complete = true;
            for (int i = 0 ; m != null ; i++, m = m.next) {
                if ((value & (1L << i)) == 0) {
                    if (m.first(token)) {
                        return new ContentModelState(m,
                                new ContentModelState(model, next, value | (1L << i))).advance(token);
                    }
                    if (!m.empty()) {
                        complete = false;
                    }
                }
            }
            if (complete) {
                if (next != null) {
                    return next.advance(token);
                } else {
                    return null;
                }
            }
            break;
          }
          default:
            if (model.content == token) {
                if (next == null && (token instanceof Element) &&
                    ((Element)token).content != null) {
                    return new ContentModelState(((Element)token).content);
                }
                return next;
            }
        }
        return null;
    }
}
