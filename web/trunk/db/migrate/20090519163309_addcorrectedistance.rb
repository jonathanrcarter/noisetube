class Addcorrectedistance < ActiveRecord::Migration
  def self.up
    add_column :measures, :corrected_distance, :float
  end

  def self.down
  end
end
