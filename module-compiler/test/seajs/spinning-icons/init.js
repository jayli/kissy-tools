module.declare("init", ["jquery", "./spinning","m1/m2"], function(require) {
  var $ = require("jquery");
  $(document).ready(function() {
    require("./spinning").spinning($("#followIcons a"));
    $("#followIcons").show().parent().css("background", "none")
  })
});

