/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package mc.client.graph;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.base.Functions;
//import java.util.function.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * The SpringLayout package represents a visualization of a set of nodes. The
 * SpringLayout, which is initialized with a Graph, assigns X/Y locations to
 * each node. When called <code>relax()</code>, the SpringLayout moves the
 * visualization forward one step.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
public class SpringlayoutBase<V, E> extends AbstractLayout<V,E> implements IterativeContext {

    private double stretch = 0.70;
    private int repulsion_range_sq = 100 * 100;
    private double force_multiplier = 1;
    private double stepO = 1;
    private Function<Object, Integer> maxNodesFunction;
    private Function<Object, Integer> repulseFunction;
    private Function<Object, Integer> springFunction;
    private Function<Object, Integer> delayFunction;
    private Function<Object, Integer> stepFunction;
    private boolean done = false;

    private LoadingCache<V, SpringVertexData> springVertexData =
            CacheBuilder.newBuilder().build(new CacheLoader<V, SpringVertexData>() {
                public SpringVertexData load(V vertex) {
                    return new SpringVertexData();
                }
            });


    /**
     * Constructor for a SpringLayout for a raw graph with associated
     * dimension--the input knows how big the graph is. Defaults to the unit
     * length function.
     * @param g the graph on which the layout algorithm is to operate
     */
    @SuppressWarnings("unchecked")
    public SpringlayoutBase(Graph<V,E> g) {
        this(g
                , (Function<Object,Integer>)Functions.<Integer>constant(100)
                , (Function<Object,Integer>)Functions.<Integer>constant(50)
                , (Function<Object,Integer>)Functions.<Integer>constant(25)
                , (Function<Object,Integer>)Functions.<Integer>constant(100)
                , (Function<Object,Integer>)Functions.<Integer>constant(10)
        );}

    /**
     * Constructor for a SpringLayout for a raw graph with associated component.
     *
     * @param g the graph on which the layout algorithm is to operate
     * @param maxNodes_function provides a length for each edge
     *
     * @param spring_function provides a length for each edge
     * @param delay_function provides a length for each edge
     */
    public SpringlayoutBase(Graph<V,E> g
            , Function<Object, Integer> maxNodes_function
            , Function<Object, Integer> spring_function
            , Function<Object, Integer> repulse_function
            , Function<Object, Integer> step_function
            , Function<Object, Integer> delay_function
    )
    {
        super(g);
        this.maxNodesFunction = maxNodes_function;
        this.springFunction = spring_function;
        this.repulseFunction = repulse_function;
        this.stepFunction = step_function;
        this.delayFunction = delay_function;

        /*System.out.println("\n\n\n\n    ******OK STARTING  ");
        Throwable t = new Throwable();
        t.printStackTrace();
        System.out.println("    ******OK STARTING  \n\n\n\n"); */



    }
    /**
     * @return the current value for the stretch parameter
     */
    public double getStretch() {
        return stretch;
    }

    @Override
    public void setSize(Dimension size) {
        if(!initialized)
            setInitializer(new RandomLocationTransformer<V>(size));
        super.setSize(size);
    }

    /**
     * <p>Sets the stretch parameter for this instance.  This value
     * specifies how much the degrees of an edge's incident vertices
     * should influence how easily the endpoints of that edge
     * can move (that is, that edge's tendency to change its length).
     *
     * <p>The default value is 0.70.  Positive values less than 1 cause
     * high-degree vertices to move less than low-degree vertices, and
     * values &gt; 1 cause high-degree vertices to move more than
     * low-degree vertices.  Negative values will have unpredictable
     * and inconsistent results.
     * @param stretch the stretch parameter
     */
    public void setStretch(double stretch) {
        this.stretch = stretch;
    }

    public int getRepulsionRange() {
        return (int)(Math.sqrt(repulsion_range_sq));
    }

    /**
     * Sets the node repulsion range (in drawing area units) for this instance.
     * Outside this range, nodes do not repel each other.  The default value
     * is 100.  Negative values are treated as their positive equivalents.
     * @param range the maximum repulsion range
     */
    public void setRepulsionRange(int range) {
        this.repulsion_range_sq = range * range;
    }

    /**
     * Sets the force multiplier for this instance.  This value is used to
     * specify how strongly an edge "wants" to be its default length
     * (higher values indicate a greater attraction for the default length),
     * which affects how much its endpoints move at each timestep.
     * The default value is 1/3.  A value of 0 turns off any attempt by the
     * layout to cause edges to conform to the default length.  Negative
     * values cause long edges to get longer and short edges to get shorter; use
     * at your own risk.
     * @param force an energy field created by all living things that binds the galaxy together
     */
    public void setForceMultiplier(double force) {
        this.force_multiplier = force;
    }

