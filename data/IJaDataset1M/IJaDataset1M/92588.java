package org.jumpmind.db.sql;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jumpmind.exception.IoException;
import org.jumpmind.util.LinkedCaseInsensitiveMap;

public class Row extends LinkedCaseInsensitiveMap<Object> {

    private static final long serialVersionUID = 1L;

    public Row(int numberOfColumns) {
        super(numberOfColumns);
    }

    public Row(String columnName, Object value) {
        super(1);
        put(columnName, value);
    }

    public byte[] bytesValue() {
        Object obj = this.values().iterator().next();
        return toBytes(obj);
    }

    protected byte[] toBytes(Object obj) {
        if (obj != null) {
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            } else if (obj instanceof Blob) {
                Blob blob = (Blob) obj;
                try {
                    return IOUtils.toByteArray(blob.getBinaryStream());
                } catch (IOException e) {
                    throw new IoException(e);
                } catch (SQLException e) {
                    throw new SqlException(e);
                }
            } else if (obj instanceof String) {
                return obj.toString().getBytes();
            } else {
                throw new IllegalStateException(String.format("Cannot translate a %s into a byte[]", obj.getClass().getName()));
            }
        } else {
            return null;
        }
    }

    public Number numberValue() {
        Object obj = this.values().iterator().next();
        if (obj != null) {
            if (obj instanceof Number) {
                return (Number) obj;
            } else {
                return new BigDecimal(obj.toString());
            }
        } else {
            return null;
        }
    }

    public Date dateValue() {
        Object obj = this.values().iterator().next();
        if (obj != null) {
            if (obj instanceof Date) {
                return (Date) obj;
            } else {
                return Timestamp.valueOf(obj.toString());
            }
        } else {
            return null;
        }
    }

    public String stringValue() {
        Object obj = this.values().iterator().next();
        if (obj != null) {
            return obj.toString();
        } else {
            return null;
        }
    }

    public String getString(String columnName) {
        return getString(columnName, true);
    }

    public byte[] getBytes(String columnName) {
        Object obj = get(columnName);
        return toBytes(obj);
    }

    public String getString(String columnName, boolean checkForColumn) {
        Object obj = this.get(columnName);
        if (obj != null) {
            return obj.toString();
        } else {
            if (checkForColumn) {
                checkForColumn(columnName);
            }
            return null;
        }
    }

    public int getInt(String columnName) {
        Object obj = this.get(columnName);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt(obj.toString());
        } else {
            checkForColumn(columnName);
            return 0;
        }
    }

    public long getLong(String columnName) {
        Object obj = this.get(columnName);
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else if (obj instanceof String) {
            return Long.parseLong(obj.toString());
        } else {
            checkForColumn(columnName);
            return 0;
        }
    }

    public boolean getBoolean(String columnName) {
        Object obj = this.get(columnName);
        if ("1".equals(obj)) {
            return true;
        } else if (obj instanceof Number) {
            int value = ((Number) obj).intValue();
            return value > 0 ? true : false;
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        } else {
            checkForColumn(columnName);
            return false;
        }
    }

    public Time getTime(String columnName) {
        Object obj = this.get(columnName);
        if (obj instanceof Time) {
            return (Time) obj;
        } else {
            Date date = getDateTime(columnName);
            return new Time(date.getTime());
        }
    }

    public Date getDateTime(String columnName) {
        Object obj = this.get(columnName);
        if (obj instanceof Number) {
            long value = ((Number) obj).longValue();
            return new Date(value);
        } else if (obj instanceof Date) {
            return (Date) obj;
        } else if (obj instanceof String) {
            return getDate((String) obj, SqlConstants.TIMESTAMP_PATTERNS);
        } else {
            checkForColumn(columnName);
            return null;
        }
    }

    protected void checkForColumn(String columnName) {
        if (!containsKey(columnName)) {
            throw new ColumnNotFoundException(columnName);
        }
    }

    private final java.util.Date getDate(String value, String[] pattern) {
        try {
            return DateUtils.parseDate(value, pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
