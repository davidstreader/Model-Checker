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

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import mc.client.ui.SettingsController;

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
    private Function<? super E, Integer> lengthFunction;
    private Function<? super V, Integer> repulseFunction;
    private Function<? super E, Integer> springFunction;
    private Function<? super V, Integer> speedFunction;
    private boolean done = false;
    private int aneal = 100;
    private SettingsController settings;
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
    public SpringlayoutBase(Graph<V,E> g, SettingsController s) {
        this(g, (Function<E,Integer>)Functions.<Integer>constant(120),
                (Function<V,Integer>)Functions.<Integer>constant(120),
                (Function<V,Integer>)Functions.<Integer>constant(10),
                (Function<E,Integer>)Functions.<Integer>constant(10),s);
    }

    /**
     * Constructor for a SpringLayout for a raw graph with associated component.
     *
     * @param g the graph on which the layout algorithm is to operate
     * @param length_function provides a length for each edge
     */
    public SpringlayoutBase(Graph<V,E> g,
                            Function<? super E, Integer> length_function,
                            Function<? super V, Integer> repulse_function,
                            Function<? super V, Integer> speed_function,
                            Function<? super E, Integer> spring_function,SettingsController s)
    {
        super(g);
        this.lengthFunction = length_function;
        this.repulseFunction = repulse_function;
        this.speedFunction = speed_function;
        this.springFunction = spring_function;
    }
    /**
     * @return the current value for the stretch parameter
     */
    public double getStretch() {
        return stretch;
    }
    public void setAneal(int a){aneal = a;}

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
    public void step() {
        try {
            for(V v : getGraph().getVertices()) {
                SpringVertexData svd = springVertexData.getUnchecked(v);
                if (svd == null) {
                    continue;
                }
                svd.dx /= 4;
                svd.dy /= 4;
                svd.edgedx = svd.edgedy = 0;
                svd.repulsiondx = svd.repulsiondy = 0;

            }
        } catch(ConcurrentModificationException cme) {
            step();
        }

        relaxEdges();
        calculateRepulsion();
        moveNodes();
        if (aneal%10 == 0) {
            //System.out.println("aneal "+aneal);
            }
        aneal--;
    }

    private void relaxEdges() {
        try {
            for(E e : getGraph().getEdges()) {
                Pair<V> endpoints = getGraph().getEndpoints(e);
                V v1 = endpoints.getFirst();
                V v2 = endpoints.getSecond();
                if (isLocked(v1)) continue;
                Point2D p1 = apply(v1);
                Point2D p2 = apply(v2);
                if(p1 == null || p2 == null) continue;
                double vx = p1.getX() - p2.getX();
                double vy = p1.getY() - p2.getY();
                double len = Math.sqrt(vx * vx + vy * vy);


                double desiredLen = lengthFunction.apply(e);
                double spring = (springFunction.apply(e)/10);

                // round from zero, if needed [zero would be Bad.].
                len = (len == 0) ? .0001 : len;

                double f = spring * (desiredLen - len) / len;

                //f = f * Math.pow(stretch, (getGraph().degree(v1) + getGraph().degree(v2) - 2));

                // the actual movement distance 'dx' is the force multiplied by the
                // distance to go.
                double dx = f * vx;
                double dy = f * vy;
                SpringVertexData v1D, v2D;
                v1D = springVertexData.getUnchecked(v1);
                v2D = springVertexData.getUnchecked(v2);

                v1D.edgedx += dx;
                v1D.edgedy += dy;
                v2D.edgedx += -dx;
                v2D.edgedy += -dy;
               System.out.println(String.format("**Edge"+((GraphNode) v1).getNodeId()+
                        " dx %1.2f  dy %1.2f ",v1D.edgedx ,v1D.edgedy));

            }
        } catch(ConcurrentModificationException cme) {
            relaxEdges();
        }
    }

    private void calculateRepulsion() {
        try {
            for (V v : getGraph().getVertices()) {
                if (isLocked(v)) continue;
                System.out.println("Repulse "+((GraphNode) v).getNodeId()  );
                double repulse = (repulseFunction.apply(v) *100);
                SpringVertexData svd = springVertexData.getUnchecked(v);
                if(svd == null) continue;
                double dx = 0, dy = 0;
                //System.out.printf("rep %1.2f \n", repulse);
                for (V v2 : getGraph().getVertices()) {
                    if (v == v2 || !((GraphNode)v).getProcessModelId().equals(((GraphNode)v2).getProcessModelId())) continue;
                    Point2D p = apply(v);
                    Point2D p2 = apply(v2);
                    if(p == null || p2 == null) continue;
                    double vx = p.getX() - p2.getX();
                    double vy = p.getY() - p2.getY();
                    double distanceSq = p.distanceSq(p2);
                    if (distanceSq == 0) {
                        dx += Math.random()*100;
                        dy += Math.random()*100;
                    } else if (distanceSq < repulsion_range_sq) {
                        //double factor = 3;

                        dx += repulse * vx / distanceSq;
                        dy += repulse * vy / distanceSq;
                    }
                }
                svd.repulsiondx += dx;
                svd.repulsiondy += dy;
                System.out.printf("Repulse "+((GraphNode) v).getNodeId() +" x %1.2f  y %1.2f \n", svd.repulsiondx, svd.repulsiondx);
             /*   double dlen = dx * dx + dy * dy;
                if (dlen > 0) {
                    dlen = Math.sqrt(dlen) / 2;
                    svd.repulsiondx += dx / dlen;
                    svd.repulsiondy += dy / dlen;
                } */
              //  System.out.println(String.format("**Node "+((GraphNode) v).getNodeId()+"  dx %1.2f",dx));
            }
        } catch(ConcurrentModificationException cme) {
            calculateRepulsion();
        }
    }

    private void moveNodes()
    {
        synchronized (getSize()) {
            try {
                double speed = speedFunction.apply(getGraph().getVertices().iterator().next());
                for (V v : getGraph().getVertices()) {
                    if (isLocked(v)) continue;
                    SpringVertexData vd = springVertexData.getUnchecked(v);
                    if(vd == null) continue;
                    Point2D xyd = apply(v);

                    //slow down osications
                    double newdx = vd.repulsiondx + vd.edgedx;
                    if ((newdx> 0 && vd.dx < 0 )|| (newdx< 0 && vd.dx > 0)) newdx= newdx/2;
                    vd.dx += newdx;
                    double newdy = vd.repulsiondy + vd.edgedy;
                    if ((newdy> 0 && vd.dy < 0 )|| (newdy< 0 && vd.dy > 0)) newdy= newdy/2;
                    vd.dx += newdx;
                    vd.dy += newdy;
                    //respond to the  configured speed
                    if (vd.dx>speed) System.out.println(String.format("over speed %1.2f", vd.dx));
                    // keeps nodes from moving any faster than "speed" per time unit
                    xyd.setLocation(xyd.getX()+Math.max(-speed, Math.min(speed, vd.dx)),
                            xyd.getY()+Math.max(-speed, Math.min(speed, vd.dy)));
                   /*System.out.println(String.format("**SO "+((GraphNode) v).getNodeId()+
                                    "  vd.repulsiondx %1.2f  vd.edgedx %1.2f",
                                       vd.repulsiondx,vd.edgedx)); */
                    System.out.printf("Moving "+((GraphNode) v).getNodeId() +" x %1.2f  y %1.2f \n" , vd.dx, vd.dy );

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
}