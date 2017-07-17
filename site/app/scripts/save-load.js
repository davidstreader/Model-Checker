module.exports = {
    init: init,
    downloadFile: downloadFile
};
function loadFile(file) {
    const reader = new FileReader();
    reader.readAsText(file);
    reader.onload = function(file) {
        const text = file.target.result.split("visualiser_json_layout:");
        const code = text[0];
        const json = text[1];
        if (json && json.length > 0) {
            app.models.loadJSON(json);
        }
        app.editor.setCode(code);
    }
}
/**
 * Listen for key presses.
 * Note: Needs to listen for keydown (not keyup) in order to prevent browser default action
 */
function init() {
    $("#save-dialog").on('shown.bs.modal', function () {
        $(this).find("#fileName").focus().select();
    }).submit(function(e){
        downloadFile();
        return false;
    });
    $("#save").click(()=>{
        downloadFile();
    });
    const files = $('#files');
    $("#open-bt").click(()=>files.click());
    files.on('change',function(e) {
        if (this.value === '') {
            return;
        }
        loadFile(this.files[0]);
        this.value = '';
    });
    document.addEventListener('keydown', function (e) {
        // if (app.$.help.opened()) {
        //     // CTRL + S
        //     if (e.keyCode == 83 && e.ctrlKey) {
        //         //Even if the help dialog is open, we don't want the default save dialog to show.
        //         e.preventDefault();
        //     }
        //     return;
        // }

        switch (e.keyCode) {
            case 13:
                // CTRL + ENTER
                if (e.ctrlKey) {
                    app.compile();
                    e.preventDefault();
                }
                break;
            case 79:
                // CTRL + O
                if (e.ctrlKey) {
                    app.openFile();
                    e.preventDefault();
                }
                break;
            case 83:
                // CTRL + S
                if (e.ctrlKey) {
                    jQuery.noConflict();
                    jQuery("#save-dialog").modal();
                    e.preventDefault();
                }
                break;
            case 112:
                // F1 TODO: Help
                // app.$.help.open();
                e.preventDefault();
                break;
            default:
                return;
        }
    });
}


/**
 * Save to code the user has written to their computer (as a download).
 */
function downloadFile() {
    let filename = $("#fileName").val();
    // if filename has not been defined set to untitled
    if(filename === ''){
        filename = 'untitled';
    }
    let output = "";
    if ($("#saveCode")[0].checked)
        output+= app.editor.getCode();
    if ($("#saveLayout")[0].checked) {
        output+="\nvisualiser_json_layout:"
        output+= JSON.stringify(app.models.getJSON());
    }
    const blob = new Blob(
        [output],
        {type: 'text/plain;charset=utf-8'});
    require("file-saver").saveAs(blob, filename + '.txt');
}
