package ru.newaymc.newaycore.ai.objects;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import ru.newaymc.newaycore.ai.entity.AbstractShooter;

@Getter
@Setter
public class Memory {
    // -- Entity --
    private AbstractShooter shooter;
    private AbstractShooter.State state = AbstractShooter.State.CALM;
    private boolean allowAttack = true;
    private Cover currentCover = null;
    private boolean coverStatus = false;
    // -- Target --
    private Vec3 lastTargetPos = null;
    private long lastSeenTime = 0L;

    public Memory(AbstractShooter shooter) {
        this.shooter = shooter;
    }
}
