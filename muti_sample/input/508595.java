public class
ArrowKeyMovementMethod
implements MovementMethod
{
    private boolean up(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (MetaKeyKeyListener.getMetaState(buffer,
                        MetaKeyKeyListener.META_SELECTING) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();
        if (cap) {
            if (alt) {
                Selection.extendSelection(buffer, 0);
                return true;
            } else {
                return Selection.extendUp(buffer, layout);
            }
        } else {
            if (alt) {
                Selection.setSelection(buffer, 0);
                return true;
            } else {
                return Selection.moveUp(buffer, layout);
            }
        }
    }
    private boolean down(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (MetaKeyKeyListener.getMetaState(buffer,
                        MetaKeyKeyListener.META_SELECTING) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();
        if (cap) {
            if (alt) {
                Selection.extendSelection(buffer, buffer.length());
                return true;
            } else {
                return Selection.extendDown(buffer, layout);
            }
        } else {
            if (alt) {
                Selection.setSelection(buffer, buffer.length());
                return true;
            } else {
                return Selection.moveDown(buffer, layout);
            }
        }
    }
    private boolean left(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (MetaKeyKeyListener.getMetaState(buffer,
                        MetaKeyKeyListener.META_SELECTING) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();
        if (cap) {
            if (alt) {
                return Selection.extendToLeftEdge(buffer, layout);
            } else {
                return Selection.extendLeft(buffer, layout);
            }
        } else {
            if (alt) {
                return Selection.moveToLeftEdge(buffer, layout);
            } else {
                return Selection.moveLeft(buffer, layout); 
            }
        }
    }
    private boolean right(TextView widget, Spannable buffer) {
        boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_SHIFT_ON) == 1) ||
                      (MetaKeyKeyListener.getMetaState(buffer,
                        MetaKeyKeyListener.META_SELECTING) != 0);
        boolean alt = MetaKeyKeyListener.getMetaState(buffer,
                        KeyEvent.META_ALT_ON) == 1;
        Layout layout = widget.getLayout();
        if (cap) {
            if (alt) {
                return Selection.extendToRightEdge(buffer, layout);
            } else {
                return Selection.extendRight(buffer, layout);
            }
        } else {
            if (alt) {
                return Selection.moveToRightEdge(buffer, layout);
            } else {
                return Selection.moveRight(buffer, layout); 
            }
        }
    }
    private int getOffset(int x, int y, TextView widget){
      x -= widget.getTotalPaddingLeft();
      y -= widget.getTotalPaddingTop();
      if (x < 0) {
          x = 0;
      } else if (x >= (widget.getWidth()-widget.getTotalPaddingRight())) {
          x = widget.getWidth()-widget.getTotalPaddingRight() - 1;
      }
      if (y < 0) {
          y = 0;
      } else if (y >= (widget.getHeight()-widget.getTotalPaddingBottom())) {
          y = widget.getHeight()-widget.getTotalPaddingBottom() - 1;
      }
      x += widget.getScrollX();
      y += widget.getScrollY();
      Layout layout = widget.getLayout();
      int line = layout.getLineForVertical(y);
      int offset = layout.getOffsetForHorizontal(line, x);
      return offset;
    }
    public boolean onKeyDown(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
        if (executeDown(widget, buffer, keyCode)) {
            MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
            MetaKeyKeyListener.resetLockedMeta(buffer);
            return true;
        }
        return false;
    }
    private boolean executeDown(TextView widget, Spannable buffer, int keyCode) {
        boolean handled = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
            handled |= up(widget, buffer);
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            handled |= down(widget, buffer);
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            handled |= left(widget, buffer);
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            handled |= right(widget, buffer);
            break;
        case KeyEvent.KEYCODE_DPAD_CENTER:
            if (MetaKeyKeyListener.getMetaState(buffer, MetaKeyKeyListener.META_SELECTING) != 0) {
                if (widget.showContextMenu()) {
                    handled = true;
                }
            }
        }
        if (handled) {
            MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
            MetaKeyKeyListener.resetLockedMeta(buffer);
        }
        return handled;
    }
    public boolean onKeyUp(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
        int code = event.getKeyCode();
        if (code != KeyEvent.KEYCODE_UNKNOWN
                && event.getAction() == KeyEvent.ACTION_MULTIPLE) {
            int repeat = event.getRepeatCount();
            boolean handled = false;
            while ((--repeat) > 0) {
                handled |= executeDown(view, text, code);
            }
            return handled;
        }
        return false;
    }
    public boolean onTrackballEvent(TextView widget, Spannable text,
            MotionEvent event) {
        return false;
    }
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int initialScrollX = -1, initialScrollY = -1;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            initialScrollX = Touch.getInitialScrollX(widget, buffer);
            initialScrollY = Touch.getInitialScrollY(widget, buffer);
        }
        boolean handled = Touch.onTouchEvent(widget, buffer, event);
        if (widget.isFocused() && !widget.didTouchFocusSelect()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
              boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                              KeyEvent.META_SHIFT_ON) == 1) ||
                            (MetaKeyKeyListener.getMetaState(buffer,
                              MetaKeyKeyListener.META_SELECTING) != 0);
              int x = (int) event.getX();
              int y = (int) event.getY();
              int offset = getOffset(x, y, widget);
              if (cap) {
                  buffer.setSpan(LAST_TAP_DOWN, offset, offset,
                                 Spannable.SPAN_POINT_POINT);
                  widget.getParent().requestDisallowInterceptTouchEvent(true);
              } else {
                  OnePointFiveTapState[] tap = buffer.getSpans(0, buffer.length(),
                      OnePointFiveTapState.class);
                  if (tap.length > 0) {
                      if (event.getEventTime() - tap[0].mWhen <=
                          ViewConfiguration.getDoubleTapTimeout() &&
                          sameWord(buffer, offset, Selection.getSelectionEnd(buffer))) {
                          tap[0].active = true;
                          MetaKeyKeyListener.startSelecting(widget, buffer);
                          widget.getParent().requestDisallowInterceptTouchEvent(true);
                          buffer.setSpan(LAST_TAP_DOWN, offset, offset,
                              Spannable.SPAN_POINT_POINT);
                      }
                      tap[0].mWhen = event.getEventTime();
                  } else {
                      OnePointFiveTapState newtap = new OnePointFiveTapState();
                      newtap.mWhen = event.getEventTime();
                      newtap.active = false;
                      buffer.setSpan(newtap, 0, buffer.length(),
                          Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                  }
              }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                                KeyEvent.META_SHIFT_ON) == 1) ||
                              (MetaKeyKeyListener.getMetaState(buffer,
                                MetaKeyKeyListener.META_SELECTING) != 0);
                if (cap && handled) {
                    widget.cancelLongPress();
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    int offset = getOffset(x, y, widget);
                    final OnePointFiveTapState[] tap = buffer.getSpans(0, buffer.length(),
                            OnePointFiveTapState.class);
                    if (tap.length > 0 && tap[0].active) {
                        int lastDownOffset = buffer.getSpanStart(LAST_TAP_DOWN);
                        int spanstart;
                        int spanend;
                        if (offset >= lastDownOffset) {
                            spanstart = findWordStart(buffer, lastDownOffset);
                            spanend = findWordEnd(buffer, offset);
                        } else {
                            spanstart = findWordEnd(buffer, lastDownOffset);
                            spanend = findWordStart(buffer, offset);
                        }
                        Selection.setSelection(buffer, spanstart, spanend);
                    } else {
                        Selection.extendSelection(buffer, offset);
                    }
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if ((initialScrollY >= 0 && initialScrollY != widget.getScrollY()) ||
                        (initialScrollX >= 0 && initialScrollX != widget.getScrollX())) {
                    widget.moveCursorToVisibleOffset();
                    return true;
                }
                int x = (int) event.getX();
                int y = (int) event.getY();
                int off = getOffset(x, y, widget);
                OnePointFiveTapState[] onepointfivetap = buffer.getSpans(0, buffer.length(),
                    OnePointFiveTapState.class);
                if (onepointfivetap.length > 0 && onepointfivetap[0].active &&
                    Selection.getSelectionStart(buffer) == Selection.getSelectionEnd(buffer)) {
                    MetaKeyKeyListener.stopSelecting(widget, buffer);
                    for (int i=0; i < onepointfivetap.length; i++) {
                        buffer.removeSpan(onepointfivetap[i]);
                    }
                    buffer.removeSpan(LAST_TAP_DOWN);
                }
                boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                                KeyEvent.META_SHIFT_ON) == 1) ||
                              (MetaKeyKeyListener.getMetaState(buffer,
                                MetaKeyKeyListener.META_SELECTING) != 0);
                DoubleTapState[] tap = buffer.getSpans(0, buffer.length(),
                                                       DoubleTapState.class);
                boolean doubletap = false;
                if (tap.length > 0) {
                    if (event.getEventTime() - tap[0].mWhen <=
                        ViewConfiguration.getDoubleTapTimeout() &&
                        sameWord(buffer, off, Selection.getSelectionEnd(buffer))) {
                        doubletap = true;
                    }
                    tap[0].mWhen = event.getEventTime();
                } else {
                    DoubleTapState newtap = new DoubleTapState();
                    newtap.mWhen = event.getEventTime();
                    buffer.setSpan(newtap, 0, buffer.length(),
                                   Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
                if (cap) {
                    buffer.removeSpan(LAST_TAP_DOWN);
                    if (onepointfivetap.length > 0 && onepointfivetap[0].active) {
                        MetaKeyKeyListener.stopSelecting(widget, buffer);
                    } else {
                        Selection.extendSelection(buffer, off);
                    }
                } else if (doubletap) {
                    Selection.setSelection(buffer,
                                           findWordStart(buffer, off),
                                           findWordEnd(buffer, off));
                } else {
                    Selection.setSelection(buffer, off);
                }
                MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
                MetaKeyKeyListener.resetLockedMeta(buffer);
                return true;
            }
        }
        return handled;
    }
    private static class DoubleTapState implements NoCopySpan {
        long mWhen;
    }
    private static class OnePointFiveTapState implements NoCopySpan {
        long mWhen;
        boolean active;
    }
    private static boolean sameWord(CharSequence text, int one, int two) {
        int start = findWordStart(text, one);
        int end = findWordEnd(text, one);
        if (end == start) {
            return false;
        }
        return start == findWordStart(text, two) &&
               end == findWordEnd(text, two);
    }
    private static int findWordStart(CharSequence text, int start) {
        for (; start > 0; start--) {
            char c = text.charAt(start - 1);
            int type = Character.getType(c);
            if (c != '\'' &&
                type != Character.UPPERCASE_LETTER &&
                type != Character.LOWERCASE_LETTER &&
                type != Character.TITLECASE_LETTER &&
                type != Character.MODIFIER_LETTER &&
                type != Character.DECIMAL_DIGIT_NUMBER) {
                break;
            }
        }
        return start;
    }
    private static int findWordEnd(CharSequence text, int end) {
        int len = text.length();
        for (; end < len; end++) {
            char c = text.charAt(end);
            int type = Character.getType(c);
            if (c != '\'' &&
                type != Character.UPPERCASE_LETTER &&
                type != Character.LOWERCASE_LETTER &&
                type != Character.TITLECASE_LETTER &&
                type != Character.MODIFIER_LETTER &&
                type != Character.DECIMAL_DIGIT_NUMBER) {
                break;
            }
        }
        return end;
    }
    public boolean canSelectArbitrarily() {
        return true;
    }
    public void initialize(TextView widget, Spannable text) {
        Selection.setSelection(text, 0);
    }
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        if ((dir & (View.FOCUS_FORWARD | View.FOCUS_DOWN)) != 0) {
            Layout layout = view.getLayout();
            if (layout == null) {
                Selection.setSelection(text, text.length());
            } else {
                if (layout.getLineCount() == 1) {
                    Selection.setSelection(text, text.length());
                } else {
                    Selection.setSelection(text, layout.getLineStart(1) - 1);
                }
            }
        } else {
            Selection.setSelection(text, text.length());
        }
    }
    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new ArrowKeyMovementMethod();
        return sInstance;
    }
    private static final Object LAST_TAP_DOWN = new Object();
    private static ArrowKeyMovementMethod sInstance;
}
