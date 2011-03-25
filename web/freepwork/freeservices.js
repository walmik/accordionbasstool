//
//Free Services Locator - Script File
//Created by Ilya Kreymer, 2011
//Licensed Under Creative Commons License
//

var SFBounds;

var theInfoWin = null;
var markersLoaded = false;
var rootUL;

var globalNumVisible = 0;
var globalUserAddress = "";

var selectedPlaceId;

var DATA;

var dirService;
var dirRender;

function testDir()
{
  var request = {
    origin: "San Francisco, CA",
    destination: "San Francisco, CA",
    travelMode: google.maps.DirectionsTravelMode.DRIVING
  };

  dirService.route(request, function(result, status) {
    if (status == google.maps.DirectionsStatus.OK) {
      dirDisplay.setDirections(result);
    }
  });
}

function init()
{
  // Init JSON Data
  DATA = servicesData();
  
  // Init SF Bounds
  var sw = new google.maps.LatLng(37.70567075998761, -122.51793249606325);
  var ne = new google.maps.LatLng(37.83051256186211, -122.361377320282);
  SFBounds = new google.maps.LatLngBounds(sw, ne);

  geolib.init(
  {
    mapDivName: 'map_canvas',
    formAddressName: 'address',
    formPosName: 'posLatlng',
    alwaysLookUpUserAddress: false,
    acceptPosChange: acceptPosChange,
    markerPosChanged: onMarkerPosChanged,
    markerAddrChanged: onMarkerAddrChanged,
    errorDivName: "addressError"
  });

  theInfoWin = new google.maps.InfoWindow();

  //Init Directions
  dirService = new google.maps.DirectionsService();
  dirDisplay = new google.maps.DirectionsRenderer();
  dirDisplay.setOptions(
  {
    infoWindow: theInfoWin,
    panel: document.getElementById("dirDivContent"),
    suppressMarkers: true
  });

  testDir();

  google.maps.event.addListener(geolib.getMap(), "bounds_changed", updateVisiblePlaceList);

  geolib.getMarker().setZIndex(1000);
  geolib.getMap().setZoom(12);

  geolib.getMarker().setIcon(getImageStr("UserLoc", true));

  //$("#myloc").html("<img src='" + getImageStr("UserLoc") + "'/> My Address");

  loadAllMarkers();

  buildVisiblePlaceList(geolib.getPos());
}

function acceptPosChange(pos)
{
  var acceptable = SFBounds.contains(pos);
  var statusText = (acceptable ? "" : "This is not a valid San Francisco address. Please try again");

  $("#addressError").text(statusText);

  return acceptable;
}

function onMarkerPosChanged(pos)
{
  geolib.getMap().setZoom(15);
  buildVisiblePlaceList(pos);
}

function onMarkerAddrChanged(text)
{
  globalUserAddress = text;
  updateInfoStatus();
}

function updateInfoStatus()
{
  var infoStr = "";
  //infoStr += "<i><img src='" + getImageStr("UserLoc") + "'/>";
  //infoStr += "<i>Your Address</i>";
  infoStr += "<b>" + globalUserAddress + "</b></i>";
  //infoStr += "<br/>  (" + globalNumVisible + " services shown on this map)";

  $("#statusAddr").html(infoStr);
}

var FilterData =
{
  "Pantry" :
  {
    imageQuery: "d_map_pin_letter",
    imageCode:  "P",
    imageColor: "0099FF"
  },

  "Eats" :
  {
    imageQuery: "d_map_pin_letter",
    imageCode:  "E",
    imageColor: "FF3300"
  },

  "Both" :
  {
    imageQuery: "d_map_spin",
    imageCode:  "0.7|0|FF42FF|12|b|P/E",
    imageColor: ""
  },

  "UserLoc" :
  {
    imageQuery: "d_map_pin_icon",
    imageCode:  "location",
    imageColor: "00FF00"
  }
}

function getFilterName(place)
{
  if (place.hasMeal == "True") {
    if (place.hasPantry == "True") {
      return "Both";
    } else {
      return "Eats";
    }
  }
  if (place.hasPantry) {
    return "Pantry";
  }

  // Error
  return "";
}

