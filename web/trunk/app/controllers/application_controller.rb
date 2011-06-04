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
 
# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base

  helper :all # include all helpers, all the time

  protect_from_forgery # See ActionController::RequestForgeryProtection for details

  rescue_from ActiveRecord::RecordNotFound, :with => :show_errors

  include AuthenticatedSystem

  before_filter :set_locale


  def show_errors
      render :partial => 'shared/record_not_found', :layout => 'application', :status => 404
  end

  def set_locale
    locale = params[:locale] || 'en'
    I18n.locale = locale
  end

  def get_browser_locale
    preferred_locale = nil
    if browser_locale = request.headers['HTTP_ACCEPT_LANGUAGE']
      preferred_locale = %w(en es fr de).
              select { |i| browser_locale.include?(i) }.
              collect { |i| [i, browser_locale.index(i)] }.
              sort { |a, b| a[1] <=> b[1] }.
              first
    end
    preferred_locale.try(:first) || 'en'
  end

  def get_location_by_ip
    ip=request.remote_ip
    if RAILS_ENV=="production"
      return G.look_up(ip)
    else
      return G.city(ip)
    end

  end
end
