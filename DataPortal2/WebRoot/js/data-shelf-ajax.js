$(document).ready(function() {
	$(".form-no-margin").submit(function(event) {
		event.preventDefault();

		var formInput = $(this).serialize();
		$.post("savedDataServlet", formInput);

		var operationInput = $(this).find("input:nth(0)");
		var packageIdInput = $(this).find("input:nth(1)");
		var forwardInput = $(this).find("input:nth(2)");
		var submitInput = $(this).find("input:nth(3)");
		
		var divElement = $(this).find("div:nth(0)");
		var operationValue = operationInput.attr("value");
		var packageId = packageIdInput.attr("value");
		var forwardValue = forwardInput.attr("value");
		
		if (operationValue == "save") {
			operationInput.attr("value","unsave");
			submitInput.attr("src", "images/minus_blue_small.png");
			submitInput.attr("alt", "Remove from your data shelf");
			submitInput.attr("title", "Remove from your data shelf");
			divElement.html("<small><em>On shelf</em></small><br/>");
		}
		else {
			// if this is the saved data page, hide the table row for the unsaved data package
			if (forwardValue == "savedData") {
				$(this).parent().parent().hide();
				var numFound = $("#dataShelfNumFound").html();
				numFound = numFound - 1;
				$("#dataShelfNumFound").html(numFound);
			}
			// otherwise change the form button from a minus to a plus
			else {
				operationInput.attr("value", "save");
				submitInput.attr("src", "images/plus_blue_small.png");
				submitInput.attr("alt", "Add to your data shelf");
				submitInput.attr("title", "Add to your data shelf");
				divElement.html("");
			}
		}

	});
});
