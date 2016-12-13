joint.shapes.fsa.Arrow = joint.dia.Link.extend({
  markup: [
    '<path class="connection" stroke="black" d="M 0 0 0 0"/>',
    '<path class="marker-source" fill="black" stroke="black" d="M 0 0 0 0"/>',
    '<path class="marker-target" fill="black" stroke="black" d="M 0 0 0 0"/>',
    '<path class="connection-wrap" d="M 0 0 0 0"/>',
    '<g class="labels"/>',
    '<g class="marker-vertices"/>',
    '<g class="marker-arrowheads"/>',
    '<g class="link-tools"/>',
    '<title class="title" />'
  ].join(''),
  defaults: _.defaultsDeep({
    type: 'fsa.Arrow',
    attrs: { '.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z' }},
    smooth: true
  }, joint.dia.Link.prototype.defaults)
});
joint.shapes.fsa.State = joint.shapes.basic.Circle.extend({
  markup: '<g class="rotatable"><g class="scalable"><circle/></g><text/></g><title class="title" />',
  defaults: _.defaultsDeep({
    type: 'fsa.State',
    attrs: {
      circle: { 'stroke-width': 3 },
      text: { 'font-weight': '800' }
    }
  }, joint.shapes.basic.Circle.prototype.defaults)
});
/**
 * Styling for the default joint.js shapes.
 */
joint.shapes.fsa.EndState = joint.dia.Element.extend({

  markup: '<g class="rotatable"><g class="scalable"><circle class="outer"/><circle class="inner"/></g></g><title class="title" />',

  defaults: _.defaultsDeep({

    type: 'fsa.EndState',
    size: { width: 60, height: 60 },
    attrs: {
      '.outer': {
        'stroke-width': 3,
        transform: 'translate(10, 10)',
        r: 10,
        fill: '#ffffff',
        stroke: '#000000'
      },

      '.inner': {
        'stroke-width': 3,
        transform: 'translate(10, 10)',
        r: 6,
        fill: Colours.grey,
        stroke:'#000000'
      }
    }

  }, joint.dia.Element.prototype.defaults)
});

//Modify the transition element to place the label inside and not above.

joint.shapes.pn.Transition = joint.shapes.basic.Generic.extend({

  markup: '<g class="rotatable"><g class="scalable"><rect class="root"/></g></g><text class="label"/><title class="title" />',

  defaults: _.defaultsDeep({

    type: 'pn.Transition',
    size: { width: 60, height: 60 },
    attrs: {
      'rect': {
        width: 60,
        height: 60,
        fill: '#000000',
        stroke: '#000000'
      },
      '.label': {
        'text-anchor': 'middle',
        'ref-x': .5,
        'ref-y': 10,
        ref: 'rect',
        fill: '#FFFFFF',
        'font-size': 12
      }
    }

  }, joint.shapes.basic.Generic.prototype.defaults)
});

joint.shapes.pn.StartPlace = joint.dia.Element.extend({

  markup: '<g class="rotatable"><g class="scalable"><circle class="outer"/><circle class="inner"/></g></g>',

  defaults: _.defaultsDeep({

    type: 'pn.StartPlace',
    size: { width: 60, height: 60 },
    attrs: {
      '.outer': {
        'stroke-width': 3,
        transform: 'translate(10, 10)',
        r: 10,
        fill: Colours.grey,
        stroke: '#000000'
      },

      '.inner': {
        'stroke-width': 3,
        transform: 'translate(10, 10)',
        r: 3,
        fill: '#000000'
      }
    }

  }, joint.dia.Element.prototype.defaults)
});

joint.shapes.pn.Place = joint.shapes.pn.Place.extend({
  defaults: _.defaultsDeep({
    size: {width: 60, height: 60},
    attrs: {
      '.root': { fill:Colours.grey, 'stroke-width':3 },
      '.label': {text: '', fill: '#7c68fc'}
    }
  }, joint.shapes.pn.Place.prototype.defaults)
});

joint.shapes.pn.TerminalPlace = joint.dia.Element.extend({

  markup: '<g class="rotatable"><g class="scalable"><circle class="outer"/><circle class="inner"/></g></g>',

  defaults: _.defaultsDeep({

    type: 'pn.TerminalPlace',
    size: { width: 60, height: 60 },
    attrs: {
      '.outer': {
        'stroke-width': 3,
        transform: 'translate(10, 10)',
        r: 10,
        fill: 'green',
        stroke: '#000000'
      },

      '.inner': {
        'stroke-width': 3,
        transform: 'translate(10, 10)',
        r: 6,
        fill: Colours.grey,
        stroke:'#000000'
      }
    }

  }, joint.dia.Element.prototype.defaults)
});

joint.shapes.parent = joint.shapes.basic.Rect.extend();
joint.shapes.interruptParentNode = joint.shapes.basic.Rect.extend();
joint.shapes.interruptEmbedNode = joint.shapes.basic.Rect.extend();
joint.shapes.interruptLabel = joint.shapes.basic.Rect.extend({

  markup: '<g class="rotatable"><g class="scalable"><rect/></g><text/></g>',

  defaults: _.defaultsDeep({

    type: "interruptLabel",
    size: {width: 100, height: 30},
    attrs: {
      rect: {fill: 'transparent', stroke: 'none'},
      text: {fill: 'red', 'font-size': 20}
    }

  }, joint.shapes.basic.Generic.prototype.defaults)
});
/**
 * Get a cell's jquery object
 * @param cell the cell
 * @returns {jQuery|HTMLElement}
 */
function get$Cell(cell) {
  return $("[model-id='" + cell.id + "']");
}
