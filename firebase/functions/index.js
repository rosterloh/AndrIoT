'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
// Initialize Firebase Admin SDK.
admin.initializeApp();
const { ApolloServer, gql } = require('apollo-server');

const keys = {};
admin.database().ref('keys').on('value', results => {
    console.log('Keys updated: ' + JSON.stringify(results));
    keys = results
})

const books = [
    {
        title: 'Harry Potter and the Chamber of Secrets',
        author: 'J.K. Rowling',
    },
    {
        title: 'Jurassic Park',
        author: 'Michael Crichton',
    },
];

const typeDefs = gql`
    # This "Book" type can be used in other type declarations.
    type Book {
        title: String
        author: String
    }

    # The "Query" type is the root of all GraphQL queries.
    # (A "Mutation" type will be covered later on.)
    type Query {
        books: [Book]
    }
`;  

const resolvers = {
    Query: {
        books: () => books,
    },
};

const server = new ApolloServer({ typeDefs, resolvers });

server.listen().then(({ url }) => {
    console.log(`🚀  Server ready at ${url}`);
});

// https://firebase.google.com/docs/functions
exports.andriotApp = functions.https.onRequest((request, response) => {
    console.log('Request headers: ' + JSON.stringify(req.headers));
    console.log('Request body: ' + JSON.stringify(req.body));
});
