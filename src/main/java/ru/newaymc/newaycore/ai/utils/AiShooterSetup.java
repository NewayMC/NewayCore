package ru.newaymc.newaycore.ai.utils;

import ru.newaymc.newaycore.gun.DGunSetup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiShooterSetup {
    DGunSetup.Type aiType() default DGunSetup.Type.MACHINEGUN;
    double ammunition() default 30;
    double damage() default 3;
    double speed() default 3;
    double inaccuracyAccumulation() default 3;
    int recoveryTime() default 20;
}
