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
 
require "rexml/document"
require "will_paginate"

class UsersController < ApplicationController
  
  # Be sure to include AuthenticationSystem in Application Controller instead
  include AuthenticatedSystem

 # caches_page :index, :show

  before_filter :find_user, :except => [:new, :index, :create, :realtime, :reset_password, :forgot_password, :change_password]
  before_filter :authenticate_user, :only => [:edit, :deltrack, :update, :uploaddata, :uploadxml, :change_password]

  # Generate a map with only the recently updated sensors "for" parameter
  # defining the window (in minutes)
  def realtime
   
    respond_to do |format|
         
      # Google Earth
      format.kml{
        
        @baseurl= url_for :controller => 'users' 
        @rooturl= root_url
        
        # generate the kml file with a "network link" to update the
        if (params[:update].nil?)
          render :template => "users/realtime.kml.erb", :mimetype => :kml
        
          # generate the network link updating every 5 seconds
        else
          since=Time.now-60*60 # default 10 minutes
          since=Time.now-params[:for].to_i*60 unless params[:for].blank?

          @users=[]
          @tagged_measures=[]
          
          # for each public user
          User.find(:all, :conditions=>"public=true").each { |user|
            exposure=user.measures.find(:last, :order=>"created_at")

            unless exposure.nil?
              tagging=Tagging.find(:last , :conditions=>"tagger_id=#{user.id} and created_at>'#{since.to_formatted_s(:db)}'", :order=>"created_at")
              #for Open lab demo
              if (exposure.user_id==4)
                  lat=48.8435
                  lng=2.347
              else
                if exposure.geom.nil?
                localized_measure=user.measures.find(:last, :conditions=>" geom is not null", :order=>"made_at")
                if (localized_measure.nil?)
                  # by default we take the lat/lng of the user's city
                  unless (user.city.nil?)
                    lat=user.city.lat
                    lng=user.city.lng
                  end
                else
                  lat=localized_measure.geom.lat
                  lng=localized_measure.geom.lng
                 end
              else
                  lat=exposure.geom.lat
                  lng=exposure.geom.lng
              end
              end
              @users << {:user=>user, :m=>exposure, :lat=>lat, :lng=>lng, :age=>exposure, :tagging=>tagging} unless (lat.nil?)
            end 
          }
          
          # for the semantic layer
          tags=Tag.find(:all).collect{|tag| tag.name} 
          @tagged_measures=Measure.tagged_with(tags,:on=>:tags, :conditions=>["geom is not null and measures.made_at > ?",since])

          # TODO badly requested
          render :template => "users/realtime_update.kml.erb", :mimetype => :kml
          
        end
      }
    end
  end
  
  def map
    
    # since
    for_=15
    for_=params[:for].to_i unless (params[:for].nil?)
    since=for_.days.ago
      
    respond_to do |format|

      # image
      format.png {
        require 'gchart'
        points=@user.measures.find(:all, :select=>"loudness", :conditions => ["made_at > ?", since], :limit=>2000)
        points.map!{|m| m.loudness.to_i}
        image=Gchart.line(:data => points, :size => '500x100', :line_colors => '0077CC', :max_value => 100)
        return redirect_to(image.to_s)
      }

      # map
      format.kml  {
        @points=@user.measures.find(:all, :conditions => ["made_at > ? and lat!=0 and lng!=0", since])
        # #TODO improve call
        render :template => "users/map.kml.erb", :mimetype => :kml
        # xml=generate_map_with_circle(points)
        # send_data(xml.target!,:type=>"application/vnd.google-earth.kml+xml")
      }
    end
  end
  

  # render new.rhtml
  def new
    @user = User.new
    location=get_location_by_ip
    @user[:location]="#{location[:city]}, #{location[:country_name]}" unless location.nil?
  end

  def create
    logout_keeping_session!

    user_hash=params[:user]

    unless user_hash.nil?
      @user = User.new(user_hash)
      if user_hash[:password_confirmation] != user_hash[:password]
        @user.errors.add_to_base("Password and password confirmation do not match!")
        render :action => 'new'
        return
      end
      if verify_recaptcha
        @user.public=true #default
        city=City.build_city(user_hash[:location])
        @user.city=city
        if @user.save
          # Protects against session fixation attacks, causes request forgery
          # protection if visitor resubmits an earlier form using back button.
          # Uncomment if you understand the tradeoffs. reset session
          self.current_user = @user # !! now logged in
          flash[:notice] = "Your are now registred! We suggest to put a picture of you.<br/>You can now download the mobile application"

          #expires_page(:controller => "/users", :action => "index") #TODO this doesn't work (causes crash), fix it
          redirect_to "/download"
        else
          render :action => 'new'
        end
      else
        @user.errors.add_to_base("ReCAPTCHA is not correct, try again please.")
        render :action => 'new'
      end
    end

  end
 
  
  def show
    respond_to do |format|

      # only public track
      clause="public=true" unless @user==current_user
      
      format.html {
        @summary={}
        @summary[:last_activity]=@user.measures.find(:last).made_at unless @user.measures.find(:last).nil?
        @summary[:measures_size]=@user.measures.count
        @summary[:tags_assignement_size]=@user.owned_taggings.count
        @tags=Measure.tag_counts_on("tags", {:conditions=>"tagger_id=#{@user.id} and taggings.context='tags'", :order=>"tags.name"})

        @tags_time=Measure.tag_counts_on("time",{:conditions=>"tagger_id=#{@user.id}", :order=>"tags.name"})
        @tags_location=Measure.tag_counts_on("location",{:conditions=>"tagger_id=#{@user.id}", :order=>"tags.name"})
        @tags_loudness=Measure.tag_counts_on("loudness",{:conditions=>"tagger_id=#{@user.id}", :order=>"tags.name"})
        @tracks=@user.tracks.paginate(:all,:conditions=>clause, :per_page => 10, :order=>"tracks.created_at desc", :page => params[:page])
        # #@onload="initialize();" #@onunload="GUnload();"
      }
      
      format.rss{         
        @tracks=@user.tracks.find(:all,:conditions=>clause, :order=>"tracks.created_at desc", :limit=>10)
        render :layout=>false,:mimetype =>:rss
      }
    
      format.js {
        render :update do |page|
          p params
          @tracks=@user.tracks.paginate(:all,:conditions=>clause, :per_page => 10, :order=>"tracks.created_at desc", :page => params[:page])
          page.replace_html 'results', :partial => 'track_list'
        end
      }
      
      format.jpg{
        render :type => :flexi
      }
    end
  end
  
  def index
    users=User.find(:all,:select=>"distinct users.*", :joins=>:tracks, :conditions=>"tracks.processed=true", :order=>"users.updated_at desc", :limit=>10)
    @users=[]
    users.each {|user|
      tags=Measure.tag_counts({:conditions=>"tagger_id=#{user.id} and taggings.context='tags'", :order=>"count desc" , :limit=>"10"})
      @users<<{:user=>user , :tags=>tags} 
    } 
  end
  
  ### UPLOAD DATA 

  def uploaddata #view page
    #work just like that
  end
  
  def uploadxml #process the data
    begin
      # get the uploaded file and read it...
      uploaded_file = params[:xml_file]
      xml_data = uploaded_file.read if uploaded_file.respond_to? :read

      # do we have data from the file?
      unless request.post? and xml_data
        redirect_to root_url
        return
      end

      # append session closing tag if needed:
      closing_tag = "</NoiseTube-Mobile-Session>"
      xml_data += closing_tag if xml_data.rindex(closing_tag).nil?

      # parse the data using REXML...
      xml_doc = REXML::Document.new(xml_data)
      track=Track.from_xml(xml_doc, @user)
      #track.process (handled by separate process)

      # redirect
      flash[:upload_notice] = "A track with a total of #{track.measures.size} measures was added. Thanks for your contribution!"
      redirect_to(@user)
    rescue Exception => exc
       logger.error("Error processing uploaded track (XML file): #{exc.message}")
       flash[:error] = "Upload failed, please mail the file to support@noisetube.net"
       render :action => 'uploaddata'
    end
  end

  
  ################ ACCOUNT INFORMATION ###################

  def edit
    @user
  end
  
  def deltrack
      track = @user.tracks.find(params[:id])
      track.destroy
  end
  
  def update  
    if @user.update_attributes(params[:user])  
      flash[:notice] ="Profile saved"  
      expire_photo(@user)
      redirect_to(@user)
    else
      flash[:error] ="Error"
      render :action => 'edit'
    end
  end
 
  # ################## PASSWORD STUFF #######################

  def change_password
    return unless request.post?
    if User.authenticate(current_user.login, params[:old_password])
      if ((params[:password] == params[:password_confirmation]) && 
            !params[:password_confirmation].blank?)
        current_user.password_confirmation = params[:password_confirmation]
        current_user.password = params[:password]

        if current_user.save
          flash[:notice] = "Password successfully updated" 
          redirect_to profile_url(current_user.login)
        else
          flash[:alert] = "Password not changed" 
        end
      else
        flash[:alert] = "New Password mismatch" 
        @old_password = params[:old_password]
      end
    else
      flash[:alert] = "Old password incorrect" 
    end
  end

  def forgot_password
    return unless request.post?
    if @user = User.find_by_email(params[:user][:email])
      @user.forgot_password
      success = @user && @user.save!
      if success
        redirect_to login_url  
        flash[:notice] = "A password reset link has been sent to your email address" 
      else 
        redirect_to login_url  
        flash[:error] = "hmm, something's wrong..." 
      end 
    else
      redirect_to login_url
      flash[:error] = "Could not find a user with that email address" 
    end
  end
  
  def reset_password
    @user = User.find_by_password_reset_code(params[:id])
    return if @user unless params[:user]
    if ((params[:user][:password] && params[:user][:password_confirmation]) && !params[:user][:password_confirmation].blank?)
      self.current_user = @user #for the next two lines to work
      current_user.password_confirmation = params[:user][:password_confirmation]
      current_user.password = params[:user][:password]
      @user.reset_password
      redirect_back_or_default('/')
    else
      flash[:alert] = "Password mismatch" 
    end  
  end
  

  private

  def authenticate_user
    redirect_to root_url unless logged_in? && @user==current_user
  end  
  
  def find_user
    if params[:id] =~ /^\d*$/
      @user = User.find(params[:id])
    else
      unless @user = User.find_by_login(params[:id]) 
        raise ActiveRecord::RecordNotFound
      end
    end
  end
  
  def tracks
    respond_to do |format|
      format.json  {
        json=@user.tracks.to_json
        render :json=>"data="+json          
      }
    end
  end
  
  def expire_photo(user)
    expire_page user_path(user, :jpg)
  end

end
