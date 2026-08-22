package com.seamoth.entity;

import com.mojang.math.Axis;
import com.seamoth.menu.SeamothMenu;
import com.seamoth.network.SeamothSyncPacket;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

public class SeamothEntity extends LivingEntity implements HasCustomInventoryScreen, ExtendedMenuProvider<Integer> {

    private static final float MIN_PITCH = -60.0F;
    private static final float MAX_PITCH = 60.0F;

    public boolean pressingUp = false;
    public boolean pressingDown = false;
    public boolean pressingForward = false;
    public boolean pressingBackward = false;
    public boolean pressingLeft = false;
    public boolean pressingRight = false;

    public void setInputs(boolean up, boolean down, boolean forward, boolean backward, boolean left, boolean right) {
        this.pressingDown = down;
        this.pressingUp = up;
        this.pressingForward = forward;
        this.pressingBackward = backward;
        this.pressingLeft = left;
        this.pressingRight = right;
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
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
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

    @Override
    public boolean canBeSeenAsEnemy() {
        return !this.getPassengers().isEmpty();
    }

    @Override
    public void handleEntityEvent(byte id) {
        return;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        this.dropContainerContents(level, this.specialInventory);
    }

    private void dropContainerContents(ServerLevel serverLevel, Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (!itemStack.isEmpty()) {
                this.spawnAtLocation(serverLevel, itemStack);
            }
        }

        container.clearContent();
    }

    @Override
    public boolean canBeAffected(MobEffectInstance newEffect) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /**
     * Обычный isInWater() у ванильной сущности становится true, стоит воде
     * коснуться хотя бы нижней кромки хитбокса. У Мотылька хитбокс высокий,
     * поэтому при всплытии он ещё долго "касается" воды снизу, хотя корпус уже
     * торчит наружу — из-за этого управление (в т.ч. подъём по пробелу)
     * продолжало работать над водой и позволяло случайно улететь.
     * <p>
     * Тут мы требуем, чтобы вода доставала примерно до уровня глаз пилота —
     * то есть корпус должен быть реально погружён, а не просто задевать
     * поверхность снизу.
     */
    private boolean isSeamothSubmerged() {
        return this.isEyeInFluid(FluidTags.WATER);
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
    private Vec3 rotateOffsetLikeModel(float yRot, float xRot, double offsetX, double offsetY, double offsetZ) {
        Quaternionf rotation = Axis.YP.rotationDegrees(-yRot)
                .mul(Axis.XP.rotationDegrees(xRot));

        Vector3f local = new Vector3f((float) offsetX, (float) offsetY, (float) offsetZ);
        rotation.transform(local);

        return new Vec3(local.x, local.y, local.z);
    }

    private Vec3 rotateOffsetLikeModel(double offsetX, double offsetY, double offsetZ) {
        return this.rotateOffsetLikeModel(this.getYRot(), this.getXRot(), offsetX, offsetY, offsetZ);
    }


    public Vec3 getInterpolatedPilotEyePosition(float partialTick) {
        float interpYRot = this.getViewYRot(partialTick);
        float interpXRot = Mth.lerp(partialTick, this.xRotO, this.getXRot());

        Vec3 rotatedEyeOffset = this.rotateOffsetLikeModel(
                interpYRot, interpXRot,
                PILOT_EYE_OFFSET_X, PILOT_EYE_OFFSET_Y, PILOT_EYE_OFFSET_Z
        );

        double bodyX = Mth.lerp(partialTick, this.xo, this.getX());
        double bodyY = Mth.lerp(partialTick, this.yo, this.getY());
        double bodyZ = Mth.lerp(partialTick, this.zo, this.getZ());

        return new Vec3(bodyX + rotatedEyeOffset.x, bodyY + rotatedEyeOffset.y, bodyZ + rotatedEyeOffset.z);
    }


    @Override
    public float maxUpStep() {
        return 0.0f;
    }

    @Override
    public float getViewYRot(float partialTick) {
        return Mth.lerp(partialTick, this.yRotO, this.getYRot());
    }

    @Override
    protected boolean isLocalClientAuthoritative() {
        return false;
    }

    @Override
    public boolean isColliding(BlockPos pos, BlockState state) {
        return this.isAlive();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return this.isAlive();
    }

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity pilot = this.getFirstPassenger() instanceof  LivingEntity ? (LivingEntity) this.getFirstPassenger() : null;
        boolean inWater = this.isSeamothSubmerged();

        if (this.isAlive()) {
            this.handleWaterEntryRotation(inWater, pilot);

            this.handlePhysicsAndMovement(inWater, pilot);

            this.spawnEngineBubbles(inWater, pilot);

            if (!this.level().isClientSide()) {
                this.hurtMarked = true;
            }

            return;
        }

        super.travel(travelVector);
    }

