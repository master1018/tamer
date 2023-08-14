public class DefaultScriptObject implements ScriptObject {
  public Object[] getIds() {
    return EMPTY_ARRAY;
  }
  public Object get(String name) {
    return UNDEFINED;
  }
  public Object get(int index) {
    return UNDEFINED;
  }
  public void put(String name, Object value) {
  }
  public void put(int index, Object value) {
  }
  public boolean has(String name) {
    return false;
  }
  public boolean has(int index) {
    return false;
  }
  public boolean delete(String name) {
    return false;
  }
  public boolean delete(int index) {
    return false;
  }
}
