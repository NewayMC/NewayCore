package ru.newaymc.newaycore.ai.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiShooterSetup {
    String aiType() default "standard";
    double ammunition() default 30;
    double damage() default 3;
    double speed() default 3;
    double inaccuracyAccumulation() default 3;
    int recoveryTime() default 20;
}
