public abstract class NativeSignatureIterator extends SignatureIterator {
  private Method method;
  private int offset;     
  private int prepended;  
  private int jni_offset; 
  public void doBool  ()                     { passInt();    jni_offset++; offset++;       }
  public void doChar  ()                     { passInt();    jni_offset++; offset++;       }
  public void doFloat () {
    if (VM.getVM().isLP64()) {
      passFloat();
    } else {
      passInt();
    }
    jni_offset++; offset++;
  }
  public void doDouble() {
    if (VM.getVM().isLP64()) {
      passDouble(); jni_offset++; offset += 2;
    } else {
      passDouble(); jni_offset += 2; offset += 2;
    }
  }
  public void doByte  ()                     { passInt();    jni_offset++; offset++;       }
  public void doShort ()                     { passInt();    jni_offset++; offset++;       }
  public void doInt   ()                     { passInt();    jni_offset++; offset++;       }
  public void doLong  () {
    if (VM.getVM().isLP64()) {
      passLong(); jni_offset++; offset += 2;
    } else {
      passLong(); jni_offset += 2; offset += 2;
    }
  }
  public void doVoid  ()                     { throw new RuntimeException("should not reach here"); }
  public void doObject(int begin, int end)   { passObject(); jni_offset++; offset++;        }
  public void doArray (int begin, int end)   { passObject(); jni_offset++; offset++;        }
  public Method       method()               { return method; }
  public int          offset()               { return offset; }
  public int       jniOffset()               { return jni_offset + prepended; }
  public boolean    isStatic()               { return method.isStatic(); }
  public abstract void passInt();
  public abstract void passLong();
  public abstract void passObject();
  public abstract void passFloat();
  public abstract void passDouble();
  public NativeSignatureIterator(Method method) {
    super(method.getSignature());
    this.method = method;
    offset = 0;
    jni_offset = 0;
    int JNIEnv_words = 1;
    int mirror_words = 1;
    prepended = !isStatic() ? JNIEnv_words : JNIEnv_words + mirror_words;
  }
  public void iterate() {
    if (!isStatic()) {
      passObject(); jni_offset++; offset++;
    }
    iterateParameters();
  }
}
