function Utils() {
	
	this.readTwitterTeamsUser = function(){
		
		return $('#tuser').val();
		
	};
	
	this.readTwitterTeamsProblem = function(){
		
		return $('#problem').val();
		
	};
}

var utils = new Utils();