/* utils used at bilby.io to aid bootstrap */

/** set background for piece-header * */

function setBG(element, url) {
	var img = new Image();
	$(img).error(function() {
		// URL not exists
		$(element).addClass("unresolved");
		// $(element).css("background-color", getRandomColor());
	});
	img.onload = function() {
		var resolves = img.height !== 0;
		if (resolves) {
			// URL OK
			$(element).css("background-image", "url(" + url + ")");
		}
	};
	img.src = url;
}

/**
 * recompute page-header padding with respect to the navbar... TODO: find pure
 * CSS solution *
 */
function remarginPageHeader() {
	var nav_height = $('.navbar').height() + 10;
	$(".page-header").css({
		marginTop : nav_height
	});
}

/**
 * for edit.piece.scala.html -> count remaining characters inside input source
 * and update child tooltip *
 */
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

/** for edit.piece.scala.html -> activates a retractor button * */
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

$(".modal").each(function(modal) {
	$(this).click(function() {
		$(this).modal();
	});
});

/** PAGE CONTROL * */

// EDIT page
function readyEditor() {
	var controller = "#retractor";
	var data_source = "#title";
	var target = "#mini-header";
	// take care of the retracting stuff
	$(target).hide();
	activateRetractor(controller, data_source, target);
	// fire up tooltips
	$('#title').tooltip({
		'trigger' : 'focus',
		'title' : 'A good title should be short, concise and captivating.',
		'placement' : 'bottom'
	}).on('shown.bs.tooltip', countChars("#title", 75));

	$('#titleCoverUrl')
			.tooltip(
					{
						'trigger' : 'focus',
						'title' : 'Provide a link to an image which should be used instead of the default title cover.',
						'placement' : 'bottom'
					});

	// enhance tagsinput
	$("#tags").attr("data-role", "tagsinput");
	$("#tags").tagsinput({
		maxTags : 10,
		maxChars : 15,
		trimValue : true,
		tagClass : "label label-default hashtag heavy"
	});
	// load tags, not sure why this is not done automatically...
	var tags = $("#tags").val();
	if (tags.length > 0) {
		var source = jQuery.parseJSON($("#tags").val());
		for (var i = 0; i < source.length; i++) {
			$("#tags").tagsinput('add', source[i]);
		}
	}
	$('#tags_field>.bootstrap-tagsinput')
			.tooltip(
					{
						'trigger' : 'focus',
						'title' : 'A comma delimited list of up to 10 tags, each of which is at most 15 characters long. Tags aid in categorizing your post.',
						'placement' : 'top'
					});

	$('#shortSummary')
			.tooltip(
					{
						'trigger' : 'focus',
						'title' : 'Describe your contribution in a short paragraph by summarazing and hightling key points.',
						'placement' : 'bottom'
					}).on('shown.bs.tooltip', countChars("#shortSummary", 300));

	fadeOutAlerts(5000); // 5 sec
}

function getRandomColor() {
	var letters = '0123456789ABCDEF'.split('');
	var color = '#';
	for (var i = 0; i < 6; i++) {
		color += letters[Math.floor(Math.random() * 16)];
	}
	return color;
}

var substringMatcher = function(strs) {
	return function findMatches(q, cb) {
		var matches, substrRegex;

		// an array that will be populated with substring matches
		matches = [];

		// regex used to determine if a string contains the substring `q`
		substrRegex = new RegExp(q, 'i');

		// iterate through the pool of strings and for any string that
		// contains the substring `q`, add it to the `matches` array
		$.each(strs, function(i, str) {
			if (substrRegex.test(str)) {
				// the typeahead jQuery plugin expects suggestions to a
				// JavaScript object, refer to typeahead docs for more info
				matches.push({
					value : str
				});
			}
		});

		cb(matches);
	};
};

/** a read-only class used to pass as an array parameter to the function below **/
function Dataset(name, prefetch, url) {
	this.name = name;
	this.prefetch = prefetch;
	this.url = url;
}

/** 
 * takes parameters: 
 * 
 * element - selector on which typeahead is enabled
 * datasets - array of Dataset
 *
 **/

function initTypeAhead(element, datasets) {

	$(element).keypress(function(e) {
		// '32' is the keyCode for 'space'
		if (e.keyCode == '32' || e.charCode == '32') {
			var token = $(element).val().replace(/,|\./, "").trim();
			$(element).val(token);
		}
	});

	var adaptors = [];
	function compare(remoteMatch, localMatch) {
		return remoteMatch.value === localMatch.value;
	}
	for(var i = 0; i < datasets.length; i++){
		// init bloodhoud obj
		var bloodhound = new Bloodhound({
			datumTokenizer : Bloodhound.tokenizers.obj.whitespace('value'),
			queryTokenizer : Bloodhound.tokenizers.whitespace,
			limit : 25,
			dupDetector: compare(remoteMatch, localMatch),
			prefetch: datasets[i].prefetch,
			remote : {
				url : datasets[i].url,
				wildcard: "%25QUERY"
			}
		});
		bloodhound.initialize();

		// init typeahead obj
		var adaptor = {
			name : datasets[i].name,
			displayKey : 'value',
			source : bloodhound.ttAdapter(),
			templates : {
				header : '<div class="tt-suggestion tt-suggestion-header">' + datasets[i].name + '</div>'
			}
		};
		adaptors.push(adaptor);
	}
	

	$(element).typeahead({
		hint : true,
		highlight : true,
		minLength : 1
	}, adaptors);
}
