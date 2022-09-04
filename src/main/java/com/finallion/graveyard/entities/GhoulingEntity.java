package com.finallion.graveyard.entities;

import com.finallion.graveyard.entities.ai.goals.AttackWithOwnerGoal;
import com.finallion.graveyard.entities.ai.goals.FollowOwnerGoal;
import com.finallion.graveyard.entities.ai.goals.GhoulingMeleeAttackGoal;
import com.finallion.graveyard.entities.ai.goals.TrackOwnerAttackerGoal;
import com.finallion.graveyard.init.TGBlocks;
import com.finallion.graveyard.init.TGItems;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.*;


public class GhoulingEntity extends GraveyardMinionEntity implements IAnimatable, NamedScreenHandlerFactory {
    private AnimationFactory factory = new AnimationFactory(this);

    private static final TrackedData<Integer> ATTACK_ANIM_TIMER;
    private static final TrackedData<Integer> ANIMATION;
    private static final TrackedData<Integer> SPAWN_TIMER;
    private static final TrackedData<Boolean> COFFIN;
    private static final TrackedData<ItemStack> CARRIED_ITEM;
    //private static final TrackedData<Boolean> CAN_COLLECT;

    private final AnimationBuilder SPAWN_ANIMATION = new AnimationBuilder().addAnimation("spawn", false);
    private final AnimationBuilder IDLE_ANIMATION = new AnimationBuilder().addAnimation("idle", true);
    private final AnimationBuilder ATTACK_ANIMATION = new AnimationBuilder().addAnimation("attack", true);
    private final AnimationBuilder WALK_ANIMATION = new AnimationBuilder().addAnimation("walk", true);
    private final AnimationBuilder DEATH_ANIMATION = new AnimationBuilder().addAnimation("death", false);
    protected static final int ANIMATION_SPAWN = 0;
    protected static final int ANIMATION_IDLE = 1;
    protected static final int ANIMATION_MELEE = 2;
    protected static final int ANIMATION_WALK = 3;
    protected static final int ANIMATION_DEATH = 4;

    public final int ATTACK_ANIMATION_DURATION = 14;
    protected Inventory inventory;
    private ItemStack staff;
    //private Goal collectGoal;

    private static final List<Item> GHOULING_HOLDABLE = new ArrayList<>();

