public class Keywords
{
  private static Hashtable m_keywords = new Hashtable();
  private static Hashtable m_axisnames = new Hashtable();
  private static Hashtable m_nodetests = new Hashtable();
  private static Hashtable m_nodetypes = new Hashtable();
  private static final String FROM_ANCESTORS_STRING = "ancestor";
  private static final String FROM_ANCESTORS_OR_SELF_STRING =
    "ancestor-or-self";
  private static final String FROM_ATTRIBUTES_STRING = "attribute";
  private static final String FROM_CHILDREN_STRING = "child";
  private static final String FROM_DESCENDANTS_STRING = "descendant";
  private static final String FROM_DESCENDANTS_OR_SELF_STRING =
    "descendant-or-self";
  private static final String FROM_FOLLOWING_STRING = "following";
  private static final String FROM_FOLLOWING_SIBLINGS_STRING =
    "following-sibling";
  private static final String FROM_PARENT_STRING = "parent";
  private static final String FROM_PRECEDING_STRING = "preceding";
  private static final String FROM_PRECEDING_SIBLINGS_STRING =
    "preceding-sibling";
  private static final String FROM_SELF_STRING = "self";
  private static final String FROM_NAMESPACE_STRING = "namespace";
  private static final String FROM_SELF_ABBREVIATED_STRING = ".";
  private static final String NODETYPE_COMMENT_STRING = "comment";
  private static final String NODETYPE_TEXT_STRING = "text";
  private static final String NODETYPE_PI_STRING = "processing-instruction";
  private static final String NODETYPE_NODE_STRING = "node";
  private static final String NODETYPE_ANYELEMENT_STRING = "*";
  public static final String FUNC_CURRENT_STRING = "current";
  public static final String FUNC_LAST_STRING = "last";
  public static final String FUNC_POSITION_STRING = "position";
  public static final String FUNC_COUNT_STRING = "count";
  static final String FUNC_ID_STRING = "id";
  public static final String FUNC_KEY_STRING = "key";
  public static final String FUNC_LOCAL_PART_STRING = "local-name";
  public static final String FUNC_NAMESPACE_STRING = "namespace-uri";
  public static final String FUNC_NAME_STRING = "name";
  public static final String FUNC_GENERATE_ID_STRING = "generate-id";
  public static final String FUNC_NOT_STRING = "not";
  public static final String FUNC_TRUE_STRING = "true";
  public static final String FUNC_FALSE_STRING = "false";
  public static final String FUNC_BOOLEAN_STRING = "boolean";
  public static final String FUNC_LANG_STRING = "lang";
  public static final String FUNC_NUMBER_STRING = "number";
  public static final String FUNC_FLOOR_STRING = "floor";
  public static final String FUNC_CEILING_STRING = "ceiling";
  public static final String FUNC_ROUND_STRING = "round";
  public static final String FUNC_SUM_STRING = "sum";
  public static final String FUNC_STRING_STRING = "string";
  public static final String FUNC_STARTS_WITH_STRING = "starts-with";
  public static final String FUNC_CONTAINS_STRING = "contains";
  public static final String FUNC_SUBSTRING_BEFORE_STRING =
    "substring-before";
  public static final String FUNC_SUBSTRING_AFTER_STRING = "substring-after";
  public static final String FUNC_NORMALIZE_SPACE_STRING = "normalize-space";
  public static final String FUNC_TRANSLATE_STRING = "translate";
  public static final String FUNC_CONCAT_STRING = "concat";
  public static final String FUNC_SYSTEM_PROPERTY_STRING = "system-property";
  public static final String FUNC_EXT_FUNCTION_AVAILABLE_STRING =
    "function-available";
  public static final String FUNC_EXT_ELEM_AVAILABLE_STRING =
    "element-available";
  public static final String FUNC_SUBSTRING_STRING = "substring";
  public static final String FUNC_STRING_LENGTH_STRING = "string-length";
  public static final String FUNC_UNPARSED_ENTITY_URI_STRING =
    "unparsed-entity-uri";
  public static final String FUNC_DOCLOCATION_STRING = "document-location";
  static
  {
    m_axisnames.put(FROM_ANCESTORS_STRING,
                    new Integer(OpCodes.FROM_ANCESTORS));
    m_axisnames.put(FROM_ANCESTORS_OR_SELF_STRING,
                    new Integer(OpCodes.FROM_ANCESTORS_OR_SELF));
    m_axisnames.put(FROM_ATTRIBUTES_STRING,
                    new Integer(OpCodes.FROM_ATTRIBUTES));
    m_axisnames.put(FROM_CHILDREN_STRING,
                    new Integer(OpCodes.FROM_CHILDREN));
    m_axisnames.put(FROM_DESCENDANTS_STRING,
                    new Integer(OpCodes.FROM_DESCENDANTS));
    m_axisnames.put(FROM_DESCENDANTS_OR_SELF_STRING,
                    new Integer(OpCodes.FROM_DESCENDANTS_OR_SELF));
    m_axisnames.put(FROM_FOLLOWING_STRING,
                    new Integer(OpCodes.FROM_FOLLOWING));
    m_axisnames.put(FROM_FOLLOWING_SIBLINGS_STRING,
                    new Integer(OpCodes.FROM_FOLLOWING_SIBLINGS));
    m_axisnames.put(FROM_PARENT_STRING,
                    new Integer(OpCodes.FROM_PARENT));
    m_axisnames.put(FROM_PRECEDING_STRING,
                    new Integer(OpCodes.FROM_PRECEDING));
    m_axisnames.put(FROM_PRECEDING_SIBLINGS_STRING,
                    new Integer(OpCodes.FROM_PRECEDING_SIBLINGS));
    m_axisnames.put(FROM_SELF_STRING,
                    new Integer(OpCodes.FROM_SELF));
    m_axisnames.put(FROM_NAMESPACE_STRING,
                    new Integer(OpCodes.FROM_NAMESPACE));
    m_nodetypes.put(NODETYPE_COMMENT_STRING,
                    new Integer(OpCodes.NODETYPE_COMMENT));
    m_nodetypes.put(NODETYPE_TEXT_STRING,
                    new Integer(OpCodes.NODETYPE_TEXT));
    m_nodetypes.put(NODETYPE_PI_STRING,
                    new Integer(OpCodes.NODETYPE_PI));
    m_nodetypes.put(NODETYPE_NODE_STRING,
                    new Integer(OpCodes.NODETYPE_NODE));
    m_nodetypes.put(NODETYPE_ANYELEMENT_STRING,
                    new Integer(OpCodes.NODETYPE_ANYELEMENT));
    m_keywords.put(FROM_SELF_ABBREVIATED_STRING,
                   new Integer(OpCodes.FROM_SELF));
    m_keywords.put(FUNC_ID_STRING,
                   new Integer(FunctionTable.FUNC_ID));
    m_keywords.put(FUNC_KEY_STRING,
                   new Integer(FunctionTable.FUNC_KEY));
    m_nodetests.put(NODETYPE_COMMENT_STRING,
                    new Integer(OpCodes.NODETYPE_COMMENT));
    m_nodetests.put(NODETYPE_TEXT_STRING,
                    new Integer(OpCodes.NODETYPE_TEXT));
    m_nodetests.put(NODETYPE_PI_STRING,
                    new Integer(OpCodes.NODETYPE_PI));
    m_nodetests.put(NODETYPE_NODE_STRING,
                    new Integer(OpCodes.NODETYPE_NODE));
  }
  static Object getAxisName(String key){
          return m_axisnames.get(key);
  }
  static Object lookupNodeTest(String key){
          return m_nodetests.get(key);
  }
  static Object getKeyWord(String key){
          return m_keywords.get(key);
  }
  static Object getNodeType(String key){
          return m_nodetypes.get(key);
  }      
}
