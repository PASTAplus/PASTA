/* Toggle element block on/off */
function eToggle(eName) {
	var e = document.getElementById(eName);
	if (e.style.display == "none") {
		e.style.display = "block";
	} else {
		e.style.display = "none";
	}
}