    private static final double ENGINE_OFFSET_X = 0.0D;
    private static final double ENGINE_OFFSET_Y = 0.15D;
    private static final double ENGINE_OFFSET_Z = -1.1D;

    private void spawnEngineBubbles(boolean inWater, LivingEntity pilot) {
        if (!(this.level() instanceof ServerLevel serverLevel) || !inWater || pilot == null) {
            return;
        }

        boolean thrusting = this.pressingForward || this.pressingBackward
                || this.pressingLeft || this.pressingRight
                || this.pressingUp || this.pressingDown;

        if (!thrusting) {
            return;
        }

        Vec3 enginePos = this.position().add(
                this.rotateOffsetLikeModel(ENGINE_OFFSET_X, ENGINE_OFFSET_Y, ENGINE_OFFSET_Z)
        );

        // "Назад" относительно корпуса — пузырьки летят в сторону, противоположную
        // носу, как будто их выталкивает струёй из сопла.
        Vec3 backwards = this.rotateOffsetLikeModel(0.0D, 0.0D, -1.0D).normalize();

        int count = 2 + this.random.nextInt(2);

        for (int i = 0; i < count; i++) {
            double spreadX = (this.random.nextDouble() - 0.5D) * 0.25D;
            double spreadY = (this.random.nextDouble() - 0.5D) * 0.15D;
            double spreadZ = (this.random.nextDouble() - 0.5D) * 0.25D;

            double velX = backwards.x * 0.05D + spreadX * 0.4D;
            double velY = 0.03D + Math.abs(spreadY) * 0.4D; // лёгкий подъём — пузырьки всплывают
            double velZ = backwards.z * 0.05D + spreadZ * 0.4D;

            serverLevel.sendParticles(
                    ParticleTypes.BUBBLE,
                    enginePos.x + spreadX, enginePos.y + spreadY, enginePos.z + spreadZ,
                    0,
                    velX, velY, velZ,
                    1.0D
            );
        }
    }


    private void breakFallingBlocks() {
        AABB checkBounds = this.getBoundingBox().expandTowards(0.0, 0.5, 0.0);

        List<FallingBlockEntity> fallingBlocks = this.level().getEntitiesOfClass(FallingBlockEntity.class, checkBounds, entity -> entity.isAlive() && entity.getBoundingBox().intersects(checkBounds));

        for (FallingBlockEntity fallingBlock : fallingBlocks) {
            if (fallingBlock.getDeltaMovement().y < 0) {
                BlockState state = fallingBlock.getBlockState();

                if (this.level() instanceof ServerLevel serverLevel) {
                    if (fallingBlock.dropItem && serverLevel.getGameRules().get(GameRules.BLOCK_DROPS)) {
                        ItemStack itemStack = new ItemStack(state.getBlock());
                        this.spawnAtLocation(serverLevel, itemStack);
                    }
                }

                this.level().levelEvent(2001, fallingBlock.blockPosition(), Block.getId(state));

                fallingBlock.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.breakFallingBlocks();

            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof LivingEntity livingPassenger) {
                    livingPassenger.setAirSupply(livingPassenger.getMaxAirSupply());
                }
            }

            this.travel(Vec3.ZERO);

            if (this.level() instanceof ServerLevel) {
                SeamothSyncPacket.broadcast(this);
            }
        } else if (this.getControllingPassenger() instanceof Player player && player.isLocalPlayer()) {
            this.travel(Vec3.ZERO);
        }

        boolean inWater = this.isSeamothSubmerged();

