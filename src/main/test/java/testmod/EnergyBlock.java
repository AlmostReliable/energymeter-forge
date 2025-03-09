package java.testmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.github.almostreliable.energymeter.block.entity.TickableBlock;

import org.jetbrains.annotations.Nullable;

public class EnergyBlock extends TickableBlock {

    public EnergyBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyBlockEntity(pos, state);
    }
}
