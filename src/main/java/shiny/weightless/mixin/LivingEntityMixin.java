package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiny.weightless.ModConfig;
import shiny.weightless.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Unique private boolean wasSprinting = false;
    @Unique private boolean wasSprintFlying = false;
    @Unique private int startFlyingTicks = 0;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void weightless$sprintingCallback(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof PlayerEntity player && WeightlessComponent.has(player)) {
            boolean bl = (entity.isSprinting() || WeightlessComponent.autopilot(player)) && WeightlessComponent.canFly(player);
            if (bl && WeightlessComponent.flying(player) && !this.wasSprintFlying) {
                sendSoundPackets(player);
                this.wasSprintFlying = true;
            }
            else if (this.wasSprintFlying && (!bl || !WeightlessComponent.flying(player))) {
                this.wasSprintFlying = false;
            }

            if (bl && !this.wasSprinting) {
                this.calculateDimensions();
                this.wasSprinting = true;
            }
            else if (!bl && this.wasSprinting) {
                this.calculateDimensions();
                this.wasSprinting = false;
            }
        }
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/Vec3d;)V"))
    private void weightless$crosshairBasedMovement(LivingEntity entity, Vec3d movementInput, Operation<Void> original) {
        if (entity instanceof PlayerEntity player && WeightlessComponent.has(player)) {
            boolean bl = WeightlessComponent.autopilot(player);

            boolean bl2 = !WeightlessComponent.flying(player);
            bl2 &= (entity.getPitch() < -10 && movementInput.z > 0.0) || (entity.getPitch() > 10 && movementInput.z < 0.0) || bl;

            boolean bl3 = false;
            if (this.startFlyingTicks == 0) {
                if (bl2) {
                    this.startFlyingTicks++;
                }
                else {
                    bl3 = checkCollision(entity);
                }
            }
            WeightlessComponent.get(player).setFlying(!bl3);

            if (WeightlessComponent.flying(player)) {
                if (bl) movementInput = new Vec3d(0, 0, 1);

                float speed = getFlightSpeed(entity, entity.isSprinting() || bl);
                Vec3d movement = weightlessMovement(movementInput, speed, entity.getPitch(), entity.getYaw());
                Vec3d velocity = entity.getVelocity().add(movement).multiply(0.85, 0.85, 0.85);

                if (entity.isLogicalSideForUpdatingMovement()) {
                    double x = velocity.x;
                    double y = velocity.y;
                    double z = velocity.z;

                    if (entity.isSneaking() || entity.isUsingItem()) y *= 0.5;

                    if (Math.abs(x) < 0.003) {
                        x = 0.0;
                    }
                    if (Math.abs(y) < 0.003) {
                        y = 0.0;
                    }
                    if (Math.abs(z) < 0.003) {
                        z = 0.0;
                    }
                    velocity = new Vec3d(x, y, z);

                    entity.setVelocity(velocity);
                    entity.move(MovementType.SELF, entity.getVelocity());
                    entity.fallDistance = 0.1f;
                }
                if (entity.isSneaking()) entity.updateLimbs(false);
                else entity.limbAnimator.updateLimbs(0.0f, 0.1f);

                entity.setOnGround(bl3, velocity);
            }
            else {
                original.call(entity, movementInput);
            }

            if (this.startFlyingTicks < 5 && this.startFlyingTicks > 0) this.startFlyingTicks++;
            else this.startFlyingTicks = 0;
        }
        else {
            original.call(entity, movementInput);
        }
    }

    @WrapOperation(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;multiply(D)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d weightless$receiveIncreasedKnockback(Vec3d vector, double value, Operation<Vec3d> original) {
        if (ModConfig.increaseKnockback) {
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity instanceof PlayerEntity player && WeightlessComponent.has(player)) {
                value *= ModConfig.knockbackMultiplier;
            }
        }
        return original.call(vector, value);
    }

    @Inject(method = "damage", at = @At(value = "HEAD"))
    private void weightless$stunOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.shouldStun && amount >= ModConfig.damageRequirement) {
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity instanceof PlayerEntity player && player.isAlive() && WeightlessComponent.flying(player)
                    && (source.isIn(Weightless.CAN_STUN) || source.getAttacker() != null)) {
                WeightlessComponent.get(player).setStunned();
            }
        }
    }

    @Unique
    private static float getFlightSpeed(LivingEntity entity, boolean sprinting) {
        if (entity instanceof PlayerEntity player && WeightlessComponent.get(player).isStunned()) {
            return 0.0f;
        }

        float speed = ModConfig.movementSpeedAffectSpeed ? entity.getMovementSpeed() : 0.1f;
        speed *= sprinting ? 1.0f : 0.35f;
        speed *= ModConfig.speedMultiplier;

        if (ModConfig.armorAffectSpeed) {
            speed *= Math.max(0.1f, -0.025f * entity.getArmor() + 1);
        }

        if (ModConfig.increaseSpeedWhenHigh && entity.getPos().y >= ModConfig.altitude) {
            speed *= 1.0f + ModConfig.highSpeedMultiplier;
        }
        return speed;
    }

    @Unique
    private static Vec3d weightlessMovement(Vec3d movementInput, float speed, float pitch, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }
        else {
            Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
            float vertical = (float) Math.sqrt(1.0f - Math.abs(pitch) / 90.0f);
            float x = MathHelper.sin(yaw * (float) (Math.PI / 180.0)) * vertical;
            float y = -MathHelper.sin(pitch * (float) (Math.PI / 180.0));
            float z = MathHelper.cos(yaw * (float) (Math.PI / 180.0)) * vertical;

            return new Vec3d(
                    vec3d.x * z - vec3d.z * x,
                    (vec3d.z > 0 ? y : -y) * speed * (movementInput.z != 0 ? 1.1 : 0),
                    vec3d.z * z + vec3d.x * x
            );
        }
    }

    @Unique
    private static boolean checkCollision(Entity entity) {
        World world = entity.getWorld();
        Box box = entity.getBoundingBox().stretch(0, -0.01, 0);
        BlockCollisionSpliterator<VoxelShape> spliterator = new BlockCollisionSpliterator<>(world, entity, box, false, (mutable, voxelShape) -> voxelShape);

        while (spliterator.hasNext()) {
            VoxelShape shape = spliterator.next();
            if (!shape.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static void sendSoundPackets(PlayerEntity source) {
        if (!source.getWorld().isClient()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(source.getId());

            for (ServerPlayerEntity recipient : PlayerLookup.tracking(source)) {
                ServerPlayNetworking.send(recipient, Weightless.FLYING_SOUND_S2C_PACKET, buf);
            }
            ServerPlayNetworking.send((ServerPlayerEntity) source, Weightless.FLYING_SOUND_S2C_PACKET, buf);
        }
    }
}
