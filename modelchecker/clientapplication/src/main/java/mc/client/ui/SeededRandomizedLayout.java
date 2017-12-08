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
import java.util.HashMap;
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
    Integer counter = 0;

    HashMap<String, Integer> Spacing = new HashMap<>();
    HashMap<String, Integer> processModelsPreviousSpacing = new HashMap<>();


    /**
     * Creates an instance with the specified size which uses the current time
     * as the random seed.
     * @param d the size of the layout area
     */
    public SeededRandomizedLayout(Dimension d) {
        this.d = d;
        this.random = new Random(42);
    }

    public Point2D apply(V v) {
        if(v instanceof GraphNode) {

            if(!processModelsPreviousSpacing.containsKey(((GraphNode) v).getAutomata())) { // If we are adding a new process model
                processModelsPreviousSpacing.put(((GraphNode) v).getAutomata(), counter);
                Spacing.put(Integer.toString(v.hashCode()), counter);
                counter += 300;
            } else if(!Spacing.containsKey(Integer.toString(v.hashCode()))){
                Integer previousSpacing = processModelsPreviousSpacing.get(((GraphNode) v).getAutomata());

                processModelsPreviousSpacing.put(((GraphNode) v).getAutomata(),previousSpacing+50);
                Spacing.put(Integer.toString(v.hashCode()), previousSpacing+50);
            }



            return new Point2D.Double(Spacing.get(Integer.toString(v.hashCode())), 100);

        } else {
            return new Point2D.Double(0, 0);

        }

    }
}
