public final class ObexHelper {
    public static final int BASE_PACKET_LENGTH = 3;
    private ObexHelper() {
    }
    public static final int MAX_PACKET_SIZE_INT = 0xFFFE;
    public static final int OBEX_OPCODE_CONNECT = 0x80;
    public static final int OBEX_OPCODE_DISCONNECT = 0x81;
    public static final int OBEX_OPCODE_PUT = 0x02;
    public static final int OBEX_OPCODE_PUT_FINAL = 0x82;
    public static final int OBEX_OPCODE_GET = 0x03;
    public static final int OBEX_OPCODE_GET_FINAL = 0x83;
    public static final int OBEX_OPCODE_RESERVED = 0x04;
    public static final int OBEX_OPCODE_RESERVED_FINAL = 0x84;
    public static final int OBEX_OPCODE_SETPATH = 0x85;
    public static final int OBEX_OPCODE_ABORT = 0xFF;
    public static final int OBEX_AUTH_REALM_CHARSET_ASCII = 0x00;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_1 = 0x01;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_2 = 0x02;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_3 = 0x03;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_4 = 0x04;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_5 = 0x05;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_6 = 0x06;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_7 = 0x07;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_8 = 0x08;
    public static final int OBEX_AUTH_REALM_CHARSET_ISO_8859_9 = 0x09;
    public static final int OBEX_AUTH_REALM_CHARSET_UNICODE = 0xFF;
    public static byte[] updateHeaderSet(HeaderSet header, byte[] headerArray) throws IOException {
        int index = 0;
        int length = 0;
        int headerID;
        byte[] value = null;
        byte[] body = null;
        HeaderSet headerImpl = header;
        try {
            while (index < headerArray.length) {
                headerID = 0xFF & headerArray[index];
                switch (headerID & (0xC0)) {
                    case 0x00:
                    case 0x40:
                        boolean trimTail = true;
                        index++;
                        length = 0xFF & headerArray[index];
                        length = length << 8;
                        index++;
                        length += 0xFF & headerArray[index];
                        length -= 3;
                        index++;
                        value = new byte[length];
                        System.arraycopy(headerArray, index, value, 0, length);
                        if (length == 0 || (length > 0 && (value[length - 1] != 0))) {
                            trimTail = false;
                        }
                        switch (headerID) {
                            case HeaderSet.TYPE:
                                try {
                                    if (trimTail == false) {
                                        headerImpl.setHeader(headerID, new String(value, 0,
                                                value.length, "ISO8859_1"));
                                    } else {
                                        headerImpl.setHeader(headerID, new String(value, 0,
                                                value.length - 1, "ISO8859_1"));
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    throw e;
                                }
                                break;
                            case HeaderSet.AUTH_CHALLENGE:
                                headerImpl.mAuthChall = new byte[length];
                                System.arraycopy(headerArray, index, headerImpl.mAuthChall, 0,
                                        length);
                                break;
                            case HeaderSet.AUTH_RESPONSE:
                                headerImpl.mAuthResp = new byte[length];
                                System.arraycopy(headerArray, index, headerImpl.mAuthResp, 0,
                                        length);
                                break;
                            case HeaderSet.BODY:
                            case HeaderSet.END_OF_BODY:
                                body = new byte[length + 1];
                                body[0] = (byte)headerID;
                                System.arraycopy(headerArray, index, body, 1, length);
                                break;
                            case HeaderSet.TIME_ISO_8601:
                                try {
                                    String dateString = new String(value, "ISO8859_1");
                                    Calendar temp = Calendar.getInstance();
                                    if ((dateString.length() == 16)
                                            && (dateString.charAt(15) == 'Z')) {
                                        temp.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    }
                                    temp.set(Calendar.YEAR, Integer.parseInt(dateString.substring(
                                            0, 4)));
                                    temp.set(Calendar.MONTH, Integer.parseInt(dateString.substring(
                                            4, 6)));
                                    temp.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateString
                                            .substring(6, 8)));
                                    temp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateString
                                            .substring(9, 11)));
                                    temp.set(Calendar.MINUTE, Integer.parseInt(dateString
                                            .substring(11, 13)));
                                    temp.set(Calendar.SECOND, Integer.parseInt(dateString
                                            .substring(13, 15)));
                                    headerImpl.setHeader(HeaderSet.TIME_ISO_8601, temp);
                                } catch (UnsupportedEncodingException e) {
                                    throw e;
                                }
                                break;
                            default:
                                if ((headerID & 0xC0) == 0x00) {
                                    headerImpl.setHeader(headerID, ObexHelper.convertToUnicode(
                                            value, true));
                                } else {
                                    headerImpl.setHeader(headerID, value);
                                }
                        }
                        index += length;
                        break;
                    case 0x80:
                        index++;
                        try {
                            headerImpl.setHeader(headerID, Byte.valueOf(headerArray[index]));
                        } catch (Exception e) {
                        }
                        index++;
                        break;
                    case 0xC0:
                        index++;
                        value = new byte[4];
                        System.arraycopy(headerArray, index, value, 0, 4);
                        try {
                            if (headerID != HeaderSet.TIME_4_BYTE) {
                                if (headerID == HeaderSet.CONNECTION_ID) {
                                    headerImpl.mConnectionID = new byte[4];
                                    System.arraycopy(value, 0, headerImpl.mConnectionID, 0, 4);
                                } else {
                                    headerImpl.setHeader(headerID, Long
                                            .valueOf(convertToLong(value)));
                                }
                            } else {
                                Calendar temp = Calendar.getInstance();
                                temp.setTime(new Date(convertToLong(value) * 1000L));
                                headerImpl.setHeader(HeaderSet.TIME_4_BYTE, temp);
                            }
                        } catch (Exception e) {
                            throw new IOException("Header was not formatted properly");
                        }
                        index += 4;
                        break;
                }
            }
        } catch (IOException e) {
            throw new IOException("Header was not formatted properly");
        }
        return body;
    }
    public static byte[] createHeader(HeaderSet head, boolean nullOut) {
        Long intHeader = null;
        String stringHeader = null;
        Calendar dateHeader = null;
        Byte byteHeader = null;
        StringBuffer buffer = null;
        byte[] value = null;
        byte[] result = null;
        byte[] lengthArray = new byte[2];
        int length;
        HeaderSet headImpl = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        headImpl = head;
        try {
            if ((headImpl.mConnectionID != null) && (headImpl.getHeader(HeaderSet.TARGET) == null)) {
                out.write((byte)HeaderSet.CONNECTION_ID);
                out.write(headImpl.mConnectionID);
            }
            intHeader = (Long)headImpl.getHeader(HeaderSet.COUNT);
            if (intHeader != null) {
                out.write((byte)HeaderSet.COUNT);
                value = ObexHelper.convertToByteArray(intHeader.longValue());
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.COUNT, null);
                }
            }
            stringHeader = (String)headImpl.getHeader(HeaderSet.NAME);
            if (stringHeader != null) {
                out.write((byte)HeaderSet.NAME);
                value = ObexHelper.convertToUnicodeByteArray(stringHeader);
                length = value.length + 3;
                lengthArray[0] = (byte)(0xFF & (length >> 8));
                lengthArray[1] = (byte)(0xFF & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.NAME, null);
                }
            }
            stringHeader = (String)headImpl.getHeader(HeaderSet.TYPE);
            if (stringHeader != null) {
                out.write((byte)HeaderSet.TYPE);
                try {
                    value = stringHeader.getBytes("ISO8859_1");
                } catch (UnsupportedEncodingException e) {
                    throw e;
                }
                length = value.length + 4;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(value);
                out.write(0x00);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.TYPE, null);
                }
            }
            intHeader = (Long)headImpl.getHeader(HeaderSet.LENGTH);
            if (intHeader != null) {
                out.write((byte)HeaderSet.LENGTH);
                value = ObexHelper.convertToByteArray(intHeader.longValue());
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.LENGTH, null);
                }
            }
            dateHeader = (Calendar)headImpl.getHeader(HeaderSet.TIME_ISO_8601);
            if (dateHeader != null) {
                buffer = new StringBuffer();
                int temp = dateHeader.get(Calendar.YEAR);
                for (int i = temp; i < 1000; i = i * 10) {
                    buffer.append("0");
                }
                buffer.append(temp);
                temp = dateHeader.get(Calendar.MONTH);
                if (temp < 10) {
                    buffer.append("0");
                }
                buffer.append(temp);
                temp = dateHeader.get(Calendar.DAY_OF_MONTH);
                if (temp < 10) {
                    buffer.append("0");
                }
                buffer.append(temp);
                buffer.append("T");
                temp = dateHeader.get(Calendar.HOUR_OF_DAY);
                if (temp < 10) {
                    buffer.append("0");
                }
                buffer.append(temp);
                temp = dateHeader.get(Calendar.MINUTE);
                if (temp < 10) {
                    buffer.append("0");
                }
                buffer.append(temp);
                temp = dateHeader.get(Calendar.SECOND);
                if (temp < 10) {
                    buffer.append("0");
                }
                buffer.append(temp);
                if (dateHeader.getTimeZone().getID().equals("UTC")) {
                    buffer.append("Z");
                }
                try {
                    value = buffer.toString().getBytes("ISO8859_1");
                } catch (UnsupportedEncodingException e) {
                    throw e;
                }
                length = value.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(HeaderSet.TIME_ISO_8601);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.TIME_ISO_8601, null);
                }
            }
            dateHeader = (Calendar)headImpl.getHeader(HeaderSet.TIME_4_BYTE);
            if (dateHeader != null) {
                out.write(HeaderSet.TIME_4_BYTE);
                value = ObexHelper.convertToByteArray(dateHeader.getTime().getTime() / 1000L);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.TIME_4_BYTE, null);
                }
            }
            stringHeader = (String)headImpl.getHeader(HeaderSet.DESCRIPTION);
            if (stringHeader != null) {
                out.write((byte)HeaderSet.DESCRIPTION);
                value = ObexHelper.convertToUnicodeByteArray(stringHeader);
                length = value.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.DESCRIPTION, null);
                }
            }
            value = (byte[])headImpl.getHeader(HeaderSet.TARGET);
            if (value != null) {
                out.write((byte)HeaderSet.TARGET);
                length = value.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.TARGET, null);
                }
            }
            value = (byte[])headImpl.getHeader(HeaderSet.HTTP);
            if (value != null) {
                out.write((byte)HeaderSet.HTTP);
                length = value.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.HTTP, null);
                }
            }
            value = (byte[])headImpl.getHeader(HeaderSet.WHO);
            if (value != null) {
                out.write((byte)HeaderSet.WHO);
                length = value.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.WHO, null);
                }
            }
            value = (byte[])headImpl.getHeader(HeaderSet.APPLICATION_PARAMETER);
            if (value != null) {
                out.write((byte)HeaderSet.APPLICATION_PARAMETER);
                length = value.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.APPLICATION_PARAMETER, null);
                }
            }
            value = (byte[])headImpl.getHeader(HeaderSet.OBJECT_CLASS);
            if (value != null) {
                out.write((byte)HeaderSet.OBJECT_CLASS);
                length = value.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(value);
                if (nullOut) {
                    headImpl.setHeader(HeaderSet.OBJECT_CLASS, null);
                }
            }
            for (int i = 0; i < 16; i++) {
                stringHeader = (String)headImpl.getHeader(i + 0x30);
                if (stringHeader != null) {
                    out.write((byte)i + 0x30);
                    value = ObexHelper.convertToUnicodeByteArray(stringHeader);
                    length = value.length + 3;
                    lengthArray[0] = (byte)(255 & (length >> 8));
                    lengthArray[1] = (byte)(255 & length);
                    out.write(lengthArray);
                    out.write(value);
                    if (nullOut) {
                        headImpl.setHeader(i + 0x30, null);
                    }
                }
                value = (byte[])headImpl.getHeader(i + 0x70);
                if (value != null) {
                    out.write((byte)i + 0x70);
                    length = value.length + 3;
                    lengthArray[0] = (byte)(255 & (length >> 8));
                    lengthArray[1] = (byte)(255 & length);
                    out.write(lengthArray);
                    out.write(value);
                    if (nullOut) {
                        headImpl.setHeader(i + 0x70, null);
                    }
                }
                byteHeader = (Byte)headImpl.getHeader(i + 0xB0);
                if (byteHeader != null) {
                    out.write((byte)i + 0xB0);
                    out.write(byteHeader.byteValue());
                    if (nullOut) {
                        headImpl.setHeader(i + 0xB0, null);
                    }
                }
                intHeader = (Long)headImpl.getHeader(i + 0xF0);
                if (intHeader != null) {
                    out.write((byte)i + 0xF0);
                    out.write(ObexHelper.convertToByteArray(intHeader.longValue()));
                    if (nullOut) {
                        headImpl.setHeader(i + 0xF0, null);
                    }
                }
            }
            if (headImpl.mAuthChall != null) {
                out.write((byte)HeaderSet.AUTH_CHALLENGE);
                length = headImpl.mAuthChall.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(headImpl.mAuthChall);
                if (nullOut) {
                    headImpl.mAuthChall = null;
                }
            }
            if (headImpl.mAuthResp != null) {
                out.write((byte)HeaderSet.AUTH_RESPONSE);
                length = headImpl.mAuthResp.length + 3;
                lengthArray[0] = (byte)(255 & (length >> 8));
                lengthArray[1] = (byte)(255 & length);
                out.write(lengthArray);
                out.write(headImpl.mAuthResp);
                if (nullOut) {
                    headImpl.mAuthResp = null;
                }
            }
        } catch (IOException e) {
        } finally {
            result = out.toByteArray();
            try {
                out.close();
            } catch (Exception ex) {
            }
        }
        return result;
    }
    public static int findHeaderEnd(byte[] headerArray, int start, int maxSize) {
        int fullLength = 0;
        int lastLength = -1;
        int index = start;
        int length = 0;
        while ((fullLength < maxSize) && (index < headerArray.length)) {
            int headerID = (headerArray[index] < 0 ? headerArray[index] + 256 : headerArray[index]);
            lastLength = fullLength;
            switch (headerID & (0xC0)) {
                case 0x00:
                case 0x40:
                    index++;
                    length = (headerArray[index] < 0 ? headerArray[index] + 256
                            : headerArray[index]);
                    length = length << 8;
                    index++;
                    length += (headerArray[index] < 0 ? headerArray[index] + 256
                            : headerArray[index]);
                    length -= 3;
                    index++;
                    index += length;
                    fullLength += length + 3;
                    break;
                case 0x80:
                    index++;
                    index++;
                    fullLength += 2;
                    break;
                case 0xC0:
                    index += 5;
                    fullLength += 5;
                    break;
            }
        }
        if (lastLength == 0) {
            if (fullLength < maxSize) {
                return headerArray.length;
            } else {
                return -1;
            }
        } else {
            return lastLength + start;
        }
    }
    public static long convertToLong(byte[] b) {
        long result = 0;
        long value = 0;
        long power = 0;
        for (int i = (b.length - 1); i >= 0; i--) {
            value = b[i];
            if (value < 0) {
                value += 256;
            }
            result = result | (value << power);
            power += 8;
        }
        return result;
    }
    public static byte[] convertToByteArray(long l) {
        byte[] b = new byte[4];
        b[0] = (byte)(255 & (l >> 24));
        b[1] = (byte)(255 & (l >> 16));
        b[2] = (byte)(255 & (l >> 8));
        b[3] = (byte)(255 & l);
        return b;
    }
    public static byte[] convertToUnicodeByteArray(String s) {
        if (s == null) {
            return null;
        }
        char c[] = s.toCharArray();
        byte[] result = new byte[(c.length * 2) + 2];
        for (int i = 0; i < c.length; i++) {
            result[(i * 2)] = (byte)(c[i] >> 8);
            result[((i * 2) + 1)] = (byte)c[i];
        }
        result[result.length - 2] = 0;
        result[result.length - 1] = 0;
        return result;
    }
    public static byte[] getTagValue(byte tag, byte[] triplet) {
        int index = findTag(tag, triplet);
        if (index == -1) {
            return null;
        }
        index++;
        int length = triplet[index] & 0xFF;
        byte[] result = new byte[length];
        index++;
        System.arraycopy(triplet, index, result, 0, length);
        return result;
    }
    public static int findTag(byte tag, byte[] value) {
        int length = 0;
        if (value == null) {
            return -1;
        }
        int index = 0;
        while ((index < value.length) && (value[index] != tag)) {
            length = value[index + 1] & 0xFF;
            index += length + 2;
        }
        if (index >= value.length) {
            return -1;
        }
        return index;
    }
    public static String convertToUnicode(byte[] b, boolean includesNull) {
        if (b == null || b.length == 0) {
            return null;
        }
        int arrayLength = b.length;
        if (!((arrayLength % 2) == 0)) {
            throw new IllegalArgumentException("Byte array not of a valid form");
        }
        arrayLength = (arrayLength >> 1);
        if (includesNull) {
            arrayLength -= 1;
        }
        char[] c = new char[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            int upper = b[2 * i];
            int lower = b[(2 * i) + 1];
            if (upper < 0) {
                upper += 256;
            }
            if (lower < 0) {
                lower += 256;
            }
            if (upper == 0 && lower == 0) {
                return new String(c, 0, i);
            }
            c[i] = (char)((upper << 8) | lower);
        }
        return new String(c);
    }
    public static byte[] computeMd5Hash(byte[] in) {
        Md5MessageDigest md5 = new Md5MessageDigest();
        return md5.digest(in);
    }
    public static byte[] computeAuthenticationChallenge(byte[] nonce, String realm, boolean access,
            boolean userID) throws IOException {
        byte[] authChall = null;
        if (nonce.length != 16) {
            throw new IllegalArgumentException("Nonce must be 16 bytes long");
        }
        if (realm == null) {
            authChall = new byte[21];
        } else {
            if (realm.length() >= 255) {
                throw new IllegalArgumentException("Realm must be less then 255 bytes");
            }
            authChall = new byte[24 + realm.length()];
            authChall[21] = 0x02;
            authChall[22] = (byte)(realm.length() + 1);
            authChall[23] = 0x01; 
            System.arraycopy(realm.getBytes("ISO8859_1"), 0, authChall, 24, realm.length());
        }
        authChall[0] = 0x00;
        authChall[1] = 0x10;
        System.arraycopy(nonce, 0, authChall, 2, 16);
        authChall[18] = 0x01;
        authChall[19] = 0x01;
        authChall[20] = 0x00;
        if (!access) {
            authChall[20] = (byte)(authChall[20] | 0x02);
        }
        if (userID) {
            authChall[20] = (byte)(authChall[20] | 0x01);
        }
        return authChall;
    }
}
