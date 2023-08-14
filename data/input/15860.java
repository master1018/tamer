public class PlaceholderTable extends TwoOopHashtable {
  public PlaceholderTable(Address addr) {
    super(addr);
  }
  protected Class getHashtableEntryClass() {
    return PlaceholderEntry.class;
  }
  public void primArrayClassesDo(SystemDictionary.ClassAndLoaderVisitor v) {
    ObjectHeap heap = VM.getVM().getObjectHeap();
    int tblSize = tableSize();
    for (int index = 0; index < tblSize; index++) {
      for (PlaceholderEntry probe = (PlaceholderEntry) bucket(index); probe != null;
                                          probe = (PlaceholderEntry) probe.next()) {
        Symbol sym = probe.klass();
        FieldType ft = new FieldType(sym);
        if (ft.isArray()) {
          FieldType.ArrayInfo info = ft.getArrayInfo();
          if (info.elementBasicType() != BasicType.getTObject()) {
            Klass arrayKlass = heap.typeArrayKlassObj(info.elementBasicType());
            arrayKlass = arrayKlass.arrayKlassOrNull(info.dimension());
            v.visit(arrayKlass, probe.loader());
          }
        }
      }
    }
  }
}
