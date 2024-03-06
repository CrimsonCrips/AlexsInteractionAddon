package com.crimsoncrips.alexsmobsinteraction.mixin;

import com.crimsoncrips.alexsmobsinteraction.AInteractionTagRegistry;
import com.crimsoncrips.alexsmobsinteraction.config.AInteractionConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityBananaSlug;
import com.github.alexthe666.alexsmobs.entity.EntityStraddler;
import com.github.alexthe666.alexsmobs.entity.EntityStradpole;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;


@Mixin(EntityStradpole.class)
public class AIStradpole extends Mob {

    int bobup = 0;

    protected AIStradpole(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void BlobFishGoals(CallbackInfo ci) {
        if (AInteractionConfig.preyfear)
            this.goalSelector.addGoal(3, new AvoidEntityGoal((EntityBananaSlug) (Object) this, LivingEntity.class, 2.0F, 1.2, 1.5, AMEntityRegistry.buildPredicateFromTag(AInteractionTagRegistry.SMALLINSECTFEAR)));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (AInteractionConfig.stradpolebobup) {
            bobup++;
            if (bobup >= 500 + random.nextInt(600) && this.isInLava()) this.setDeltaMovement(0, 0.1, 0);
            if (!this.isInLava()) bobup = 0;
        }
    }

    @Override
    @Nonnull
    protected InteractionResult mobInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
        EntityStradpole stradpole = (EntityStradpole) (Object) this;
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() == AMItemRegistry.MOSQUITO_LARVA.get()) {
            if (!player.isCreative()) {
                itemstack.shrink(1);
            }
            if (random.nextFloat() < 0.45F) {
                EntityStraddler straddler = AMEntityRegistry.STRADDLER.get().create(level());
                straddler.copyPosition(this);
                this.playSound(SoundEvents.BASALT_BREAK, 2F, 1F);
                if (!this.level().isClientSide && level().addFreshEntity(straddler)) {
                    this.remove(RemovalReason.DISCARDED);

                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (itemstack.getItem() == Items.LAVA_BUCKET && this.isAlive()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT);
            this.playSound(stradpole.getPickupSound(), 1.0F, 1.0F);
            ItemStack itemstack1 = stradpole.getBucketItemStack();
            stradpole.saveToBucketTag(itemstack1);
            ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, player, itemstack1, false);
            player.setItemInHand(hand, itemstack2);
            Level level = this.level();
            if (!level.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, itemstack1);
            }

            this.discard();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.mobInteract(player, hand);
    }

}