    public GhoulingEntity(EntityType<? extends GhoulingEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new GhoulingEntity.GhoulingEscapeDangerGoal(1.5D));
        this.goalSelector.add(5, new GhoulingMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, (new RevengeGoal(this, new Class[0])).setGroupRevenge(new Class[0]));
    }

    public static DefaultAttributeContainer.Builder createGhoulingAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.5D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.245D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0D);
    }


    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ANIMATION, ANIMATION_IDLE);
        this.dataTracker.startTracking(ATTACK_ANIM_TIMER, 0);
        this.dataTracker.startTracking(COFFIN, false);
        this.dataTracker.startTracking(SPAWN_TIMER, 0);
        this.dataTracker.startTracking(CARRIED_ITEM, ItemStack.EMPTY);
        //this.dataTracker.startTracking(CAN_COLLECT, false);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (getAnimationState() == ANIMATION_SPAWN && getSpawnTimer() >= 0) {
            event.getController().setAnimation(SPAWN_ANIMATION);
            return PlayState.CONTINUE;
        }

        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState predicate2(AnimationEvent<E> event) {
        /* DEATH */
        if (this.isDead() || this.getHealth() < 0.01) {
            event.getController().setAnimation(DEATH_ANIMATION);
            return PlayState.CONTINUE;
        }

        /* ATTACK */
        // takes one tick to get to this method (from mobtick)
        if (getAnimationState() == ANIMATION_MELEE && getAttackAnimTimer() == (ATTACK_ANIMATION_DURATION - 1) && isAttacking() && !(this.isDead() || this.getHealth() < 0.01)) {
            setAttackAnimTimer(ATTACK_ANIMATION_DURATION - 2);
            event.getController().setAnimation(ATTACK_ANIMATION);
            return PlayState.CONTINUE;
        }

        /* WALK */
        if ((getAnimationState() == ANIMATION_WALK || event.isMoving()) && getAttackAnimTimer() <= 0) {
            event.getController().setAnimation(WALK_ANIMATION);
            return PlayState.CONTINUE;
        }

        /* IDLE */
        if (getAnimationState() == ANIMATION_IDLE && getAttackAnimTimer() <= 0) {
            event.getController().setAnimation(IDLE_ANIMATION);
            return PlayState.CONTINUE;
        }

        /* STOPPERS */
        // stops idle animation from looping
        if (getAnimationState() == ANIMATION_IDLE && getAttackAnimTimer() > 0) {
            setAnimationState(ANIMATION_MELEE);
            return PlayState.STOP;
        }

        // stops attack animation from looping
        if (getAttackAnimTimer() <= 0 && !(this.isDead() || this.getHealth() < 0.01)) {
            setAnimationState(ANIMATION_IDLE);
            return PlayState.STOP;
        }


        return PlayState.CONTINUE;
    }

    protected void mobTick() {
        // ATTACK TIMER
        if (this.getAttackAnimTimer() == ATTACK_ANIMATION_DURATION) {
            setAnimationState(ANIMATION_MELEE);
        }

        if (this.getAttackAnimTimer() > 0) {
            int animTimer = this.getAttackAnimTimer() - 1;
            this.setAttackAnimTimer(animTimer);
        }

        if (this.getSpawnTimer() > 0) {
            this.setSpawnTimer(getSpawnTimer() - 1);
        }

        super.mobTick();
    }

    @Override
    public void tickMovement() {
        if (getSpawnTimer() > 0) {
            MinecraftClient.getInstance().particleManager.addBlockBreakParticles(this.getBlockPos().down(), world.getBlockState(this.getBlockPos().down()));
        }
        super.tickMovement();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.staff != null) {
            if (this.staff.getNbt() != null) {
                this.staff.getNbt().putBoolean("isAlive", false);
            }
        }
        super.onDeath(damageSource);
    }

    public int getAnimationState() {
        return this.dataTracker.get(ANIMATION);
    }

    public void setAnimationState(int state) {
        this.dataTracker.set(ANIMATION, state);
    }

    public int getAttackAnimTimer() {
        return (Integer) this.dataTracker.get(ATTACK_ANIM_TIMER);
    }

    public void setAttackAnimTimer(int time) {
        this.dataTracker.set(ATTACK_ANIM_TIMER, time);
    }

    public void onSummoned(ItemStack item) {
        this.setAnimationState(ANIMATION_SPAWN);
        setSpawnTimer(55);
        this.staff = item;
        if (this.staff.getNbt() != null && this.staff.getNbt().contains("OwnerUUID")) {
            setOwnerUuid(this.staff.getNbt().getUuid("OwnerUUID"));
        }
        //playSpawnSound();
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
        animationData.addAnimationController(new AnimationController(this, "controller2", 0, this::predicate2));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (!this.world.isClient()) {
            if (player.isSneaking() && this.hasCoffin()) {
                player.openHandledScreen(this);
                return ActionResult.SUCCESS;
            }

            if (!itemStack.isEmpty()) {
                if (!this.hasCoffin() && GHOULING_HOLDABLE.contains(itemStack.getItem())) {
                    this.equipStack(EquipmentSlot.OFFHAND, itemStack);
                    setCarriedItem(itemStack);
                    if (inventory == null) {
                        inventory = new SimpleInventory(54);
                        this.setHasCoffin(true);
                    }
                    if (!player.getAbilities().creativeMode) {
                        itemStack.decrement(1);
                    }
                    return ActionResult.SUCCESS;
                }
            }

            // bone staff stuff
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected void updatePostDeath() {
        ++this.deathTime;

        if (this.deathTime == 5) {
            //playDeathSound();
        }

        if (this.deathTime == 44 && !this.world.isClient()) {
            this.world.sendEntityStatus(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    public int getSpawnTimer() {
        return (Integer) this.dataTracker.get(SPAWN_TIMER);
    }

    public void setSpawnTimer(int ticks) {
        this.dataTracker.set(SPAWN_TIMER, ticks);
    }

    public void setCarriedItem(ItemStack stack) {
        this.dataTracker.set(CARRIED_ITEM, stack);
    }

    public ItemStack getCarriedItem() {
        return this.dataTracker.get(CARRIED_ITEM);
    }

/*
    public boolean canCollect() {
        return (Boolean) this.dataTracker.get(CAN_COLLECT);
    }

    public void setCanCollect(boolean canCollect) {
        this.dataTracker.set(CAN_COLLECT, canCollect);
    }

    public void setCollectGoal(Goal goal) {
        collectGoal = goal;
        this.goalSelector.add(1, collectGoal);
    }

    public void removeCollectGoal() {
        this.goalSelector.remove(collectGoal);
    }

 */

    /* INVENTORY */
    public boolean hasCoffin() {
        return (Boolean) this.dataTracker.get(COFFIN);
    }

    public void setHasCoffin(boolean hasCoffin) {
        this.dataTracker.set(COFFIN, hasCoffin);
    }

    protected void dropInventory() {
        super.dropInventory();
        if (this.hasCoffin()) {
            if (!this.world.isClient) {
                ItemStack itemStack = this.getCarriedItem();
                if (itemStack != null) {
                    this.dropItem(itemStack.getItem());
                }

                if (this.inventory != null) {
                    for (int i = 0; i < this.inventory.size(); i++) {
                        ItemStack stack = this.inventory.getStack(i);
                        if (!stack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(stack)) {
                            this.dropStack(stack);
                        }
                    }
                }
            }
            this.setHasCoffin(false);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("CoffinGhouling", this.hasCoffin());
        //nbt.put("Ghouling", this.writeNbt(new NbtCompound()));
        if (staff != null) {
            nbt.put("Staff", staff.writeNbt(new NbtCompound()));
        }
        if (inventory != null) {
            final NbtList inv = new NbtList();
            for (int i = 0; i < this.inventory.size(); i++) {
                inv.add(inventory.getStack(i).writeNbt(new NbtCompound()));
            }
            nbt.put("Inventory", inv);
        }
        if (!this.getCarriedItem().isEmpty()) {
            nbt.put("carriedItem", this.getCarriedItem().writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setHasCoffin(nbt.getBoolean("CoffinGhouling"));
        if (nbt.contains("Staff")) {
            staff = ItemStack.fromNbt(nbt.getCompound("Staff"));
        }
        if (nbt.contains("Inventory")) {
            final NbtList inv = nbt.getList("Inventory", 10);
            inventory = new SimpleInventory(inv.size());
            for (int i = 0; i < inv.size(); i++) {
                inventory.setStack(i, ItemStack.fromNbt(inv.getCompound(i)));
            }
        }
        if (nbt.contains("carriedItem", 10)) {
            ItemStack itemStack = ItemStack.fromNbt(nbt.getCompound("carriedItem"));
            this.setCarriedItem(itemStack);
        }

    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if (inventory == null) {
            return null;
        }
        return GenericContainerScreenHandler.createGeneric9x6(syncId, inv, inventory);
    }


    static {
        GHOULING_HOLDABLE.add(TGItems.SARCOPHAGUS);
        GHOULING_HOLDABLE.add(TGItems.OAK_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.DARK_OAK_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.SPRUCE_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.BIRCH_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.JUNGLE_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.ACACIA_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.MANGROVE_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.WARPED_COFFIN);
        GHOULING_HOLDABLE.add(TGItems.CRIMSON_COFFIN);

        ATTACK_ANIM_TIMER = DataTracker.registerData(GhoulingEntity.class, TrackedDataHandlerRegistry.INTEGER);
        ANIMATION = DataTracker.registerData(GhoulingEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SPAWN_TIMER = DataTracker.registerData(GhoulingEntity.class, TrackedDataHandlerRegistry.INTEGER);
        COFFIN = DataTracker.registerData(GhoulingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CARRIED_ITEM = DataTracker.registerData(GhoulingEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
        //CAN_COLLECT = DataTracker.registerData(GhoulingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }


    class GhoulingEscapeDangerGoal extends EscapeDangerGoal {
        public GhoulingEscapeDangerGoal(double speed) {
            super(GhoulingEntity.this, speed);
        }

        protected boolean isInDanger() {
            return this.mob.shouldEscapePowderSnow() || this.mob.isOnFire();
        }
    }
}