package ru.newaymc.newaycore.gun;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class GunSetup {

    // The gun utils/tools
    public static class GunUtils {
        // Numeric values representing gun properties
        public static final Value AMMO_NUMBER 				= new Value("gun_setup_01", Integer.class);
        public static final Value RECOVERY_TIME 			= new Value("gun_setup_02", Integer.class);
        public static final Value SHOOTED_ROUNDS 			= new Value("gun_setup_03", Integer.class);
        public static final Value ACCUMULATED_INACCURACY 	= new Value("gun_setup_04",  Double.class);

        // Boolean values representing gun states
        public static final Value IS_SHOOTING		 		= new Value("gun_setup_05", Boolean.class);
        public static final Value IS_RELOADING		 		= new Value("gun_setup_06", Boolean.class);
        public static final Value SHOULD_SHOOT 				= new Value("gun_setup_07", Boolean.class);
        public static final Value HAS_SHOOTED 				= new Value("gun_setup_08", Boolean.class);

        private static final TagKey<Item> GUNS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("neoforge", "guns"));

        public static boolean isGun(ItemStack stack) {
            return stack.is(GUNS);
        }

        public static void setValue(ItemStack stack, Value value, Object obj) {
            CompoundTag tag = getCompoundTag(stack);
            setValue(tag, value, obj);
            setCompoundTag(stack, tag);
        }

        public static Object getValue(ItemStack stack, Value value) {
            return getValue(getCompoundTag(stack), value);
        }

        private static CompoundTag getCompoundTag(ItemStack stack) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            return customData.copyTag();
        }

        private static void setCompoundTag(ItemStack stack, CompoundTag tag) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        public static void setValue(Entity entity, Value value, Object obj) {
            setValue(getCompoundTag(entity), value, obj);
        }

        public static Object getValue(Entity entity, Value value) {
            return getValue(getCompoundTag(entity), value);
        }

        private static CompoundTag getCompoundTag(Entity entity) {
            return entity.getPersistentData();
        }

        private static void setValue(CompoundTag tag, Value value, Object obj) {
            String name 	= value.getName();
            Class<?> type 	= value.getType();

            if (!name.isEmpty()) {
                if (type == Boolean.class  && obj instanceof Boolean data) tag.putBoolean(name, data.booleanValue());
                else if (type == Integer.class && obj instanceof Number index) 	tag.putInt(name, index.intValue());
                else if (type == Double.class && obj instanceof Number index) 	tag.putDouble(name, index.doubleValue());
                else if (type == Float.class && obj instanceof Number index) 	tag.putFloat(name, index.floatValue());
            }
        }

        private static Object getValue(CompoundTag tag, Value value) {
            String name 	= value.getName();
            Class<?> type 	= value.getType();

            if (!name.isEmpty()) {
                if (type == Boolean.class) return tag.getBoolean(name);
                else if (type == Integer.class) return tag.getInt(name);
                else if (type == Double.class) 	return tag.getDouble(name);
                else if (type == Float.class) 	return tag.getFloat(name);
            }
            return null;
        }
    }

    private static class Value {

        private final String VALUE_NAME;
        private final Class<?> VALUE_TYPE;

        public Value(String valueName, Class<?> valueType) {
            this.VALUE_NAME = valueName;
            this.VALUE_TYPE = valueType;
        }

        public String getName() {
            if (!VALUE_NAME.isEmpty()) return VALUE_NAME;

            return "";
        }

        public Class<?> getType() {
            if (VALUE_TYPE != null) return VALUE_TYPE;

            return null;
        }
    }
}
