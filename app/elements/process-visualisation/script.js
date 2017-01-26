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
      this.displayedGraphs = [];
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
      this.cy.cxtmenu( {
        openMenuEvents: 'cxttapstart taphold',
        menuRadius: 100, // the radius of the circular menu in pixels
        selector: '[isParent]', // elements matching this Cytoscape.js selector will trigger cxtmenus
        commands: [ // an array of commands to list in the menu or a function that returns the array
          {
            content: 'Explode', // html/text content to be displayed in the menu
            select: function (ele) { // a function to execute when the command is selected
              _this.cy.fit(ele);
            }
          },
          {
            content: 'Remove', // html/text content to be displayed in the menu
            select: function (ele) { // a function to execute when the command is selected
              _this.removeGraph(ele) // `ele` holds the reference to the active element
            }
          }
        ]
      } );
      this.cy.panzoom();
      this.cy.on("layoutstop",()=>{
        let y = 0;
        const cur = this.displayedGraphs[this.displayedGraphs.length-1];
        if (cur === undefined) return;
        if (this.displayedGraphs.length > 1) {
          const prev = this.displayedGraphs[this.displayedGraphs.length-2];
          //Work out the bottom of the last element, and then add a 20px buffer, plus some space
          //For interrupts
          y = prev.parent.position("y")+(prev.parent.height()/2)+20+(10*cur.interrupts);
        }
        //Move all descendants, and also add some padding to the left of interrupts to make them line up correctly.
        cur.parent.descendants().positions((i,node)=>{
          return {y: node.position("y")+y,x: node.position("x")+cur.interrupts*2}
        });
        this.rendering = false;
        if (this.graphsToAdd.length > 0) {
          const graph = this.graphsToAdd.pop();
          this.addGraph(graph.name,graph.hidden);
        }
        _this.saveChanges();
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
        _this.displayedGraphs = [];
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
      this.cy.remove(cell);
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
      this.convertAndAddGraph(graph,name,hidden);
    },
    /**
     * Redraw the automata.
     */
    redraw: function() {
      $("#process-name-selector")[0].contentElement.selected = null;
      this.automata = this.graphMap;
      if (!this.loaded) {
        this.loadJSON()
      }
      this.fire('process-visualisation-rendered');
      this.rescale();
    },
    saveChanges: function() {
      if (app.willSaveCookie && !this.exploded)
        localStorage.setItem("layout", JSON.stringify(this.cy.json()));
    },
    loadJSON: function() {
      if (localStorage.getItem("layout") !== null)
        this.cy.json(JSON.parse(localStorage.getItem("layout")));
      this.loaded = true;
    },
    convertAndAddGraph: function(graph,id,hidden) {
      const glGraph = convertGraph(graph,id,hidden);
      const parent = {
        group: "nodes",
        data: { id: id, label:graph.id, isParent: true },
        position: { x: 10, y: 10},
      };
      this.cy.add(parent);
      this.displayedGraphs.push({parent: this.cy.elements('node[id="'+id+'"]')[0], interrupts: (glGraph.interrupts || []).length});
      for (let nodeId in glGraph.nodes) {
        const node = glGraph.nodes[nodeId];
        node.position = { x: 10+Math.random(), y: 10+Math.random()},
          this.cy.add(node);
      }
      for (let edgeId in glGraph.edges) {
        const edge = glGraph.edges[edgeId];
        this.cy.add(edge);
      }

      this.cy.collection('[parent="'+graph.id+'"]').union(this.cy.collection('[id="'+graph.id+'"]')).layout({
        name: 'cose-bilkent',
        fit: false,
        // Whether to enable incremental mode
        randomize: true,
        // // Node repulsion (non overlapping) multiplier
        // nodeRepulsion: 4500,
        // // Ideal edge (non nested) length
        // idealEdgeLength: 100,
        // // Divisor to compute edge forces
        // edgeElasticity: 0.45,
        // // Nesting factor (multiplier) to compute ideal edge length for nested edges
        // nestingFactor: 0.1,
        // // Gravity force (constant)
        // gravity: 0.25,
        // // Maximum number of iterations to perform
        // numIter: 2500,
        // // For enabling tiling
        // tile: false,
        // // Type of layout animation. The option set is {'during', 'end', false}
        // animate: 'end',
        // // Represents the amount of the vertical space to put between the zero degree members during the tiling operation(can also be a function)
        // tilingPaddingVertical: 10,
        // // Represents the amount of the horizontal space to put between the zero degree members during the tiling operation(can also be a function)
        // tilingPaddingHorizontal: 10,
        // // Gravity range (constant) for compounds
        // gravityRangeCompound: 1.5,
        // // Gravity force (constant) for compounds
        // gravityCompound: 1.0,
        // // Gravity range (constant)
        // gravityRange: 3.8
      });
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
'use strict';

/*!
 Event object based on jQuery events, MIT license
 https://jquery.org/license/
 https://tldrlegal.com/license/mit-license
 https://github.com/jquery/jquery/blob/master/src/event.js
 */

var Event2 = function( src, props ){
  // Allow instantiation without the 'new' keyword
  if( !(this instanceof Event2) ){
    return new Event2( src, props );
  }
  // Event object
  if( src && src.type ){
    this.originalEvent = src;
    this.type = src.type;

    // Events bubbling up the document may have been marked as prevented
    // by a handler lower down the tree; reflect the correct value.
    this.isDefaultPrevented = ( src.defaultPrevented ) ? returnTrue : returnFalse;

    // Event type
  } else {
    this.type = src;
  }

  // Put explicitly provided properties onto the event object
  if( props ){
    // util.extend( this, props );

    // more efficient to manually copy fields we use
    this.type = props.type !== undefined ? props.type : this.type;
    this.cy = props.cy;
    this.cyTarget = props.cyTarget;
    this.cyPosition = props.cyPosition;
    this.cyRenderedPosition = props.cyRenderedPosition;
    this.namespace = props.namespace;
    this.layout = props.layout;
    this.data = props.data;
    this.message = props.message;
  }

  // Create a timestamp if incoming event doesn't have one
  this.timeStamp = src && src.timeStamp || Date.now();
};

function returnFalse(){
  return false;
}

function returnTrue(){
  return true;
}

