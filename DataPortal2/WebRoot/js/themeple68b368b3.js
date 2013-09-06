jQuery(function($) { 
    
    $(".carousel_wrapper").each(function(){

        var $self = $(this);
        var viewport = $(window).width();
        var items = parseInt($self.parent().find('.num_el').html(), 10);

        if(viewport <= 480)
          items = 1;
        else if(viewport > 480 && viewport <= 768)
          items = 1;
        if( $('.caro:first img', $self).size() ) {
          $('.caro:first img', $self).one("load", function(){
          $self.carouFredSel({
              items: items,
              auto  : false,
              circular:false,
              infinite:false,
              responsive:true,

              prev  : { 
                button  : $self.parent().parent().find('.recent_pagination .prev'),
                key   : "left"
              },
              next  : { 
                button  : $self.parent().parent().find('.recent_pagination .next'),
                key   : "right"
              }



          });
        });
        }


    });


    $(".menu .sub-menu a").live('click', function(){
    	var button = $(this);
    	if(button.length > 0){
			var title = button.attr('title').split("-");
    		if(title[0] == 'skin'){

      			document.cookie = 'themeple_skin='+title[1] ; 
      			setTimeout(function(){
                    window.location.hash = "#wpwrap";
                  window.location.reload(true);
                
             	}, 1000);

    		}
		}
  	});
	$('.flexslider').flexslider({slideshow:false});
  		$('.pixel_slider').flexslider_pixel({slideshow:false, controlNav: false});
        $('[rel=tooltip]').tooltip();
        $('[rel=popover]').popover();
        $("audio,video").mediaelementplayer(); 

    $(".carousel_wrapper_2").each(function(){

        var $self = $(this);
        var viewport = $(window).width();
        var items = parseInt($self.parent().find('.num_el').html(), 10);

        if(viewport <= 480)
          items = 1;
        else if(viewport > 480 && viewport <= 768)
          items = 1;
        if( $('.caro:first img', $self).size() ) {
          $('.caro:first img', $self).one("load", function(){
          $self.carouFredSel({
              items:items,
              circular:false,
              auto  : false,
              responsive:true,
              prev  : { 
                button  : $self.parent().find('.prev'),
                key   : "left"
              },
              next  : { 
                button  : $self.parent().find('.next'),
                key   : "right"
              }
              



          });
      });
        }

    });

    $(".clients").each(function(){

        var $self = $(this);
        var viewport = $(window).width();
        var items = parseInt($self.parent().find('.num_el').html(), 10);

        if(viewport <= 480)
          items = 1;
        else if(viewport > 480 && viewport <= 768)
          items = 1;
        if( $('.caro:first img:first', $self).size() ) {
          $('.caro:first img:first', $self).one("load", function(){
          $self.carouFredSel({
              items:items,
              circular:false,
              auto  : false,
              responsive:true,
              prev  : { 
                button  : $self.parent().find('.prev'),
                key   : "left"
              },
              next  : { 
                button  : $self.parent().find('.next'),
                key   : "right"
              }
              



          });
      });
        }

    });

    $(".accordion-group").live('click', function(){
        var $self = $(this);
        $body = $self.find('.accordion-body');
        if($self.find('.accordion-heading').hasClass('in_head')){
          $self.parent().find('.accordion-heading').removeClass('in_head');
        }else{  
          $self.parent().find('.accordion-heading').removeClass('in_head');
          $self.find('.accordion-heading').addClass('in_head');
        }
          
    });


    $(".recent_box .recent_box_image").themeple_zooming_effect();
    $('.tab-content').each(function(){
        $('<div />').addClass('bottom_shadow_full').insertAfter($(this));

    });

    $('.box_image_portfolio').eq(0).load(function(){
        var height = $(this).height();
        

        $container.find('.box_port').attr('height', height+30+'px');
        
    });

    
    var $container = $('#holder .filterable');
    
    
    if( $('.box_image_portfolio', $container).size() ) {
          $('.box_image_portfolio', $container).one("load", function(){
    $container.isotope({
      filter: '*',
      animationOptions: {
         duration: 750,
         easing: 'linear',
         queue: false,
       }
    });
    setTimeout(function(){
        $container.isotope({
          filter: '*',
          animationOptions: {
             duration: 750,
             easing: 'linear',
             queue: false,
           }
        });
    }, 100);
    });
}

  $('.nav-port li a').click(function(){
    var selector = $(this).attr('data-filter');
    $(this).parent().parent().find('.active').removeClass('active');
    $(this).parent().addClass('active');
    $container.isotope({ 
    filter: selector,
    animationOptions: {
       duration: 750,
       easing: 'linear',
       queue: false,
     
     }
    });
    return false;
  });

  
  if($().mobileMenu) {
    $('div.menu').each(function(){
      $(this).mobileMenu();
    });
  }  
    
  



});
(function($)
{
	
    

    $.fn.themeple_ajax_latest_blog = function(variables)
    {
        return this.each(function(){
            var $base_container = $(this);
            jQuery(".recent_pagination .arrow", $base_container).click(function(){ 
                var load = '';
                var $type = $(this).children();
                var $button = $(this);
                var $container = $base_container.find('.recent_content');
                if($type.hasClass('next')){
                    load = 'next';
                }else
                    load = 'prev';   

                var paged = $container.find('.paged').html();
                var num_to_load = AjaxRequests.num_to_load;
                var ajaxurl = AjaxRequests.ajaxurl;
                if(paged == 0)
                    paged = 1;
                if(load == 'next'){
                    paged++;
                    if(paged > AjaxRequests.max_num_pages)
                        paged = 1;
                    
                }else{
                    paged--;
                    if(paged < 1)
                        paged = AjaxRequests.max_num_pages;   
                }
                jQuery.ajax({  
                      type: 'POST',  
                      url: ajaxurl,  
                      data: {  
                          action: 'pixel_latest_blog',  
                          paged: paged,
                          num_to_load: num_to_load

                      },  
                  success: function(data, textStatus, XMLHttpRequest){  
                      jQuery($container).css('display', 'none');
                      setTimeout(function(){

                            jQuery($container).html(data).fadeIn();
                            $('.flexslider').flexslider({slideshow:false});
                            $('.pixel_slider').flexslider_pixel({slideshow:false, controlNav: false});
                      },200);
                  },  
                  error: function(MLHttpRequest, textStatus, errorThrown){  
                    alert(errorThrown);  
                  }  
                });  
            });  
           
        });
    },

    $.fn.themeple_ajax_recent_portfolio = function(variables)
    {
        return this.each(function(){
            var $base_container = $(this);
            jQuery(".recent_pagination .arrow", $base_container).click(function(){ 
                var load = '';
                
                var $button = $(this);
                var $type = $button.children();
                var $container = $base_container.find('.recent_content');
                if($type.hasClass('next')){
                    load = 'next';
                }else
                    load = 'prev';   

                var paged = $container.find('.paged').html();
                var num_to_load = AjaxRequestsPort.num_to_load;
                var ajaxurl = AjaxRequestsPort.ajaxurl;
                if(paged == 0)
                    paged = 1;
                if(load == 'next'){
                    paged++;
                    if(paged > AjaxRequestsPort.max_num_pages)
                        paged = 1;
                    
                }else{
                    paged--;
                    if(paged < 1)
                        paged = AjaxRequestsPort.max_num_pages;
                       
                }
                jQuery.ajax({  
                      type: 'POST',  
                      url: ajaxurl,  
                      data: {  
                          action: 'pixel_recent_portfolio',  
                          paged: paged,
                          num_to_load: num_to_load

                      },  
                  success: function(data, textStatus, XMLHttpRequest){  
                      jQuery($container).css('display', 'none');
                      setTimeout(function(){

                            jQuery($container).html(data).fadeIn();
                            $('.flexslider').flexslider({slideshow:false});
                            $('.pixel_slider').flexslider_pixel({slideshow:false, controlNav: false});
                      },200);
                  },  
                  error: function(MLHttpRequest, textStatus, errorThrown){  
                    alert(errorThrown);  
                  }  
                });  
            });  
           
        });
    },



    $.fn.themeple_zooming_effect = function(variables)
    {
        return this.each(function(){
            var $el = $(this);
            
            
                $el.parent().hoverZoom({zoom:100, overlay:false});
                $el.one("load", function(){
                    
                
                    $el.greyScale({
                      fadeTime: 500,
                        reverse: false  
                    });
                    $el.animate({
                      opacity: 1
                    }, 1000);

                
                    


                    
                  }).each(function() {
                      if(this.complete) $(this).trigger("load");
                  });

                  
                      
                 
                  
                  
                  
            
           
        });
    }
    

})(jQuery);



