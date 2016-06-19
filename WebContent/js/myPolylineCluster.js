function myPolylineCluster(prop, opts){
	    this.prop = prop;
	    this.Polyline = new google.maps.Polyline(opts);
	    
	    function setMap (map) {
	        this.Polyline.setMap(map);
	    }
	    
	   this.Polyline.addListener("click",
				function(polyMouseEvent) {
					alert("Moving Object ID : '" +prop.trajid+"' \n"
							+ "Micro-group ID : '"	+prop.microgroupid+"' \n"
							+ "Cluster ID : '"	+prop.clusterid+"' \n");

				});	    
	}

	myPolylineCluster.prototype.getPolyline = function() {
	    return this.Polyline;
	}

	myPolylineCluster.prototype.setMap = function(map) {
	    return this.Polyline.setMap(map);
	}

	myPolylineCluster.prototype.getPath = function() {
	    return this.Polyline.getPath();
	}

	myPolylineCluster.prototype.addListener= function(prop) {
	    return this.Polyline.addListener();
	} 

	myPolylineCluster.prototype.getProp= function() {
	    return this.prop;
	}

	myPolylineCluster.prototype.setProp= function(prop) {
	    return this.prop = prop;
	} 
	
	
