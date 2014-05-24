var queryText = "";	  
var isFirst = true;
var lastReq = "";
		$("document").ready(function()
		{
			$("input[name=query]").focus();
			var position = $("input[name=query]").position();
			$(".dropDown").css({"top":position.top+$("input[name=query]").height()+4,"left":position.left+2,"width":$("input[name=query]").width()+2});
						
			$("input[name=query]").keyup(function(e)
			{
				if(e.keyCode==13)
					return true;
				else if(e.keyCode==27 || e.keyCode==32)
				{
					$(".dropDown").slideUp("fast");
				}
				else if(e.keyCode==40 && $(".dropDown").css("display")!="none")
				{
					queryText = $(this).val();
					$(this).blur();
					$(".dropDown>div>input:first").focus();
				}
				else if(e.keyCode==38 && $(".dropDown").css("display")!="none")
				{
					queryText = $(this).val();
					$(this).blur();
					$(".dropDown>div>input:last").focus();
				}
				else
				{
					var sug = $("input[name=query]").val();
					if(sug=="")
					{
						$(".dropDown").slideUp();
						return;
					}
					var temp = sug.split(" ");
					var index = sug.lastIndexOf(" ");					
					if(index!=-1)
						sug = sug.substring(0,index)+" ";
					else
						sug="";
					if(temp[temp.length-1]!="")
					{
						if(lastReq != temp[temp.length-1])
						$.get("suggest?q="+temp[temp.length-1],function(data,status)
						{							
							if(data!="" && data!=null)
							{
								data = data.split(",");
								var child = $(".dropDown>div:nth-child(1)");
								for(i=0;i<data.length;i++)
								{
									child.children().val(sug+data[i]).show();
									child = child.next();
								}
								$(".dropDown").slideDown("fast");
							}
							else
							{
								$(".dropDown").slideUp("fast");
							}
						});	
						lastReq = temp[temp.length-1];
					}
				}						
			});
			
			$(document).keydown(function(e) {
			     var key = e.which;
			      if(key == 38 || key == 40)
			      {		    	  
			          //e.preventDefault();
			          return false;
			      }
			      return true;
			});
			
			$(".dropDown>div>input").keyup(function(e)
			{				
				var key = e.keyCode;
				if(key==27)
				{
					$("input[name=query]").val(queryText).focus();
					$(".dropDown").slideUp();
				}
				else if(key==40)
				{
					$("input[name=query]").val(queryText).focus();
					$(this).blur().parent().next().children().focus();
				}
				else if(key==38)
				{
					$("input[name=query]").val(queryText).focus();
					$(this).blur().parent().prev().children().focus();
				}
				else if(key==39)
				{
					$(".dropDown").hide();
					$("input[name=query]").focus();
					return false;
				}
				else if(key==13)
				{
					$(".dropDown").hide();
					$("input[name=query]").focus();
					return true;
				}
//				else if(key==32)
//				{
//					$(".dropDown").hide();					
//					$("input[name=query]").val($(this).val()+" ").focus();
//					return false;
//				}
			});

			$(".dropDown>div>input").click(function()
			{
				$("input[name=query]").val($(this).val()).focus();
				$(".dropDown").hide();
			});
			
			$(".dropDown>div>input").focus(function()
			{
				this.selectionStart = this.selectionEnd;
				$(this).css({"background":"#e5e5e5"}).parent().css({"background":"#e5e5e5"});
				$("input[name=query]").val($(this).val());
			});
			
			$(".dropDown>div>input").blur(function()
			{
				$(this).css({"background":"#ffffff"}).parent().css({"background":"#ffffff"});
			});
			
		});
		
//google maps code
		
		function initialize()
		{			
			var temp = $("#latlng").text();
			var d = $("#dist").text();
			temp = temp.split(";");
			temp[0] = temp[0].split(",");
			var latlng = new google.maps.LatLng(temp[0][0],temp[0][1]);
			
			var mapProp = {
				center:latlng,
				zoom:6,
				mapTypeId:google.maps.MapTypeId.ROADMAP
		  	};
			var map=new google.maps.Map(document.getElementById("googleMap"),mapProp);
			
			new google.maps.Marker({
				position:latlng,
				icon:"http://www.google.com/intl/en_us/mapfiles/ms/micons/red-dot.png"
			}).setMap(map);
			
			var myCity = new google.maps.Circle({
				  center:latlng,
				  radius:d*1000,
				  strokeColor:"#888888",
				  strokeOpacity:0.8,
				  strokeWeight:2,
				  fillColor:"#bbbbbb",
				  fillOpacity:0.3
				  });

				myCity.setMap(map);
				for(var i=1;i<temp.length;i++)
				{
					temp[i] = temp[i].split(",");
					new google.maps.Marker({
						position:new google.maps.LatLng(temp[i][0],temp[i][1]),
						animation: google.maps.Animation.DROP,
						icon:"http://www.google.com/intl/en_us/mapfiles/ms/micons/blue-dot.png"
					}).setMap(map);
					
				}

			
			
//			var infowindow = new google.maps.InfoWindow({
//				  content:"<a href='http://192.168.1.2:8080/IR_Project_Three/Results.jsp?query=google+map' target='_parent'>Search all news in this Location</a>"
//				  });
//
//				infowindow.open(map,marker);
		}

		