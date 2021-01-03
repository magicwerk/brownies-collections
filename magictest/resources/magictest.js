$(document).ready(function() {
	// Initialize choices
	var choiceTitle = "";
	$(".choice-head").each(function() {
		var head = $(this);
		var selected = $(this).attr('data-selected');
		var choice = "";
		var choices = new Array();
		$(head).children(".choice-body").each(function() {
			// Get array with all classes of an object
			//var classList = $(this).attr('class').split(/\s+/);
			if ($(this).hasClass('choice-body')) {
				var name = $(this).attr('data-name');
				choices.push(name);
				if (choice == "" || name == selected) {
					choice = name;
				}
			}
		});

		// Add chooser
		// (Do not use href='#' in the a link or the scroll position will be reset)
		var div = $("<div class='choice-chooser'>" + choiceTitle + "</div>");
		for (var i = 0; i < choices.length; i++) {
			var link = $("<a class='choice' data-name='"+choices[i]+"'>"+choices[i]+"</a>");
			div.append(link);
			$(link).click(function() {
				var name = $(this).attr('data-name');
				showChoice(head, name);
			});
		}
		$(this).prepend(div);

		// Update HTML
		showChoice(head, choice);
	});

	function showChoice(head, choice) {
		//console.debug("showChoice: " + head + ", " + choice);
		$(head).children(".choice-chooser").find("a").each(function() {
			var name = $(this).attr('data-name');
			$(this).removeClass("choice-selected");
			if (name == choice) {
				$(this).addClass("choice-selected");
			}
		});
		$(head).children(".choice-body").each(function() {
			var name = $(this).attr('data-name');
			if (name == choice) {
				$(this).show();
			} else {
				$(this).hide();
			}
		});
	};

});
