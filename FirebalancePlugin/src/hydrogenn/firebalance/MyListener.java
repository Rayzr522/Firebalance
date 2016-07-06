
package hydrogenn.firebalance;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hydrogenn.firebalance.utils.Messenger;
import hydrogenn.firebalance.utils.TextUtils;

public class MyListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore()) {
			event.setJoinMessage("�6" + player.getName() + " has joined Firebalance for the first time!");
		} else {
			event.setJoinMessage("�e" + player.getName() + " has joined.");
			for (PlayerSpec s : Firebalance.playerSpecList) {
				if (s.getName().equals(player.getName())) {
					s.setOnline(true);
					int rank = s.getKing();
					String rankString = "citizen.";
					String nationString = Firebalance.getNationColor(s.getNation(), false) + Firebalance.getNationName(s.getNation(), false);
					if (rank == 1) rankString = "leader.";
					else if (rank != 0) rankString = "official.";
					player.sendMessage("�7You are a " + nationString + "�7 " + rankString);
				}
			}
			if (Firebalance.killList.containsValue(player.getName())) {
				for (String s : Firebalance.killList.keySet()) {
					if (Firebalance.killList.get(s).equals(player.getName())) Firebalance.killList.remove(s);
				}
			}
		}
		if (Firebalance.getPlayerFromName(player.getName()) == null) Firebalance.playerSpecList.add(new PlayerSpec(event.getPlayer().getName(), (byte) -1, 0, 0, true));
		player.sendMessage("This server uses a plugin in-development. Issues may arise. Report them for credits.");
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (!event.getPlayer().hasPlayedBefore()) event.setQuitMessage("�6" + event.getPlayer().getName() + " has left. We hope to see you again!");
		else event.setQuitMessage("�e" + event.getPlayer().getName() + " has left.");
		for (PlayerSpec s : Firebalance.playerSpecList)
			if (s.getName().equals(event.getPlayer().getName())) {
				s.setOnline(false);
			}
	}

	@EventHandler
	public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player att = null;
			Player def = (Player) event.getEntity();
			if (event.getDamager().getType() == EntityType.PLAYER) {
				att = (Player) event.getDamager();
				def = (Player) event.getEntity();
			} else if (event.getDamager() instanceof Projectile) {
				Projectile proj = (Projectile) event.getDamager();
				if (proj.getShooter() instanceof Player) {
					att = (Player) proj.getShooter();
					def = (Player) event.getEntity();
				}
			}
			if (att != null) {
				double xpAtt = att.getLevel();
				double xpDef = def.getLevel();
				if (xpDef == 0)
					xpDef = 1;
				if (xpAtt > xpDef)
					event.setDamage(event.getDamage() * xpAtt / xpDef);
				for (Iterator<SchedulerCache> i = Firebalance.scheduleList.iterator(); i.hasNext();) {
					SchedulerCache s = i.next();
					if (s.type.contains("chunk") && s.callerName.equals(def.getName()))
						if (Bukkit.getScheduler().isQueued(s.id)) {
							Messenger.send(def, "&cTask cancelled.");
							Bukkit.getScheduler().cancelTask(s.id);
							i.remove();
						}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player victim = event.getEntity();
		List<ItemStack> drops = event.getDrops();
		int lvl = (int) victim.getLevel();
		int xp = (int) (((lvl * .75) * (lvl * .75) + 6 * (lvl * .75)) / 7);
		String perpName;
		if (victim.getKiller() != null)
			perpName = victim.getKiller().getName();
		else
			perpName = "nature";
		boolean victKing = false;
		boolean validElites = false;
		byte nationVict = -1;
		String nationString = "<?>";
		Date banDate = new Date();
		String coords = victim.getLocation().getBlockX() + ", " + victim.getLocation().getBlockY() + ", "
				+ victim.getLocation().getBlockZ();
		PlayerSpec result = Firebalance.getPlayerFromName(perpName);
		int nationPerp = -1;
		for (PlayerSpec s : Firebalance.playerSpecList) {
			if (s.getName().equals(perpName)) {
				nationPerp = s.getNation();
			}
			if (s.getName().equals(victim.getName())) {
				nationVict = s.getNation();
				if (Firebalance.getNationName(nationVict, true) != null)
					nationString = Firebalance.getNationName(nationVict, true);
				if (s.getKing() == 1)
					victKing = true;
				s.setKing(0);
			}
			if (s.getNation() == nationVict && s.getKing() > 1)
				validElites = true;
		}
		event.setDeathMessage(ChatColor.RED + event.getDeathMessage() + ". (" + coords + ") They were level "
				+ Integer.toString(lvl) + ".");
		if (nationPerp == nationVict && victKing) {
			result.setKing(1);
			event.setDeathMessage(TextUtils
					.colorize("&c" + victim.getName() + " was &lmurdered&c by " + perpName + ", the new leader of "
							+ nationString + ". (" + coords + ") They were level " + Integer.toString(lvl) + "."));
		} else {
			final String ns = nationString;
			if (victKing) {
				if (!validElites)
					Messenger.broadcast(ns + " is without a leader! Anyone can claim the throne with /enthrone!");
				else {
					Messenger.broadcast(ns
							+ " is without a leader! Only officials can claim the throne with /enthrone for the next minute.");
					Firebalance.addScheduler("leaderWait", ns, 1200L, new Runnable() {

						public void run() {
							Messenger.broadcast(ns
									+ "'s officials did not take the throne! Anyone can claim the throne with /enthrone!");
						}
					});
				}
			}
		}
		event.setDroppedExp(0);
		for (int i = 64; i <= xp; i += 64) {
			drops.add(new ItemStack(Material.EXP_BOTTLE, 64));
		}
		if (xp % 64 > 0)
			drops.add(new ItemStack(Material.EXP_BOTTLE, xp % 64));
		if (!perpName.equals("nature")) {
			Firebalance.killList.put(perpName, victim.getName());
			victim.getKiller().sendMessage(ChatColor.GRAY
					+ "'/sentence new' or '/oopsmybad' if you want something other than the 5 minute wait.");
			banDate.setTime(System.currentTimeMillis() + 300000);
			Bukkit.getBanList(Type.NAME)
					.addBan(victim.getName(), "You've died recently and must wait 5 minutes.", banDate, "").save();
			Firebalance.addSyncScheduler("kickDeadPlayer", victim.getName(), 20L, new Runnable() {

				public void run() {
					victim.kickPlayer("You've died! Wait 5 minutes to return.");
				}
			});
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		// Check if the player is in untamed lands, ie nether/the end
		if (event.getPlayer().getWorld().getName().contains("_nether")
				|| event.getPlayer().getWorld().getName().contains("_the_end")) {
			return;
		}
		if (!event.getTo().getChunk().equals(event.getFrom().getChunk())
				|| (event.getTo().getBlockY() < 56) != (event.getFrom().getBlockY() < 56)
				|| (event.getTo().getBlockY() > 112) != (event.getFrom().getBlockY() > 112)) {
			for (Iterator<SchedulerCache> i = Firebalance.scheduleList.iterator(); i.hasNext();) {
				SchedulerCache s = i.next();
				if (s.type.contains("chunk") && s.callerName.equals(event.getPlayer().getName()))
					if (Bukkit.getScheduler().isQueued(s.id)) {
						Messenger.send(event.getPlayer(), "&cTask cancelled.");
						Bukkit.getScheduler().cancelTask(s.id);
						i.remove();
					}
			}
			byte nationFrom = -1;
			byte nationTo = -1;
			int yt = 0;
			if (event.getTo().getBlockY() < 56)
				yt = -1;
			if (event.getTo().getBlockY() > 112)
				yt = 1;
			int yf = 0;
			if (event.getFrom().getBlockY() < 56)
				yf = -1;
			if (event.getFrom().getBlockY() > 112)
				yf = 1;
			String nationString = ChatColor.WHITE + "<?>";
			String heightPrefix = "";
			String heightSuffix = "";
			for (ChunkSpec s : Firebalance.chunkSpecList) {
				if (s.x == event.getTo().getChunk().getX() && s.y == yt && s.z == event.getTo().getChunk().getZ()) {
					nationTo = s.nation;
					if (s.nation == 0)
						nationString = ChatColor.WHITE + "" + s.owner + "'s";
				}
				if (s.x == event.getFrom().getChunk().getX() && s.y == yf && s.z == event.getFrom().getChunk().getZ()) {
					nationFrom = s.nation;
					if (s.nation == 0)
						nationString = ChatColor.WHITE + "" + s.owner + "'s";
				}
			}
			if (nationTo != -1) {
				if (Firebalance.getNationName(nationTo, true) != null)
					nationString = Firebalance.getNationColor(nationTo, true)
							+ Firebalance.getNationName(nationTo, true);
				if (yt == 1) {
					if (nationTo != 0)
						heightPrefix = "the ";
					heightSuffix = " " + ChatColor.GRAY + "Skyloft";
				} else if (yt == -1) {
					if (nationTo != 0)
						heightPrefix = "the ";
					heightSuffix = " " + ChatColor.GRAY + "Undergrounds";
				} else if (nationTo == 0)
					heightSuffix = " " + ChatColor.GRAY + "territory";
			} else if (nationFrom != -1) {
				if (Firebalance.getNationName(nationFrom, true) != null)
					nationString = Firebalance.getNationColor(nationFrom, true)
							+ Firebalance.getNationName(nationFrom, true);
				if (yf == 1) {
					if (nationFrom != 0)
						heightPrefix = "the ";
					heightSuffix = " " + ChatColor.GRAY + "Skyloft";
				} else if (yf == -1) {
					if (nationFrom != 0)
						heightPrefix = "the ";
					heightSuffix = " " + ChatColor.GRAY + "Undergrounds";
				} else if (nationTo == 0)
					heightSuffix = " " + ChatColor.GRAY + "territory";
			}
			if (nationFrom != nationTo && nationTo != -1) {
				Messenger.send(event.getPlayer(),
						"&7You are now entering " + heightPrefix + nationString + heightSuffix);
			} else if (nationFrom != nationTo && nationFrom != -1)
				Messenger.send(event.getPlayer(),
						"&7You are now leaving " + heightPrefix + nationString + heightSuffix);
		}
	}

	@EventHandler
	public void onChatMessage(AsyncPlayerChatEvent event) {
		// TODO re-add channels
		boolean king = false;
		String prefix = ChatColor.RED + "";
		byte nationValue = -1;
		// Set<Player> recipients = event.getRecipients();
		for (PlayerSpec s : Firebalance.playerSpecList) {
			if (s.getName().equals(event.getPlayer().getName())) {
				nationValue = s.getNation();
				if (s.getKing() == 1)
					king = true;
				prefix = Firebalance.getNationColor(nationValue, false);
				if (prefix == "")
					prefix = ChatColor.DARK_GRAY + "";
			}
		}
		if (king)
			event.setFormat(TextUtils.colorize(prefix + "%1$s: &6%2$s"));
		else if (nationValue != -1)
			event.setFormat(TextUtils.colorize(prefix + "%1$s: &f%2$s"));
		else
			event.setFormat(TextUtils.colorize(prefix + "%1$s: &7%2$s"));
		/*
		 * for (Iterator<Player> i = recipients.iterator(); i.hasNext();) {
		 * Player p = i.next(); String pn = p.getName(); if
		 * (Firebalance.getPlayerFromName(pn)!=null &&
		 * (Firebalance.channelListening.get(pn)&channel)==0) i.remove(); else
		 * if (Firebalance.getPlayerFromName(pn)!=null && channel == 2 &&
		 * Firebalance.getPlayerFromName(pn).nation!=nationValue) i.remove(); }
		 */
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void customCrafting(PrepareItemCraftEvent e) {
		ItemStack r = e.getInventory().getResult();
		if (r.getType() == Material.TRIPWIRE_HOOK && r.getItemMeta().getDisplayName() != null
				&& r.getItemMeta().getDisplayName().contains(ChatColor.WHITE + "Key")) {
			// Do all the universal variable declarations
			boolean success = true;
			String dn = e.getInventory().getResult().getItemMeta().getDisplayName();
			ItemStack result = new ItemStack(Material.TRIPWIRE_HOOK);
			ItemMeta resultMeta = result.getItemMeta();
			List<String> keyLore = new ArrayList<>();
			keyLore.add(ChatColor.GRAY + "");
			resultMeta.setLore(keyLore);
			resultMeta.setDisplayName(ChatColor.WHITE + "Key");
			// Set the amount to double if it's a dupe function
			if (dn.equals(ChatColor.WHITE + "KeyD"))
				result.setAmount(2);
			// Show that a new key is being crafted if one is
			if (dn.equals(ChatColor.WHITE + "KeyC")) {
				resultMeta.setDisplayName(ChatColor.GOLD + "New Key");
				List<String> resultLore = new ArrayList<>();
				resultLore.add("****");
				resultMeta.setLore(resultLore);
			}
			// Run the item loop if it's not a craft function
			if (!dn.equals(ChatColor.WHITE + "KeyC"))
				for (ItemStack s : e.getInventory()) {
					if (s != null) {
						if (s.getType() == Material.TRIPWIRE_HOOK && s.getItemMeta().getLore() != null) {
							String keyID = s.getItemMeta().getLore().get(0).replace(ChatColor.GRAY + "", "");
							String oldKeyID = resultMeta.getLore().get(0);
							if (oldKeyID.length() < 18) {
								List<String> resultLore = resultMeta.getLore();
								resultLore.set(0, oldKeyID + keyID);
								resultMeta.setLore(resultLore);
							}

						} else if (dn.equals(ChatColor.WHITE + "KeyD") && s.getType() == Material.TRIPWIRE_HOOK)
							result.setAmount(1);
						else if (s.getType() == Material.TRIPWIRE_HOOK)
							success = false;
					}
				}
			// Change the result to what is intended
			if (!success) {
				resultMeta.setDisplayName("Nice try, pal!");
				resultMeta.setLore(null);
			}
			result.setItemMeta(resultMeta);
			e.getInventory().setResult(result);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack i = event.getCurrentItem();
		if (event.getResult() == Result.ALLOW && i != null && i.getType() == Material.TRIPWIRE_HOOK && i.hasItemMeta()
				&& i.getItemMeta().hasDisplayName()
				&& i.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "New Key")) {
			ItemMeta im = i.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Key");
			Random rand = new Random();
			String randid = String.format("%04x", rand.nextInt(65536));
			List<String> resultLore = new ArrayList<>();
			resultLore.add(ChatColor.GRAY + randid);
			im.setLore(resultLore);
			i.setItemMeta(im);
			event.setCurrentItem(i);
		}
	}

	@EventHandler
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (event.getClickedBlock().getType() == Material.CHEST
				|| event.getClickedBlock().getType() == Material.TRAPPED_CHEST) {
			Player player = event.getPlayer();
			Block chest = event.getClickedBlock();
			String chestId = null;
			ItemStack key = event.getItem();
			ItemMeta keyMeta = null;
			if (key != null)
				keyMeta = key.getItemMeta();
			boolean isKey = false;
			try {
				if (keyMeta.getDisplayName().equals(ChatColor.WHITE + "Key")
						&& key.getType().equals(Material.TRIPWIRE_HOOK)) {
					isKey = true;
				}
			} catch (NullPointerException e) {
			}
			for (ChestSpec s : Firebalance.chestSpecList) {
				if (s.coords.equals(chest.getLocation())) {
					chestId = s.id;
					if (chestId.length() < 1) {
						chestId = null;
						return;
					}
				}
			}
			if (chestId != null) {
				try {

					if (keyMeta.getLore().get(0).contains(chestId)) {
					} else {
						player.sendMessage(
								"&cThis chest is locked with id " + chestId.substring(0, chestId.length() * 3 / 4)
										+ "****".substring(0, chestId.length() / 4));
						event.setCancelled(true);
						if (chest.getType() == Material.TRAPPED_CHEST) {
							// TODO allow the device to emit a single redstone
							// pulse
						}
					}
				} catch (NullPointerException e) {
					Messenger.send(player,
							"&cThis chest is locked with id " + chestId.substring(0, chestId.length() * 3 / 4)
									+ "****".substring(0, chestId.length() / 4));
					event.setCancelled(true);
				}
			} else if (isKey && !player.isSneaking()) {
				Firebalance.chestSpecList.add(
						new ChestSpec(chest.getLocation(), keyMeta.getLore().get(0).replace(ChatColor.GRAY + "", "")));
				event.setCancelled(true);
			}
			if (key != null & keyMeta != null)
				key.setItemMeta(keyMeta);
		}
	}

	@EventHandler
	public void onEntityInteract(EntityInteractEvent event) {
		if (event.getBlock().getType() == Material.SOIL && event.getEntity() instanceof Creature)
			event.setCancelled(true);
	}

	@EventHandler
	public void onBreakBlock(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (Firebalance.getPlayerFromName(player.getName()).getNation() == -1) {
			// TODO adjust this for the new nation system
			event.getPlayer().sendMessage(ChatColor.RED + "You're not in a nation yet. Do '/nation' for some help.");
			event.setCancelled(true);
		}
		// Check if the player is in untamed lands, ie nether/the end
		if (!(event.getBlock().getWorld().getName().contains("_nether")
				|| event.getBlock().getWorld().getName().contains("_the_end"))) {
			int x = event.getBlock().getChunk().getX();
			int y = 0;
			if (player.getLocation().getBlockY() < 56)
				y = -1;
			if (player.getLocation().getBlockY() > 112)
				y = 1;
			int z = event.getBlock().getChunk().getZ();
			int playerNation = -1;
			int chunkNation = -1;
			boolean chunkUnlocked = true;
			String chunkOwner = "";
			ArrayList<String> chunkShared = new ArrayList<>();
			boolean hasAccess = false;
			for (ChunkSpec s : Firebalance.chunkSpecList) {
				if (s.x == x && s.y == y && s.z == z) {
					chunkNation = s.nation;
					chunkOwner = s.owner;
					chunkUnlocked = s.national;
					chunkShared = s.shared;
				}
			}
			for (PlayerSpec s : Firebalance.playerSpecList) {
				if (s.getName().equals(player.getName())) {
					playerNation = s.getNation();
					if (chunkOwner.equals(s.getName()) || chunkShared.contains(s.getName())) {
						hasAccess = true;
					}
				}
			}
			if (!hasAccess) {
				// Check if the player is in foreign territory
				if (chunkNation != -1 && chunkNation != 0 && (chunkNation & playerNation) <= 0) {
					Messenger.send(player, "This is claimed by a foreign nation.");
					event.setCancelled(true);
				}

				// Check if the chunk is private or not
				if (!chunkUnlocked && (chunkNation & playerNation) != 0) {
					Messenger.send(player, "This chunk is private.");
					event.setCancelled(true);
				}

				// Check if the player is in freelance territory (not their own)
				if (chunkNation == 0) {
					if (playerNation != 0)
						Messenger.send(player, "Freelancers have claimed this.");
					else
						Messenger.send(player, "Someone else owns this.");
					event.setCancelled(true);
				}
			}
		}
		if (!event.isCancelled()) {
			for (Iterator<ChestSpec> i = Firebalance.chestSpecList.iterator(); i.hasNext();) {
				ChestSpec s = (ChestSpec) i.next();
				if (s.coords.equals(event.getBlock().getLocation())) {
					i.remove();
					event.getPlayer().sendMessage("Chest lock removed");
				}
			}
		}
	}

	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		int x = event.getBlock().getChunk().getX();
		int y = 0;
		if (player.getLocation().getBlockY() < 56)
			y = -1;
		if (player.getLocation().getBlockY() > 112)
			y = 1;
		int z = event.getBlock().getChunk().getZ();
		int playerNation = -1;
		int chunkNation = -1;
		boolean chunkUnlocked = true;
		String chunkOwner = "";
		ArrayList<String> chunkShared = new ArrayList<>();
		boolean hasAccess = false;
		// Check if the player is placing a blacklisted block
		if (event.getItemInHand().getItemMeta().getDisplayName() != null)
			if (event.getBlock().getType() == Material.TRIPWIRE_HOOK
					&& event.getItemInHand().getItemMeta().getLore() != null) {
				event.setCancelled(true);
				return;
			}
		if (Firebalance.getPlayerFromName(player.getName()).getNation() == -1) {
			// TODO adjust this for the new nation system
			event.getPlayer().sendMessage(ChatColor.RED + "You're not in a nation yet. Do '/nation' for some help.");
			event.setCancelled(true);
		}
		// Check if the player is in untamed lands, ie nether/the end
		if (event.getBlock().getWorld().getName().contains("_nether")
				|| event.getBlock().getWorld().getName().contains("_the_end")) {
			return;
		}
		for (ChunkSpec s : Firebalance.chunkSpecList) {
			if (s.x == x && s.y == y && s.z == z) {
				chunkNation = s.nation;
				chunkOwner = s.owner;
				chunkUnlocked = s.national;
				chunkShared = s.shared;
			}
		}
		for (PlayerSpec s : Firebalance.playerSpecList) {
			if (s.getName().equals(player.getName())) {
				playerNation = s.getNation();
				if (chunkOwner.equals(s.getName()) || chunkShared.contains(s.getName())) {
					hasAccess = true;
				}
			}
		}
		if (!hasAccess) {
			// Check if the player is in foreign territory
			if (chunkNation != -1 && chunkNation != 0 && (chunkNation & playerNation) <= 0) {
				Messenger.send(player, "This is claimed by a foreign nation.");
				event.setCancelled(true);
			}

			// Check if the chunk is private or not
			if (!chunkUnlocked && (chunkNation & playerNation) != 0) {
				Messenger.send(player, "This chunk is private.");
				event.setCancelled(true);
			}

			// Check if the player is in freelance territory (not their own)
			if (chunkNation == 0) {
				if (playerNation != 0)
					Messenger.send(player, "Freelancers have claimed this.");
				else
					Messenger.send(player, "Someone else owns this.");
				event.setCancelled(true);
			}
		}
	}
}
