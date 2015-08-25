package team.chisel.common.util;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.common.property.IExtendedBlockState;
import team.chisel.common.block.BlockCarvable;
import team.chisel.common.connections.CTMConnections;
import team.chisel.common.connections.EnumConnection;

import com.google.common.base.Optional;

/**
 * Think of this class as a "Two dimensional ForgeDirection, with diagonals".
 * <p>
 * It represents the eight different directions a face of a block can connect with CTM, and contains the logic for determining if a block is indeed connected in that direction.
 * <p>
 * Note that, for example, {@link #TOP_RIGHT} does not mean connected to the {@link #TOP} and {@link #RIGHT}, but connected in the diagonal direction represented by {@link #TOP_RIGHT}. This is used
 * for inner corner rendering.
 */
public enum Dir {
	// @formatter:off
    TOP(UP), 
    TOP_RIGHT(UP, EAST), 
    RIGHT(EAST), 
    BOTTOM_RIGHT(DOWN, EAST), 
    BOTTOM(DOWN), 
    BOTTOM_LEFT(DOWN, WEST), 
    LEFT(WEST), 
    TOP_LEFT(UP, WEST);
    // @formatter:on

	/**
	 * All values of this enum, used to prevent unnecessary allocation via {@link #values()}.
	 */
	public static final Dir[] VALUES = values();
	private static final EnumFacing NORMAL = SOUTH;

	private EnumFacing[] dirs;

	private Dir(EnumFacing... dirs) {
		this.dirs = dirs;
	}

	/**
	 * Finds if this block is connected for the given side in this Dir.
	 * 
	 * @param state The BlockState fo the block
	 * @param side The Side of the block that is being checked
	 * @return Whether the block is connected on the specified side
	 */
	@SuppressWarnings("unchecked")
	public boolean isConnected(IExtendedBlockState state, EnumFacing side) {
		EnumFacing[] dirs = getNormalizedDirs(side);
		EnumConnection connection = EnumConnection.fromFacings(dirs);
		return ((Optional<CTMConnections>) state.getUnlistedProperties().get(BlockCarvable.CONNECTIONS)).or(new CTMConnections()).isConnected(connection);
	}

	private EnumFacing[] getNormalizedDirs(EnumFacing normal) {
		if (normal == NORMAL) {
			return dirs;
		} else if (normal == NORMAL.getOpposite()) {
			// If this is the opposite direction of the default normal, we
			// need to mirror the dirs
			// A mirror version does not affect y+ and y- so we ignore those
			EnumFacing[] ret = new EnumFacing[dirs.length];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = dirs[i].getFrontOffsetY() != 0 ? dirs[i] : dirs[i].getOpposite();
			}
			return ret;
		} else {
			EnumFacing axis = null;
			// Next, we need different a different rotation axis depending
			// on if this is up/down or not
			if (normal.getFrontOffsetY() == 0) {
				// If it is not up/down, pick either the left or right-hand
				// rotation
				axis = normal == NORMAL.rotateY() ? UP : DOWN;
			} else {
				// If it is up/down, pick either the up or down rotation.
				axis = normal == UP ? NORMAL.rotateYCCW() : NORMAL.rotateY();
			}
			EnumFacing[] ret = new EnumFacing[dirs.length];
			// Finally apply all the rotations
			for (int i = 0; i < ret.length; i++) {
				ret[i] = rotate(dirs[i], axis);
			}
			return ret;
		}
	}

	// God why

	private static final int[] FACING_LOOKUP = new int[EnumFacing.values().length];
	static {
		FACING_LOOKUP[NORTH.ordinal()] = 1;
		FACING_LOOKUP[EAST.ordinal()] = 2;
		FACING_LOOKUP[SOUTH.ordinal()] = 3;
		FACING_LOOKUP[WEST.ordinal()] = 4;
		FACING_LOOKUP[UP.ordinal()] = 5;
		FACING_LOOKUP[DOWN.ordinal()] = 6;
	}

	private EnumFacing rotate(EnumFacing facing, EnumFacing axisFacing) {
		Axis axis = axisFacing.getAxis();
		AxisDirection axisDir = axisFacing.getAxisDirection();

		try {
			if (axisDir == AxisDirection.POSITIVE) {
				return facing.rotateAround(axis);
			}

			switch (axis) {
			case X:
				// I did some manual testing and this is what worked...I don't get it either
				switch (FACING_LOOKUP[facing.ordinal()]) {
				case 1:
					return NORTH;
				case 2:
				case 4:
				default:
					throw new IllegalStateException("Unable to get X-rotated facing of " + this);
				case 3:
					return SOUTH;
				case 5:
					return SOUTH;
				case 6:
					return NORTH;
				}
			case Y:
				return facing.rotateYCCW();
			case Z:
				switch (FACING_LOOKUP[facing.ordinal()]) {
				case 2:
					return EAST;
				case 3:
				default:
					throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
				case 4:
					return WEST;
				case 5:
					return DOWN;
				case 6:
					return UP;
				}
			}
		} catch (IllegalStateException e) {
			// fall through
		}
		return facing;
	}
}