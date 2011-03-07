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

function init()
{
  // Init SF Bounds
  var sw = new google.maps.LatLng(37.70567075998761, -122.51793249606325);
  var ne = new google.maps.LatLng(37.83051256186211, -122.361377320282);
  SFBounds = new google.maps.LatLngBounds(sw, ne);

  geolib.init(
  {
    mapDivName: 'map_canvas',
    formAddressName: 'address',
    formPosName: 'posLatlng',
    alwaysLookUpUserAddress: true,
    acceptPosChange: acceptPosChange,
    markerPosChanged: onMarkerPosChanged,
    markerAddrChanged: onMarkerAddrChanged,
    errorDivName: "addressError"
  });

  theInfoWin = new google.maps.InfoWindow();

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

  "Shelter" :
  {
    imageQuery: "d_map_pin_letter",
    imageCode:  "S",
    imageColor: "FFEE00"
  },

  "UserLoc" :
  {
    imageQuery: "d_map_pin_icon",
    imageCode:  "location",
    imageColor: "00FF00"
  }
}


//var imageUrls2D =
//{
//  "Pantry": "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|0099FF",
//  "Eats" : "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=E|FF3300",
//  "Shelter" : "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=S|FFFF00",
//  "MyLoc" : "http://chart.apis.google.com/chart?chst=d_map_pin_icon&chld=location|00FF00"
//};
//
//var imageUrls3D =
//{
//  "Pantry": "http://chart.apis.google.com/chart?chst=d_map_pin_letter_withshadow&chld=P|0099FF",
//  "Eats" : "http://chart.apis.google.com/chart?chst=d_map_pin_letter_withshadow&chld=E|FF3300",
//  "Shelter" : "http://chart.apis.google.com/chart?chst=d_map_pin_letter_withshadow&chld=S|FFFF00",
//  "MyLoc" : "http://chart.apis.google.com/chart?chst=d_map_pin_icon_withshadow&chld=location|00FF00"
//};
//
//var filterColors =
//{
//  "Pantry" : "0099FF",
//  "Eats" : "FF3300",
//  "Shelter" : "FFCC00",
//  "MyLoc" : "00FF00"
//};

function getImageStr(filter, is3D)
{
  var imgUrl = "http://chart.apis.google.com/chart?chst=";
  var entry = FilterData[filter];
  imgUrl += entry.imageQuery;
  if (is3D) {
    imgUrl += "_withshadow";
  }
  imgUrl += "&chld=";
  imgUrl += entry.imageCode;
  imgUrl += "|";
  imgUrl += entry.imageColor;
  return imgUrl;
}


function addFilterCheck(index, folder)
{
  var root = document.getElementById("markerFilters");

  var span = document.createElement("span");
  root.appendChild(span);

  var img = document.createElement("img");
  img.src = getImageStr(folder.name);
  span.appendChild(img);

  var input = document.createElement("input");
  input.type = "checkbox";
  input.name = "checkbox" + folder.name;
  input.id = input.name;
  input.onclick = function()
  {
    toggleFilterCheck(folder.name, this.checked);
  };
  //input.setAttribute("class", "largerCheck");

  span.appendChild(input);
  input.checked = "checked";

  var label = document.createElement("label");
  span.appendChild(label);
  label.setAttribute("for", input.id);
  label.style.color = "#" + FilterData[folder.name].imageColor;
  label.appendChild(document.createTextNode("Show " + folder.name));

  span.onmouseover = function() {
    $("#filter" + folder.name).removeClass("hidden");
  }
  span.onmouseout = function() {
    $("#filter" + folder.name).addClass("hidden");
  }
}

function round(value, decimal)
{
  var scale = Math.pow(10, decimal);
  return Math.round(value * scale) / scale;
}

function computeMarkerDist()
{
  var orig = geolib.getPos();

  for (i = 0; i < DATA.Markers.Folder.length; i++)
  {
    var folder = DATA.Markers.Folder[i];

    for (j = 0; j < folder.Placemark.length; j++)
    {
      var place = folder.Placemark[j];

      var pos = place.marker.getPosition();
      place.dist = google.maps.geometry.spherical.computeDistanceBetween(orig, pos);

      var miles = place.dist / 1609.344;
      place.distDesc = String(miles);
      place.distDesc = round(place.distDesc, 1);
      place.distDesc += "mi. Away";
    }
  }
}


function toggleFilterCheck(name, state)
{
  theInfoWin.close();

  for (i = 0; i < DATA.Markers.Folder.length; i++)
  {
    var folder = DATA.Markers.Folder[i];

    if (folder.name == name) {
      for (j = 0; j < folder.Placemark.length; j++)
      {
        var place = folder.Placemark[j];
        place.marker.setVisible(state);
      }
    }
  }

  updateVisiblePlaceList();
}

