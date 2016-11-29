/**
 * Styling for the default joint.js shapes.
 */
//Modify the transition element to place the label inside and not above.

joint.shapes.pn.Transition = joint.shapes.basic.Generic.extend({

  markup: '<g class="rotatable"><g class="scalable"><rect class="root"/></g></g><text class="label"/>',

  defaults: _.defaultsDeep({

    type: 'pn.Transition',
    size: { width: 20, height: 60 },
    attrs: {
      'rect': {
        width: 12,
        height: 50,
        fill: '#000000',
        stroke: '#000000'
      },
      '.label': {
        'text-anchor': 'middle',
        'ref-x': .5,
        'ref-y': 23,
        ref: 'rect',
        fill: '#FFFFFF',
        'font-size': 12
      }
    }

  }, joint.shapes.basic.Generic.prototype.defaults)
});
joint.shapes.parent = joint.shapes.basic.Rect.extend();
function intersection(cell,line) {
  var box = cell.getBBox();
  var lines = [g.line(box.bottomLeft(),box.corner()),g.line(box.bottomLeft(),box.origin()),g.line(box.origin(),box.topRight()),g.line(box.topRight(),box.corner())];
  var ret = [];
  for (var cellLine in lines) {
    var int = lines[cellLine].intersection(line);
      if(int) {
        ret.push(int);
      }
  }
  return ret;
}
function adjustVertices(graph, paper, cell) {
  if (cell === undefined) return;
  // If the cell is a view, find its model.
  cell = cell.model || cell;

  if (cell instanceof joint.dia.Element) {

    _.chain(graph.getConnectedLinks(cell)).groupBy(function(link) {
      // the key of the group is the model id of the link's source or target, but not our cell id.
      return _.omit([link.get('source').id, link.get('target').id], cell.id)[0];
    }).each(function(group, key) {
      // If the member of the group has both source and target model adjust vertices.
      if (key !== 'undefined') adjustVertices(graph, _.first(group));
    });

    return;
  }

  // The cell is a link. Let's find its source and target models.
  var srcId = cell.get('source').id || cell.previous('source').id;
  var trgId = cell.get('target').id || cell.previous('target').id;

  // If one of the ends is not a model, the link has no siblings.
  if (!srcId || !trgId) return;
  var vertices = [];
  var srcCenter = graph.getCell(srcId).getBBox().center();
  var trgCenter = graph.getCell(trgId).getBBox().center();
  var line =  g.line(srcCenter, trgCenter);
  graph.getCells().forEach(function(cell) {
    if (!cell.getBBox || cell == graph.getCell(srcId) || cell == graph.getCell(trgId)|| cell.get("ignoreChecks")) return;
    var int = intersection(cell,line);
    if(int.length != 0) {
      var pointOnEdge = cell.getBBox().pointNearestToPoint(g.line(int[0],int[1]).midpoint());
      pointOnEdge = pointOnEdge.move(cell.getBBox().center(),10);
      vertices.push(pointOnEdge);
    }
  });
  //TODO: we should also do another version of this that is based on nearby verticies
  var siblings = _.filter(graph.getLinks(), function(sibling) {

    var _srcId = sibling.get('source').id;
    var _trgId = sibling.get('target').id;

    return (_srcId === srcId && _trgId === trgId) || (_srcId === trgId && _trgId === srcId);
  });

  switch (siblings.length) {

    case 0:
      // The link was removed and had no siblings.
      break;

    case 1:
      // There is only one link between the source and target. No vertices needed.
      cell.set('vertices',vertices);
      break;

    default:

      // There is more than one siblings. We need to create vertices.

      // First of all we'll find the middle point of the link.
      var midPoint = g.line(srcCenter, trgCenter).midpoint();

      // Then find the angle it forms.
      var theta = srcCenter.theta(trgCenter);

      // This is the maximum distance between links
      var gap = 20;

      _.each(siblings, function(sibling, index) {

        // We want the offset values to be calculated as follows 0, 20, 20, 40, 40, 60, 60 ..
        var offset = gap * Math.ceil(index / 2);

        // Now we need the vertices to be placed at points which are 'offset' pixels distant
        // from the first link and forms a perpendicular angle to it. And as index goes up
        // alternate left and right.
        //
        //  ^  odd indexes
        //  |
        //  |---->  index 0 line (straight line between a source center and a target center.
        //  |
        //  v  even indexes
        var sign = index % 2 ? 1 : -1;
        var angle = g.toRad(theta + sign * 90);

        // We found the vertex.
        var vertex = g.point.fromPolar(offset, angle, midPoint);
        //TODO: We need to work out offsets based on the previous vertices.
        //TODO: as the line is not a direct line between two cells, but a bent one.
        //sibling.set('vertices', _.union(vertices,[vertex]));
        sibling.set('vertices', [vertex]);
      });
  }
};

