(function() {
  'use strict';
  Polymer({
    is: 'process-visualisation',
    properties: {
      /**
       * The name of the automata.
       */
      name: {
        type: String,
        value: ''
      },

      graph: {
        type: Object,
        value: {}
      }
    },
    attached: function(){
      Polymer.dom.flush();
      this.displayedGraphs = {};
      const _this = this;
      this.graphMap = {};
      app.$.selector.locked = false;
      this.cy = cytoscape({
        container: document.getElementById('svg-parent'),

        layout: {
          name: 'cose-bilkent'
        },
        style: getCytoscapeStyle(),
      });
      this.cy.on("layoutstop",()=>{
        this.rendering = false;
        if (this.graphsToAdd.length > 0) {
          const graph = this.graphsToAdd.pop();
          this.addGraph(graph.name,graph.hidden);
        }
      });
      document.addEventListener('addProcess', function(e){
        app.$.console.log("Starting Render. While rendering, you can not use the editor tab.");
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        _this.addGraph(app.$.selector.getSelectedName(), app.$.selector.hideInterrupts);
      });
      document.addEventListener('clearProcess',function(e) {
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        //Reset the explosion status since the last exploded item is now gone
        app.$.selector._explosionLabel = "Explode to process";
        if (_this.exploded)
          _this.fire('explode',false);
        _this.cy.remove(_this.cy.elements())
        _this.displayedGraphs = {};
        _this.saveChanges();
      });
      //Espand the drawing area to the size of the screen when zooming
      window.addEventListener('resize', function(){
        _this.rescale();
      });
      document.addEventListener('addAll', function(){
        app.$.console.log("Starting Render. While rendering, you can not use the editor tab.");
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        _.each(app.get("automata.values"),graph => _this.addGraph(graph.id,app.$.selector.hideInterrupts));
      });

    },
    removeGraph: function(cell){
      //If we are currently rendering, ignore any events
      if (this.rendering) return;
      //Reset the explosion status since the last exploded item is now gone
      app.$.selector._explosionLabel = "Explode to process";
      if (this.exploded)
        this.fire('explode',false);
      //TODO: remove just wont work like this.
      this.saveChanges();
    },
    graphsToAdd: [],
    addGraph: function(name, hidden) {
      console.log(this.rendering);
      if (this.rendering) {
        this.graphsToAdd.push({name: name, hidden: hidden});
        return;
      }
      this.rendering = true;
      app.$.console.clear();
      //app.$.selector.locked = this.rendering = true;
      let graph = _.findWhere(app.get("automata.values"), {id: name});
      switch (graph.type) {
        case "automata": this.addAutomata(graph);
      }
    },
    /**
     * Redraw the automata.
     */
    redraw: function() {
      $("#process-name-selector")[0].contentElement.selected = null;
      this.automata = this.graphMap;
      this.fire('process-visualisation-rendered');
      this.rescale();
    },
    saveChanges: function() {
      if (app.willSaveCookie && !this.exploded)
        localStorage.setItem("layout", JSON.stringify(this.jgraph.toJSON()));
    },
    loadJSON: function(layout) {
      this.layout = layout;
      if (app.$.maintabs.selected === 1) {
        this.redraw();
      }
    },
    addAutomata: function(graph) {
      this.cy.add({
        group: "nodes",
        data: { id: graph.id, label:graph.id },
      });


      for (let nodeId in graph.nodeMap) {
        const node = graph.nodeMap[nodeId];
        this.cy.add({
          group: "nodes",
          data: { id: nodeId, label:node.metaData.label, parent: graph.id },
          position: { x: 10+Math.random(), y: 10+Math.random()},
        });
      }
      for (let edgeId in graph.edgeMap) {
        const edge = graph.edgeMap[edgeId];
        this.cy.add({
          group: "edges",
          data: { id: edgeId, label:edge.label, source: edge.from, target: edge.to, parent: graph.id},
        });
      }
       this.cy.collection('[parent="'+graph.id+'"]').union(this.cy.collection('[id="'+graph.id+'"]')).layout({
         name: 'cose-bilkent',
         fit: false,
         // Padding on fit
         padding: 10,
         // Whether to enable incremental mode
         randomize: true,
         // Node repulsion (non overlapping) multiplier
         nodeRepulsion: 4500,
         // Ideal edge (non nested) length
         idealEdgeLength: 100,
         // Divisor to compute edge forces
         edgeElasticity: 0.45,
         // Nesting factor (multiplier) to compute ideal edge length for nested edges
         nestingFactor: 0.1,
         // Gravity force (constant)
         gravity: 0.25,
         // Maximum number of iterations to perform
         numIter: 2500,
         // For enabling tiling
         tile: false,
         // Type of layout animation. The option set is {'during', 'end', false}
         animate: 'end',
         // Represents the amount of the vertical space to put between the zero degree members during the tiling operation(can also be a function)
         tilingPaddingVertical: 10,
         // Represents the amount of the horizontal space to put between the zero degree members during the tiling operation(can also be a function)
         tilingPaddingHorizontal: 10,
         // Gravity range (constant) for compounds
         gravityRangeCompound: 1.5,
         // Gravity force (constant) for compounds
         gravityCompound: 1.0,
         // Gravity range (constant)
         gravityRange: 3.8});
    },
    rescale: function() {
      if (this.exploded) {
        return;
      }
      let screenHeight = $(this.cy.container()).parent().parent().height();
      $(this.cy.container()).height(screenHeight);
    },
  });
})();
