public final class ErrorCode extends Exception
{ 
  public static final RuntimeException getException(int error)
  {
    if (error <= U_ZERO_ERROR && error >= U_ERROR_LIMIT) {
      return null;
    }
    String errorname = ERROR_NAMES_[U_ILLEGAL_ARGUMENT_ERROR];
    switch (error) {
      case U_ILLEGAL_ARGUMENT_ERROR :
        return new IllegalArgumentException(errorname);
      case U_INDEX_OUTOFBOUNDS_ERROR :
        return new ArrayIndexOutOfBoundsException(errorname);
      case U_BUFFER_OVERFLOW_ERROR :
        return new ArrayIndexOutOfBoundsException(errorname);
      case U_UNSUPPORTED_ERROR :
        return new UnsupportedOperationException(errorname);
      default :
        return new RuntimeException(errorname);
    }
  }
  public static final int U_ERROR_INFO_START = -128;
  public static final int U_USING_FALLBACK_ERROR = -128;
  public static final int U_USING_DEFAULT_ERROR = -127;
  public static final int U_SAFECLONE_ALLOCATED_ERROR = -126;
  public static final int U_ERROR_INFO_LIMIT = -125;
  public static final int U_ZERO_ERROR = 0;
  public static final int U_ILLEGAL_ARGUMENT_ERROR = 1;
  public static final int U_MISSING_RESOURCE_ERROR = 2;
  public static final int U_INVALID_FORMAT_ERROR = 3;
  public static final int U_FILE_ACCESS_ERROR = 4;
  public static final int U_INTERNAL_PROGRAM_ERROR = 5;
  public static final int U_MESSAGE_PARSE_ERROR = 6;
  public static final int U_MEMORY_ALLOCATION_ERROR = 7;
  public static final int U_INDEX_OUTOFBOUNDS_ERROR = 8;
  public static final int U_PARSE_ERROR = 9;
  public static final int U_INVALID_CHAR_FOUND = 10;
  public static final int U_TRUNCATED_CHAR_FOUND = 11;
  public static final int U_ILLEGAL_CHAR_FOUND = 12;
  public static final int U_INVALID_TABLE_FORMAT = 13;
  public static final int U_INVALID_TABLE_FILE = 14;
  public static final int U_BUFFER_OVERFLOW_ERROR = 15;
  public static final int U_UNSUPPORTED_ERROR = 16;
  public static final int U_RESOURCE_TYPE_MISMATCH = 17;
  public static final int U_ILLEGAL_ESCAPE_SEQUENCE = 18;
  public static final int U_UNSUPPORTED_ESCAPE_SEQUENCE = 19;
  public static final int U_NO_SPACE_AVAILABLE = 20;
  public static final int U_ERROR_LIMIT = 21;
  public static boolean LIBRARY_LOADED = false;
  private static final String ERROR_NAMES_[] = { 
    "U_ZERO_ERROR",               "U_ILLEGAL_ARGUMENT_ERROR", 
    "U_MISSING_RESOURCE_ERROR",   "U_INVALID_FORMAT_ERROR", 
    "U_FILE_ACCESS_ERROR",        "U_INTERNAL_PROGRAM_ERROR", 
    "U_MESSAGE_PARSE_ERROR",      "U_MEMORY_ALLOCATION_ERROR",
    "U_INDEX_OUTOFBOUNDS_ERROR",  "U_PARSE_ERROR",
    "U_INVALID_CHAR_FOUND",       "U_TRUNCATED_CHAR_FOUND", 
    "U_ILLEGAL_CHAR_FOUND",       "U_INVALID_TABLE_FORMAT",
    "U_INVALID_TABLE_FILE",       "U_BUFFER_OVERFLOW_ERROR",
    "U_UNSUPPORTED_ERROR",        "U_RESOURCE_TYPE_MISMATCH",
    "U_ILLEGAL_ESCAPE_SEQUENCE",  "U_UNSUPPORTED_ESCAPE_SEQUENCE"
  };
  public static String getErrorName(int ec){
    return ERROR_NAMES_[ec];
  }
  public static boolean isSuccess(int ec){
    return (ec<=U_ZERO_ERROR);
  }
  public static boolean isFailure(int ec){
    return (ec>U_ZERO_ERROR);
  }
}
