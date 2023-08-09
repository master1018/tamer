class field1 {
    @field int x;
}
@java.lang.annotation.Target({ElementType.METHOD})
@interface method {
}
class method1 {
    @method void m() {}
}
@java.lang.annotation.Target({ElementType.CONSTRUCTOR})
@interface constructor {
}
class constructor1 {
    @constructor constructor1() {}
}
@java.lang.annotation.Target({ElementType.ANNOTATION_TYPE})
@annotation
@interface annotation {
}
