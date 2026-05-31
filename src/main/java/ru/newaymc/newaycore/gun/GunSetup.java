package ru.newaymc.newaycore.gun;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GunSetup {

    // The gun utils/tools
    public static class GunUtils {
        // Numeric values representing gun properties
        public static final Value LEVEL 					= new Value("gun_setup_00", Integer.class);
        public static final Value AMMO_NUMBER 				= new Value("gun_setup_01", Integer.class);
        public static final Value MAX_AMMO_NUMBER 			= new Value("gun_setup_02", Integer.class);
        public static final Value RECOVERY_TIME 			= new Value("gun_setup_03", Integer.class);
        public static final Value SHOOTED_ROUNDS 			= new Value("gun_setup_04", Integer.class);
        public static final Value ACCUMULATED_INACCURACY 	= new Value("gun_setup_05",  Double.class);

        // Boolean values representing gun states
        public static final Value IS_AIMING 				= new Value("gun_setup_06", Boolean.class);
        public static final Value IS_SHOOTING		 		= new Value("gun_setup_07", Boolean.class);
        public static final Value IS_RELOADING		 		= new Value("gun_setup_08", Boolean.class);
        public static final Value SHOULD_SHOOT 				= new Value("gun_setup_09", Boolean.class);

        public static final Value MOUSE_LEFT 				= new Value("gun_setup_10", Boolean.class);
        public static final Value MOUSE_RIGHT 				= new Value("gun_setup_11", Boolean.class);
        public static final Value HAS_SHOOTED 				= new Value("gun_setup_12", Boolean.class);

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

    @EventBusSubscriber(modid = "${modid}")
    public static class Events {

        // Tracks the last held item to detect when the player switches items
        private static ItemStack LAST_ITEM = ItemStack.EMPTY;

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {

            if (event.getEntity() == null) return;
            LivingEntity living = (LivingEntity) event.getEntity();

            if (living.level().isClientSide) return;
            ItemStack CURRENT_ITEM = living.getMainHandItem();

            if (CURRENT_ITEM != LAST_ITEM) {
                if (GunUtils.isGun(CURRENT_ITEM) && CURRENT_ITEM != ItemStack.EMPTY) {

                    living.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(0);
                    living.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(0);
                    living.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(1024D);

                } else if (!GunUtils.isGun(CURRENT_ITEM) || CURRENT_ITEM == ItemStack.EMPTY) {

                    living.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(4.5D);
                    living.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(3D);
                    living.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(4D);
                }
            }
            LAST_ITEM = living.getMainHandItem().copy();
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {

            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            for (Player player : level.players())
                if (GunUtils.isGun(player.getMainHandItem())) player.swinging = false;
        }

        @EventBusSubscriber(modid = "${modid}", value = Dist.CLIENT)
        public static class ClientRenderEvents {
            @SubscribeEvent
            public static void onEventTriggered(RenderPlayerEvent.Pre event) {

                Entity entity = event.getEntity();
                ItemStack stack = ItemStack.EMPTY;

                if (entity instanceof LivingEntity living) stack = living.getMainHandItem();
                if (GunUtils.isGun(stack) && (boolean) GunUtils.getValue(stack, GunUtils.IS_AIMING)) {

                    PlayerModel<?> model = (PlayerModel<?>) event.getRenderer().getModel();
                    model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
            }
        }

        private static boolean lastLeft  = false;
        private static boolean lastRight = false;

        @SubscribeEvent
        public static void onClientMouseTick(ClientTickEvent.Post event) {
            if (Minecraft.getInstance().player != null) {
                boolean left  = Minecraft.getInstance().mouseHandler.isLeftPressed();
                boolean right = Minecraft.getInstance().mouseHandler.isRightPressed();

                if (left != lastLeft || right != lastRight) {
                    PacketDistributor.sendToServer(new PacketHandler.MouseClickPacket(left, right));
                    lastLeft  = left;
                    lastRight = right;
                }
            }
        }
    }

    @EventBusSubscriber(modid = "${modid}", bus = EventBusSubscriber.Bus.MOD)
    public static class PacketHandler {

        public record MouseClickPacket(boolean leftClick, boolean rightClick) implements CustomPacketPayload {

            public static final Type<MouseClickPacket> TYPE =
                    new Type<>(ResourceLocation.fromNamespaceAndPath("${modid}", "main_channel"));

            public static final StreamCodec<RegistryFriendlyByteBuf, MouseClickPacket> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.BOOL, MouseClickPacket::leftClick,
                    ByteBufCodecs.BOOL, MouseClickPacket::rightClick,
                    MouseClickPacket::new
            );

            @Override
            public Type<? extends CustomPacketPayload> type() {
                return TYPE;
            }
        }

        public static void handleMouseClick(MouseClickPacket msg, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    GunUtils mg = null;
                    mg.setValue(player, mg.MOUSE_LEFT, msg.leftClick());
                    mg.setValue(player, mg.MOUSE_RIGHT, msg.rightClick());
                }
            });
        }

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToServer(
                    MouseClickPacket.TYPE,
                    MouseClickPacket.STREAM_CODEC,
                    PacketHandler::handleMouseClick
            );
        }
    }
}
