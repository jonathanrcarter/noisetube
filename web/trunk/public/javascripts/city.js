var map;
var images=[];
var selected_tag=null;
var tags={};
var marker_tags={};

function initialize() {
    if (GBrowserIsCompatible()) {
        map = new GMap2(document.getElementById("map_canvas"));
        map.setCenter(center, 13);
        
        map.addControl(new GSmallMapControl());
        map.addControl(new GMapTypeControl());
        GEvent.addListener(map, "moveend", function() {
            contextualize_info();
        });
        
        for (var i=0;i<tracks.length;i++){
            var overlay=new GGroundOverlay(tracks[i][2], tracks[i][1]);
            map.addOverlay(overlay);
        }
        
        contextualize_info();
    }
}
                      
function contextualize_info(){
    var sw=map.getBounds().getSouthWest();
    var ne=map.getBounds().getNorthEast();    
    contextualize_timedist(sw,ne);
    contextualize_tagdist(sw,ne);
    contextualize_userdist(sw,ne);
    contextualize_leqdist(sw,ne);
    tags={};
    if (selected_tag!=null){
        display_tagdist(selected_tag);
    }                            
}
       
function contextualize_timedist(sw,ne){
    var url="/api/timedist_extended.png?box="+sw.lng()+","+sw.lat()+","+ne.lng()+","+ne.lat();
    $("timeWait").show(); 
    $("timeDistImg").src=url;
}

function time_complete(){
    $("timeWait").hide(); 
}

function contextualize_leqdist(sw,ne){
    var url="/api/leqdist.png?box="+sw.lng()+","+sw.lat()+","+ne.lng()+","+ne.lat();
    $("leqWait").show(); 
    $("leqDistImg").src=url;
}

function leq_complete(){
    $("leqWait").hide(); 
}

function show_all_tags(){
    for (i=0 ;i<tags.length;i++){
        show_tag($("tag_"+tags[i][0]),tags[i][1]);
    }
}
 
function hide_all_tags(){
    if (marker_tags!=undefined){
        for(var i=0;i<marker_tags.length;i++){
            for(var j=0;j<marker_tags[i].length;j++){
                map.removeOverlay(marker_tags[i][j]);
            }
        }
    }
}

function toggle_tag(el,tag){  
    if (el.checked){
        hide_tag(el, tag);
    }else {
        show_tag(el, tag);
    }
}

function hide_tag(el,tag){
    if (el.checked){
        if (marker_tags[tag]!=undefined){
            for(var i=0;i<marker_tags[tag].length;i++){
                map.removeOverlay(marker_tags[tag][i]);
            }
            marker_tags[tag]=undefined;
        }
        el.checked=false;
    }
}

function show_tag(el,tag){
    
    if (!el.checked){
        // check el
        el.checked=true;
        
        // retrieve data 
        var sw=map.getBounds().getSouthWest();
        var ne=map.getBounds().getNorthEast();
        var url="/api/search?tag="+tag+"&box="+sw.lng()+","+sw.lat()+","+ne.lng()+","+ne.lat();
        new Ajax.Request(url, {method:'get',onSuccess: function(transport){
                var data = transport.responseText.evalJSON();
                marker_tags[tag]=[];
                for (var i=0 ;i<data.length;i++){
                    var m=data[i];
                    if (m.lat){
                        var point1 = new GLatLng(m.lat,m.lng);
                        var marker = new GMarker(point1,{title:m.tags});
                        marker_tags[tag].push(marker);
                        map.addOverlay(marker); 
                    }
                }
            }
        });
    }
}

function contextualize_tagdist(sw,ne){               
    var url="/api/tagdist?box="+sw.lng()+","+sw.lat()+","+ne.lng()+","+ne.lat();
    $("tagWait").show(); 
    new Ajax.Request(url, {method:'get',onSuccess: function(transport){
            $("tagWait").hide(); 
            tags = transport.responseText.evalJSON();
            $("tagList").innerHTML="";                       
            var sum=0.0;
            var i=0
            for (i=0 ;i<tags.length;i++){sum+=tags[i][1];}
            for (i=0 ;i<tags.length;i++){
                var li = document.createElement('li');
                var s="<input type='checkbox' id=\"tag_"+tags[i][0]+"\" onClick='display_tag(this,\""+tags[i][0]+"\");'>"
                var style_css=  (tags[i][1]/sum > 0.3) ?  "font-size:1.6em;font-weight:bold;" :
                    (tags[i][1]/sum > 0.2) ?  "font-size:1.4em;font-weight:bold;":
                    (tags[i][1]/sum > 0.1) ?  "font-size:1.2em":
                    (tags[i][1]/sum > 0.05) ? "font-size:1.0em":  "font-size:0.8em";                            
                s+="<a style='"+style_css+"' href='javascript:display_tagdist(\""+tags[i][0]+"\");'>"+tags[i][0]+"("+tags[i][1]+")</a>";
                s+="</input>";
                li.innerHTML=s;
                $("tagList").appendChild(li);
                //$("<a>").text(data[i][0]+"("+data[i][1]+")").attr({title:"count: "+(data[i][1]), href:"javascript:display_tag('"+data[i][0]+"');"}).appendTo(li);                              
                // li.appendTo("#tagList"); */
            }
        }});
}

function contextualize_userdist(sw,ne){
    var url="/api/userdist?box="+sw.lng()+","+sw.lat()+","+ne.lng()+","+ne.lat();
    new Ajax.Request(url, {method:'get',onSuccess: function(transport){
            var data = transport.responseText.evalJSON();
            $("userList").innerHTML="";
            for (var i=0 ;i<data.length;i++){
                var li = document.createElement('li');
                var img = document.createElement('img');
                img.src="http://binarylogic.lighthouseapp.com/images/avatar.gif";
                //var s="<a href='/users/1'><img alt='nico' src='' width=30 height=30 />100%</a>";
                li.appendChild=img;
                $("userList").appendChild(li);
            }
        }});
}



            
function display_tagdist(tag){
    $("tagDist").show();              
    selected_tag=tag;
    var sw=map.getBounds().getSouthWest();
    var ne=map.getBounds().getNorthEast();
    var url="/api/leqdist.png?tag="+tag+"&box="+sw.lng()+","+sw.lat()+","+ne.lng()+","+ne.lat();
    $("tagDistImg").src="http://static.space.canoe.ca/s-rsoc/img/load.gif";
    $("tagDistImg").src=url;
    $("tagname").innerHTML=tag;
}
