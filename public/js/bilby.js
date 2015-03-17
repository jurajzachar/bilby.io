/* utils used at bilby.io to aid bootstrap */

/** for edit.piece.scala.html -> count remaining characters inside input source and update child tooltip * */
function countChars(source, text_max) {
	$(source).keyup(
			function() {
				var text_length = $(source).val().length;
				var text_remaining = text_max - text_length;
				if (text_remaining < 0)
					text_remaining = 0;
				$(source).next().children('.tooltip-inner').html(
						"<span>" + text_remaining + " remaining.</span>");
			});
}

/** for edit.piece.scala.html -> activates a retractor button **/
function activateRetractor(controller, data_source, target) {
	$(controller).click(function() {
		$('.collapsible').slideToggle('slow');
		if ($(controller).hasClass("glyphicon-menu-up")) {
			$(controller).attr("class", "btn glyphicon glyphicon-menu-down");
			$(target).show();
			var title = $(data_source).val();
			if (title === "") {
				title = "Untitled";
			}
			$(target).html("<strong><h4>" + title + "</h4></strong>");
		} else {
			$(controller).attr("class", "btn glyphicon glyphicon-menu-up");
			$(target).hide();
		}
	});
}

function fadeOutAlerts(timeout) {
	window.setTimeout(function() {
		$(".alert").fadeTo(2000, 500).slideUp(500, function() {
			$(".alert").alert('close');
		});
	}, timeout);
}

$(".modal").each(function(modal){
	$(this).click(function(){
	    $(this).modal();
	  });
  });