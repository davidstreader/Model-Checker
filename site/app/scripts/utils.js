module.exports = {
    fillSelect: function(vals, select, clear) {
        if (clear) select.html("");
        select.prop("disabled",Object.keys(vals).length === 0);
        $.each(vals, function(key, value) {
            select.append($("<option/>", {
                value: key,
                text: value
            }));
        });
    }
};
