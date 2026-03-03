package testmod.content;

import com.almostreliable.energymeter.block.entity.TickableMenuBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class EnergyEmitterBlock extends TickableMenuBlock {

    public EnergyEmitterBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyEmitterBlockEntity(pos, state);
    }
}
