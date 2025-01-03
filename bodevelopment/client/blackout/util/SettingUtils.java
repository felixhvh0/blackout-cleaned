package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.enums.RotationType;
import bodevelopment.client.blackout.enums.SwingState;
import bodevelopment.client.blackout.enums.SwingType;
import bodevelopment.client.blackout.interfaces.functional.DoublePredicate;
import bodevelopment.client.blackout.interfaces.mixin.IEndCrystalEntity;
import bodevelopment.client.blackout.module.modules.client.settings.ExtrapolationSettings;
import bodevelopment.client.blackout.module.modules.client.settings.FacingSettings;
import bodevelopment.client.blackout.module.modules.client.settings.RangeSettings;
import bodevelopment.client.blackout.module.modules.client.settings.RaytraceSettings;
import bodevelopment.client.blackout.module.modules.client.settings.RotationSettings;
import bodevelopment.client.blackout.module.modules.client.settings.ServerSettings;
import bodevelopment.client.blackout.module.modules.client.settings.SwingSettings;
import bodevelopment.client.blackout.randomstuff.PlaceData;
import bodevelopment.client.blackout.randomstuff.Rotation;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SettingUtils {
   private static FacingSettings facing;
   private static RangeSettings range;
   private static RaytraceSettings raytrace;
   private static RotationSettings rotation;
   private static ServerSettings server;
   private static SwingSettings swing;
   private static ExtrapolationSettings extrapolation;

   public static void init() {
      facing = FacingSettings.getInstance();
      range = RangeSettings.getInstance();
      raytrace = RaytraceSettings.getInstance();
      rotation = RotationSettings.getInstance();
      server = ServerSettings.getInstance();
      swing = SwingSettings.getInstance();
      extrapolation = ExtrapolationSettings.getInstance();
   }

   public static double maxInteractRange() {
      return Math.max(getInteractRange(), getInteractWallsRange());
   }

   public static double maxPlaceRange() {
      return Math.max(getPlaceRange(), getPlaceWallsRange());
   }

   public static double maxMineRange() {
      return Math.max(getMineRange(), getMineWallsRange());
   }

   public static double getInteractRange() {
      return (Double)range.interactRange.get();
   }

   public static double getInteractWallsRange() {
      return (Double)range.interactRangeWalls.get();
   }

   public static double interactRangeTo(BlockPos pos) {
      return range.interactRangeTo(pos, (Vec3d)null);
   }

   public static boolean inInteractRange(BlockPos pos) {
      return range.inInteractRange(pos, (Vec3d)null);
   }

   public static boolean inInteractRange(BlockPos pos, Vec3d from) {
      return range.inInteractRange(pos, from);
   }

   public static boolean inInteractRangeNoTrace(BlockPos pos) {
      return range.inInteractRangeNoTrace(pos, (Vec3d)null);
   }

   public static boolean inInteractRangeNoTrace(BlockPos pos, Vec3d from) {
      return range.inInteractRangeNoTrace(pos, from);
   }

   public static double getPlaceRange() {
      return (Double)range.placeRange.get();
   }

   public static double getPlaceWallsRange() {
      return (Double)range.placeRangeWalls.get();
   }

   public static double placeRangeTo(BlockPos pos) {
      return range.placeRangeTo(pos, (Vec3d)null);
   }

   public static boolean inPlaceRange(BlockPos pos) {
      return range.inPlaceRange(pos, (Vec3d)null);
   }

   public static boolean inPlaceRange(BlockPos pos, Vec3d from) {
      return range.inPlaceRange(pos, from);
   }

   public static boolean inPlaceRangeNoTrace(BlockPos pos) {
      return range.inPlaceRangeNoTrace(pos, (Vec3d)null);
   }

   public static boolean inPlaceRangeNoTrace(BlockPos pos, Vec3d from) {
      return range.inPlaceRangeNoTrace(pos, from);
   }

   public static double getAttackRange() {
      return (Double)range.attackRange.get();
   }

   public static double getAttackWallsRange() {
      return (Double)range.attackRangeWalls.get();
   }

   public static double attackRangeTo(Box bb, Vec3d feet) {
      return range.innerAttackRangeTo(bb, feet, false);
   }

   public static double wallAttackRangeTo(Box bb, Vec3d feet) {
      return range.innerAttackRangeTo(bb, feet, true);
   }

   public static boolean inAttackRange(Box bb) {
      return range.inAttackRange(bb, (Vec3d)null);
   }

   public static boolean inAttackRange(Box bb, Vec3d from) {
      return range.inAttackRange(bb, from);
   }

   public static boolean inAttackRangeNoTrace(Box bb) {
      return range.inAttackRangeNoTrace(bb, (Vec3d)null);
   }

   public static boolean inAttackRangeNoTrace(Box bb, Vec3d from) {
      return range.inAttackRangeNoTrace(bb, from);
   }

   public static double getMineRange() {
      return (Double)range.mineRange.get();
   }

   public static double getMineWallsRange() {
      return (Double)range.mineRangeWalls.get();
   }

   public static double mineRangeTo(BlockPos pos) {
      return range.miningRangeTo(pos, (Vec3d)null);
   }

   public static boolean inMineRange(BlockPos pos) {
      return range.inMineRange(pos);
   }

   public static boolean inMineRangeNoTrace(BlockPos pos) {
      return range.inMineRangeNoTrace(pos);
   }

   public static boolean startMineRot() {
      return rotation.startMineRot();
   }

   public static boolean endMineRot() {
      return rotation.endMineRot();
   }

   public static boolean shouldVanillaRotate() {
      return (Boolean)rotation.vanillaRotation.get();
   }

   public static boolean shouldRotate(RotationType type) {
      return rotation.shouldRotate(type);
   }

   public static boolean blockRotationCheck(BlockPos pos, Direction dir, float yaw, float pitch, RotationType type) {
      return rotation.blockRotationCheck(pos, dir, yaw, pitch, type);
   }

   public static boolean attackRotationCheck(Box box, float yaw, float pitch) {
      return rotation.attackRotationCheck(box, yaw, pitch);
   }

   public static double yawStep(RotationType type) {
      return rotation.yawStep(type);
   }

   public static double pitchStep(RotationType type) {
      return rotation.pitchStep(type);
   }

   public static Rotation getRotation(BlockPos pos, Direction dir, Vec3d vec, RotationType type) {
      return rotation.getRotation(pos, dir, vec, type);
   }

   public static Vec3d getRotationVec(BlockPos pos, Direction dir, Vec3d vec, RotationType type) {
      return rotation.getRotationVec(pos, dir, vec, type);
   }

   public static Rotation getRotation(Vec3d vec) {
      return rotation.getRotation(vec);
   }

   public static Rotation getAttackRotation(Box box, Vec3d vec) {
      return rotation.getAttackRotation(box, vec);
   }

   public static double returnSpeed() {
      return (Double)rotation.returnSpeed.get();
   }

   public static Rotation applyStep(Rotation rot, RotationType type, boolean rotated) {
      return rotation.applyStep(rot, type, rotated);
   }

   public static boolean attackLimit() {
      return (Boolean)rotation.attackLimit.get();
   }

   public static double attackSpeed() {
      return (Double)rotation.attackMaxSpeed.get();
   }

   public static int attackTicks() {
      return (Integer)rotation.attackTicks.get();
   }

   public static boolean rotationIgnoreEnabled() {
      return (Double)rotation.noOwnTime.get() > 0.0D || (Double)rotation.noOtherTime.get() > 0.0D;
   }

   public static boolean shouldIgnoreRotations(EndCrystalEntity entity) {
      IEndCrystalEntity iEntity = (IEndCrystalEntity)entity;
      long since = System.currentTimeMillis() - iEntity.blackout_Client$getSpawnTime();
      return (double)since < (Double)(iEntity.blackout_Client$isOwn() ? rotation.noOwnTime : rotation.noOtherTime).get() * 1000.0D;
   }

   public static void swing(SwingState state, SwingType type, Hand hand) {
      swing.swing(state, type, hand);
   }

   public static void mineSwing(SwingSettings.MiningSwingState state) {
      swing.mineSwing(state);
   }

   public static PlaceData getPlaceData(BlockPos pos) {
      return facing.getPlaceData(pos, (DoublePredicate)null, (DoublePredicate)null, true);
   }

   public static PlaceData getPlaceData(BlockPos pos, boolean ignoreContainers) {
      return facing.getPlaceData(pos, (DoublePredicate)null, (DoublePredicate)null, ignoreContainers);
   }

   public static PlaceData getPlaceData(BlockPos pos, DoublePredicate<BlockPos, Direction> predicateOR, DoublePredicate<BlockPos, Direction> predicateAND, boolean ignoreContainers) {
      return facing.getPlaceData(pos, predicateOR, predicateAND, ignoreContainers);
   }

   public static PlaceData getPlaceData(BlockPos pos, DoublePredicate<BlockPos, Direction> predicateOR, DoublePredicate<BlockPos, Direction> predicateAND) {
      return facing.getPlaceData(pos, predicateOR, predicateAND, true);
   }

   public static Direction getPlaceOnDirection(BlockPos pos) {
      return facing.getPlaceOnDirection(pos);
   }

   public static boolean shouldInteractTrace() {
      return (Boolean)raytrace.interactTrace.get();
   }

   public static boolean shouldPlaceTrace() {
      return (Boolean)raytrace.placeTrace.get();
   }

   public static boolean shouldAttackTrace() {
      return (Boolean)raytrace.attackTrace.get();
   }

   public static boolean shouldMineTrace() {
      return (Boolean)raytrace.mineTrace.get();
   }

   public static boolean interactTrace(BlockPos pos) {
      return raytrace.interactTrace(pos);
   }

   public static boolean placeTrace(BlockPos pos) {
      return raytrace.placeTrace(pos);
   }

   public static boolean attackTrace(Box bb) {
      return raytrace.attackTrace(bb);
   }

   public static boolean mineTrace(BlockPos pos) {
      return raytrace.mineTrace(pos);
   }

   public static boolean oldCrystals() {
      return (Boolean)server.oldCrystals.get();
   }

   public static boolean cc() {
      return (Boolean)server.cc.get();
   }

   public static boolean grimMovement() {
      return (Boolean)server.grimMovement.get();
   }

   public static boolean grimPackets() {
      return (Boolean)server.grimPackets.get();
   }

   public static boolean grimUsing() {
      return (Boolean)server.grimUsing.get();
   }

   public static boolean strictSprint() {
      return (Boolean)server.strictSprint.get();
   }

   public static boolean stepPredict() {
      return (Boolean)extrapolation.stepPredict.get();
   }

   public static int reverseStepTicks() {
      return (Integer)extrapolation.reverseStepTicks.get();
   }

   public static boolean reverseStepPredict() {
      return (Boolean)extrapolation.reverseStepPredict.get();
   }

   public static int stepTicks() {
      return (Integer)extrapolation.stepTicks.get();
   }

   public static boolean jumpPredict() {
      return (Boolean)extrapolation.jumpPredict.get();
   }
}
