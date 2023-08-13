public abstract class _BindingIteratorImplBase extends org.omg.CORBA.DynamicImplementation implements org.omg.CosNaming.BindingIterator {
    public _BindingIteratorImplBase() {
        super();
    }
    private static final String _type_ids[] = {
        "IDL:omg.org/CosNaming/BindingIterator:1.0"
    };
    public String[] _ids() { return (String[]) _type_ids.clone(); }
    private static java.util.Dictionary _methods = new java.util.Hashtable();
    static {
        _methods.put("next_one", new java.lang.Integer(0));
        _methods.put("next_n", new java.lang.Integer(1));
        _methods.put("destroy", new java.lang.Integer(2));
    }
    public void invoke(org.omg.CORBA.ServerRequest r) {
        switch (((java.lang.Integer) _methods.get(r.op_name())).intValue()) {
        case 0: 
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _b = _orb().create_any();
                _b.type(org.omg.CosNaming.BindingHelper.type());
                _list.add_value("b", _b, org.omg.CORBA.ARG_OUT.value);
                r.params(_list);
                org.omg.CosNaming.BindingHolder b;
                b = new org.omg.CosNaming.BindingHolder();
                boolean ___result;
                ___result = this.next_one(b);
                org.omg.CosNaming.BindingHelper.insert(_b, b.value);
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_boolean(___result);
                r.result(__result);
            }
            break;
        case 1: 
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                org.omg.CORBA.Any _how_many = _orb().create_any();
                _how_many.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_ulong));
                _list.add_value("how_many", _how_many, org.omg.CORBA.ARG_IN.value);
                org.omg.CORBA.Any _bl = _orb().create_any();
                _bl.type(org.omg.CosNaming.BindingListHelper.type());
                _list.add_value("bl", _bl, org.omg.CORBA.ARG_OUT.value);
                r.params(_list);
                int how_many;
                how_many = _how_many.extract_ulong();
                org.omg.CosNaming.BindingListHolder bl;
                bl = new org.omg.CosNaming.BindingListHolder();
                boolean ___result;
                ___result = this.next_n(how_many, bl);
                org.omg.CosNaming.BindingListHelper.insert(_bl, bl.value);
                org.omg.CORBA.Any __result = _orb().create_any();
                __result.insert_boolean(___result);
                r.result(__result);
            }
            break;
        case 2: 
            {
                org.omg.CORBA.NVList _list = _orb().create_list(0);
                r.params(_list);
                this.destroy();
                org.omg.CORBA.Any __return = _orb().create_any();
                __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
                r.result(__return);
            }
            break;
        default:
            throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }
    }
}
