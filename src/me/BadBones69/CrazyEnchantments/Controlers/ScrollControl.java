package me.BadBones69.CrazyEnchantments.Controlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.BadBones69.CrazyEnchantments.Main;
import me.BadBones69.CrazyEnchantments.Methods;
import me.BadBones69.CrazyEnchantments.API.CEBook;
import me.BadBones69.CrazyEnchantments.API.CEnchantments;
import me.BadBones69.CrazyEnchantments.API.CrazyEnchantments;
import me.BadBones69.CrazyEnchantments.API.CustomEBook;
import me.BadBones69.CrazyEnchantments.API.CustomEnchantments;
import me.BadBones69.CrazyEnchantments.API.EnchantmentType;

public class ScrollControl implements Listener{
	
	@EventHandler
	public void onScrollUse(InventoryClickEvent e){
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();
		ItemStack item = e.getCurrentItem();
		ItemStack scroll = e.getCursor();
		if(inv != null){
			if(item == null) item = new ItemStack(Material.AIR);
			if(scroll == null) scroll = new ItemStack(Material.AIR);
			if(item.getType() != Material.AIR && scroll.getType() != Material.AIR){
				if(inv.getType() == InventoryType.CRAFTING){
					if(e.getRawSlot() < 9){
						return;
					}
				}
				if(scroll.isSimilar(getTransmogScroll(1))){
					if(player.getGameMode() == GameMode.CREATIVE && scroll.getAmount() > 1){
						player.sendMessage(Methods.getPrefix() + Methods.color("&cPlease unstack the scrolls for them to work."));
						return;
					}
					if(Main.CE.hasEnchantments(item) || Main.CustomE.hasEnchantments(item)){
						if(item.isSimilar(orderEnchantments(item.clone()))){
							return;
						}
						e.setCancelled(true);
						e.setCurrentItem(Methods.addGlow(orderEnchantments(item)));
						player.setItemOnCursor(Methods.removeItem(scroll));
						player.updateInventory();
						return;
					}
				}
				if(scroll.isSimilar(getWhiteScroll(1))){
					if(player.getGameMode() == GameMode.CREATIVE && scroll.getAmount() > 1){
						player.sendMessage(Methods.getPrefix() + Methods.color("&cPlease unstack the scrolls for them to work."));
						return;
					}
					if(!Methods.isProtected(item)){
						ArrayList<Material> types = new ArrayList<Material>();
						types.addAll(EnchantmentType.ALL.getItems());
						if(types.contains(item.getType())){
							e.setCancelled(true);
							e.setCurrentItem(Methods.addLore(item, Main.settings.getConfig().getString("Settings.WhiteScroll.ProtectedName")));
							player.setItemOnCursor(Methods.removeItem(scroll));
							return;
						}
					}
				}
				if(scroll.isSimilar(getBlackScroll(1))){
					if(player.getGameMode() == GameMode.CREATIVE && scroll.getAmount() > 1){
						player.sendMessage(Methods.getPrefix() + Methods.color("&cPlease unstack the scrolls for them to work."));
						return;
					}
					ArrayList<String> customEnchants = new ArrayList<String>();
					HashMap<String, Integer> lvl = new HashMap<String, Integer>();
					ArrayList<CEnchantments> enchants = new ArrayList<CEnchantments>();
					Boolean i = false;
					Boolean custom = false;
					if(Main.CE.hasEnchantments(item)){
						for(CEnchantments en : Main.CE.getEnchantments()){
							if(Main.CE.hasEnchantment(item, en)){
								enchants.add(en);
								lvl.put(en.getName(), Main.CE.getPower(item, en));
								i = true;
							}
						}
					}
					if(Main.CustomE.hasEnchantments(item)){
						for(String en : Main.CustomE.getEnchantments()){
							if(Main.CustomE.hasEnchantment(item, en)){
								customEnchants.add(en);
								lvl.put(en, Main.CustomE.getPower(item, en));
								i = true;
								custom = true;
							}
						}
					}
					if(i){
						e.setCancelled(true);
						player.setItemOnCursor(Methods.removeItem(scroll));
						if(custom){
							String enchantment = pickCustomEnchant(customEnchants);
							e.setCurrentItem(Main.CustomE.removeEnchantment(item, enchantment));
							CustomEBook book = new CustomEBook(enchantment, lvl.get(enchantment), 1);
							player.getInventory().addItem(book.buildBook());
						}else{
							CEnchantments enchantment = pickEnchant(enchants);
							e.setCurrentItem(Main.CE.removeEnchantment(item, enchantment));
							CEBook book = new CEBook(enchantment, lvl.get(enchantment.getName()), 1);
							player.getInventory().addItem(book.buildBook());
						}
						player.updateInventory();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e){
		Player player = e.getPlayer();
		if(e.getItem()!=null){
			ItemStack item = e.getItem();
			if(item.hasItemMeta()){
				if(item.getItemMeta().hasDisplayName()){
					if(item.getItemMeta().getDisplayName().equals(Methods.color(Main.settings.getConfig().getString("Settings.BlackScroll.Name")))){
						player.sendMessage(Methods.getPrefix()+Methods.color(Main.settings.getMsg().getString("Messages.Right-Click-Black-Scroll")));
					}
				}
			}
		}
	}
	
	public static ItemStack orderEnchantments(ItemStack item){
		CrazyEnchantments CE = Main.CE;
		CustomEnchantments customE = Main.CustomE;
		HashMap<String, Integer> enchants = new HashMap<String, Integer>();
		HashMap<String, Integer> categories = new HashMap<String, Integer>();
		List<String> order = new ArrayList<String>();
		ArrayList<String> enchantments = new ArrayList<String>();
		for(CEnchantments en : CE.getItemEnchantments(item)){
			enchantments.add(en.getName());
		}
		for(String en : customE.getItemEnchantments(item)){
			enchantments.add(en);
		}
		for(String ench : enchantments){
			int top = 0;
			if(CE.isEnchantment(ench) || customE.isEnchantment(ench)){
				if(CE.isEnchantment(ench)){
					for(String cat : CE.getEnchantmentCategories(CE.getFromName(ench))){
						if(top < CE.getCategoryRarity(cat)){
							top = CE.getCategoryRarity(cat);
						}
					}
					enchants.put(ench, CE.getPower(item, CE.getFromName(ench)));
					CE.removeEnchantment(item, CE.getFromName(ench));
				}else if(customE.isEnchantment(ench)){
					for(String cat : customE.getEnchantmentCategories(ench)){
						if(top < customE.getCategoryRarity(cat)){
							top = customE.getCategoryRarity(cat);
						}
					}
					enchants.put(ench, customE.getPower(item, ench));
					customE.removeEnchantment(item, ench);
				}
			}
			categories.put(ench, top);
			order.add(ench);
		}
		order = orderInts(order, categories);
		ItemMeta m = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		for(String ench : order){
			if(CE.isEnchantment(ench)){
				CEnchantments en = CE.getFromName(ench);
				lore.add(en.getEnchantmentColor() + en.getCustomName() + " " + CE.convertPower(enchants.get(ench)));
			}else if(customE.isEnchantment(ench)){
				lore.add(customE.getEnchantmentColor(ench) + customE.getCustomName(ench) + " " + customE.convertPower(enchants.get(ench)));
			}
		}
		if(m.hasLore()){
			for(String l : m.getLore()){
				lore.add(l);
			}
		}
		m.setLore(lore);
		String name = item.getType().toString();
		String enchs = Main.settings.getConfig().getString("Settings.TransmogScroll.Amount-of-Enchantments");
		if(m.hasDisplayName()){
			name = m.getDisplayName();
			for(int i = 0; i <= 100; i++){
				if(m.getDisplayName().endsWith(Methods.color(enchs.replaceAll("%Amount%", i + "").replaceAll("%amount%", i + "")))){
					name = m.getDisplayName().substring(0, m.getDisplayName().length() - (enchs.replaceAll("%Amount%", i + "").replaceAll("%amount%", i + "")).length());
				}
			}
		}
		int amount = order.size();
		amount += item.getEnchantments().size();
		m.setDisplayName(name + Methods.color(enchs.replaceAll("%Amount%", amount + "").replaceAll("%amount%", amount + "")));
		item.setItemMeta(m);
		return item;
	}
	
	public static List<String> orderInts(List<String> list, final Map<String, Integer> map){
	    Collections.sort(list, new Comparator<String>() {
	        @Override
	        public int compare(String a1, String a2) {
	        	Integer string1 = map.get(a1);
	        	Integer string2 = map.get(a2);
	            return string2.compareTo(string1);
	        }
	    });
	    return list;
	}
	
	public static ItemStack getBlackScroll(int amount){
		String name = Methods.color(Main.settings.getConfig().getString("Settings.BlackScroll.Name"));
		String id = Main.settings.getConfig().getString("Settings.BlackScroll.Item");
		return Methods.makeItem(id, amount, name, Main.settings.getConfig().getStringList("Settings.BlackScroll.Item-Lore"));
	}
	
	public static ItemStack getWhiteScroll(int amount){
		String name = Methods.color(Main.settings.getConfig().getString("Settings.WhiteScroll.Name"));
		String id = Main.settings.getConfig().getString("Settings.WhiteScroll.Item");
		return Methods.makeItem(id, amount, name, Main.settings.getConfig().getStringList("Settings.WhiteScroll.Item-Lore"));
	}
	
	public static ItemStack getTransmogScroll(int amount){
		String name = Methods.color(Main.settings.getConfig().getString("Settings.TransmogScroll.Name"));
		String id = Main.settings.getConfig().getString("Settings.TransmogScroll.Item");
		return Methods.makeItem(id, amount, name, Main.settings.getConfig().getStringList("Settings.TransmogScroll.Item-Lore"));
	}
	
	private CEnchantments pickEnchant(List<CEnchantments> enchants){
		Random i = new Random();
		return enchants.get(i.nextInt(enchants.size()));
	}
	
	private String pickCustomEnchant(List<String> enchants){
		Random i = new Random();
		return enchants.get(i.nextInt(enchants.size()));
	}
}