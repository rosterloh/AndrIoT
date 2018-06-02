const { ApolloServer } = require('apollo-server');
const { registerServer } = require('apollo-server-express');
const { typeDefs, resolvers } = require('./schema');

const setupGraphQLServer = (keys) => {
    
    const server = new ApolloServer({ 
        typeDefs, 
        resolvers,
        context: ({ req }) => ({
            secrets: keys
        })
     });

    server.listen().then(({ url }) => {
        console.log(`🚀  Server ready at ${url}`);
    });

    return server
}

export default setupGraphQLServer