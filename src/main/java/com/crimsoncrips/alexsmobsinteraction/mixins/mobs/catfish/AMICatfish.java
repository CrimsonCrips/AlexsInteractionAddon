package com.crimsoncrips.alexsmobsinteraction.mixins.mobs.catfish;

import com.crimsoncrips.alexsmobsinteraction.AlexsMobsInteraction;
import com.crimsoncrips.alexsmobsinteraction.server.goal.AMITargetFood;
import com.github.alexthe666.alexsmobs.entity.EntityCatfish;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(EntityCatfish.class)
public abstract class AMICatfish extends WaterAnimal {


    @Shadow protected abstract void registerGoals();

    @Shadow public abstract int getCatfishSize();

    @Shadow public SimpleContainer catfishInventory;

    protected AMICatfish(EntityType<? extends WaterAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void alexsmobsinteraction$registerGoals(CallbackInfo ci) {
        EntityCatfish catfish = (EntityCatfish)(Object)this;
        if (AlexsMobsInteraction.COMMON_CONFIG.CATFISH_CANNIBALIZE_ENABLED.get()) {
            this.goalSelector.addGoal(3, new AMITargetFood(catfish));
        }
    }

    @WrapWithCondition(method = "registerGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V",ordinal = 2))
    private boolean alexsmobsinteraction$targetFood(GoalSelector instance, int pPriority, Goal pGoal) {
        return !AlexsMobsInteraction.COMMON_CONFIG.CATFISH_CANNIBALIZE_ENABLED.get();
    }

    @Inject(method = "hurt", at = @At(value = "HEAD"))
    private void alexsmobsinteraction$hurt(DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1 + this.getCatfishSize()))) {
            if (entity != this && this.getRandom().nextDouble() < 1) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 0));
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void alexsmobsinteraction$tick(CallbackInfo ci) {
        for (int i = 0; i < catfishInventory.getContainerSize(); i++) {
            if (catfishInventory.getItem(i).is(Items.LAVA_BUCKET)){
                System.out.println("HOT");
            }
        }
    }

}
