import request from 'request';

const baseUrl = 'https://api.darksky.net/forecast/';
const urlParams = '?units=us&exclude=minutely,hourly,daily,flags';
const coords = ['51.5285582', '-0.2416781'];

function getWeather(apiKey, place) {
    return new Promise((resolve, reject) => {
        request(`${baseUrl}${apiKey}/${coords[0]},${coords[1]}${urlParams}`, (error, response, body) => {
            if (error) {
              reject(error);
            }
            const data = JSON.parse(body);
            const summary = data.currently.summary;
            const temperature = data.currently.temperature;
            resolve({
              summary,
              temperature,
              coords
            });
        });
    });
}

const resolvers = {
    Query: {
        darksky(root, args, context, info) {
            return getWeather(context.secrets.darksky, args.place);
        }
    }
}

export default resolvers