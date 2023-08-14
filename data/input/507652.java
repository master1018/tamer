final class InstrVisitor extends AbstractClassDefVisitor
                         implements IClassDefVisitor, IAttributeVisitor, IOpcodes, IConstants
{
    public static final class InstrResult
    {
        public boolean m_instrumented;
        public ClassDescriptor m_descriptor;
    } 
    public InstrVisitor (final CoverageOptions options)
    {
        m_excludeSyntheticMethods = options.excludeSyntheticMethods ();
        m_excludeBridgeMethods = options.excludeBridgeMethods ();
        m_doSUIDCompensation = options.doSUIDCompensation ();
        m_log = Logger.getLogger ();
    }
    public void process (final ClassDef cls,
                         final boolean ignoreAlreadyInstrumented,
                         final boolean instrument, final boolean metadata,
                         final InstrResult out)
    {
        out.m_instrumented = false;
        out.m_descriptor = null;
        if (! (instrument || metadata)) return; 
        if (cls.isInterface ())
            return; 
        else
        {
            reset ();
            m_cls = cls;
            m_instrument = instrument;
            m_metadata = metadata;
            m_ignoreAlreadyInstrumented = ignoreAlreadyInstrumented;
            visit ((ClassDef) null, null); 
            if (m_metadata)
            {
                setClassName (cls.getName ());
                out.m_descriptor = new ClassDescriptor (m_classPackageName, m_className, m_classSignature, m_classSrcFileName, m_classMethodDescriptors);
            }
            out.m_instrumented = m_instrument;
        }
    }
    public Object visit (final ClassDef ignore, final Object ctx)
    {
        final ClassDef cls = m_cls;
        final String clsVMName = cls.getName ();
        final String clsName = Types.vmNameToJavaName (clsVMName);
        final boolean trace1 = m_log.atTRACE1 (); 
        if (trace1) m_log.trace1 ("visit", "class: [" + clsVMName + "]");
        if (SKIP_SYNTHETIC_CLASSES && cls.isSynthetic ())
        {
            m_instrument = false;
            m_metadata = false;
            if (trace1) m_log.trace1 ("visit", "skipping synthetic class");
            return ctx;
        }
        if (! m_warningIssued && clsName.startsWith (IAppConstants.APP_PACKAGE))
        {
            m_warningIssued = true;
            m_log.warning (IAppConstants.APP_NAME + " classes appear to be included on the instrumentation");
            m_log.warning ("path: this is not a correct way to use " + IAppConstants.APP_NAME);
        }
        {
            final int [] existing = cls.getFields (COVERAGE_FIELD_NAME);
            if (existing.length > 0)
            {
                m_instrument = false;
                m_metadata = false;
                if (m_ignoreAlreadyInstrumented)
                {
                    if (trace1) m_log.trace1 ("visit", "skipping instrumented class");
                    return ctx;
                }
                else
                {
                    throw new IllegalStateException ("class [" + clsName + "] appears to be instrumented already");
                }
            }
        }
        final IConstantCollection constants = cls.getConstants ();
        SyntheticAttribute_info syntheticMarker = null;
        {
            if (MARK_ADDED_ELEMENTS_SYNTHETIC)
                m_syntheticStringIndex = cls.addCONSTANT_Utf8 (Attribute_info.ATTRIBUTE_SYNTHETIC, true);
        }
        {
            final int coverageFieldOffset;
            final String fieldDescriptor = "[[Z";
            final int fieldModifiers = IAccessFlags.ACC_PRIVATE | IAccessFlags.ACC_STATIC | IAccessFlags.ACC_FINAL;
            if (MARK_ADDED_ELEMENTS_SYNTHETIC)
            {
                final IAttributeCollection fieldAttributes = ElementFactory.newAttributeCollection (1);
                syntheticMarker = new SyntheticAttribute_info (m_syntheticStringIndex);
                fieldAttributes.add (syntheticMarker);
                coverageFieldOffset = cls.addField (COVERAGE_FIELD_NAME, fieldDescriptor,
                    fieldModifiers, fieldAttributes);
            }
            else
            {
                coverageFieldOffset = cls.addField (COVERAGE_FIELD_NAME, fieldDescriptor,
                    fieldModifiers);
            }
            m_coverageFieldrefIndex = cls.addFieldref (coverageFieldOffset);
        }
        {
            final String classJVMName = "com/vladium/emma/rt/RT";
            final int class_index = cls.addClassref (classJVMName);
            final String methodDescriptor = "([[ZLjava/lang/String;J)V";
            final int nametype_index = cls.addNameType ("r", methodDescriptor);
            m_registerMethodrefIndex = constants.add (new CONSTANT_Methodref_info (class_index, nametype_index));
        }
        {
            final String methodDescriptor = "()[[Z";
            final int nametype_index = cls.addNameType (PRECLINIT_METHOD_NAME, methodDescriptor);
            m_preclinitMethodrefIndex = constants.add (new CONSTANT_Methodref_info (cls.getThisClassIndex (), nametype_index));
        }
        {
            m_classNameConstantIndex = constants.add (new CONSTANT_String_info (cls.getThisClass ().m_name_index));
        }
        visit (cls.getMethods (), ctx);
        if (m_doSUIDCompensation)
        {
            boolean compensate = ((m_clinitStatus & IMetadataConstants.METHOD_ADDED) != 0);
            int existingSUIDFieldCount = 0;
            if (compensate)
            {
                {
                    final int [] existing = cls.getFields (SUID_FIELD_NAME);
                    existingSUIDFieldCount = existing.length;
                    if (existingSUIDFieldCount > 0)
                    {
                        final IFieldCollection fields = cls.getFields ();
                        for (int f = 0; f < existingSUIDFieldCount; ++ f)
                        {
                            final Field_info field = fields.get (existing [f]);
                            if ((field.getAccessFlags () & (IAccessFlags.ACC_STATIC | IAccessFlags.ACC_FINAL))
                                 == (IAccessFlags.ACC_STATIC | IAccessFlags.ACC_FINAL))
                            {
                                compensate = false;
                                break;
                            }
                        }
                    }
                }
                if (compensate && (cls.getThisClassIndex () == 0)) 
                {
                    boolean serializable = false;
                    final IInterfaceCollection interfaces = cls.getInterfaces ();
                    for (int i = 0, iLimit = interfaces.size (); i < iLimit; ++ i)
                    {
                        final CONSTANT_Class_info ifc = (CONSTANT_Class_info) constants.get (interfaces.get (i));
                        final String ifcName = ifc.getName (cls); 
                        if (JAVA_IO_SERIALIZABLE_NAME.equals (ifcName) || JAVA_IO_EXTERNALIZABLE_NAME.equals (ifcName))
                        {
                            serializable = true;
                            break;
                        }
                    }
                    if (! serializable) compensate = false;
                }
            }
            if (compensate)
            {
                if (existingSUIDFieldCount > 0)
                {
                    m_log.warning ("class [" + clsName + "] declares a 'serialVersionUID'");
                    m_log.warning ("field that is not static and final: this is likely an implementation mistake");
                    m_log.warning ("and can interfere with " + IAppConstants.APP_NAME + "'s SUID compensation");
                }
                final String fieldDescriptor = "J";
                final int fieldModifiers = IAccessFlags.ACC_PRIVATE | IAccessFlags.ACC_STATIC | IAccessFlags.ACC_FINAL;
                final IAttributeCollection fieldAttributes = ElementFactory.newAttributeCollection (MARK_ADDED_ELEMENTS_SYNTHETIC ? 2 : 1);
                final int nameIndex = cls.addCONSTANT_Utf8 (Attribute_info.ATTRIBUTE_CONSTANT_VALUE, true);
                final int valueIndex = constants.add (new CONSTANT_Long_info (cls.computeSUID (true))); 
                final ConstantValueAttribute_info initializer = new ConstantValueAttribute_info (nameIndex, valueIndex);
                fieldAttributes.add (initializer);
                if (MARK_ADDED_ELEMENTS_SYNTHETIC)
                {
                    if (syntheticMarker == null) syntheticMarker = new SyntheticAttribute_info (m_syntheticStringIndex);
                    fieldAttributes.add (syntheticMarker);
                }
                cls.addField (SUID_FIELD_NAME, fieldDescriptor, fieldModifiers, fieldAttributes);
            }
        } 
        visit (cls.getAttributes (), ctx);
        return ctx;
    }
    public Object visit (final IMethodCollection methods, final Object ctx)
    {
        final ClassDef cls = m_cls;
        final boolean trace2 = m_log.atTRACE2 ();
        final int originalMethodCount = methods.size ();
        final boolean constructMetadata = m_metadata;
        m_classBlockCounts = new int [originalMethodCount + 1];        
        if (constructMetadata)
        {
            m_classBlockMetadata = new int [originalMethodCount + 1] [] []; 
            m_classMethodDescriptors = new MethodDescriptor [originalMethodCount];
        }
        for (int m = 0; m < originalMethodCount; ++ m)
        {
            final Method_info method = methods.get (m);
            m_methodName = method.getName (cls); 
            if (trace2) m_log.trace2 ("visit", (method.isSynthetic () ? "synthetic " : "") + "method #" + m + ": [" + m_methodName + "]");
            final boolean isClinit = IClassDefConstants.CLINIT_NAME.equals (m_methodName);
            boolean excluded = false;
            if (! isClinit)
            {
                if (m_excludeSyntheticMethods && method.isSynthetic ())
                {
                    excluded = true;
                    if (trace2) m_log.trace2 ("visit", "skipped synthetic method");
                }
                else if (m_excludeBridgeMethods && method.isBridge ())
                {
                    excluded = true;
                    if (trace2) m_log.trace2 ("visit", "skipped bridge method");
                } 
            }
            if (excluded)
            {
                if (constructMetadata)
                {
                    m_classMethodDescriptors [m] = new MethodDescriptor (m_methodName, method.getDescriptor (cls), IMetadataConstants.METHOD_EXCLUDED, m_methodBlockSizes, null, 0);
                }
            }
            else
            {
                if ((method.getAccessFlags () & (IAccessFlags.ACC_ABSTRACT | IAccessFlags.ACC_NATIVE)) != 0)
                {
                    if (constructMetadata)
                    {
                        m_classMethodDescriptors [m] = new MethodDescriptor (m_methodName, method.getDescriptor (cls), IMetadataConstants.METHOD_ABSTRACT_OR_NATIVE, m_methodBlockSizes, null, 0);
                    }
                    if (trace2) m_log.trace2 ("visit", "skipped " + (method.isAbstract () ? "abstract" : "native") + " method");
                }
                else 
                {
                    m_methodFirstLine = 0;
                    m_methodID = m;
                    if (isClinit)
                    {
                        m_clinitID = m;
                        if (trace2) m_log.trace2 ("visit", "<clinit> method delayed");
                    }
                    else
                    {
                        final IAttributeCollection attributes = method.getAttributes ();
                        final int attributeCount = attributes.size ();
                        for (int a = 0; a < attributeCount; ++ a)
                        {
                            final Attribute_info attribute = attributes.get (a);
                            attribute.accept (this, ctx);
                        }
                        if (constructMetadata)
                        {
                            if ($assert.ENABLED) $assert.ASSERT (m_classBlockCounts [m_methodID] > 0, "invalid block count for method " + m_methodID + ": " + m_classBlockCounts [m_methodID]);
                            if ($assert.ENABLED) $assert.ASSERT (m_methodBlockSizes != null && m_methodBlockSizes.length == m_classBlockCounts [m_methodID], "invalid block sizes map for method " + m_methodID);
                            final int [][] methodBlockMetadata = m_classBlockMetadata [m_methodID];
                            final int status = (methodBlockMetadata == null ? IMetadataConstants.METHOD_NO_LINE_NUMBER_TABLE : 0);
                            m_classMethodDescriptors [m] = new MethodDescriptor (m_methodName, method.getDescriptor (cls), status, m_methodBlockSizes, methodBlockMetadata, m_methodFirstLine);
                        }
                    }                
                }
            }
        }
        final boolean instrumentClinit = false; 
        final Method_info clinit;
        if (m_clinitID >= 0)
        {
            clinit = methods.get (m_clinitID);
            m_classInstrMethodCount = originalMethodCount;
        }
        else
        {
            m_clinitStatus = IMetadataConstants.METHOD_ADDED;  
            final int attribute_name_index = cls.addCONSTANT_Utf8 (Attribute_info.ATTRIBUTE_CODE, true);
            final int name_index = cls.addCONSTANT_Utf8 (IClassDefConstants.CLINIT_NAME, true);
            final int descriptor_index = cls.addCONSTANT_Utf8 ("()V", true);
            final IAttributeCollection attributes;
            if (MARK_ADDED_ELEMENTS_SYNTHETIC)
                attributes = ElementFactory.newAttributeCollection (2);
            else
                attributes = ElementFactory.newAttributeCollection (1);
            final CodeAttribute_info code = new CodeAttribute_info (attribute_name_index,
                0, 0,
                new byte [] {(byte) _return},
                AttributeElementFactory.newExceptionHandlerTable (0),
                ElementFactory.newAttributeCollection (0));
            attributes.add (code);
            if (MARK_ADDED_ELEMENTS_SYNTHETIC)
            {
                attributes.add (new SyntheticAttribute_info (m_syntheticStringIndex));
            }
            clinit = new Method_info (IAccessFlags.ACC_STATIC | IAccessFlags.ACC_PRIVATE, name_index, descriptor_index, attributes);
            m_clinitID = cls.addMethod (clinit);
            if (trace2) m_log.trace2 ("visit", "added synthetic <clinit> method");
            m_classInstrMethodCount = originalMethodCount + 1;
        }
        if ($assert.ENABLED) $assert.ASSERT (m_classInstrMethodCount >= 0,
            "m_classInstrMethodCount not set");
        {
            m_methodFirstLine = 0;
            m_methodID = m_clinitID;
            if (trace2) m_log.trace2 ("visit", (clinit.isSynthetic () ? "synthetic " : "") + "method #" + m_methodID + ": [<clinit>]");
            final IAttributeCollection attributes = clinit.getAttributes ();
            final int attributeCount = attributes.size ();
            for (int a = 0; a < attributeCount; ++ a)
            {
                final Attribute_info attribute = attributes.get (a);
                attribute.accept (this, ctx);
            }
        }
        {
            final int attribute_name_index = cls.addCONSTANT_Utf8 (Attribute_info.ATTRIBUTE_CODE, true);
            final int name_index = cls.addCONSTANT_Utf8 (PRECLINIT_METHOD_NAME, false);
            final int descriptor_index = cls.addCONSTANT_Utf8 ("()[[Z", false);
            final IAttributeCollection attributes;
            if (MARK_ADDED_ELEMENTS_SYNTHETIC)
                attributes = ElementFactory.newAttributeCollection (2);
            else
                attributes = ElementFactory.newAttributeCollection (1);
            final ByteArrayOStream buf = new ByteArrayOStream (PRECLINIT_INIT_CAPACITY);  
            {
                final int [] blockCounts = m_classBlockCounts;
                final int instrMethodCount = m_classInstrMethodCount; 
                if ($assert.ENABLED) $assert.ASSERT (blockCounts != null && blockCounts.length >= instrMethodCount,
                    "invalid block count map");
                CodeGen.push_int_value (buf, cls, instrMethodCount);
                final int type_index = cls.addClassref ("[[Z");
                buf.write4 (_multianewarray,
                            type_index >>> 8,    
                            type_index,          
                            1); 
                buf.write4 (_dup,
                            _putstatic,
                            m_coverageFieldrefIndex >>> 8,    
                            m_coverageFieldrefIndex);          
                for (int m = 0; m < instrMethodCount; ++ m)
                {
                    final int blockCount = blockCounts [m]; 
                    if (blockCount > 0)
                    {
                        buf.write (_dup);
                        CodeGen.push_int_value (buf, cls, m);
                        CodeGen.push_int_value (buf, cls, blockCount);
                        buf.write3 (_newarray,
                                    4, 
                                    _aastore);
                    }
                }
                {
                    buf.write (_dup);
                    CodeGen.push_constant_index (buf, m_classNameConstantIndex);
                    buf.write3 (_ldc2_w,
                                m_stampIndex >>> 8,    
                                m_stampIndex);         
                    buf.write3 (_invokestatic,
                                m_registerMethodrefIndex >>> 8,    
                                m_registerMethodrefIndex);         
                }
                buf.write (_areturn);
            }
            final CodeAttribute_info code = new CodeAttribute_info (attribute_name_index,
                5, 0, 
                EMPTY_BYTE_ARRAY,
                AttributeElementFactory.newExceptionHandlerTable (0),
                ElementFactory.newAttributeCollection (0));
            code.setCode (buf.getByteArray (), buf.size ());
            attributes.add (code);
            if (MARK_ADDED_ELEMENTS_SYNTHETIC)
            {
                attributes.add (new SyntheticAttribute_info (m_syntheticStringIndex));
            }
            final Method_info preclinit = new Method_info (IAccessFlags.ACC_STATIC | IAccessFlags.ACC_PRIVATE, name_index, descriptor_index, attributes);
            cls.addMethod (preclinit);
            if (trace2) m_log.trace2 ("visit", "added synthetic pre-<clinit> method");
        }
        if (constructMetadata)
        {
            if ($assert.ENABLED) $assert.ASSERT (m_classBlockCounts [m_methodID] > 0, "invalid block count for method " + m_methodID + " (" + IClassDefConstants.CLINIT_NAME + "): " + m_classBlockCounts [m_methodID]);
            if ($assert.ENABLED) $assert.ASSERT (m_methodBlockSizes != null && m_methodBlockSizes.length == m_classBlockCounts [m_methodID], "invalid block sizes map for method " + m_methodID);
            final int [][] methodBlockMetadata = m_classBlockMetadata [m_methodID];
            m_clinitStatus |= (methodBlockMetadata == null ? IMetadataConstants.METHOD_NO_LINE_NUMBER_TABLE : 0);
            if ((m_clinitStatus & IMetadataConstants.METHOD_ADDED) == 0)
                m_classMethodDescriptors [m_methodID] = new MethodDescriptor (IClassDefConstants.CLINIT_NAME, clinit.getDescriptor (cls), m_clinitStatus, m_methodBlockSizes, methodBlockMetadata, m_methodFirstLine);
        }
        return ctx;
    }
    public Object visit (final IAttributeCollection attributes, Object ctx)
    {
        for (int a = 0, aCount = attributes.size (); a < aCount; ++ a)
        {
            attributes.get (a).accept (this, ctx);
        } 
        return ctx;
    }
    public Object visit (final CodeAttribute_info attribute, final Object ctx)
    {
        final boolean trace2 = m_log.atTRACE2 ();
        final boolean trace3 = m_log.atTRACE3 ();
        final byte [] code = attribute.getCode ();
        final int codeSize = attribute.getCodeSize ();
        if (trace2) m_log.trace2 ("visit", "code attribute for method #" + m_methodID + ": size = " + codeSize);
        final IntSet leaders = new IntSet ();
        final IntIntMap  instructionMap = new IntIntMap ();
        leaders.add (0); 
        final IExceptionHandlerTable exceptions = attribute.getExceptionTable ();
        final int exceptionCount = exceptions.size ();
        for (int e = 0; e < exceptionCount; ++ e)
        {
            final Exception_info exception = exceptions.get (e);
            leaders.add (exception.m_handler_pc);
        }
        final IntObjectMap branches = new IntObjectMap ();
        boolean branch = false;
        boolean wide = false;
        int instructionCount = 0;
        instructionMap.put (0, 0);
        for (int ip = 0; ip < codeSize; )
        {
            final int opcode = 0xFF & code [ip];
            int size = 0; 
            { 
                int iv, ov;
                if (branch)
                {
                    leaders.add (ip);
                    branch = false;
                }
                switch (opcode)
                {
                    case _ifeq:
                    case _iflt:
                    case _ifle:
                    case _ifne:
                    case _ifgt:
                    case _ifge:
                    case _ifnull:
                    case _ifnonnull:
                    case _if_icmpeq:
                    case _if_icmpne:
                    case _if_icmplt:
                    case _if_icmpgt:
                    case _if_icmple:
                    case _if_icmpge:
                    case _if_acmpeq:
                    case _if_acmpne:
                    {
                        int scan = ip + 1;
                        ov = (code [scan] << 8) | (0xFF & code [++ scan]);
                        final int target = ip + ov;
                        leaders.add (target); 
                        branches.put (ip, new IFJUMP2 (opcode, target));
                        branch = true;
                    }
                    break;
                    case _goto:
                    case _jsr:
                    {
                        int scan = ip + 1;
                        ov = (code [scan] << 8) | (0xFF & code [++ scan]);
                        final int target = ip + ov;
                        leaders.add (target); 
                        branches.put (ip, new JUMP2 (opcode, target));
                        branch = true;
                    }
                    break;
                    case _lookupswitch:
                    {
                        int scan = ip + 4 - (ip & 3); 
                        ov = (code [scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                        leaders.add (ip + ov);
                        final int npairs = ((0xFF & code [++ scan]) << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                        final int [] keys = new int [npairs];
                        final int [] targets = new int [npairs + 1];
                        targets [0] = ip + ov;
                        for (int p = 0; p < npairs; ++ p)
                        {
                            iv = (code [++ scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                            keys [p] = iv;
                            ov = (code [++ scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                            targets [p + 1] = ip + ov;
                            leaders.add (ip + ov);
                        }
                        branches.put (ip, new LOOKUPSWITCH (keys, targets));
                        branch = true;
                        size = ip - scan - 1; 
                    }
                    break;
                    case _tableswitch:
                    {
                        int scan = ip + 4 - (ip & 3); 
                        ov = (code [scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                        leaders.add (ip + ov);
                        final int low = (code [++ scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                        final int high = (code [++ scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                        final int [] targets = new int [high - low + 2];
                        targets [0] = ip + ov;
                        for (int index = low; index <= high; ++ index)
                        {
                            ov = (code [++ scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                            targets [index - low + 1] = ip + ov;
                            leaders.add (ip + ov);
                        }
                        branches.put (ip, new TABLESWITCH (low, high, targets));
                        branch = true;
                        size = ip - scan - 1; 
                    }
                    break;
                    case _goto_w:
                    case _jsr_w:
                    {
                        int scan = ip + 1;
                        ov = (code [scan] << 24) | ((0xFF & code [++ scan]) << 16) | ((0xFF & code [++ scan]) << 8) | (0xFF & code [++ scan]);
                        final int target = ip + ov;
                        leaders.add (target);
                        branches.put (ip, new JUMP4 (opcode, target));
                        branch = true;
                    }
                    break;
                    case _ret:
                    {
                        int scan = ip + 1;
                        iv = wide ? (((0xFF & code [scan]) << 8) | (0xFF & code [++ scan])) : (0xFF & code [scan]);
                        branches.put (ip, new RET (opcode, iv));
                        branch = true;
                    } 
                    break; 
                    case _athrow:
                    case _ireturn:
                    case _lreturn:
                    case _freturn:
                    case _dreturn:
                    case _areturn:
                    case _return:
                    {
                        branches.put (ip, new TERMINATE (opcode));
                        branch = true;
                    }
                    break;
                } 
            } 
            if (size == 0)
                size = (wide ? WIDE_SIZE : NARROW_SIZE) [opcode];
            else
                size = -size;
            ip += size;
            wide = (opcode == _wide);
            instructionMap.put (ip, ++ instructionCount);
        } 
        final int blockCount = leaders.size ();
        if (trace2) m_log.trace2 ("visit", "method contains " + blockCount + " basic blocks");
        final BlockList blocks = new BlockList (blockCount);
        final int [] _leaders = new int [blockCount + 1]; 
        leaders.values (_leaders, 0);
        _leaders [blockCount] = codeSize;
        Arrays.sort (_leaders);
        final int [] _branch_locations = branches.keys (); 
        Arrays.sort (_branch_locations);
        final IntIntMap leaderToBlockID = new IntIntMap (_leaders.length);
        if (m_metadata)
        {
            m_methodBlockSizes = new int [blockCount];
            m_methodBlockOffsets = _leaders;
        }
        consumeSignatureData (m_methodID, _leaders);
        final int [] intHolder = new int [1];
        int instr_count = 0, prev_instr_count;
        for (int bl = 0, br = 0; bl < blockCount; ++ bl)
        {
            final Block block = new Block ();
            blocks.m_blocks.add (block);
            final int leader = _leaders [bl];
            block.m_first = leader; 
            leaderToBlockID.put (leader, bl);
            final int next_leader = _leaders [bl + 1];
            boolean branchDelimited = false;
            prev_instr_count = instr_count;
            if (_branch_locations.length > br)
            {
                final int next_branch_location = _branch_locations [br];
                if (next_branch_location < next_leader)
                {
                    branchDelimited = true;
                    block.m_length = next_branch_location - leader; 
                    if ($assert.ENABLED)
                        $assert.ASSERT (instructionMap.get (next_branch_location, intHolder), "no mapping for " + next_branch_location);
                    else
                        instructionMap.get (next_branch_location, intHolder);
                    instr_count = intHolder [0] + 1; 
                    block.m_branch = (Branch) branches.get (next_branch_location);
                    block.m_branch.m_parentBlockID = bl; 
                    ++ br;
                }
            }
            if (! branchDelimited)
            {
                block.m_length = next_leader - leader; 
                if ($assert.ENABLED)
                    $assert.ASSERT (instructionMap.get (next_leader, intHolder), "no mapping for " + next_leader);
                else
                    instructionMap.get (next_leader, intHolder);
                instr_count = intHolder [0];
            }
            block.m_instrCount = instr_count - prev_instr_count; 
            if ($assert.ENABLED) $assert.ASSERT (block.m_length == 0 || block.m_instrCount > 0, "invalid instr count for block " + bl + ": " + block.m_instrCount);
            if (m_metadata) m_methodBlockSizes [bl] = block.m_instrCount; 
        }
        final Block [] _blocks = (Block []) blocks.m_blocks.toArray (new Block [blockCount]);
        for (int l = 0; l < blockCount; ++ l)
        {
            final Block block = _blocks [l];
            if (block.m_branch != null)
            {
                final int [] targets = block.m_branch.m_targets;
                if (targets != null)
                {
                    for (int t = 0, targetCount = targets.length; t < targetCount; ++ t)
                    {
                        if ($assert.ENABLED)
                            $assert.ASSERT (leaderToBlockID.get (targets [t], intHolder), "no mapping for " + targets [t]);
                        else
                            leaderToBlockID.get (targets [t], intHolder);
                        targets [t] = intHolder [0];
                    }
                }
            }
        }
        m_classBlockCounts [m_methodID] = blockCount;
        {
            if (trace2) m_log.trace2 ("visit", "instrumenting... ");
            final int localVarIndex = attribute.m_max_locals ++;
            if (m_methodID == m_clinitID) 
            {
                m_stampIndex = m_cls.getConstants ().add (new CONSTANT_Long_info (m_classSignature));
                blocks.m_header = new clinitHeader (this, localVarIndex);
            }
            else
                blocks.m_header = new methodHeader (this, localVarIndex);
            int headerMaxStack = blocks.m_header.maxstack ();
            int methodMaxStack = 0;
            for (int l = 0; l < blockCount; ++ l)
            {
                final Block block = _blocks [l];
                final CodeSegment insertion = new BlockSegment (this, localVarIndex, l);
                block.m_insertion = insertion;
                final int insertionMaxStack = insertion.maxstack (); 
                if (insertionMaxStack > methodMaxStack)
                    methodMaxStack = insertionMaxStack;
            }
            {
                final int oldMaxStack = attribute.m_max_stack;
                attribute.m_max_stack += methodMaxStack; 
                if (headerMaxStack > attribute.m_max_stack)
                attribute.m_max_stack = headerMaxStack;
                if (trace3) m_log.trace3 ("visit", "increasing maxstack by " + (attribute.m_max_stack - oldMaxStack));
            }
            if ($assert.ENABLED) $assert.ASSERT (blocks.m_header != null, "header not set");
        }
        if (trace2) m_log.trace2 ("visit", "assembling... ");
        int newcodeCapacity = codeSize << 1;
        if (newcodeCapacity < EMIT_CTX_MIN_INIT_CAPACITY) newcodeCapacity = EMIT_CTX_MIN_INIT_CAPACITY;
        final ByteArrayOStream newcode = new ByteArrayOStream (newcodeCapacity); 
        final EmitCtx emitctx = new EmitCtx (blocks, newcode);
        final int [] jumpAdjOffsets = new int [blockCount]; 
        final int [] jumpAdjMap = new int [jumpAdjOffsets.length]; 
        if ($assert.ENABLED) $assert.ASSERT (jumpAdjOffsets.length == jumpAdjMap.length,
            "jumpAdjOffsets and jumpAdjMap length mismatch");
        blocks.m_header.emit (emitctx);
        jumpAdjMap [0] = emitctx.m_out.size ();
        for (int l = 0; l < blockCount; ++ l)
        {
            final Block block = _blocks [l];
            if (l + 1 < blockCount)
            {
                jumpAdjOffsets [l + 1] = _blocks [l].m_first + _blocks [l].m_length; 
            }
            block.emit (emitctx, code);
            if (l + 1 < blockCount)
            {
                jumpAdjMap [l + 1] = emitctx.m_out.size () - _blocks [l + 1].m_first;
            }
        }
        m_methodJumpAdjOffsets = jumpAdjOffsets;
        m_methodJumpAdjValues = jumpAdjMap;
        if (trace3)
        {
            final StringBuffer s = new StringBuffer ("jump adjustment map:" + EOL);
            for (int a = 0; a < jumpAdjOffsets.length; ++ a)
            {
                s.append ("    " + jumpAdjOffsets [a] + ": +" + jumpAdjMap [a]);
                if (a < jumpAdjOffsets.length - 1) s.append (EOL);
            }
            m_log.trace3 ("visit", s.toString ());
        }
        final byte [] _newcode = newcode.getByteArray (); 
        final int _newcodeSize = newcode.size ();
        if (trace3) m_log.trace3 ("visit", "backpatching " + emitctx.m_backpatchQueue.size () + " ip(s)");
        for (Iterator i = emitctx.m_backpatchQueue.iterator (); i.hasNext (); )
        {
            final int [] patchData = (int []) i.next ();
            int ip = patchData [1];
            if ($assert.ENABLED) $assert.ASSERT (patchData != null, "null patch data for ip " + ip);
            final int jump = _blocks [patchData [3]].m_first - patchData [2];
            if ($assert.ENABLED) $assert.ASSERT (jump > 0, "negative backpatch jump offset " + jump + " for ip " + ip);
            switch (patchData [0])
            {
                case 4:
                {
                    _newcode [ip ++] = (byte) (jump >>> 24);
                    _newcode [ip ++] = (byte) (jump >>> 16);
                } 
                case 2:
                {
                    _newcode [ip ++] = (byte) (jump >>> 8);
                    _newcode [ip] = (byte) jump;
                }
            }
        }
        attribute.setCode (_newcode, _newcodeSize);
        if (trace2) m_log.trace2 ("visit", "method assembled into " + _newcodeSize + " code bytes");
        final IExceptionHandlerTable exceptionTable = attribute.getExceptionTable ();
        for (int e = 0; e < exceptionTable.size (); ++ e)
        {
            final Exception_info exception = exceptionTable.get (e);
            int adjSegment = lowbound (jumpAdjOffsets, exception.m_start_pc);
            exception.m_start_pc += jumpAdjMap [adjSegment];
            adjSegment = lowbound (jumpAdjOffsets, exception.m_end_pc);
            exception.m_end_pc += jumpAdjMap [adjSegment];
            adjSegment = lowbound (jumpAdjOffsets, exception.m_handler_pc);
            exception.m_handler_pc += jumpAdjMap [adjSegment];
        }
        final IAttributeCollection attributes = attribute.getAttributes ();
        final int attributeCount = attributes.size ();
        for (int a = 0; a < attributeCount; ++ a)
        {
            final Attribute_info nested = attributes.get (a);
            nested.accept (this, ctx);
        }
        return ctx;
    }
    public Object visit (final LineNumberTableAttribute_info attribute, final Object ctx)
    {
        final boolean trace2 = m_log.atTRACE2 ();
        final boolean trace3 = m_log.atTRACE3 (); 
        if (trace2) m_log.trace2 ("visit", "attribute: [" + attribute.getName (m_cls) + "]");
        final int lineCount = attribute.size ();
        if (m_metadata)
        {
            if (trace2) m_log.trace2 ("visit", "processing line number table for metadata...");
            final int blockCount = m_classBlockCounts [m_methodID];
            if ($assert.ENABLED) $assert.ASSERT (blockCount > 0, "invalid method block count for method " + m_methodID);
            final int [][] blockLineMap = new int [blockCount][];
            if ($assert.ENABLED) $assert.ASSERT (blockCount + 1 == m_methodBlockOffsets.length,
                    "invalid m_methodBlockOffsets");
            if (lineCount == 0)
            {
                for (int bl = 0; bl < blockCount; ++ bl)
                    blockLineMap [bl] = EMPTY_INT_ARRAY;
            }
            else
            {
                final LineNumber_info [] sortedLines = new LineNumber_info [attribute.size ()];
                for (int l = 0; l < lineCount; ++ l)
                {
                    final LineNumber_info line = attribute.get (l);
                    sortedLines [l] = line;
                }
                Arrays.sort (sortedLines, LINE_NUMBER_COMPARATOR);
                final int [] methodBlockOffsets = m_methodBlockOffsets;
                LineNumber_info line = sortedLines [0]; 
                LineNumber_info prev_line = null;
                m_methodFirstLine = line.m_line_number;
                for (int bl = 0, l = 0; bl < blockCount; ++ bl)
                {                   
                    final IntSet blockLines = new IntSet ();
                    if ((prev_line != null) && (line.m_start_pc > methodBlockOffsets [bl]))
                    {
                        blockLines.add (prev_line.m_line_number);
                    }
                    while (line.m_start_pc < methodBlockOffsets [bl + 1])
                    {
                        blockLines.add (line.m_line_number);
                        if (l == lineCount - 1)
                            break;
                        else
                        {
                            prev_line = line;
                            line = sortedLines [++ l]; 
                        }
                    }
                    blockLineMap [bl] = blockLines.values ();
                }                
            }
            m_classBlockMetadata [m_methodID] = blockLineMap;
            if (trace3)
            {
                StringBuffer s = new StringBuffer ("block-line map for method #" + m_methodID + ":");
                for (int bl = 0; bl < blockCount; ++ bl)
                {
                    s.append (EOL);
                    s.append ("    block " + bl + ": ");
                    final int [] lines = blockLineMap [bl];
                    for (int l = 0; l < lines.length; ++ l)
                    {
                        if (l != 0) s.append (", ");
                        s.append (lines [l]);
                    }
                }
                m_log.trace3 ("visit", s.toString ());
            }
        }
        for (int l = 0; l < lineCount; ++ l)
        {
            final LineNumber_info line = attribute.get (l);
            int adjSegment = lowbound (m_methodJumpAdjOffsets, line.m_start_pc);                
            line.m_start_pc += m_methodJumpAdjValues [adjSegment];
        }
        return ctx;
    }
    public Object visit (final ExceptionsAttribute_info attribute, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final ConstantValueAttribute_info attribute, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final SourceFileAttribute_info attribute, final Object ctx)
    {
        m_classSrcFileName = attribute.getSourceFile (m_cls).m_value;
        return ctx;
    }
    public Object visit (final SyntheticAttribute_info attribute, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final BridgeAttribute_info attribute, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final InnerClassesAttribute_info attribute, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final GenericAttribute_info attribute, final Object ctx)
    {
        return ctx;
    }
    private static final class BlockList
    {
        BlockList ()
        {
            m_blocks = new ArrayList ();
        }
        BlockList (final int capacity)
        {
            m_blocks = new ArrayList (capacity);
        }
        final List  m_blocks; 
        CodeSegment m_header;
    } 
    private static final class Block
    {
        int m_first;    
        int m_length;   
        int m_instrCount; 
        void emit (final EmitCtx ctx, final byte [] code) 
        {
            final ByteArrayOStream out = ctx.m_out;
            final int first = m_first;
            m_first = out.size (); 
            for (int i = 0, length = m_length; i < length; ++ i)
            {
                out.write (code [first + i]);
            }
            if (m_insertion != null)
                m_insertion.emit (ctx);
            if (m_branch != null)
                m_branch.emit (ctx);
        }
        public CodeSegment m_insertion;
        public Branch m_branch; 
    } 
    static final class EmitCtx
    {
        EmitCtx (final BlockList blocks, final ByteArrayOStream out)
        {
            m_blocks = blocks;
            m_out = out;
            m_backpatchQueue = new ArrayList ();
        }
        final BlockList m_blocks;
        final ByteArrayOStream m_out;
        final List  m_backpatchQueue;
    } 
    static abstract class Branch
    {
        protected Branch (final int opcode, final int [] targets)
        {
            m_opcode = (byte) opcode;
            m_targets = targets;
        }
        int maxlength () { return 1; }
        abstract void emit (EmitCtx ctx);
        protected final void emitJumpOffset2 (final EmitCtx ctx, final int ip, final int targetBlockID)
        {
            final ByteArrayOStream out = ctx.m_out;
            if (targetBlockID <= m_parentBlockID)
            {
                final int jumpOffset = ((Block) ctx.m_blocks.m_blocks.get (targetBlockID)).m_first - ip;
                out.write2 (jumpOffset >>> 8,   
                            jumpOffset);         
            }
            else
            {
                final int jumpOffsetLocation = out.size (); 
                out.write2 (0,
                            0);
                ctx.m_backpatchQueue.add (new int [] {2, jumpOffsetLocation, ip, targetBlockID});
            }
        }
        protected final void emitJumpOffset4 (final EmitCtx ctx, final int ip, final int targetBlockID)
        {
            final ByteArrayOStream out = ctx.m_out;
            if (targetBlockID <= m_parentBlockID)
            {
                final int jumpOffset = ((Block) ctx.m_blocks.m_blocks.get (targetBlockID)).m_first - ip;
                out.write4 (jumpOffset >>> 24,    
                            jumpOffset >>> 16,    
                            jumpOffset >>> 8,     
                            jumpOffset);           
            }
            else
            {
                final int jumpOffsetLocation = out.size (); 
                out.write4 (0,
                            0,
                            0,
                            0);
                ctx.m_backpatchQueue.add (new int [] {4, jumpOffsetLocation, ip, targetBlockID});
            }
        } 
        final byte m_opcode;
        final int [] m_targets; 
        int m_parentBlockID;
    } 
    static final class TERMINATE extends Branch 
    {
        TERMINATE (final int opcode)
        {
            super (opcode, null);
        }      
        int length () { return 1; }
        void emit (final EmitCtx ctx)
        {
            ctx.m_out.write (m_opcode);
        }
    } 
    static final class RET extends Branch 
    {
        RET (final int opcode, final int varindex)
        {
            super (opcode, null);
            m_varindex = varindex;
        }      
        int length () { return (m_varindex <= 0xFF) ? 2 : 3; }
        void emit (final EmitCtx ctx)
        {
            final ByteArrayOStream out = ctx.m_out;
            if (m_varindex <= 0xFF)
            {
                out.write2 (m_opcode,
                            m_varindex);  
            }
            else
            {
                out.write4 (_wide,
                            m_opcode,
                            m_varindex >>> 8,   
                            m_varindex);         
            }
        }
        final int m_varindex;
    } 
    static final class JUMP2 extends Branch 
    {
        JUMP2 (final int opcode, final int target)
        {
            super (opcode, new int [] {target});
        }
        int maxlength () { return 5; }
        void emit (final EmitCtx ctx)
        {
            final ByteArrayOStream out = ctx.m_out;
            final int targetBlockID = m_targets [0];
            final int ip = out.size ();
            out.write (m_opcode);
            emitJumpOffset2 (ctx, ip, targetBlockID);
        }
    } 
    static final class JUMP4 extends Branch 
    {
        JUMP4 (final int opcode, final int target)
        {
            super (opcode, new int [] {target});
        }
        int maxlength () { return 5; }
        void emit (final EmitCtx ctx)
        {
            final ByteArrayOStream out = ctx.m_out;
            final int targetBlockID = m_targets [0];
            final int ip = out.size ();
            out.write (m_opcode);
            emitJumpOffset4 (ctx, ip, targetBlockID);
        }
    } 
    static final class IFJUMP2 extends Branch 
    {
        IFJUMP2 (final int opcode, final int target)
        {
            super (opcode, new int [] {target});
        }
        int maxlength () { return 8; }
        void emit (final EmitCtx ctx)
        {
            final ByteArrayOStream out = ctx.m_out;
            final int targetBlockID = m_targets [0];
            final int ip = out.size ();
            out.write (m_opcode);
            emitJumpOffset2 (ctx, ip, targetBlockID);
        }
    } 
    static final class LOOKUPSWITCH extends Branch
    {
        LOOKUPSWITCH (final int [] keys, final int [] targets )
        {
            super (_lookupswitch, targets);
            m_keys = keys;
        }
        int maxlength () { return 12 + (m_keys.length << 3); }
        void emit (final EmitCtx ctx)
        {
            final ByteArrayOStream out = ctx.m_out;
            final int ip = out.size ();
            out.write (m_opcode);
            for (int p = 0, padCount = 3 - (ip & 3); p < padCount; ++ p) out.write (0);
            emitJumpOffset4 (ctx, ip, m_targets [0]);
            final int npairs = m_keys.length;
            out.write4 (npairs >>> 24,  
                        npairs >>> 16,  
                        npairs >>> 8,   
                        npairs);        
            for (int t = 1; t < m_targets.length; ++ t)
            {
                final int key = m_keys [t - 1];
                out.write4 (key >>> 24,  
                            key >>> 16,  
                            key >>> 8,   
                            key);         
                emitJumpOffset4 (ctx, ip, m_targets [t]); 
            }
        }
        final int [] m_keys;
    } 
    static final class TABLESWITCH extends Branch
    {
        TABLESWITCH (final int low, final int high, final int [] targets )
        {
            super (_tableswitch, targets);
            m_low = low;
            m_high = high;
        }
        int maxlength () { return 12 + (m_targets.length << 2); }
        void emit (final EmitCtx ctx)
        {
            final ByteArrayOStream out = ctx.m_out;
            final int ip = out.size ();
            out.write (m_opcode);
            for (int p = 0, padCount = 3 - (ip & 3); p < padCount; ++ p) out.write (0);
            emitJumpOffset4 (ctx, ip, m_targets [0]);
            final int low = m_low;
            out.write4 (low >>> 24,  
                        low >>> 16,  
                        low >>> 8,   
                        low);        
            final int high = m_high;
            out.write4 (high >>> 24,  
                        high >>> 16,  
                        high >>> 8,   
                        high);        
            for (int t = 1; t < m_targets.length; ++ t)
            {
                emitJumpOffset4 (ctx, ip, m_targets [t]); 
            }
        }
        final int m_low, m_high;
    } 
    static abstract class CodeSegment
    {
        CodeSegment (final InstrVisitor visitor)
        {
            m_visitor = visitor; 
        }
        abstract int length ();
        abstract int maxstack ();
        abstract void emit (EmitCtx ctx);
        final InstrVisitor m_visitor;
    } 
    static final class clinitHeader extends CodeSegment
    {
        clinitHeader (final InstrVisitor visitor, final int localVarIndex)
        {
            super (visitor);
            final ByteArrayOStream buf = new ByteArrayOStream (CLINIT_HEADER_INIT_CAPACITY); 
            m_buf = buf;
            final ClassDef cls = visitor.m_cls;
            final int [] blockCounts = visitor.m_classBlockCounts;
            final int instrMethodCount = visitor.m_classInstrMethodCount; 
            if ($assert.ENABLED) $assert.ASSERT (blockCounts != null && blockCounts.length >= instrMethodCount,
                "invalid block count map");
            final int coverageFieldrefIndex = visitor.m_coverageFieldrefIndex;
            final int preclinitMethodrefIndex = visitor.m_preclinitMethodrefIndex;
            final int classNameConstantIndex = visitor.m_classNameConstantIndex;
            if ($assert.ENABLED)
            {
                $assert.ASSERT (coverageFieldrefIndex > 0, "invalid coverageFieldrefIndex");
                $assert.ASSERT (preclinitMethodrefIndex > 0, "invalid registerMethodrefIndex");
                $assert.ASSERT (classNameConstantIndex > 0, "invalid classNameConstantIndex");
            }
            buf.write3 (_invokestatic,
                        preclinitMethodrefIndex >>> 8,    
                        preclinitMethodrefIndex);         
            CodeGen.push_int_value (buf, cls, visitor.m_methodID);
            buf.write (_aaload);
            CodeGen.store_local_object_var (buf, localVarIndex);
        }
        int length () { return m_buf.size (); }
        int maxstack () { return 2; } 
        void emit (final EmitCtx ctx)
        {
            try
            {
                m_buf.writeTo (ctx.m_out);
            }
            catch (IOException ioe)
            {
                if ($assert.ENABLED) $assert.ASSERT (false, ioe.toString ());
            }
        }
        private final ByteArrayOStream m_buf;
        private static final int CLINIT_HEADER_INIT_CAPACITY = 32; 
    } 
    static final class methodHeader extends CodeSegment
    {
        methodHeader (final InstrVisitor visitor, final int localVarIndex)
        {
            super (visitor);
            final ByteArrayOStream buf = new ByteArrayOStream (HEADER_INIT_CAPACITY);
            m_buf = buf;
            final ClassDef cls = visitor.m_cls;
            final int coverageFieldrefIndex = visitor.m_coverageFieldrefIndex;
            final int preclinitMethodrefIndex = visitor.m_preclinitMethodrefIndex;
            buf.write4 (_getstatic,
                        coverageFieldrefIndex >>> 8, 
                        coverageFieldrefIndex,       
                        _dup);
            buf.write3 (_ifnonnull, 
                        0,
                        3 +  4);
            {
                buf.write4 (_pop,
                            _invokestatic,
                            preclinitMethodrefIndex >>> 8,    
                            preclinitMethodrefIndex);         
            }
            CodeGen.push_int_value (buf, cls, visitor.m_methodID);
            buf.write (_aaload);
            CodeGen.store_local_object_var (buf, localVarIndex);
        }
        int length () { return m_buf.size (); }
        int maxstack () { return 2; } 
        void emit (final EmitCtx ctx)
        {
            try
            {
                m_buf.writeTo (ctx.m_out);
            }
            catch (IOException ioe)
            {
                if ($assert.ENABLED) $assert.ASSERT (false, ioe.toString ());
            }
        }
        private final ByteArrayOStream m_buf;
        private static final int HEADER_INIT_CAPACITY = 16;
    } 
    static final class BlockSegment extends CodeSegment
    {
        public BlockSegment (final InstrVisitor visitor, final int localVarIndex, final int blockID)
        {
            super (visitor);
            final ByteArrayOStream buf = new ByteArrayOStream (BLOCK_INIT_CAPACITY); 
            m_buf = buf;
            final ClassDef cls = visitor.m_cls;
            CodeGen.load_local_object_var (buf, localVarIndex);
            CodeGen.push_int_value (buf, cls, blockID);
            buf.write2 (_iconst_1,
                        _bastore);
        }
        int length () { return m_buf.size (); }
        int maxstack () { return 3; } 
        void emit (final EmitCtx ctx)
        {
            try
            {
                m_buf.writeTo (ctx.m_out);
            }
            catch (IOException ioe)
            {
                if ($assert.ENABLED) $assert.ASSERT (false, ioe.toString ());
            }
        }
        private final ByteArrayOStream m_buf;
        private static final int BLOCK_INIT_CAPACITY = 16;
    } 
    private static final class LineNumberComparator implements Comparator
    {
        public final int compare (final Object o1, final Object o2)
        {
            return ((LineNumber_info) o1).m_start_pc - ((LineNumber_info) o2).m_start_pc;
        }
    } 
    private void setClassName (final String fullName)
    {
        if ($assert.ENABLED) $assert.ASSERT (fullName != null && fullName.length () > 0,
            "null or empty input: fullName");
        final int lastSlash = fullName.lastIndexOf ('/');
        if (lastSlash < 0)
        {
            m_classPackageName = "";
            m_className = fullName;
        }
        else
        {
            if ($assert.ENABLED) $assert.ASSERT (lastSlash < fullName.length () - 1,
                "malformed class name [" + fullName + "]");
            m_classPackageName = fullName.substring (0, lastSlash);
            m_className = fullName.substring (lastSlash + 1);
        }   
    }
    private void consumeSignatureData (final int methodID, final int [] basicBlockOffsets)
    {
        final int temp1 = basicBlockOffsets.length;
        long temp2 = NBEAST * m_classSignature + (methodID + 1) * temp1;
        for (int i = 1; i < temp1; ++ i) 
        {
            temp2 = NBEAST * temp2 + basicBlockOffsets [i];
        }
        m_classSignature = temp2;
    }
    private static int lowbound (final int [] values, final int x)
    {
        int low = 0, high = values.length - 1;
        while (low <= high)
        {
            final int m = (low + high) >> 1;
            final int v = values [m];
            if (v == x)
                return m;
            else if (v < x)
                low = m + 1;
            else 
                high = m - 1;
        }
        return high;
    }
    private void reset ()
    {
        m_instrument = false;
        m_metadata = false;
        m_ignoreAlreadyInstrumented = false;
        m_cls = null;
        m_classPackageName = null;
        m_className = null;
        m_classSrcFileName = null;
        m_classBlockMetadata = null;
        m_classMethodDescriptors = null;
        m_syntheticStringIndex = -1;
        m_coverageFieldrefIndex = -1;
        m_registerMethodrefIndex = -1;
        m_preclinitMethodrefIndex = -1;
        m_classNameConstantIndex = -1;
        m_clinitID = -1;
        m_clinitStatus = 0;
        m_classInstrMethodCount = -1;
        m_classBlockCounts = null;
        m_classSignature = 0;
        m_methodID = -1;
        m_methodName = null;
        m_methodFirstLine = 0;
        m_methodBlockOffsets = null;
        m_methodJumpAdjOffsets = null;
        m_methodJumpAdjValues = null;
    }
    private final boolean m_excludeSyntheticMethods;
    private final boolean m_excludeBridgeMethods;
    private final boolean m_doSUIDCompensation;
    private final Logger m_log; 
    private boolean m_warningIssued;
    private boolean m_instrument;
    private boolean m_metadata;
    private boolean m_ignoreAlreadyInstrumented;
     ClassDef m_cls;
    private String m_classPackageName; 
    private String m_className; 
    private String m_classSrcFileName;
    private int [][][] m_classBlockMetadata; 
    private MethodDescriptor [] m_classMethodDescriptors;
    private int m_syntheticStringIndex;     
     int m_coverageFieldrefIndex;    
    private int m_registerMethodrefIndex;   
     int m_preclinitMethodrefIndex;  
     int m_classNameConstantIndex;   
    private int m_stampIndex;               
    private int m_clinitID;                 
    private int m_clinitStatus;
     int m_classInstrMethodCount;    
     int [] m_classBlockCounts;      
    private long m_classSignature;
     int m_methodID;                 
    private String m_methodName;
    private int m_methodFirstLine;
    private int [] m_methodBlockOffsets;    
    private int [] m_methodBlockSizes;
    private int [] m_methodJumpAdjOffsets;    
    private int [] m_methodJumpAdjValues;        
    private static final long NBEAST = 16661; 
    private static final String COVERAGE_FIELD_NAME = "$VR" + "c";
    private static final String SUID_FIELD_NAME = "serialVersionUID";
    private static final String PRECLINIT_METHOD_NAME = "$VR" + "i";
    private static final String JAVA_IO_SERIALIZABLE_NAME = "java/io/Serializable";
    private static final String JAVA_IO_EXTERNALIZABLE_NAME = "java/io/Externalizable";
    private static final int EMIT_CTX_MIN_INIT_CAPACITY = 64; 
    private static final int PRECLINIT_INIT_CAPACITY = 128; 
    private static final boolean MARK_ADDED_ELEMENTS_SYNTHETIC = true;
    private static final boolean SKIP_SYNTHETIC_CLASSES = false;
    private static final LineNumberComparator LINE_NUMBER_COMPARATOR = new LineNumberComparator ();
    private static final byte [] EMPTY_BYTE_ARRAY = new byte [0];
} 
