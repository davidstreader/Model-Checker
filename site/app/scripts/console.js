const _ = require("lodash");
const AU = require("ansi-up");
const ansi_up = new AU.AnsiUp();
const c = $("#console");
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
    },

    log: function(msg){
        this._addMessage(msg,'log')
    },

    warn: function(msg){
        this._addMessage(msg,'warn')
    },

    error: function(msg){
        this._addMessage(msg,'error')
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
