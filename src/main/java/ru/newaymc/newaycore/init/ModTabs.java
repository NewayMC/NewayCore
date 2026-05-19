package ru.newaymc.newaycore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewaycoreMod.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GUNS = REGISTRY.register("guns",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.newaycore.guns")).icon(() -> new ItemStack(ModItems.AKM.get())).displayItems((parameters, tabData) -> {
                tabData.accept(ModItems.AKM.get());
                tabData.accept(ModItems.M_4_A_1.get());
                tabData.accept(ModItems.MP_5.get());
                tabData.accept(ModItems.AR_15_SNIPER.get());
                tabData.accept(ModBlocks.COVER_MARKER_AI.get().asItem());
                tabData.accept(ModItems.SHOOTER_AI_ENTITY_SPAWN_EGG.get());
            }).build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ALS_TAB = REGISTRY.register("als_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.newaycore.als_tab")).icon(() -> new ItemStack(ModBlocks.OUTPOST_HUB.get())).displayItems((parameters, tabData) -> {
                tabData.accept(ModBlocks.OUTPOST_HUB.get().asItem());
            }).withTabsBefore(GUNS.getId()).build());
}