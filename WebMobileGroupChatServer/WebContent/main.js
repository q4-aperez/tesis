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
	webSocket = new WebSocket("ws://" + socket_url + ":" + port
			+ "/WebMobileGroupChatServer/chat?name=" + name);

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
	// var value = $('#job_value').val();
	// var job = $("#job_name option:selected").val();
	//
	// if (value.trim().length > 0 && isNumeric(value)) {
	// queueNewJob(job, value);
	// } else {
	// alert('Please enter a value for the job!');
	// }
	generateJobs();
}

function queueNewJob(job, value) {
	var li = '<li tabindex="1"><span class="name">' + job + '</span> (' + value
			+ ')</li>';
	$('#jobs').append(li);
	$('#jobs li').last().focus();
	jobsList.push({
		job : job,
		value : value
	});
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
		// if the flag is 'new', a client joined grid
		var new_name = 'You';

		// number of people online
		var online_count = jObj.onlineCount - 1;

		$('p.online_count').html(
				'Hello, <span class="green">' + name + '</span>. <b>'
						+ online_count + '</b> devices online right now')
				.fadeIn();

		if (jObj.sessionId != sessionId) {
			new_name = jObj.name;
			devicesInfo[new_name] = {
				arrayIndex : devicesArray.length
			};
			devicesArray.push(new_name);
		}

		var li = '<li class="new" tabindex="1"><span class="name">' + new_name
				+ '</span> ' + jObj.message + '</li>';
		$('#messages').append(li);

		$('#input_message').val('');

	} else if (jObj.flag == 'message') {
		if (jObj.sessionId == sessionId) {
			var li = '<li tabindex="1">' + jObj.message + '</li>';
			// appending the job to sent list
			appendSentJob(li);
		} else {
			var li = '<li tabindex="1"><span class="name">' + jObj.name
					+ '</span> ' + jObj.message + '</li>';
			// appending the chat message to list
			appendChatMessage(li);

			var info = jObj.message.split(":");

			if (devicesInfo[jObj.name] == null) {
				devicesInfo[jObj.name] = {
					benchmark : null,
					battery : null,
					jobs : 0
				};
			}
			if (info[0] == "processor count") {
				devicesInfo[jObj.name]["benchmark"] = info[1];
			} else if (info[0] == "battery") {
				var batteryData = devicesInfo[jObj.name]["battery"];
				if (batteryData) {
					batteryData.oldCharge = batteryData.newCharge;
					batteryData.oldTime = batteryData.newTime;
					batteryData.newCharge = info[1];
					batteryData.newTime = new Date().getTime();
					var dischargeRate = (batteryData.newTime - batteryData.oldTime)
							/ (batteryData.oldCharge - batteryData.newCharge);
					var estimatedUptime = batteryData.newTime
							- batteryData.startTime + batteryData.newCharge
							* dischargeRate;
					batteryData.previousEstimations.push(estimatedUptime);
					var newEstimatedUptime = getAverage(batteryData.previousEstimations)
							- (batteryData.newTime - batteryData.startTime);
					// Save new estimated uptime
					batteryData.estimatedUptime = newEstimatedUptime;
				} else {
					devicesInfo[jObj.name].battery = {
						oldCharge : null,
						newCharge : info[1],
						oldTime : null,
						newTime : new Date().getTime(),
						startTime : new Date().getTime(),
						previousEstimations : [],
						estimatedUptime : null
					}
				}
			} else { // it's a result
				// decrement the jobs counter on the device
				devicesInfo[jObj.name].jobs = devicesInfo[jObj.name].jobs - 1;
			}
		}
	} else if (jObj.flag == 'exit') {
		// if the json flag is 'exit', it means somebody left the grid
		var li = '<li class="exit" tabindex="1"><span class="name red">'
				+ jObj.name + '</span> ' + jObj.message + '</li>';

		var online_count = jObj.onlineCount;

		$('p.online_count').html(
				'Hello, <span class="green">' + name + '</span>. <b>'
						+ online_count + '</b> devices online right now');
		var index = devicesArray.indexOf(jObj.name);
		if (index > -1) {
			devicesArray.splice(index, 1);
		}
		appendChatMessage(li);
	}
}

var devicesInfo = {};

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

var lastSelected = 0;
var devicesArray = [];

function randomScheduler() {
	lastSelected = Math.floor(Math.random() * devicesArray.length);
	var deviceName = devicesArray[lastSelected];
	return deviceName;
}

function roundRobinScheduler() {
	var deviceName = devicesArray[lastSelected];
	lastSelected = (lastSelected + 1) % devicesArray.length;
	return deviceName;
}

function seasScheduler() {
	var tempBest = 0, tempName;
	for (var i = 0, j = devicesArray.length; i < j; i++) {
		var deviceName = devicesArray[i];
		var deviceInfo = devicesInfo[deviceName];
		var score = deviceInfo.battery.estimatedUptime * deviceInfo.benchmark
				/ (deviceInfo.jobs + 1);
		if (score > tempBest) {
			tempBest = score;
			tempName = deviceName;
		}
	}
	return tempName;
}

function getAverage(estimationsArray) {
	var total = 0;
	var count = estimationsArray.length > 0 ? estimationsArray.length : 1;
	for (var i = 0, j = estimationsArray.length; i < j; i++) {
		total += estimationsArray[i];
	}
	return total * 1.0 / count; //*1.0 is a float conversion
}

/**
 * Sending message to socket server message will be in json format
 */
function sendMessageToServer(flag, message) {
	var json = '{""}';
	var scheduler = $("#scheduler_option option:selected").val();
	var selectedDevice;
	if (scheduler == "random") {
		selectedDevice = randomScheduler();
	} else if (scheduler == "roundrobin") {
		selectedDevice = roundRobinScheduler();
	} else if (scheduler == "seas") {
		selectedDevice = seasScheduler();
	}

	// preparing json object
	var myObject = {};
	myObject.sessionId = sessionId;
	myObject.message = selectedDevice + "@" + message;
	myObject.flag = flag;

	// converting json object to json string
	json = JSON.stringify(myObject);

	// sending message to server
	webSocket.send(json);
	// increment the jobs counter on the device
	devicesInfo[selectedDevice].jobs = devicesInfo[selectedDevice].jobs + 1;
}

var totalJobsCreated = 0;
var jobsArray = ["fibonacci","factorial"];

function generateJobs() {
	if (totalJobsCreated < 10) {
		createRandomJobs();
	}
}

function createRandomJobs(){
	setTimeout(function() {
		var jobsToCreate = getRandomIntInclusive(1, 10);		
		totalJobsCreated += jobsToCreate;
		console.log("JobsToCreate: " + jobsToCreate);
		console.log("totalJobsCreated: " + totalJobsCreated);
		for (var i = 0, j = jobsToCreate; i < j; i++) {
			queueNewJob(jobsArray[getRandomIntInclusive(0, jobsArray.length-1)], getRandomIntInclusive(1, 60));
		}
		sendJobs();
		generateJobs();
	}, getRandomIntInclusive(500, 3000));
}

function getRandomIntInclusive(min, max) {
	min = Math.ceil(min);
	max = Math.floor(max);
	return Math.floor(Math.random() * (max - min + 1)) + min;
}