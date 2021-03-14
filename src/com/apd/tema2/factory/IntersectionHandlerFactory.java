package com.apd.tema2.factory;

import com.apd.tema2.Main;
import com.apd.tema2.entities.*;
import com.apd.tema2.intersections.*;
import com.apd.tema2.utils.Constants;

import java.sql.SQLOutput;
import java.util.SortedMap;
import java.util.concurrent.BrokenBarrierException;

import static java.lang.Thread.sleep;

/**
 * Clasa Factory ce returneaza implementari ale InterfaceHandler sub forma unor
 * clase anonime.
 */
public class IntersectionHandlerFactory {

    public static IntersectionHandler getHandler(String handlerType) {
        // simple semaphore intersection
        // max random N cars roundabout (s time to exit each of them)
        // roundabout with exactly one car from each lane simultaneously
        // roundabout with exactly X cars from each lane simultaneously
        // roundabout with at most X cars from each lane simultaneously
        // entering a road without any priority
        // crosswalk activated on at least a number of people (s time to finish all of
        // them)
        // road in maintenance - 2 ways 1 lane each, X cars at a time
        // road in maintenance - 1 way, M out of N lanes are blocked, X cars at a time
        // railroad blockage for s seconds for all the cars
        // unmarked intersection
        // cars racing
        return switch (handlerType) {
            case "simple_semaphore" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    // Only uses a sleep to synchronize the cars
                    System.out.println("Car " + car.getId() + " has reached the semaphore, now waiting...");
                    try {
                        Thread.sleep(car.getWaitingTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Car " + car.getId() + " has waited enough, now driving...");
                }
            };
            case "simple_n_roundabout" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    // Uses a semaphore to limit the number of cars in the roundabout
                    System.out.println("Car " + car.getId() + " has reached the roundabout, now waiting...");
                    try {
                        Thread.sleep(car.getWaitingTime());
                        SimpleIntersection.roundBarrier.await();
                        SimpleIntersection.sem.acquire();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " has entered the roundabout");
                    try {
                        Thread.sleep(SimpleIntersection.waitTimeRound);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " has exited the roundabout after "
                            + SimpleIntersection.waitTimeRound / 1000 + " seconds");
                    SimpleIntersection.sem.release();
                }
            };
            case "simple_strict_1_car_roundabout" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    // Uses a list of semaphores to restrict traffic
                    System.out.println("Car " + car.getId() + " has reached the roundabout");
                    try {
                        SimpleIntersection.semList.get(car.getStartDirection()).acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " has entered the roundabout from lane "
                            + car.getStartDirection());
                    try {
                        Thread.sleep(SimpleIntersection.waitTimeRound);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " has exited the roundabout after "
                            + SimpleIntersection.waitTimeRound / 1000 + " seconds");
                    SimpleIntersection.semList.get(car.getStartDirection()).release();
                }
            };
            case "simple_strict_x_car_roundabout" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    // Uses a barrier to wait for all cars to enter
                    System.out.println("Car " + car.getId() + " has reached the roundabout, now waiting...");
                    try {
                        // Wait for all cars to arrive
                        SimpleIntersection.roundBarrier.await();

                        // Wait for lanes and the roundabout to be clear
                        SimpleIntersection.semList.get(car.getStartDirection()).acquire();

                        SimpleIntersection.lanesBarrier.await();
                        System.out.println("Car " + car.getId() + " was selected to enter the roundabout " +
                                "from lane " + car.getStartDirection());

                        SimpleIntersection.lanesBarrier.await();

                        // Remove the cars from the lanes and roundabout
                        System.out.println("Car " + car.getId() + " has entered the roundabout from lane " +
                                car.getStartDirection());
                        SimpleIntersection.sem.acquire();
                        Thread.sleep(SimpleIntersection.waitTimeRound);
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " has exited the roundabout after "
                            + SimpleIntersection.waitTimeRound / 1000 + " seconds");
                    SimpleIntersection.semList.get(car.getStartDirection()).release();
                    SimpleIntersection.sem.release();
                }
            };
            case "simple_max_x_car_roundabout" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    try {
                        sleep(car.getWaitingTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " has reached the roundabout from lane " + car.getStartDirection());

                    try {
                        // The cars and enter the roundabout whenever a spot is free
                        SimpleIntersection.semList.get(car.getStartDirection()).acquire();

                        System.out.println("Car " + car.getId() + " has entered the roundabout " +
                                "from lane " + car.getStartDirection());

                        Thread.sleep(SimpleIntersection.waitTimeRound);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " has exited the roundabout after "
                            + SimpleIntersection.waitTimeRound / 1000 + " seconds");
                    SimpleIntersection.semList.get(car.getStartDirection()).release();
                }
            };
            case "priority_intersection" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    try {
                        sleep(car.getWaitingTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (car.getPriority() != 1) {
                        System.out.println("Car " + car.getId() + " with high priority " +
                                "has entered the intersection");
                        try {
                            SimpleIntersection.sem.acquire();
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println("Car " + car.getId() + " with high priority " +
                                "has exited the intersection");
                        SimpleIntersection.sem.release();

                        // When no more cars are in the intersection, notify all the waiting cars
                        synchronized (SimpleIntersection.myLock) {
                            if (SimpleIntersection.sem.availablePermits() == SimpleIntersection.noHighPriority) {
                                SimpleIntersection.myLock.notifyAll();
                            }
                        }
                    } else {
                        // Use a wait/notify combination to resume traffic flow
                        try {
                            synchronized (SimpleIntersection.myLock) {
                                    System.out.println("Car " + car.getId() + " with low priority " +
                                            "is trying to enter the intersection...");
                                    SimpleIntersection.idQueue.put(car.getId());
                                    if (SimpleIntersection.sem.availablePermits() != SimpleIntersection.noHighPriority) {
                                        SimpleIntersection.myLock.wait();
                                    }
                            }

                            // Verify that the cars exit in the same order that they entered
                            while (true) {
                                int currID = (int) SimpleIntersection.idQueue.peek();
                                if (currID == car.getId()) {
                                    SimpleIntersection.idQueue.take();
                                    break;
                                }
                            }
                            System.out.println("Car " + car.getId() + " with low priority has entered the intersection");

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            case "crosswalk" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    // Use a wait/notify combination to alert the cars when they can pass
                    while (!Main.pedestrians.isFinished()) {
                        if (Main.pedestrians.isPass()) {

                            String message = "Car " + car.getId() + " has now red light";
                            if (SimpleIntersection.messageList.get(car.getId()) == null || !SimpleIntersection.messageList.get(car.getId()).equals(message)) {
                                System.out.println(message);
                                SimpleIntersection.messageList.set(car.getId(), message);
                            }

                            try {
                                synchronized (SimpleIntersection.myLock) {
                                    SimpleIntersection.myLock.wait();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            String message = "Car " + car.getId() + " has now green light";
                            if (SimpleIntersection.messageList.get(car.getId()) == null || !SimpleIntersection.messageList.get(car.getId()).equals(message)) {
                                System.out.println(message);
                                SimpleIntersection.messageList.set(car.getId(), message);
                            }
                        }
                    }
                    // Check for the last pass
                    String message = "Car " + car.getId() + " has now green light";
                    if (SimpleIntersection.messageList.get(car.getId()) == null || !SimpleIntersection.messageList.get(car.getId()).equals(message)) {
                        System.out.println(message);
                        SimpleIntersection.messageList.set(car.getId(), message);
                    }
                }
            };
            case "simple_maintenance" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    // Use a semaphore for both directions to check if the cars can pass
                    // Initially, semaphore 0 has max permits while semaphore 1 has no permits
                    System.out.println("Car " + car.getId() + " from side number " + car.getStartDirection() +
                            " has reached the bottleneck");
                    try {
                        SimpleIntersection.lanesBarrier.await();
                        SimpleIntersection.semList.get(car.getStartDirection()).acquire();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Car " + car.getId() + " from side number " + car.getStartDirection() +
                            " has passed the bottleneck");
                    try {
                        SimpleIntersection.waitBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    // If semaphore 0 ran out of permits, give semaphore 1 the max num of permits
                    if (SimpleIntersection.semList.get(SimpleIntersection.freeDir).availablePermits() == 0) {
                        SimpleIntersection.freeDir = (SimpleIntersection.freeDir == 0) ? 1 : 0;
                        SimpleIntersection.semList.get(SimpleIntersection.freeDir).release(SimpleIntersection.blockadeCars);
                    }
                }
            };
            case "complex_maintenance" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    Integer currVal = SimpleIntersection.laneMap.putIfAbsent(car.getStartDirection(), 1);
                    if (currVal != null) {
                        SimpleIntersection.laneMap.put(car.getStartDirection(), 1 + currVal);
                    }


                    try {
                        SimpleIntersection.waitBarrier.await();
                        SimpleIntersection.laneQueue.get(car.getStartDirection()).put(car.getId());
                        System.out.println("Car " + car.getId() + " has come from the lane number " + car.getStartDirection());
                        SimpleIntersection.semList.get(car.getStartDirection()).acquire();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    while (true) {
                        int currFreeLane = SimpleIntersection.laneRepartition.get(car.getStartDirection());
                        if (car.getStartDirection() == SimpleIntersection.laneList.get(currFreeLane).get(0)) {
                            int currID = (int) SimpleIntersection.laneQueue.get(car.getStartDirection()).peek();

                            if (car.getId() == currID) {
                                System.out.println("Car " + car.getId() + " from lane " + car.getStartDirection() +
                                        " has entered lane number " + currFreeLane);

                                Integer remVal = SimpleIntersection.laneMap.putIfAbsent(car.getStartDirection(), 0);
                                SimpleIntersection.laneMap.put(car.getStartDirection(), remVal - 1);
                                remVal = SimpleIntersection.laneMap.get(car.getStartDirection());

                                if (SimpleIntersection.semList.get(car.getStartDirection()).availablePermits() == 0 || remVal == 0) {

                                    if (remVal == 0) {
                                        SimpleIntersection.laneList.get(currFreeLane).remove(0);
                                        System.out.println("The initial lane " + car.getStartDirection() +
                                                " has been emptied and removed from the new lane queue");
                                    } else {
                                        int removedLane = SimpleIntersection.laneList.get(currFreeLane).remove(0);
                                        SimpleIntersection.laneList.get(currFreeLane).add(removedLane);
                                        System.out.println("The initial lane " + car.getStartDirection() +
                                                " has no permits and is moved to the back of the new lane queue");
                                    }

                                    if (SimpleIntersection.laneList.get(currFreeLane).size() != 0) {
                                        SimpleIntersection.semList.get(SimpleIntersection.laneList.get(currFreeLane).get(0)).release(SimpleIntersection.blockadeCars);
                                    }
                                }

                                try {
                                    SimpleIntersection.laneQueue.get(car.getStartDirection()).take();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }
            };
            case "railroad" -> new IntersectionHandler() {
                @Override
                public void handle(Car car) {
                    // Use a barrier to wait for the train to pass
                    System.out.println("Car " + car.getId() + " from side number " + car.getStartDirection() +
                            " has stopped by the railroad");
                    try {
                        synchronized (SimpleIntersection.myLock) {
                            SimpleIntersection.idQueue.put(car.getId());
                        }
                        SimpleIntersection.lanesBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    if (car.getId() == 0) {
                        System.out.println("The train has passed, cars can now proceed");
                    }
                    try {
                        SimpleIntersection.lanesBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    // Use a synchronized queue to retain the order of passing
                    while (true) {
                        int currID = (int) SimpleIntersection.idQueue.peek();
                        if (currID == car.getId()) {
                            System.out.println("Car " + car.getId() + " from side number " + car.getStartDirection() +
                                    " has started driving");
                            try {
                                SimpleIntersection.idQueue.take();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            };
            default -> null;
        };
    }
}
