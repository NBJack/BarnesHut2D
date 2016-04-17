package org.layfield.graph.barnes;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ryan Layfield on 4/14/2016.
 */
public class TestClass {

    public static class Place implements Locationable {

        Vector2D position;
        String data;

        public Place(String name, Vector2D loc) {
            this.data = name;
            this.position = loc;
        }

        public String getData() {
            return data;
        }

        @Override
        public Vector2D getLocation() {
            return position;
        }
    }

    /**
     * Find all places within distance.
     * @param origin
     * @param dist
     * @return
     */
    public static List<Place> findNearby(Vector2D origin, double dist, BarnesNode<Place> root) {
        List<Place> places = new LinkedList<>();
        List<BarnesNode<Place>> queue = new LinkedList<>();

        queue.add(root);

        int checks = 0;

        while (!queue.isEmpty()) {
            BarnesNode<Place> place = queue.remove(0);
            checks++;
            place.flag();

            if (place.hasData()) {
                // Is the current place within our radius?
                if (place.getData().getLocation().distTo(origin) <= dist) {
                    places.add(place.getData());
                }
            } else {
                // See if there's any nodes
                if (!place.isLeaf()) {
                    for (BarnesNode<Place> child : place.getChildren()) {
                        if (child.isWithin(origin) || minDistTo(child, origin) < dist) {
                            queue.add(child);
                        }
                    }
                }
            }
        }

        places.sort((Place a, Place b) -> Double.compare(origin.distTo(a.getLocation()), origin.distTo(b.getLocation())));

        System.out.println("Checks performed: " + checks);

        return places;
    }

    /**
     * Calculates the shortest distance among all sides of the node. Useful to see if there exists a possibility it will
     * be within distance of something else.
     * @param node
     * @param origin
     * @param <T>
     * @return
     */
    private static <T extends Locationable> double minDistTo(BarnesNode<T> node, Vector2D origin) {
        return Math.min(
                Math.min(
                        Line2D.ptSegDist(node.getUpLeft().getX(), node.getUpLeft().getY(), node.getUpLeft().getX(), node.getLowRight().getY(), origin.getX(), origin.getY()),
                        Line2D.ptSegDist(node.getUpLeft().getX(), node.getUpLeft().getY(), node.getLowRight().getX(), node.getUpLeft().getY(), origin.getX(), origin.getY())
                ),
                Math.min(
                        Line2D.ptSegDist(node.getLowRight().getX(), node.getLowRight().getY(), node.getLowRight().getX(), node.getUpLeft().getY(), origin.getX(), origin.getY()),
                        Line2D.ptSegDist(node.getLowRight().getX(), node.getLowRight().getY(), node.getUpLeft().getX(), node.getLowRight().getY(), origin.getX(), origin.getY())
                )
        );
    }

    public static List<Place> readPlaces(File file) throws IOException {
        List<Place> places = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        // Read headers
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            String [] args = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            Place place = new Place(args[0], new Vector2D(Double.valueOf(args[3]), -Double.valueOf(args[2])));
            places.add(place);
        }

        return places;
    }

    public static void main(String [] args) throws IOException {
        BarnesNode<Place> root = new BarnesNode<Place>(new Vector2D(-180,-180), new Vector2D(180,180));

        List<Place> allPlaces = new LinkedList<>();
        allPlaces.addAll(readPlaces(new File("C:\\temp\\cities.csv")));

        HashMap<String, Place> namePlace = new HashMap<>();
//        Place x = new Place("McKinney", new Vector2D(20, 20));

//        root.addData(x);
//        allPlaces.add(x);

        System.out.println("Adding lots of places... " + allPlaces.size());

        long n = 0;
        Random rand = new Random();
        List<Place> newRecruits = new LinkedList<>();
        for (Place p : allPlaces) {
            root.addData(p);
            namePlace.put(p.getData(), p);

            // Add in several surrounding
            for (int  i = rand.nextInt(100) + 10; i > 0; i--) {
                Place aroundP = new Place(p.getData() + "-" + (n++), new Vector2D(
                        p.getLocation().getX() + rand.nextDouble() * 0.1 - 0.05,
                        p.getLocation().getY() + rand.nextDouble() * 0.1 - 0.05
                ));
                root.addData(aroundP);
                newRecruits.add(aroundP);
            }
        }
        allPlaces.addAll(newRecruits);

        final EasyBitmap bitWriter = new EasyBitmap(3601,1801);
        bitWriter.clear(Color.white);
        bitWriter.setOffset(1800, 900);

        System.out.println("Done!");

        System.out.println("Searching..");

        final Place place1 = namePlace.get("Dallas");
        Vector2D origin = place1.getLocation();
        List<Place> allWithin = findNearby(origin, 2, root);
        System.out.println("Places found: " + allWithin.size());
        List<Place> allWithinBrute = findNearbyBrute(origin, 2, allPlaces);
        System.out.println("Places found (brute): " + allWithinBrute.size());

        for (Place p : allWithin) {
            System.out.println("  " + p.getData() + "  " + p.getLocation().distTo(origin));
        }


        //
        // Now, output that result.
        //

        // Bounds
        root.visitAll((place) -> {
            // Write the bounding rect
            if (place.isLeaf()) {
                Color backFill = Color.white;
                if (place.getFlag()) {
                    backFill = Color.yellow;
                } else {
                    if (place.hasData()) {
                        backFill = Color.lightGray;
                    }
                }
                bitWriter.drawFillRect(backFill, place.getUpLeft().scaleBy(10), place.getLowRight().scaleBy(10));
//                bitWriter.drawRect(Color.gray, place.getUpLeft().scaleBy(10), place.getLowRight().scaleBy(10));
            } else if (place.hasData()) {
                throw new IllegalStateException();
            }
        });

        final AtomicInteger maxLevel = new AtomicInteger(0);

        final AtomicInteger counter = new AtomicInteger(0);
        // Data
        root.visitAll((place, level) -> {
            // Write the location, if it exists.
            if (place.hasData()) {
                counter.getAndIncrement();
                bitWriter.drawPoint(Color.red, place.getData().getLocation().scaleBy(10));

                //if (place.getData().getData().equals(place1.getData()))
                //    bitWriter.drawText(Color.red, place.getData().getLocation().scaleBy(10), place.getData().getData());
            }

            if (maxLevel.get() < level) {
                maxLevel.set(level);
            }
        });

        System.out.println("Max level: " + maxLevel.get() + " Data count: " + counter.get());

        bitWriter.save(new File("c:\\temp\\out.bmp"));
    }

    private static List<Place> findNearbyBrute(Vector2D origin, double dist, List<Place> places) {
        List<Place> results = new LinkedList<>();

        for (Place p : places) {
            if (p.getLocation().distTo(origin) <= dist) {
                results.add(p);
            }
        }

        results.sort((Place a, Place b) -> Double.compare(origin.distTo(a.getLocation()), origin.distTo(b.getLocation())));

        System.out.println("Checks performed: " + places.size());

        return results;
    }
}
