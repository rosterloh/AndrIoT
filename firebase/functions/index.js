'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin SDK.
admin.initializeApp(functions.config().firebase);

// https://firebase.google.com/docs/functions
exports.andriotApp = functions.https.onRequest((request, response) => {
    console.log('Request headers: ' + JSON.stringify(req.headers));
    console.log('Request body: ' + JSON.stringify(req.body));
});
