package fr.mrflamme26.playervisibility;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerVisibility extends JavaPlugin implements Listener {
	
	private Map<Player, Boolean> isHidingPlayers = new HashMap<>();
	private HashMap<String, String> replacements = new HashMap<>();
	
	public final Logger logger = Logger.getLogger("Minecraft");
	public static PlayerVisibility plugin;

	@Override
	public void onEnable() {

		PluginDescriptionFile pluginFile = this.getDescription();
		
		this.logger.info("§f[§e" + pluginFile.getName() + "§f] §aVersion " + pluginFile.getVersion() + ".");
		this.logger.info("§f[§e" + pluginFile.getName() + "§f] §aChargé avec succès.");
		this.logger.info("§f[§e" + pluginFile.getName() + "§f] §cUn plugin réalisé par MrFlamme26.");
		
		plugin = this;
		getServer().getPluginManager().registerEvents(this, this);
		configurationLoader();
	}

	@Override
	public void onDisable() {
		
		PluginDescriptionFile pluginFile = this.getDescription();
		
		this.logger.info("§f[§e" + pluginFile.getName() + "§f] §aDéchargé avec succès.");
		
		configurationLoader();
	}

	public void configurationLoader() {
		
		FileConfiguration configurationFile = getConfig();
		
		configurationFile.options().copyDefaults(true);
		configurationFile.options().copyHeader(true);
		
		if (!new File(this.getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		String actualWorld = player.getWorld().getName();
		
		for (String activatedWorlds : getConfig().getStringList("activatedWorlds")) {
			if (activatedWorlds.equals(actualWorld)) {
				if (getConfig().getBoolean("others.giveOnJoin")) {
					if (player.getInventory().contains(makeVanishItem(true)) || player.getInventory().contains(makeVanishItem(false))) {
						player.getInventory().remove(makeVanishItem(true));
						player.getInventory().remove(makeVanishItem(false));
						show(player, true);
						player.getInventory().setItem(getConfig().getInt("PlayerVisibility.inventorySlot") - 1, makeVanishItem(true));
					}
					else {
						show(player, true);
						player.getInventory().setItem(getConfig().getInt("PlayerVisibility.inventorySlot") - 1, makeVanishItem(true));
					}
				}
			}
		}

		if (getServer().getOnlinePlayers() != null) {
			for (Player players : getServer().getOnlinePlayers()) {
				if (isHidingPlayers(players)) {
					hide(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		Player player = event.getPlayer();
		String actualWorld = player.getWorld().getName();
		
		for (String activatedWorlds : getConfig().getStringList("activatedWorlds")) {
			if (activatedWorlds.equals(actualWorld)) {
				if (getConfig().getBoolean("others.giveOnRespawn")) {
					if (player.getInventory().contains(makeVanishItem(true)) || player.getInventory().contains(makeVanishItem(false))) {
						player.getInventory().remove(makeVanishItem(true));
						player.getInventory().remove(makeVanishItem(false));
						show(player, true);
						player.getInventory().setItem(getConfig().getInt("PlayerVisibility.inventorySlot") - 1, makeVanishItem(true));
					}
					else {
						show(player, true);
						player.getInventory().setItem(getConfig().getInt("PlayerVisibility.inventorySlot") - 1, makeVanishItem(true));
					}
				}
			}
		}

		if (getServer().getOnlinePlayers() != null) {
			for (Player players : getServer().getOnlinePlayers()) {
				if (isHidingPlayers(players)) {
					hide(player);
				}
			}
		}
	}

	public static String stringFromArguments(String[] arguments, int start, int minNumberArguments) throws Exceptions {
		
		StringBuilder stringBuilder = new StringBuilder();

		if (arguments.length < minNumberArguments) {
			throw new Exceptions();
		}
		else {
			for (int i = start; i < arguments.length; i++) {
				stringBuilder.append(arguments[i]);
				stringBuilder.append(" ");
			}
		}
		
		return stringBuilder.toString().trim();
	}

	public void sendMessage(String path, CommandSender sender) {
		
		if (sender != null) {
			String message = getConfig().getString("messages." + path);
			
			if (replacements != null) {
				for (Map.Entry<String, String> a : replacements.entrySet()) {
					message = message.replace(a.getKey(), a.getValue());
				}
			}
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			sendMessage("consoleError", sender);
			
			return false;
		}

		Player player = (Player) sender;

		if (command.getName().equalsIgnoreCase("playervisibility")) {
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("show")) {
					if (player.hasPermission("playervisibility.item.show") || player.isOp()) {
						show(player, false);
						sendMessage("playersON", player);
					}
					else {
						sendMessage("permissionError", player);
					}
				}
				else if (args[0].equalsIgnoreCase("hide")) {
					if (player.hasPermission("playervisibility.item.hide") || player.isOp()) {
						hide(player);
						sendMessage("playersOFF", player);
					}
					else {
						sendMessage("permissionError", player);
					}
				}
				else if (args[0].equalsIgnoreCase("toolon")) {
					if (player.hasPermission("playervisibility.tool") || player.isOp()) {
						player.getInventory().setItemInMainHand(makeVanishItem(true));
					}
					else {
						sendMessage("permissionError", player);
					}
				}
				else if (args[0].equalsIgnoreCase("tooloff")) {
					if (player.hasPermission("playervisibility.tool") || player.isOp()) {
						player.getInventory().setItemInMainHand(makeVanishItem(false));
					}
					else {
						sendMessage("permissionError", player);
					}
				}
				else if (args[0].equalsIgnoreCase("reload")) {
					if (player.hasPermission("playervisibility.reload") || player.isOp()) {
						reloadConfig();
						saveConfig();
						sendMessage("configurationReloaded", player);
					}
					else {
						sendMessage("permissionError", player);
					}
				}
				else {
					sendMessage("commandError", player);
				}
			} else if (args.length > 1) {
				try {
					if (args[0].equalsIgnoreCase("setitemname")) {
						if (player.hasPermission("playervisibility.setItemName") || player.isOp()) {
							replacements.put("%type%", args[1]);
							
							String message = stringFromArguments(args, 2, 3);
							
							replacements.put("%args%", message);
							
							if (args[1].equalsIgnoreCase("on")) {
								getConfig().set("PlayerVisibility.itemNameON", message);
							} 
							else if (args[1].equalsIgnoreCase("off")) {
								getConfig().set("PlayerVisibility.itemNameON", message);
							} 
							else {
								sendMessage("commandError", player);
							}
							
							sendMessage("itemName", player);
						} 
						else {
							sendMessage("permissionError", player);
						}
						
					} else if (args[0].equalsIgnoreCase("setmessage")) {
						if (player.hasPermission("playervisibility.setItemMessage") || player.isOp()) {
							replacements.put("%type%", args[1]);
							
							String message = stringFromArguments(args, 2, 3);
							
							replacements.put("%args%", message);
							
							if (args[1].equalsIgnoreCase("playerson")) {
								getConfig().set("messages.playersON", message);
							}
							else if (args[1].equalsIgnoreCase("playersoff")) {
								getConfig().set("messages.playersOFF", message);
							}
							else if (args[1].equalsIgnoreCase("cooldown")) {
								getConfig().set("messages.cooldownTime", message);
							}
							else if (args[1].equalsIgnoreCase("permissionError")) {
								getConfig().set("messages.permissionError", message);
							}
							else if (args[1].equalsIgnoreCase("commandError")) {
								getConfig().set("messages.commandError", message);
							}
							else if (args[1].equalsIgnoreCase("configurationReloaded")) {
								getConfig().set("messages.configurationReloaded", message);
							} 
							else if (args[1].equalsIgnoreCase("itemName")) {
								getConfig().set("messages.itemName", message);
							}
							else if (args[1].equalsIgnoreCase("itemMessage")) {
								getConfig().set("messages.itemMessage", message);
							}
							else {
								sendMessage("commandError", player);
							}
							
							saveConfig();
							reloadConfig();
							sendMessage("itemMessage", player);
							
						}
						else {
							sendMessage("permissionError", player);
						}
					}
					else {
						sendMessage("commandError", player);
					}
				}
				catch (Exception e) {
					sendMessage("commandError", player);
				}
			}
			else {
				sendMessage("commandError", player);
			}
		}
		return false;
	}

	public ItemStack makeVanishItem(boolean itemToggled) {
		
		String item;
		String itemNamePath;
		
		if (itemToggled) {
			item = getConfig().getString("PlayerVisibility.itemPlayersON");
			itemNamePath = "PlayerVisibility.itemNameON";
		}
		else {
			item = getConfig().getString("PlayerVisibility.itemPlayersOFF");
			itemNamePath = "PlayerVisibility.itemNameOFF";
		}

		ItemStack vanishItem = new ItemStack(Material.getMaterial(item.toUpperCase()));
		ItemMeta meta = vanishItem.getItemMeta();
		
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString(itemNamePath)));
		meta.addEnchant(Enchantment.DURABILITY, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		vanishItem.setItemMeta(meta);
		
		return vanishItem;
	}

	public void hide(Player player) {
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!p.hasPermission("playervisibility.forceVisibility")) {
				player.hidePlayer(plugin, p);
			}
		}
		
		player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 5, 0);

		player.getInventory().removeItem(makeVanishItem(true));
		player.getInventory().setItemInMainHand(makeVanishItem(false));
		
		sendMessage("playersOFF", player);

		if (isHidingPlayers.get(player) == null) {
			isHidingPlayers.put(player, true);
			isHidingPlayers.put(player, true);
		}
	}

	public void show(Player player, boolean join) {
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			player.showPlayer(plugin, p);
		}
		
		if (!join) {
			player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 5, 0);

			player.getInventory().removeItem(makeVanishItem(false));
			player.getInventory().setItemInMainHand(makeVanishItem(true));
			
			sendMessage("playersON", player);
		}
		
		if (isHidingPlayers(player)) {
			isHidingPlayers.put(player, false);
		}
	}

	@EventHandler
	public void onPlayerClickEvent(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		
		if (player.getInventory().getItemInMainHand().equals(makeVanishItem(true))) {
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
				if (player.hasPermission("playervisibility.item.hide") || player.isOp()) {
					hide(player);
				}
				else
					sendMessage("permissionError", player);
			}
		}
		else if (player.getInventory().getItemInMainHand().equals(makeVanishItem(false))) {
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (player.hasPermission("playervisibility.item.hide") || player.isOp()) {
					show(player, false);
				}
				else
					sendMessage("permissionError", player);
			}
		}
	}

	public boolean isHidingPlayers(Player player) {
		
		if (isHidingPlayers.get(player) != null) {
			if (isHidingPlayers.get(player)) {
				
				return true;
			}
		}
		
		return false;
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		
		Player player = event.getPlayer();
		
		for (String activatedWorlds : getConfig().getStringList("activatedWorlds")) {
			if (activatedWorlds.equals(player.getWorld().getName())) {
				if (isHidingPlayers(player)) {
					hide(player);
				}
			}
		}
	}
}
