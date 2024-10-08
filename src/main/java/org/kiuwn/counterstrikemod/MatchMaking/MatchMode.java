package org.kiuwn.counterstrikemod.MatchMaking;

public enum MatchMode {
    DEATH_MATCH("Death Match"), DOMINATION("Domination"), ALL_VS_ALL("All vs All"), BOMB("Bomb");

    private final String name;

    MatchMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