    public void initialize() {}

    /**
     * Relaxation step. Moves all nodes a smidge.
     */
    public void step()  {
        Dummy d = new Dummy();
        Integer delay = 0;
        try {
            boolean first = true;
            for(V v : getGraph().getVertices()) {
                SpringVertexData svd = springVertexData.getUnchecked(v);
                if (svd == null) {
                    continue;
                }
                // svd.dx /= 4;
                // svd.dy /= 4;
                svd.dx = svd.dy = 0;
                svd.oldx = svd.oldy = 0;
                svd.edgedx = svd.edgedy = 0;
                svd.repulsiondx = svd.repulsiondy = 0;
                delay = delayFunction.apply(d);
            }
        } catch(ConcurrentModificationException cme) {
            System.out.println("\n SpringLayout istep ConcurrentModificationException\n");
            step();
        }

        springEdges();
        calculateRepulsion();


        try {
            long now = System.currentTimeMillis();
            //System.out.println("Start delay of 20 sec, Time is: " +  now);
            TimeUnit.MICROSECONDS.sleep(delay);
            now = System.currentTimeMillis();
            //System.out.println("Stop delay of 20 sec, Time is: " +  now);
            // System.out.println("Delay "+delay);
        } catch(InterruptedException ie) {
            System.out.println("\n SpringLayout step InterruptedException \n");
        }
        moveNodes();

    }

    private void springEdges() {
        Dummy d = new Dummy();
        double delta = 0.0001;
        try {

            for(E e : getGraph().getEdges()) {
                Pair<V> endpoints = getGraph().getEndpoints(e);
                V v1 = endpoints.getFirst();
                V v2 = endpoints.getSecond();
                double vx, vy = 0;
                Point2D p1 = apply(v1);
                Point2D p2 = apply(v2);
                if(p1 == null || p2 == null) continue;
                vx = p1.getX() - p2.getX();
                vy = p1.getY() - p2.getY();
                if (vx==0) vx = Math.random()*100;
                if (vy==0) vy = Math.random()*100;
                double len = Math.sqrt(vx * vx + vy * vy);

                Integer spring =  springFunction.apply(d); // Needs to be within the forLoop!
                System.out.println("spring "+spring);



                // round from zero, if needed [zero would be Bad.].
                len = (len == 0) ? delta : len;

                double f = (spring.doubleValue()); // * (desiredLen.doubleValue() - len) / len;
                //System.out.printf(" spring %1.2f\n", spring.doubleValue());

                // the actual movement distance 'dx' is the force multiplied by the
                // distance to go.
                double dx = f * vx;
                double dy = f * vy;
                //System.out.printf("spring "+((GraphNode) v1).getNodeId()+" vx  %1.2f vy  %1.2f  f %1.2f \n",vx, vy,f);
                SpringVertexData v1D, v2D;
                v1D = springVertexData.getUnchecked(v1);
                v2D = springVertexData.getUnchecked(v2);
   /*System.out.printf("Edge old "+((GraphNode) v1).getNodeId()+" olddx  %1.2f olddy  %1.2f  dx  %1.2f dy  %1.2f\n"
                              ,v1D.edgedx, v1D.edgedy,dx, dy); */

                v1D.edgedx -= dx;
                v1D.edgedy -= dy;
                v2D.edgedx += dx;
                v2D.edgedy += dy;
                //System.out.printf("Edge new "+((GraphNode) v1).getNodeId()+" edx  %1.2f edy  %1.2f \n",v1D.edgedx, v1D.edgedy);
            }
        } catch(ConcurrentModificationException cme) {
            springEdges();
        }
    }

