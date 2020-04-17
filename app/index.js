'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/notifications/{userId}/{notificationsId}').onWrite(event => {
	const userId = event.params.userId;
	const notificationId = event.params.notificationId;
	
	console.log('The User Id: ', userId);
	
} );

