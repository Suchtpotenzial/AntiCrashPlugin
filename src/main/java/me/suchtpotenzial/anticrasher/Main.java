package me.suchtpotenzial.anticrasher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import me.suchtpotenzial.anticrasher.prefix.AntiPrefix;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.minecraft.server.v1_8_R3.PacketPlayOutExplosion;
import net.minecraft.server.v1_8_R3.Vec3D;

public class Main extends JavaPlugin implements Listener {

    // ------------------------------------------------------------------------------------------------------------------------------------------//

    public static Plugin plugin;
    private ProtocolManager protocolManager;
    private boolean usecmd = true;
    private int delay = 1;
    private ArrayList<Player> cooldown = new ArrayList<>();

    // ------------------------------------------------------------------------------------------------------------------------------------------//

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.broadcastMessage(Bukkit.getServer().getIp());
        getConfig().addDefault("usepunish", "true");
        getConfig().addDefault("delay", "1");
        getConfig().options().copyDefaults(true);
        saveConfig();

        Bukkit.getConsoleSender().sendMessage(AntiPrefix.prefix + "- AntiCrash gestartet!");
        Bukkit.getPluginManager().registerEvents(this, this);

        // ------------------------------------------------------------------------------------------------------------------------------------------//
        // AntiServerCrasher
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.delay = getConfig().getInt("delay");
        this.usecmd = Boolean.parseBoolean(getConfig().getString("usepunish"));
        this.protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, new PacketType[] { PacketType.Play.Client.POSITION }) {
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.getPacketType().equals(PacketType.Play.Client.POSITION)) {
                            try {
                                int before = (int) event.getPlayer().getLocation().getY();
                                int next = ((Double) event.getPacket().getDoubles().read(1)).intValue();
                                if (before + 200 < next) {
                                    event.setCancelled(true);
                                }
                            } catch (Exception localException) {
                            }
                        }
                    }
                });
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
                new PacketType[] { PacketType.Play.Client.CUSTOM_PAYLOAD }) {
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType().equals(PacketType.Play.Client.CUSTOM_PAYLOAD)) {
                    String x = (String) event.getPacket().getStrings().read(0);
                    if ((x.equals("MC|BSign")) || (x.equals("MC|BEdit"))) {
                        if (event.getPlayer().getItemInHand().getType() == Material.BOOK_AND_QUILL) {
                            return;
                        }
                        event.setCancelled(true);
                        if (Main.this.usecmd == true) {
                            Main.this.punish(event.getPlayer());
                        }
                    }
                }
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------//

    private ArrayList<Player> crasher = new ArrayList<>();

    // AntiCrash
    private void punish(final Player player) {
        if (!this.cooldown.contains(player)) {
            this.cooldown.add(player);
            if (!crasher.contains(player)) {
                Bukkit.broadcastMessage(AntiPrefix.prefix + "Der liebe " + player.getName()+ " wollte den Server crashen und wurde jetzt vom schlauen Anticrasher gecrashed!");
                crasher.add(player);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @SuppressWarnings("unchecked")
                public void run() {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutExplosion(
                            Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Float.MAX_VALUE,
                            Collections.EMPTY_LIST, new Vec3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)));

                    Main.this.removeCooldown(player);
                }
            }, 0L);

        }

    }

    private void removeCooldown(final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                Main.this.cooldown.remove(player);
            }
        }, this.delay * 20);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------//

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(AntiPrefix.prefix + "- AntiCrash gestoppt!");

    }

    // ------------------------------------------------------------------------------------------------------------------------------------------//

   //private void registerListener() {
   //    try {
   //        for (ClassPath.ClassInfo classInfo : ClassPath.from(getClassLoader())
   //                .getTopLevelClasses("de.einfachleax.generalsystem.listeners")) {
   //            Class<?> currentClass = Class.forName(classInfo.getName());

   //            if (Listener.class.isAssignableFrom(currentClass)) {
   //                Bukkit.getPluginManager().registerEvents((Listener) currentClass.newInstance(), this);
   //            }
   //        }
   //    } catch (Exception exception) {
   //        exception.printStackTrace();
   //    }
   //}
    // ------------------------------------------------------------------------------------------------------------------------------------------//

    @EventHandler
    public void onMount(final VehicleEnterEvent event) {
        if ((event.getEntered() instanceof Player)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    if ((event.getVehicle().isValid()) && (event.getEntered().isValid())) {
                        ProtocolLibrary.getProtocolManager().updateEntity(event.getVehicle(),
                                Arrays.asList(new Player[] { (Player) event.getEntered() }));
                    }
                }
            });
        }
    }
    // ------------------------------------------------------------------------------------------------------------------------------------------//
}
