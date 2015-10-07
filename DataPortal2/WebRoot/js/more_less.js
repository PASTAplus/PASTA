/* jQuery logic for "Show more" and "Show less" text control */

var showChar = 220;  // How many characters are shown by default
var ellipsestext = "...";
var moretext = "Show more >";
var lesstext = "< Show less";
    
$('.more').each(function() {
	var content = $(this).html();
 
	if (content.length > showChar) {
		var c = content.substr(0, showChar);
		var h = content.substr(showChar, content.length - showChar);
		var html = c + '<span class="moreellipses">' + ellipsestext + '&nbsp;</span><span class="morecontent"><span>' + 
				   h + '</span>&nbsp;&nbsp;<a href="" class="morelink searchsubcat">' + moretext + '</a></span>';
				   $(this).html(html);
	}
});
 
$(".morelink").click(function() {
	if ($(this).hasClass("less")) {
		$(this).removeClass("less");
		$(this).html(moretext);
	} 
	else {
		$(this).addClass("less");
		$(this).html(lesstext);
	}
	$(this).parent().prev().toggle();
	$(this).prev().toggle();
	return false;
});
