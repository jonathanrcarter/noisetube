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
 
module GeoFeatures

  require 'net/http'
  require 'xmlsimple'
  require 'geokit'

  def self.coverage(geodata)
    sum=0
    last=nil
    geodata.each{ |geom|
      unless (geom.nil?)
        sum+=geom.spherical_distance(last) unless last.nil?
        last=geom
      end
    }
    return sum
  end
  
  def self.envelope(geodata)
    geodata.compact! #remove nil values from array
    return nil if (geodata.empty?)
    return GeometryCollection.from_geometries(geodata).bounding_box
   end

  def self.to_box(geodata,margin=0.001)
    box=self.envelope(geodata)
    return nil if (box.nil?)
    # 4 corners of the image
    ne,sw=box
    se=Point.from_lon_lat(ne.lng, sw.lat)
    nw=Point.from_lon_lat(sw.lng, ne.lat)

    # MARGIN for image if (height==0) puts "one point"
    ne.y+=margin
    ne.x+=margin
    sw.y-=margin
    sw.x-=margin
    return Polygon.from_points([[sw, se, ne, nw, sw]])
  end

  # segmentation of streets (urban element) in smaller lines
  def self.urban_element_segmentation
    # delete the segment
    puts "deleting all the segments"
    Segment.delete_all("true")

    # resegmentation
    roads=Urban.find(:all, :conditions=>"name is not null")
    size=roads.size
    roads.each_with_index{ |road, j|
      unless road.the_geom.nil?
        Segment.transaction do
          points=road.the_geom.points
          points.each_with_index{ |p, i|
            if i>0
              s=Segment.new(:geom=>LineString.from_points(points[(i-1)..i]), :urban=>road)
              s.save
            end
          }
          puts "segmenting road #{road.name} (#{j}/#{size}) with #{points.size-1} segments" if (j%100==0)
        end
      end
    }
  end

  def self.correct_track(track)
    sql="update measures set corrected=subquery2.corrected_geom, segment_id=subquery2.seg_id, corrected_distance= ST_DISTANCE(ST_transform(measures.geom,2163), ST_transform(subquery2.corrected_geom, 2163)) from(SELECT DISTINCT ON (pt_id) pt_id, seg_id,
   ln_name,
   pt_geom,
   ST_line_interpolate_point(
       ln_geom,
       ST_line_locate_point(ln_geom, pt_geom)
     )
   as corrected_geom

 FROM
   (
SELECT
    DIstinct on (pt_id)
     m.id AS pt_id,
     /*r.id AS ln_id,*/
     s.id AS seg_id,
     s.geom AS ln_geom,
     m.geom AS pt_geom,
     r.name AS ln_name,
     ST_Distance(s.geom, m.geom)  as d
   FROM
     measures m

     inner JOIN segments s on ST_DWithin(m.geom, s.geom, 0.0002)
     LEFT JOIN roads r on s.urban_id=r.id
   WHERE
     m.track_id=#{track.id} and
     m.geom is not null  and loudness>0 order by pt_id, d
   ) As subquery
   ) As subquery2    where measures.id = subquery2.pt_id;"

    r=ActiveRecord::Base.connection().execute(sql);
    return r.cmdtuples()

  end



=begin


  desc "linking geo measures to urban element (segments)"
  task(:urbanellink=>:environment) do
    time_start=Time.now
    sql="update measures set corrected=subquery2.corrected_geom, urban_id=subquery2.ln_id, corrected_distance= ST_DISTANCE(ST_transform(measures.geom,2163), ST_transform(subquery2.corrected_geom, 2163)) from(SELECT DISTINCT ON (pt_id) pt_id, ln_id,
   ln_name,
   pt_geom,
   ST_line_interpolate_point(
       ln_geom,
       ST_line_locate_point(ln_geom, pt_geom)
     )
   as corrected_geom

 FROM
   (
SELECT
    DIstinct on (pt_id)
     m.id AS pt_id,
     r.id AS ln_id,
     r.geom AS ln_geom,
     m.geom AS pt_geom,
     r.name AS ln_name,
     ST_Distance(r.geom, m.geom)  as d
   FROM
     measures m
     LEFT JOIN tracks t on m.track_id=t.id
     LEFT JOIN roads r on ST_DWithin(m.geom, r.geom, 0.0002)
   WHERE
     m.geom is not null and loudness>0 order by pt_id, d
   ) As subquery
   ) As subquery2    where measures.id = subquery2.pt_id;"


    ActiveRecord::Base.establish_connection
    ActiveRecord::Base.connection().execute(sql);
    time_end=Time.now
    puts "time elasped #{(time_end-time_start)} "

  end



  desc "linking geo measures to urban element (segments)"
  task(:urbanseglink=>:environment) do
    puts "remove previous corrected geom"
    Measure.update_all("corrected =null")
    puts "done."


    time_start=Time.now
    # #{}sql="update measures set corrected=subquery2.corrected_geom,
    # urban_id=subquery2.seg_id from(SELECT DISTINCT ON (pt_id) pt_id, seg_id,
    sql="update measures set corrected=subquery2.corrected_geom, segment_id=subquery2.seg_id, corrected_distance= ST_DISTANCE(ST_transform(measures.geom,2163), ST_transform(subquery2.corrected_geom, 2163)) from(SELECT DISTINCT ON (pt_id) pt_id, seg_id,
   ln_name,
   pt_geom,
   ST_line_interpolate_point(
       ln_geom,
       ST_line_locate_point(ln_geom, pt_geom)
     )
   as corrected_geom

 FROM
   (
SELECT
    DIstinct on (pt_id)
     m.id AS pt_id,
     /*r.id AS ln_id,*/
     s.id AS seg_id,
     s.geom AS ln_geom,
     m.geom AS pt_geom,
     r.name AS ln_name,
     ST_Distance(s.geom, m.geom)  as d
   FROM
     measures m
     LEFT JOIN tracks t on m.track_id=t.id
     inner JOIN segments s on ST_DWithin(m.geom, s.geom, 0.0002)
     LEFT JOIN roads r on s.urban_id=r.id
   WHERE
     m.geom is not null and loudness>0 order by pt_id, d
   ) As subquery
   ) As subquery2    where measures.id = subquery2.pt_id;"


    ActiveRecord::Base.establish_connection
    ActiveRecord::Base.connection().execute(sql);
    time_end=Time.now
    puts "time elasped #{(time_end-time_start)} "

  end

 def compute_urbanelement
    res=Geokit::Geocoders::GoogleGeocoder.reverse_geocode "#{self.lat},#{self.lng}"
    res.address
    self.urbanelement=Urbanelement.find_or_create_by_name
  end

=end



end
