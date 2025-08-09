package de.hallo5000.listener;

import de.hallo5000.main.Main;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Set;
import java.util.stream.Collectors;

public class handleGuests implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        User u = Main.lp.getUserManager().getUser(p.getUniqueId());
        if(!u.getPrimaryGroup().equals("default")) return;
        p.setGameMode(GameMode.ADVENTURE);
        p.setSleepingIgnored(true);
        ItemStack i = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta book = (BookMeta) i.getItemMeta();
        book.setAuthor("hallo5000 - SAW");
        book.setTitle("§6§lWelcome - Willkommen");
        book.setGeneration(BookMeta.Generation.ORIGINAL);
        book.addPages(
                Component.text("Willkommen auf dem SAW Minecraft Server").appendNewline().appendNewline()
                    .append(Component.text("Auf dieser Seite folgen einige grundlegende Informationen um auf diesem Server zu navigieren: WIP"))
                    .append(Component.text("Commands: "))
                    .append(Component.text("/authenticate"))
                    .append(Component.text("/friend")),
                Component.text("Welcome").appendNewline().append(Component.text("WIP"))
        );
        i.setItemMeta(book);
        p.openBook(i);
    }

    public static void onNodeMutate(NodeMutateEvent e){
        if(e.isGroup()) return;
        User u = (User) e.getTarget();
        Player p = Main.getPlugin(Main.class).getServer().getPlayer(u.getUniqueId());
        if(p == null) return;
        Set<String> groupsBefore = e.getDataBefore().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());
        Set<String> groupsAfter = e.getDataAfter().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());
        if(groupsAfter.equals(groupsBefore)) return;
        if(groupsBefore.contains("default") && groupsBefore.size() == 1 && groupsAfter.size() > groupsBefore.size()){
            //promote from default while keeping default
            Main.getPlugin(Main.class).getServer().getScheduler().runTask(Main.getPlugin(Main.class), ()->{
                p.setGameMode(GameMode.SURVIVAL);
                p.setSleepingIgnored(false);
            });
        }else if(groupsBefore.size() > 1 && groupsAfter.size() == 1 && groupsAfter.contains("default")){
            //had multiple groups and removed everything except default || demote
            u.setPrimaryGroup("default");
            Main.getPlugin(Main.class).getServer().getScheduler().runTask(Main.getPlugin(Main.class), ()->{
                Main.getPlugin(Main.class).getServer().getPluginManager().callEvent(new PlayerJoinEvent(p, Component.text("")));
            });
        }else if(groupsBefore.contains("default") && groupsBefore.size() == 1 && !groupsAfter.contains("default") && !groupsAfter.isEmpty()){
            //promote from default while removing default
            Main.getPlugin(Main.class).getServer().getScheduler().runTask(Main.getPlugin(Main.class), ()->{
                p.setGameMode(GameMode.SURVIVAL);
                p.setSleepingIgnored(false);
            });
        }else if(groupsBefore.size() == 1 && !groupsBefore.contains("default") && groupsAfter.contains("default")){
            //had 1 group != default and replaced it with default || demote
            u.setPrimaryGroup("default");
            Main.getPlugin(Main.class).getServer().getScheduler().runTask(Main.getPlugin(Main.class), ()->{
                Main.getPlugin(Main.class).getServer().getPluginManager().callEvent(new PlayerJoinEvent(p, Component.text("")));
            });
        }
        else if(groupsBefore.isEmpty() && groupsAfter.contains("default")) {
            //demoting from unknown/'nothing' to default
            u.setPrimaryGroup("default");
            Main.getPlugin(Main.class).getServer().getScheduler().runTask(Main.getPlugin(Main.class), () -> {
                Main.getPlugin(Main.class).getServer().getPluginManager().callEvent(new PlayerJoinEvent(p, Component.text("")));
            });
        }else if(groupsBefore.isEmpty() && !groupsAfter.isEmpty() && !groupsAfter.contains("default")){
            //promoting from unknown/'nothing' higher than default
            Main.getPlugin(Main.class).getServer().getScheduler().runTask(Main.getPlugin(Main.class), ()->{
                p.setGameMode(GameMode.SURVIVAL);
                p.setSleepingIgnored(false);
            });
        }
        //ignoring one case where groupsAfter is Empty because this would also be triggered in the promotion/demotion process
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if(Main.lp.getUserManager().getUser(e.getPlayer().getUniqueId()).getPrimaryGroup().equals("default")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent e){
        if(e.getEntity() instanceof Player p){
            if(Main.lp.getUserManager().getUser(p.getUniqueId()).getPrimaryGroup().equals("default")){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPrePlayerAttackEntity(PrePlayerAttackEntityEvent e){
        if(Main.lp.getUserManager().getUser(e.getPlayer().getUniqueId()).getPrimaryGroup().equals("default")){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player p){
            if(Main.lp.getUserManager().getUser(p.getUniqueId()).getPrimaryGroup().equals("default")){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent e){
        if(e.getEntity() instanceof Player p) {
            if (Main.lp.getUserManager().getUser(p.getUniqueId()).getPrimaryGroup().equals("default")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityMount(EntityMountEvent e){
        if(e.getEntity() instanceof Player p){
            if(Main.lp.getUserManager().getUser(p.getUniqueId()).getPrimaryGroup().equals("default")){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent e){
        if(Main.lp.getUserManager().getUser(e.getPlayer().getUniqueId()).getPrimaryGroup().equals("default")){
            e.setCancelled(true);
        }
    }
}
