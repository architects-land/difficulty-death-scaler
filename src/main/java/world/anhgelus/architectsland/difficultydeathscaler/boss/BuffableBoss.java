package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.entity.mob.MobEntity;

public abstract class BuffableBoss<T extends MobEntity> {
    protected final T entity;

    public BuffableBoss(T entity) {
        this.entity = entity;
    }

    abstract void buff();
}
