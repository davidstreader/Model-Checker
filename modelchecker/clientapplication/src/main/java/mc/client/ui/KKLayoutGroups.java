/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package mc.client.ui;
/*
 * This source is under the same license with JUNG.
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import lombok.Data;
import mc.client.graph.GraphNode;

/**
 * Implements the Kamada-Kawai algorithm for node layout.
 * Does not respect filter calls, and sometimes crashes when the view changes to it.
 *
 * @see "Tomihisa Kamada and Satoru Kawai: An algorithm for drawing general indirect graphs. Information Processing Letters 31(1):7-15, 1989"
 * @see "Tomihisa Kamada: On visualization of abstract objects and relations. Ph.D. dissertation, Dept. of Information Science, Univ. of Tokyo, Dec. 1988."
 *
 * @author Masanori Harada
 */
public class KKLayoutGroups<V,E> extends AbstractLayout<V,E> implements IterativeContext {


    private class diagramIndividualData {
        public ArrayList<V> vertices = new ArrayList<V>();
        public ArrayList<Point2D> xydata = new ArrayList<Point2D>();
        public double distanceMatrix[][];

    }

    private double EPSILON = 0.1d;

    private int currentIteration;
    private int maxIterations = 2000;
    private String status = "KKLayout";

    private double L = 10;			// the ideal length of an edge
    private double K = 1;		// arbitrary const number

    private boolean adjustForGravity = false;
    private boolean exchangeVertices = false;


    private HashMap<String, diagramIndividualData> diagrams = new HashMap<String, diagramIndividualData>();

    /**
     * Retrieves graph distances between vertices of the visible graph
     */
    protected Distance<V> distance;

    /**
     * The diameter of the visible graph. In other words, the maximum over all pairs
     * of vertices of the length of the shortest path between a and bf the visible graph.
     */
    protected double diameter;

    /**
     * A multiplicative factor which partly specifies the "preferred" length of an edge (L).
     */
    private double length_factor = 0.9;

    /**
     * A multiplicative factor which specifies the fraction of the graph's diameter to be
     * used as the inter-vertex distance between disconnected vertices.
     */
    private double disconnected_multiplier = 0.5;

    public KKLayoutGroups(Graph<V,E> g)
    {
        this(g, new UnweightedShortestPath<V,E>(g));
    }

    /**
     * Creates an instance for the specified graph and distance metric.
     * @param g the graph on which the layout algorithm is to operate
     * @param distance specifies the distance between pairs of vertices
     */
    public KKLayoutGroups(Graph<V,E> g, Distance<V> distance){
        super(g);
        this.distance = distance;
    }

    /**
     * @param length_factor a multiplicative factor which partially specifies
     *     the preferred length of an edge
     */
    public void setLengthFactor(double length_factor){
        this.length_factor = length_factor;
    }

    /**
     * @param disconnected_multiplier a multiplicative factor that specifies the fraction of the
     *     graph's diameter to be used as the inter-vertex distance between disconnected vertices
     */
    public void setDisconnectedDistanceMultiplier(double disconnected_multiplier){
        this.disconnected_multiplier = disconnected_multiplier;
    }

