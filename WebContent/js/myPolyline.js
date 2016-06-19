function myPolyline(prop, opts){
	    this.prop = prop;
	    this.Polyline = new google.maps.Polyline(opts);	    
	    
	    function setMap (map) {
	        this.Polyline.setMap(map);
	    }
	    
		   this.Polyline.addListener("click",
					function(polyMouseEvent) {
						alert("Moving Object ID : '" +prop.trajid+"' \n"
								+ "Micro-group ID : '"	+prop.microgroupid+"' \n"
								+ "Micro-group Evolution : '"	+prop.evolution+"' \n");

					});	   
	    
	}

	myPolyline.prototype.getPolyline = function() {
	    return this.Polyline;
	}

	myPolyline.prototype.setMap = function(map) {
	    return this.Polyline.setMap(map);
	}

	myPolyline.prototype.getPath = function() {
	    return this.Polyline.getPath();
	}

	myPolyline.prototype.addListener= function(prop) {
	    return this.Polyline.addListener();
	} 

	myPolyline.prototype.getProp= function() {
	    return this.prop;
	}

	myPolyline.prototype.setProp= function(prop) {
	    return this.prop = prop;
	} 
	
	