function getImageStr(filter, is3D)
{
  var imgUrl = "http://chart.apis.google.com/chart?chst=";
  var entry = FilterData[filter];
  imgUrl += entry.imageQuery;
  if (is3D && (filter != "Both")) {
    imgUrl += "_withshadow";
  }
  imgUrl += "&chld=";
  imgUrl += entry.imageCode;
  if (entry.imageColor != "") {
    imgUrl += "|";
    imgUrl += entry.imageColor;
  }
  return imgUrl;
}


function addFilterCheck(filterName, displayOnly)
{
  var root = document.getElementById("markerFilters");

  var span = document.createElement("span");
  root.appendChild(span);

  var img = document.createElement("img");
  img.src = getImageStr(filterName);
  span.appendChild(img);

  if (displayOnly) {
    span.appendChild(document.createTextNode(filterName));
    return;
  }

  var input = document.createElement("input");
  input.type = "checkbox";
  input.name = "checkbox" + filterName;
  input.id = input.name;
  input.onclick = function()
  {
    toggleFilterCheck(filterName, this.checked);
  };
  //input.setAttribute("class", "largerCheck");

  span.appendChild(input);
  input.checked = "checked";

  var label = document.createElement("label");
  span.appendChild(label);
  label.setAttribute("for", input.id);
  label.style.color = "#" + FilterData[filterName].imageColor;
  label.appendChild(document.createTextNode("Show " + filterName));

//  span.onmouseover = function() {
//    $("#filter" + folder.name).removeClass("hidden");
//  }
//  span.onmouseout = function() {
//    $("#filter" + folder.name).addClass("hidden");
//  }
}

function round(value, decimal)
{
  var scale = Math.pow(10, decimal);
  return Math.round(value * scale) / scale;
}

function computeMarkerDist()
{
  var orig = geolib.getPos();

  for (i = 0; i < DATA.Markers.length; i++)
  {
    var place = DATA.Markers[i];

    if (!place.marker) {
      continue;
    }

    var pos = place.marker.getPosition();
    place.dist = google.maps.geometry.spherical.computeDistanceBetween(orig, pos);

    var miles = place.dist / 1609.344;
    place.distDesc = String(miles);
    place.distDesc = round(place.distDesc, 1);
    place.distDesc += "mi. Away";
  }
}


function toggleFilterCheck(name, state)
{
  theInfoWin.close();

  for (i = 0; i < DATA.Markers.length; i++)
  {
    var place = DATA.Markers[i];

    if (place.marker) {
      place.marker.setVisible(isPlaceVisible(place));
    }
  }

  updateVisiblePlaceList();
}

function selectMarker(i, elem)
{
  var marker = DATA.Markers[i].marker;
  if (marker) {
    google.maps.event.trigger(marker, "click");
    geolib.getMap().setCenter(marker.getPosition());
  }
}

/*
      function setSelectedPlace(i, j)
      {
        if (selectedPlaceId) {
          var oldSel = document.getElementById(selectedPlaceId);
          if (oldSel) {
            oldSel.setAttribute("class", "visiblePlace");
          }
        }

        selectedPlaceId = i + ", " + j;

        var newSel = document.getElementById(selectedPlaceId);
        if (newSel) {
          var scrollOff = $(newSel).position().top - $("#data_list").position().top;
          //alert(scrollOff);
          $("#data_list").scrollTop(scrollOff);
          newSel.setAttribute("class", "selectedPlace");
        }
      }
 */
function getAddressString(place, lineBr)
{
  var full = place.address;
  if (place.address != "") {
    full += ", ";
  }

  full += lineBr;
  
  if (place.city != "") {
    full += place.city + ", " + place.state;
  }
  full += " " + place.zip;
  return full;
}

function getSpanString(spanClass, contents)
{
  return "<span class='" + spanClass + "' + id='" + spanClass + "'>" + contents + "</span>";
}

function getHiddenFieldString(name, value)
{
  return "<input type='hidden' name='" + name + "' id='" + name + "' value='" + value + "' />";
}

