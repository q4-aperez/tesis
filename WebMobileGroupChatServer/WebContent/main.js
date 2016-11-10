// to keep the session id
var sessionId = '';

// name of the client
var name = '';

// socket connection url and port
var socket_url = '192.168.1.24';
var port = '8080';

var jobsList = [];
var devicesInfo = {};
var lastSelected = 0;
var devicesArray = [];
var totalJobsSent = 0;
var totalResultsReceived = 0;
var jobsArray = [ "fibonacci", "factorial" ];

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
		totalJobsSent++;
	}
	// console.log("Total jobs sent so far: " + totalJobsSent);
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
			var index = devicesArray.indexOf(new_name);
			if (index < 0) {
				devicesArray.push(new_name);
			}
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
			var trimmedInfo = info[0].trim();
			if (trimmedInfo == "BogoMIPS" || trimmedInfo == "processor count") {
				var mips = 0.0;
				var processors = 1;
				for (var i = 0, j = info.length; i < j; i++) {
					if (info[i].trim().split("\n")[0] == "BogoMIPS"
							|| info[i].trim().split("\n")[1] == "BogoMIPS") {
						var currentMips = parseFloat(info[i + 1].trim().split(
								"\n")[0]);
						mips += currentMips;
					}
				}
				devicesInfo[jObj.name]["benchmark"] = mips;
			} else if (info[0] == "battery") {
				var batteryData = devicesInfo[jObj.name]["battery"];
				if (batteryData) {
					batteryData.oldCharge = batteryData.newCharge;
					batteryData.oldTime = batteryData.newTime;
					batteryData.newCharge = info[1];
					batteryData.newTime = new Date().getTime();
					if (batteryData.oldCharge - batteryData.newCharge != 0) {
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
					}
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
				// update jobs counter on the device
				var devicePendingJobs = jObj.message.split("/")[1].split(":")[1]
						.trim();
				devicePendingJobs = parseInt(devicePendingJobs);
				if (devicesInfo[jObj.name]) {
					devicesInfo[jObj.name].jobs = devicePendingJobs;
					// console.log(jObj.name + " jobs pending: "
					// + devicePendingJobs);
				}
				totalResultsReceived++;
				$('p.total_results').html(
						'Resultados recibidos: ' + totalResultsReceived)
						.fadeIn();
				// console.log("Results Received: " + totalResultsReceived);
			}
		}
	} else if (jObj.flag == 'exit') {
		// if the json flag is 'exit', it means somebody left the grid
		var li = '<li class="exit" tabindex="1"><span class="name red">'
				+ jObj.name + '</span> ' + jObj.message + '</li>';

		// number of people online
		var online_count = jObj.onlineCount - 1;

		$('p.online_count').html(
				'Hello, <span class="green">' + name + '</span>. <b>'
						+ online_count + '</b> devices online right now')
				.fadeIn();

		var index = devicesArray.indexOf(jObj.name);
		if (index > -1) {
			devicesArray.splice(index, 1);
			console.log(jObj.name + " deleted from array, remaining: "
					+ devicesArray.length + " - Online: " + online_count);
		} else {
			console.log(jObj.name + " not found in devices array.");
		}
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

function randomScheduler() {
	lastSelected = Math.floor(Math.random() * devicesArray.length);
	var deviceName = devicesArray[lastSelected];
	return deviceName;
}

function roundRobinScheduler() {	
	lastSelected = (lastSelected + 1) % devicesArray.length;
	var deviceName = devicesArray[lastSelected];
	return deviceName;
}

function seasScheduler() {
	var tempBest = 0, tempName;
	for (var i = 0, j = devicesArray.length; i < j; i++) {
		var deviceName = devicesArray[i];
		var deviceInfo = devicesInfo[deviceName];
		var score = 1;
		if(deviceInfo && deviceInfo.battery && deviceInfo.battery.estimatedUptime && deviceInfo.benchmark){
			score = deviceInfo.battery.estimatedUptime * deviceInfo.benchmark
			/ (deviceInfo.jobs + 1);
		}		
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
	return total * 1.0 / count; // *1.0 is a float conversion
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
	if (devicesInfo[selectedDevice]) {
		devicesInfo[selectedDevice].jobs = devicesInfo[selectedDevice].jobs + 1;
	}
}

function generateJobs() {
	// Create jobs if there are devices connected and not queued jobs
	if (devicesArray.length > 0 && jobsList.length == 0) {
		// console.log("Devices array size: " + devicesArray.length);
		createRandomJobs();
	}
}

function createRandomJobs() {
	setTimeout(function() {
		var jobsToCreate = getRandomIntInclusive(1, 10);

		console.log("Creating " + jobsToCreate + " new jobs");
		for (var i = 0, j = jobsToCreate; i < j; i++) {
			queueNewJob(
					jobsArray[getRandomIntInclusive(0, jobsArray.length - 1)],
					getRandomIntInclusive(1, 60));
		}
		sendJobs();
		generateJobs();
	}, getRandomIntInclusive(1000, 5000));
}

function getRandomIntInclusive(min, max) {
	min = Math.ceil(min);
	max = Math.floor(max);
	return Math.floor(Math.random() * (max - min + 1)) + min;
}