var webpack = require('webpack');
const BabiliPlugin = require("babili-webpack-plugin");
module.exports = {
    entry: "./scripts/app.js",
    output: {
        path: __dirname,
        filename: "bundle.js"
    },

    module: {
        loaders: [

            {
                test: /\.css$/,
                loader: "style-loader!css-loader"
            },
            {
                test: /\.html$/, // handles html files. <link rel="import" href="path.html"> and import 'path.html';
                loader: 'wc-loader'
                // if you are using es6 inside html use
                // loader: 'babel-loader!wc-loader'
                // similarly you can use coffee, typescript etc. pipe wc result through the respective loader.
            },
            {
                test: /\.js$/, // handles js files. <script src="path.js"></script> and import 'path';
                loader: 'babel-loader',
                exclude: /node_modules/
            },
            {
                test: /\.(png|jpg|gif|svg)$/, // handles assets. eg <img src="path.png">
                loader: 'file-loader',
                query: {
                    name: '[name].[ext]?[hash]'
                }
            },
            {test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader?limit=10000&mimetype=application/font-woff'},
            {test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader?limit=10000&mimetype=application/octet-stream'},
            {test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: 'file-loader'},
            {test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader?limit=10000&mimetype=image/svg+xml'}
        ]
    },
    plugins: [
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery'
        }),
        new BabiliPlugin()
    ]
};
