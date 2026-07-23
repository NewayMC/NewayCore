package ru.newaymc.newaycore.ai;

import net.minecraft.world.entity.PathfinderMob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.nodes.BattleNodes;
import ru.newaymc.newaycore.ai.nodes.Composite;
import ru.newaymc.newaycore.ai.nodes.ConditionNodes;
import ru.newaymc.newaycore.ai.nodes.MovementNodes;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.utils.Node;

// TODO: [HIGH] Switch back from BT to FSM. Reason: Unstable & very difficult to maintain
/**
 * @deprecated
 */
@Deprecated
public class ShooterCore {
    public static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/ShooterCore");
    public static final boolean debug = true;

    public static Memory memory;

    private static PathfinderMob entity;

    public static void setup(Memory _memory) {
        entity = _memory.getMob();
        memory = _memory;
       /* for (Method method : entity.getClass().getDeclaredMethods()) {
            AiShooterSetup aiShooterSetup = method.getAnnotation(AiShooterSetup.class);
            if (aiShooterSetup != null) {
                gunUtils.setMAX_SHOOT_DISTANCE_SQR(aiShooterSetup.maxShootDistance() * aiShooterSetup.maxShootDistance());
                gunUtils.setBASE_SPREAD_DEGREES(aiShooterSetup.baseSpread());
                gunUtils.setSPREAD_INCREASE_PER_BLOCK(aiShooterSetup.spreadIncrease());
                gunUtils.setMIN_BURST_SHOTS(aiShooterSetup.minBurst());
                gunUtils.setMAX_BURST_SHOTS(aiShooterSetup.maxBurst());
                gunUtils.setMIN_BURST_COOLDOWN_TICKS(aiShooterSetup.minBurstCooldown());
                gunUtils.setMAX_BURST_COOLDOWN_TICKS(aiShooterSetup.maxBurstCooldown());
            }
        }
        buildTree().tick(memory);*/
    }

    public static Node buildTree() {
        // Priority: 1
        Node lowHealth = new ConditionNodes.IsHealthLowNode(30);
        Node findCover = new MovementNodes.MoveToCoverNode();
        Node survivalSequence = new Composite.SequenceNode(lowHealth, findCover);

        // Priority: 2
        Node canSeeTarget = new BattleNodes.CanSeeEnemyNode();
        Node hasAmmo = new ConditionNodes.HasAmmoNode();

        Node attack = new BattleNodes.AttackNode();

        Node baseAttack = new Composite.SequenceNode(canSeeTarget,
                /*new Composite.SelectorNode(hasAmmo, reload),*/ attack);

        return new Composite.ActiveSelectorNode(survivalSequence, baseAttack);
    }
}
