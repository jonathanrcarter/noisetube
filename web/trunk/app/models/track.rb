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
 
require 'geo_features.rb'
require 'automatic_tagging.rb'

class Track < ActiveRecord::Base

  acts_as_geom :geom

  belongs_to :user
  belongs_to :city
  has_many :measures, :dependent => :destroy
  has_many :tags, :through => :measures
  has_many :overlays

  validates_presence_of  :user
  # #validates_format_of :graph_image_url, :with => /http\:/ , :allow_nil=>true,
  # :allow_blank=>true

  attr_accessible :graph_image_url, :user, :city


  # Create a track from a xml
  def self.from_xml (xml_doc, user)
    # get the rootnode
    root = REXML::XPath.first( xml_doc, "//NoiseTube-Mobile-Session")

    # TODO if attribute "userKey" is present on the rootNode it's contents
    # should be matched with the API key of the current user

    # #make a new track and set attributes...
    track = Track.new(:user=>user)
    track.starts_at = DateTime.parse(root.attribute("startTime").value)
    track.client = root.attribute("client").value  #TODO do a lookup/insert in the clients table and set the client_id field instead of using this string field
    track.client_version = root.attribute("clientVersion").value
    if (!root.attribute("deviceBrand").nil? && root.attribute("deviceBrand").value != "" && !root.attribute("deviceModel").nil? && root.attribute("deviceModel").value != "")
      track.device = root.attribute("deviceBrand").value + " " + root.attribute("deviceModel").value + (((!root.attribute("deviceModelVersion").nil?) && root.attribute("deviceModelVersion").value != "") ? " v" + root.attribute("deviceModelVersion").value : "") + (((!root.attribute("devicePlatform").nil?) && root.attribute("devicePlatform").value != "") ? " (" + root.attribute("devicePlatform").value + (((!root.attribute("devicePlatformVersion").nil?) && root.attribute("devicePlatformVersion").value != "") ? " " + root.attribute("devicePlatformVersion").value : "") + ")" : "")
    else
      track.device = ("J2ME string: " + root.attribute("deviceJ2MEPlatform").value) if ((!root.attribute("deviceJ2MEPlatform").nil?) && root.attribute("deviceJ2MEPlatform").value != "" && root.attribute("deviceJ2MEPlatform").value != "j2me")
    end
    track.track_comment = "Uploaded through website"
    track.save

    # process all the measurements
    #   example: <measurement timeStamp="2009-05-14T19:14:50+02:00" loudness="21" location="" tags="car"/>    
    logger.info "----- New track by uploding data"
    REXML::XPath.each( xml_doc, "//measurement" ) { |measurement|
      params={}
      params[:time] = measurement.attribute("timeStamp").value #is being parsed in measure.save_request
      params[:db]  = measurement.attribute("loudness").value.to_f if !measurement.attribute("loudness").nil?
      params[:l]  = measurement.attribute("location").value if !measurement.attribute("location").nil?
      params[:tag]  = measurement.attribute("tags").value if !measurement.attribute("tags").nil?
      track.add_measurement(params)
    }

    return track
  end

  def add_measurement(param)
    Measure.save_request(param, self, self.user)
  end

  def process(email=false)

    puts "processing track #{self.id} (#{self.measures.count} measurements) of #{User.find(self.user_id).login}"
    logger.info "processing track #{self.id} (#{self.measures.count} measurements) of #{User.find(self.user_id).login}"

    return if self.process_clean #stop processing if process_clean returns true (track was invalid and is deleted)
    logger.info "cleaning done."

    self.process_base
    logger.info "base done."

    self.process_geo_correction if self.geolocated
    logger.info "geo correction done."

    self.process_automatic_tagging
    logger.info "automatic tagging done."

    self.process_indexing
    logger.info "indexation done."

    self.processed = true
    self.public = self.user.public
    self.save

    # update last activity attribute
    self.user.last_activity_at=self.ends_at
    self.user.save

    self.city.last_activity_at=self.ends_at
    self.city.save
    
    UserMailer.deliver_new_track(self) if email
    logger.info "User mailed"

    logger.info "track #{self.id} processed"
  end

  def process_geo_correction
    GeoFeatures::correct_track(self)
  end

  def process_clean
    self.measures.each { |m| m.destroy unless m.valid? }
    if (self.measures.size < 3)
      logger.warn "Not enough (valid) measures => deleting track #{self.id}"
      self.destroy
      return true
    end
    return false
  end

  def process_indexing
    self.measures.each { |m|
      m.loudness_index=SoundLevel.compute_loudness_index(m.loudness)
      m.time_index=Measure.compute_time_index(m)
      m.save
    }
  end

  def process_base

    # set starts_at to timestamp of first measurement
    self.starts_at = self.measures.find(:first, :order=>"made_at").made_at
    
    # set ends_at to timestamp of last measurement
    self.ends_at = self.measures.find(:last, :order=>"made_at").made_at

    # number of measurements
    self.count = self.measures.size

    # duration
    self.duration = self.ends_at - self.starts_at

    # avg loudness
    loudness_data=self.measures.collect{ |m| m.loudness }
    self.avgloudness=SoundLevel::compute_decibel(loudness_data)
    min = 1000000.0
    max = 0.0
    loudness_data.each { |leq|
      min = leq if leq < min
      max = leq if leq > max
    }
    self.minloudness = min
    self.maxloudness = max

    # graph computing
    self.graph_image_url=self.to_graph_url

    # location computing
    geodata = []
    self.measures.each { |m| geodata << m.geom unless m.geom.nil? }
    
    unless (geodata.size < 2)
      puts "Track is geolocated"
      self.geolocated = true

      # bounding box
      self.geom=GeoFeatures::to_box(geodata)

      # coverage
      self.distance=GeoFeatures::coverage(geodata)

      # compute image overlay
      # Overlay.build_image(self.measures, "#{RAILS_ROOT}/public/images/tracks/#{self.id}.png")
      # #self.image_relative_url="/images/tracks/#{self.id}.png"
    else
      puts "Track is not geolocated"
      self.geolocated = false #just to be sure (and correct wrongly labeled tracks due to previous buggy postprocessing)
    end

    find_location # find associated city geographically

    self.save #!!!
  end

  def process_automatic_tagging

    logger.info "Automatic tagging of track #{self.id}  (#{self.measures.count} measurements)"

    Measure.update_all("tagged = NULL", "track_id=#{self.id}")

    last=self.measures.find(:last, :order=>"made_at")
    unless last.nil?

      # machine interpreter
      Interpreter.interpreters.each{ |interpreterclass|
        interpreter=interpreterclass.new
        interpreter.process(self)
      }
      # mass update
      Measure.update_all("tagged = true", "track_id=#{self.id}")
    end

  end

  def tags_count(context="tags")
    unless (context.nil?)
      context_sql= "and context='#{context}'"
    end
    Tagging.count(:joins=>"LEFT OUTER JOIN measures ON measures.id = taggings.taggable_id", :conditions=> "measures.track_id=#{self.id} #{context_sql}")
  end

  def tags(limit=100, context="tags")
    Measure.tag_counts_on(context, {:conditions=> "measures.track_id=#{self.id}", :order=>"count desc", :limit=>limit})
  end


  def to_graph_url(options={})

    unless self.graph_image_url.nil?
      return self.graph_image_url
    else
      # no graph url => compute it
      measures = (self.measures.size>2000) ? self.measures[0..1999] : self.measures
      graph = SoundLevel::graph(measures, options)

      # save only if the track has been processed
      if self.processed
        self.graph_image_url=graph
        self.save
      end

      return graph
    end
  end

  def find_location

    measurement=self.measures.find(:first, :conditions=>"geom is not null", :order=>"made_at")
    if measurement.nil?
      self.city=user.city if self.city.nil?
      self.location=nil
    else
      begin
        res = Geokit::Geocoders::GoogleGeocoder.reverse_geocode "#{measurement.geom.lat},#{measurement.geom.lng}"
        unless (res.nil?)
          self.location=res.full_address if (res.street_address.nil?)
          unless (res.city.nil?)
            self.city=City.find_or_build(res.city.downcase, res.country_code)
          else
            self.city=user.city
          end
        end
      rescue
        self.city=user.city
      end
    end
  end

end