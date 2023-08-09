public class BreakpointEvent extends Event {
  private Oop thread;
  private Oop clazz;
  private JNIid method;
  private int location;
  public BreakpointEvent(Oop thread,
                         Oop clazz,
                         JNIid method,
                         int location) {
    super(Event.Type.BREAKPOINT);
    this.thread = thread;
    this.clazz = clazz;
    this.method = method;
    this.location = location;
  }
  public Oop thread()     { return thread;   }
  public Oop clazz()      { return clazz;    }
  public JNIid methodID() { return method;   }
  public int location()   { return location; }
}
