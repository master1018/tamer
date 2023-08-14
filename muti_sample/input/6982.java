class DerInputBuffer extends ByteArrayInputStream implements Cloneable {
    DerInputBuffer(byte[] buf) { super(buf); }
    DerInputBuffer(byte[] buf, int offset, int len) {
        super(buf, offset, len);
    }
    DerInputBuffer dup() {
        try {
            DerInputBuffer retval = (DerInputBuffer)clone();
            retval.mark(Integer.MAX_VALUE);
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
    byte[] toByteArray() {
        int     len = available();
        if (len <= 0)
            return null;
        byte[]  retval = new byte[len];
        System.arraycopy(buf, pos, retval, 0, len);
        return retval;
    }
    int peek() throws IOException {
        if (pos >= count)
            throw new IOException("out of data");
        else
            return buf[pos];
    }
    public boolean equals(Object other) {
        if (other instanceof DerInputBuffer)
            return equals((DerInputBuffer)other);
        else
            return false;
    }
    boolean equals(DerInputBuffer other) {
        if (this == other)
            return true;
        int max = this.available();
        if (other.available() != max)
            return false;
        for (int i = 0; i < max; i++) {
            if (this.buf[this.pos + i] != other.buf[other.pos + i]) {
                return false;
            }
        }
        return true;
    }
    public int hashCode() {
        int retval = 0;
        int len = available();
        int p = pos;
        for (int i = 0; i < len; i++)
            retval += buf[p + i] * i;
        return retval;
    }
    void truncate(int len) throws IOException {
        if (len > available())
            throw new IOException("insufficient data");
        count = pos + len;
    }
    BigInteger getBigInteger(int len, boolean makePositive) throws IOException {
        if (len > available())
            throw new IOException("short read of integer");
        if (len == 0) {
            throw new IOException("Invalid encoding: zero length Int value");
        }
        byte[] bytes = new byte[len];
        System.arraycopy(buf, pos, bytes, 0, len);
        skip(len);
        if (makePositive) {
            return new BigInteger(1, bytes);
        } else {
            return new BigInteger(bytes);
        }
    }
    public int getInteger(int len) throws IOException {
        BigInteger result = getBigInteger(len, false);
        if (result.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
            throw new IOException("Integer below minimum valid value");
        }
        if (result.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IOException("Integer exceeds maximum valid value");
        }
        return result.intValue();
    }
    public byte[] getBitString(int len) throws IOException {
        if (len > available())
            throw new IOException("short read of bit string");
        if (len == 0) {
            throw new IOException("Invalid encoding: zero length bit string");
        }
        int numOfPadBits = buf[pos];
        if ((numOfPadBits < 0) || (numOfPadBits > 7)) {
            throw new IOException("Invalid number of padding bits");
        }
        byte[] retval = new byte[len - 1];
        System.arraycopy(buf, pos + 1, retval, 0, len - 1);
        if (numOfPadBits != 0) {
            retval[len - 2] &= (0xff << numOfPadBits);
        }
        skip(len);
        return retval;
    }
    byte[] getBitString() throws IOException {
        return getBitString(available());
    }
    BitArray getUnalignedBitString() throws IOException {
        if (pos >= count)
            return null;
        int len = available();
        int unusedBits = buf[pos] & 0xff;
        if (unusedBits > 7 ) {
            throw new IOException("Invalid value for unused bits: " + unusedBits);
        }
        byte[] bits = new byte[len - 1];
        int length = (bits.length == 0) ? 0 : bits.length * 8 - unusedBits;
        System.arraycopy(buf, pos + 1, bits, 0, len - 1);
        BitArray bitArray = new BitArray(length, bits);
        pos = count;
        return bitArray;
    }
    public Date getUTCTime(int len) throws IOException {
        if (len > available())
            throw new IOException("short read of DER UTC Time");
        if (len < 11 || len > 17)
            throw new IOException("DER UTC Time length error");
        return getTime(len, false);
    }
    public Date getGeneralizedTime(int len) throws IOException {
        if (len > available())
            throw new IOException("short read of DER Generalized Time");
        if (len < 13 || len > 23)
            throw new IOException("DER Generalized Time length error");
        return getTime(len, true);
    }
    private Date getTime(int len, boolean generalized) throws IOException {
        int year, month, day, hour, minute, second, millis;
        String type = null;
        if (generalized) {
            type = "Generalized";
            year = 1000 * Character.digit((char)buf[pos++], 10);
            year += 100 * Character.digit((char)buf[pos++], 10);
            year += 10 * Character.digit((char)buf[pos++], 10);
            year += Character.digit((char)buf[pos++], 10);
            len -= 2; 
        } else {
            type = "UTC";
            year = 10 * Character.digit((char)buf[pos++], 10);
            year += Character.digit((char)buf[pos++], 10);
            if (year < 50)              
                year += 2000;
            else
                year += 1900;   
        }
        month = 10 * Character.digit((char)buf[pos++], 10);
        month += Character.digit((char)buf[pos++], 10);
        day = 10 * Character.digit((char)buf[pos++], 10);
        day += Character.digit((char)buf[pos++], 10);
        hour = 10 * Character.digit((char)buf[pos++], 10);
        hour += Character.digit((char)buf[pos++], 10);
        minute = 10 * Character.digit((char)buf[pos++], 10);
        minute += Character.digit((char)buf[pos++], 10);
        len -= 10; 
        millis = 0;
        if (len > 2 && len < 12) {
            second = 10 * Character.digit((char)buf[pos++], 10);
            second += Character.digit((char)buf[pos++], 10);
            len -= 2;
            if (buf[pos] == '.' || buf[pos] == ',') {
                len --;
                pos++;
                int precision = 0;
                int peek = pos;
                while (buf[peek] != 'Z' &&
                       buf[peek] != '+' &&
                       buf[peek] != '-') {
                    peek++;
                    precision++;
                }
                switch (precision) {
                case 3:
                    millis += 100 * Character.digit((char)buf[pos++], 10);
                    millis += 10 * Character.digit((char)buf[pos++], 10);
                    millis += Character.digit((char)buf[pos++], 10);
                    break;
                case 2:
                    millis += 100 * Character.digit((char)buf[pos++], 10);
                    millis += 10 * Character.digit((char)buf[pos++], 10);
                    break;
                case 1:
                    millis += 100 * Character.digit((char)buf[pos++], 10);
                    break;
                default:
                        throw new IOException("Parse " + type +
                            " time, unsupported precision for seconds value");
                }
                len -= precision;
            }
        } else
            second = 0;
        if (month == 0 || day == 0
            || month > 12 || day > 31
            || hour >= 24 || minute >= 60 || second >= 60)
            throw new IOException("Parse " + type + " time, invalid format");
        CalendarSystem gcal = CalendarSystem.getGregorianCalendar();
        CalendarDate date = gcal.newCalendarDate(null); 
        date.setDate(year, month, day);
        date.setTimeOfDay(hour, minute, second, millis);
        long time = gcal.getTime(date);
        if (! (len == 1 || len == 5))
            throw new IOException("Parse " + type + " time, invalid offset");
        int hr, min;
        switch (buf[pos++]) {
        case '+':
            hr = 10 * Character.digit((char)buf[pos++], 10);
            hr += Character.digit((char)buf[pos++], 10);
            min = 10 * Character.digit((char)buf[pos++], 10);
            min += Character.digit((char)buf[pos++], 10);
            if (hr >= 24 || min >= 60)
                throw new IOException("Parse " + type + " time, +hhmm");
            time -= ((hr * 60) + min) * 60 * 1000;
            break;
        case '-':
            hr = 10 * Character.digit((char)buf[pos++], 10);
            hr += Character.digit((char)buf[pos++], 10);
            min = 10 * Character.digit((char)buf[pos++], 10);
            min += Character.digit((char)buf[pos++], 10);
            if (hr >= 24 || min >= 60)
                throw new IOException("Parse " + type + " time, -hhmm");
            time += ((hr * 60) + min) * 60 * 1000;
            break;
        case 'Z':
            break;
        default:
            throw new IOException("Parse " + type + " time, garbage offset");
        }
        return new Date(time);
    }
}
