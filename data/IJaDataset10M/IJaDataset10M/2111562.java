package net.sourceforge.poi.hssf.record.formula;

import net.sourceforge.poi.util.LittleEndian;
import net.sourceforge.poi.util.BitField;

/**
 *
 * @author  andy
 */
public class ValueReferencePtg extends Ptg {

    private static final int SIZE = 5;

    public static final byte sid = 0x44;

    private short field_1_row;

    private short field_2_col;

    private BitField rowRelative = new BitField(0x8000);

    private BitField colRelative = new BitField(0x4000);

    /** Creates new ValueReferencePtg */
    public ValueReferencePtg() {
    }

    /** Creates new ValueReferencePtg */
    public ValueReferencePtg(byte[] data, int offset) {
        offset++;
        field_1_row = LittleEndian.getShort(data, offset + 0);
        field_2_col = LittleEndian.getShort(data, offset + 2);
        System.out.println(toString());
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("[ValueReferencePtg]\n");
        buffer.append("row = ").append(getRow()).append("\n");
        buffer.append("col = ").append(getColumnRaw()).append("\n");
        buffer.append("rowrelative = ").append(isRowRelative()).append("\n");
        buffer.append("colrelative = ").append(isColRelative()).append("\n");
        return buffer.toString();
    }

    public void writeBytes(byte[] array, int offset) {
    }

    public void setRow(short row) {
        field_1_row = row;
    }

    public short getRow() {
        return field_1_row;
    }

    public boolean isRowRelative() {
        return rowRelative.isSet(field_2_col);
    }

    public boolean isColRelative() {
        return rowRelative.isSet(field_2_col);
    }

    public void setColumnRaw(short col) {
        field_2_col = col;
    }

    public short getColumnRaw() {
        return field_2_col;
    }

    public void setColumn(short col) {
        field_2_col = col;
    }

    public short getColumn() {
        return field_2_col;
    }

    public int getSize() {
        return SIZE;
    }

    public String toFormulaString() {
        return "NO IDEA YET VALUE REF";
    }
}