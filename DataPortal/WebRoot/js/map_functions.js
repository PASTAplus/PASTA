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


// Declare global variables
var map;
var mapMarkers = [];
var markerHTML = [];
var markersOn = false;

// Setup base icon
/*
var baseIcon = new GIcon();
baseIcon.shadow = "./images/shadow50.png";
baseIcon.iconSize = new GSize(20, 34);
baseIcon.shadowSize = new GSize(37, 34);
baseIcon.iconAnchor = new GPoint(9, 34);
baseIcon.infoWindowAnchor = new GPoint(9, 2);
baseIcon.infoShadowAnchor = new GPoint(18, 25);
*/

var baseIcon = new GIcon();
//baseIcon.shadow = "./images/shadow50.png";
baseIcon.iconSize = new GSize(10,10);
//baseIcon.shadowSize = new GSize(37, 34);
baseIcon.iconAnchor = new GPoint(0,0);
baseIcon.infoWindowAnchor = new GPoint(5,5);
//baseIcon.infoShadowAnchor = new GPoint(18, 25);

function addMarker(lat, lon, site, html) {

	var nIcon = new GIcon(baseIcon);
	nIcon.imageOut  = "./images/circle-green.png";
	nIcon.imageOver = "./images/circle-orange.png";
	nIcon.image = nIcon.imageOut;
	

	// Set up our GMarkerOptions object
	var markerOptions = {};
	markerOptions.title = site;
	markerOptions.icon = nIcon;
    
	var marker = new GMarker(new GLatLng(lat, lon), markerOptions); //Create new marker object
	
	
	GEvent.addListener(marker, "mouseover", function() {
    	marker.setImage(marker.getIcon().imageOver);
  	});
  	GEvent.addListener(marker, "mouseout", function() {
    	marker.setImage(marker.getIcon().imageOut);
  	});
  	
  	if (mapPage == "participating-sites") {
  		
  		GEvent.addListener(marker, "infowindowopen", function() {
    		marker.setImage(marker.getIcon().imageOver);
  		});
  		
  		GEvent.addListener(marker, "infowindowclose", function() {
    		marker.setImage(marker.getIcon().imageOut);
  		});
  		
		GEvent.addListener(marker, "click",	function() {
			marker.openInfoWindowHtml(html);
		});
	}
	
	return marker;
	
}

function toggleMarkers() {

	var marker;

	if (mapPage == "participating-sites") {
		var sideBar = document.getElementById("sideBar");
	}
	
	if (markersOn) {	
		
		map.clearOverlays();
		markersOn = false;
		
	} else {
		
		var id;
		for (id in markers) {
		
			marker = addMarker(markers[id].lat, markers[id].lon, markers[id].site, markers[id].html);
			map.addOverlay(marker);
			mapMarkers.push(marker);
			markerHTML.push(markers[id].html);
			
			if (mapPage == "participating-sites") {

				var anchor = document.createElement("a");
        		anchor.setAttribute("href","javascript:markerClicked('" + id +"')");
        		anchor.style.color = "#000000";
	       		anchor.appendChild(document.createTextNode(markers[id].site));
        		sideBar.appendChild(anchor);
        		sideBar.appendChild(document.createElement("br"));
        		sideBar.appendChild(document.createElement("br"));
			
			}
			
		}
		
		markersOn = true;
	}

}

function markerClicked(markerNum) {
  mapMarkers[markerNum].openInfoWindowHtml(markerHTML[markerNum]);
}


function mapInitialize()
{
    if (GBrowserIsCompatible()) {

		var centerLatitude = 0.0;
		var centerLongitude = -110.0;
		
		if (mapPage == "advancedSearch") {
			var startZoom = 0;
		} else {
			var startZoom = 1;
		}
   	
    	// Create new map
        //map = new GMap2(document.getElementById("map"),{ size: new GSize(300,200) });
        map = new GMap2(document.getElementById("map"));

        if (mapPage == "advancedSearch") {
        
        	// Add map listener for map moves
        	GEvent.addListener(map, "moveend", function() {
				var bounds = map.getBounds();
				var northeast = bounds.getNorthEast();
				var southwest = bounds.getSouthWest();
		
				// Set form values of the map extent
				document.advancedSearchForm.northBound.value = northeast.lat();
				document.advancedSearchForm.eastBound.value = northeast.lng();
				document.advancedSearchForm.southBound.value = southwest.lat();
				document.advancedSearchForm.westBound.value = southwest.lng();
			});
        	
        	// Add map listener for map zooms
        	GEvent.addListener(map, "zoomend", function() {
				var bounds = map.getBounds();
				var northeast = bounds.getNorthEast();
				var southwest = bounds.getSouthWest();
		
				// Set form values of the map extent
				document.advancedSearchForm.northBound.value = northeast.lat();
				document.advancedSearchForm.eastBound.value = northeast.lng();
				document.advancedSearchForm.southBound.value = southwest.lat();
				document.advancedSearchForm.westBound.value = southwest.lng();
			});
		
		}
		
        // Add map controls
        if (mapPage == "advancedSearch") {
        	map.addControl(new GSmallMapControl());
        } else {
        	map.addControl(new GLargeMapControl());
        	map.addControl(new GScaleControl());
        }
        //map.addControl(new GMapTypeControl());
        //map.addControl(new GOverviewMapControl());
        
		// Dragzoom is a Google maps utility
		var boxStyleOpts = {
        	opacity: .2,
        	border: "2px solid red"
        };
        
        var otherOpts = {
          buttonHTML: "<img src='./images/zoom-button.gif' />",
          buttonZoomingHTML: "<img src='./images/zoom-button-activated.gif' />",
          buttonStartingStyle: {width: '24px', height: '24px'}
        };        
        
        if (mapPage == "advancedSearch") {
        	gcp = new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(13,120));
        } else {
        	gcp = new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(23,290));
		}
		
        // Add dragzoom control
        map.addControl(new DragZoomControl(boxStyleOpts, otherOpts), gcp);
        
        
    	var location = new GLatLng(centerLatitude, centerLongitude); //Create new location object
        map.setCenter(location, startZoom, G_HYBRID_MAP); //Set map center

        if (mapPage == "advancedSearch") {
        
			// Set default form values for map extent
			document.advancedSearchForm.northBound.value = "90.0";
			document.advancedSearchForm.eastBound.value = "180.0";
			document.advancedSearchForm.southBound.value = "-90.0";
			document.advancedSearchForm.westBound.value = "-180.0";
		
		}
		
        // Start with markers on
		toggleMarkers();
            
    }
}

window.onload = mapInitialize;
window.onunload = GUnload;