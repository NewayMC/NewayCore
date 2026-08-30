package ru.newaymc.newaycore.ai.goals;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import ru.newaymc.newaycore.ai.GunSetup;
import ru.newaymc.newaycore.ai.entity.AbstractShooter;

import java.util.EnumSet;

public class GunAttack extends Goal {
    private final AbstractShooter mob;
    private LivingEntity target;

    private final IGunOperator operator;
    private final ItemStack gunStack;
    private final IGun iGun;

    private final float MAX_SHOOT_DISTANCE_SQR;
    private final float BASE_SPREAD_DEGREES;
    private final float SPREAD_INCREASE_PER_BLOCK;
    private final int MIN_BURST_SHOTS;
    private final int MAX_BURST_SHOTS;
    private final int MIN_BURST_COOLDOWN_TICKS;
    private final int MAX_BURST_COOLDOWN_TICKS;
    private static final int MAX_TICKS_STUCK_ACTION = 100;
    private int ticksWaitingForBusyAction = 0;
    private State currentState = State.IDLE;
    private enum State {
        IDLE,
        BURST_FIRING,
        BURST_COOLDOWN
    }

    private int burstShotsFired = 0;
    private int currentBurstTarget = 0;
    private int burstCooldownTicks = 0;

    private int attackDelay = 0;
    private boolean cachedHasLoS = false;

    public GunAttack(AbstractShooter mob, float maxShootDistance, float baseSpread, float spreadIncrease, int minBurst, int maxBurst, int minBurstCooldown, int maxBurstCooldown) {
        this.mob = mob;
        this.operator = IGunOperator.fromLivingEntity(mob);
        this.gunStack = this.mob.getMainHandItem();
        this.iGun = IGun.getIGunOrNull(gunStack);

        this.MAX_SHOOT_DISTANCE_SQR = maxShootDistance * maxShootDistance;
        this.BASE_SPREAD_DEGREES = baseSpread;
        this.SPREAD_INCREASE_PER_BLOCK = spreadIncrease;
        this.MIN_BURST_SHOTS = minBurst;
        this.MAX_BURST_SHOTS = maxBurst;
        this.MIN_BURST_COOLDOWN_TICKS = minBurstCooldown;
        this.MAX_BURST_COOLDOWN_TICKS = maxBurstCooldown;

        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity currentTarget = this.mob.getTarget();

        if (mob.getMemory().getState() != AbstractShooter.State.BATTLE) {
            return false;
        }

        if (currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }

        if (!(this.mob.getMainHandItem().getItem() instanceof AbstractGunItem)) {
            return false;
        }

        if (this.mob.distanceToSqr(currentTarget) > this.MAX_SHOOT_DISTANCE_SQR) {
            return false;
        }

        return this.mob.getSensing().hasLineOfSight(currentTarget);
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive() && this.mob.getMainHandItem().getItem() instanceof AbstractGunItem;
    }

