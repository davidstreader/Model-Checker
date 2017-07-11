module.exports = {
    load: load
};
/**
 * Listen for key presses.
 * Note: Needs to listen for keydown (not keyup) in order to prevent browser default action
 */
function load() {
    $("#save-dialog").on('shown.bs.modal', function () {
        $(this).find("#fileName").focus().select();
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
