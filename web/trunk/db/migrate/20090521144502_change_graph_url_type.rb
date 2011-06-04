class ChangeGraphUrlType < ActiveRecord::Migration
  def self.up
    change_column :tracks, :graph_image_url, :text
  end

  def self.down
  end
end
