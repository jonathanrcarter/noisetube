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
 
require 'time'
require 'sound_level.rb'

class Measure < ActiveRecord::Base

  acts_as_geom :geom
  acts_as_taggable_on :tags
  

  belongs_to :track
  belongs_to :user
  belongs_to :segment

  validates_numericality_of :loudness
  validates_presence_of :user
  validates_presence_of :track

  attr_accessible :user, :track, :segment, :made_at, :loudness, :loudness_index, :geom #:ozone, :temperature, :humidity, :pressure, :wind

  # ######################## DAO FUNCTIONS ######################

  def self.search(params, includes=nil)
    cond, joins = Measure.search_options(params)
    if cond.nil?
    	raise SearchException.new("Result too large, please add parameters to limit search space")
    else
	return Measure.find(:all, :include=>includes, :joins=>joins, :conditions=>cond, :limit=>500, :order => "made_at DESC")
    end
  end

  def valid?
    SoundLevel.valid?(self.loudness)
  end
  
  # generic search query preparation
  def self.search_options(params)  
    cond=[]
    joins=[]

    # LOUDNESS
    cond<<"loudness <#{params[:dbmax]}" unless params[:dbmax].blank?
    cond<<"loudness >#{params[:dbmin]}" unless params[:dbmin].blank?

    # TAG
    # TODO: does not seem to work, FIX THIS
    unless params[:tags].blank?
      joins<<"INNER JOIN taggings ON taggings.taggable_id = measures.id"
      joins<<"INNER JOIN tags ON tags.id = taggings.tag_id"
      cond1=[]
      params[:tags].split(",").each{ |tag|
        context, name=tag.split(":")
        context="tags" if context.nil?
        name=name.gsub(/\\/, '\&\&').gsub(/'/, "''")
        cond1<<"(tags.name =E'#{name}' and taggings.context=E'#{context}')"

      }
      cond<< "measures.id IN (SELECT taggings.taggable_id FROM taggings LEFT OUTER JOIN tags ON tags.id = taggings.tag_id WHERE #{cond1.join(" OR ")} GROUP BY taggings.taggable_id HAVING COUNT(taggings.taggable_id)=#{cond1.size})"
    end

    # Geoprahical bounding box #
    unless params[:geo].blank?
      cond<< Measure.geoboxCond(params[:geo])
    else
      unless params[:box].blank?
        cond<< Measure.geoboxCond(params[:box])
      end
    end	
	
    # CITY
    #TODO city by name
    city=City.find(params[:city]) unless params[:city].blank? 	# by ID
    unless city.nil?
      cond<<"tracks.city_id=#{city.id}"
      joins<<"LEFT OUTER JOIN tracks ON tracks.id = measures.track_id"
    end

    # TRACK
    track=Track.find(params[:track]) unless params[:track].blank?
    unless track.nil?
      cond<<"tracks.id=#{track.id}"
      joins<<"LEFT OUTER JOIN tracks ON tracks.id = measures.track_id"
    end

    # USER
    unless params[:user].blank?
         u = nil
    	 begin
    	   u = User.find(params[:user])				# by ID
    	 rescue Exception
	   #ignore
    	 end
    	 begin
    	   u = User.find_by_login(params[:user]) if u.nil?	# by login
    	 rescue Exception
	   #ignore
    	 end
    	 cond<<"measures.user_id=#{u.id}" unless u.nil?
    end
    
    # TIME
    #TODO since
    #TODO until
    
    # MAX NUMBER
    #TODO max


    [cond.size>0? cond.join(" and ") : nil, joins.join(" ")]
    
  end


  # save the noise exposure with its meta-data
  def self.save_request(params, track, user)

    # time
    made_at = Time.now
    made_at = DateTime.parse(params[:time]) unless params[:time].blank?

    # store the measure
    measure=Measure.new(:made_at=>made_at,
                        :loudness=> params[:db],
                        :user=>user,
                        :track=>track)
                        
    #loudness_index
    measure.loudness_index = SoundLevel.compute_loudness_index(measure.loudness)
    
    # location
    loc = params[:l]
    # if location is lat,lng coordinates
    if loc=~ /geo\:(.*),(.*)/
      measure.geom=Point.from_x_y($2.to_f, $1.to_f)
    else
    # if location is tag location
      user.tag(measure, :with=>loc.downcase, :on=>:location) unless loc.blank?
    end

    # tags
    if params[:tag]=="variation:high"
      measure.peak=1
    elsif params[:tag]=="exposure:high"
      measure.longexposure=1
    else
      unless params[:tag].blank?
        user.tag(measure, :with=>params[:tag], :on=>:tags)
      end
    end

    # ozone
    #measure.ozone=params[:ozone].to_i unless params[:ozone].blank?
    measure.save
  end

  # LOUDNESS INDEXATION
  def self.by_range(measures)
    ranges=[]
    SoundLevel::STEP_DB.times {|i|
      measures_in_range=measures.find(:all, :conditions => "loudness_index=#{i} and measures.geom is not null")
      ranges<<{:label=>SoundLevel::DISTRIBUTION_LABELS[i], :measures=>measures_in_range}
    }
    return ranges
  end

  # LOCAL TIME INDEXATION
  def self.compute_time_index(measure)
    case measure.created_at.localtime.hour #WHY? localtime?
      when 0..5 then
        return 0
      when 6..11 then
        return 1
      when 12..17 then
        return 2
      when 18..21 then
        return 3
      when 22..23 then
        return 0
    end
  end  
  
  
  private
  
  # Geoprahical bounding box #
  def self.geoboxCond(paramvalue)
      box=paramvalue.split(",")
      if (box.size==4)
        # condition
        box_left=box[0]
        box_bottom=box[1]
        box_right=box[2]
        box_top=box[3]
        #return "measures.lat<#{box_top} and measures.lat>#{box_bottom} and measures.lng<#{box_right} and measures.lng>#{box_left}"
        return "measures.geom && SetSRID('BOX3D(#{box_left} #{box_bottom}, #{box_right} #{box_top})'::box3d,4326) "
      else
        raise SearchException.new("parameter geo/box #{paramvalue} not valid")
      end    
  end

end
