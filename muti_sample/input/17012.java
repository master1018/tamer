class RegisterDecoder implements  RTLDataTypes {
    private static SPARCFloatRegister decodeDouble(int num) {
        boolean lsb = (0x1 & num) != 0;
        if (lsb)
            num |= 0x20;  
        if ((num % 2) != 0)
            return null;
        return SPARCFloatRegisters.getRegister(num);
    }
    private static SPARCFloatRegister decodeQuad(int num) {
        boolean lsb = (0x1 & num) != 0;
        if (lsb)
            num |= 0x20; 
        if ((num % 4) != 0)
            return null;
        return SPARCFloatRegisters.getRegister(num);
    }
    static SPARCRegister decode(int dataType, int regNum) {
        regNum &= 0x1F; 
        SPARCRegister result = null;
        switch (dataType) {
            case RTLDT_FL_SINGLE:
                result = SPARCFloatRegisters.getRegister(regNum);
                break;
            case RTLDT_FL_DOUBLE:
                result = decodeDouble(regNum);
                break;
            case RTLDT_FL_QUAD:
                result = decodeQuad(regNum);
                break;
            case RTLDT_UNKNOWN:
                result = null;
                break;
            default: 
                result = SPARCRegisters.getRegister(regNum);
                break;
        }
        return result;
    }
}
