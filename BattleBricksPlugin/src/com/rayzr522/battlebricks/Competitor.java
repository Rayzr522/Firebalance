package com.rayzr522.battlebricks;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/* 
 * Competitor.java
 * Made by Rayzr522
 * Date: Jul 11, 2016
 */
public class Competitor {

	private Player player;
	private BrickItem brick;
	private boolean fighting;
	private int damageTaken;
	private boolean nextIsLeft;
	private int comboTaken;
	private int recovery;
	
	public Competitor(Player player) {

		this.player = player;

		ItemStack item = player.getInventory().getItemInMainHand();

		System.out.println("BrickItem.isValid(item)? " + BrickItem.isValid(item));
		if (BrickItem.isValid(item)) {

			brick = BrickItem.fromItem(item);

		}

	}

	public boolean isValid() {
		return brick != null;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public BrickItem getBrick() {
		return brick;
	}

	public void updateBrick() {
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (brick.equals(item)) {
				inv.setItem(i, brick);
			}
		}

	}

	public void setBrick(BrickItem brick) {
		this.brick = brick;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Competitor) {
			return ((Competitor) obj).getPlayer().getUniqueId().equals(getPlayer().getUniqueId());
		} else if (obj instanceof Player) {
			return ((Player) obj).getUniqueId().equals(getPlayer().getUniqueId());
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return player.hashCode();
	}

	public void msg(String msg) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	public String getName() {
		return player.getDisplayName();
	}

	public boolean isFighting() {
		return fighting;
	}

	public void setFighting(boolean fighting) {
		this.fighting = fighting;
	}

}
