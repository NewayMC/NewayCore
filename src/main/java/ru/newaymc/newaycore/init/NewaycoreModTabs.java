/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package ru.newaymc.newaycore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.newaymc.newaycore.NewaycoreMod;

public class NewaycoreModTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewaycoreMod.MODID);
    public static final RegistryObject<CreativeModeTab> GUNS = REGISTRY.register("guns",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.newaycore.guns")).icon(() -> new ItemStack(NewaycoreModItems.AKM.get())).displayItems((parameters, tabData) -> {
                tabData.accept(NewaycoreModItems.AKM.get());
                tabData.accept(NewaycoreModItems.M_4_A_1.get());
                tabData.accept(NewaycoreModItems.MP_5.get());
                tabData.accept(NewaycoreModItems.AR_15_SNIPER.get());
                tabData.accept(NewaycoreModBlocks.COVER_MARKER_AI.get().asItem());
                tabData.accept(NewaycoreModBlocks.OBJECT_MARKER_AI.get().asItem());
                tabData.accept(NewaycoreModItems.ELITE_SHOOTER_ENTITY_SPAWN_EGG.get());
            }).build());
}