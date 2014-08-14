(function () {
  var nbSlides = document.querySelectorAll('.reveal .slides section:not(.stack)' ).length;
  var baseUrl = 'http://localhost:8080/'
  function post(url, data, callback) {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
      if (xhr.readyState == 4 && xhr.status == 200) {
        callback(xhr.responseText);
      }
    }
    xhr.open('POST', baseUrl + url, true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhr.send("data=" + data);
  }
  function ackAndListen(command) {
    if (command === 'next') {
      Reveal.next();
    }
    if (command === 'previous') {
      Reveal.prev();
    }
    var note = Reveal.getCurrentSlide().querySelector('aside');
    if(note) {
      note = note.innerHTML;
    } else {
      note = "No Speaker notes";
    }
    var acknowledge = {
      totalSlides: nbSlides,
      slideNumber: getCurrentSlideNumber(),
      message: note
    };
    post('ack', JSON.stringify(acknowledge), ackAndListen);
  }
  post('listen', null, ackAndListen);

  function getCurrentSlideNumber() {
      var horizontalSlides = Array.prototype.slice.call( document.querySelectorAll( '.reveal .slides>section' ) );
      var pastCount = 0;
      // Step through all slides and count the past ones
      mainLoop: for( var i = 0; i < horizontalSlides.length; i++ ) {
        var horizontalSlide = horizontalSlides[i];
        var verticalSlides = Array.prototype.slice.call( horizontalSlide.querySelectorAll( 'section' ) );
        for( var j = 0; j < verticalSlides.length; j++ ) {
          // Stop as soon as we arrive at the present
          if( verticalSlides[j].classList.contains( 'present' ) ) {
            break mainLoop;
          }
          pastCount++;
        }
        // Stop as soon as we arrive at the present
        if( horizontalSlide.classList.contains( 'present' ) ) {
          break;
        }
        // Don't count the wrapping section for vertical slides
        if( horizontalSlide.classList.contains( 'stack' ) === false ) {
          pastCount++;
        }
      }
      return pastCount;
  }

})();
