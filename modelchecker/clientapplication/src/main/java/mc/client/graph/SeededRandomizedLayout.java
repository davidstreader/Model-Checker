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
package mc.client.graph;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import java.util.HashMap;
import java.util.Random;

import com.google.common.base.Function;

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

    private Dimension screenDimensions;

    private Integer counter = 100;

    private  HashMap<String, Integer> processModelsPreviousSpacing = new HashMap<>();


    public SeededRandomizedLayout() {}

    /**
     *  Creates an instance that sets up the size of the screen
     *
     * @param d_ the size of the layout area
     */
    public SeededRandomizedLayout(Dimension d_) {
        this.screenDimensions = d_;
    }

    /**
     * Dimensions of the visible window. So we know how much space we have
     * @param d_
     */
    public void setDimensions(Dimension d_) {
        this.screenDimensions = d_;
    }

    public Point2D apply(V v) {
        if(v instanceof GraphNode) {
            Random random = new Random(v.hashCode());
            if(!processModelsPreviousSpacing.containsKey(((GraphNode) v).getProcessModelId())) { // If we are adding a new process model
                processModelsPreviousSpacing.put(((GraphNode) v).getProcessModelId(), counter);
                counter += 200;
             }

            int currentSpacing = processModelsPreviousSpacing.get(((GraphNode) v).getProcessModelId());
            return new Point2D.Double(((currentSpacing-200 < 0 )? 0: currentSpacing-200)+random.nextDouble() * currentSpacing, 50+random.nextDouble() * screenDimensions.height);


        } else {return null;}

    }
}
