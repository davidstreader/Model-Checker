module.exports = {
    fillSelect: function(vals, select, clear, useKey, emptyMessage) {
        if (clear) select.html("");
        const empt = Object.keys(vals).length === 0;
        select.prop("disabled",empt);
        if (empt && emptyMessage) {
            vals = {0:emptyMessage}
        }
        $.each(vals, function(key, value) {
            select.append($("<option/>", {
                value: useKey?key:value,
                text: value
            }));
        });
    },
    debounce: function(func, wait, immediate) {
        let timeout;
        return function() {
            const context = this,
                args = arguments;
            const later = function () {
                timeout = null;
                if (!immediate) {
                    func.apply(context, args);
                }
            };
            const callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait || 200);
            if ( callNow ) {
                func.apply(context, args);
            }
        };
    }
};
