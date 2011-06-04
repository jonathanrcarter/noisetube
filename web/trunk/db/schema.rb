# This file is auto-generated from the current state of the database. Instead of editing this file, 
# please use the migrations feature of Active Record to incrementally modify your database, and
# then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your database schema. If you need
# to create the application database on another system, you should be using db:schema:load, not running
# all the migrations from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20100910173000) do

  create_table "associations", :id => false, :force => true do |t|
    t.column "measure_id", :integer
    t.column "urban_id", :integer
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "beliefs", :force => true do |t|
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "cities", :force => true do |t|
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "name", :string, :limit => 45, :null => false
    t.column "lat", :float
    t.column "lng", :float
    t.column "country", :string, :limit => 45, :null => false
    t.column "timezone", :string
    t.column "measures_size", :integer
    t.column "tags_size", :integer
    t.column "daily_graph", :text
    t.column "last_activity_at", :datetime
  end

  create_table "client", :force => true do |t|
    t.column "name", :string
    t.column "vendor", :string
  end

  create_table "datasets", :force => true do |t|
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "delayed_jobs", :force => true do |t|
    t.column "priority", :integer, :default => 0
    t.column "attempts", :integer, :default => 0
    t.column "handler", :text
    t.column "last_error", :text
    t.column "run_at", :datetime
    t.column "locked_at", :datetime
    t.column "failed_at", :datetime
    t.column "locked_by", :string
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "device_brand_aliases", :force => true do |t|
    t.column "device_brand_id", :integer
    t.column "alias", :string
  end

  create_table "device_brands", :force => true do |t|
    t.column "name", :string
  end

  create_table "device_models", :force => true do |t|
    t.column "device_brand_id", :integer
    t.column "name", :string
    t.column "code_name", :string
    t.column "commercial_name", :string
    t.column "specs_url", :string
  end

  create_table "districts", :primary_key => "gid", :force => true do |t|
    t.column "area", :float
    t.column "nsq", :integer, :limit => 8
    t.column "nsq_ca", :integer, :limit => 8
    t.column "tln", :string, :limit => 28
    t.column "ndep", :integer
    t.column "nar", :integer
    t.column "nqu", :integer
    t.column "geom", :geometry, :srid => nil
    t.column "population", :integer
  end

  add_index "districts", ["geom"], :name => "districts_geom_gist", :spatial=> true 

  create_table "exposures", :force => true do |t|
    t.column "avg", :float
    t.column "min", :float
    t.column "max", :float
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "image_url", :text
  end

  create_table "favlocations", :force => true do |t|
    t.column "name", :string
    t.column "lon", :float
    t.column "lat", :float
    t.column "user_id", :integer
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "followership", :force => true do |t|
    t.column "city_id", :integer
    t.column "follower_id", :integer
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "invitations", :force => true do |t|
    t.column "name", :string
    t.column "email", :string
    t.column "comment", :text
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "location", :string
    t.column "phone", :string
  end

  create_table "issues", :force => true do |t|
    t.column "title", :string
    t.column "description", :text
    t.column "city_id", :integer
    t.column "status", :integer
    t.column "creator_id", :integer
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "measures", :force => true do |t|
    t.column "created_at", :datetime
    t.column "lng", :float
    t.column "lat", :float
    t.column "loudness", :integer
    t.column "track_id", :integer
    t.column "user_id", :integer
    t.column "location", :string
    t.column "geom", :point
    t.column "loudness_index", :integer
    t.column "corrected", :point
    t.column "corrected_distance", :float
    t.column "segment_id", :integer
    t.column "peak", :integer
    t.column "longexposure", :integer
    t.column "ozone", :float
    t.column "temperature", :integer
    t.column "wind", :integer
    t.column "humidity", :integer
    t.column "pressure", :integer
    t.column "time_tagged", :boolean, :default => false
    t.column "location_tagged", :boolean, :default => false
    t.column "useractivity_tagged", :boolean, :default => false
    t.column "loudness_tagged", :boolean, :default => false
    t.column "weather_tagged", :boolean, :default => false
    t.column "tagged", :boolean, :default => false
    t.column "time_index", :integer
    t.column "made_at", :datetime
  end

  add_index "measures", ["geom"], :name => "index_measures_on_geom", :spatial=> true 
  add_index "measures", ["loudness_index"], :name => "index_measures_on_loudness_index"
  add_index "measures", ["time_index"], :name => "index_measures_on_time_index"

  create_table "measures_tags", :force => true do |t|
    t.column "measure_id", :integer
    t.column "tag_id", :integer
  end

  create_table "open_id_authentication_associations", :force => true do |t|
    t.column "issued", :integer
    t.column "lifetime", :integer
    t.column "handle", :string
    t.column "assoc_type", :string
    t.column "server_url", :binary
    t.column "secret", :binary
  end

  create_table "open_id_authentication_nonces", :force => true do |t|
    t.column "timestamp", :integer, :null => false
    t.column "server_url", :string
    t.column "salt", :string, :null => false
  end

  create_table "overlays", :force => true do |t|
    t.column "track_id", :integer
    t.column "loudness_index", :integer
    t.column "url", :string
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "geom", :polygon, :srid => 4326
  end

  add_index "overlays", ["geom"], :name => "index_overlays_on_geom", :spatial=> true 

  create_table "quartier", :force => true do |t|
    t.column "area", :float
    t.column "nsq", :integer
    t.column "nsq_ca", :integer
    t.column "tln", :string
    t.column "ndep", :integer
    t.column "nar", :integer
    t.column "nqu", :integer
    t.column "the_geom", :multi_polygon
  end

  add_index "quartier", ["the_geom"], :name => "index_quartier_on_the_geom", :spatial=> true 

  create_table "roads", :force => true do |t|
    t.column "osm_id", :integer, :limit => 8
    t.column "name", :string, :limit => 32
    t.column "type", :string, :limit => 16
    t.column "oneway", :integer, :limit => 2
    t.column "the_geom", :geometry, :srid => nil
  end

  create_table "segments", :force => true do |t|
    t.column "urban_id", :integer
    t.column "geom", :line_string, :srid => 4326
    t.column "district_id", :integer
  end

  add_index "segments", ["geom"], :name => "index_segments_on_geom", :spatial=> true 

  create_table "sms", :force => true do |t|
    t.column "text", :string
    t.column "location", :string
    t.column "usersms_id", :integer
    t.column "city_id", :integer
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "geom", :point
  end

  create_table "taggings", :force => true do |t|
    t.column "tag_id", :integer
    t.column "taggable_id", :integer
    t.column "tagger_id", :integer
    t.column "tagger_type", :string
    t.column "taggable_type", :string
    t.column "context", :string
    t.column "created_at", :datetime
  end

  add_index "taggings", ["context"], :name => "index_taggings_on_context"
  add_index "taggings", ["tag_id"], :name => "index_taggings_on_tag_id"
  add_index "taggings", ["taggable_id", "taggable_type", "context"], :name => "index_taggings_on_taggable_id_and_taggable_type_and_context"

  create_table "tags", :force => true do |t|
    t.column "name", :string
  end

  create_table "tracks", :force => true do |t|
    t.column "user_id", :integer, :null => false
    t.column "duration", :integer
    t.column "avgloudness", :float
    t.column "minloudness", :float
    t.column "maxloudness", :float
    t.column "ends_at", :datetime
    t.column "devloudness", :float
    t.column "lat", :float
    t.column "lng", :float
    t.column "exposure_id", :integer
    t.column "distance", :float
    t.column "count", :integer
    t.column "public", :boolean
    t.column "processed", :boolean, :default => false
    t.column "image_relative_url", :string
    t.column "city_id", :integer
    t.column "client", :string
    t.column "client_id", :integer
    t.column "client_version", :string
    t.column "device", :string
    t.column "device_model_id", :integer
    t.column "created_at", :datetime
    t.column "geolocated", :boolean, :default => false
    t.column "track_comment", :string
    t.column "graph_image_url", :text
    t.column "geom", :polygon, :srid => 4326
    t.column "location", :text
    t.column "highvariation", :integer, :default => 0
    t.column "highexposure", :integer, :default => 0
    t.column "starts_at", :datetime
  end

  add_index "tracks", ["geom"], :name => "index_tracks_on_geom", :spatial=> true 

  create_table "urbanelements", :force => true do |t|
    t.column "name", :string
    t.column "type", :integer
    t.column "city_id", :integer
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  create_table "users", :force => true do |t|
    t.column "login", :string
    t.column "email", :string
    t.column "crypted_password", :string, :limit => 40
    t.column "salt", :string, :limit => 40
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
    t.column "remember_token", :string
    t.column "remember_token_expires_at", :datetime
    t.column "openid_url", :string
    t.column "image_url", :string
    t.column "public", :boolean, :default => true
    t.column "comment", :text
    t.column "location", :string
    t.column "password_reset_code", :string, :limit => 40
    t.column "phonemodel", :string, :limit => 40
    t.column "device_model_id", :integer
    t.column "city_id", :integer
    t.column "role", :string, :default => "citizen"
    t.column "twitter", :string
    t.column "measures_size", :integer
    t.column "tags_size", :integer
    t.column "daily_graph", :text
    t.column "twitter_acoount", :string
    t.column "twitter_password", :string
    t.column "last_activity_at", :datetime
  end

  create_table "usersms", :force => true do |t|
    t.column "phone", :string
    t.column "created_at", :datetime
    t.column "updated_at", :datetime
  end

  add_index "usersms", ["phone"], :name => "index_usersms_on_phone"

end
