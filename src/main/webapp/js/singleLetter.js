$(document).ready(function () {
	$('[data-toggle="tooltip"]').tooltip();
	
	var r = 0;
	var g = 0;
	var b = 0;
	$('.section').each(function(i,section){
		$(this).css({"backgroundColor":"rgba("+r+","+g+","+b+",0.4)"});
		r+=10;
		b+=10;
	});
});