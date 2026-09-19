package ru.newaymc.newaycore.ai.utils;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import ru.newaymc.newaycore.ai.entity.AbstractShooter;

import java.util.List;

@Getter
@Setter
public class Memory {
    // -- Entity --
    private AbstractShooter shooter;
    private State state = State.CALM;
    private boolean allowAttack = true;
    private Cover currentCover = null;
    private boolean coverStatus = false;
    private boolean allowPatrol = false;
    // -- Target --
    private LivingEntity target = null;
    private Vec3 lastTargetPos = null;
    private long lastSeenTime = 0L;
    // -- Team --
    private List<AbstractShooter> teammates = null;

    public Memory(AbstractShooter shooter) {
        this.shooter = shooter;
    }
}
