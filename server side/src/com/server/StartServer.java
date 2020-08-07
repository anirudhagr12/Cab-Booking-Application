package com.server;

class StartServer{
    public static void main(String[] args){
        Map map = Map.getMap();

        new ClientsAdderThread(8001);
        TripControl tripControl = TripControl.getTripControl();

        System.out.println("Serer Initialized");

    }
}