class AddMeasure < ActiveRecord::Migration
  def self.up
    add_column :measures, :ozone, :float
    add_column :measures, :temperature, :integer
    add_column :measures, :wind, :integer
    add_column :measures, :humidity, :integer
    add_column :measures, :pressure, :integer
  end

  def self.down
  end
end