    /**
     * @return a string with information about the current status of the algorithm.
     */
    public String getStatus() {
        return status + this.getSize();
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * @return true
     */
    public boolean isIncremental() {
        return true;
    }

    /**
     * @return true if the current iteration has passed the maximum count.
     */
    public boolean done() {
        if (currentIteration > maxIterations) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void initialize() {
        currentIteration = 0;

        if(graph != null && size != null) {

            double height = size.getHeight();
            double width = size.getWidth();

            V[] vertices = (V[])graph.getVertices().toArray();

                for (V v : vertices) {
                    String key = ((GraphNode) v).getAutomata();
                    if (!diagrams.containsKey(key)) {
                        diagramIndividualData newDiagram = new diagramIndividualData();
                        newDiagram.vertices.add(v);
                        newDiagram.xydata.add(apply(v));
                        diagrams.put(key, newDiagram);
                    } else if(!diagrams.get(key).vertices.contains(v)){ // This may cause problems for larger diagrams
                        diagrams.get(key).vertices.add(v);
                        diagrams.get(key).xydata.add(apply(v)); //Applies the inital layout (Randomly placing it down)
                    }
                }



            diameter = DistanceStatistics.<V,E>diameter(graph, distance, true);

            double L0 = Math.min(height, width);
            L = (L0 / diameter) * length_factor;  // length_factor used to be hardcoded to 0.9
            //L = 0.75 * Math.sqrt(height * width / n);



            for(String process : diagrams.keySet()) {
                ArrayList<V> vertexes  = diagrams.get(process).vertices;
                diagrams.get(process).distanceMatrix = new double[vertexes.size()][vertexes.size()];
                for(int i = 0; i < vertexes.size()-1; i++) {
                    for(int j = i+1; j < vertexes.size(); j++) {
                        Number d_ij = distance.getDistance(vertices[i], vertices[j]);
                        Number d_ji = distance.getDistance(vertices[j], vertices[i]);
                        double dist = diameter * disconnected_multiplier;
                        if (d_ij != null)
                            dist = Math.min(d_ij.doubleValue(), dist);
                        if (d_ji != null)
                            dist = Math.min(d_ji.doubleValue(), dist);
                        diagrams.get(process).distanceMatrix[i][j] =  diagrams.get(process).distanceMatrix[j][i] = dist;
                    }
                }

            }

        }
    }

    public void step() {
        try {
            currentIteration++;

            if(getGraph().getVertexCount() == 0)
                return;


            for(String currentProcess : diagrams.keySet()) {
                ArrayList<V> vertices = diagrams.get(currentProcess).vertices;
                ArrayList<Point2D> xydata = diagrams.get(currentProcess).xydata;

                double energy = calcEnergy(diagrams.get(currentProcess));
                status = "Kamada-Kawai V=" + getGraph().getVertexCount()
                        + "(" + getGraph().getVertexCount() + ")"
                        + " IT: " + currentIteration
                        + " E=" + energy
                ;

                int nodeWithMaxDelta = -1; // the node having max deltaM
                double maxDeltaM = 0;
                for (int i = 0; i < vertices.size(); i++) {
                    if (isLocked(vertices.get(i)))
                        continue;
                    double deltam = calcDeltaM(i, diagrams.get(currentProcess));

                    if (maxDeltaM < deltam) {
                        maxDeltaM = deltam;
                        nodeWithMaxDelta = i;
                    }
                }

                if (nodeWithMaxDelta == -1)
                    continue;

                for (int i = 0; i < 100; i++) {
                    double[] dxy = calcDeltaXY(nodeWithMaxDelta, diagrams.get(currentProcess));
                    xydata.get(nodeWithMaxDelta).setLocation(xydata.get(nodeWithMaxDelta).getX() + dxy[0], xydata.get(nodeWithMaxDelta).getY() + dxy[1]);

                    double deltam = calcDeltaM(nodeWithMaxDelta, diagrams.get(currentProcess));
                    if (deltam < EPSILON)
                        break;
                }


               if (adjustForGravity)
                   adjustForGravity(diagrams.get(currentProcess));

                if (exchangeVertices && maxDeltaM < EPSILON) {
                    energy = calcEnergy(diagrams.get(currentProcess));
                    for (int i = 0; i < vertices.size() - 1; i++) {
                        if (isLocked(vertices.get(i)))
                            continue;
                        for (int j = i + 1; j < vertices.size(); j++) {
                            if (isLocked(vertices.get(j)))
                             continue;
                            double xenergy = calcEnergyIfExchanged(i, j, diagrams.get(currentProcess));
                            if (energy > xenergy) {
                                double sx = xydata.get(i).getX();
                                double sy = xydata.get(i).getY();
                                xydata.get(i).setLocation(xydata.get(j));
                                xydata.get(j).setLocation(sx, sy);
                                return;
                             }
                        }
                    }
                }
            }
        }
        finally {
//			fireStateChanged();
        }
    }

    /**
     * Shift all vertices so that the center of gravity is located at
     * the center of the screen.
     */
    public void adjustForGravity(diagramIndividualData thisDiagram) {
        Dimension d = getSize();
        double height = d.getHeight();
        double width = d.getWidth();
        double gx = 0;
        double gy = 0;
        for (int i = 0; i < thisDiagram.xydata.size(); i++) {
            gx += thisDiagram.xydata.get(i).getX();
            gy += thisDiagram.xydata.get(i).getY();
        }
        gx /= thisDiagram.xydata.size();
        gy /= thisDiagram.xydata.size();
        double diffx = width / 2 - gx;
        double diffy = height / 2 - gy;
        for (int i = 0; i < thisDiagram.xydata.size(); i++) {
            thisDiagram.xydata.get(i).setLocation( thisDiagram.xydata.get(i).getX()+diffx,  thisDiagram.xydata.get(i).getY()+diffy);
        }
    }

    @Override
    public void setSize(Dimension size) {
        if(initialized == false)
            setInitializer(new RandomLocationTransformer<V>(size));
        super.setSize(size);
    }

    public void setAdjustForGravity(boolean on) {
        adjustForGravity = on;
    }

    public boolean getAdjustForGravity() {
        return adjustForGravity;
    }

    /**
     * Enable or disable the local minimum escape technique by
     * exchanging vertices.
     * @param on iff the local minimum escape technique is to be enabled
     */
    public void setExchangeVertices(boolean on) {
        exchangeVertices = on;
    }

    public boolean getExchangeVertices() {
        return exchangeVertices;
    }

    /**
     * Determines a step to new position of the vertex m.
     */
    private double[] calcDeltaXY(int node, diagramIndividualData elements) {
        double dE_dxm = 0;
        double dE_dym = 0;
        double d2E_d2xm = 0;
        double d2E_dxmdym = 0;
        double d2E_dymdxm = 0;
        double d2E_d2ym = 0;

        for (int i = 0; i < elements.vertices.size(); i++) {
            if (i != node) {

                double dist = elements.distanceMatrix[node][i];
                double l_mi = L * dist;
                double k_mi = K / (dist * dist);
                double dx = elements.xydata.get(node).getX() - elements.xydata.get(i).getX();
                double dy = elements.xydata.get(node).getY() - elements.xydata.get(i).getY();
                double d = Math.sqrt(dx * dx + dy * dy);
                double ddd = d * d * d;

                dE_dxm += k_mi * (1 - l_mi / d) * dx;
                dE_dym += k_mi * (1 - l_mi / d) * dy;
                d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd);
                d2E_dxmdym += k_mi * l_mi * dx * dy / ddd;
                d2E_d2ym += k_mi * (1 - l_mi * dx * dx / ddd);
            }
        }
        // d2E_dymdxm equals to d2E_dxmdym.
        d2E_dymdxm = d2E_dxmdym;

        double denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm;
        double deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi;
        double deltaY = (d2E_dymdxm * dE_dxm - d2E_d2xm * dE_dym) / denomi;
        return new double[]{deltaX, deltaY};
    }

    /**
     * Calculates the gradient of energy function at the vertex m.
     */

    private double calcDeltaM(int node, diagramIndividualData elements) {
        double dEdxm = 0;
        double dEdym = 0;
        for (int i = 0; i < elements.vertices.size(); i++) {
            if (i != node) {
                double dist = elements.distanceMatrix[node][i];
                double l_mi = L * dist;
                double k_mi = K / (dist * dist);

                double dx = elements.xydata.get(node).getX() - elements.xydata.get(i).getX();
                double dy = elements.xydata.get(node).getY() - elements.xydata.get(i).getY();
                double d = Math.sqrt(dx * dx + dy * dy);

                double common = k_mi * (1 - l_mi / d);
                dEdxm += common * dx;
                dEdym += common * dy;
            }
        }
        return Math.sqrt(dEdxm * dEdxm + dEdym * dEdym);
    }

    /**
     * Calculates the energy function E.
     */
    private double calcEnergy(diagramIndividualData currentDiagram) {
        double energy = 0;
        for (int i = 0; i < currentDiagram.vertices.size() - 1; i++) {
            for (int j = i + 1; j < currentDiagram.vertices.size(); j++) {
                double dist = currentDiagram.distanceMatrix[i][j];
                double l_ij = L * dist;
                double k_ij = K / (dist * dist);
                double dx = currentDiagram.xydata.get(i).getX() - currentDiagram.xydata.get(j).getX();
                double dy = currentDiagram.xydata.get(i).getY() - currentDiagram.xydata.get(j).getY();
                double d = Math.sqrt(dx * dx + dy * dy);


                energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij -
                        2 * l_ij * d);
            }
        }
        return energy;
    }

    /**
     * Calculates the energy function E as if positions of the
     * specified vertices are exchanged.
     */
    private double calcEnergyIfExchanged(int p, int q, diagramIndividualData currentDiagram) {
        if (p >= q)
            throw new RuntimeException("p should be < q");
        double energy = 0;		// < 0
        for (int i = 0; i < currentDiagram.vertices.size()-1; i++) {
            for (int j = i + 1; j < currentDiagram.vertices.size(); j++) {
                int ii = i;
                int jj = j;
                if (i == p) ii = q;
                if (j == q) jj = p;

                double dist = currentDiagram.distanceMatrix[i][j];
                double l_ij = L * dist;
                double k_ij = K / (dist * dist);
                double dx =  currentDiagram.xydata.get(ii).getX() - currentDiagram.xydata.get(jj).getX();
                double dy =  currentDiagram.xydata.get(ii).getY() - currentDiagram.xydata.get(jj).getY();
                double d = Math.sqrt(dx * dx + dy * dy);

                energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij -
                        2 * l_ij * d);
            }
        }
        return energy;
    }

    public void reset() {
        currentIteration = 0;
    }
}
