import { makeExecutableSchema } from 'graphql-tools';

import resolvers from "./resolvers"

const typeDefs = `
    type Query {
        darksky(place: String!): Weather
    }

    type Weather {
        summary: String
        temperature: Float
        coords: [Float]
    } 
`

export default makeExecutableSchema({
  typeDefs,
  resolvers
})