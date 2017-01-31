var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.post('/search', function(req, res, next) {

  //console.log(req.body);
  var db = req.db[req.body.id];
  //console.log(db);
  var _cn = req.body.className;
  var _mn = req.body.methodName;
  db.collection('IndexSyncCall').find({
    name: _cn,
    methodName: _mn
  }).toArray(function (err, items) {
        res.json(items);
    });
  console.log("end");
  //res.render('index', { title: 'Express' });
});
module.exports = router;