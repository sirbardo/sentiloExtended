function TwitterTeams() {
	
	this.find = function(problem, user){
		
		alert(problem);
		alert(user);
		var data = {
			problem: problem,
			user: user
		};
	     
		 $.ajaxSetup({ 
		 		'beforeSend': function(xhr) {
		 			xhr.setRequestHeader("Accept", "text/html")} 
		 		}
		 	);
		
		$.ajax({
			type: "POST",
			url: "/finder/find",
			data: data,
			success: function(result) {
				alert(result);
			},
			error: function(result) {
				alert("Error");
			},
			dataType: "html"
		});
	};
	
}

$(function() {
	$( "#selectable" ).selectable({
		stop: function() {
			var job = $("#job").html();
			
			var username = $("#selectable .ui-selected .username").html();
			
			var data = {
				user: username
			};
			     
			$.ajaxSetup({ 
				'beforeSend': function(xhr) {
					xhr.setRequestHeader("Accept", "application/json")
				} 
			});
			
			var centralityWeight = $("#selectable .ui-selected .centrality").html();
			
			centralityWeight = parseInt(centralityWeight);
			
			var pattern;
			if(centralityWeight > 0){
				pattern = "hub.user.pattern";
			}
			else{
				pattern = "information.source.pattern";
			}
			
			$.ajax({
				type: "GET",
				url: "/pattern/" + job + "/" + pattern,
				data: data,
				success: function(result) {
					console.log(result);
					var jsonOBJ = jQuery.parseJSON(result);
					$("#infovis").html("");
					init(jsonOBJ);
				},
				error: function(result) {
					alert("Error");
				},
				dataType: "html"
			});
		}
	});
});

var currentPage = 0;
var showPerPage = 10;

var resultsOfPages = 0;
$(document).ready(function(){  
	  
    //getting the amount of elements inside content div  
    var number_of_items = $('#selectable').children().size();  
    //calculate the number of pages we are going to have  
    var number_of_pages = Math.ceil(number_of_items/showPerPage);  
  
    //now when we got all we need for the navigation let's make it '  
  
    //add active_page class to the first page link  
    $('#page_navigation .page_link:first').addClass('active_page');  
  
    //hide all the elements inside content div  
    $('#selectable').children().css('display', 'none');  
  
    //and show the first n (show_per_page) elements  
    $('#selectable').children().slice(0, showPerPage).css('display', 'block');
    
    resultsOfPages = $('#selectable').children().length / showPerPage;
    
    for(var i=0; i<resultsOfPages; i++){
    	var content = '<li><span class="jPag-current" style="color: rgb(0, 0, 0); background-color: rgb(255, 255, 255); border: 1px solid rgb(204, 204, 204); ">' + i + '</span></li>'
    }
    
    $("#pagination_list").html(content);
    
    $("#demo1").paginate({
		count 		: resultsOfPages,
		start 		: 1,
		display     : 10,
		border					: true,
		border_color			: '#fff',
		text_color  			: '#fff',
		background_color    	: 'black',	
		border_hover_color		: '#ccc',
		text_hover_color  		: '#000',
		background_hover_color	: '#fff', 
		images					: false,
		mouse					: 'press',
		onChange				: 	function(page){
										go_to_page(page-1);
									}
	});
  
    //init();
});  
  
function previous(){  
  
    new_page = currentPage - 1;  
    
    if(new_page > 0){
    	go_to_page(new_page);  
    }
      
  
}  
  
function next(){  
    new_page = currentPage + 1;  
    //if there is an item after the current active link run the function  
    if(new_page <= resultsOfPages){  
        go_to_page(new_page);  
    }  
  
}  
function go_to_page(page_num){  
    
	//get the element number where to start the slice from  
    start_from = page_num * showPerPage;  
  
    //get the element number where to end the slice  
    end_on = start_from + showPerPage;  
  
    //hide all children elements of content div, get specific items and show them  
    $('#selectable').children().css('display', 'none').slice(start_from, end_on).css('display', 'block');  
  
    //update the current page input field  
    currentPage = page_num;  
}  

$(function() {

    var $sidebar   = $("#infovis"), 
        $window    = $(window),
        offset     = $sidebar.offset(),
        topPadding = 15;

    $window.scroll(function() {
        if ($window.scrollTop() > offset.top) {
            $sidebar.stop().animate({
                marginTop: $window.scrollTop() - offset.top + topPadding
            });
        } else {
            $sidebar.stop().animate({
                marginTop: 0
            });
        }
    });
    
});

var twitterTeams = new TwitterTeams();