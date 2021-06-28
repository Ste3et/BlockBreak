package de.Ste3et_C0st.DiceBlockBreak;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class BlockBreakEvent implements Listener {

	private HashSet<BlockBreaker> blockBreakSet = new HashSet<BlockBreaker>();
	private static HashSet<Material> transparentBlocks = new HashSet<>(Arrays.asList(Material.WATER, Material.AIR, Material.CAVE_AIR));
	private HashSet<Material> materialBreakList = new HashSet<Material>(Arrays.asList(Material.BEDROCK, Material.END_PORTAL_FRAME, Material.BARRIER, Material.LIGHT));
	private HashSet<Material> breakableMaterial = new HashSet<Material>(Arrays.asList(Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE, Material.LIGHT));
	
	/*
	 * Author: Ste3et_C0st
	 * Description: Let the Player can mine non destroying blocks(Bedrock,End_Portal_Frame,Barrier) 
	 */
	
	public BlockBreakEvent() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(DiceBlockBreak.getInstance(),
				ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
			public void onPacketReceiving(PacketEvent event) {
				BlockBreaker breaker = blockBreakSet.stream().filter(entry -> entry.isLocation(event.getPacket().getBlockPositionModifier().read(0))).findFirst().orElse(null);
				if(Objects.nonNull(breaker)) {
					if(breaker.getPlayer().equals(event.getPlayer())) {
						breaker.setStep(10);
						breaker.sendPacket();
						blockBreakSet.remove(breaker);
					}
				}
			}
		});
	}
	
	
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		if(e.isCancelled()) return;
		if(e.useInteractedBlock() == Result.DENY) return;
		if(e.useItemInHand() == Result.DENY) return;
		if(e.hasBlock() && e.hasItem()) {
			ItemStack stack = e.getItem();
			Block block = e.getClickedBlock();
			Location location = block.getLocation();
			if(breakableMaterial.contains(stack.getType())) {
				if(materialBreakList.contains(block.getType())) {
					if(location.getBlockY() < 1) {return;}
					if(location.getWorld().getEnvironment() == Environment.NETHER && location.getBlockY() >= 127) {return;}
					if(location.getWorld().getEnvironment() == Environment.THE_END) return;
					BlockBreaker breaker = blockBreakSet.stream().filter(entry -> entry.isLocation(location))
							.findFirst().orElse(null);
					if (Objects.isNull(breaker)) {
						
						
						int durability = 1500;
						
						if(block.getType() == Material.BARRIER) {
							block.getWorld().spawnParticle(Particle.BARRIER, location.add(.5, .5, .5), 1);
							durability = 500;
						}else if(block.getType() == Material.LIGHT) {
							block.getWorld().spawnParticle(Particle.LIGHT, location.add(.5, .5, .5), 1);
							durability = 50;
						}
						
						breaker = new BlockBreaker(location, durability, e.getPlayer());
						blockBreakSet.add(breaker);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		if(Objects.isNull(player)) return;
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			Block block = player.getTargetBlock(transparentBlocks, 5);
			Location blockPosition = block.getLocation();
			
			ItemStack stack = player.getInventory().getItemInMainHand();
			if (!breakableMaterial.contains(stack.getType())) {
				return;
			}
			ItemMeta meta = stack.getItemMeta();
			double efficiency = 1;
			if(meta.hasEnchant(Enchantment.DIG_SPEED)) {
				efficiency = meta.getEnchantLevel(Enchantment.DIG_SPEED) * 1.25;
			}
			
			if (Objects.nonNull(blockPosition)) {
				Material material = block.getType();
				if (materialBreakList.contains(material)) {
					BlockBreaker breaker = blockBreakSet.stream().filter(entry -> entry.isLocation(blockPosition))
							.findFirst().orElse(null);
					if(Objects.isNull(breaker)) return;
					if ((System.currentTimeMillis() - breaker.getTimestamp() - 200) > breaker.getStep() * (breaker.getTime() / efficiency)) {
						if (breaker.getStep() < breaker.getMaxStep()) {
							breaker.setStep(breaker.getStep() + 1);
							if(material.equals(Material.BARRIER)) {
								block.getWorld().spawnParticle(Particle.BARRIER, blockPosition.add(.5, .5, .5), 1);
							}
						} else {
							org.bukkit.event.block.BlockBreakEvent breakEvent = new org.bukkit.event.block.BlockBreakEvent(block, player);
							Bukkit.getPluginManager().callEvent(breakEvent);
							if(!breakEvent.isCancelled()) {
								blockBreakSet.remove(breaker);
								block.getWorld().dropItem(blockPosition.add(.5, .5, .5), new ItemStack(block.getType()));
								block.getWorld().playEffect(blockPosition, Effect.STEP_SOUND, block.getType());
								block.setType(Material.AIR);
								player.playSound(blockPosition, Sound.BLOCK_STONE_BREAK, 1, 1);
								breaker.setStep(10);
								event.setCancelled(true);
							}else {
								breaker.setStep(10);
								blockBreakSet.remove(breaker);
								event.setCancelled(true);
							}
						}
						breaker.sendPacket();
						return;
					}
					if(material.equals(Material.BARRIER)) {
						block.getWorld().spawnParticle(Particle.BLOCK_CRACK, blockPosition.add(.5, .5, .5), 5, .25, .25, .25, Material.BARRIER.createBlockData());
					}
				}
			}
		}
	}

}
