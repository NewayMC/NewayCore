package ru.newaymc.newaycore.ai.utils;

import ru.newaymc.newaycore.gun.GunSetup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiShooterSetup {
    GunSetup.Type aiType() default GunSetup.Type.MACHINEGUN;
    double ammunition() default 30;
    double damage() default 3;
    double speed() default 3;
    double inaccuracyAccumulation() default 3;
    int recoveryTime() default 20;
}
