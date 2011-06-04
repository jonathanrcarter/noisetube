class Addurbanasso < ActiveRecord::Migration
  def self.up
    add_column :measures, :urban_id, :integer
  end

  def self.down
  end
end
