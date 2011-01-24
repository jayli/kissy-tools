KISSY.add("dom", function() {
});

KISSY.add("event/base", function() {
});

KISSY.add("event/ie", function() {
}, {requires:["./base"]});

KISSY.add("event", function() {
}, {requires:["event/ie"]});

KISSY.add("overlay", function() {
}, {requires:["dom", "event"]});


