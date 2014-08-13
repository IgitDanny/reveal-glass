(function () {
  var source = new EventSource('http://192.168.0.42:8080/listen');
  source.addEventListener('message', function(e) {
    console.log(e.data);
  }, false);
})();
