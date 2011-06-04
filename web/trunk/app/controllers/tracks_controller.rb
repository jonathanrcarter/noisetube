# --------------------------------------------------------------------------------
#  NoiseTube Web application
#  
#  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
#  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
# --------------------------------------------------------------------------------
#  This library is free software; you can redistribute it and/or modify it under
#  the terms of the GNU Lesser General Public License, version 2.1, as published
#  by the Free Software Foundation.
#  
#  This library is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
#  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
#  details.
#  
#  You should have received a copy of the GNU Lesser General Public License along
#  with this library; if not, write to:
#    Free Software Foundation, Inc.,
#    51 Franklin Street, Fifth Floor,
#    Boston, MA  02110-1301, USA.
#  
#  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
#  NoiseTube project source code repository: http://code.google.com/p/noisetube
# --------------------------------------------------------------------------------
#  More information:
#   - NoiseTube project website: http://www.noisetube.net
#   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
#   - VUB BrusSense team: http://www.brussense.be
# --------------------------------------------------------------------------------
 
class TracksController < ApplicationController
  
  before_filter :find_track
    
  def destroy
   # if logged_in? && @user==current_user
      @track.destroy
    #end
    
    respond_to do |format|
      format.html { redirect_to(@user) }
      format.xml  { head :ok }
    end
    
  end

  def dist
    
  end

  # tracks/{last,id}.{json,png}


  def animated_map
    respond_to do |format|
      format.kml  {
        @frames,@taged_frames,@segments=AnimatedMap.generate_track(@track.id)    
        @time_offset=AnimatedMap::Time_offset
      }
    end
  end

    
  def show
        
    respond_to do |format|
      
      format.json  {
        
        # special format for chronoscope
        if params[:chronoscope]
          chronoscope
        else          
          measures=[]
          @track.measures.find(:all, :order=>"made_at").each {|m|
            measures << {:tags=> m.tag_list,
              :lat=>(m.geom.nil?)? nil : m.geom.lat,
              :lng=>(m.geom.nil?)? nil : m.geom.lng,
              :db=>m.loudness,
              :date=>m.made_at.to_s}
            }
          render :json=>"dataset=#{measures.to_json}"
        end
      }
      
      format.kml{

        @baseurl=root_url
        @resource_url=user_track_url(@user, @track)

        # tracks/{id}.kml?dl=1 => dynamic legend
        if (params[:dl]=="1")
          @box=params[:BBOX]
          render :template=>"tracks/map_legend.kml.erb" , :mimetype => :kml

        # kml map
        else          
          @ranges=Measure.by_range(@track.measures)
          @tags=[]
          Measure.tag_counts(:order=>"name DESC").each{|tag|
            taggings=tag.taggings.find(:all, :include=>[:taggable, :tagger], :joins=>"LEFT OUTER JOIN measures ON taggings.taggable_id = measures.id LEFT OUTER JOIN tracks ON measures.track_id = tracks.id" , :conditions=>"measures.loudness is not null and measures.geom is not null and tracks.id=#{@track.id} and taggings.context='tags'")
            @tags<<{:name=>tag.name, :taggings=>taggings, :count=>taggings.size} unless taggings.empty?
          }
          render :template => "tracks/map.kml.erb", :mimetype => :kml
        end
      }
      
      format.png{
        url=@track.to_graph_url
        
        # replace size if requested
        url=url.gsub(/chs=(\d+)x(\d+)/,"chs=#{params[:size]}") unless params[:size].blank?
        return redirect_to(url)
      }
    end
  end
  
  private 
  
  def chronoscope
    domain=[]
    range=[]  
    tags=[]
    track=User.find(params[:user_id]).tracks.find(params[:id])        
    track.measures.find(:all, :select=>"loudness, made_at", :order=>"made_at").each {|m|
      domain << m.made_at.to_i*1000
      range << m.loudness
    }
    taggings=Tagging.find(:all,:joins=>"INNER JOIN measures ON measures.id = taggings.taggable_id" , :conditions=>"measures.track_id=#{track.id}", :order=>"measures.made_at")
    taggings.each{|tagging|
      tags<<"[#{tagging.created_at.to_i*1000},\""+tagging.tag.name+"\"]" 
    }        
    render :json=>"dataset={dataset :{Id:\"#{params[:tid]}\", domain:[#{domain.join(",")}], range:[#{range.join(",")}], rangeTop:100, rangeBottom:30, label: \"Recording\", axis:\"dB(A)\"},  tags :[#{tags.join(",")}]}"
  end
  
  
  def find_track
    @user=User.find(params[:user_id])
      
    if (params[:id]=="last")
      @track=@user.tracks.find(:last)
    else  
      @track=@user.tracks.find(params[:id])
    end
    
    unless @track 
      raise ActiveRecord::RecordNotFound
    end
  end

end

