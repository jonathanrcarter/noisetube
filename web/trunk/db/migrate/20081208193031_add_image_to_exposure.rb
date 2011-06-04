class AddImageToExposure < ActiveRecord::Migration
  def self.up
    add_column :exposures, :image_url, :text
  end

  def self.down
    remove_column :exposures, :image_url
  end
end
