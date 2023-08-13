public abstract class SnmpMsg implements SnmpDefinitions {
    public int version = 0;
    public byte[] data = null;
    public int dataLength = 0;
    public InetAddress address = null;
    public int port = 0;
    public SnmpSecurityParameters securityParameters = null;
    public static int getProtocolVersion(byte[] data)
        throws SnmpStatusException {
        int version = 0;
        BerDecoder bdec = null;
        try {
            bdec = new BerDecoder(data);
            bdec.openSequence();
            version = bdec.fetchInteger();
        }
        catch(BerException x) {
            throw new SnmpStatusException("Invalid encoding") ;
        }
        try {
            bdec.closeSequence();
        }
        catch(BerException x) {
        }
        return version;
    }
    public abstract int getRequestId(byte[] data) throws SnmpStatusException;
    public abstract int encodeMessage(byte[] outputBytes)
        throws SnmpTooBigException;
    public abstract void decodeMessage(byte[] inputBytes, int byteCount)
        throws SnmpStatusException;
    public abstract void encodeSnmpPdu(SnmpPdu pdu, int maxDataLength)
        throws SnmpStatusException, SnmpTooBigException;
    public abstract SnmpPdu decodeSnmpPdu()
        throws SnmpStatusException;
    public static String dumpHexBuffer(byte [] b, int offset, int len) {
        StringBuffer buf = new StringBuffer(len << 1) ;
        int k = 1 ;
        int flen = offset + len ;
        for (int i = offset; i < flen ; i++) {
            int j = b[i] & 0xFF ;
            buf.append(Character.forDigit((j >>> 4) , 16)) ;
            buf.append(Character.forDigit((j & 0x0F), 16)) ;
            k++ ;
            if (k%16 == 0) {
                buf.append('\n') ;
                k = 1 ;
            } else
                buf.append(' ') ;
        }
        return buf.toString() ;
    }
    public String printMessage() {
        StringBuffer sb = new StringBuffer() ;
        sb.append("Version: ") ;
        sb.append(version) ;
        sb.append("\n") ;
        if (data == null) {
            sb.append("Data: null") ;
        }
        else {
            sb.append("Data: {\n") ;
            sb.append(dumpHexBuffer(data, 0, dataLength)) ;
            sb.append("\n}\n") ;
        }
        return sb.toString() ;
    }
    public void encodeVarBindList(BerEncoder benc,
                                  SnmpVarBind[] varBindList)
        throws SnmpStatusException, SnmpTooBigException {
        int encodedVarBindCount = 0 ;
        try {
            benc.openSequence() ;
            if (varBindList != null) {
                for (int i = varBindList.length - 1 ; i >= 0 ; i--) {
                    SnmpVarBind bind = varBindList[i] ;
                    if (bind != null) {
                        benc.openSequence() ;
                        encodeVarBindValue(benc, bind.value) ;
                        benc.putOid(bind.oid.longValue()) ;
                        benc.closeSequence() ;
                        encodedVarBindCount++ ;
                    }
                }
            }
            benc.closeSequence() ;
        }
        catch(ArrayIndexOutOfBoundsException x) {
            throw new SnmpTooBigException(encodedVarBindCount) ;
        }
    }
    void encodeVarBindValue(BerEncoder benc,
                            SnmpValue v)throws SnmpStatusException {
        if (v == null) {
            benc.putNull() ;
        }
        else if (v instanceof SnmpIpAddress) {
            benc.putOctetString(((SnmpIpAddress)v).byteValue(), SnmpValue.IpAddressTag) ;
        }
        else if (v instanceof SnmpCounter) {
            benc.putInteger(((SnmpCounter)v).longValue(), SnmpValue.CounterTag) ;
        }
        else if (v instanceof SnmpGauge) {
            benc.putInteger(((SnmpGauge)v).longValue(), SnmpValue.GaugeTag) ;
        }
        else if (v instanceof SnmpTimeticks) {
            benc.putInteger(((SnmpTimeticks)v).longValue(), SnmpValue.TimeticksTag) ;
        }
        else if (v instanceof SnmpOpaque) {
            benc.putOctetString(((SnmpOpaque)v).byteValue(), SnmpValue.OpaqueTag) ;
        }
        else if (v instanceof SnmpInt) {
            benc.putInteger(((SnmpInt)v).intValue()) ;
        }
        else if (v instanceof SnmpString) {
            benc.putOctetString(((SnmpString)v).byteValue()) ;
        }
        else if (v instanceof SnmpOid) {
            benc.putOid(((SnmpOid)v).longValue()) ;
        }
        else if (v instanceof SnmpCounter64) {
            if (version == snmpVersionOne) {
                throw new SnmpStatusException("Invalid value for SNMP v1 : " + v) ;
            }
            benc.putInteger(((SnmpCounter64)v).longValue(), SnmpValue.Counter64Tag) ;
        }
        else if (v instanceof SnmpNull) {
            int tag = ((SnmpNull)v).getTag() ;
            if ((version == snmpVersionOne) && (tag != SnmpValue.NullTag)) {
                throw new SnmpStatusException("Invalid value for SNMP v1 : " + v) ;
            }
            if ((version == snmpVersionTwo) &&
                (tag != SnmpValue.NullTag) &&
                (tag != SnmpVarBind.errNoSuchObjectTag) &&
                (tag != SnmpVarBind.errNoSuchInstanceTag) &&
                (tag != SnmpVarBind.errEndOfMibViewTag)) {
                throw new SnmpStatusException("Invalid value " + v) ;
            }
            benc.putNull(tag) ;
        }
        else {
            throw new SnmpStatusException("Invalid value " + v) ;
        }
    }
    public SnmpVarBind[] decodeVarBindList(BerDecoder bdec)
        throws BerException {
            bdec.openSequence() ;
            Vector<SnmpVarBind> tmp = new Vector<SnmpVarBind>() ;
            while (bdec.cannotCloseSequence()) {
                SnmpVarBind bind = new SnmpVarBind() ;
                bdec.openSequence() ;
                bind.oid = new SnmpOid(bdec.fetchOid()) ;
                bind.setSnmpValue(decodeVarBindValue(bdec)) ;
                bdec.closeSequence() ;
                tmp.addElement(bind) ;
            }
            bdec.closeSequence() ;
            SnmpVarBind[] varBindList= new SnmpVarBind[tmp.size()] ;
            tmp.copyInto(varBindList);
            return varBindList ;
        }
    SnmpValue decodeVarBindValue(BerDecoder bdec)
        throws BerException {
        SnmpValue result = null ;
        int tag = bdec.getTag() ;
        switch(tag) {
        case BerDecoder.IntegerTag :
            try {
                result = new SnmpInt(bdec.fetchInteger()) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case BerDecoder.OctetStringTag :
            try {
                result = new SnmpString(bdec.fetchOctetString()) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case BerDecoder.OidTag :
            try {
                result = new SnmpOid(bdec.fetchOid()) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case BerDecoder.NullTag :
            bdec.fetchNull() ;
            try {
                result = new SnmpNull() ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case SnmpValue.IpAddressTag :
            try {
                result = new SnmpIpAddress(bdec.fetchOctetString(tag)) ;
            } catch (RuntimeException r) {
                throw new  BerException();
            }
            break ;
        case SnmpValue.CounterTag :
            try {
                result = new SnmpCounter(bdec.fetchIntegerAsLong(tag)) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case SnmpValue.GaugeTag :
            try {
                result = new SnmpGauge(bdec.fetchIntegerAsLong(tag)) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case SnmpValue.TimeticksTag :
            try {
                result = new SnmpTimeticks(bdec.fetchIntegerAsLong(tag)) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case SnmpValue.OpaqueTag :
            try {
                result = new SnmpOpaque(bdec.fetchOctetString(tag)) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case SnmpValue.Counter64Tag :
            if (version == snmpVersionOne) {
                throw new BerException(BerException.BAD_VERSION) ;
            }
            try {
                result = new SnmpCounter64(bdec.fetchIntegerAsLong(tag)) ;
            } catch(RuntimeException r) {
                throw new BerException();
            }
            break ;
        case SnmpVarBind.errNoSuchObjectTag :
            if (version == snmpVersionOne) {
                throw new BerException(BerException.BAD_VERSION) ;
            }
            bdec.fetchNull(tag) ;
            result = SnmpVarBind.noSuchObject ;
            break ;
        case SnmpVarBind.errNoSuchInstanceTag :
            if (version == snmpVersionOne) {
                throw new BerException(BerException.BAD_VERSION) ;
            }
            bdec.fetchNull(tag) ;
            result = SnmpVarBind.noSuchInstance ;
            break ;
        case SnmpVarBind.errEndOfMibViewTag :
            if (version == snmpVersionOne) {
                throw new BerException(BerException.BAD_VERSION) ;
            }
            bdec.fetchNull(tag) ;
            result = SnmpVarBind.endOfMibView ;
            break ;
        default:
            throw new BerException() ;
        }
        return result ;
    }
}
