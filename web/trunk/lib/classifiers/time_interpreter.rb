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
 
require 'classifiers/interpreter.rb'

class TimeInterpreter<Interpreter

  def initialize
    super
    @tags_type=["time_day",
          "time_week",
          "time_season"]
  end
  
  def interpret(measure)

    tags=[]
    # #d=measure.made_at #d2=d.localtime #puts " #{d} #{d2}  #{d.hour}
    # #{d2.hour}"
    date=measure.made_at.localtime #???

    case date.hour
      when 0..5 then tags<<"day:night"
      when 6..11 then tags<<"day:morning"
      when 12..17 then tags<<"day:afternoon"
      when 18..21 then tags<<"day:evening"
      when 22..23 then tags<<"day:night"
    end

    case date.wday
      when 1..5 then tags<<"week:workingdays"
      when 6 then tags<<"week:weekend"
      when 0 then tags<<"week:weekend"
    end

    # south hemisphere
    geom=measure.geom
    if (geom.nil?)
      lat=measure.user.city.lat unless measure.user.city.nil?
    else
      lat=geom.lat
    end
    
    if (!lat.nil? && lat>0)
      # north hemisphere
      case date.month
      when 0..2 then tags<<"season:winter"
      when 2..4 then tags<<"season:spring"
      when 5..7 then tags<<"season:summer"
      when 8..10 then tags<<"season:autumn"
      when 11 then tags<<"season:winter"
      end
    else
      # south hemisphere
      case date.month
      when 0..2 then tags<<"season:summer"
      when 2..4 then tags<<"season:autumn"
      when 5..7 then tags<<"season:winter"
      when 8..10 then tags<<"season:spring"
      when 11 then tags<<"season:summer"
      end
    end

    
    tag(measure,tags,"time")

  end

end