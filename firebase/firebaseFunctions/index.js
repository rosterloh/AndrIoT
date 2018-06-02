'use strict';

import { https } from 'firebase-functions';
import setupGraphQLServer from "./graphql/server"
const admin = require('firebase-admin');
// Initialize Firebase Admin SDK.
admin.initializeApp();

const keys = {};
admin.database().ref('keys').on('value', results => {
    console.log('Keys updated: ' + JSON.stringify(results));
    keys = results
})

const graphQLServer = setupGraphQLServer(keys)

export const api = https.onRequest(graphQLServer)