    @Override
    public void start() {
        this.attackDelay = 3 + this.mob.getRandom().nextInt(5);
        this.target = this.mob.getTarget();

        this.burstShotsFired = 0;
        this.currentBurstTarget = 0;
        this.burstCooldownTicks = 0;
        this.currentState = State.IDLE;
        this.ticksWaitingForBusyAction = 0;

        operator.aim(true);
        if (this.target != null) {
            this.mob.getLookControl().setLookAt(this.target, 30F, 30F);
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.burstShotsFired = 0;
        this.currentBurstTarget = 0;
        this.burstCooldownTicks = 0;
        this.currentState = State.IDLE;
        this.ticksWaitingForBusyAction = 0;

        operator.aim(false);
    }

    @Override
    public void tick() {
        if (this.attackDelay > 0) {
            this.attackDelay--;

            if (this.target != null) this.mob.getLookControl().setLookAt(this.target, 30F, 30F);
            return;
        }
        this.target = this.mob.getTarget();

        if (this.target == null || !this.target.isAlive() || this.mob.level().isClientSide) {

            if (currentState != State.IDLE) {
                resetGoalStates();
            }
            return;
        }
        this.mob.getLookControl().setLookAt(this.target, 30F, 30F);

        if (!(gunStack.getItem() instanceof AbstractGunItem)) {
            if (currentState != State.IDLE) {
                resetGoalStates();
            }
            return;
        }

        if (iGun == null) {
            if (currentState != State.IDLE) {
                resetGoalStates();
            }
            return;
        }

        boolean canSeeTargetRoughly = this.mob.getSensing().hasLineOfSight(this.target);
        if (canSeeTargetRoughly) {
            this.mob.getLookControl().setLookAt(this.target, 30F, 30F);
            this.mob.yBodyRot = this.mob.yHeadRot;
        }

        boolean isDrawingTACZ = operator.getSynDrawCoolDown() > 0;
        boolean isReloadingTACZ = operator.getSynReloadState().getStateType().isReloading();
        boolean isBoltingTACZ = operator.getSynIsBolting();
        boolean isBusyTACZ = isDrawingTACZ || isReloadingTACZ || isBoltingTACZ;

        if (isBusyTACZ) {
            this.ticksWaitingForBusyAction++;
            if (this.ticksWaitingForBusyAction > MAX_TICKS_STUCK_ACTION) {
                resetGoalStates();
                return;
            }
        } else {
            this.ticksWaitingForBusyAction = 0;
        }

        double distanceSq = this.mob.distanceToSqr(this.target);
        boolean inShootRange = distanceSq <= this.MAX_SHOOT_DISTANCE_SQR;

        if ((this.mob.tickCount + this.mob.getId()) % 5 == 0) {
            this.cachedHasLoS = hasLineOfSightToTarget(this.target);
        }
        boolean hasClearLoSNow = this.cachedHasLoS;

        switch (this.currentState) {
            case IDLE:
                handleIdleState(distanceSq, canSeeTargetRoughly, isBusyTACZ);
                break;
            case BURST_COOLDOWN:
                handleBurstCooldownState(inShootRange, canSeeTargetRoughly, hasClearLoSNow);
                break;
            case BURST_FIRING:
                handleBurstFiringState(distanceSq, canSeeTargetRoughly, hasClearLoSNow, isBusyTACZ);
                break;
        }
    }

    private void resetGoalStates() {
        this.burstShotsFired = 0;
        this.currentBurstTarget = 0;
        this.burstCooldownTicks = 0;
        this.currentState = State.IDLE;
        this.ticksWaitingForBusyAction = 0;
    }

    private void handleIdleState(double distanceSq, boolean hasClearLoSNow, boolean isBusyTACZ) {
        boolean inShootRange = distanceSq <= MAX_SHOOT_DISTANCE_SQR;

        if (!inShootRange || !hasClearLoSNow) {
            return;
        }

        if (isBusyTACZ) {
            return;
        }

        if (operator.getSynDrawCoolDown() > 0) {
            operator.draw(() -> gunStack);
            return;
        }

        if (operator.getSynReloadState().getStateType().isReloading()) {
            return;
        }

        if (operator.getSynIsBolting()) {
            operator.bolt();
            return;
        }

        if (operator.getSynShootCoolDown() <= 0) {
            this.currentState = State.BURST_FIRING;
        }
    }

    private void handleBurstCooldownState(boolean inShootRange, boolean canSeeTargetRoughly, boolean hasClearLoSNow) {
        if (this.burstCooldownTicks > 0) {
            this.burstCooldownTicks--;
        } else {
            this.currentState = State.IDLE;
            this.burstShotsFired = 0;
            this.currentBurstTarget = 0;
        }

        if (!inShootRange || !canSeeTargetRoughly || !hasClearLoSNow) {
            resetGoalStates();
        }
    }

    private void handleBurstFiringState(double distanceSq, boolean canSeeTargetRoughly, boolean hasClearLoSNow, boolean isBusyTACZ) {
        boolean inShootRange = distanceSq <= MAX_SHOOT_DISTANCE_SQR;

        if (!inShootRange || !canSeeTargetRoughly || !hasClearLoSNow) {
            resetGoalStates();
            this.burstCooldownTicks = this.MIN_BURST_COOLDOWN_TICKS;
            return;
        }

        if (isBusyTACZ) {
            resetGoalStates();
            return;
        }

        if (this.currentBurstTarget == 0) {
            RandomSource rand = this.mob.getRandom();
            this.currentBurstTarget = this.MIN_BURST_SHOTS + rand.nextInt(this.MAX_BURST_SHOTS - this.MIN_BURST_SHOTS + 1);
            this.burstShotsFired = 0;
        }

        if (this.burstShotsFired >= this.currentBurstTarget) {
            this.burstCooldownTicks = this.MIN_BURST_COOLDOWN_TICKS + this.mob.getRandom().nextInt(this.MAX_BURST_COOLDOWN_TICKS - this.MIN_BURST_COOLDOWN_TICKS + 1);
            this.currentState = State.BURST_COOLDOWN;
            this.currentBurstTarget = 0;
            this.burstShotsFired = 0;

            return;
        }
        Vec3 eyePos = this.mob.getEyePosition();
        Vec3 targetCenter = this.target.position().add(0, this.target.getBbHeight() / 2.0, 0);
        Vec3 lookVec = targetCenter.subtract(eyePos).normalize();

        this.mob.getLookControl().setLookAt(targetCenter.x, targetCenter.y, targetCenter.z);
        this.mob.yBodyRot = this.mob.yHeadRot;

        double dx = lookVec.x;
        double dy = lookVec.y;
        double dz = lookVec.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float basePitch = Mth.wrapDegrees((float) (-(Math.atan2(dy, horizontal) * (180.0 / Math.PI))));
        float baseYaw = Mth.wrapDegrees((float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F);

        float distance = (float) eyePos.distanceTo(targetCenter);
        RandomSource random = this.mob.getRandom();

        float finalPitch = GunSetup.GunUtils.calculateSpread(basePitch, distance, this.BASE_SPREAD_DEGREES, this.SPREAD_INCREASE_PER_BLOCK, random);
        float finalYaw = GunSetup.GunUtils.calculateSpread(baseYaw, distance, this.BASE_SPREAD_DEGREES, this.SPREAD_INCREASE_PER_BLOCK, random);

        operator.aim(true);
        ShootResult shootResult = operator.shoot(() -> finalPitch, () -> finalYaw);

        switch (shootResult) {
            case SUCCESS:
                this.burstShotsFired++;
                break;
            case NO_AMMO:
                operator.reload();
                resetGoalStates();
                break;
            case NOT_DRAW:
                operator.draw(() -> gunStack);
                resetGoalStates();
                break;
            case NEED_BOLT:
                operator.bolt();
                resetGoalStates();
                break;
            case COOL_DOWN:
                break;
            default:
                resetGoalStates();
                break;
        }
    }

    private boolean hasLineOfSightToTarget(LivingEntity pTarget) {
        if (pTarget == null) {
            return false;
        }
        Level level = this.mob.level();
        Vec3 mobEyePos = this.mob.getEyePosition();
        Vec3 targetCenterPos = pTarget.getEyePosition();

        ClipContext context = new ClipContext(mobEyePos, targetCenterPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob);
        HitResult hitResult = level.clip(context);

        return hitResult.getType() == HitResult.Type.MISS;
    }
}