    private void calculateRepulsion() {
        Dummy d = new Dummy();

        try {
            for (V v : getGraph().getVertices()) {
                if (isLocked(v)) continue;
                Integer repel =  repulseFunction.apply(d);


                double f = repel.doubleValue()*100;

                SpringVertexData svd = springVertexData.getUnchecked(v);
                if(svd == null) continue;
                double dx = 0, dy = 0;
                for (V v2 : getGraph().getVertices()) {
                    if (v == v2 || !((GraphNode)v).getProcessModelId().equals(((GraphNode)v2).getProcessModelId())) continue;
                    Point2D p = apply(v);
                    Point2D p2 = apply(v2);
                    if(p == null || p2 == null) continue;
                    double vx = p.getX() - p2.getX();
                    double vy = p.getY() - p2.getY();
                    double distanceSq = p.distanceSq(p2);
                    double distance = (p.distance(p2)/10);
                    if (distanceSq == 0) {
                        dx += Math.random();
                        dy += Math.random();
                    } //else if (distanceSq < repulsion_range_sq) {
                    dx += f*f * vx / (distanceSq * distance);
                    dy += f*f * vy / (distanceSq * distance);
                    // }
                    /*System.out.printf("REP "+((GraphNode) v).getNodeId()+" rdx  %1.2f rdy  %1.2f \n",
                            svd.repulsiondx, svd.repulsiondy);
                    System.out.printf("REP "+((GraphNode) v).getNodeId()+"  dx  %1.2f  dy  %1.2f   dis  %1.2f \n",
                            dx, dy, distanceSq); */
                }
                svd.repulsiondx += dx;
                svd.repulsiondy += dy;
                /*System.out.printf("REP "+((GraphNode) v).getNodeId()+" rdx  %1.2f rdy  %1.2f \n",
                        svd.repulsiondx, svd.repulsiondy);*/
              /*  double dlen = dx * dx + dy * dy;
                if (dlen > 0) {
                    dlen = Math.sqrt(dlen) / 2;
                    svd.repulsiondx += dx / dlen;
                    svd.repulsiondy += dy / dlen;
                }*/
            }
        } catch(ConcurrentModificationException cme) {
            calculateRepulsion();
        }
    }

    private void moveNodes()
    {
        synchronized (getSize()) {
            Dummy d = new Dummy();
            double  decay;  // keep nodes with in a bound (used a lot in testing)
            double maxXSofar = 0;
            double minXSofar =  1000000;
            double maxYSofar = 0;
            double minYSofar =  1000000;
            try {
                //((GraphNode) v).getNodeId()

                for (V v : getGraph().getVertices()) {
                    double step =  stepFunction.apply(d);

                   // System.out.printf("StepO %1.2f \n",stepO);
                    if (isLocked(v)) continue;
                    SpringVertexData vd = springVertexData.getUnchecked(v);
                    if(vd == null) continue;
                    Point2D xyd = apply(v);  // gets location
                    double oldx = xyd.getX();
                    double oldy = xyd.getY();

                    if (((Double) vd.repulsiondx).isNaN()) vd.repulsiondx = 0.1;
                    if (((Double) vd.repulsiondy).isNaN()) vd.repulsiondy = 0.1;
                    vd.dx += vd.repulsiondx + vd.edgedx;
                    vd.dy += vd.repulsiondy + vd.edgedy;

                    //System.out.printf("**move "+((GraphNode) v).getNodeId()+" oldx %1.2f %1.2f oldy %1.2f %1.2f \n",
                    //  oldx, xyd.getX(), oldy,xyd.getY());
                    //System.out.printf("**REP "+((GraphNode) v).getNodeId()+" rep.dx %1.2f  rep.dy %1.2f \n",vd.repulsiondx, vd.repulsiondy);
                    //System.out.printf("**EDG "+((GraphNode) v).getNodeId()+" edg.dx %1.2f  edg.dy %1.2f \n",vd.edgedx, vd.edgedy);
                    double deltax = Math.max(-50, Math.min(50, vd.dx)); // keeps nodes from moving any faster than 5 per time unit
                    double deltay = Math.max(-50, Math.min(50, vd.dy));
                    double newx = oldx + (deltax/step);
                    double newy = oldy + (deltay/step);

               // Oscilation suppression

                    System.out.printf("*change "+((GraphNode) v).getNodeId()+" deltax %1.2f   at x %1.2f  \n",
                            newx-xyd.getX(), xyd.getX());

                    xyd.setLocation(newx, newy);

                    //System.out.printf("*moved "+((GraphNode) v).getNodeId()+" newx %1.2f %1.2f  newy %1.2f %1.2f \n",
                    //   newx,xyd.getX(), newy,xyd.getY());

                }


            } catch(ConcurrentModificationException cme) {
                moveNodes();
            }
        }
    }

    private static class SpringVertexData {
        private double edgedx;
        private double edgedy;
        private double repulsiondx;
        private double repulsiondy;

        /** movement speed, x */
        private double dx;
        /** movement speed, y */
        private double dy;

        private double oldx;
        private double oldy;

    }


    /**
     * Used for changing the size of the layout in response to a component's size.
     */
    public class SpringDimensionChecker extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            setSize(e.getComponent().getSize());
        }
    }

    /**
     * @return true
     */
    public boolean isIncremental() {
        return true;
    }

    /**
     * @return false
     */
    public boolean done() {
        return done;
    }

    //Means we can continue
    public void setDone(boolean val) {done = val;}

    /**
     * No effect.
     */
    public void reset() {
    }

    public class Dummy {

    }
}