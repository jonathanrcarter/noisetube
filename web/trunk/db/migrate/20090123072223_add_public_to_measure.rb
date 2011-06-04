class AddPublicToMeasure < ActiveRecord::Migration
  def self.up
      add_column :measures, :private, :boolean, :null=>false,  :default => false
  end

  def self.down
    remove_column :measures, :private
  end
end
