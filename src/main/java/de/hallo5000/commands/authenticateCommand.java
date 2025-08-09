package de.hallo5000.commands;

import de.hallo5000.main.Main;
import net.luckperms.api.context.Context;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class authenticateCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!sender.hasPermission("saw.authenticate.self")){
            sender.sendMessage("§cYou do not have permission to use this command!");
            return true;
        }
        if(args.length == 0){ //authenticate self
            if(sender instanceof Player p){ // get IP address in format "127.0.0.1": p.getAddress().getAddress().getHostAddress()
                try {
                    if(!Main.isIPinRange(p.getAddress().getAddress(), InetAddress.getByName("10.0.0.0"), InetAddress.getByName("10.255.255.255"))
                            && !Main.isIPinRange(p.getAddress().getAddress(), InetAddress.getByName("172.16.0.0"), InetAddress.getByName("172.31.255.255"))
                            && !Main.isIPinRange(p.getAddress().getAddress(), InetAddress.getByName("192.168.0.0"), InetAddress.getByName("192.168.255.255"))){
                        sender.sendMessage("§cAccording to your ip address you are not a member of the dorm!");
                        return true;
                    }
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                User u = Main.lp.getUserManager().getUser(p.getUniqueId());
                if(u.getPrimaryGroup().equals("admin")) {
                    p.sendMessage("§cYou already are an admin!");
                    return true;
                }
                else if(u.getPrimaryGroup().equals("resident")){
                    p.sendMessage("§cYou already are a resident!");
                    return true;
                }
                if(u.getPrimaryGroup().equals("default")){
                    Main.lp.getTrackManager().getTrack("base-track").promote(u, ImmutableContextSet.empty());
                    Main.lp.getTrackManager().getTrack("base-track").promote(u, ImmutableContextSet.empty());
                    Main.lp.getUserManager().saveUser(u);
                }else if(u.getPrimaryGroup().equals("friend")) Main.lp.getTrackManager().getTrack("base-track").promote(u, ImmutableContextSet.empty());
                Main.lp.getUserManager().saveUser(u);
            }else{
                sender.sendMessage("§cOnly players can authenticate themselves!");
            }
            return true;
        }
        if(sender.hasPermission("saw.authenticate.others")){
            if(args.length == 1 || args.length == 2){ //authenticate others
                Player other = Main.getPlugin(Main.class).getServer().getPlayer(args[0]);
                if(other == null){
                    sender.sendMessage("§cNo player found by this name!");
                    return true;
                }
                boolean bypass = false;
                if(args.length == 2){ //bypass ip check
                    if(args[1].equals("true")) bypass = true;
                    else if(!args[1].equals("false")){
                        sender.sendMessage("§cThe bypass-flag can only be set to 'true' or 'false'!");
                        return true;
                    }
                }
                if(!bypass){
                    if(other.getAddress() == null || other.getAddress().getAddress() == null){
                        sender.sendMessage("§cPlayer's IP address could not be found!");
                    }
                    try {
                        if(!Main.isIPinRange(other.getAddress().getAddress(), InetAddress.getByName("10.0.0.0"), InetAddress.getByName("10.255.255.255"))
                        && !Main.isIPinRange(other.getAddress().getAddress(), InetAddress.getByName("172.16.0.0"), InetAddress.getByName("172.31.255.255"))
                        && !Main.isIPinRange(other.getAddress().getAddress(), InetAddress.getByName("192.168.0.0"), InetAddress.getByName("192.168.255.255"))){
                            sender.sendMessage("§cAccording to their ip address the given player is not a member of the dorm!");
                            return true;
                        }
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                }
                User u = Main.lp.getUserManager().getUser(other.getUniqueId());
                if(u.getPrimaryGroup().equals("admin")) {
                    sender.sendMessage("§cThis player already is an admin!");
                    return true;
                }
                else if(u.getPrimaryGroup().equals("resident")){
                    sender.sendMessage("§cThis player already is a resident!");
                    return true;
                }
                if(u.getPrimaryGroup().equals("default")){
                    Main.lp.getTrackManager().getTrack("base-track").promote(u, ImmutableContextSet.empty());
                    Main.lp.getTrackManager().getTrack("base-track").promote(u, ImmutableContextSet.empty());
                    Main.lp.getUserManager().saveUser(u);
                }else if(u.getPrimaryGroup().equals("friend")) Main.lp.getTrackManager().getTrack("base-track").promote(u, ImmutableContextSet.empty());
                Main.lp.getUserManager().saveUser(u);
            }else{
                sender.sendMessage("Usage: /authenticate [others]");
            }
        }else{
            sender.sendMessage("Usage: /authenticate");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(sender.hasPermission("saw.authenticate.others") && args.length == 1){
            return Main.getPlugin(Main.class).getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return Collections.emptyList();
    }

}
