
import java.lang.annotation.*;

/**
 * Specifies that the annotated class is the prototype for items (variables)
 *   of that class.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Item {
}
