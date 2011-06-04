class AddGraphurl < ActiveRecord::Migration
  def self.up
    add_column :tracks, :graph_image_url, :string
  end

  def self.down
  end
end
