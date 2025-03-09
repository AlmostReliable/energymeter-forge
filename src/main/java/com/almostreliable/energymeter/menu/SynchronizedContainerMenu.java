package com.almostreliable.energymeter.menu;

import com.almostreliable.energymeter.network.menu.MenuSynchronizer;
import com.almostreliable.energymeter.network.packet.MenuSyncPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class SynchronizedContainerMenu<E extends BlockEntity> extends AbstractContainerMenu {

    protected final Inventory playerInventory;
    protected final E blockEntity;
    protected final MenuSynchronizer menuSynchronizer;
    private final ContainerLevelAccess access;

    SynchronizedContainerMenu(MenuType<?> menuType, int wid, Inventory playerInventory, E blockEntity) {
        super(menuType, wid);
        this.playerInventory = playerInventory;
        this.blockEntity = blockEntity;
        this.menuSynchronizer = new MenuSynchronizer();
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), blockEntity.getBlockPos());
        // noinspection AbstractMethodCallInConstructor
        setupDataHandlers();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        if (playerInventory.player instanceof ServerPlayer player && menuSynchronizer.hasDataHandlers()) {
            PacketDistributor.sendToPlayer(player, MenuSyncPacket.of(containerId, menuSynchronizer::encodeAll));
        }
    }

    @Override
    public void broadcastChanges() {
        if (playerInventory.player instanceof ServerPlayer player && menuSynchronizer.hasChanged()) {
            PacketDistributor.sendToPlayer(player, MenuSyncPacket.of(containerId, menuSynchronizer::encode));
        }
        super.broadcastChanges();
    }

    public void receiveServerData(FriendlyByteBuf data) {
        menuSynchronizer.decode(data);
    }

    public abstract void setupDataHandlers();

    public abstract void receiveClientData(ServerPlayer player, CompoundTag data);

    @SuppressWarnings("resource")
    public boolean isClient() {
        return playerInventory.player.level().isClientSide();
    }
}
