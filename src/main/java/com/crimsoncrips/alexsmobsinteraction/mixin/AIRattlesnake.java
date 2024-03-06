package com.crimsoncrips.alexsmobsinteraction.mixin;

import com.crimsoncrips.alexsmobsinteraction.AInteractionTagRegistry;
import com.crimsoncrips.alexsmobsinteraction.config.AInteractionConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityAnaconda;
import com.github.alexthe666.alexsmobs.entity.EntityRattlesnake;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;


@Mixin(EntityRattlesnake.class)
public class AIRattlesnake extends Mob {

    private int ate = 0;
    protected AIRattlesnake(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void RattlesnakeGoals(CallbackInfo ci){
        Predicate<LivingEntity> rattlesnakecannibalism = (livingEntity) -> {
            return (livingEntity.getHealth() <= 0.70F * livingEntity.getMaxHealth() && livingEntity.isBaby()) && ate >= 5000;
        };
        this.targetSelector.addGoal(5, new EntityAINearestTarget3D<>(this, LivingEntity.class, 200, true, true, AMEntityRegistry.buildPredicateFromTag(AInteractionTagRegistry.RATTLESNAKE_KILL)) {
            public void start(){
                super.start();
                ate = 0;
            }
        });
        if (AInteractionConfig.rattlesnakecannibalize) {
            this.targetSelector.addGoal(5, new EntityAINearestTarget3D<>(this, EntityRattlesnake.class, 200, true, true, rattlesnakecannibalism) {
                public void start() {
                    super.start();
                    ate = 200;
                }
            });
        }
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void AlexInteraction$tick(CallbackInfo ci) {
        if (AInteractionConfig.anacondacanibalize) ate++;
    }


}
