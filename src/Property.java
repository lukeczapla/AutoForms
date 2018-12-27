
import java.lang.annotation.*;

/**
 * Specifies that the annotated instance variable is a 
 *   user-inputted value to display on a form
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String value() default "";
    int order() default 0;
}