        this.wasInWater = inWater;
    }

    private void handleWaterEntryRotation(boolean inWater, LivingEntity pilot) {
        if (inWater && !this.wasInWater && pilot != null) {
            this.setYRot(pilot.getYRot());
            this.setXRot(Mth.clamp(pilot.getXRot(), MIN_PITCH, MAX_PITCH));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
            this.yBodyRot = this.getYRot();
            this.yBodyRotO = this.getYRot();
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

        if (!inWater && !this.onGround() && currentVelocity.y > 0) {
            // Скорость вверх, набранная ещё под водой (или на границе погружения),
            // не должна по инерции выносить корпус в воздух — как только мы
            // реально вышли из воды, вертикальный "разгон" сбрасываем, дальше
            // просто падаем под гравитацией.
            currentVelocity = new Vec3(currentVelocity.x, 0.0D, currentVelocity.z);
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

            Vec3 lookVec = pilot.getLookAngle();
            Vec3 inputVec = Vec3.ZERO;

            // Форвард/страйф теперь идут из собственного пакета ввода
            // (this.pressingForward и т.д.), а не с pilot.zza/xxa.
            if (this.pressingForward) inputVec = inputVec.add(lookVec);
            if (this.pressingBackward) inputVec = inputVec.subtract(lookVec);

            Vec3 sideVec = lookVec.cross(new Vec3(0, 1, 0)).normalize(); // указывает вправо от взгляда
            if (this.pressingRight) inputVec = inputVec.add(sideVec);
            if (this.pressingLeft) inputVec = inputVec.subtract(sideVec);

            // Этот метод вызывается только пока this.isSeamothSubmerged() == true,
            // так что отдельная проверка воды тут больше не нужна.
            double verticalMove = 0;
            if (this.pressingUp) verticalMove += 1.0D;
            if (this.pressingDown) verticalMove -= 1.0D;

            if (verticalMove != 0) {
                inputVec = inputVec.add(new Vec3(0, verticalMove, 0));
            }

            if (inputVec.lengthSqr() > 0.01D) {
                Vec3 desiredDir = inputVec.normalize();

                // Раскладываем текущую скорость на составляющую ВДОЛЬ желаемого
                // направления и "паразитную" составляющую поперёк него. Раньше
                // мы просто прибавляли ускорение к вектору скорости целиком —
                // из-за этого после манёвра по вертикали остаточная скорость ещё
                // долго "тянула" в старом направлении, а видимая скорость в новом
                // направлении казалась заниженной (упиралась в maxSpeed вместе
                // со старой составляющей). Теперь поперечная часть гасится
                // трением отдельно и заметно быстрее.
                double alongSpeed = currentVelocity.dot(desiredDir);
                Vec3 alongVelocity = desiredDir.scale(alongSpeed);
                Vec3 lateralVelocity = currentVelocity.subtract(alongVelocity).scale(friction);

                double newAlongSpeed = Math.min(alongSpeed + acc, maxSpeed);
                currentVelocity = desiredDir.scale(newAlongSpeed).add(lateralVelocity);

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

        if (this.isSeamothSubmerged()) {
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
            this.yBodyRot = this.getYRot();
            this.yBodyRotO = this.getYRot();

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

    private final SimpleContainer specialInventory = new SimpleContainer(SeamothMenu.SPECIAL_SLOT_COUNT);

    public Container getSpecialInventory() {
        return this.specialInventory;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new SeamothMenu(syncId, playerInventory, this);
    }

    @Override
    public Integer getScreenOpeningData(ServerPlayer player) {
        return this.getId();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.seamoth.seamoth");
    }

    public SeamothEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
    }

    // Пустой намеренно: ванильная LivingEntity.aiStep() гасит deltaMovement
    // (умножает на 0.98 и обнуляет составляющие меньше 0.003 каждый тик),
    // что убивало нашу собственную инерцию. Но именно aiStep() был тем местом,
    // откуда движок неявно вызывал travel() — теперь мы делаем это сами,
    // явно, из SeamothEntity.tick(), так что здесь ничего вызывать не нужно.
    @Override
    public void aiStep() {

    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isRemoved()) {
            return false;
        }

        Entity attacker = source.getEntity();
        Entity directAttacker = source.getDirectEntity();

        if ((attacker != null && this.hasPassenger(attacker)) || (directAttacker != null && this.hasPassenger(directAttacker))) {
            return false;
        }

        if (source.getEntity() instanceof Player player) {
            if (player.isCreative()) {
                this.dropContainerContents(level, this.specialInventory);
                this.discard();
                return true;
            }
        }

        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.loadContainer(input, "SpecialInventory", this.specialInventory);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.saveContainer(output, "SpecialInventory", this.specialInventory);
    }

    private void saveContainer(ValueOutput output, String key, Container container) {
        NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < container.getContainerSize(); i++) {
            items.set(i, container.getItem(i));
        }
        ContainerHelper.saveAllItems(output.child(key), items);
    }

    private void loadContainer(ValueInput input, String key, Container container) {
        input.child(key).ifPresent(child -> {
            NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(child, items);
            for (int i = 0; i < items.size(); i++) {
                container.setItem(i, items.get(i));
            }
        });

    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 lookAngle = this.getLookAngle();
        return new Vec3(this.getX() - lookAngle.x * 2.5, this.getY(), this.getZ() - lookAngle.z * 2.5);
//        return new Vec3(this.getX(), this.getBoundingBox().maxY + 0.1, this.getZ());
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
        return false;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
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
            if (this.getPassengers().isEmpty() && !player.isShiftKeyDown()) {
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

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack itemStack) {
        return;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    public static AttributeSupplier.Builder createSeamothAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }
}