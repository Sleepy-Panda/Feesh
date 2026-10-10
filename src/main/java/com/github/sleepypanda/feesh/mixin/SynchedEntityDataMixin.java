package com.github.sleepypanda.feesh.mixin;

import com.github.sleepypanda.feesh.events.publishers.ArmorStandPublisher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private Component feesh$previousCustomName;

    @Inject(method = "assignValues", at = @At("HEAD"))
    private void feesh$beforeAssignValues(List<SynchedEntityData.DataValue<?>> packet, CallbackInfo ci) {
        this.feesh$previousCustomName = null;
        if (!(this.entity instanceof ArmorStand armorStand)) return;
        if (!feesh$isPacketUpdatingCustomName(packet)) return;

        this.feesh$previousCustomName = armorStand.getCustomName();
    }

    @Inject(method = "assignValues", at = @At("TAIL"))
    private void feesh$onAssignValues(List<SynchedEntityData.DataValue<?>> packet, CallbackInfo ci) {
        if (!(this.entity instanceof ArmorStand armorStand)) return;
        if (!feesh$isPacketUpdatingCustomName(packet)) return;

        ArmorStandPublisher.onArmorStandCustomNameChanged(armorStand, this.feesh$previousCustomName);
        this.feesh$previousCustomName = null;
    }

    @Unique
    private boolean feesh$isPacketUpdatingCustomName(List<SynchedEntityData.DataValue<?>> packet) {
        for (SynchedEntityData.DataValue<?> value : packet) {
            SynchedEntityData.DataItem<?> item = this.itemsById[value.id()];
            if (item != null && item.getAccessor() == EntityAccessor.feesh$getDataCustomName()) {
                return true;
            }
        }
        return false;
    }
}
