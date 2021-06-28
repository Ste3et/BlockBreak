package de.Ste3et_C0st.DiceBlockBreak;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

public class BlockBreaker {

	private final long timestamp = System.currentTimeMillis();
	private final int x, y, z, maxStep = 9, time;
	private int step = 0;
	private final PacketContainer container = new PacketContainer(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
	private final Player player;
	
	public BlockBreaker(Location location, int time, Player player) {
		this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), time, player);
	}
	
	public BlockBreaker(int x, int y, int z, int time, Player player) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.time = time;
		this.player = player;
		this.container.getIntegers().write(0, getBlockEntityId(x, y, z));
		this.container.getBlockPositionModifier().write(0, new BlockPosition(x,y,z));
	}
	
	public int getStep() {
		return this.step;
	}
	
	public int getMaxStep() {
		return this.maxStep;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
	
	public boolean isLocation(Location location) {
		return this.x == location.getBlockX() && this.y == location.getBlockY() && this.z == location.getBlockZ();
	}
	
	public boolean isLocation(BlockPosition location) {
		return this.x == location.getX() && this.y == location.getY() && this.z == location.getZ();
	}

	public int getTime() {
		return time;
	}

	public void setStep(int i) {
		this.step = i;
		this.container.getIntegers().write(1, this.step);
	}
	
	public void sendPacket() {
		ProtocolLibrary.getProtocolManager().broadcastServerPacket(this.container);
	}
	
	private static int getBlockEntityId(int x, int y, int z) {
        return   ((x & 0xFFF) << 20)
               | ((z & 0xFFF) << 8)
               | (y & 0xFF);
    }

	public Player getPlayer() {
		return player;
	}

	
}
