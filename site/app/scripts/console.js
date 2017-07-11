const _ = require("lodash");
const ansi_up = require("ansi-up").AnsiUp.prototype;
const c = $("#console");
ansi_up.ansi_to_html("Test");
module.exports = {
    clear: function(lines){
        if (lines) {
            for (lines = Number.parseInt(lines); lines > 0; lines--){
                c.children().last().remove(); // content
                c.children().last().remove(); // line break
            }
        } else {
            c.html("");
        }
        // this.fire('console-change',{clear:true,lines:lines});
    },

    log: function(msg){
        this._addMessages(msg,'log')
        // this.fire('console-change',{msg:msg,type:"log"});
    },

    warn: function(msg){
        this._addMessages(msg,'warn')
        // this.fire('console-change',{msg:msg,type:"warn"});
    },

    error: function(msg){
        this._addMessages(msg,'error')
        // this.fire('console-change',{msg:msg,type:"error"});
    },

    _addMessages: function(msg,style){
        let nodes = [];
        msg.split("\n").forEach(msgs => nodes.push(this._addMessage(msgs,style)));
        return nodes;
    },
    _addMessage: function(msg,style) {
        let node = document.createElement('SPAN');
        node.innerHTML = ansi_up.ansi_to_html(msg);
        node.classList.add(style);
        c.append(node);
        c.append(document.createElement('BR'));
        c.scrollTop = c.scrollHeight;
        return node;
    }
};
