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
 
require 'color'

module KML

  DEG2RAD=Math::PI/180
  RAD2DEG=180/Math::PI

  # generate a kmz from a set of points
  def self.generate_kmz_file(xml, output_filename)

    # check to see if the file exists already, and if it does, delete it.
    if File.file?(output_filename)
      File.delete(output_filename)
    end

    Zip::ZipFile.open( output_filename, Zip::ZipFile::CREATE) {|zipfile|
      zipfile.get_output_stream("doc.kml") {|file|
        file.puts xml
        file.close}
    }

    # set read permissions on the file
    File.chmod(0777, output_filename)
    return output_filename
  end

  def self.get_coordinate(lat, lon, y, x)
    lat1=DEG2RAD*lat
    lon1=DEG2RAD*lon

    angle=Math.atan2(y, x)
    d_rad=Math.sqrt(x**2 + y**2)/6378137.0

    lat_rad = Math::asin(Math::sin(lat1)* Math::cos(d_rad) + Math::cos(lat1)* Math::sin(d_rad)* Math::cos(angle))
    dlon_rad = Math::atan2(Math::sin(angle)* Math::sin(d_rad)* Math::cos(lat1), Math::cos(d_rad)- Math::sin(lat1)* Math::sin(lat_rad))
    lon_rad = ((lon1 + dlon_rad + Math::PI) % (2*Math::PI)) - Math::PI
    return [lon_rad*RAD2DEG, lat_rad*RAD2DEG]
  end

  
  def self.generate_kmlcircle(altitude, lon, lat, radius, nb_segments, simple=false)
      return (simple)?_generate_kmlcircle_simple(altitude, lon, lat, radius, nb_segments):
              _generate_kmlcircle_standard(altitude, lon, lat, radius, nb_segments)
  end

  private
  def _generate_kmlcircle_standard(altitude, lon, lat, radius, nb_segments)
    coord=""
    lat1=DEG2RAD*lat
    lon1=DEG2RAD*lon
    d_rad=radius/6378137.0
    for i in 0..nb_segments do
      radial=i*(2*Math::PI/nb_segments)
      lat_rad = Math::asin(Math::sin(lat1)* Math::cos(d_rad) + Math::cos(lat1)* Math::sin(d_rad)* Math::cos(radial))
      dlon_rad = Math::atan2(Math::sin(radial)* Math::sin(d_rad)* Math::cos(lat1), Math::cos(d_rad)- Math::sin(lat1)* Math::sin(lat_rad))
      lon_rad = ((lon1 + dlon_rad + Math::PI) % (2*Math::PI)) - Math::PI
      coord+="#{lon_rad*RAD2DEG},#{lat_rad*RAD2DEG},#{altitude} \n"
    end
    return coord
  end


  def _generate_kmlcircle_simple(altitude, lon, lat, radius, nb_segments)
    coord=""
    d_rad=radius/63781.370
    for i in 0..(nb_segments+1) do
      # #j=(i)%nb_segment
      radial=i*(2*Math::PI/nb_segments)
      lon_rad=lon+Math::cos(radial)*d_rad
      lat_rad=lat+Math::sin(radial)*d_rad
      coord+="#{lon_rad},#{lat_rad},#{altitude} \n"
    end
    return coord
  end
end