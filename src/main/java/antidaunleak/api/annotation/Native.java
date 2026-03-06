
package antidaunleak.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.METHOD, ElementType.TYPE})
public @interface Native {
    public Type type() default Type.STANDARD;

    public static enum Type {
        STANDARD,
        VMProtectBeginVirtualization,
        VMProtectBeginMutation,
        VMProtectBeginUltra;

    }
}

