package multybot.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import net.dv8tion.jda.api.Permission;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermissions {
    Permission[] value() default {};
}