function zoomTo(elem, zoom)
{
  var idStr = $(elem).parent().attr("id");

  zoomToId(idStr, zoom);
}

function zoomToId(idStr, zoom)
{
  var place = getPlaceInfo(idStr);

  if (!place) {
    return;
  }
  
  geolib.getMap().setCenter(place.marker.getPosition());
  geolib.getMap().setZoom(zoom);
}

function getPlaceHtml(place, isInInfoWin)
{
  var descText = "";

  // Hidden Dist
  if (place.dist) {
    descText += getHiddenFieldString("hiddenDist", place.dist);
  }

  var imgStr = "<img src=\"" + getImageStr(getFilterName(place)) + "\"/>";
  //  if ((place.hasPantry == "True")) {
  //    imgStr += "<img src=\"" + getImageStr("Pantry") + "\"/>";
  //    // To avoid setting the icon twice...
  //    if (place.hasMeal != "True") {
  //      place.marker.setIcon(getImageStr("Pantry", true));
  //    }
  //  }
  //  if ((place.hasMeal == "True")) {
  //    imgStr += "<img src=\"" + getImageStr("Eats") + "\"/>";
  //    place.marker.setIcon(getImageStr("Eats", true));
  //  }

  if (place.distDesc && (place.address != "")) {
    var dirLink = "";

    dirLink = "<br/><a href='#' onclick='zoomTo(this.parentNode, 15)'>Zoom Here</a>";
    dirLink += "<br/><a href='#' onclick='showDirections($(this).parent().parent().attr(\"id\"))'>Directions</a>";
    descText += getSpanString("placeDist", place.distDesc + dirLink);
    
  } else if (place.address == "") {
    descText += getSpanString("placeDist", "Citywide Service<br/>Call For Info");
  }

  if (place.name) {
    descText += getSpanString("placeName", imgStr + place.name);
  }

  //var links = "";//<a href='#' onclick='zoomTo(this, 11)'>Zoom To City</a>";


  descText += "<div style='clear: both'></div>";
  //descText += "</div>";
  //descText += "<br/>";

  if (place.address) {
    descText += getSpanString("placeAddress", getAddressString(place, "<br/>"));
    descText += "<br/>";
  }

  if (place.phone) {
    descText += getSpanString("placePhone", place.phone);
    descText += "<br/>";
  }

  if (place.description) {
    descText += getSpanString("placeDesc", "<p>" + place.description + "</p>");
  }

  return descText;
}

function hoverListItem(inOut, rootDiv)
{
  //var dirDiv = $("//span[id=placeDir]", rootDiv);
  if (inOut) {
    $(rootDiv).addClass("placeHover");
  //dirDiv.html("<br/><a href='#'>Show Directions</a>");
  } else {
    $(rootDiv).removeClass("placeHover");
  //dirDiv.html("");
  }
}

function createLI(place, i)
{
  var newLI = document.createElement("li");

  var id = i;
  newLI.setAttribute("id", id);

  var onclickSelectMarker = "onclick=" + "\"selectMarker(";
  onclickSelectMarker += id + ", this)\"";
  onclickSelectMarker += " onmouseover = 'hoverListItem(true, this)' onmouseout='hoverListItem(false, this)' ";

  var descText = getPlaceHtml(place, false);

  //newLI.innerHTML = "<a " + onclickSelectMarker + "href='#'>" + descText + "</a>";
  $(newLI).html("<div id='" + id + "' " + onclickSelectMarker + ">" + descText + "</div>");

  //place.theLI = newLI;

  if (place.dist == undefined) {
    rootUL.appendChild(newLI);
    return;
  }

  var newDist = place.dist;

  for (c = 0; c < rootUL.childNodes.length; c++)
  {
    var dist = parseFloat($("//input[id='hiddenDist']", rootUL.childNodes[c]).attr("value"));

    if (newDist < dist) {
      rootUL.insertBefore(newLI, rootUL.childNodes[c]);
      return;
    }
  }

  rootUL.appendChild(newLI);
}

function getPlaceInfo(idStr)
{
  var i = parseInt(idStr);
  if (isNaN(i)) {
    return null;
  }

  if (i >= DATA.Markers.length) {
    return null;
  }

  return DATA.Markers[i];
}

