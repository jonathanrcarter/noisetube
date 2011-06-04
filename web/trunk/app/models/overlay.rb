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
 
require 'RMagick'  

class Overlay < ActiveRecord::Base

  include GeoRuby::SimpleFeatures
  
  acts_as_geom :geom
  
  belongs_to :track
    
  def self.build_image(measures, image_path, box=nil)
    
    #puts "BUILDING IMAGE---- #{image_path}"
        
    #logger.info "#{se.lat} #{nw.lat},#{sw.lat}, #{ne.lat} #{measures.size} #{measures[0].track_id} #{measures.size}"
    
    # #Overlay.new()
    geom=track.to_geobox
    sw=geom[0]
    nw=geom[1]
    se=geom[3]
    height=sw.ellipsoidal_distance(nw)*0.5  
    width=sw.ellipsoidal_distance(se)*0.5 
    
    #puts("Size calculted")    
    
    if (height>0)
      # creae image
      map = Magick::Image.new(width,height) { self.background_color = 'transparent'}
      
      # draw
      measures.each { |m|
        unless (m.geom.nil?)
          x,y = latlon_to_screen(height,width, ne,sw, m.geom)
          draw_measure(map,x,y,m.loudness_index)
        end
      }
      map.write(image_path)
      map.destroy!
      
    
    end
  end
  
  def self.build(track, loudness_index)
#    d=ActiveRecord::Base.connection.execute("select extent(geom) as box from measures")    
     measures=track.measures.find(:all,:select=>"geom, loudness_index", :conditions=>"loudness_index=#{loudness_index} and measures.geom is not null")
     o=Overlay.new(:track=>track,:loudness_index=>loudness_index)     
     box=Measure.envelope(measures)
     unless (box.nil?)
       # save to get the id
       o.save 
       # compute image + polygon of the image
       o.url="/images/overlays/#{o.id}.png"
       o.geom=build_image(measures,"#{RAILS_ROOT}/public/images/overlays/#{o.id}.png", box)
       o.save  
     end
     
     # save
  end
  
  private
  def self.interpolate(lo_to,hi_to,lo_from,hi_from,current)
    lo_to + (current-lo_from)*(hi_to-lo_to)/(hi_from-lo_from)
  end

  def self.latlon_to_screen(height,width, sw,ne, point)
    [interpolate(0,width,sw.lng,ne.lng,point.lng), interpolate(0,height,ne.lat,sw.lat,point.lat)]
  end
  
  def self.draw_measure(image,x,y,loudness)
    #puts loudness
    gc = Magick::Draw.new
    gc.fill = Measure.color_index(loudness)
    gc.circle(x,y,x-5,y)
    gc.draw(image)
  end
end
