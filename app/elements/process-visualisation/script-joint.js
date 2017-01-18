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
    ready: function(){
      this.displayedGraphs = {};
      const _this = this;
      this.graphMap = {};
      this.jgraph = new joint.dia.Graph();
      //Create a paper element, pointing to #svg-parent, with restrictions on moving elements out of bounds
      this.paper = new joint.dia.Paper({
        el: this.$['svg-parent'],
        gridSize: 1,
        model: this.jgraph,
        restrictTranslate: true,
        async: true
      });
      const paper = this.paper;
      //Process selector listener
      document.addEventListener('change-process', function(e){
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        //When we swap, its much easier to just restore then explode again.
        if (_this.exploded) {
          _this.fire('explode',false);
          _this.fire('explode',true);
          return;
        }
        //Find the bounding box of the selected processes parent, and scroll to it
        //The scroll bar is inside .process-display
        const nodeBBox = _this.displayedGraphs[e.detail.id].getBBox();
        const scale = V(paper.viewport).scale();
        $(".process-display").stop().animate({scrollTop:nodeBBox.origin().y*scale.sy}, '100', 'swing');
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
        _this.jgraph.resetCells();
        _this.displayedGraphs = {};
        _this.saveChanges();
      });
      //Espand the drawing area to the size of the screen when zooming
      window.addEventListener('resize', function(){
        _this.rescale();
      });
      document.addEventListener('explode', function(e){
        _this.explode(e.detail);
      });
      document.addEventListener('addAll', function(){
        app.$.console.log("Starting Render. While rendering, you can not use the editor tab.");
        //If we are currently rendering, ignore any events
        if (_this.rendering) return;
        _.each(app.get("automata.values"),graph => _this.addGraph(graph.id,app.$.selector.hideInterrupts));
      });
      this.paper.on('render:done', ()=> {
        app.$.selector.locked = this.rendering = false;
        //Jointjs has a bug where you can only set attributes.
        //To combat this, we can search for all titles and map their innerhtml to the text attribute.
        _.each($(".title"),title=> {
          title.innerHTML = _this.jgraph.getCell(title.parentElement.getAttribute("model-id")).get("tooltip") || '';
        });
        if (_this.graphsToAdd.length > 0) {
          const graph = _this.graphsToAdd.pop();
          _this.addGraph(graph.name,graph.hidden);
        } else {
          app.$.console.log("Finished rendering!");
          $(".disableOnRender")[0].disabled = false;
        }
      });
      this.paper.on('cell:pointermove', function(cell) {
        cell = cell.model;
        const parentId = cell.get('parent');
        if (!parentId) {
          _this.saveChanges();
          return;
        }
        adjustVertices(_this.jgraph,cell);
        _this.saveChanges();
      });
      this.jgraph.on('change:size', function(cell) {
        //Exploded diagrams are limited by changing the dimensions of the paper
        if (_this.exploded || _this.rendering) {
          return;
        }
        const parentId = cell.get('parent');
        if (!parentId) return;
        const parent = _this.jgraph.getCell(parentId);
        resizeParentToFit(parent);
        _this.saveChanges();
      });
      this.jgraph.on('change:position', function(cell) {
        //Exploded diagrams are limited by changing the dimensions of the paper
        //Don't update the size of the parent when the interrupt updates
        if (_this.exploded || _this.rendering) {
          _this.saveChanges();
          return;
        }
        const parentId = cell.get('parent');
        if (!parentId) return;
        const parent = _this.jgraph.getCell(parentId);
        resizeParentToFit(parent);
        _this.saveChanges();
      });
      if (app.willSaveCookie)
        localStorage.setItem("layout", JSON.stringify(_this.jgraph.toJSON()));
    },
    explode: function(exploded, cell) {
      let name;
      if (!cell) {
        name = this.lastOptions.key;
      } else {
        cell = this.jgraph.getCell($(cell).parent().parent().parent().attr("model-id"));
        name = cell.get("graphID");
      }
      const processDisplay = $(".process-display");
      this.exploded = exploded;
      app.$.selector.exploded = exploded;
      const paperWidth = this.paper.$el.width();
      const paperHeight = this.paper.$el.height();
      const _this = this;
      const paper = _this.paper;
      _.each(this.displayedGraphs,function (parentNode, key) {
        const {x: minX, y: minY} = parentNode.getBBox().origin();
        const {width,height} = parentNode.getBBox();
        const scaleX = paperWidth / width;
        const scaleY = paperHeight / height;
        if (_this.exploded) {
          app.$.selector.locked = true;
          //Hide the parent node so we don't see a box around the element
          get$Cell(parentNode).hide();
          if(!cell.name) get$Cell(cell).hide();
          if (key == name) {
            _this.lastOptions = {
              scrollTop: processDisplay.scrollTop(),
              origin: paper.options.origin,
              scale: V(paper.viewport).scale(),
              key: key
            };
            paper.setOrigin(0, 0);
            paper.scale(Math.min(scaleX, scaleY), Math.min(scaleX, scaleY), 0, 0);
            processDisplay.scrollTop(0);
            _this.paper.setDimensions(paperWidth, height * Math.min(scaleX, scaleY));
            parentNode.translate(-minX,-minY);
            parentNode.minX = minX;
            parentNode.minY = minY;
          } else {
            _.each(collectDeepEmbedded(parentNode),node => get$Cell(node).hide());
          }
        } else {
          app.$.selector.locked = false;
          //Show the element that was hidden above
          get$Cell(parentNode).show();
          if (key == _this.lastOptions.key) {
            paper.scale(_this.lastOptions.scale.sx, _this.lastOptions.scale.sy, 0, 0);
            paper.setOrigin(_this.lastOptions.origin.x, _this.lastOptions.origin.y);
            processDisplay.scrollTop(_this.lastOptions.scrollTop);
            parentNode.translate(parentNode.minX, parentNode.minY);
          }
          _.each(collectDeepEmbedded(parentNode),node => {
            if (node.attributes.type === 'InterruptEmbedNode') {
              return;
            }
            get$Cell(node).show()
          });

          _this.rescale();
        }
      });
      //We might have hidden interrupted states, so lets show them
      _.each(collectDeepEmbedded(_this.displayedGraphs[name]),cell=>{
        if (cell.attributes.type==='Buttons') {
          if (exploded) get$Cell(cell).hide();
          else get$Cell(cell).show();
          return;
        }
        get$Cell(cell).show();
        if (cell.attributes.name === 'InterruptParentNode') return;
        //Update links as they have a bug where they point to incorrect locations after exploding.
        var vertices = cell.get('vertices');
        if (vertices && vertices.length) {
          var newVertices = [];
          _.each(vertices, function(vertex) {
            newVertices.push({ x: vertex.x+1, y: vertex.y});
          });
          cell.set('vertices', newVertices);
          cell.set('vertices', vertices);
        }
      });
    },
    removeGraph: function(cell){
      //If we are currently rendering, ignore any events
      if (this.rendering) return;
      //Reset the explosion status since the last exploded item is now gone
      app.$.selector._explosionLabel = "Explode to process";
      if (this.exploded)
        this.fire('explode',false);
      cell = this.jgraph.getCell($(cell).parent().parent().parent().attr("model-id"));
      const name = cell.get("graphID");
      cell = this.jgraph.getCell(cell.get("parent"));
      //Remove children
      this.jgraph.removeCells(cell.getEmbeddedCells());
      //Remove parent
      this.jgraph.removeCells([cell]);
      delete this.displayedGraphs[name];
      this.saveChanges();
    },
    graphsToAdd: [],
    addGraph: function(name, hidden) {
      if (this.rendering) {
        this.graphsToAdd.push({name: name, hidden: hidden});
        return;
      }
      $(".disableOnRender")[0].disabled = true;
      app.$.console.clear();
      app.$.console.log("Rendering: "+name);
      app.$.console.log("While rendering, you can not use the editor tab.");
      app.$.selector.locked = this.rendering = true;
      //construct the graph first if it doesnt exist or has changed
      constructGraphs(this.graphMap,name, hidden, ()=>{
        //Work out the bottom corner of the lowest element
        let maxY = 0;
        _.each(this.displayedGraphs,graph => maxY = Math.max(maxY,graph.getBBox().corner().y));
        //Find a name that isnt in use
        let oldName = name;
        let tmpIdx = 1;
        while (this.displayedGraphs[name]) {
          name = oldName+(tmpIdx++);
        }
        if (hidden) oldName +=".hidden";
        //Assign that name to the old label
        this.graphMap[oldName].label.attributes.attrs.text.text = name;

        //Clone the process into cells, also cloning all children
        let cells = this.graphMap[oldName].parentNode.clone({deep: true});
        let graph = {};
        //We need to make sure all nodes are rendered before edges.
        cells.sort((a,b) => a.attributes.type==="fsa.Arrow"?1:-1);
        //All we really care about is the parent at this point
        graph.parentNode = cells[0];
        graph.id = name;
        this.displayedGraphs[name] = cells[0];
        this.jgraph.addCells(cells);
        //Note, we need to add the cells before translating the cell to its correct position
        graph.parentNode.translate(0,maxY);
        graph.parentNode.set("graphId",graph.id);
        this.rescale();
        const _this = this;
        const directEmbeds = _.filter(cells[0].getEmbeddedCells(),cell=>cell.attributes.type=='InterruptLabel');
        //Each interrupt pushes the entire graph 25 px to the left
        let pad = 0;
        cells.forEach(cell => {
          if (directEmbeds.indexOf(cell) !== -1) {
            return;
          }
          if (cell.attributes.type == "InterruptParentNode") {
            //Add 25 px each interrupt
            pad+=25;
            resizeParentToFit(cell);
            //Resize parent will set this to false so we need to set it back to true
            app.$.visualiser.rendering = true;
          }
          //box means interrupt
          if (cell.attributes.type === 'InterruptLabel') {
            let lbl = cell.attributes.attrs.text.text;
            lbl = lbl.replace(oldName.replace(".hidden",""),name);
            cell.attr({ text: { text: lbl } });
            let graph = {};
            graph.parentNode = _this.jgraph.getCell(cell.get("parent"));
            graph.parentNode.set("graphId",graph.id);
            graph.id = lbl;
            _this.displayedGraphs[graph.id] = graph.parentNode;
          }
        });
        resizeParentToFit(graph.parentNode);
        app.$.visualiser.rendering = true;
        //Now we move the box back
        graph.parentNode.translate(pad,0);
        //Find the bounding box of the selected processes parent, and scroll to it
        //The scroll bar is inside .process-display
        const nodeBBox = graph.parentNode.getBBox();
        const scale = V(this.paper.viewport).scale();
        $(".process-display").stop().animate({scrollTop:nodeBBox.origin().y*scale.sx}, '100', 'swing');
        this.saveChanges();
      });
    },
    /**
     * Redraw the automata.
     */
    redraw: function() {
      if (this.paper === undefined) return;
      $("#process-name-selector")[0].contentElement.selected = null;
      const _this = this;
      if ((!this.loaded && app.willSaveCookie) || this.layout) {
        this.loaded = true;
        const layout = this.layout || localStorage.getItem("layout");
        if (layout != null) {
          app.$.console.clear();
          app.$.console.log("Rendering from "+(this.layout?"File":"Autosave")+" please wait.");
          app.$.console.log("While rendering, you can not use the editor tab.");
          $(".disableOnRender")[0].disabled = true;
          this.jgraph.fromJSON(JSON.parse(layout));
        }
        if (app.get("automata.display") === undefined) app.set("automata.display",[]);
        _.each(this.jgraph.getCells(),cell => {
          if (cell.get("graphId")) {
            let graph = {};
            graph.id = cell.get("graphId");
            graph.parentNode = cell;
            _this.displayedGraphs[graph.id] = cell;
            app.push("automata.display",graph);
          }
        });
        _this.rescale();
        delete this.layout;
      } else {
        app.$.selector.locked = false;
      }
      if (!app.get("automata.values") || app.get("automata.values").length == 0) return;
      this.automata = this.graphMap;
      this.fire('process-visualisation-rendered');
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
    rescale: function() {
      if (this.exploded) {
        return;
      }
      let screenHeight = this.paper.$el.parent().parent().height();
      let screenWidth = this.paper.$el.width();
      const scale = V(this.paper.viewport).scale();
      let maxX = 0;
      let maxY = 0;
      _.each(this.displayedGraphs,function (graph) {
        const bbox = graph.getBBox();
        maxX = Math.max(maxX,bbox.corner().x);
        maxY = Math.max(maxY,bbox.corner().y);
      });
      this.paper.setDimensions(Math.max(maxX*scale.sx,screenWidth),Math.max(maxY*scale.sy+200,screenHeight));
    },
  });
})();
