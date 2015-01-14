package com.cricketcraft.chisel.compat;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;

import com.cricketcraft.chisel.block.BlockCarvable;

public class Waila implements IWailaDataProvider {

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler configHandler) {
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack stack, List<String> strings, IWailaDataAccessor accessor, IWailaConfigHandler configHandler) {
		return strings;
	}

	@Override
	public List<String> getWailaBody(ItemStack stack, List<String> strings, IWailaDataAccessor accessor, IWailaConfigHandler configHandler) {
		if (accessor.getBlock() instanceof BlockCarvable) {
			BlockCarvable block = (BlockCarvable) accessor.getBlock();
			strings.add(block.getVariation(accessor.getMetadata()).getDescription());
		}
		return strings;
	}

	@Override
	public List<String> getWailaTail(ItemStack stack, List<String> strings, IWailaDataAccessor accessor, IWailaConfigHandler configHandler) {
		return strings;
	}

	public static void register(IWailaRegistrar registrar) {
		Waila instance = new Waila();
		registrar.registerBodyProvider(instance, BlockCarvable.class);
	}
}