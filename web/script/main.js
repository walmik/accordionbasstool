var appletPrefWidth = 0;
var appletPrefHeight = 0;
var loaded = false;
var requestMode = null;

function onAppletPrefSizeChanged(prefWidth, prefHeight)
{
  // If requestMode was set while loading, switch to that mode
  // Clear mode before setting to avoid reentry
  if (requestMode != null) {
    var mode = requestMode;
    requestMode = null;
    document.the_applet.setMode(mode);
    return;
  }

  appletPrefWidth = prefWidth;
  appletPrefHeight = prefHeight;

  //if ($('input[id=resizeCheck]').is(':checked')) {
  //  return;
  //}

  var elem = document.getElementById("applet_div");

  //      prefWidth += elem.style.paddingRight;
  //      prefHeight += elem.style.paddingBottom;

  var maxWidth = $(window).width();
  var maxHeight = $(window).height();


  if ((prefWidth < maxWidth) && (prefHeight < maxHeight) && (prefWidth > 300))
  {
    var xRatio = maxWidth / prefWidth;
    var yRatio = maxHeight / prefHeight;

    if (xRatio < yRatio) {
      prefHeight *= xRatio;
      prefWidth = maxWidth;
    } else {
      prefWidth *= yRatio;
      prefHeight = maxHeight;
    }
  }

  //alert(prefWidth + " " + prefHeight);
  $("#applet_div").css({
    width: prefWidth,
    height: prefHeight
  });

  loaded = true;
}

function toggleTabContent(vis)
{
  if (!vis) {
    var headHeight = $("#tabHeader").outerHeight();
    $(".ui-widget-content").height(headHeight);
  } else {
    $(".ui-widget-content").height("auto");
  }

  $(".tabContent").css({
    "visibility" : (vis ? "visible" : "hidden")
    });

  vis = !vis;
}

/* JQuery Inits Below */

$(document).ready(function()
{
  $("#tabs").tabs();
  //$(window).resize(function () { /* do something */ });
  $('#tabs').bind('tabsselect', function(event, ui){
    if (loaded) {
      document.the_applet.setMode(ui.panel.id);
    } else {
      requestMode = ui.panel.id;
    }
  });
  //     $("#applet_div").draggable({/*containment: "#drag_div",*/ scroll: false });
  $("#applet_div").resizable({/*handles: "n, e, s, w, ne, se, sw, nw"*/
    //       resize: function(event, ui) { setAbs(); },
    //       start: function(event, ui) { setAbs(); },
    //       stop: function(event, ui) { setAbs(); }

    });

//var headHeight = $("#tabHeader").outerHeight();
//$(".ui-widget-content").css({"visibility" : "hidden"});
//$(".ui-widget-content").height(headHeight);
//        var anchor = $(document).attr("location").hash; // the anchor in the URL
//        var index = $('#tabs div.ui-tabs-panel').index($(anchor)); // in tab index of the anchor in the URL
//        $('#tabs').tabs('select', index); // select the tab
//        $('#tabs').bind('tabsshow', function(event, ui)
//        {
//          document.location = $(document).attr('location').pathname + "#" + ui.panel.id;
//        }); // change the url anchor when we click on a tab
});