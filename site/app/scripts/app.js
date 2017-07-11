window.$ = window.jQuery = jQuery;
require("bootstrap-webpack");
require("font-awesome/css/font-awesome.css");
require("bootstrap-slider/dist/bootstrap-slider.js");
require("bootstrap-slider/dist/css/bootstrap-slider.css");
require("jquery-resizable-dom/src/jquery-resizable");
require("ace-builds/src-min-noconflict/ace.js");
require("ace-builds/src-min-noconflict/theme-vibrant_ink.js");
require("ace-builds/src-min-noconflict/theme-crimson_editor.js");
require("ace-builds/src-min-noconflict/ext-language_tools");
require("cytoscape-panzoom/cytoscape.js-panzoom.css");
require("./mode-example");
require("./theme-example");

$(function() {
    'use strict';
    const ReconnectingWebSocket = require("reconnecting-websocket");
    const app = {};
    window.app = app;
    $(".left-panel").resizable({
        handleSelector: ".splitter",
        resizeHeight: false
    });



    app.utils = require("./utils");
    app.editor = require("./editor");
    app.file = require("./save-load");
    app.file.init();
    app.console = require("./console");
    app.automata = {values: [],display:[]};
    app.helpDialogSelectedTab = 0;
    app.currentBuild = {};
    app.previousBuild = {};
    app.selectedCtx = 0;
    app.connected = false;
    app.decoder = new TextDecoder("UTF-8");
    const proto = window.location.protocol.replace("http","").replace(":","");
    app.socket = new ReconnectingWebSocket("ws"+proto+"://" + location.hostname + ":" + location.port + "/socket/");
    app.socket.onmessage = function (msg) {
        if (msg === undefined) return;
        let results = JSON.parse(msg.data);
        const data = results.data;
        if (results.event === "compileReturn") {
            if (data.type === "error") {
                app.showError(data);
                return;
            }
            app.finalizeBuild(data);
        } else if (results.event === "log") {
            if (data.clear) {
                if (data.clearAmt !== -1) {
                    app.console.clear(data.clearAmt);
                } else {
                    app.console.clear();
                }
            }
            if (data.error) {
                app.console.error(data.message);
                if (data.location) {
                    const Range = ace.require("ace/range").Range;
                    const editor = app.$.editor._editor.getSession();
                    editor.clearAnnotations();
                    _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
                    const l = data.location;
                    editor.addMarker(new Range(l.lineStart-1, l.colStart, l.lineEnd-1, l.colEnd), "ace_underline");
                    for (let i = l.lineStart; i <= l.lineEnd; i++) {
                        editor.setAnnotations([{row:i-1 ,column: 0, text:data.message,type:"error"}]);
                    }
                }
            }
            else {
                app.console.log(data.message);
            }
        }
    };
    const statusBadge = $("#status");
    app.socket.onopen = function () {
        app.connected = true;
        statusBadge.text("Connected");
        statusBadge.removeClass("label-danger");
        statusBadge.addClass("label-success");
        if (app.liveCompiling)
            app.compileAndBuild();
    };
    app.socket.onclose = function () {
        app.connected = false;
        statusBadge.text("Disconnected");
        statusBadge.addClass("label-danger");
        statusBadge.removeClass("label-success");
        app.console.warn("You have been disconnected from the server.");
        app.console.warn("As a result, your last compilation may not have completed successfully.");
    };

    app.compile = function(overrideBuild) {
        const code = app.editor.getCodeClean();
        if(!overrideBuild){
            // if there is nothing to parse then do not continue
            if(code.length === 0){
                app.console.clear();
                app.automata = {};
                app.previousCode = '';
                return;
            }

            // if the code has not changed then do not continue
            if(code === app.previousCode){
                return;
            }
        }
        app.build(overrideBuild);
    };


    /**
     * Runs the compiler converting the code in the editor into visualisable
     * graphs and calls the renderer.
     */
    app.build = function(override) {
        app.console.clear();
        const editor = app.editor._editor.getSession();
        editor.clearAnnotations();
        _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
        if (!app.connected) {
            app.console.error("Unable to compile: No connection to server.")
        }
        setTimeout(function() {
            if(app.liveCompiling === true || override){
                app.lastCompileStartTime = (new Date()).getTime();
                const code = app.editor.getCode();
                const settings = app.settings.getSettings();
                app.socket.send(JSON.stringify({code:code,context:settings}));
            }
        }.bind(this), 0);
    };
    app.showError = function(error) {
        const Range = ace.require("ace/range").Range;
        const editor = app.editor._editor.getSession();
        if (error.stack) {
            app.console.error("An exception was thrown that was not related to your script.");
            app.console.error(error.message+"\n"+error.stack);
            console.log(error.message+"\n"+error.stack);
        } else {
            app.console.error(error.message);
            if (error.location) {
                const l = error.location;
                //If colEnd==colStart, add one to colEnd so that a character is highlighted.
                if (l.colEnd == l.colStart) l.colEnd++;
                editor.addMarker(new Range(l.lineStart-1, l.colStart, l.lineEnd-1, l.colEnd), "ace_underline");
                for (let i = l.lineStart; i <= l.lineEnd; i++) {
                    editor.setAnnotations([{row:i-1 ,column: 0, text:error.message,type:"error"}]);
                }
            }
        }
    };
    app.finalizeBuild = function(results) {
        const editor = app.editor._editor.getSession();
        editor.clearAnnotations();
        _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
        const graphs = [];
        const allGraphs = [];
        const skipped = results.skipped;
        for(let id in results.processes){
            if(!_.find(skipped, { id: results.processes[id].id })){
                results.processes[id].id = id;
                graphs.push(results.processes[id]);
            }

            allGraphs.push(results.processes[id]);
        }
        app.automata.values = graphs.reverse();
        app.automata.allValues = allGraphs.reverse();
        app.automata.analysis = results.analysis;
        app.utils.fillSelect(_.map(app.automata.values,val=>val.id),$(".process-list"),true,false,"No processes found");
        $(".disable-no-process").prop("disabled",app.automata.values.length===0);
        app.console.clear();
        app.console.log('Successfully Compiled!');
        if(results.operations.length !== 0){
            let passed = 0;
            app.console.log('Operations:');
            for(let i = 0; i < results.operations.length; i++){
                const { operation, process1, process2, result, extra } = results.operations[i];
                const op = process1.ident + ' ' + operation + ' ' + process2.ident + ' = ' + result + ' '+ extra;
                if(result=="true"){
                    app.console.log(op);
                    passed++;
                } else{
                    app.console.error(op);
                }
            }

            if(passed === results.operations.length){
                app.console.log('All operations passed!');
            }
            else{
                app.console.log(passed + '/' + results.operations.length + ' operations passed');
            }
        }
        if(results.equations.length !== 0){
            let passed = 0;
            app.console.log('Equations:');
            for(let i = 0; i < results.equations.length; i++){
                const { operation, process1, process2, result, extra } = results.equations[i];
                const op = process1.ident + ' ' + operation + ' ' + process2.ident + ' = ' + result + ' ' + extra;
                if(result=="true"){
                    app.console.log(op);
                    passed++;
                } else{
                    app.console.error(op);
                }
            }

            if(passed === results.equations.length){
                app.console.log('All equations passed!');
            }
            else{
                app.console.log(passed + '/' + results.equations.length + ' operations passed');
            }
        }
        app.console.log("Compiled in: "+(((new Date()).getTime()-app.lastCompileStartTime)/1000)+" seconds");
        _.each(skipped, skip=> {
            if (skip.type != "user")
                app.console.log("Skipped adding "+skip.id+" to the render list, as it has too many "+skip.type+" ("+skip.length +" > "+skip.maxLength+")");
        });
    };
    /**
     * Compiles and builds what has currently been entered into the text-area.
     * Ignores whether or not live compile and build are currently set.
     */
    app.compileAndBuild = function() {
        app.compile(true);
    };


    // /**
    //  * Simple event listener for when the user switches tabs.
    //  * When we switch to index 1 (Diagram), we need to redraw the canvas,
    //  * as it needs to be showing currently to render.
    //  * If we switch to the editor, request focus on it.
    //  */
    // app.$['maintabs'].addEventListener('iron-select', function (e) {
    //     if (app.$.maintabs.selected === 1) {
    //         app.$.visualiser.redraw();
    //     } else if (app.$.maintabs.selected === 0) {
    //         app.$.editor._editor.focus();
    //     }else if (app.$.maintabs.selected === 2) {
    //         app.$.modify.redraw();
    //     }
    // });
    /**
     * This is the event which triggers when the text in the text area is changed.
     * Only care about this if the live-compiling check-box is ticked.
     */
    app.editor._editor.on("input", function() {
        if (app.settings.getSettings().liveCompiling && app.connected)
            app.compileAndBuild();
        if (app.settings.getSettings().autoSave)
            localStorage.setItem("editor",encodeURIComponent(app.editor.getCode()));
    });

    app.settings = require("./settings");
    app.settings.init();
    app.models = require("./models");
    app.models.init();
    app.generator = require("./generator");
    app.generator.init();
    $("#model-tab").on('shown.bs.tab',app.models.redraw);
    $("#generator-tab").on('shown.bs.tab',app.generator.redraw);
    $("#editor-tab").on('shown.bs.tab',()=>app.editor._editor.focus());
});
