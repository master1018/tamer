public class DefaultSymtabFactory implements SymtabFactory
{
  public AttributeEntry attributeEntry ()
  {
    return new AttributeEntry ();
  } 
  public AttributeEntry attributeEntry (InterfaceEntry container, IDLID id)
  {
    return new AttributeEntry (container, id);
  } 
  public ConstEntry constEntry ()
  {
    return new ConstEntry ();
  } 
  public ConstEntry constEntry (SymtabEntry container, IDLID id)
  {
    return new ConstEntry (container, id);
  } 
  public NativeEntry nativeEntry ()
  {
    return new NativeEntry ();
  } 
  public NativeEntry nativeEntry (SymtabEntry container, IDLID id)
  {
    return new NativeEntry (container, id);
  } 
  public EnumEntry enumEntry ()
  {
    return new EnumEntry ();
  } 
  public EnumEntry enumEntry (SymtabEntry container, IDLID id)
  {
    return new EnumEntry (container, id);
  } 
  public ExceptionEntry exceptionEntry ()
  {
    return new ExceptionEntry ();
  } 
  public ExceptionEntry exceptionEntry (SymtabEntry container, IDLID id)
  {
    return new ExceptionEntry (container, id);
  } 
  public ForwardEntry forwardEntry ()
  {
    return new ForwardEntry ();
  } 
  public ForwardEntry forwardEntry (ModuleEntry container, IDLID id)
  {
    return new ForwardEntry (container, id);
  } 
  public ForwardValueEntry forwardValueEntry ()
  {
    return new ForwardValueEntry ();
  } 
  public ForwardValueEntry forwardValueEntry (ModuleEntry container, IDLID id)
  {
    return new ForwardValueEntry (container, id);
  } 
  public IncludeEntry includeEntry ()
  {
    return new IncludeEntry ();
  } 
  public IncludeEntry includeEntry (SymtabEntry container)
  {
    return new IncludeEntry (container);
  } 
  public InterfaceEntry interfaceEntry ()
  {
    return new InterfaceEntry ();
  } 
  public InterfaceEntry interfaceEntry (ModuleEntry container, IDLID id)
  {
    return new InterfaceEntry (container, id);
  } 
  public ValueEntry valueEntry ()
  {
    return new ValueEntry ();
  } 
  public ValueEntry valueEntry (ModuleEntry container, IDLID id)
  {
    return new ValueEntry (container, id);
  } 
  public ValueBoxEntry valueBoxEntry ()
  {
    return new ValueBoxEntry ();
  } 
  public ValueBoxEntry valueBoxEntry (ModuleEntry container, IDLID id)
  {
    return new ValueBoxEntry (container, id);
  } 
  public MethodEntry methodEntry ()
  {
    return new MethodEntry ();
  } 
  public MethodEntry methodEntry (InterfaceEntry container, IDLID id)
  {
    return new MethodEntry (container, id);
  } 
  public ModuleEntry moduleEntry ()
  {
    return new ModuleEntry ();
  } 
  public ModuleEntry moduleEntry (ModuleEntry container, IDLID id)
  {
    return new ModuleEntry (container, id);
  } 
  public ParameterEntry parameterEntry ()
  {
    return new ParameterEntry ();
  } 
  public ParameterEntry parameterEntry (MethodEntry container, IDLID id)
  {
    return new ParameterEntry (container, id);
  } 
  public PragmaEntry pragmaEntry ()
  {
    return new PragmaEntry ();
  } 
  public PragmaEntry pragmaEntry (SymtabEntry container)
  {
    return new PragmaEntry (container);
  } 
  public PrimitiveEntry primitiveEntry ()
  {
    return new PrimitiveEntry ();
  } 
  public PrimitiveEntry primitiveEntry (String name)
  {
    return new PrimitiveEntry (name);
  } 
  public SequenceEntry sequenceEntry ()
  {
    return new SequenceEntry ();
  } 
  public SequenceEntry sequenceEntry (SymtabEntry container, IDLID id)
  {
    return new SequenceEntry (container, id);
  } 
  public StringEntry stringEntry ()
  {
    return new StringEntry ();
  } 
  public StructEntry structEntry ()
  {
    return new StructEntry ();
  } 
  public StructEntry structEntry (SymtabEntry container, IDLID id)
  {
    return new StructEntry (container, id);
  } 
  public TypedefEntry typedefEntry ()
  {
    return new TypedefEntry ();
  } 
  public TypedefEntry typedefEntry (SymtabEntry container, IDLID id)
  {
    return new TypedefEntry (container, id);
  } 
  public UnionEntry unionEntry ()
  {
    return new UnionEntry ();
  } 
  public UnionEntry unionEntry (SymtabEntry container, IDLID id)
  {
    return new UnionEntry (container, id);
  } 
} 
