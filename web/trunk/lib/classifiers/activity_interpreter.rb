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

class SensorTypeInterpreter<Interpreter

  CALIBRATED_SENSORS=["nokia n96", "sony ericsson w995", "nokia n95", "nokia n85", "nokia e65", "nokia 5800", "nokia 5230"]

  def initialize
    super
    @device=nil
    @tags_type=[
            "user_sensor",
            "user_calibrated"]
  end

  def interpret(measure)
    tags=[]
    tags<<"sensor:#{@device}"
    tags<<"calibrated:#{@calibrated}"
    #tags<<"name:#{@name}"
    tag(measure, tags, "user")
  end

  def _process(track)

    @device=track.device || "unknown"
    @user=track.user.login

    @device=@device.gsub(/(-|_|(v\.)).*/, "") unless @device.downcase[/nokia/].nil?
    
    @device="sony ericsson w995" unless @device.downcase[/sony ericsson w995/].nil?
    @device="sony ericsson u1i" unless @device.downcase[/sony ericsson u1i/].nil?
    @device="samsung 910" unless @device.downcase[/samsung 910/].nil?
    @device="unknown" unless @device.downcase[/unknown/].nil?
    
    @device=@device.downcase.strip
    puts "device track #{track.device} => #{@device}"

    @calibrated=CALIBRATED_SENSORS.include?(@device) || "no"
    self.class.superclass.instance_method("_process").bind(self).call(track)
  end

end

class ActivityInterpreter<Interpreter

  def initialize
    super
    @speed=nil
    @tags_type=[
            "user_mobility"
    ]
  end

  def compute_speed(measure, last_measure)
    distance=measure.geom.ellipsoidal_distance(last_measure.geom)
    time = measure.made_at - last_measure.made_at
    @speed = (distance*3600)/(time*1000)
    return @speed
  end

  def _process(track)
    last_measure=nil
    idx=0
    first=true
    track.measures.find(:all, :order=>"made_at").each{ |measure|

      if measure.geom.nil?
        # reset the counter
        idx=0
        @speed=nil
        last_measure=nil
        first=true
      else

        # computing speed every 10 measurements
        if (!last_measure.nil? && (idx>10 || first))
          compute_speed(measure, last_measure)
          idx=0
          first=false
        end

        idx+=1
        last_measure=measure
      end
      unless (@speed.nil?)
        tags=[]
        if (@speed<1)
          tags<<"mobility:stationary"
        elsif (@speed<10)
          tags<<"mobility:walking"
        else
          tags<<"mobility:using transport"
        end
        tag(measure, tags, "user")
      end

    }
  end

end