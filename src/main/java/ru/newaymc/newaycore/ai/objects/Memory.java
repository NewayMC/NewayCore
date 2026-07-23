package ru.newaymc.newaycore.ai.objects;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class Memory {
    private int ticks;
    // -- Entity --
    private PathfinderMob mob;
    private Cover currentCover = null;
    private boolean coverStatus = false;
    private boolean borderPatrol = false;
    // -- Target --
    private LivingEntity target = null;
    private Vec3 lastTargetPos = null;
    private boolean seeTarget = false;
    private long lastSeenTime = 0;
    // -- Faction --
    private String role = null;

    public Memory(PathfinderMob mob) {
        this.mob = mob;
    }
}
