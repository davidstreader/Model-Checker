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
      _.each(this.added,function(process) {
        output +="(";
        if (process.name !== "") {
          output+=process.name+":";
        }
        output+=process.id;
        if (process.renamed.length > 0) {
          let addedBracket = false;
          _.each(process.renamed,function(alphabet) {
            if (alphabet.renamed !== "") {
              if (!addedBracket) {
                addedBracket = true;
                output+="/{"
              }
              output += alphabet.renamed + "/" + alphabet.id+",";
            }
          });
          if (addedBracket) {
            output = output.substring(0,output.length-1);
            output += "}";
          }
        }
        output +=") || ";
      });
      this.set("compiledResult",output.substring(0,output.length-4)+".");
    },
    addToEditor: function() {
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
