import com.sun.tools.classfile.Annotation;
import com.sun.tools.classfile.Annotation.Annotation_element_value;
import com.sun.tools.classfile.Annotation.Array_element_value;
import com.sun.tools.classfile.Annotation.Class_element_value;
import com.sun.tools.classfile.Annotation.Enum_element_value;
import com.sun.tools.classfile.Annotation.Primitive_element_value;
import com.sun.tools.classfile.ConstantPool;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Descriptor;
import com.sun.tools.classfile.Descriptor.InvalidDescriptor;
public class AnnotationWriter extends BasicWriter {
    static AnnotationWriter instance(Context context) {
        AnnotationWriter instance = context.get(AnnotationWriter.class);
        if (instance == null)
            instance = new AnnotationWriter(context);
        return instance;
    }
    protected AnnotationWriter(Context context) {
        super(context);
        classWriter = ClassWriter.instance(context);
        constantWriter = ConstantWriter.instance(context);
    }
    public void write(Annotation annot) {
        write(annot, false);
    }
    public void write(Annotation annot, boolean resolveIndices) {
        writeDescriptor(annot.type_index, resolveIndices);
        boolean showParens = annot.num_element_value_pairs > 0 || !resolveIndices;
        if (showParens)
            print("(");
        for (int i = 0; i < annot.num_element_value_pairs; i++) {
            if (i > 0)
                print(",");
            write(annot.element_value_pairs[i], resolveIndices);
        }
        if (showParens)
            print(")");
    }
    public void write(Annotation.element_value_pair pair) {
        write(pair, false);
    }
    public void write(Annotation.element_value_pair pair, boolean resolveIndices) {
        writeIndex(pair.element_name_index, resolveIndices);
        print("=");
        write(pair.value, resolveIndices);
    }
    public void write(Annotation.element_value value) {
        write(value, false);
    }
    public void write(Annotation.element_value value, boolean resolveIndices) {
        ev_writer.write(value, resolveIndices);
    }
    private void writeDescriptor(int index, boolean resolveIndices) {
        if (resolveIndices) {
            try {
                ConstantPool constant_pool = classWriter.getClassFile().constant_pool;
                Descriptor d = new Descriptor(index);
                print(d.getFieldType(constant_pool));
                return;
            } catch (ConstantPoolException ignore) {
            } catch (InvalidDescriptor ignore) {
            }
        }
        print("#" + index);
    }
    private void writeIndex(int index, boolean resolveIndices) {
        if (resolveIndices) {
            print(constantWriter.stringValue(index));
        } else
            print("#" + index);
    }
    element_value_Writer ev_writer = new element_value_Writer();
    class element_value_Writer implements Annotation.element_value.Visitor<Void,Boolean> {
        public void write(Annotation.element_value value, boolean resolveIndices) {
            value.accept(this, resolveIndices);
        }
        public Void visitPrimitive(Primitive_element_value ev, Boolean resolveIndices) {
            if (resolveIndices)
                writeIndex(ev.const_value_index, resolveIndices);
            else
                print(((char) ev.tag) + "#" + ev.const_value_index);
            return null;
        }
        public Void visitEnum(Enum_element_value ev, Boolean resolveIndices) {
            if (resolveIndices) {
                writeIndex(ev.type_name_index, resolveIndices);
                print(".");
                writeIndex(ev.const_name_index, resolveIndices);
            } else
                print(((char) ev.tag) + "#" + ev.type_name_index + ".#" + ev.const_name_index);
            return null;
        }
        public Void visitClass(Class_element_value ev, Boolean resolveIndices) {
            if (resolveIndices) {
                writeIndex(ev.class_info_index, resolveIndices);
                print(".class");
            } else
                print(((char) ev.tag) + "#" + ev.class_info_index);
            return null;
        }
        public Void visitAnnotation(Annotation_element_value ev, Boolean resolveIndices) {
            print((char) ev.tag);
            AnnotationWriter.this.write(ev.annotation_value, resolveIndices);
            return null;
        }
        public Void visitArray(Array_element_value ev, Boolean resolveIndices) {
            print("[");
            for (int i = 0; i < ev.num_values; i++) {
                if (i > 0)
                    print(",");
                write(ev.values[i], resolveIndices);
            }
            print("]");
            return null;
        }
    }
    private ClassWriter classWriter;
    private ConstantWriter constantWriter;
}
