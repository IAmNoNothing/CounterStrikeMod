package org.kiuwn.counterstrikemod.MatchMaking;

import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;

public class MatchMap {

    private final String name;
    private HashMap<String, ArrayList<Vec3>> spawnPositions = new HashMap<>();
    private final MatchMode mode;

    public MatchMap(String name, MatchMode mode) {
        this.name = name;
        this.mode = mode;
    }

    public MatchMap(String name, MatchMode mode, HashMap<String, ArrayList<Vec3>> spawnPositions) {
        this.name = name;
        this.mode = mode;
        this.spawnPositions = spawnPositions;
    }

    public String[] getTeams() {
        return spawnPositions.keySet().toArray(new String[0]);
    }

    public String getName() {
        return name;
    }

    public HashMap<String, ArrayList<Vec3>> getSpawnPositions() {
        return spawnPositions;
    }

    public MatchMode getMode() {
        return mode;
    }

    public void addSpawnPosition(String name, Vec3 position) {
        if (!spawnPositions.containsKey(name)) {
            spawnPositions.put(name, new ArrayList<>());
        }
        spawnPositions.get(name).add(position);
    }

    public void removeSpawnPosition(String name, int index) {
        if (spawnPositions.containsKey(name)) {
            spawnPositions.get(name).remove(index);
            if (spawnPositions.get(name).isEmpty()) {
                spawnPositions.remove(name);
            }
        }
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<map name=\"").append(name);
        sb.append("\" mode=\"").append(mode.toString()).append("\">\n");

        for (String name : spawnPositions.keySet()) {
            sb.append("\t<spawn team=\"").append(name).append("\">\n");
            for (Vec3 position : spawnPositions.get(name)) {
                sb.append("\t\t<position x=\"").append(position.x).append("\" y=\"").append(position.y).append("\" z=\"").append(position.z).append("\"/>\n");
            }
            sb.append("\t</spawn>\n");
        }

        sb.append("</map>\n");
        return sb.toString();
    }
}
