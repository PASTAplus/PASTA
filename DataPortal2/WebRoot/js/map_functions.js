/**
  *   '$RCSfile$'
  *   Copyright:   2005 University of New Mexico
  *
  *   '$Author: dcosta $'
  *   '$Date: 2008-04-07 16:39:07 -0400 (Mon, 07 Apr 2008) $'
  *   '$Revision: 1926 $'
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  */

/*
 * Initialize the Google map on the Advanced Search page
 */
function initialize() {	
	var mapOptions = {
			center: new google.maps.LatLng(0, -106.67648),
			zoom: 0,
			mapTypeId: google.maps.MapTypeId.HYBRID,
			mapTypeControl: true,
			panControl: true,
			scaleControl: true,
			streetViewControl: false,
			zoomControl: true
	};
  
	var map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
        
	// Add map listener for map moves
	google.maps.event.addListener(map, "bounds_changed", function() {
		var bounds = map.getBounds();
		var northeast = bounds.getNorthEast();
		var southwest = bounds.getSouthWest();
		
		// Set form values of the map extent
		document.advancedSearchForm.northBound.value = northeast.lat();
		document.advancedSearchForm.eastBound.value = northeast.lng();
		document.advancedSearchForm.southBound.value = southwest.lat();
		document.advancedSearchForm.westBound.value = southwest.lng();
    
		if (boundsChangedCount == 0) {
			document.advancedSearchForm.northBound.value = 90.0;
			document.advancedSearchForm.southBound.value = -90.0;
		}

		// Set to "1" for initial map load
		boundsChangedCount++;
		document.advancedSearchForm.boundsChangedCount.value = boundsChangedCount;
	});
  
	map.enableKeyDragZoom();
}


/*
 * Initialize the Google map on the Data Package Summary page
 */
function initialize_summary_map(coordinatesArray, north, south, east, west) {
	var lat = (north + south)  / 2.0;
	var lng = (west + east) / 2.0;

    var mapOptions = {
			center : new google.maps.LatLng(lat, lng),
			zoom : 9,
			mapTypeId : google.maps.MapTypeId.TERRAIN,
            disableDefaultUI: true,
            navigationControlOptions: { style: google.maps.NavigationControlStyle.SMALL },
            keyboardShortcuts: true
	};

	var map = new google.maps.Map(document.getElementById("map-canvas-summary"), mapOptions);
	var maxNorth = -999;
	var minSouth = 999;
	var maxEast = -999;
	var minWest = 999;
	
	for (var i = 0; i < coordinatesArray.length; i++) {
		var coord = coordinatesArray[i];
		
		var north = Number(coord.north);
		maxNorth = Math.max(maxNorth, north);
		
		var south = Number(coord.south);
		minSouth = Math.min(minSouth, south);
		
		var east = Number(coord.east);
		maxEast = Math.max(maxEast, east);
		
		var west = Number(coord.west);
		minWest = Math.min(minWest, west);
		
	    var nwLatLng = new google.maps.LatLng(north, west);
	    var seLatLng = new google.maps.LatLng(south, east);

	    if ((north == south) && (east == west)) {
	    	var nwMarker = new google.maps.Marker({position: nwLatLng, map: map, title: nwLatLng.toString() });
	    }
	    else {
	    	var bounds = new google.maps.LatLngBounds(nwLatLng, seLatLng);
	    	var rect = new google.maps.Rectangle({ map: map, bounds: bounds, strokeWeight: 0.5, strokeColor: 'black', strokeOpacity: 1, fillColor: 'yellow', fillOpacity: 0.30 });
	    	map.fitBounds(bounds);
	    }
	}

    if ((maxNorth != minSouth) || (maxEast != minWest)) {
    	var maxNwLatLng = new google.maps.LatLng(maxNorth, minWest);
    	var maxSeLatLng = new google.maps.LatLng(minSouth, maxEast);
    	var maxBounds = new google.maps.LatLngBounds(maxNwLatLng, maxSeLatLng);
    	map.fitBounds(maxBounds);
    }
}
