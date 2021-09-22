const path = require('path');
const WriteFilePlugin = require("./webpack/WriteFilePlugin");

module.exports = {
    lintOnSave: false,
    publicPath: '',
    configureWebpack: {
        resolve: {
            alias: {
                "@build": path.resolve(__dirname, 'build/')
            }
        },
        plugins: [
            new WriteFilePlugin({
                path: 'build/assets/',
                fileName: 'data.json',
                content: `[]`
            }),
            new WriteFilePlugin({
                path: 'build/assets/',
                fileName: 'application.json',
                content: `{
  "version": "unknown",
  "branch": "unknown",
  "commit": "unknown"
}`})
        ]
    },
};

