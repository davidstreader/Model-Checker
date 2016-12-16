(function() {
  'use strict';
  Polymer({
    is: 'process-modify',
    properties: {
      added: {
        type: Array,
        value: [],
        notify: true
      },
      PROCESS_TYPES: {
        type: Array,
        value: PROCESS_TYPES
      },
      processes: {
        type: Array
      },
      processName: {
        type: String
      },
      _hasSelection: {
        type: Boolean,
        value: false
      },
      _hasProcesses: {
        type: Boolean,
        computed: '_greaterThan(processes.length, 0)'
      },
      _initialSelection: {
        type: String,
        computed: 'initialSelection()'
      },
      compiledResult: {
        type: String
      }
    },
    //Call compile if added or processName are modified
    observers: ['compile(added.*,processName)'],
    compile: function() {
      if (this.added.length === 0) {
        this.set("compiledResult","");
        return;
      }
      //B3 = (one:Buff/{move/one.out} || two:Buff/{move/two.in}).
      let output = $("#process-type-selector")[0].selectedItemLabel + " "+(this.processName==""?"OUTPUT":this.processName)+" = ";
      let processes = [];
      _.each(this.added,function(process) {
        let current = "";
        if (process.name) {
          current+=process.name+":";
        }
        current+=process.id;
        if (process.renamed.length > 0) {
          let rename = [];
          let hidden = [];
          _.each(process.renamed,function(alphabet) {
            if (alphabet.renamed &&!alphabet.hidden)
              rename.push(alphabet.renamed + "/" + alphabet.id);
            if (alphabet.hidden)
              hidden.push(alphabet.id);
          });
          if (rename.length > 0)
            current += "/{"+rename.join()+"}";
          if (hidden.length > 0)
            current += "\\{"+hidden.join()+"}";

        }
        processes.push("("+current+")");
      });
      this.set("compiledResult",output+processes.join(" || ")+".");
    },
    addToEditor: function() {
      if (!this.compiledResult) return;
      app.$.editor.setCode(app.$.editor.getCode()+"\n"+this.compiledResult);
      app.$.editor.focus();
    },
    clear: function(e) {
      this.set("added",[]);
    },
    removeProcess: function(e) {
      this.splice("added",this.added.indexOf(e.model.process),1);
    },
    _initialSelection: function () {
      return "0";
    },
    /**
     * @returns {!boolean} a > b
     */
    _greaterThan: function (a, b) {
      return a > b;
    },
    ready: function () {
      const _this = this;
      //Expand the drawing area to the size of the screen when zooming
      window.addEventListener('resize', function () {
        _this.rescale();
      });
    },
    addProcess: function() {
      const id = $("#process-modify-selector")[0].selectedItemLabel;
      this.push("added",{id:id,name:"",renamed:_.keys(_.findWhere(app.automata.allValues,{id:id}).compiledAlphabet).map(id=>{
        const val = {};
        val.id = id;
        val.renamed = "";
        val.hidden = false;
        return val;
      })});
    },
    _onSelection: function (e, detail) {
      this._hasSelection = true;
      const graph = {name: detail.item.dataAutomatonName};
      for (let i in this.display) {
        if (this.display[i].id === graph.name) {
          graph.graph = this.display[i].graph;
          this.fire('change-process', this.display[i]);
          break;
        }
      }

    },
    redraw: function() {
      $("#process-modify-selector")[0].contentElement.selected = null;
      this._hasSelection = false;
    },
  });
})();
