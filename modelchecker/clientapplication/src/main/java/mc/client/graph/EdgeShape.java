/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on March 10, 2005
 */
package mc.client.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

import java.awt.geom.RectangularShape;
import java.util.ArrayList;


import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;

import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeTransformer;



/**
 * An interface for decorators that return a
 * <code>Shape</code> for a specified edge.
 *
 * All edge shapes must be defined so that their endpoints are at
 * (0,0) and (1,0). They will be scaled, rotated and translated into
 * position by the PluggableRenderer.
 *
 * Modified 15/12/2017 to automatically adjust from line to curve to stop overlapping of labels/edges
 *
 * @author Tom Nelson, Jordan Smith
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class EdgeShape<V,E> {
    private static final Line2D LINE = new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
    private static final QuadCurve2D QUAD_CURVE = new QuadCurve2D.Float();
    private static final Ellipse2D ELLIPSE = new Ellipse2D.Float(-.5f, -.5f, 1, 1);

    protected final Graph<V, E> graph;

    /**
     * A convenience instance for other edge shapes to use for self-loop edges
     * where parallel instances will not overlay each other.
     */
    protected final Loop loop;

    /**
     * A convenience instance for other edge shapes to use for self-loop edges
     * where parallel instances overlay each other.
     */
    protected final SimpleLoop simpleLoop;

    public EdgeShape(Graph<V, E> g) {
        this.graph = g;

        this.loop = new Loop();
        this.simpleLoop = new SimpleLoop();
    }

    private Shape getLoopOrNull(E e) {
        return getLoopOrNull(e, loop);
    }

    private Shape getLoopOrNull(E e, Function<? super E, Shape> loop) {
        Pair<V> endpoints = graph.getEndpoints(e);
        checkNotNull(endpoints);
        boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
        if (isLoop) {
            return loop.apply(e);
        }
        return null;
    }

    public static <V, E> EdgeShape<V, E>.Line line(Graph<V, E> graph) {
        return new EdgeShape<V, E>(graph).new Line();
    }

    public static <V, E> EdgeShape<V, E>.QuadCurve quadCurve(Graph<V, E> graph) {
        return new EdgeShape<V, E>(graph).new QuadCurve();
    }

    public static <V, E> EdgeShape<V, E>.LineCurve mixedLineCurve(Graph<V, E> graph) {
        return new EdgeShape<V, E>(graph).new LineCurve();
    }

    /**
     * An edge shape that renders as a straight line between
     * the vertex endpoints.
     */
    public class Line implements Function<E, Shape> {
        /**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
        public Shape apply(E e) {
            Shape loop = getLoopOrNull(e);
            return loop == null
                    ? LINE
                    : loop;
        }
    }

    private int getIndex(E e, EdgeIndexFunction<V, E> edgeIndexFunction) {
        return edgeIndexFunction == null
                ? 1
                : edgeIndexFunction.getIndex(graph, e);
    }



    /**
     * An edge shape that renders as a QuadCurve between vertex
     * endpoints.
     */
    public class QuadCurve extends ParallelEdgeShapeTransformer<V,E> {
        @Override
        public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.edgeIndexFunction = parallelEdgeIndexFunction;
            loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
        }

        /**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
        public Shape apply(E e) {
            Shape edgeShape = getLoopOrNull(e);
            if (edgeShape != null) {
                return edgeShape;
            }

            int index = getIndex(e, edgeIndexFunction);

            float controlY = control_offset_increment +
                    control_offset_increment * index;
            QUAD_CURVE.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
            return QUAD_CURVE;
        }
    }

    public class LineCurve extends ParallelEdgeShapeTransformer<V,E> {


        public Shape apply(E e) {

            ArrayList<V> Vertexes = new ArrayList<>(graph.getIncidentVertices(e));
            if(Vertexes.size() == 2) {
                V first = Vertexes.get(0);
                V second = Vertexes.get(1);


                //If the second vertex appears in any other edges from the first node then make it a curve
                ArrayList<E> firstNodeEdges = new ArrayList<>(graph.getOutEdges(first));
                for(E edge : firstNodeEdges) {
                    if(edge.equals(e))
                        continue;

                    if(graph.getIncidentVertices(edge).contains(second)) {

                        return new QuadCurve().apply(e);
                    }
                }

                ArrayList<E> secondNodeEdges = new ArrayList<>(graph.getOutEdges(second));
                for(E edge : secondNodeEdges) {
                    if(edge.equals(e))
                        continue;

                    if(graph.getIncidentVertices(edge).contains(first)) {

                        return new QuadCurve().apply(e);
                    }
                }

                return new Line().apply(e);

            }


            return null; // If there arent two vertexs to an edge then make it all burn down.
        }

    }


    /**
     * An edge shape that renders as a loop with its nadir at the center of the
     * vertex. Parallel instances will overlap.
     *
     * @author Tom Nelson
     */
    public class SimpleLoop extends ParallelEdgeShapeTransformer<V,E> {
        public Shape apply(E e) {
            return ELLIPSE;
        }
    }

    private Shape buildFrame(RectangularShape shape, int index) {
        float x = -.5f;
        float y = -.5f;
        float diam = 1.f;
        diam += diam * index/2;
        x += x * index/2;
        y += y * index/2;

        shape.setFrame(x, y, diam, diam);

        return shape;
    }

    /**
     * An edge shape that renders as a loop with its nadir at the
     * center of the vertex. Parallel instances will not overlap.
     */
    public class Loop extends ParallelEdgeShapeTransformer<V,E> {
        public Shape apply(E e) {
            return buildFrame(ELLIPSE, getIndex(e, edgeIndexFunction));
        }
    }


}


