class AddTrackAttributes < ActiveRecord::Migration
  def self.up
    add_column :tracks, :processed, :boolean , :default=>false
    add_column :tracks, :image_relative_url, :string
    add_column :tracks, :city_id, :integer
   
  end

  def self.down
  end
end

