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
 
require 'builder'
require 'gchart'

class SearchException < Exception;
end

class ApiController < ApplicationController

  before_filter :verify_apikey, :only => [:newsession, :update, :upload, :postlog, :endsession, :resumesession, :endallsessions, :search]

  def index
    redirect_to '/api_overview'
  end
  
  def mobilecalibrations
    send_file "#{RAILS_ROOT}/public/calibrations.xml", :type=>"application/xml"
  end

  # Mobile Authentication
  def authenticate
    user = User.find_by_login(params[:login])
    if user.nil? || !user.authenticated?(params[:password])
      render :json => "error"
    else
      render :json=> user.salt
    end
  end
  
  # Ping tool
  def ping
    render :text => "ok", :status=>200
  end


  # Start a new measurement session #
  def newsession
    # in case we haven't closed the previous session
    # close_session  # closes the most recent running session
    
    # create the new track
    track = start_session
    render :text => "ok #{track.id}", :status=>200
  end
  

  # close a measurement session  #
  def endsession
    track = running_track(false) #do not start a new one
    if close_session(track)
      render :text => "ok", :status=>200
    else
      render :nothing => true, :status=>404 #specified track did no exist, or it was already closed, or there is no running track
    end
  end

  def endallsessions
    #TODO
  end

  def resumesession
    track = running_track(false) #do not start a new one
    unless track.nil?
      render :text => "ok #{track.id}", :status=>200
    else
      render :nothing => true, :status=>404 #specified track did no exist of there is no running track
    end
  end

  # ## Update the sensor request: http://noisetube.net/api/update?time=(timestamp)&db=(decibel)&l={geo:(lat,lon)|placeid}
  # Parameters:
  #   time={time}: the time of the measure (in second) (required)
  #   db=: decibel in db(A) integer (required)
  #   l: geo:{lat,lng}|placeID
  #   placeID= the id of a place e.g. one of your favoraite location (home, office) , the station of a subway
  #   geo coordinates: l=geo:lat,lng
  #   result: response 200 : ok response 500 : ##
  def update
    begin
      track = running_track(true) #start a new one if needed
      track.add_measurement(params)
      #render :nothing=> true
      render :text => "ok", :status=>200
    rescue Exception
      render :text=>"#{$!.message}"
    end
  end


  # Update the sensor request: http://noisetube.net/api/upload # batch version (through JSON format)
  def upload
    track = running_track(true) #start a new one if needed
    params[:measures].each { |m_params|
      track.add_measurement({
                :time=>m_params[0],
                :db=>m_params[1],
                :l=>m_params[2],
                :tag=>m_params[3]})
    }
    render :text => "ok", :status=>200
  end

  def postlog
    directory = "log/posted"
    # create the file path
    path = File.join(directory, "User#{@user.id}.log")
    # write the file
    File.open(path, 'a') { |f| f.write(params[:log]) }
    render :text => "ok", :status=>200
  end

  # search measures
  def search
    begin
      measures = Measure.search(params, [:user]).collect { |measure|
        {:lat=>measure.lat,
         :lng=>measure.lng,
         :made_at=>measure.made_at,
         :loudness=>measure.loudness,
         :tags=>measure.tags,
         :user=>measure.user.id}
      }

      # send
      render :json=>measures.to_json

    rescue SearchException
      render :text=>"#{$!.message}"
    end
  end


  # ## Tag distribution ##
  def tagdist

    # querying
    labels, freq=Api.distribution_tags(params)

    respond_to do |format|
      format.png{
        graph=@template.generate_tags_distribution_graph(labels,freq)
        redirect_to graph
      }

      format.json{
        render :json=>[labels, freq].to_json
      }
    end
  end

  def leqdist
    dist=Api.distribution_leq(params)
    
    respond_to do |format|
      # JSON RESPONSE
      format.json{
        dist_label={}
        dist.each_with_index{ |e, i|  dist_label[Measure::DISTRIBUTION_LABEL[i]]=e}
        render :json=>dist_label.to_json
      }

      # PNG RESPONSE
      format.png{
        graph=@template.generate_leq_distribution_graph(dist,params)
        redirect_to graph
      }
    end
  end

  private #!!!

  # verify the API Key
  def verify_apikey
    @user=User.find_by_salt(params[:key])
    if (@user.nil?)
      render(:text=> "API key not found")
    end
  end

  def most_recent_running_track
    return @user.tracks.find(:last, :conditions=>"ends_at IS NULL", :order=>"starts_at asc")
  end

  # get specified or most recent running track (or a new one if allowed)
  def running_track(start_new_if_none=false)
    track = @user.tracks.find_by_id(params[:track]) unless params[:track].blank? #try to find track by id (if id was specified)
    track = most_recent_running_track if (track.nil? or not track.ends_at.nil?) #no specified track (or it does not exist, or is already closed) --> try most recent running track
    track = start_session if (track.nil? and start_new_if_none) #or start a new one if permitted
    return track
  end

  def close_session(track=most_recent_running_track)
    unless track.nil?
      last_measurement = track.measures.find(:last, :order=>"made_at asc")
      unless last_measurement.nil?
        track.ends_at = last_measurement.made_at
        track.save
      else
        track.destroy  #no last measurement --> no measurements at all --> delete empty track
      end
      return true
    else
      return false
    end
  end

  # create a new session for a given user
  def start_session
    start_time = Time.now #default
    start_time = DateTime.parse(params[:starts_at]) unless params[:starts_at].blank?
    track = Track.create(:user=>@user)
    track.starts_at = start_time #will be overwritten with timestamp (made_at) of first measurement during postprocessing
    track.client = params[:client]
    track.client_version = params[:clientversion]
    track.device = "#{params[:devicebrand]} #{params[:devicemodel]}"
    # TODO...
    track.save
    return track
  end

end
