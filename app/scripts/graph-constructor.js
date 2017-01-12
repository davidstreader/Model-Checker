'use strict';
function addLabelAndPadding(graphMap, key, jgraph, opts) {
  graphMap[key].parentNode.fitEmbeds({padding:50});
  const bbox = graphMap[key].parentNode.getBBox();
  const lx = bbox.origin().x, ly = bbox.origin().y, ux = bbox.corner().x, uy = bbox.corner().y;
  const width = ux - lx;
  const height = uy - ly;
  let interruptHeight = 0;
  if (graphMap[key].interrupts) {
    interruptHeight = graphMap[key].interrupts.length * 30;
  }
  const cell = new joint.shapes.ParentLabel({
    type: "ParentLabel",
    size: {width: 100, height: 30},
    position: {x: lx - 50, y: ly - 50-interruptHeight+10},
    attrs: {
      rect: {fill: 'transparent', stroke: 'none', style:{'pointer-events':'none'}},
      'text': {text: key, fill: 'red', 'font-size': 20, 'text-anchor': 'start', style:{'pointer-events':'none'}}
    }
  });
  let buttons;
  if (opts.disableExplode) {
    buttons = new joint.shapes.ButtonsNoExplode({
      position: {x: ux - 50, y: ly - 50-interruptHeight+10},
    });
  } else{
    buttons = new joint.shapes.Buttons({
      position: {x: ux - 50, y: ly - 50-interruptHeight+10},
    });
  }

  buttons.set("graphID",key.replace(".hidden",""));
  graphMap[key].parentNode.embed(buttons);
  graphMap[key].parentNode.embed(cell);
  graphMap[key].label = cell;
  jgraph.addCell(cell);
  jgraph.addCell(buttons);
  graphMap[key].parentNode.resize(width+100,height+100+interruptHeight);
  //Move the parent node without moving its children, to add a padding around it
  //position needs to subtract intersize to center interrupted components
  graphMap[key].parentNode.position(lx-50,ly-50-interruptHeight);
  //But translate now needs to move everything back so its at its old position, but with a changed padding.
  //Now move the parent node and its components back by the padding
  graphMap[key].parentNode.translate(50, 50+interruptHeight);
  //Move the component back to the origin with a bit of padding
  graphMap[key].parentNode.translate(50, -ly+50);

  graphMap[key].parentNode.toFront({ deep: true });

  resizeParentToFit(graphMap[key].parentNode);
}
function constructGraphs(graphMap, id, hidden, callback, opts) {
  opts = opts || {};
  //Find the process by id
  let graph = _.findWhere(app.get("automata.values"), {id: id});
  if (hidden)
    id += ".hidden";
  if (!graph.type || (graphMap[id] && app.get("automata.analysis")[id] &&  !app.get("automata.analysis")[id].isUpdated)) {
    callback();
    return;
  }

  const worker = new Worker("scripts/graph-constructor-worker.js");
  worker.postMessage({graph:graph,id:id,hidden:hidden});
  worker.onmessage = e => {  //Calculate the bottom of the last drawn graph
    let tmpjgraph = new joint.dia.Graph();
    tmpjgraph.fromJSON(e.data);
    graphMap[id] = {parentNode: tmpjgraph.getCells()[0],alphabet:e.data.alphabet};
    addLabelAndPadding(graphMap,id,tmpjgraph, opts);
    callback();
  };
}
/**
 * Move a cells vertices when moving the cell
 * @param graph The graph
 * @param cell the cell
 */
function adjustVertices(graph, cell) {
  //TODO: it would be nice if the distance moved was proportional to the distance from the other cell
  // If the cell is a view, find its model.
  cell = cell.model || cell;
  //Some cell types should not trigger changes.
  switch (cell.attributes.type) {
    case "Parent":
    case "fsa.Arrow":
    case "ButtonsNoExplode":
    case "Buttons":
    case "ParentLabel":
    case "InterruptParentNode":
    case "InterruptLabel":
      return;
  }
    //Ignore all clicks that arent on a cell
  if (!cell.attributes.position || !cell.previous("position")) return;
  const {x:nx,y:ny} = cell.get("position");
  const {x:ox,y:oy} = cell.previous("position");
  const diff = {x:nx-ox,y:ny-oy};
  //Lets just nuke the original verticies.
  _.each(graph.getConnectedLinks(cell),link =>{
    let verticies = [];
    _.each(link.get("vertices"),function (vert) {
      verticies.push({x:vert.x+diff.x,y:vert.y+diff.y});
    })
    link.set('vertices', verticies);
  });
}
let subtree = [];
function collectDeepEmbedded(cell) {
  subtree = [];
  _collectDeepEmbedded(cell);
  return subtree;
}
function _collectDeepEmbedded(cell) {
  _.each(cell.getEmbeddedCells(), function(c) {
    subtree.push(c);
    _collectDeepEmbedded(c);
  })
}
function resizeParentToFit(parent) {
  app.$.visualiser.rendering = true;
  let minX = Number.MAX_VALUE, minY = Number.MAX_VALUE;
  let maxX = 0,maxY=0;
  let label, buttons;
  _.each(parent.getEmbeddedCells(),node => {
    if (node.attributes.type === 'ParentLabel' ||
      node.attributes.type === 'InterruptLabel') {
      label = node;
      return;
    }
    if (node.attributes.type === 'Buttons') {
      buttons = node;
      return;
    }
    if (node.attributes.type === 'fsa.Arrow') return;
    const bbox = node.getBBox();
    minX = Math.min(minX,bbox.origin().x);
    minY = Math.min(minY,bbox.origin().y);
    maxX = Math.max(maxX,bbox.corner().x);
    maxY = Math.max(maxY,bbox.corner().y);
  });
  let width = maxX-minX;
  let height = maxY-minY;
  width+=100;
  height+=100;
  parent.resize(width,height);
  parent.set("position", {x:minX-50,y:minY-50});
  if (label)
    label.set("position", {x:minX-30,y:minY-30});
  if (buttons)
    buttons.set("position", {x:maxX-15,y:minY-50});
  app.$.visualiser.rendering = false;
  if (parent.get("parent")) {
    resizeParentToFit(app.$.visualiser.jgraph.getCell(parent.get("parent")))
  }
}
