package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import net.astellismodding.golfwithmates.block.entity.BeamBlockEntity;
import net.astellismodding.golfwithmates.sound.ModSounds;
import net.astellismodding.golfwithmates.util.ClubUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BeamBlock extends BaseEntityBlock {
    public static final MapCodec<BeamBlock> CODEC = simpleCodec(BeamBlock::new);

    public BeamBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BeamBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ItemStack club = player.getMainHandItem();

        Vec3 TargetLocation = ClubUtils.calculateHitResultAbsoluteLocation(pos.getX(),pos.getY(),pos.getZ(), player.getYRot(),  1, 1);
        BeamBlockEntity beamBlock = (BeamBlockEntity) level.getBlockEntity(pos);
        Vec3 translate = new Vec3(0.5,0.5,0.5).add(TargetLocation);
        beamBlock.addTargetPosition(translate);

        if (!beamBlock.isActive()){
            beamBlock.setActive(true);
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);

    }
}
