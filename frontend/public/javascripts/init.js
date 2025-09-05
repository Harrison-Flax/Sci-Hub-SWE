$(document).ready(function() {
    $('.tooltipped').tooltip();
    $('.datepicker.mmddyyyy').datepicker({
        format: 'mm/dd/yyyy',
        autoClose: true
    });
    $(".dropdown-trigger").dropdown({
        alignment: 'right',
        constrainWidth: false,
        coverTrigger: false
    });
    $('.tooltipped').tooltip();
    $('.sidenav').sidenav({
        edge: 'right'
    });
    $('#slide-chatbox').sidenav({
        edge: 'right'
    });
    $('.modal').modal();
    $('select').formSelect();
    
    $('.collapsible').collapsible();
    $('#textarea1').val();
    $('ul.tabs').tabs();
    $('.fixed-action-btn').floatingActionButton();
    $('#mainCarouselDatasearch.carousel.carousel-slider').carousel({
        duration: 1000,
        fullWidth: true,
        indicators: true
    });
     // Setting carousel timeout option.
    mainCarouselTimeoutDataset = startMainCarouselDatasearch();
    $("#nextOnMainCarouselDarasearch").click(nextOnMainCarouselDarasearch);
    $("#prevOnMainCarouselDatasearch").click(prevOnMainCarouselDatasearch);

      $('#mainCarousel.carousel.carousel-slider').carousel({
        duration: 1000,
        fullWidth: true,
        indicators: true
    });
     
    // Setting carousel timeout option.
    mainCarouselTimeout = startMainCarousel();
    // Attach next and prev events.
   $("#nextOnMainCarousel").click(nextOnMainCarousel);
    $("#prevOnMainCarousel").click(prevOnMainCarousel);

  var el = document.getElementById('notification_tab');
  var instance = M.Tabs.init(el, {});
  if (instance) {
    instance = M.Tabs.getInstance(el);
    instance.select('notification_tab');
    instance.updateTabIndicator();
  }
});

$(window).resize(function() {
    $('#mainCarousel.carousel.carousel-slider').carousel({
        duration: 1000,
        fullWidth: true,
        indicators: true
    });
    // Setting carousel timeout option.
    mainCarouselTimeout = startMainCarousel();
});

function startMainCarousel() {
    return setInterval(function() {
        $('#mainCarousel.carousel.carousel-slider').carousel('next');
    }, 10000);
}
function nextOnMainCarousel() {
    clearTimeout(mainCarouselTimeout);
    // Perform the next function;
    $('#mainCarousel.carousel.carousel-slider').carousel('next');
    mainCarouselTimeout = startMainCarousel();
}
function prevOnMainCarousel() {
    clearTimeout(mainCarouselTimeout);
    // Perform the prev function;
    $('#mainCarousel.carousel.carousel-slider').carousel('prev');
    mainCarouselTimeout = startMainCarousel();
}
function startMainCarouselDatasearch() {
    return setInterval(function() {
        $('#mainCarouselDatasearch.carousel.carousel-slider').carousel('next');
    }, 10000);
}

function nextOnMainCarouselDarasearch() {
    clearTimeout(mainCarouselTimeoutDataset);
    // Perform the next function;
    $('#mainCarouselDatasearch.carousel.carousel-slider').carousel('next');
    mainCarouselTimeoutDataset = startMainCarouselDatasearch();

}
function prevOnMainCarouselDatasearch() {
    clearTimeout(mainCarouselTimeoutDataset);

    // Perform the prev function;
    $('#mainCarouselDatasearch.carousel.carousel-slider').carousel('prev');
    mainCarouselTimeoutDataset = startMainCarouselDatasearch();

}
