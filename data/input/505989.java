public final class ResourcesDescriptors implements IDescriptorProvider {
    public static final String ROOT_ELEMENT = "resources";  
    public static final String NAME_ATTR = "name"; 
    public static final String TYPE_ATTR = "type"; 
    private static final ResourcesDescriptors sThis = new ResourcesDescriptors();
    public final ElementDescriptor mResourcesElement;
    public static ResourcesDescriptors getInstance() {
        return sThis;
    }
    public ElementDescriptor[] getRootElementDescriptors() {
        return new ElementDescriptor[] { mResourcesElement };
    }
    public ElementDescriptor getDescriptor() {
        return mResourcesElement;
    }
    public ElementDescriptor getElementDescriptor() {
        return mResourcesElement;
    }
    private ResourcesDescriptors() {
         ElementDescriptor color_element = new ElementDescriptor(
                "color", 
                "Color",
                "A @color@ value specifies an RGB value with an alpha channel, which can be used in various places such as specifying a solid color for a Drawable or the color to use for text.  It always begins with a # character and then is followed by the alpha-red-green-blue information in one of the following formats: #RGB, #ARGB, #RRGGBB or #AARRGGBB.",
                "http:
                new AttributeDescriptor[] {
                        new TextAttributeDescriptor(NAME_ATTR,
                                "Name*",
                                null ,
                                "The mandatory name used in referring to this color."),
                        new ColorValueDescriptor(
                                "Value*",
                                "A mandatory color value.")
                },
                null,  
                false );
         ElementDescriptor string_element = new ElementDescriptor(
                "string", 
                "String",
                "@Strings@, with optional simple formatting, can be stored and retrieved as resources. You can add formatting to your string by using three standard HTML tags: b, i, and u. If you use an apostrophe or a quote in your string, you must either escape it or enclose the whole string in the other kind of enclosing quotes.",
                "http:
                new AttributeDescriptor[] {
                        new TextAttributeDescriptor(NAME_ATTR,
                                "Name*",
                                null ,
                                "The mandatory name used in referring to this string."),
                        new TextValueDescriptor(
                                "Value*",
                                "A mandatory string value.")
                },
                null,  
                false );
         ElementDescriptor item_element = new ItemElementDescriptor(
                 "item", 
                 "Item",
                 null,  
                 null,  
                 new AttributeDescriptor[] {
                         new TextAttributeDescriptor(NAME_ATTR,
                                 "Name*",
                                 null ,
                                 "The mandatory name used in referring to this resource."),
                         new ListAttributeDescriptor(TYPE_ATTR,
                                 "Type*",
                                 null ,
                                 "The mandatory type of this resource.",
                                 ResourceType.getNames()
                         ),
                         new FlagAttributeDescriptor("format",
                                 "Format",
                                 null ,
                                 "The optional format of this resource.",
                                 new String[] {
                                     "boolean",     
                                     "color",       
                                     "dimension",   
                                     "float",       
                                     "fraction",    
                                     "integer",     
                                     "reference",   
                                     "string"       
                         }),
                         new TextValueDescriptor(
                                 "Value",
                                 "A standard string, hex color value, or reference to any other resource type.")
                 },
                 null,  
                 false );
         ElementDescriptor drawable_element = new ElementDescriptor(
                "drawable", 
                "Drawable",
                "A @drawable@ defines a rectangle of color. Android accepts color values written in various web-style formats -- a hexadecimal constant in any of the following forms: #RGB, #ARGB, #RRGGBB, #AARRGGBB. Zero in the alpha channel means transparent. The default value is opaque.",
                "http:
                new AttributeDescriptor[] {
                        new TextAttributeDescriptor(NAME_ATTR,
                                "Name*",
                                null ,
                                "The mandatory name used in referring to this drawable."),
                        new TextValueDescriptor(
                                "Value*",
                                "A mandatory color value in the form #RGB, #ARGB, #RRGGBB or #AARRGGBB.")
                },
                null,  
                false );
         ElementDescriptor dimen_element = new ElementDescriptor(
                "dimen", 
                "Dimension",
                "You can create common dimensions to use for various screen elements by defining @dimension@ values in XML. A dimension resource is a number followed by a unit of measurement. Supported units are px (pixels), in (inches), mm (millimeters), pt (points at 72 DPI), dp (density-independent pixels) and sp (scale-independent pixels)",
                "http:
                new AttributeDescriptor[] {
                        new TextAttributeDescriptor(NAME_ATTR,
                                "Name*",
                                null ,
                                "The mandatory name used in referring to this dimension."),
                        new TextValueDescriptor(
                                "Value*",
                                "A mandatory dimension value is a number followed by a unit of measurement. For example: 10px, 2in, 5sp.")
                },
                null,  
                false );
         ElementDescriptor style_element = new ElementDescriptor(
                "style", 
                "Style/Theme",
                "Both @styles and themes@ are defined in a style block containing one or more string or numerical values (typically color values), or references to other resources (drawables and so on).",
                "http:
                new AttributeDescriptor[] {
                        new TextAttributeDescriptor(NAME_ATTR,
                                "Name*",
                                null ,
                                "The mandatory name used in referring to this theme."),
                        new TextAttributeDescriptor("parent", 
                                "Parent",
                                null ,
                                "An optional parent theme. All values from the specified theme will be inherited into this theme. Any values with identical names that you specify will override inherited values."),
                },
                new ElementDescriptor[] {
                    new ElementDescriptor(
                        "item", 
                        "Item",
                        "A value to use in this @theme@. It can be a standard string, a hex color value, or a reference to any other resource type.",
                        "http:
                        new AttributeDescriptor[] {
                            new TextAttributeDescriptor(NAME_ATTR,
                                "Name*",
                                null ,
                                "The mandatory name used in referring to this item."),
                            new TextValueDescriptor(
                                "Value*",
                                "A mandatory standard string, hex color value, or reference to any other resource type.")
                        },
                        null,  
                        false )
                },
                false );
         ElementDescriptor string_array_element = new ElementDescriptor(
                 "string-array", 
                 "String Array",
                 "An array of strings. Strings are added as underlying item elements to the array.",
                 null, 
                 new AttributeDescriptor[] {
                         new TextAttributeDescriptor(NAME_ATTR,
                                 "Name*",
                                 null ,
                                 "The mandatory name used in referring to this string array."),
                 },
                 new ElementDescriptor[] {
                     new ElementDescriptor(
                         "item", 
                         "Item",
                         "A string value to use in this string array.",
                         null, 
                         new AttributeDescriptor[] {
                             new TextValueDescriptor(
                                 "Value*",
                                 "A mandatory string.")
                         },
                         null,  
                         false )
                 },
                 false );
         ElementDescriptor integer_array_element = new ElementDescriptor(
                 "integer-array", 
                 "Integer Array",
                 "An array of integers. Integers are added as underlying item elements to the array.",
                 null, 
                 new AttributeDescriptor[] {
                         new TextAttributeDescriptor(NAME_ATTR,
                                 "Name*",
                                 null ,
                                 "The mandatory name used in referring to this integer array."),
                 },
                 new ElementDescriptor[] {
                     new ElementDescriptor(
                         "item", 
                         "Item",
                         "An integer value to use in this integer array.",
                         null, 
                         new AttributeDescriptor[] {
                             new TextValueDescriptor(
                                 "Value*",
                                 "A mandatory integer.")
                         },
                         null,  
                         false )
                 },
                 false );
         mResourcesElement = new ElementDescriptor(
                        ROOT_ELEMENT,
                        "Resources",
                        null,
                        "http:
                        null,  
                        new ElementDescriptor[] {
                                string_element,
                                color_element,
                                dimen_element,
                                drawable_element,
                                style_element,
                                item_element,
                                string_array_element,
                                integer_array_element,
                        },
                        true );
    }
}
