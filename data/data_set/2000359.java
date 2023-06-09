package java.sql;

/**
 * An exception, which is subclass of SQLNonTransientException, is thrown when
 * various the an integrity constraint (foreign key, primary key or unique key)
 * has been violated.
 */
public class SQLIntegrityConstraintViolationException extends SQLNonTransientException {

    private static final long serialVersionUID = 8033405298774849169L;

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to null, the SQLState string is set to null and the Error
     * Code is set to 0.
     */
    public SQLIntegrityConstraintViolationException() {
        super();
    }

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to the given reason string, the SQLState string is set to
     * null and the Error Code is set to 0.
     * 
     * @param reason
     *            the string to use as the Reason string
     */
    public SQLIntegrityConstraintViolationException(String reason) {
        super(reason, null, 0);
    }

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to the given reason string, the SQLState string is set to
     * the given SQLState string and the Error Code is set to 0.
     * 
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     */
    public SQLIntegrityConstraintViolationException(String reason, String sqlState) {
        super(reason, sqlState, 0);
    }

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to the given reason string, the SQLState string is set to
     * the given SQLState string and the Error Code is set to the given error
     * code value.
     * 
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param vendorCode
     *            the integer value for the error code
     */
    public SQLIntegrityConstraintViolationException(String reason, String sqlState, int vendorCode) {
        super(reason, sqlState, vendorCode);
    }

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to the null if cause == null or cause.toString() if
     * cause!=null,and the cause Throwable object is set to the given cause
     * Throwable object.
     * 
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLIntegrityConstraintViolationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to the given and the cause Throwable object is set to the
     * given cause Throwable object.
     * 
     * @param reason
     *            the string to use as the Reason string
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLIntegrityConstraintViolationException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to the given reason string, the SQLState string is set to
     * the given SQLState string and the cause Throwable object is set to the
     * given cause Throwable object.
     * 
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLIntegrityConstraintViolationException(String reason, String sqlState, Throwable cause) {
        super(reason, sqlState, cause);
    }

    /**
     * Creates an SQLIntegrityConstraintViolationException object. The Reason
     * string is set to the given reason string, the SQLState string is set to
     * the given SQLState string , the Error Code is set to the given error code
     * value, and the cause Throwable object is set to the given cause Throwable
     * object.
     * 
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param vendorCode
     *            the integer value for the error code
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLIntegrityConstraintViolationException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
    }
}
