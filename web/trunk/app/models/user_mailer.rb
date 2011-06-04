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
 
class UserMailer < ActionMailer::Base

  def new_track(track)
    setup_email
    user=track.user
    recipients user.email
    subject "Your NoiseTube track has been processed"
    body({
            :track_url => "#{root_url}users/#{user.id}/tracks/#{track.id}.kml",
            :user => user
    })
  end
  
  def update_city(track)
    setup_email
    
    emails=[]
    ref_user=track.user
    
    #track.city.users.each{|user|
    #  unless (user==ref_user)
    #    emails<<user.email
    #  end

    #} #TODO if the user accept email alert
    recipients emails
    
    subject "Your city has been updated"
    
    body({
            
            :ref_user_url => "#{root_url}users/#{ref_user.id}",
            :ref_user_name => ref_user.login,
            :track_url=> "#{root_url}users/#{ref_user.id}/tracks/#{track.id}.kml",
            :city_url => "#{root_url}cities/#{track.city.id}.kmz",
            :city => track.city}
    )    
  end

  def forgot_password(user)
    setup_email
    recipients "#{user.email}"
    subject "You have requested to change your password"
    body({:user => user,
          :url=>"#{root_url}reset_password/#{user.password_reset_code}"})
  end

  def reset_password(user)
    setup_email
    recipients "#{user.email}"
    body({:user => user})
    subject 'Your password has been reset.'
  end

  protected

  def setup_email
    sent_on Time.now
    content_type "text/html"
    from "NoiseTube Team" # Sets the User FROM Name and Email
    if RAILS_ENV=='development'
      default_url_options[:host]="localhost:3000"
    else
      default_url_options[:host]="noisetube.net"
    end
  end

end
