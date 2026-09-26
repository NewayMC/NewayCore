package ru.newaymc.newaycore.ai.memory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import ru.newaymc.newaycore.ai.entity.AbstractShooter;
import ru.newaymc.newaycore.ai.utils.Cover;
import ru.newaymc.newaycore.ai.utils.Order;
import ru.newaymc.newaycore.ai.utils.State;
import ru.newaymc.newaycore.ai.utils.TeamRole;

import java.util.*;

@Getter
@Setter
@ToString
public class Memory {
    // -- Entity --
    private AbstractShooter shooter;
    private State state = State.CALM;
    private Order currentOrder = Order.NONE;
    private boolean allowAttack = true;
    private boolean allowPatrol = false;
    // -- Covers --
    private Cover currentCover = null;
    private Map<AbstractShooter, Cover> blockedCovers = new HashMap<>();
    private boolean coverStatus = false;
    // -- Target --
    private LivingEntity target = null;
    private Vec3 lastTargetPos = null;
    // -- Team --
    private TeamRole role = TeamRole.STANDARD;
    private AbstractShooter commander = null;
    private List<AbstractShooter> teammates = null;
    private List<AbstractShooter> squadmates = new ArrayList<>(); // Only for COMMANDER

    public Memory(AbstractShooter shooter) {
        this.shooter = shooter;
    }
}
