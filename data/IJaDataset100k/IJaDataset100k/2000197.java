package net.sf.cglib.transform.impl;

import net.sf.cglib.transform.*;
import java.util.*;
import net.sf.cglib.core.TypeUtils;
import org.objectweb.asm.Type;

public class ExampleTask extends AbstractTransformTask {

    private List properties = new ArrayList();

    private String fieldSuffix = "";

    private ClassTransformer transformer;

    protected ClassTransformer getClassTransformer(String name[]) {
        return transformer;
    }

    protected void beforeExecute() {
        ClassTransformer t1 = new AccessFieldTransformer(new AccessFieldTransformer.Callback() {

            public String getPropertyName(Type owner, String fieldName) {
                return fieldName + fieldSuffix;
            }
        });
        int size = properties.size();
        String[] names = new String[size];
        Type[] types = new Type[size];
        for (int i = 0; i < size; i++) {
            NewProperty p = (NewProperty) properties.get(i);
            names[i] = p.name;
            types[i] = TypeUtils.parseType(p.type);
        }
        ClassTransformer t2 = new AddPropertyTransformer(names, types);
        transformer = new ClassTransformerChain(new ClassTransformer[] { t1, t2 });
    }

    public static class NewProperty {

        private String name;

        private String type;

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public void addNewproperty(NewProperty prop) {
        properties.add(prop);
    }

    public void setFieldsuffix(String fieldSuffix) {
        this.fieldSuffix = fieldSuffix;
    }
}
