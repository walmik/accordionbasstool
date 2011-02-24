/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


var geolib = (function() {

  var geocoder;

  var usingGearsGeoloc = false;

  var map;
  var mapMarker;

  var formAddress;
  var formPos;
  var initialLoc;

  var geolocLatLng;
  var geolocAccuracy;

  var isWaitingForResponse = false;
  var statusDiv = undefined;
  var errorDiv = undefined;

  var acceptPosChange = function() {
    return true;
  }

  var markerPosChanged = function() {}
  var markerAddrChanged = function() {}


  function LocAddr(pos, address)
  {
    this.pos = pos;
    this.address = address;
  }

  //=================================================================================
  //Main Entry Point
  //=================================================================================
  function init(options)
  {
    createMap(options.mapDivName);

    geocoder = new google.maps.Geocoder();

    acceptPosChange = options.acceptPosChange;
    markerPosChanged = options.markerPosChanged;
    markerAddrChanged = options.markerAddrChanged;

    if (options.formAddressName != undefined) {
      formAddress = document.getElementById(options.formAddressName);
      formAddress.onkeydown = onAddressTextKey;
    }

    if (options.formPosName != undefined) {
      formPos = document.getElementById(options.formPosName);
    }

    if ((formAddress != undefined) && (formPos != undefined)) {
      initialLoc = {
        pos: parseLatLong(formPos.value),
        address: formAddress.value
      };
    }
	
	if (options.errorDivName != undefined) {
	  errorDiv = document.getElementById(options.errorDivName);
	}

    showInitialAddr(options);
  }

  //=================================================================================
  // Create the Google Maps object
  //=================================================================================
  function createMap(mapDivName)
  {
    var mapInitOptions = {
      zoom: 15,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    map = new google.maps.Map(document.getElementById(mapDivName), mapInitOptions);

    mapMarker = new google.maps.Marker(
    {
      map: map,
      draggable: true
    });

    // Events

    google.maps.event.addListener(mapMarker, "dragend", function()
    {
      var newPos = mapMarker.getPosition();
      
      if (acceptPosChange(newPos)) {
        mapPos = newPos;
        markerPosChanged(mapPos);
        findAddrForPos(mapPos);
      } else {
        mapMarker.setPosition(mapPos);
      }
    });

    google.maps.event.addListener(mapMarker, "click", function()
    {
      if (mapMarker.getTitle() != "") {
		errorSet = true;
        clearErrorStatus();
      }

      if (formAddress != null) {
        formAddress.value = mapMarker.getTitle();
      }
    });
  }

  //=================================================================================
  // Parse maps.LatLng object from a String
  //=================================================================================
  function parseLatLong(posStr, flipCoord)
  {
    if ((posStr == undefined) || (posStr == "")) {
      return null;
    }

    var posArr = posStr.split(",", 2);
    var F0 = 0;
    var F1 = 1;
    
    if (flipCoord) {
      F0 = 1;
      F1 = 0;
    }

    if (posArr instanceof Array) {
      var posObj = new google.maps.LatLng(
        parseFloat(posArr[F0], 10),
        parseFloat(posArr[F1], 10));

      return posObj;
    }

    return null;
  }

  //=================================================================================
  // Shows the initial address, if any, on the map.
  // If no initial address is set, attempt to show the user's current location
  //=================================================================================
  function showInitialAddr(options)
  {
    if ((initialLoc != undefined) && (initialLoc.address.length > 0)) {
      if (initialLoc.pos == null) {
        findPosForAddr(initialLoc.address, true);
      } else {
        setAddress(initialLoc.address);
        setPos(initialLoc.pos);
      }
      if (options.alwaysLookUpUserAddress) {
        findUserLoc();
      }
    } else {
      findUserLoc();
    }
  }

  //=================================================================================
  // GeoLoc - Determine User Location
  // Attempt using W3C browser built in first, then Google Gears
  //=================================================================================
  function findUserLoc()
  {

    if ((geolocLatLng != undefined) && (geolocLatLng instanceof google.maps.LatLng)) {
      setPos(geolocLatLng);
      findAddrForPos(mapPos);
      return;
    }

    geolocAccuracy = -1; //to indicate we're waiting on ajax response

    // First, try default (W3C standard) geolocation
    // If that's not found, or it fails, try the google gears geolocator

    try {
      if (usingGearsGeoloc || (navigator.geolocation == undefined)) {

        var geoLocCallOptions = {
          enableHighAccuracy: true,
          timeout: 5000,
          gearsRequestAddress: false
        };

        gl = google.gears.factory.create('beta.geolocation');
        gl.getCurrentPosition(onGeolocSuccess, onGeolocFailure, geoLocCallOptions);

        usingGearsGeoloc = true;
      } else if (navigator.geolocation != undefined) {

        var geoLocCallOptions = {
          enableHighAccuracy: true,
          timeout: 1000,
          gearsRequestAddress: false
        };

        gl = navigator.geolocation;
        gl.getCurrentPosition(onGeolocSuccess, onGeolocFailure, geoLocCallOptions);
      } else {
        //TODO: mention there is no geoloc?
        setErrorStatus("geoloc", "");
      }

    } catch (e) {
      setErrorStatus("geoloc", e.toString());
    }
  }

  //=================================================================================
  // GeoLoc - Failure
  //=================================================================================
  function onGeolocFailure(posError)
  {
    geolocAccuracy = 0;
    geolocLanLng = undefined;

    // if not using gears, attempt one more time using the gears geoLoc
    if (!usingGearsGeoloc) {
      usingGearsGeoloc = true;
      findUserLoc();
    } else {
      //TODO: Pick a default location?
      setErrorStatus("geoloc", String(posError.message));
    }
  }

  //=================================================================================
  // GeoLoc - Success
  //=================================================================================
  function onGeolocSuccess(geolocPos)
  {
    geolocLatLng = new google.maps.LatLng(geolocPos.coords.latitude, geolocPos.coords.longitude);
    geolocAccuracy = geolocPos.coords.accuracy;

    if (acceptPosChange(geolocLatLng)) {
      setPos(geolocLatLng);
      findAddrForPos(mapPos);
    }
  }


  //=================================================================================
  // GeoCoding - Find Address for Long/Lat pos
  //=================================================================================

  function findAddrForPos(pos)
  {
    setWaitingForResponse(true, "Updating address...");
    geocoder.geocode({
      latLng: pos
    }, function(responses)

    {
        if (responses && responses.length > 0) {
          setAddress(responses[0].formatted_address);
          clearErrorStatus();
        } else {
          setAddress("");
          setErrorStatus("geocode", "Unable to find address for this location.");
        }
        setWaitingForResponse(false, "The address for this location is:");
      });
  }

  //=================================================================================
  // Find Pos from Address
  //=================================================================================
  function findPosForAddr(addressString, resetToUserLoc)
  {
    setWaitingForResponse(true, "Finding map for this address.");
    geocoder.geocode({
      address: addressString
    }, function(responses)

    {
        if (responses && responses.length > 0) {
          if (acceptPosChange(responses[0].geometry.location)) {
            setPos(responses[0].geometry.location, responses[0].geometry.viewport);
            setAddress(responses[0].formatted_address);
          }
          clearErrorStatus();
        } else if (resetToUserLoc) {
          findUserLoc();
        } else {
          setErrorStatus("geocode", "Unable to find this address on the map. Please check the address and try again.");
        }
        setWaitingForResponse(false, "Found the address on the map. If the map is not quite correct, you can click on the pin and place it more accurately");
      });
  }

  //=================================================================================
  // Error Handling and Display
  //=================================================================================
  var errorSet = false;

  //=================================================================================
  function setErrorStatus(type, statusText)
  {
    errorSet = true;
    if ((errorDiv != undefined) && (type == "geocode")) {
      setTextContent(errorDiv, statusText);
    }
  }

  //=================================================================================
  function setTextContent(elem, statusText)
  {
    if (elem.textContent != undefined) {
      elem.textContent = statusText;
    } else if (elem.innerText != undefined) {
      elem.innerText = statusText;
    } else {
      document.write(statusText);
    }
  }

  //=================================================================================
  function clearErrorStatus()
  {
    if (errorSet) {
      setErrorStatus("", "");
      errorSet = false;
    }
  }

  //=================================================================================
  // GeoCode - Set the current address string
  //=================================================================================
  function setAddress(text)
  {
    if (formAddress != undefined) {
      formAddress.value = text;
    }

    markerAddrChanged(text);
    
    mapMarker.setTitle(text);
  }

  //=================================================================================
  // GeoCode - Set the current long/lat pos
  //=================================================================================
  function setPos(pos, doPan)
  {
    mapPos = pos;
	
    if (doPan) {
      map.panTo(mapPos);
    } else {
      map.setCenter(mapPos);
    }

    if (formPos != undefined) {
      formPos.value = pos.toUrlValue();
    }
	
    mapMarker.setPosition(mapPos);
    markerPosChanged(mapPos);
  }

  //=================================================================================
  function setWaitingForResponse(waiting, message)
  {
    isWaitingForResponse = waiting;
    if (statusDiv != undefined) {
      setTextContent("statusMsg", message);
    }
  }

  //=================================================================================
  // Form Handling - Check if enter key was pressed, if so, update position from address
  // Clear error state and don't submit yet
  //=================================================================================
  function onAddressTextKey(event)
  {
    var key;
    if (window.event) {
      key = window.event.keyCode; //IE
    } else {
      key = event.which; //firefox
    }

    if (key != 13) {
      clearErrorStatus();
      return true;
    }

    doAddrTextUpdate();
    return false;
  }

  //=================================================================================
  // Form Handling - Update the position on map from text field.
  // Runs when 'Update' button click or the 'Enter' is pressed
  //=================================================================================
  function doAddrTextUpdate()
  {
    // minor optimization, make sure text actually changed before updating
    addressString = formAddress.value;

    if (addressString != mapMarker.getTitle()) {
      findPosForAddr(addressString, false);
	  return true;
    }
	
	return false;
  }

  //=================================================================================
  // Public Interface
  //=================================================================================
  var PUBLIC = {};
  PUBLIC.init = init;
  PUBLIC.parseLatLong = parseLatLong;
  PUBLIC.LocAddr = LocAddr;
  
  PUBLIC.getInitialLoc = function() {
    return initialLoc;
  }

  PUBLIC.getGeoCoder = function() {
    return geocoder;
  }
  
  PUBLIC.getMarker = function() {
    return mapMarker;
  };

  PUBLIC.getMap = function() {
    return map;
  };

  PUBLIC.getPos = function() {
    return mapMarker.getPosition();
  }
  
  PUBLIC.syncToEnteredAddress = doAddrTextUpdate;

  return PUBLIC;

}());