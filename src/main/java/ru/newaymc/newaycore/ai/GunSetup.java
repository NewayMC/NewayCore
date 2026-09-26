package ru.newaymc.newaycore.ai;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;

import javax.annotation.Nullable;
import java.util.Optional;

public class GunSetup {

    public static void setGun(LivingEntity entity, GunSettings settings) {
        HolderLookup.Provider provider = entity.level().registryAccess();
        ItemStack gunStack = GunItemBuilder.create()
                .setId(settings.gunId)
                .setAmmoCount(settings.maxAmmo)
                .setFireMode(getFireMode(settings.fireMode))
                .setCount(1)
                .build(provider);

        IGun iGun = IGun.getIGunOrNull(gunStack);
        assert iGun != null;

        settings.scopeId.ifPresent(scopeId -> {
            ItemStack scopeStack = AttachmentItemBuilder.create().setId(scopeId).build();
            iGun.installAttachment(provider, gunStack, scopeStack);
        });

        settings.muzzleId.ifPresent(muzzleId -> {
            ItemStack muzzleStack = AttachmentItemBuilder.create().setId(muzzleId).build();
            iGun.installAttachment(provider, gunStack, muzzleStack);
        });

        settings.gripId.ifPresent(gripId -> {
            ItemStack gripStack = AttachmentItemBuilder.create().setId(gripId).build();
            iGun.installAttachment(provider, gunStack, gripStack);
        });

        iGun.setMaxDummyAmmoAmount(gunStack, Integer.MAX_VALUE);
        iGun.setDummyAmmoAmount(gunStack, 9999);

        entity.setItemInHand(InteractionHand.MAIN_HAND, gunStack);
    }

    public static GunSettings buildSettings(ResourceLocation gun, String fireMode, int maxAmmo, ResourceLocation scope, ResourceLocation muzzle, ResourceLocation grip) {
        return new GunSettings(gun, fireMode, maxAmmo, scope, muzzle, grip);
    }

    private static FireMode getFireMode(String fireMode) {
        if ("AUTO".equalsIgnoreCase(fireMode)) {
            return FireMode.AUTO;
        }
        return FireMode.SEMI;
    }

    public static class GunUtils {
        public static float calculateSpread(float idealAngle, float distance, float baseSpread, float spreadIncreasePerBlock, RandomSource random) {
            float spread = baseSpread + Math.max(0, distance - 5.0f) * spreadIncreasePerBlock;
            spread = Math.min(spread, 2.2f);

            return idealAngle + (random.nextFloat() - 0.5f) * 2.0f * spread;
        }
    }

    public static class GunSettings {
        public final ResourceLocation gunId;
        public final int maxAmmo;
        public final String fireMode;

        public final Optional<ResourceLocation> scopeId;
        public final Optional<ResourceLocation> muzzleId;
        public final Optional<ResourceLocation> gripId;

        public GunSettings(ResourceLocation gunId, String fireMode, int maxAmmo,  @Nullable ResourceLocation scopeId, @Nullable ResourceLocation muzzleId, @Nullable ResourceLocation gripId) {
            this.gunId = gunId;
            this.maxAmmo = maxAmmo;
            this.fireMode = fireMode;

            this.scopeId = Optional.ofNullable(scopeId);
            this.muzzleId = Optional.ofNullable(muzzleId);
            this.gripId = Optional.ofNullable(gripId);
        }
    }
}
