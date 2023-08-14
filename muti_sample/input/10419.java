public class ModelIdentifier {
    private String object = null;
    private String variable = null;
    private int instance = 0;
    public ModelIdentifier(String object) {
        this.object = object;
    }
    public ModelIdentifier(String object, int instance) {
        this.object = object;
        this.instance = instance;
    }
    public ModelIdentifier(String object, String variable) {
        this.object = object;
        this.variable = variable;
    }
    public ModelIdentifier(String object, String variable, int instance) {
        this.object = object;
        this.variable = variable;
        this.instance = instance;
    }
    public int getInstance() {
        return instance;
    }
    public void setInstance(int instance) {
        this.instance = instance;
    }
    public String getObject() {
        return object;
    }
    public void setObject(String object) {
        this.object = object;
    }
    public String getVariable() {
        return variable;
    }
    public void setVariable(String variable) {
        this.variable = variable;
    }
    public int hashCode() {
        int hashcode = instance;
        if(object != null) hashcode |= object.hashCode();
        if(variable != null) hashcode |= variable.hashCode();
        return  hashcode;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof ModelIdentifier))
            return false;
        ModelIdentifier mobj = (ModelIdentifier)obj;
        if ((object == null) != (mobj.object == null))
            return false;
        if ((variable == null) != (mobj.variable == null))
            return false;
        if (mobj.getInstance() != getInstance())
            return false;
        if (!(object == null || object.equals(mobj.object)))
            return false;
        if (!(variable == null || variable.equals(mobj.variable)))
            return false;
        return true;
    }
    public String toString() {
        if (variable == null) {
            return object + "[" + instance + "]";
        } else {
            return object + "[" + instance + "]" + "." + variable;
        }
    }
}
