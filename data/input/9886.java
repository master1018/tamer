class AnnotationDriverTest {}
@Retention(RetentionPolicy.RUNTIME)
@interface NestedEnum {
    enum Vehicles {
        sports {
        };
    }
    Vehicles getVehicles();
}
