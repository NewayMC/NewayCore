package ru.newaymc.newaycore.gun;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;

import ru.newaymc.newaycore.NewaycoreMod;

import javax.annotation.Nullable;
import java.util.Optional;

public class GunSetup {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static String gun;
    private static String fireMode;
    private static String scope, muzzle, grip;
    private static int maxAmmo;

    public static void setGun(LivingEntity entity, String _gun, String _fireMode, int _maxAmmo, String _scope, String _muzzle, String _grip) {
        gun = _gun;
        fireMode = _fireMode;
        maxAmmo = _maxAmmo;
        scope = _scope;
        muzzle = _muzzle;
        grip = _grip;

        GunSettings settings = buildSettings();
        ItemStack gunStack = GunItemBuilder.create()
                .setId(settings.gunId)
                .setAmmoCount(settings.maxAmmo)
                .setFireMode(getFireMode(settings.fireMode))
                .setCount(1)
                .build(NewaycoreMod.provider);

        IGun iGun = IGun.getIGunOrNull(gunStack);
        assert iGun != null;

        settings.scopeId.ifPresent(scopeId -> {
            ItemStack scopeStack = AttachmentItemBuilder.create().setId(scopeId).build();
            iGun.installAttachment(NewaycoreMod.provider, gunStack, scopeStack);
        });

        settings.muzzleId.ifPresent(muzzleId -> {
            ItemStack muzzleStack = AttachmentItemBuilder.create().setId(muzzleId).build();
            iGun.installAttachment(NewaycoreMod.provider, gunStack, muzzleStack);
        });

        settings.gripId.ifPresent(gripId -> {
            ItemStack gripStack = AttachmentItemBuilder.create().setId(gripId).build();
            iGun.installAttachment(NewaycoreMod.provider, gunStack, gripStack);
        });

        iGun.setMaxDummyAmmoAmount(gunStack, Integer.MAX_VALUE);
        iGun.setDummyAmmoAmount(gunStack, 9999);

        entity.setItemInHand(InteractionHand.MAIN_HAND, gunStack);
    }

    private static GunSettings buildSettings() {
        ResourceLocation gunId = ResourceLocation.parse("tacz:" + gun);
        ResourceLocation scopeId = ResourceLocation.parse("tacz:" + scope);
        ResourceLocation muzzleId = ResourceLocation.parse("tacz:" + muzzle);
        ResourceLocation gripId = ResourceLocation.parse("tacz:" + grip);
        return new GunSettings(gunId, fireMode, maxAmmo, scopeId, muzzleId, gripId);
    }

    private static FireMode getFireMode(String fireMode) {
        if ("AUTO".equalsIgnoreCase(fireMode)) {
            return FireMode.AUTO;
        }
        return FireMode.SEMI;
    }

    public static class GunSettings {
        public final ResourceLocation gunId;
        public final int maxAmmo;
        public final String fireMode;

        public final Optional<ResourceLocation> scopeId;
        public final Optional<ResourceLocation> muzzleId;
        public final Optional<ResourceLocation> gripId;

        public GunSettings(ResourceLocation gunId,String fireMode, int maxAmmo,  @Nullable ResourceLocation scopeId, @Nullable ResourceLocation muzzleId, @Nullable ResourceLocation gripId) {
            this.gunId = gunId;
            this.maxAmmo = maxAmmo;
            this.fireMode = fireMode;

            this.scopeId = Optional.ofNullable(scopeId);
            this.muzzleId = Optional.ofNullable(muzzleId);
            this.gripId = Optional.ofNullable(gripId);
        }
    }
}
