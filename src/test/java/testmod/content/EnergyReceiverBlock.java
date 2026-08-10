package testmod.content;

import com.almostreliable.energymeter.block.entity.TickableMenuBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

public class EnergyReceiverBlock extends TickableMenuBlock {

    public EnergyReceiverBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyReceiverBlockEntity(pos, state);
    }
}
