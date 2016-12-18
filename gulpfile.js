/*
 Copyright (c) 2015 The Polymer Project Authors. All rights reserved.
 This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
 The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
 The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
 Code distributed by Google as part of the polymer project is also
 subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
 */

'use strict';

// Include Gulp & tools we'll use
const gulp = require('gulp');
const $ = require('gulp-load-plugins')();
let del = require('del');
let runSequence = require('run-sequence');
const browserSync = require('browser-sync');
const reload = browserSync.reload;
let merge = require('merge-stream');
const path = require('path');
let fs = require('fs');
let glob = require('glob');
const historyApiFallback = require('connect-history-api-fallback');
let packageJson = require('./package.json');
let crypto = require('crypto');
let polybuild = require('polybuild');
let dom = require('gulp-dom');
const babel = require('gulp-babel');
const vulcanize = require('gulp-vulcanize');
const rename = require("gulp-rename");
const eslint = require('gulp-eslint');

const AUTOPREFIXER_BROWSERS = [
  'ie >= 10',
  'ie_mob >= 10',
  'ff >= 30',
  'chrome >= 34',
  'safari >= 7',
  'opera >= 23',
  'ios >= 7',
  'android >= 4.4',
  'bb >= 10'
];

const styleTask = function (stylesPath, srcs) {
  return gulp.src(srcs.map(function (src) {
    return path.join('app', stylesPath, src);
  }))
    .pipe($.changed(stylesPath, {extension: '.css'}))
    .pipe($.autoprefixer(AUTOPREFIXER_BROWSERS))
    .pipe(gulp.dest('.tmp/' + stylesPath))
    .pipe($.cssmin())
    .pipe(gulp.dest('dist/' + stylesPath))
    .pipe($.size({title: stylesPath}));
};

// Compile and automatically prefix stylesheets
gulp.task('styles', function() {
  return styleTask('styles', ['**/*.css']);
});

gulp.task('elements', function() {
  return styleTask('elements', ['**/*.css']);
});

gulp.task('scripts:es6', function() {
  return gulp.src('app/**/*.es6.js')
    .pipe(babel())
    .pipe(gulp.dest('.tmp'))
    .pipe($.uglify({preserveComments: 'some'}))
    .pipe(gulp.dest('dist'));
});
gulp.task('vulcanize', function () {
  return gulp.src('app/elements/elements.html')
    .pipe(vulcanize({
      abspath: '',
      excludes: [],
      inlineScripts: false,
      inlineCss: false,
      stripExcludes: false
    }))
    .pipe(rename("elements/elements.vulcanized.html"))
    .pipe(gulp.dest('app'));
});

// Optimize images
gulp.task('images', function() {
  return gulp.src('app/images/**/*')
    .pipe($.cache($.imagemin({
      progressive: true,
      interlaced: true
    })))
    .pipe(gulp.dest('dist/images'))
    .pipe($.size({title: 'images'}));
});

// Copy web fonts to dist
gulp.task('fonts', function() {
  return gulp.src(['app/fonts/**'])
    .pipe(gulp.dest('dist/fonts'))
    .pipe($.size({title: 'fonts'}));
});

// Watch files for changes & reload
gulp.task('serve', ['styles', 'elements', 'scripts:es6', 'images', 'vulcanize'], function() {
  browserSync({
    port: 5000,
    notify: false,
    logPrefix: 'PSK',
    snippetOptions: {
      rule: {
        match: '<span id="browser-sync-binding"></span>',
        fn: function(snippet) {
          return snippet;
        }
      }
    },
    // https: true,
    server: {
      baseDir: ['.tmp', 'app'],
      middleware: [historyApiFallback()],
      routes: {
        '/bower_components': 'bower_components'
      }
    }
  });

  gulp.watch(['app/**/*.html'], reload);
  gulp.watch(['app/styles/**/*.css'], ['styles', reload]);
  gulp.watch(['app/elements/**/*.css'], ['elements', reload]);
  gulp.watch(['app/**/*.es6.js'], ['scripts:es6', reload]);
  gulp.watch(['app/{scripts,elements}/**/{*.js,*.html}'], ['vulcanize']);
  gulp.watch(['app/images/**/*'], reload);
});

// Load tasks for web-component-tester
// Adds tasks for `gulp test:local` and `gulp test:remote`
require('web-component-tester').gulp.init(gulp);

// Load custom tasks from the `tasks` directory
try { require('require-dir')('tasks'); } catch (err) {}
