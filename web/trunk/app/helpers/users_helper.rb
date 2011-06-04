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
 
module UsersHelper
  
  include TagsHelper

  def duration(t)
    unless t.count.nil?
      d=Duration.new(Time.at(t.duration))
      format=""
      format+="%hh" unless d.hours==0
      format+=" %mm" unless d.minutes==0
      format+=" %ss" unless d.seconds==0
      d.format(format)
    else
      "no measure"
    end
  end
  def pagination_links_remote(paginator)
    page_options = {:window_size => 1}
    pagination_links_each(paginator, page_options) do |n|
      options = {
        :url => {:action => 'list', :params => params.merge({:page => n})},
        :update => 'table',
        :before => "Element.show('spinner')",
        :success => "Element.hide('spinner')"
      }
      html_options = {:href => url_for(:action => 'list', :params => params.merge({:page => n}))}
      link_to_remote(n.to_s, options, html_options)
    end
  end


  def sort_td_class_helper(param)
    result = 'class="sortup"' if params[:sort] == param
    result = 'class="sortdown"' if params[:sort] == param + "_reverse"
    return result
  end

  def sort_link_helper(text, param)
    key = param
    key += "_reverse" if params[:sort] == param
    options = {
      :url => {:action => 'list', :params => params.merge({:sort => key, :page => nil})},
      :update => 'table',
      :before => "Element.show('spinner')",
      :success => "Element.hide('spinner')"
    }
    html_options = {
      :title => "Sort by this field",
      :href => url_for(:action => 'list', :params => params.merge({:sort => key, :page => nil}))
    }
    link_to_remote(text, options, html_options)
  end

  
  # 
  # Use this to wrap view elements that the user can't access. !! Note: this is
  # an *interface*, not *security* feature !! You need to do all access control
  # at the controller level.
  # 
  # Example: <%= if_authorized?(:index,   User)  do link_to('List all users',
  # users_path) end %> | <%= if_authorized?(:edit,    @user) do link_to('Edit
  # this user', edit_user_path) end %> | <%= if_authorized?(:destroy, @user) do
  # link_to 'Destroy', @user, :confirm => 'Are you sure?', :method => :delete
  # end %>
  # 
  # 
  def if_authorized?(action, resource, &block)
    if authorized?(action, resource)
      yield action, resource
    end
  end

  # 
  # Link to user's page ('users/1')
  # 
  # By default, their login is used as link text and link title (tooltip)
  # 
  # Takes options
  # * :content_text => 'Content text in place of user.login', escaped with the
  #   standard h() function.
  # * :content_method => :user_instance_method_to_call_for_content_text
  # * :title_method => :user_instance_method_to_call_for_title_attribute
  # * as well as link_to()'s standard options
  # 
  # Examples:
  #   link_to_user @user
  #   # => <a href="/users/3" title="barmy">barmy</a>
  # 
  #   # if you've added a .name attribute:
  #  content_tag :span, :class => :vcard do
  #    (link_to_user user, :class => 'fn n', :title_method => :login, :content_method => :name) +
  #          ': ' + (content_tag :span, user.email, :class => 'email')
  #   end
  #   # => <span class="vcard"><a href="/users/3" title="barmy" class="fn n">Cyril Fotheringay-Phipps</a>: <span class="email">barmy@blandings.com</span></span>
  # 
  #   link_to_user @user, :content_text => 'Your user page'
  #   # => <a href="/users/3" title="barmy" class="nickname">Your user page</a>
  # 
  def link_to_user(user, options={})
    raise "Invalid user" unless user
    options.reverse_merge! :content_method => :login, :title_method => :login, :class => :nickname
    content_text      = options.delete(:content_text)
    content_text    ||= user.send(options.delete(:content_method))
    options[:title] ||= user.send(options.delete(:title_method))
    link_to h(content_text), user_path(user), options
  end

  # 
  # Link to login page using remote ip address as link content
  # 
  # The :title (and thus, tooltip) is set to the IP address
  # 
  # Examples:
  #   link_to_login_with_IP
  #   # => <a href="/login" title="169.69.69.69">169.69.69.69</a>
  # 
  #   link_to_login_with_IP :content_text => 'not signed in'
  #   # => <a href="/login" title="169.69.69.69">not signed in</a>
  # 
  def link_to_login_with_IP content_text=nil, options={}
    ip_addr           = request.remote_ip
    content_text    ||= ip_addr
    options.reverse_merge! :title => ip_addr
    if tag = options.delete(:tag)
      content_tag tag, h(content_text), options
    else
      link_to h(content_text), login_path, options
    end
  end

  # 
  # Link to the current user's page (using link_to_user) or to the login page
  # (using link_to_login_with_IP).
  # 
  def link_to_current_user(options={})
    if current_user
      link_to_user current_user, options
    else
      content_text = options.delete(:content_text) || 'not signed in'
      # kill ignored options from link_to_user
      [:content_method, :title_method].each{|opt| options.delete(opt)} 
      link_to_login_with_IP content_text, options
    end
  end

end
