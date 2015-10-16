var gulp = require('gulp');
var minifyCss = require('gulp-minify-css');
var uglify = require('gulp-uglify');

var paths = {
    "css": "src/css/*",
    "js": "src/js/*",
    "dist": "target/dist/"
};

gulp.task('minify-css', function() {
    return gulp.src(paths.css)
        .pipe(minifyCss())
        .pipe(gulp.dest(paths.dist + 'css/'));
});

gulp.task('minify-js', function() {
    return gulp.src(paths.js)
        .pipe(uglify())
        .pipe(gulp.dest(paths.dist + 'js/'));
});

gulp.task('default', ['minify-css', 'minify-js'], function() {});