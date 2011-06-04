class TrackAddimage < ActiveRecord::Migration
  def self.up
    add_column :tracks, :image_url,:string
  end

  def self.down
    remove_column :tracks, :image_url
  end
end
