class FixTrackImageurl < ActiveRecord::Migration
  def self.up
    change_column :tracks, :image_url, :text
  end

  def self.down
    change_column :tracks, :image_url, :string
  end
end
