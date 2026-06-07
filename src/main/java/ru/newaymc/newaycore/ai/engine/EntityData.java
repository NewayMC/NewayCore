package ru.newaymc.newaycore.ai.engine;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.newaymc.newaycore.ai.ShooterAiEntity;
/**
 * @see ru.newaymc.newaycore.ai.ShooterAiEntity
 */
public class EntityData {
    private static final Logger LOGGER = LogManager.getLogger(EntityData.class);

    public static String getStringData(Entity entity, EntityDataAccessor<String> dataAccessor) {
        return (entity instanceof ShooterAiEntity data ? data.getEntityData().get(dataAccessor) : "" );
    }

    public static void setStringData(String data, Entity entity, EntityDataAccessor<String> dataAccessor) {
        if (check(entity)) {
            entity.getEntityData().set(dataAccessor, data);
        }
    }

    public static Boolean getBooleanData(Entity entity, EntityDataAccessor<Boolean> dataAccessor) {
        return (entity instanceof ShooterAiEntity data && data.getEntityData().get(dataAccessor));
    }

    public static void setBooleanData(Boolean data, Entity entity, EntityDataAccessor<Boolean> dataAccessor) {
        if (check(entity)) {
            entity.getEntityData().set(dataAccessor, data);
        }
    }

    private static Boolean check(Entity entity) {
        if (entity instanceof ShooterAiEntity) {
            return true;
        } else {
            LOGGER.error("Error with entity type");
            return false;
        }
    }
}
