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
let del = require('del');
const browserSync = require('browser-sync');
const reload = browserSync.reload;
const path = require('path');
let fs = require('fs');
const historyApiFallback = require('connect-history-api-fallback');
const vulcanize = require('gulp-vulcanize');
const rename = require("gulp-rename");

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


// Watch files for changes & reload
gulp.task('serve', ['vulcanize'], function() {
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
  gulp.watch(['app/{scripts,elements}/**/{*.js,*.html}','!app/elements/elements.vulcanized.html'], ['vulcanize']);
  gulp.watch(['app/images/**/*'], reload);
});

// Load tasks for web-component-tester
// Adds tasks for `gulp test:local` and `gulp test:remote`
require('web-component-tester').gulp.init(gulp);

// Load custom tasks from the `tasks` directory
try { require('require-dir')('tasks'); } catch (err) {}
