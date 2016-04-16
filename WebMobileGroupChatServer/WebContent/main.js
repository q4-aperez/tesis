// to keep the session id
var sessionId = '';

// name of the client
var name = '';

// socket connection url and port
var socket_url = '192.168.0.4';
var port = '8080';

$(document).ready(function() {

	// $("#form_submit, #form_send_message").submit(function(e) {
	// e.preventDefault();
	join();
	// });
});

var webSocket;

/**
 * Connecting to socket
 */
function join() {
	name = "admin";
	openSocket();
}

/**
 * Will open the socket connection
 */
function openSocket() {
	// Ensures only one connection is open at a time
	if (webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED) {
		return;
	}

	// Create a new instance of the websocket
	webSocket = new WebSocket("ws://" + socket_url + ":" + port + "/WebMobileGroupChatServer/chat?name=" + name);

	/**
	 * Binds functions to the listeners for the websocket.
	 */
	webSocket.onopen = function(event) {
		// $('#message_container').fadeIn();

		if (event.data === undefined)
			return;
	};

	webSocket.onmessage = function(event) {

		// parsing the json data
		parseMessage(event.data);
	};

	webSocket.onclose = function(event) {

	};
}

/**
 * Sending the chat message to server
 */
function send() {
	var message = $('#input_message').val();

	if (message.trim().length > 0) {
		sendMessageToServer('message', message);
	} else {
		alert('Please enter message to send!');
	}

}

var jobsList = [];
function addJob() {
	var value = $('#job_value').val();
	var job = $("#job_name option:selected").val();

	if (value.trim().length > 0 && isNumeric(value)) {
		var li = '<li tabindex="1"><span class="name">' + job + '</span> (' + value + ')</li>';
		$('#jobs').append(li);
		$('#jobs li').last().focus();
		jobsList.push({
			job : job,
			value : value
		});
	} else {
		alert('Please enter a value for the job!');
	}

}

function sendJobs() {
	for (var i = 0; i < jobsList.length; i++) {
		var job = jobsList[i];
		sendMessageToServer('message', job.job + ";" + Math.floor(job.value));
	}
	$('#jobs').html('');
	// alert('Jobs sent!');
	jobsList = [];
}

function isNumeric(value) {
	return !isNaN(Math.floor(value));
}

/**
 * Closing the socket connection
 */
function closeSocket() {
	webSocket.close();

	$('#message_container').fadeOut(600, function() {
		$('#prompt_name_container').fadeIn();
		// clearing the name and session id
		sessionId = '';
		name = '';

		// clear the ul li messages
		$('#messages').html('');
		$('p.online_count').hide();
	});
}

/**
 * Parsing the json message. The type of message is identified by 'flag' node
 * value flag can be self, new, message, exit
 */
function parseMessage(message) {
	var jObj = $.parseJSON(message);

	// if the flag is 'self' message contains the session id
	if (jObj.flag == 'self') {

		sessionId = jObj.sessionId;

	} else if (jObj.flag == 'new') {
		// if the flag is 'new', a client joined the chat room
		var new_name = 'You';

		// number of people online
		var online_count = jObj.onlineCount - 1;

		$('p.online_count').html('Hello, <span class="green">' + name + '</span>. <b>' + online_count + '</b> devices online right now').fadeIn();

		if (jObj.sessionId != sessionId) {
			new_name = jObj.name;
		}

		var li = '<li class="new"><span class="name">' + new_name + '</span> ' + jObj.message + '</li>';
		$('#messages').append(li);

		$('#input_message').val('');

	} else if (jObj.flag == 'message') {
		// if the json flag is 'message', it means somebody sent the chat
		// message

		if (jObj.sessionId == sessionId) {
			var li = '<li tabindex="1">' + jObj.message + '</li>';
			// appending the job to sent list
			appendSentJob(li);
		} else {
			var li = '<li tabindex="1"><span class="name">' + jObj.name + '</span> ' + jObj.message + '</li>';
			// appending the chat message to list
			appendChatMessage(li);
		}
	} else if (jObj.flag == 'exit') {
		// if the json flag is 'exit', it means somebody left the chat room
		var li = '<li class="exit"><span class="name red">' + jObj.name + '</span> ' + jObj.message + '</li>';

		var online_count = jObj.onlineCount;

		$('p.online_count').html('Hello, <span class="green">' + name + '</span>. <b>' + online_count + '</b> people online right now');

		appendChatMessage(li);
	}
}

/**
 * Appending the job message to jobs list
 */
function appendChatMessage(li) {
	$('#messages').append(li);

	// scrolling the list to bottom so that new message will be visible
	$('#messages li').last().focus();
}

/**
 * Appending the chat message to list
 */
function appendSentJob(li) {
	$('#sent_jobs').append(li);

	// scrolling the list to bottom so that new message will be visible
	$('#sent_jobs li').last().focus();
}

/**
 * Sending message to socket server message will be in json format
 */
function sendMessageToServer(flag, message) {
	var json = '{""}';

	// preparing json object
	var myObject = new Object();
	myObject.sessionId = sessionId;
	myObject.message = message;
	myObject.flag = flag;

	// converting json object to json string
	json = JSON.stringify(myObject);

	// sending message to server
	webSocket.send(json);
}