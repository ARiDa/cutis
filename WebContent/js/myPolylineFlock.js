function myPolylineFlock(prop, opts){
	    this.prop = prop;
	    this.Polyline = new google.maps.Polyline(opts);
	    
	    function setMap (map) {
	        this.Polyline.setMap(map);
	    }
	    
	   this.Polyline.addListener("click",
				function(polyMouseEvent) {
					alert("Moving Object ID : '" +prop.trajid+"' \n"
							+ "Flock ID : '"	+prop.flockid+"' \n");

				});	    
	}

	myPolylineFlock.prototype.getPolyline = function() {
	    return this.Polyline;
	}

	myPolylineFlock.prototype.setMap = function(map) {
	    return this.Polyline.setMap(map);
	}

	myPolylineFlock.prototype.getPath = function() {
	    return this.Polyline.getPath();
	}

	myPolylineFlock.prototype.addListener= function(prop) {
	    return this.Polyline.addListener();
	} 

	myPolylineFlock.prototype.getProp= function() {
	    return this.prop;
	}

	myPolylineFlock.prototype.setProp= function(prop) {
	    return this.prop = prop;
	} 
	
	
