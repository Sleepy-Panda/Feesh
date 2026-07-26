package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.publishers.ArmorStandPublisher;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin {
    @Shadow
    @Final
    private SyncedDataHolder entity;

    @Shadow
    @Final
    private SynchedEntityData.DataItem<?>[] itemsById;

    @Inject(method = "assignValues", at = @At("TAIL"))
    private void feesh$onAssignValues(List<SynchedEntityData.DataValue<?>> packet, CallbackInfo ci) {
        if (!(this.entity instanceof ArmorStand armorStand)) return;

        for (SynchedEntityData.DataValue<?> value : packet) {
            SynchedEntityData.DataItem<?> item = this.itemsById[value.id()];
            if (item != null && item.getAccessor() == EntityAccessor.feesh$getDataCustomName()) {
                ArmorStandPublisher.onArmorStandCustomNameChanged(armorStand);
                return;
            }
        }
    }
}
