public class TypeAnnotationPosition {
    public TargetType type = TargetType.UNKNOWN;
    public List<Integer> location = List.nil();
    public int pos = -1;
    public boolean isValidOffset = false;
    public int offset = -1;
    public int[] lvarOffset = null;
    public int[] lvarLength = null;
    public int[] lvarIndex = null;
    public int bound_index = Integer.MIN_VALUE;
    public int parameter_index = Integer.MIN_VALUE;
    public int type_index = Integer.MIN_VALUE;
    public TypeAnnotationPosition wildcard_position = null;
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(type);
        switch (type) {
        case TYPECAST:
        case TYPECAST_GENERIC_OR_ARRAY:
        case INSTANCEOF:
        case INSTANCEOF_GENERIC_OR_ARRAY:
        case NEW:
        case NEW_GENERIC_OR_ARRAY:
        case NEW_TYPE_ARGUMENT:
        case NEW_TYPE_ARGUMENT_GENERIC_OR_ARRAY:
            sb.append(", offset = ");
            sb.append(offset);
            break;
        case LOCAL_VARIABLE:
        case LOCAL_VARIABLE_GENERIC_OR_ARRAY:
            sb.append(", {");
            for (int i = 0; i < lvarOffset.length; ++i) {
                if (i != 0) sb.append("; ");
                sb.append(", start_pc = ");
                sb.append(lvarOffset[i]);
                sb.append(", length = ");
                sb.append(lvarLength[i]);
                sb.append(", index = ");
                sb.append(lvarIndex[i]);
            }
            sb.append("}");
            break;
        case METHOD_RECEIVER:
            break;
        case CLASS_TYPE_PARAMETER:
        case METHOD_TYPE_PARAMETER:
            sb.append(", param_index = ");
            sb.append(parameter_index);
            break;
        case CLASS_TYPE_PARAMETER_BOUND:
        case CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
        case METHOD_TYPE_PARAMETER_BOUND:
        case METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
            sb.append(", param_index = ");
            sb.append(parameter_index);
            sb.append(", bound_index = ");
            sb.append(bound_index);
            break;
        case WILDCARD_BOUND:
        case WILDCARD_BOUND_GENERIC_OR_ARRAY:
            sb.append(", wild_card = ");
            sb.append(wildcard_position);
            break;
        case CLASS_EXTENDS:
        case CLASS_EXTENDS_GENERIC_OR_ARRAY:
            sb.append(", type_index = ");
            sb.append(type_index);
            break;
        case THROWS:
            sb.append(", type_index = ");
            sb.append(type_index);
            break;
        case CLASS_LITERAL:
        case CLASS_LITERAL_GENERIC_OR_ARRAY:
            sb.append(", offset = ");
            sb.append(offset);
            break;
        case METHOD_PARAMETER_GENERIC_OR_ARRAY:
            sb.append(", param_index = ");
            sb.append(parameter_index);
            break;
        case METHOD_TYPE_ARGUMENT:
        case METHOD_TYPE_ARGUMENT_GENERIC_OR_ARRAY:
            sb.append(", offset = ");
            sb.append(offset);
            sb.append(", type_index = ");
            sb.append(type_index);
            break;
        case METHOD_RETURN_GENERIC_OR_ARRAY:
        case FIELD_GENERIC_OR_ARRAY:
            break;
        case UNKNOWN:
            break;
        default:
        }
        if (type.hasLocation()) {
            sb.append(", location = (");
            sb.append(location);
            sb.append(")");
        }
        sb.append(", pos = ");
        sb.append(pos);
        sb.append(']');
        return sb.toString();
    }
    public boolean emitToClassfile() {
        if (type == TargetType.WILDCARD_BOUND
            || type == TargetType.WILDCARD_BOUND_GENERIC_OR_ARRAY)
            return wildcard_position.isValidOffset;
        else
            return !type.isLocal() || isValidOffset;
    }
}
