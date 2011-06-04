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
 
require 'digest/sha1'

class User < ActiveRecord::Base

  acts_as_tagger

  after_save :emailer_callback

  # photo image
  acts_as_fleximage do
    image_directory 'public/images/photos'
    default_image_path  'public/images/default_user.png'
    preprocess_image do |image|
      image.resize '100x100'
    end
    require_image false
  end

  include Authentication
  include Authentication::ByPassword
  include Authentication::ByCookieToken

  has_many :measures, :dependent => :destroy
  has_many :tracks, :dependent => :destroy
  belongs_to :city

  validates_presence_of     :login
  validates_length_of       :login, :within => 3..40
  validates_uniqueness_of   :login
  validates_format_of       :login, :with => Authentication.login_regex, :message => Authentication.bad_login_message

  # validates_format_of       :name,     :with => Authentication.name_regex,
  # :message => Authentication.bad_name_message, :allow_nil => true
  # validates_length_of       :name,     :maximum => 100

  validates_presence_of     :email
  validates_length_of       :email, :within => 6..100 #r@a.wk
  validates_uniqueness_of   :email
  validates_format_of       :email, :with => Authentication.email_regex, :message => Authentication.bad_email_message
  validates_presence_of :city
  # #validates_presence_of     :location
  validates_presence_of     :phonemodel

  # #before_validation_on_create :validate_location

  def valid_location?
    return false  if self.location.nil?
    return false unless self.location.match('(.*),(.*)')    
    return true
  end

  def validate

    errors.add(:location, "no location") if self.location.nil?

    unless self.location.match('(.*),(.*)')
      errors.add(:location, "badly formatted (city, country)")
    end

    unless City.exist?(self.location)
      errors.add(:location, "not found")
    end
  end

  attr_accessible :login,
                  :email,
                  :password,
                  :password_confirmation,
                  :comment,
                  :location,
                  :public,
                  :image_file,
                  :phonemodel,
                  :last_activity_at

  
  def twitter_update(msg)
    if (self.twitter_account.nil?)
      httpauth = Twitter::HTTPAuth.new(self.twitter_account, self.twitter_password)
      base = Twitter::Base.new(httpauth)
      base.update(msg)
    end
  end


  # Authenticates a user by their login name and unencrypted password.  Returns
  # the user or nil.
  #
  # uff.  this is really an authorization, not authentication routine. We really
  # need a Dispatch Chain here or something. This will also let us return a
  # human error message.
  #
  def self.authenticate(login, password)
    return nil if login.blank? || password.blank?
    u = find_by_login(login.downcase) # need to get the salt
    u && u.authenticated?(password) ? u : nil
  end

  def login=(value)
    write_attribute :login, (value ? value.downcase : nil)
  end

  def email=(value)
    write_attribute :email, (value ? value.downcase : nil)
  end

  def make_password_reset_code
    self.password_reset_code = Digest::SHA1.hexdigest( Time.now.to_s.split(//).sort_by {rand}.join )
  end

  def forgot_password
    @forgotten_password = true
    self.make_password_reset_code
  end

  def reset_password
    # First update the password_reset_code before setting the reset_password
    # flag to avoid duplicate email notifications.
    update_attributes(:password_reset_code => nil)
    @reset_password = true
  end

  # #used in user_observer
  def recently_forgot_password?
    @forgotten_password
  end

  def recently_reset_password?
    @reset_password
  end

  def emailer_callback
    UserMailer.deliver_forgot_password(self) if self.recently_forgot_password?
    UserMailer.deliver_reset_password(self) if self.recently_reset_password?
  end

end