(function($){

    $.fn.extend({ 

        hoverZoom: function(settings) {
 
            var defaults = {
                overlay: true,
                overlayColor: '#2e9dbd',
                overlayOpacity: 0.7,
                zoom: 25,
                speed: 300
            };
             
            var settings = $.extend(defaults, settings);
         
            return this.each(function() {
            
                var s = settings;
                var hz = $(this);
                var image = $('img', hz);

                image.load(function() {
                    
                    if(s.overlay === true) {
                        $(this).parent().append('<div class="zoomOverlay" />');
                        $(this).parent().find('.zoomOverlay').css({
                            opacity:0, 
                            display: 'block', 
                            backgroundColor: s.overlayColor
                        }); 
                    }
                
                    var width = $(image).width();
                    var height = $(image).height();
                
                    $(this).fadeIn(1000, function() {
                        $(this).parent().css('background-image', 'none');
                        hz.hover(function() {
                            
                            
                            $('img', this).stop().animate({
                                height: height + s.zoom,
                                marginLeft: -(s.zoom),
                                marginTop: -(s.zoom)
                            }, s.speed);
                            image.css('width', 'auto');
                            if(s.overlay === true) {
                                $(this).parent().find('.zoomOverlay').stop().animate({
                                    opacity: s.overlayOpacity
                                }, s.speed);
                            }
                        }, function() {
                            $('img', this).stop().animate({
                                height: height,
                                marginLeft: 0,
                                marginTop: 0
                            }, s.speed);
                            if(s.overlay === true) {
                                $(this).parent().find('.zoomOverlay').stop().animate({
                                    opacity: 0
                                }, s.speed);
                            }
                        });
                    });
                });    
            });
        }
    });
})(jQuery);