function isPlaceVisible(place, bounds)
{
  var show = false;

  if ((place.hasMeal == "True") && document.getElementById("checkboxEats").checked) {
    show = true;
  }

  if ((place.hasPantry == "True") && document.getElementById("checkboxPantry").checked) {
    show = true;
  }

  if (!show) {
    return false;
  }

  if (bounds && (place.address != "") && !bounds.contains(place.marker.getPosition())) {
    return false;
  }

  //  if (!place.marker.getVisible()) {
  //    return false;
  //  }

  return true;
}

function updateVisiblePlaceList()
{
  if (!markersLoaded || !rootUL) {
    return;
  }

  var count = 0;
  var accumHeight = 0;
  var selFound = false;

  var bounds = geolib.getMap().getBounds();

  for (c = 0; c < rootUL.childNodes.length; c++)
  {
    var idStr = rootUL.childNodes[c].id;
    var place = getPlaceInfo(idStr);

    if (!place) {
      continue;
    }

    var selChild;

    var visible = isPlaceVisible(place, bounds);
    if (visible) {
      count++;
      if (selectedPlaceId && (idStr == selectedPlaceId)) {
        selChild = rootUL.childNodes[c];
        selFound = true;
      }
      if (!selFound) {
        accumHeight += $(rootUL.childNodes[c]).height();
      }

      $(rootUL.childNodes[c]).removeClass("hidden");
    } else {
      $(rootUL.childNodes[c]).addClass("hidden");
    }
  }

  if (selChild) {
  //accumHeight += $("#data_list").position().top;
  //$("#data_list").scrollTop(accumHeight);
  }

  globalNumVisible = count;
  updateInfoStatus();
}

var userPos;

function buildVisiblePlaceList(pos)
{
  if (!markersLoaded) {
    return;
  }

  if (userPos && userPos.equals(pos)) {
    return;
  }

  computeMarkerDist();

  var root = document.getElementById("data_list");

  if (root.childNodes.length > 0) {
    root.removeChild(root.childNodes[0]);
  }
  rootUL = document.createElement("ul");
  root.appendChild(rootUL);

  //var bounds = geolib.getMap().getBounds();

  //var count = 0;

  for (i = 0; i < DATA.Markers.length; i++)
  {
    createLI(DATA.Markers[i], i);
  }

  //document.getElementById("statusFound").innerHTML = "<b>Showing " + count + " Free Services on this map.</b>";
  updateVisiblePlaceList();
}

//var allPlaces;

function loadAllMarkers()
{
  var map = geolib.getMap();

  addFilterCheck("Pantry");
  addFilterCheck("Eats");
  addFilterCheck("Both", true);

  for (i = 0; i < DATA.Markers.length; i++)
  {
    var place = DATA.Markers[i];

    // Skip Markers with No Address
    if (place.address == "") {
      continue;
    }

    place.marker = new google.maps.Marker(
    {
      map: map,
      position: geolib.parseLatLong(place.Point.coordinates, true),
      draggable: false,
      title: place.name + "\n" + getAddressString(place, "\n"),
      icon: getImageStr(getFilterName(place), true)
    });

    function doOpen(win, place, i)
    {
      return function()
      {
        var idStr = i;
        var divWrap = "<div id='" + idStr + "'> " + getPlaceHtml(place, true) + "</div>";
        win.setContent(divWrap);
        win.open(map, place.marker);

        selectedPlaceId = i;
      }
    }

    google.maps.event.addListener(place.marker, "click", doOpen(theInfoWin, place, i));
  }

  markersLoaded = true;
}

function showClicked()
{
  hideDirections();
  $("addressError").text("");
  if (!geolib.syncToEnteredAddress()) {
    geolib.getMap().setCenter(geolib.getPos());
  }
  document.getElementById("address").value = globalUserAddress;

  geolib.getMap().setZoom(15);
}

function fixSizes()
{
  var margin = parseInt($(document.body).css("marginBottom"));
  $("#floatSplitter").height($(window).height() - margin - $('#floatSplitter').position().top);
  $("#data_list").height($("#leftDiv").height() - $("#data_list").position().top);
}

