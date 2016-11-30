/**
 * Styling for the default joint.js shapes.
 */
joint.shapes.fsa.EndState = joint.dia.Element.extend({

  markup: '<g class="rotatable"><g class="scalable"><circle class="outer"/><circle class="inner"/></g></g>',

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
        fill: '#000000'
      }
    }

  }, joint.dia.Element.prototype.defaults)
});
//Modify the transition element to place the label inside and not above.

joint.shapes.pn.Transition = joint.shapes.basic.Generic.extend({

  markup: '<g class="rotatable"><g class="scalable"><rect class="root"/></g></g><text class="label"/>',

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
joint.shapes.parent = joint.shapes.basic.Rect.extend();

