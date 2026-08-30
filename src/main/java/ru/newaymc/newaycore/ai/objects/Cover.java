package ru.newaymc.newaycore.ai.objects;

import lombok.*;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Cover {
    private Vec3 vec3;
    private double score;
    private double distance;

    public Cover(Vec3 vec3, double distance) {
        this.vec3 = vec3;
        this.distance = distance;
        this.score = 0;
    }
}
