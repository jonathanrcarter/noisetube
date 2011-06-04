class Update < ActiveRecord::Migration
  def self.up
  #  remove_column :tracks , :image_url
    add_column :tracks , :distance, :float
  end

  def self.down
   # add_column :tracks , :image_url
    remove_column :tracks , :distance
  end
end
