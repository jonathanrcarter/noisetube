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
require 'classifiers/time_interpreter.rb'
require 'classifiers/location_interpreter.rb'
require 'classifiers/loudness_interpreter.rb'
require 'classifiers/weather_interpreter.rb'
require 'classifiers/personal_interpreter.rb'
require 'classifiers/activity_interpreter.rb'

Interpreter.interpreters=[

# time interpreter
TimeInterpreter,

# Location interpreter
LocationInterpreter,

# #user activity interpreter
ActivityInterpreter,

# #Environmental-related interpreters
#WeatherInterpreter, # TODO has a bug (17/09/2010)

# Loudness interpreter
LoudnessInterpreter,

SensorTypeInterpreter,

PersonalInterpreter
]