var currDirId;

var globalDirPaths;
var globalDirTexts;

var dirBounds = null;

function overrideGoogleDefaults()
{
  var icons = $("img", $("table.adp-placemark", $("#jsts")));
  $(icons[0]).attr("jsvalues", ".src:globalDirPaths[$waypointIndex]");
  $(icons[0]).removeAttr("jstcache");

  var texts = $(".adp-text", $("#jsts"));
  $(texts).attr("jscontent", "globalDirTexts[$waypointIndex]");
  $(texts).removeAttr("jstcache");
}

var directionsMode = null;

function showDirections(idStr, newDirMode)
{
  var place = getPlaceInfo(idStr);

  if (!place || !place.marker) {
    return;
  }

  currDirId = idStr;

  if (newDirMode) {
    if (newDirMode == "Drive")
      directionsMode = google.maps.DirectionsTravelMode.DRIVING;
    else if (newDirMode == "Walk")
      directionsMode = google.maps.DirectionsTravelMode.WALKING;
    else if (newDirMode == "Bike")
      directionsMode = google.maps.DirectionsTravelMode.BICYCLING;
  }
  if (directionsMode == null) {
    directionsMode = google.maps.DirectionsTravelMode.DRIVING;
  }
  
  var start = geolib.getMarker().getTitle();
  //var end = getAddressString(place, "");
  var end = place.marker.getPosition();

  var request = {
    origin: start,
    destination: end,
    travelMode: directionsMode
  };

  dirService.route(request, function(result, status) {
    if (status == google.maps.DirectionsStatus.OK) {

      var startHtml = "<img src=\"" + getImageStr("UserLoc") + "\"/>";
      startHtml += geolib.getMarker().getTitle();

      var endHtml = "";
      endHtml += "<img src=\"" + getImageStr(getFilterName(place)) + "\"/>";
      endHtml += "<b>" + place.name + "</b><br/>";
      endHtml += getAddressString(place, "<br/>");
      //endHtml += "<br/><br/><br/>" + "<span style='font-size: small'>" + result.routes[0].copyrights + "</span>";

      //globalDirPaths = [getImageStr("UserLoc"), getImageStr(getFilterName(place))];
      //globalDirTexts = [geolib.getMarker().getTitle(), endHtml];
      //overrideGoogleDefaults();

      dirDisplay.setDirections(result);
      dirDisplay.setMap(geolib.getMap());

      if (result.routes && result.routes[0]) {
        dirBounds = result.routes[0].bounds;
      } else {
        dirBounds = null;
      }

      //$("img", $("table.adp-placemark", $("#dirDivContent"))).attr("jsvalues", ".src:'" + getImageStr("UserLoc") + "'");

      $("#dirDivStartMark").html(startHtml);
      $("#dirDivEndMark").html(endHtml);
      
      $("#dirDiv").removeClass("hidden");
      $("#keyHeader").addClass("hidden");
      $("#data_list").addClass("hidden");

      $("table.adp-placemark").each(function(index, elem)
      {
        $(elem).addClass("hidden");
      });

      $(".adp-legal").each(function(index, elem)
      {
        $(elem).addClass("hidden");
      });

      theInfoWin.close();
    }
  });
}
function zoomDirections()
{
  if (dirBounds) {
    geolib.getMap().fitBounds(dirBounds);
  }
}

function hideDirections()
{
  currDirId = "";
  $("#dirDiv").addClass("hidden");
  $("#keyHeader").removeClass("hidden");
  $("#data_list").removeClass("hidden");
  dirDisplay.setMap(null);
}

$(document).ready(function()
{
  $(window).bind("resize", fixSizes);

  init();

  fixSizes();

  $("#floatSplitter").splitter({
    minLeft: 300,
    sizeLeft: 500,
    minRight: 100,
    resizeToWidth: true
  });

  $("#map_canvas").bind("resize", function()
  {
    google.maps.event.trigger(geolib.getMap(), "resize");
  });

  $("#data_list").height($("#floatSplitter").height() - $("#data_list").position().top);
});

