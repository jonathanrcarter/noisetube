class Addcorrected < ActiveRecord::Migration
  def self.up
    add_column :measures, :corrected, :point
  end

  def self.down
  end
end
