package com.apd.tema2.factory;

import com.apd.tema2.Main;
import com.apd.tema2.entities.Intersection;
import com.apd.tema2.entities.Pedestrians;
import com.apd.tema2.entities.ReaderHandler;
import com.apd.tema2.intersections.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * Returneaza sub forma unor clase anonime implementari pentru metoda de citire din fisier.
 */
public class ReaderHandlerFactory {

    public static ReaderHandler getHandler(String handlerType) {
        // simple semaphore intersection
        // max random N cars roundabout (s time to exit each of them)
        // roundabout with exactly one car from each lane simultaneously
        // roundabout with exactly X cars from each lane simultaneously
        // roundabout with at most X cars from each lane simultaneously
        // entering a road without any priority
        // crosswalk activated on at least a number of people (s time to finish all of them)
        // road in maintenance - 1 lane 2 ways, X cars at a time
        // road in maintenance - N lanes 2 ways, X cars at a time
        // railroad blockage for T seconds for all the cars
        // unmarked intersection
        // cars racing
        return switch (handlerType) {
            case "simple_semaphore" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) {
                    Main.intersection = IntersectionFactory.getIntersection("simpleIntersection");
                }
            };
            case "simple_n_roundabout" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {

                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.noRound = Integer.parseInt(line[0]);
                    SimpleIntersection.waitTimeRound = Integer.parseInt(line[1]);

                    SimpleIntersection.sem = new Semaphore(SimpleIntersection.noRound);
                    SimpleIntersection.roundBarrier = new CyclicBarrier(Main.carsNo);
                    Main.intersection = IntersectionFactory.getIntersection("simple_n_roundabout");
                }
            };
            case "simple_strict_1_car_roundabout" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.noLanes = Integer.parseInt(line[0]);
                    SimpleIntersection.waitTimeRound = Integer.parseInt(line[1]);

                    SimpleIntersection.semList = Collections.synchronizedList(new ArrayList<>());
                    for (int i = 0; i < SimpleIntersection.noLanes; ++i) {
                        SimpleIntersection.semList.add(new Semaphore(1));
                    }
                    Main.intersection = IntersectionFactory.getIntersection("simple_strict_1_car_roundabout");
                }
            };
            case "simple_strict_x_car_roundabout" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.noLanes = Integer.parseInt(line[0]);
                    SimpleIntersection.waitTimeRound = Integer.parseInt(line[1]);
                    SimpleIntersection.maxCarsLane = Integer.parseInt(line[2]);

                    SimpleIntersection.roundBarrier = new CyclicBarrier(Main.carsNo);
                    SimpleIntersection.lanesBarrier = new CyclicBarrier(SimpleIntersection.noLanes * SimpleIntersection.maxCarsLane);
                    SimpleIntersection.sem = new Semaphore(SimpleIntersection.noLanes * SimpleIntersection.maxCarsLane);

                    SimpleIntersection.semList = Collections.synchronizedList(new ArrayList<>());
                    for (int i = 0; i < SimpleIntersection.noLanes; ++i) {
                        SimpleIntersection.semList.add(new Semaphore(SimpleIntersection.maxCarsLane));
                    }
                    Main.intersection = IntersectionFactory.getIntersection("simple_strict_x_car_roundabout");
                }
            };
            case "simple_max_x_car_roundabout" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.noLanes = Integer.parseInt(line[0]);
                    SimpleIntersection.waitTimeRound = Integer.parseInt(line[1]);
                    SimpleIntersection.maxCarsLane = Integer.parseInt(line[2]);

                    SimpleIntersection.semList = Collections.synchronizedList(new ArrayList<>());
                    for (int i = 0; i < SimpleIntersection.noLanes; ++i) {
                        SimpleIntersection.semList.add(new Semaphore(SimpleIntersection.maxCarsLane));
                    }

                    Main.intersection = IntersectionFactory.getIntersection("simple_max_x_car_roundabout");
                }
            };
            case "priority_intersection" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.noHighPriority = Integer.parseInt(line[0]);
                    SimpleIntersection.noLowPriority = Integer.parseInt(line[1]);

                    SimpleIntersection.sem = new Semaphore(SimpleIntersection.noHighPriority);
                    SimpleIntersection.idQueue = new ArrayBlockingQueue<Integer>(SimpleIntersection.noLowPriority);
                    Main.intersection = IntersectionFactory.getIntersection("priority_intersection");
                }
            };
            case "crosswalk" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.executeTime = Integer.parseInt(line[0]);
                    SimpleIntersection.noPedestrians = Integer.parseInt(line[1]);

                    SimpleIntersection.messageList = Collections.synchronizedList(new ArrayList<>());
                    for (int i = 0; i < Main.carsNo; ++i) {
                        SimpleIntersection.messageList.add(null);
                    }
                    Main.pedestrians = new Pedestrians(SimpleIntersection.executeTime, SimpleIntersection.noPedestrians);
                    Main.intersection = IntersectionFactory.getIntersection("crosswalk");
                }
            };
            case "simple_maintenance" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.blockadeCars = Integer.parseInt(line[0]);
                    SimpleIntersection.freeDir = 0;

                    SimpleIntersection.semList = Collections.synchronizedList(new ArrayList<>());
                    SimpleIntersection.semList.add(new Semaphore(SimpleIntersection.blockadeCars));
                    SimpleIntersection.semList.add(new Semaphore(0));
                    SimpleIntersection.lanesBarrier = new CyclicBarrier(Main.carsNo);
                    SimpleIntersection.waitBarrier = new CyclicBarrier(SimpleIntersection.blockadeCars);

                    Main.intersection = IntersectionFactory.getIntersection("simple_maintenance");
                }
            };
            case "complex_maintenance" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    String[] line = br.readLine().split(" ");
                    SimpleIntersection.noFreeLanes = Integer.parseInt(line[0]);
                    SimpleIntersection.noInitLanes = Integer.parseInt(line[1]);
                    SimpleIntersection.blockadeCars = Integer.parseInt(line[2]);

                    // Create the lane repartition
                    SimpleIntersection.waitBarrier = new CyclicBarrier(Main.carsNo);
                    SimpleIntersection.laneMap = new ConcurrentHashMap<>();

                    SimpleIntersection.laneQueue = Collections.synchronizedList(new ArrayList<>());
                    for (int i = 0; i < SimpleIntersection.noInitLanes; ++i) {
                        SimpleIntersection.laneQueue.add(new ArrayBlockingQueue(Main.carsNo));
                    }

                    // Divide the old lanes into the corresponding new lanes
                    SimpleIntersection.laneList = Collections.synchronizedList(new ArrayList<>());
                    SimpleIntersection.semList = Collections.synchronizedList(new ArrayList<>());
                    SimpleIntersection.laneRepartition = new ConcurrentHashMap<>();
                    for (int i = 0; i < SimpleIntersection.noFreeLanes; ++i) {
                        int start = (int) (i * (double)SimpleIntersection.noInitLanes / SimpleIntersection.noFreeLanes);
                        int end = (int) Math.min((i + 1) * (double)SimpleIntersection.noInitLanes / SimpleIntersection.noFreeLanes, SimpleIntersection.noInitLanes);
                        System.out.println("start: " + start  + " end : " + end + " id; " + i);
                        SimpleIntersection.laneList.add(Collections.synchronizedList(new ArrayList<>()));

                        for (int j = start; j < end; ++j) {
                            SimpleIntersection.laneList.get(i).add(j);
                            SimpleIntersection.laneRepartition.putIfAbsent(j, i);
                            if (j == start) {
                                SimpleIntersection.semList.add(new Semaphore(SimpleIntersection.blockadeCars));
                            } else {
                                SimpleIntersection.semList.add(new Semaphore(0));
                            }
                        }


                    }

                    Main.intersection = IntersectionFactory.getIntersection("complex_maintenance");
                }
            };
            case "railroad" -> new ReaderHandler() {
                @Override
                public void handle(final String handlerType, final BufferedReader br) throws IOException {
                    SimpleIntersection.lanesBarrier = new CyclicBarrier(Main.carsNo);
                    SimpleIntersection.idQueue = new ArrayBlockingQueue(Main.carsNo);

                    Main.intersection = IntersectionFactory.getIntersection("railroad");
                }
            };
            default -> null;
        };
    }

}
