package org.kiuwn.counterstrikemod.MatchMaking;

import net.minecraft.world.phys.Vec3;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MapManager {
    private final File file;
    private final HashMap<String, MatchMap> maps = new HashMap<>();
    private static MapManager instance;

    public static MapManager getInstance() {
        return instance;
    }

    public MapManager(File file) {
        this.file = file;
        instance = this;
        load();
    }

    public HashMap<String, MatchMap> getMaps() {
        return maps;
    }

    public void addMap(String name, MatchMode mode) {
        if (!maps.containsKey(name)) {
            maps.put(name, new MatchMap(name, mode));
            save();
        }
    }

    public void removeMap(String name) {
        maps.remove(name);
        save();
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<maps>\n");
        for (MatchMap map : maps.values()) {
            sb.append("\t").append(map.toXML()).append("\n");
        }
        sb.append("</maps>\n");
        return sb.toString();
    }

    public void save() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(toXML());
            writer.close();
            System.out.println("MapManager saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("MapManager failed to save to " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    public void load() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("map");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element map = (Element) node;
                    String name = map.getAttribute("name");
                    MatchMode mode = MatchMode.valueOf(map.getAttribute("mode"));
                    NodeList spawnNode = map.getElementsByTagName("spawn");

                    HashMap<String, ArrayList<Vec3>> spawnPositions = new HashMap<>();

                    for (int j = 0; j < spawnNode.getLength(); j++) {
                        Element spawn = (Element) spawnNode.item(j);
                        String teamName = spawn.getAttribute("team");
                        NodeList positionNode = spawn.getElementsByTagName("position");

                        ArrayList<Vec3> positions = new ArrayList<>();

                        for (int k = 0; k < positionNode.getLength(); k++) {
                            Element position = (Element) positionNode.item(k);
                            double x = Double.parseDouble(position.getAttribute("x"));
                            double y = Double.parseDouble(position.getAttribute("y"));
                            double z = Double.parseDouble(position.getAttribute("z"));
                            positions.add(new Vec3(x, y, z));
                        }

                        spawnPositions.put(teamName, positions);
                    }

                    MatchMap matchMap = new MatchMap(name, mode, spawnPositions);
                    maps.put(name, matchMap);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
