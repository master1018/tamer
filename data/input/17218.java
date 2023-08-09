public class ConnectionEvent extends java.util.EventObject {
  public ConnectionEvent(PooledConnection con) {
    super(con);
  }
  public ConnectionEvent(PooledConnection con, SQLException ex) {
    super(con);
    this.ex = ex;
  }
  public SQLException getSQLException() { return ex; }
  private SQLException ex = null;
  static final long serialVersionUID = -4843217645290030002L;
 }
