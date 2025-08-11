package de.hallo5000.commands;

import de.hallo5000.main.Main;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class unfriendCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(sender.hasPermission("saw.unfriend")) {
            if (args.length == 1) {
                Player p = Main.getPlugin(Main.class).getServer().getPlayer(args[0]);
                if (p != null) {
                    if(p.hasPermission("group.admin") || p.hasPermission("group.resident")){
                        sender.sendMessage("§cYou can't 'unfriend' admins or residents!");
                        return true;
                    }
                    if(!p.hasPermission("group.friend")){
                        sender.sendMessage("§cYou can't remove someone who isn't a friend from the server!");
                        return true;
                    }
                    User u = Main.lp.getUserManager().getUser(p.getUniqueId());
                    Main.lp.getTrackManager().getTrack("base-track").demote(u, ImmutableContextSet.empty());
                    Main.lp.getUserManager().saveUser(u);
                    sender.sendMessage("§aYou've succesfully §cremoved §f" + p.getName() + " §aas friend from the server!");
                    if(sender instanceof Player) Main.logCommand(command, sender, ((Player) sender).getUniqueId().toString(), p.getName());
                    else Main.logCommand(command, sender, "NOT-A-PLAYER", p.getName());
                }else{
                    sender.sendMessage("§cThe player was not found!");
                }
            }else return false;
        }else{
            sender.sendMessage("§cYou do not have permission to use this command!");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1){
            return Main.getPlugin(Main.class).getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return Collections.emptyList();
    }

}
