  final class SerialCallbackContext {
      private final Object obj;
      private final ObjectStreamClass desc;
      private Thread thread;
      public SerialCallbackContext(Object obj, ObjectStreamClass desc) {
          this.obj = obj;
          this.desc = desc;
          this.thread = Thread.currentThread();
      }
      public Object getObj() throws NotActiveException {
          checkAndSetUsed();
          return obj;
      }
      public ObjectStreamClass getDesc() {
          return desc;
      }
      private void checkAndSetUsed() throws NotActiveException {
          if (thread != Thread.currentThread()) {
               throw new NotActiveException(
                "not in readObject invocation or fields already read");
          }
          thread = null;
      }
      public void setUsed() {
          thread = null;
      }
  }