function selectMarker(i, j, elem)
{
  var marker = DATA.Markers.Folder[i].Placemark[j].marker;
  google.maps.event.trigger(marker, "click");
  geolib.getMap().setCenter(marker.getPosition());
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
function getAddressString(place)
{
  var full = place.address;
  if (place.address != "")
    full += ", ";
  full += "<br/>";
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

  var result = getPlaceInfo(idStr);
  if (result) {
    geolib.getMap().setCenter(result.place.marker.getPosition());
  }
  geolib.getMap().setZoom(zoom);
}

function getPlaceHtml(place, i, isInInfoWin)
{
  var descText = "";

  // Hidden Dist
  if (place.dist) {
    descText += getHiddenFieldString("hiddenDist", place.dist);
  }

  var imgStr = "<img src=\"" + getImageStr(DATA.Markers.Folder[i].name) + "\"/>";

  if (place.distDesc) {
    var dirLink = "";
    if (isInInfoWin) {
    //dirLink = "<br/><a href='#'>Directions</a>";
    }

    dirLink = "<br/><a href='#' onclick='zoomTo(this.parentNode, 15)'>Zoom Here</a>";
    descText += getSpanString("placeDist", place.distDesc + dirLink);
  }

  if (place.name) {
    descText += getSpanString("placeName", imgStr + place.name);
  }

  //var links = "";//<a href='#' onclick='zoomTo(this, 11)'>Zoom To City</a>";


  descText += "<div style='clear: both'></div>";
  //descText += "</div>";
  //descText += "<br/>";

  if (place.address) {
    descText += getSpanString("placeAddress", getAddressString(place));
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

function createLI(place, i, j)
{
  var newLI = document.createElement("li");

  var id = i + ", " + j;
  newLI.setAttribute("id", id);

  var onclickSelectMarker = "onclick=" + "\"selectMarker(";
  onclickSelectMarker += id + ", this)\"";
  onclickSelectMarker += " onmouseover = 'hoverListItem(true, this)' onmouseout='hoverListItem(false, this)' ";

  var descText = getPlaceHtml(place, i, false);

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
  var vals = idStr.split(",", 2);
  if (!vals) {
    return null;
  }

  var i = parseInt(vals[0]);
  var j = parseInt(vals[1]);

  if (i >= DATA.Markers.Folder.length) {
    return null;
  }

  var folder = DATA.Markers.Folder[i];

  if (j >= folder.Placemark.length) {
    return null;
  }

  var place = folder.Placemark[j];

  return {
    "place" : place,
    "folder" : folder
  };
}

function isPlaceVisible(folder, place, bounds)
{
  if (!folder.name || !document.getElementById("checkbox" + folder.name).checked) {
    return false;
  }

  if (!place.marker.getVisible()) {
    return false;
  }

  if (!bounds.contains(place.marker.getPosition())) {
    return false;
  }

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
    var result = getPlaceInfo(idStr);

    if (!result) {
      continue;
    }

    var selChild;

    var visible = isPlaceVisible(result.folder, result.place, bounds);
    if (visible) {
      count++;
      if (selectedPlaceId && (idStr == selectedPlaceId)) {
        selChild = rootUL.childNodes[c];
        selFound = true;
      }
      if (!selFound)
        accumHeight += $(rootUL.childNodes[c]).height();
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

  for (i = 0; i < DATA.Markers.Folder.length; i++)
  {
    var folder = DATA.Markers.Folder[i];

    for (j = 0; j < folder.Placemark.length; j++)
    {

      var place = folder.Placemark[j];

      createLI(place, i, j);
    }
  }

  //document.getElementById("statusFound").innerHTML = "<b>Showing " + count + " Free Services on this map.</b>";
  updateVisiblePlaceList();
}

//var allPlaces;

function loadAllMarkers()
{
  var map = geolib.getMap();

  //allPlaces = new Array();
  //var count = 0;

  for (i = 0; i < DATA.Markers.Folder.length; i++)
  {
    var folder = DATA.Markers.Folder[i];

    addFilterCheck(i, folder);

    for (j = 0; j < folder.Placemark.length; j++)
    {
      var place = folder.Placemark[j];

      place.marker = new google.maps.Marker(
      {
        map: map,
        position: geolib.parseLatLong(place.Point.coordinates, true),
        draggable: false,
        title: place.name + "\n" + getAddressString(place),
        icon: new google.maps.MarkerImage(getImageStr(folder.name, true))
      });

      //place.index = count++;
      //allPlaces.push(place);

      function doOpen(win, place, i, j)
      {
        return function()
        {
          var idStr = i + ", " + j;
          var divWrap = "<div id='" + idStr + "'> " + getPlaceHtml(place, i, true) + "</div>";
          win.setContent(divWrap);
          win.open(map, place.marker);

          selectedPlaceId = i + ", " + j;
        }
      }

      google.maps.event.addListener(place.marker, "click", doOpen(theInfoWin, place, i, j));
    }
  }

  markersLoaded = true;
}

function showClicked()
{
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

