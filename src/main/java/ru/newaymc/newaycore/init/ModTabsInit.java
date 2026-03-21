package ru.newaymc.newaycore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import ru.newaymc.newaycore.NewaycoreMod;

public class ModTabsInit {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewaycoreMod.MODID);
    public static final RegistryObject<CreativeModeTab> GUNS = REGISTRY.register("guns",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.newaycore.guns")).icon(() -> new ItemStack(ModItemsInit.AKM.get())).displayItems((parameters, tabData) -> {
                tabData.accept(ModItemsInit.AKM.get());
                tabData.accept(ModItemsInit.M_4_A_1.get());
                tabData.accept(ModItemsInit.MP_5.get());
                tabData.accept(ModItemsInit.AR_15_SNIPER.get());
                tabData.accept(ModBlocksInit.COVER_MARKER_AI.get().asItem());
                tabData.accept(ModItemsInit.ELITE_SHOOTER_ENTITY_SPAWN_EGG.get());
                tabData.accept(ModItemsInit.STANDART_SHOOTER_ENTITY_SPAWN_EGG.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> ALS_TAB = REGISTRY.register("als_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.newaycore.als_tab")).icon(() -> new ItemStack(ModBlocksInit.OUTPOST_HUB.get())).displayItems((parameters, tabData) -> {
                tabData.accept(ModBlocksInit.OUTPOST_HUB.get().asItem());
            }).withTabsBefore(GUNS.getId()).build());

}