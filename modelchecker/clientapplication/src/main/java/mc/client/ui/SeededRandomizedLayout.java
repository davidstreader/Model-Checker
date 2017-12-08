/*
 * Created on Jul 19, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package mc.client.ui;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Date;
import java.util.Random;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import mc.client.graph.GraphNode;

/**
 * Provides a random vertex location within the bounds of the Dimension property.
 * This provides a random location for unmapped vertices
 * the first time they are accessed.
 *
 * @author Tom Nelson
 *
 * @param <V> the vertex type
 */
public class SeededRandomizedLayout<V> implements Function<V,Point2D> {
    Dimension d;
    Random random;
    int xSpacing = 0;
    String lastAutomataLabel;


    /**
     * Creates an instance with the specified size which uses the current time
     * as the random seed.
     * @param d the size of the layout area
     */
    public SeededRandomizedLayout(Dimension d) {
        this(d, 42);
    }

    /**
     * Creates an instance with the specified dimension and random seed.
     * @param d the size of the layout area
     * @param seed the seed for the internal random number generator
     */
    public SeededRandomizedLayout(final Dimension d, long seed) {
        this.d = d;
        this.random = new Random(seed);
    }

    public Point2D apply(V v) {
        if(v instanceof GraphNode) {



            return new Point2D.Double(random.nextDouble() * d.width+xSpacing, random.nextDouble() * d.height);

        } else {
            return new Point2D.Double(random.nextDouble() * d.width, random.nextDouble() * d.height);

        }

    }
}
