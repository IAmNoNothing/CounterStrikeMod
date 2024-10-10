package org.kiuwn.counterstrikemod.MatchMaking;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.kiuwn.counterstrikemod.Counterstrikemod;
import org.kiuwn.counterstrikemod.Gameplay.Shop;
import org.kiuwn.counterstrikemod.Gameplay.ShopItem;
import oshi.util.tuples.Pair;

import java.util.*;

public class Match {
    private final MatchMode mode;
    private final MatchMap map;
    private final ArrayList<Player> players = new ArrayList<>();
    private boolean started = false;
    private int teamIndex = 0;
    private final HashMap<String, ArrayList<Vec3>> spawnPositions;
    private ArrayList<Vec3> allSpawnPositions = new ArrayList<>();
    private final HashMap<Player, Integer> playerBalances = new HashMap<>();
    private int startBalance = 1500;
    private HashMap<String, Integer> scores = new HashMap<>();
    private final HashMap<String, Pair<String, Player>> teams = new HashMap<>();
    private final int maxScore = 50;
    private final Shop shop = new Shop();
    private final int moneyForKill = 250;
    private Objective scoreObjective;
    private Scoreboard scoreboard;

    public int getStartBalance() {
        return startBalance;
    }

    public HashMap<String, Integer> getScores() {
        return scores;
    }

    public void setScores(HashMap<String, Integer> scores) {
        this.scores = scores;
    }

    public void setScoreFor(String team, int score) {
        scores.put(team, score);
    }

    public void setStartBalance(int startBalance) {
        this.startBalance = startBalance;
    }

