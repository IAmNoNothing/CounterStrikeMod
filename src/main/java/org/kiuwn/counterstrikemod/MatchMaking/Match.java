package org.kiuwn.counterstrikemod.MatchMaking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.kiuwn.counterstrikemod.Counterstrikemod;

import java.util.*;

public class Match {
    private final MatchMode mode;
    private final MatchMap map;
    private final ArrayList<Player> players = new ArrayList<>();
    private boolean started = false;
    private int teamIndex = 0;
    private final HashMap<String, ArrayList<Vec3>> spawnPositions;
    private ArrayList<Vec3> allSpawnPositions = new ArrayList<>();
    private int startBalance = 800;
    private HashMap<String, Integer> scores = new HashMap<>();

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

    public Match(MatchMap map) {
        this.mode = map.getMode();
        this.map = map;

        this.spawnPositions = new HashMap<>();
        for (Map.Entry<String, ArrayList<Vec3>> entry : map.getSpawnPositions().entrySet()) {
            this.spawnPositions.put(entry.getKey(), copyList(entry.getValue()));
        }

        allSpawnPositions = getAllSpawnPositions();
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

    private void spawnPlayer(Player player) {
        Vec3 position;
        if (mode != MatchMode.ALL_VS_ALL) {
            position = nextSpawnPosition(nextTeam());
        } else {
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
            spawnPlayer(player);
        }
    }

    public void stop() {
        started = false;
        players.clear();
    }

    public void setStarted(boolean started) { this.started = started; }

    public MatchMap getMap() {
        return map;
    }

    public void onLivingDeath(LivingDeathEvent event) {
        Counterstrikemod.getLOGGER().debug("player died");
        Counterstrikemod.getLOGGER().debug(event.toString());
        if (event.getEntity() instanceof Player player) {
            DamageSource source = event.getSource();
            player.sendSystemMessage(Component.literal(source.toString()));
            player.sendSystemMessage(Component.literal("Is projectile " + source.isProjectile()));
            player.sendSystemMessage(Component.literal("Is magic " + source.isMagic()));
            player.sendSystemMessage(Component.literal("Killer: " + source.getDirectEntity()));
            player.sendSystemMessage(Component.literal(event.toString()));
        }
    }
}
