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
 
class Urban < ActiveRecord::Base
  set_inheritance_column :ruby_type
  set_table_name "roads"
  has_many :measures, :through=> :segment
  has_many :segment
   
  #TODO incremental measures
  
  def self.associating_urban_element(measures)
    accuracy=0.0002 # radius
    
    sql="update measures set corrected=subquery2.corrected_geom, urban_id=seg_id from(SELECT DISTINCT ON (pt_id) pt_id, seg_id, 
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
     s.id AS seg_id,  
     s.geom AS ln_geom, 
     m.geom AS pt_geom, 
     r.name AS ln_name,
     ST_Distance(s.geom, m.geom)  as d 
   FROM 
     measures m
     LEFT JOIN tracks t on m.track_id=t.id
     inner JOIN segments s on ST_DWithin(m.geom, s.geom, #{accuracy})     
     LEFT JOIN roads r on s.urban_id=r.id
   WHERE 
     m.geom is not null and loudness>0 and m.id in (#{measures.collect{|i| i.id}.join(",")})
     order by pt_id, d 
   ) As subquery
   ) As subquery2"
    
  end
  # aggregate all the measures of this urban element
  def self.aggregate_measure(el) 
    if (el.measures.size==0)
      return 0
    else 
      return Measure.aggregate_loudness(el.measures) 
    end   
  end
  
end