    private void addItemsToShop() {
        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:ak47");
            itemStack.getOrCreateTag().putString("GunFireMode", "AUTO");
            shop.addItem(new ShopItem("AK 47", "42", 1200, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:m4a1");
            itemStack.getOrCreateTag().putString("GunFireMode", "AUTO");
            shop.addItem(new ShopItem("M4A1", "41", 1200, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:mk14");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("MK14 EBR", "43", 1400, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:scar_l");
            itemStack.getOrCreateTag().putString("GunFireMode", "AUTO");
            shop.addItem(new ShopItem("SCAR-L", "44", 1300, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:aug");
            itemStack.getOrCreateTag().putString("GunFireMode", "AUTO");
            shop.addItem(new ShopItem("AUH", "45", 1300, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:sks_tactical");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("SKS", "46", 1400, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:m95");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("M95", "11", 1000, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:ai_awp");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("AVM", "12", 2500, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:m249");
            itemStack.getOrCreateTag().putString("GunFireMode", "AUTO");
            shop.addItem(new ShopItem("M249", "21", 3500, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:rpk");
            itemStack.getOrCreateTag().putString("GunFireMode", "AUTO");
            shop.addItem(new ShopItem("RPK", "22", 3000, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:deagle");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("Deagle", "31", 1000, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:cz75");
            itemStack.getOrCreateTag().putString("GunFireMode", "AUTO");
            shop.addItem(new ShopItem("CZ 75", "32", 700, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:glock_17");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("Glock 17", "33", 500, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:p320");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("P320", "34", 400, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            ItemStack itemStack = new ItemStack(item, 1);
            itemStack.getOrCreateTag().putString("GunId", "tacz:deagle_golden");
            itemStack.getOrCreateTag().putString("GunFireMode", "SEMI");
            shop.addItem(new ShopItem("Golden Deagle", "35", 1500, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 30);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:556x45");
            shop.addItem(new ShopItem("5.56x45", "556", 200, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 30);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:762x39");
            shop.addItem(new ShopItem("7.62x39", "762", 200, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 6);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:50bmg");
            shop.addItem(new ShopItem(".50 BMG", "50bmg", 500, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 6);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:338");
            shop.addItem(new ShopItem(".338 Lapua Bullet", "338", 500, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 21);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:308");
            shop.addItem(new ShopItem(".308 Winchester", "308", 350, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 8);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:50ae");
            shop.addItem(new ShopItem(".50 AE", "50ae", 200, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 8);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:9mm");
            shop.addItem(new ShopItem("9 MM", "9mm", 100, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 8);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:45acp");
            shop.addItem(new ShopItem(".45 ACP", "45acp", 100, itemStack));
        }

        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:ammo"));
            ItemStack itemStack = new ItemStack(item, 8);
            itemStack.getOrCreateTag().putString("AmmoId", "tacz:357mag");
            shop.addItem(new ShopItem(".357 Magnum", "357mag", 600, itemStack));
        }
    }

    public Match(MatchMap map) {
        this.mode = map.getMode();
        this.map = map;

        this.spawnPositions = new HashMap<>();
        for (Map.Entry<String, ArrayList<Vec3>> entry : map.getSpawnPositions().entrySet()) {
            this.spawnPositions.put(entry.getKey(), copyList(entry.getValue()));
        }

        allSpawnPositions = getAllSpawnPositions();
        addItemsToShop();
    }

    private ArrayList<Vec3> getAllSpawnPositions() {
        ArrayList<Vec3> positions = new ArrayList<>();
        for (String team : map.getTeams()) {
            positions.addAll(copyList(spawnPositions.get(team)));
        }
        return positions;
    }

    public void join(Player player) {
        players.add(player);
        playerBalances.put(player, startBalance);
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        started = true;

        List<ServerPlayer> players_on_server = Counterstrikemod.getInstance().getServer().getPlayerList().getPlayers();

        for (ServerPlayer player : players_on_server) {
            join(player);
        }

        spawnPlayers();
        setupScoreBoard();
    }

    private void setupScoreBoard() {

        Level world = players.get(0).getLevel();
        scoreboard = world.getScoreboard();
        scoreObjective = scoreboard.getObjective("score");
        if (scoreObjective == null) {
            scoreObjective = scoreboard.addObjective(
                "score",
                ObjectiveCriteria.DUMMY,
                Component.literal("Score"),
                ObjectiveCriteria.RenderType.INTEGER
            );
        }

        scoreboard.setDisplayObjective(Scoreboard.DISPLAY_SLOT_LIST, scoreObjective);
        scoreboard.setDisplayObjective(Scoreboard.DISPLAY_SLOT_SIDEBAR, scoreObjective);

        if (mode.equals(MatchMode.ALL_VS_ALL)) {
            for (Player player : players) {
                String playerName = player.getScoreboardName();
                Score score = scoreboard.getOrCreatePlayerScore(playerName, scoreObjective);
                score.setScore(0);
            }
        } else {
            for (String teamName : map.getTeams()) {
                Score score = scoreboard.getOrCreatePlayerScore(teamName, scoreObjective);
                score.setScore(0);
            }
        }
    }

    private String nextTeam() {
        teamIndex %= map.getTeams().length;
        return map.getTeams()[teamIndex++];
    }

    private ArrayList<Vec3> copyList(ArrayList<Vec3> list) {
        ArrayList<Vec3> copy = new ArrayList<>();
        for (Vec3 vec3 : list) {
            copy.add(new Vec3(vec3.x, vec3.y, vec3.z));
        }
        return copy;
    }

    private Vec3 nextSpawnPosition(String team) {
        if (spawnPositions.get(team).isEmpty()) {
            spawnPositions.put(team, copyList(map.getSpawnPositions().get(team)));
        }

        Random random = new Random();
        int index = random.nextInt(spawnPositions.get(team).size());
        Vec3 position = spawnPositions.get(team).get(index);
        spawnPositions.get(team).remove(index);
        return position;
    }

    private void spawnPlayer(Player player, boolean newTeam) {
        Vec3 position;
        if (mode != MatchMode.ALL_VS_ALL) {
            String team;
            if (newTeam) {
                team = nextTeam();
                player.sendSystemMessage(Component.literal("You are in team " + team));
                teams.put(team, new Pair<>(team, player));
            } else {
                team = teams.get(player.getName().getString()).getA();
            }

            scores.putIfAbsent(team, 0);
            position = nextSpawnPosition(team);
        } else {
            String team = player.getName().getString();
            scores.putIfAbsent(team, 0);
            if (newTeam) teams.put(team, new Pair<>(team, player));
            position = nextAloneSpawnPosition();
        }
        player.teleportTo(position.x, position.y, position.z);
    }

    private Vec3 nextAloneSpawnPosition() {
        if (allSpawnPositions.isEmpty()) {
            allSpawnPositions = getAllSpawnPositions();
        }

        Random random = new Random();
        int index = random.nextInt(allSpawnPositions.size());
        Vec3 position = allSpawnPositions.get(index);
        allSpawnPositions.remove(index);
        return position;
    }

    private void spawnPlayers() {
        for (Player player : players) {
            spawnPlayer(player, true);
            player.setHealth(player.getMaxHealth());
            player.getInventory().clearContent();
        }
    }

    public void stop() {
        started = false;
        players.clear();
        playerBalances.clear();
        teams.clear();
        scoreboard.removeObjective(scoreObjective);
    }

    public void setStarted(boolean started) { this.started = started; }

    public MatchMap getMap() {
        return map;
    }

    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player dead) {
            DamageSource source = event.getSource();
            if (source.getEntity() instanceof Player killer) {
                String killerTeam = teams.get(killer.getName().getString()).getA();
                int newScore;
                if (killer.equals(dead)) {
                    newScore = scores.get(killerTeam) - 1;
                    if (newScore < 0) newScore = 0;
                } else {
                    newScore = scores.get(killerTeam) + 1;
                }
                scores.put(killerTeam, newScore);
                playerBalances.put(killer, playerBalances.get(killer) + moneyForKill);

                if (mode.equals(MatchMode.ALL_VS_ALL)) {
                    scoreboard.getOrCreatePlayerScore(killer.getScoreboardName(), scoreObjective).setScore(newScore);
                } else {
                    scoreboard.getOrCreatePlayerScore(killerTeam, scoreObjective).setScore(newScore);
                }

                killer.sendSystemMessage(Component.literal("+1 Score"));
                sendActionBarMoneyCount(killer, "Money: %d$ + " + moneyForKill + "$");
                checkForWin();
            }
        }
    }

    private void checkForWin() {
        // iterate over all player teams and check if score > maxScore
        for (Map.Entry<String, Pair<String, Player>> entry : teams.entrySet()) {
            String team = entry.getKey();
            if (scores.get(team) >= maxScore) {
                Counterstrikemod.getInstance().getServer().getPlayerList().broadcastAll((Packet<?>) Component.literal("Team " + team + " won!"));
                for (Player player : players) {
                    player.getInventory().clearContent();
                }
                stop();
            }
        }
    }

    public void buy(String path, Player player) {
        if (shop.getItem(path) != null) {
            ShopItem shopItem = shop.getItem(path);

            if (shopItem.price() > playerBalances.get(player)) {
                player.sendSystemMessage(Component.literal("Not enough money to buy this item(Price: %d, Needed: %d)".formatted(shopItem.price(), playerBalances.get(player))));
                return;
            }

            player.getInventory().add(shopItem.item().copy());
            playerBalances.put(player, playerBalances.get(player) - shopItem.price());
            player.sendSystemMessage(Component.literal("Bought %s".formatted(shopItem.name())));
        }
    }

    public Shop getShop() {
        return shop;
    }

    public void sendActionBarMoneyCount(Player player, String fmt) {
        String moneyMsg = fmt.formatted(playerBalances.get(player));
        Component message = Component.literal(moneyMsg);
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        Objects.requireNonNull(Counterstrikemod.getInstance().getServer().getPlayerList().getPlayerByName(player.getName().getString())).connection.send(packet);
    }

    public void setMoney(String playerName, int amount, Player sender) {
        MinecraftServer server = Counterstrikemod.getInstance().getServer();
        Player player = server.getPlayerList().getPlayerByName(playerName);

        if (player != null) {
            playerBalances.put(player, amount);
            sender.sendSystemMessage(Component.literal("Balance set to " + amount + " for " + playerName));
            sendActionBarMoneyCount(player, "Money set to %d$");
            return;
        }

        sender.sendSystemMessage(Component.literal("Player " + playerName + " not found!"));
    }

    public void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        Vec3 position;
        if (mode == MatchMode.ALL_VS_ALL) {
            position = nextAloneSpawnPosition();
        } else {
            position = nextSpawnPosition(teams.get(player.getName().getString()).getA());
        }
        player.teleportTo(position.x, position.y, position.z);
        if (playerBalances.get(player) < startBalance) {
            playerBalances.put(player, startBalance);
        }
        sendActionBarMoneyCount(player, "Money: %d$");
    }
}
