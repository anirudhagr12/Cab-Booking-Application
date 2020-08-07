package com.server;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

class Edge{
    final private Node destination;
//    final private double dist;
    final private double time;

    /**
     * Creates a new edge
     * @param destination
     * @param dist
     */
    Edge(Node destination, double dist){
        this.destination = destination;
//        this.dist = dist;
        time = dist/10;
    }

    /**
     * @return time
     */
    double getTime(){
        return time;
    }

    /**
     * @return destination
     */
    Node getDestination(){
        return destination;
    }
}

class Node{
    final private int sector;
    final private int x;
    final private int y;
    final private char block;
    final ArrayList<Edge> edges = new ArrayList<>();

    /**
     * Creates a new node
     * @param sector
     * @param block
     * @param x
     * @param y
     */
    Node(int sector, char block, int x, int y){
        this.sector = sector;
        this.block = block;
        this.x = x;
        this.y = y;
        System.out.println("Node Created: " + sector + " " + block + " at (" + x + ", " + y + ")");
    }

    /**
     * @return sector
     */
    int getSector(){
        return sector;
    }

    /**
     * @return x
     */
    int getX(){
        return x;
    }

    /**
     * @return y
     */
    int getY(){
        return y;
    }

    /**
     * @return block
     */
    char getBlock(){
        return block;
    }

    /**
     * checks if two nodes are same
     * @param node
     * @return true if this.sector == node.sector && this.block == node.block, otherwise false
     * @overrides
     */
    public boolean equals(Node node) {
        return (this.sector == node.sector && this.block == node.block);
    }

}

public class Map{
    private static Map map = null;
    final ArrayList<Node> nodes = new ArrayList<Node>();

    private Map() {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("", "", "");
            Statement st = conn.createStatement();

            ResultSet nodesSet = st.executeQuery("SELECT * FROM nodes;");
            while (nodesSet.next()) {
                int sector = nodesSet.getInt("sector");
                char block = (nodesSet.getString("block")).charAt(0);
                int x = nodesSet.getInt("x");
                int y = nodesSet.getInt("y");
                nodes.add(new Node(sector, block, x, y));
            }

            ResultSet edgesSet = st.executeQuery("SELECT * FROM edges;");
            while (edgesSet.next()) {
                Node n1, n2;
                n1 = getNode(edgesSet.getInt("sector_1"), edgesSet.getString("block_1").charAt(0));
                n2 = getNode(edgesSet.getInt("sector_2"), edgesSet.getString("block_2").charAt(0));
                double dist = Math.sqrt(Math.pow(n1.getX() - n2.getX(), 2) + Math.pow(n1.getY() - n2.getY(), 2));
                n1.edges.add(new Edge(n2, dist));
                n2.edges.add(new Edge(n1, dist));
                System.out.println("Edge Created: " + n1.getSector() + " " + n1.getBlock() + " to " + n2.getSector() + " " + n2.getBlock() + " with dist: " + dist);
            }
            st.close();
            conn.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    static Map getMap(){
        if (map == null){
            map = new Map();
        }
        return map;
    }

//    Node getNode(int id){
//        for (Node node: nodes){
//            if (node.id == id){
//                return node;
//            }
//        }
//        return null;
//    }

    Node getNode(int sector, char block){
        for (Node node: nodes){
            if (node.getSector() == sector && node.getBlock() == block){
                return node;
            }
        }
        return null;
    }

    Node getClosestNode(int x, int y){
        int minDist = -1;
        Node closestNode = null;
        for (Node node: nodes){
            int deltaX = Math.abs(node.getX() - x);
            int deltaY = Math.abs(node.getY() - y);
            if (minDist == -1 || deltaX + deltaY < minDist){
                closestNode = node;
                minDist = deltaX + deltaY;
            }
        }
        return closestNode;
    }

    ArrayList<Node> getShortestPath(Node currentNode, Node destinationNode){
        ArrayList<Node> shortestPath;
        ArrayList<Node> visited = new ArrayList<Node>();
        visited.add(currentNode);
        HashMap<ArrayList<Node>,Double> bfsQueue = new HashMap<ArrayList<Node>, Double>();
        bfsQueue.put((ArrayList<Node>) visited.clone(), 0.0);
        boolean found = false;
        System.out.println(" ");
        if (currentNode.equals(destinationNode)){
            found = true;
            return (ArrayList<Node>) visited.clone();
        }

        while (!found){
            Entry<ArrayList<Node>, Double> min = null;
            for (Entry<ArrayList<Node>, Double> entry : bfsQueue.entrySet()) {
                if (min == null || min.getValue() > entry.getValue()) {
                    min = entry;
                }
            }
            shortestPath = min.getKey();
            bfsQueue.remove(shortestPath);
            Node lastNode = shortestPath.get(shortestPath.size()-1);
            for (Edge edge: lastNode.edges){
                if (!visited.contains(edge.getDestination())){
                    visited.add(edge.getDestination());
                    shortestPath.add(edge.getDestination());
//                    for (Node node:shortestPath){
//                        System.out.print(node.sector + ":" + node.block + " ");
//                    }
//                    System.out.println(" ");
                    if (edge.getDestination().equals(destinationNode)){
                        found = true;
                        return shortestPath;
                    }
                    bfsQueue.put((ArrayList<Node>) shortestPath.clone(), min.getValue() + map.getTimeToAdjacentNode(lastNode, edge.getDestination()));
                    shortestPath.remove(edge.getDestination());
                }
            }
        }

        return null;
    }

    double getTimeToAdjacentNode(Node currentNode, Node destinationNode){
        for (Edge edge: currentNode.edges){
            if (destinationNode == edge.getDestination()){
                return edge.getTime();
            }
        }
        return -1;
    }

    double getTimeOfPath(ArrayList<Node> path){
        int size = path.size() - 1;
        double time = 0;
        for (int i = 0; i < size; i++){
            time += getTimeToAdjacentNode(path.get(i), path.get(i + 1));
        }
        return time;
//        return getTimeToAdjacentNode(path.get(0), path.get(1)) + getTimeOfPath((ArrayList<Node>) path.subList(1,path.size()-1));
    }

}
