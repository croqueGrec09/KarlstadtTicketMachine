$(document).ready(function(){
	$("#dateRange input").each(function() {
		$(this).datepicker({
			format: 'dd.mm.yyyy',
			defaultViewDate: '08.10.1784',
			startDate: '08.10.1784',
			endDate: '31.12.1802'
		});
	});
});