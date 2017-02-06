var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var index = require('./routes/index');
var users = require('./routes/users');
var mongo = require('mongoskin');

var app = express();

// Database
const MongoClient = require('mongodb').MongoClient;

//var activemq;

var activemq = mongo.db('mongodb://localhost:27017/activemq', {native_parser:true});
var airavata = mongo.db('mongodb://localhost:27017/airavata', {native_parser:true});
var jetty = mongo.db('mongodb://localhost:27017/jetty', {native_parser:true});
var vuze = mongo.db('mongodb://localhost:27017/vuze', {native_parser:true});
var wildfly_core = mongo.db('mongodb://localhost:27017/wildfly-core', {native_parser:true});

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));


app.use(function(req,res,next){
    var db = [];
    db[0] = activemq;
    db[1] = airavata;
    db[2] = jetty;
    db[3] = vuze;
    db[4] = wildfly_core;
    req.db = db;
    next();
});

app.use('/', index);
app.use('/users', users);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : err;
  console.log(err);
  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
