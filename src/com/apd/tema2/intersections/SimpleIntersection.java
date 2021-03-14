package com.apd.tema2.intersections;

import com.apd.tema2.entities.Intersection;

import java.util.List;
import java.util.concurrent.*;

public class SimpleIntersection implements Intersection {
    // Define your variables here.
    // Task 2
    public static CyclicBarrier roundBarrier;
    public static Semaphore sem;
    public static int noRound;
    public static int waitTimeRound;

    // Task 3
    public static List<Semaphore> semList;
    public static int noLanes;

    // Task 4
    public static int maxCarsLane;
    public static CyclicBarrier lanesBarrier;
    public static final String myLock = "lock";

    // Task 6
    public static int noHighPriority;
    public static int noLowPriority;
    public static ArrayBlockingQueue idQueue;

    // Task 7
    public static int noPedestrians;
    public static int executeTime;
    public static List<String> messageList;

    // Task 8
    public static int blockadeCars;
    public static int freeDir;
    public static CyclicBarrier waitBarrier;

    // Task 9
    public static int noFreeLanes;
    public static int noInitLanes;
    public static List<List<Integer>> laneList;
    public static ConcurrentHashMap<Integer, Integer> laneMap;
    public static List<ArrayBlockingQueue> laneQueue;
    public static ConcurrentHashMap<Integer, Integer> laneRepartition;
}
