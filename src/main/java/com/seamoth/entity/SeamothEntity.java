package com.seamoth.entity;

import com.mojang.math.Axis;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SeamothEntity extends Entity {

    private static final float MIN_PITCH = -60.0F;
    private static final float MAX_PITCH = 60.0F;

    public boolean pressingUp = false;
    public boolean pressingDown = false;

    public void setInputs(boolean up, boolean down) {
        this.pressingDown = down;
        this.pressingUp = up;
    }

    private boolean wasInWater;

    // Смещение точки ГЛАЗ игрока относительно "нуля" Мотылька (в локальных, ещё не
    // повёрнутых координатах корпуса). Двигайте эти три числа, чтобы подогнать
    // положение камеры внутри кабины — теперь это можно делать не боясь наклонов,
    // все три оси корректно поворачиваются вместе с моделью.
    private static final double PILOT_EYE_OFFSET_X = 0.0D;
    private static final double PILOT_EYE_OFFSET_Y = 1.22D; // высота глаз при ровном полёте
    private static final double PILOT_EYE_OFFSET_Z = 0.1D;  // насколько глаза смещены вперёд

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            Vec3 rotatedEyeOffset = this.rotateOffsetLikeModel(
                    PILOT_EYE_OFFSET_X, PILOT_EYE_OFFSET_Y, PILOT_EYE_OFFSET_Z
            );

            // Ваниль сама прибавит passenger.getEyeHeight() к Y позиции пассажира,
            // чтобы получить точку камеры (Entity/Camera всегда добавляют eyeHeight
            // строго вверх в мировых координатах, никак не учитывая наклон сущности).
            // Поэтому здесь мы заранее вычитаем eyeHeight из уже повёрнутого смещения:
            // когда ваниль прибавит его обратно (уже НЕ повёрнутым), итоговая точка
            // всё равно окажется ровно там, где нужно — как будто eyeHeight тоже
            // повернулся вместе с корпусом.
            double eyeHeight = passenger.getEyeHeight();

            moveFunction.accept(
                    passenger,
                    this.getX() + rotatedEyeOffset.x,
                    this.getY() + rotatedEyeOffset.y - eyeHeight,
                    this.getZ() + rotatedEyeOffset.z
            );
        }
    }

    /**
     * Поворачивает локальное смещение (относительно корпуса) ровно так же,
     * как SeamothEntityRenderer поворачивает саму 3D-модель:
     * сначала тангаж (наклон носа) вокруг локальной оси X, затем курс вокруг мировой оси Y.
     * <p>
     * Используя тот же порядок поворотов и те же углы (getXRot()/getYRot()), что и
     * рендерер, мы гарантируем, что заданная точка всегда остаётся там же, где визуально
     * находится кабина, при любом наклоне — то есть камера буквально "приклеена"
     * к корпусу Мотылька.
     */
    private Vec3 rotateOffsetLikeModel(double offsetX, double offsetY, double offsetZ) {
        Quaternionf rotation = Axis.YP.rotationDegrees(-this.getYRot())
                .mul(Axis.XP.rotationDegrees(this.getXRot()));

        Vector3f local = new Vector3f((float) offsetX, (float) offsetY, (float) offsetZ);
        rotation.transform(local);

        return new Vec3(local.x, local.y, local.z);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof LivingEntity livingPassenger) {
                    livingPassenger.setAirSupply(livingPassenger.getMaxAirSupply());
                }
            }
        }

        boolean inWater = this.isInWater();
        LivingEntity pilot = this.getFirstPassenger() instanceof  LivingEntity ? (LivingEntity) this.getFirstPassenger() : null;

        this.handleWaterEntryRotation(inWater, pilot);

        this.handlePhysicsAndMovement(inWater, pilot);

        this.wasInWater = inWater;
    }

    private void handleWaterEntryRotation(boolean inWater, LivingEntity pilot) {
        if (inWater && !this.wasInWater && pilot != null) {
            this.setYRot(pilot.getYRot());
            this.setXRot(Mth.clamp(pilot.getXRot(), MIN_PITCH, MAX_PITCH));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    private void handlePhysicsAndMovement(boolean inWater, LivingEntity pilot) {
        double ACCELERATION = 0.05D;
        double MAX_SPEED = 0.6D;
        double WATER_FRICTION = 0.92D;
        double AIR_FRICTION = 0.98D;
        double GRAVITY = 0.04D;
        Vec3 currentVelocity = this.getDeltaMovement();

        if (inWater) {
            currentVelocity = this.calculateWaterMovement(pilot, currentVelocity, ACCELERATION, MAX_SPEED, WATER_FRICTION);
        }
        else if (this.onGround()) {
            currentVelocity = this.calculateGroundMovement(currentVelocity);
        }
        else {
            currentVelocity = this.calculateAirMovement(currentVelocity, AIR_FRICTION, GRAVITY);
        }

        if (!inWater && !this.onGround()) {
            currentVelocity = new Vec3(currentVelocity.x, currentVelocity.y - GRAVITY, currentVelocity.z);
        }

        this.setDeltaMovement(currentVelocity);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private Vec3 calculateWaterMovement(LivingEntity pilot, Vec3 currentVelocity, double acc, double maxSpeed, double friction) {
        if (pilot != null) {
            this.setYRot(pilot.getYRot());
            this.setXRot(Mth.clamp(pilot.getXRot(), MIN_PITCH, MAX_PITCH));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();

            pilot.setYBodyRot(this.getYRot());
            pilot.setYHeadRot(this.getYRot());

            float forward = pilot.zza;
            float strafe = pilot.xxa;

            Vec3 lookVec = pilot.getLookAngle();
            Vec3 inputVec = Vec3.ZERO;

            if (forward != 0) inputVec = inputVec.add(lookVec.scale(forward));
            if (strafe != 0) {
                Vec3 sideVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
                inputVec = inputVec.add(sideVec.scale(-strafe));
            }

            double verticalMove = 0;
            if (this.pressingUp) verticalMove += 1.0D;
            if (this.pressingDown) verticalMove -= 1.0D;

            if (verticalMove != 0) {
                inputVec = inputVec.add(new Vec3(0, verticalMove, 0));
            }

            if (inputVec.lengthSqr() > 0.01D) {
                Vec3 accelVec = inputVec.normalize().scale(acc);

                if (currentVelocity.length() < 0.1D) {
                    currentVelocity = inputVec.normalize().scale(acc * 2.0D);
                } else {
                    currentVelocity = currentVelocity.add(accelVec);
                }

                if (currentVelocity.length() > maxSpeed) {
                    currentVelocity = currentVelocity.normalize().scale(maxSpeed);
                }
            } else {
                currentVelocity = currentVelocity.scale(friction);
            }
        } else {
            currentVelocity = currentVelocity.scale(friction);

            if (currentVelocity.lengthSqr() < 0.0001D) {
                currentVelocity = Vec3.ZERO;
            }
        }

        return currentVelocity;
    }

    private Vec3 calculateGroundMovement(Vec3 currentVelocity) {
        float blockFriction = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction();

        double newX = currentVelocity.x * blockFriction * 0.5D;
        double newZ = currentVelocity.z * blockFriction * 0.5D;

        if (Math.abs(newX) < 0.001D) newX = 0;
        if (Math.abs(newZ) < 0.001D) newZ = 0;

        return new Vec3(newX, currentVelocity.y, newZ);
    }

    private Vec3 calculateAirMovement(Vec3 currentVelocity, double friction, double gravity) {
        return new Vec3(
                currentVelocity.x * friction,
                currentVelocity.y - gravity,
                currentVelocity.z * friction
        );
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        if (!(passenger instanceof LivingEntity livingPassenger)) {
            return;
        }

        if (this.isInWater()) {
            // Entity.turn() уже применил движение мыши к livingPassenger.xRot/yRot.
            // Поскольку в конце прошлого вызова мы синхронизировали игрока с транспортом,
            // разница между текущим поворотом игрока и текущим поворотом транспорта
            // и есть "дельта" от движения мыши за этот кадр.
            float deltaYaw = Mth.wrapDegrees(livingPassenger.getYRot() - this.getYRot());
            float deltaPitch = livingPassenger.getXRot() - this.getXRot();

            // Этой дельтой поворачиваем сам транспорт.
            this.setYRot(this.getYRot() + deltaYaw);
            this.setXRot(Mth.clamp(this.getXRot() + deltaPitch, MIN_PITCH, MAX_PITCH));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();

            // Возвращаем игроку ровно поворот транспорта: камера и голова
            // остаются "смотрящими вперёд" и не крутятся отдельно от корпуса.
            livingPassenger.setYRot(this.getYRot());
            livingPassenger.setXRot(this.getXRot());
            livingPassenger.yRotO = this.getYRot();
            livingPassenger.xRotO = this.getXRot();
            livingPassenger.setYHeadRot(this.getYRot());
            livingPassenger.setYBodyRot(this.getYRot());
            livingPassenger.yHeadRotO = this.getYRot();
        } else {
            float vehicleRot = this.getYRot();
            float clampAngle = 100.0F;

            float wrappedDelta = Mth.wrapDegrees(livingPassenger.getYHeadRot() - vehicleRot);

            if (Math.abs(wrappedDelta) > clampAngle) {
                float sign = Math.signum(wrappedDelta);

                float lockedHeadRot = vehicleRot + (sign * clampAngle);

                livingPassenger.setYHeadRot(lockedHeadRot);
                livingPassenger.setYRot(lockedHeadRot);
                livingPassenger.yRotO = lockedHeadRot;
            }
        }
    }

    public SeamothEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {

    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isRemoved()) {
            return false;
        }

        if (source.getEntity() instanceof Player player) {
            this.discard();
            return true;
        }
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {

    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY + 0.1, this.getZ());
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, 0.15, 0.0);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return true;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (!this.level().isClientSide()) {
            if (this.getPassengers().isEmpty()) {
                if (this.isInWater()) {
                    player.setXRot(this.getXRot());
                    player.setYRot(this.getYRot());
                    player.setYBodyRot(this.getYRot());
                    player.setYHeadRot(this.getYRot());

                    player.yRotO = this.getYRot();
                    player.xRotO = this.getXRot();
                    player.yBodyRotO = this.getYRot();
                    player.yHeadRotO = this.getYRot();
                }

                player.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}