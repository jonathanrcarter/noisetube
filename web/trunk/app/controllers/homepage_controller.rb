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
 
class HomepageController < ApplicationController
  include Geokit::Geocoders

  caches_action :index, :layout=>false

  def index
    @invitation=Invitation.new
    # fill the location according to the IP
    location=get_location_by_ip
    if (!location.nil? && (location[:country_code]=='FR') && (params[:locale].nil?))
      I18n.locale='fr'
    end
    @invitation.location="#{location[:city]}, #{location[:country_code]}" unless location.nil?
    render :layout => false
  end
  
  def team
    render :layout => "layouts/application"
  end
  
  def publications
    render :layout => "layouts/application"
  end
    
  # POST /invitations POST /invitations.xml
  def invitation
    @invitation = Invitation.new(params[:invitation])
    respond_to do |format|
      if @invitation.save
        format.js{
          # no need rjs file
          render :update do |page|
            page.remove "invitation_form"
            page.replace_html :notice, "Thank you!"
            page.visual_effect :highlight, :notice, :duration => 2 
          end
        }
      else
        format.js{
          render :update do |page| 
            page.replace_html :notice, @invitation.errors.full_messages[0]
            page.visual_effect :highlight, :notice, :duration => 2 
          end 
        }
      end
    end
  end
 
  # #################### TEST Function ######################
 
  def test7
    measures=Measure.find(:all, :conditions=>"location is null")
    Measure.corrector(measures)
  end
  

  def map_normalgps
    respond_to do |format|     
      format.kml{
        @tracks=Track.find(:all, :include=>:measures, :conditions => "geolocated=true and count>500")
        render :template=>"/cities/map_normal_gps.kml.erb"
      }
    end
  end
  
  def map_correctedgps
    respond_to do |format|     
      format.kml{
        city=City.find(:first,:conditions=>{:name=>"Paris", :country=>"FR"})
        @tracks=city.tracks.find(:all,:conditions=>"geolocated=true")
        render :template=>"/cities/map_corrected_gps.kml.erb"
      }
    end
  end
  
  def map_districts_impacts
      
    @data=[]
    districts=District.find(:all)
    # #calcul the total length
    
    
    respond_to do |format|     
      format.kml{
        
        districts.each{|s|
         
          if s.measures.size>0    
              average=Measure.aggregate_loudness(s.measures) 
              population_density=s.population.to_f/s.surf.to_f
              hia_indicator=average.to_f*population_density
              puts "#{average} #{population_density} #{hia_indicator}"
            @data << {:o=>s, :geom=>s.geom, :size=>hia_indicator, :average=>average, :name=>s.tln}
          end
        }
            
        @ranges=[]
        Measure.loudness_index_range(Measure::MIN_DB,Measure::MAX_DB,Measure::STEP_DB){ |min,max,i|
          text=""
          seg=[]
          if (max.nil?)
            text="> #{min} dB(A)" 
            seg=@data.select{|d| d[:average]>=min }
          else if (min.nil?)
              text="< #{max} dB(A)" 
              seg=@data.select{|d| d[:average]<=max }
            else
              text="[#{min}, #{max}] dB(A)" 
              seg=@data.select{|d| d[:average]>=min && d[:average]<=max }
            end
          end
          
          @ranges<<{:label=>text+", #{seg.length} areas", :urbanel=>seg}
        }
        render :template=>"/cities/map_with_districts_impacts.kml.erb"
      }
    end
  end
  
  def map_districts
    
    @data=[]
    districts=District.find(:all)
    # #calcul the total length
    
    
    respond_to do |format|     
      format.kml{
        
        districts.each{|s|
         
          if s.measures.size>0    
            average=Measure.aggregate_loudness(s.measures)  
            @data << {:o=>s, :geom=>s.geom, :size=>s.measures.size , :average=>average, :name=>s.tln}
          end
            s.segment.find(:all, :joins=>:measures, :conditions=>"measures.segment_id is not null").each{|d|
            p1=d.points[0]
            p2=d.points[1]
            distance=p1.ellipsoidal_distance(p2).to_f
            cov=distance/s.roaddist.to_f
            exposure=Measure.aggregate_loudness(d.measures)
            certainty=d.measures.size
          #  segment<<{:s=>s, :cov=>cov , :distance=>distance, :size=>, :average=>Measure.aggregate_loudness(s.measures)}
        }
        }
            
        @ranges=[]
        Measure.loudness_index_range(Measure::MIN_DB,Measure::MAX_DB,Measure::STEP_DB){ |min,max,i|
          text=""
          seg=[]
          if (max.nil?)
            text="> #{min} dB(A)" 
            seg=@data.select{|d| d[:average]>=min }
          else if (min.nil?)
              text="< #{max} dB(A)" 
              seg=@data.select{|d| d[:average]<=max }
            else
              text="[#{min}, #{max}] dB(A)" 
              seg=@data.select{|d| d[:average]>=min && d[:average]<=max }
            end
          end
          
          @ranges<<{:label=>text+", #{seg.length} areas", :urbanel=>seg}
        }
        render :template=>"/cities/map_with_districts.kml.erb"
      }
    end
  end
  
  def map_population_districts
    respond_to do |format|     
      format.kml{
        @districts=District.find(:all)
        render :template=>"/cities/map_with_populationdistricts.kml.erb"
      }
    end
  end
  
  def map_segments
    respond_to do |format|     
      @data=[]
      segments=Segment.find(:all, :include=>:measures, :conditions=>"measures.segment_id is not null")
      segments.each{|s|
        # #level number of data , color =aggregated level
        @data << {:geom=>s.geom, :size=>s.measures.size , :average=>Measure.aggregate_loudness(s.measures), :name=>s.urban.name }
      }
      @ranges=[]
      Measure.loudness_index_range(Measure::MIN_DB,Measure::MAX_DB,Measure::STEP_DB){ |min,max,i|
        text=""
        seg=[]
        
        if (max.nil?)
          text="> #{min} dB(A)" 
          seg=@data.select{|d| d[:average]>=min }
        else if (min.nil?)
            text="< #{max} dB(A)" 
            seg=@data.select{|d| d[:average]<=max }
          else
            text="[#{min}, #{max}] dB(A)" 
            seg=@data.select{|d| d[:average]>=min && d[:average]<=max }
          end
        end
          
        @ranges<<{:label=>text+", #{seg.length} areas", :segment=>seg}
      }
      format.kml{
        render :template=>"/cities/map_with_segments.kml.erb"
      }
    end
  end
  
  
  def map_roads
    respond_to do |format|     
     
      roads=Urban.find(:all, :include=>:measures, :conditions=>"measures.segments and city=1")
      roads.each{|r|
        # #level number of data , color =aggregated level
      }
      format.kml{
        render :template=>"/cities/map2.kml.erb"
      }
    end
  end

  # ## reverse geocoding +geocoding didnt work
  def test8
    last_m=nil
    measures=Measure.find(:all, :conditions=>"location is not null and corrected is null").each{|m|
      if (!last_m.nil? && last_m.location==m.location)
        m.corrected=Point.from_x_y(last_m.corrected.lng,last_m.corrected.lat)  
      else 
        # #puts "location ==== #{m.location} EBD "
        res=MultiGeocoder.geocode(m.location)
        unless (res.nil?)
          # p res.ll.split(",")
          lat,lng=res.ll.split(",")
          m.corrected=Point.from_x_y(lng.to_f,lat.to_f)
        end
        m.save
        last_m=m
      end
    }
    Measure.corrector(measures)
  end 
    
  def polygon(points) 
    points
  end

  def test10
    Track.find_by_id(477).calcule_postprocessing_attribute
  end

  def features

  end

  def indexation
     t=City.find_by_name("Paris").tracks.first
     t.indexation
  end
  
  def populatetag
    s=Tag.count
    user=User.first
    # #if (rand(100)==3)
    #  name=Tag.find_by_id(rand(s)+1).name
    #  user.tag(e, :with=>name, :on=>:tags)
    # #end
  end
  
  def reindex
    
    render :nothing => true
  end
  
  def test4
    # delete bad measure of paris
    ny=City.find(2)
    Measure.find(:all, :select=>" distinct track_id, user_id").each {|m|
      size=Track.count(:conditions=>"id=#{m.track_id}")
     
      if  (size==0)
        t=Track.new
        t.id=m.track_id
        t.user=m.user
        t.city=ny
        t.save!
      end
    }
    render :nothing=>true
  end
   
end
